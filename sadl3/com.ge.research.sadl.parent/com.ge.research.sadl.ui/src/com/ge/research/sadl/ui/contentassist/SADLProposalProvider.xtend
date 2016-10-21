/************************************************************************
 * Copyright © 2007-2016 - General Electric Company, All Rights Reserved
 *
 * Project: SADL
 *
 * Description: The Semantic Application Design Language (SADL) is a
 * language for building semantic models and expressing rules that
 * capture additional domain knowledge. The SADL-IDE (integrated
 * development environment) is a set of Eclipse plug-ins that
 * support the editing and testing of semantic models using the
 * SADL language.
 *
 * This software is distributed "AS-IS" without ANY WARRANTIES
 * and licensed under the Eclipse Public License - v 1.0
 * which is available at http://www.eclipse.org/org/documents/epl-v10.php
 *
 ***********************************************************************/
/*
 * generated by Xtext 2.9.0-SNAPSHOT
 */
package com.ge.research.sadl.ui.contentassist

import com.ge.research.sadl.model.DeclarationExtensions
import com.google.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.Assignment
import org.eclipse.xtext.Keyword
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor
import org.eclipse.xtext.RuleCall
import org.eclipse.swt.graphics.Image
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.xtext.CrossReference
import org.eclipse.xtext.resource.IEObjectDescription
import com.google.common.base.Predicates
import org.eclipse.xtext.scoping.IScope
import com.google.common.base.Predicate
import org.eclipse.emf.ecore.EReference
import com.google.common.base.Function
import org.eclipse.xtext.ParserRule
import org.eclipse.xtext.GrammarUtil
import java.util.ArrayList
import java.util.HashMap
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.EcoreUtil2
import com.ge.research.sadl.sADL.SadlModel
import com.ge.research.sadl.sADL.SadlImport
import com.ge.research.sadl.sADL.SadlProperty
import com.ge.research.sadl.model.OntConceptType
import com.ge.research.sadl.sADL.SadlResource
import com.ge.research.sadl.sADL.SubjHasProp
import java.util.List
import com.ge.research.sadl.sADL.Declaration
import com.ge.research.sadl.sADL.PropOfSubject
import com.ge.research.sadl.sADL.Name

/**
 * See https://www.eclipse.org/Xtext/documentation/304_ide_concepts.html#content-assist
 * on how to customize the content assistant.
 */
class SADLProposalProvider extends AbstractSADLProposalProvider {
	@Inject protected DeclarationExtensions declarationExtensions
	
	protected List<OntConceptType> typeRestrictions
	
	override void completeSadlModel_BaseUri(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		var rsrcNm = context.resource.URI.lastSegment
		var proposal = "\"http://sadl.org/" + rsrcNm + "\""
		acceptor.accept(createCompletionProposal(proposal, context))
	}
	
	override void completeSadlModel_Alias(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		var rsrcNm = context.resource.URI.trimFileExtension.lastSegment
		var proposal = rsrcNm
		acceptor.accept(createCompletionProposal(proposal, context))
		
	}
	
	override def void completeSadlImport_ImportedResource(EObject model,  Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		val term = assignment.terminal
		val container = EcoreUtil2.getContainerOfType(model, SadlModel)
		
// When there is a partial statement of the form "import ", cursor at end, the call on the next line results in a NullPointerException and no proposals are generated.		
//		val imports = container.imports.map[importedResource.baseUri].toSet
		
// An alternative for the above is the code below, up to the call to lookupCrossReference, which has sufficient not null checks to work
		val imports = new ArrayList<String>()
		val importsList = container.imports
		if (importsList != null) {
			for (imp:importsList) {
				if (imp != null) {
					val import = imp.importedResource
					if (import != null) {
						val impUri = import.baseUri
						if (impUri != null) {
							imports.add(impUri)
						}
					}
				}
			}
		}

		lookupCrossReference(term as CrossReference, context, acceptor, new Predicate<IEObjectDescription>() {
					override apply(IEObjectDescription input) {
						val fnm = input.EObjectURI.lastSegment
						if (fnm != null && (fnm.toLowerCase().endsWith(".sadl") || 
							fnm.toLowerCase().endsWith(".owl") ||
							 fnm.toLowerCase().endsWith(".n3") || 
							 fnm.toLowerCase().endsWith(".ntriple") ||
							 fnm.toLowerCase().endsWith(".nt")) && 
							!fnm.equals("SadlImplicitModel.sadl") &&
							!imports.contains(input.name.toString)
						) {
							return true;
						}
						return false
					}
					
				})
	}

	// Creates a proposal for an EOS terminal.  Xtext can't guess (at
    // the moment) what the valid values for a terminal rule are, so
    // that's why there is no automatic content assist for EOS.
	override void complete_EOS(EObject model, RuleCall ruleCall, 
	        ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		var proposalText = ".\n";
		var displayText = ". - End of Sentence";
		var image = getImage(model);
		var proposal = createCompletionProposal(proposalText, displayText, image, context);
		acceptor.accept(proposal);
	}

	override void completeKeyword(Keyword keyword, ContentAssistContext context,
			ICompletionProposalAcceptor acceptor) {
		if (!includeKeyword(context)) {
			return
		}
		var proposalText = keyword.getValue();
		if (isInvokedDirectlyAfterKeyword(context) && requireSpaceBefore(keyword, context) && !hasSpaceBefore(context)) {
			proposalText = " " + proposalText;
		}
		if (requireSpaceAfter(keyword, context) && !hasSpaceAfter(context)) {
			proposalText = proposalText + " ";
		}
	
		var proposal = createCompletionProposal(proposalText, getKeywordDisplayString(keyword),
				getImage(keyword), context);
		getPriorityHelper().adjustKeywordPriority(proposal, context.getPrefix());
		acceptor.accept(proposal);
	}
	
	def includeKeyword(ContentAssistContext context) {
		var model = context.currentModel
		if (model == null) {
			model = context.previousModel
		}
		if (model != null) {
			if (model instanceof Declaration) { return false}
			val container = model.eContainer
			if (container instanceof SadlProperty) {
				return false
			}
		}
		return true
	}
	
//	// this is without filtering out duplicates
//	override void lookupCrossReference(CrossReference crossReference, ContentAssistContext context,
//			ICompletionProposalAcceptor acceptor) {
//		lookupCrossReference(crossReference, context, acceptor,
//				Predicates.<IEObjectDescription> alwaysTrue());
//	}

	// this is with filtering out of duplicates
	/**
	 * Method to lookup cross reference but eliminating duplicates. A given SadlResource will appear twice, 
	 * once as an unqualified name, e.g., "foo" and once as a qualified name, e.g., "ns1:foo".
	 * If multiple imports contain a SadlResource with the same localname but in different namespaces, 
	 * then there will be a "foo", "ns1:foo" in one import and a "foo", "ns2:foo" in another. If this
	 * happens then we want to include the qualified name (apply return true) otherwise the unqualified name.
	 */
	override void lookupCrossReference(CrossReference crossReference, ContentAssistContext context,
				ICompletionProposalAcceptor acceptor) {
		val criterable = getFilteredCrossReferenceList(crossReference, context)		//Iterable<IEObjectDescription>
		val itr = criterable.iterator
				
		val pm = context.previousModel
		if (pm != null) {
			if (pm instanceof Declaration) {
				val declcontainer = (pm as Declaration).eContainer
				if (declcontainer != null && declcontainer instanceof SubjHasProp) {
					val sadlprop = (declcontainer as SubjHasProp).prop
					restrictTypeToClass(sadlprop)
				}
			}
			else if (pm instanceof SubjHasProp) {
				restrictTypeToAllPropertyTypes
			}
			else if (pm instanceof PropOfSubject && (pm as PropOfSubject).left != null) {
				val prop = (pm as PropOfSubject).left
				if (prop instanceof Name) {
					restrictTypeToClassPlusVars((prop as Name).name)
				}
				else if (prop instanceof SadlResource) {
					restrictTypeToClassPlusVars(prop as SadlResource)	
				}
			}
			else {
				val container = pm.eContainer
				if (container instanceof SubjHasProp) {
					restrictTypeToAllPropertyTypes
				}
				else if (container instanceof SadlProperty) {
					val propsr = (container as SadlProperty).nameOrRef
					restrictTypeToClass(propsr)
				}
			}
		}
		
		val nmMap = new HashMap<String, QualifiedName>		// a map of qualified names with simple name as key, qualified name as value
		val qnmList = new ArrayList<QualifiedName>
		val eliminatedNames = new ArrayList<String>			// simple names of SadlReferences that require a qualified name
		try {
			if (!itr.empty) {
				while (!itr.empty) {
					val nxt = itr.next
					if (nxt.qualifiedName.segmentCount > 1) {
						val nm = nxt.qualifiedName.lastSegment
						if (nmMap.containsKey(nm) && !nxt.qualifiedName.equals(nmMap.get(nm))) {
							// we already have a qname with the same local name 
							qnmList.add(nxt.qualifiedName)
							if (!qnmList.contains(nmMap.get(nm))) {
								qnmList.add(nmMap.get(nm))
							}
							eliminatedNames.add(nxt.qualifiedName.lastSegment)
						}
						else {
							nmMap.put(nxt.name.lastSegment, nxt.qualifiedName)
						}
					}
				}			
			}
		}
		catch (Throwable t) {
			t.printStackTrace
		}
		
		lookupCrossReference(crossReference, context, acceptor,new Predicate<IEObjectDescription>() {
				override apply(IEObjectDescription input) {
					if (typeRestrictions != null && typeRestrictions.size > 0) {
						val element = input.EObjectOrProxy
						if (element instanceof SadlResource) {
							val eltype = declarationExtensions.getOntConceptType(element as SadlResource)
							if (!typeRestrictions.contains(eltype)) {
								return false;
							}
						}
					}
					val isQName = input.qualifiedName.segmentCount > 1
					if (isQName) {
						if (qnmList.contains(input.name)) {
							// qnmList only contains qualified names that are ambiguous so return true for this qualified name
							return true
						}
						else {
							return false
						}
					}
					val nm = input.name.lastSegment
					if (eliminatedNames.contains(nm)) {
						return false;
					}
					return true;
				}
			})
	}
	
	def restrictTypeToClassPlusVars(SadlResource resource) {
		restrictTypeToClass(resource)
		if (typeRestrictions != null) {
			typeRestrictions.add(OntConceptType.VARIABLE)
		}
		else {
			val typeList = new ArrayList<OntConceptType>
			typeList.add(OntConceptType.VARIABLE)
			typeRestrictions = typeList
		}
	}
	
	def restrictTypeToClass(SadlResource propsr) {
		// only classes in the domain of the property
		if (propsr != null) {
			// for now just filter to classes
			val typeList = new ArrayList<OntConceptType>
			typeList.add(OntConceptType.CLASS)
			typeRestrictions = typeList
		}
	}
	
	def restrictTypeToAllPropertyTypes() {
				val typeList = new ArrayList<OntConceptType>
				typeList.add(OntConceptType.ANNOTATION_PROPERTY)
				typeList.add(OntConceptType.CLASS_PROPERTY)
				typeList.add(OntConceptType.DATATYPE_PROPERTY)
				typeList.add(OntConceptType.RDF_PROPERTY)
				typeRestrictions = typeList
	}
	
	def getFilteredCrossReferenceList(CrossReference crossReference, ContentAssistContext context) {
		val containingParserRule = GrammarUtil.containingParserRule(crossReference);	// ParserRule
		if (!GrammarUtil.isDatatypeRule(containingParserRule)) {
			if (containingParserRule.isWildcard()) {
				// TODO we need better ctrl flow analysis here
				// The cross reference may come from another parser rule then the current model 
				val ref = GrammarUtil.getReference(crossReference, context.getCurrentModel().eClass());
				if (ref != null) {
					val scope = getScopeProvider().getScope(context.currentModel, ref) as IScope;	//IScope
					return scope.allElements
				}
			} else {
				val ref = GrammarUtil.getReference(crossReference);
				if (ref != null) {
					val scope = getScopeProvider().getScope(context.currentModel, ref) as IScope;	//IScope
					return scope.allElements
				}
			}
		}
		return null
	}
	
	def isInvokedDirectlyAfterKeyword (ContentAssistContext context) {
		return context.getLastCompleteNode().getTotalEndOffset()==context.getOffset();
	}
	
	def requireSpaceBefore (Keyword keyword, ContentAssistContext context) {
//		if (!keywordsWithSpaceBefore.contains(keyword)) {
//			return false;
//		}
		return true;
	}
	
	def hasSpaceBefore (ContentAssistContext context) {
		//TODO: Detect space before invocation offset
		return false;
	}
	
	def requireSpaceAfter (Keyword keyword, ContentAssistContext context) {
//		if (!keywordsWithSpaceAfter.contains(keyword)) {
//			return false;
//		}
		return true;
	}
	
	def boolean hasSpaceAfter (ContentAssistContext context) {
		if (!context.getCurrentNode().hasNextSibling()) return false; // EOF
		//TODO: Detect space after invocation offset
		return false;
	}
	
}
