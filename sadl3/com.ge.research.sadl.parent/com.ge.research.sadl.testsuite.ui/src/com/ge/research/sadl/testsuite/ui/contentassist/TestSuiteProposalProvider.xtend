/*
 * generated by Xtext 2.11.0.RC2
 */
package com.ge.research.sadl.testsuite.ui.contentassist

import com.ge.research.sadl.testsuite.testSuite.TestModel
import java.util.Collection
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.Assignment
import org.eclipse.xtext.CrossReference
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.RuleCall
import org.eclipse.xtext.resource.IEObjectDescription
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor

/**
 * See https://www.eclipse.org/Xtext/documentation/304_ide_concepts.html#content-assist
 * on how to customize the content assistant.
 */
class TestSuiteProposalProvider extends AbstractTestSuiteProposalProvider {

	protected static val SUPPORTED_FILE_EXTENSION = #{'sadl', 'n3', 'owl', 'ntriple', 'nt'};
	protected static val BUILTIN_FILES = #{'SadlImplicitModel.sadl', 'SadlBuiltinFunctions.sadl'};

	override void complete_TestModel(EObject model, RuleCall ruleCall, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		if (model instanceof TestModel) {
			val i = 0
		}
	}

	override void complete_Test(EObject model, RuleCall ruleCall, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		if (model instanceof TestModel) {
			val i = 0;
		}
	}
	
	protected def Collection<String> getBuiltinFiles() {
		return BUILTIN_FILES;
	}

	protected def Collection<String> getSupporedFileExtensions() {
		return SUPPORTED_FILE_EXTENSION;
	}

	protected def boolean canBeImported(IEObjectDescription it, Collection<String> alreadyImportedFiles) {
		val fileExtension = EObjectURI?.fileExtension;
		val fileName = EObjectURI?.lastSegment;
		return supporedFileExtensions.contains(fileExtension) && !builtinFiles.contains(fileName) &&
			!alreadyImportedFiles.contains(name?.toString);
	}

	override void completeTest_TestResource(EObject model, Assignment assignment,
		ContentAssistContext context, ICompletionProposalAcceptor acceptor) {

		val term = assignment.terminal;
		val testModel = EcoreUtil2.getContainerOfType(model, TestModel);
		if (testModel !== null) {
			val imports = testModel.tests.map[testResource].filterNull.map[baseUri].filterNull.toSet;
			lookupCrossReference(term as CrossReference, context, acceptor) [
				return canBeImported(it, imports);
			];
		}
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

}
