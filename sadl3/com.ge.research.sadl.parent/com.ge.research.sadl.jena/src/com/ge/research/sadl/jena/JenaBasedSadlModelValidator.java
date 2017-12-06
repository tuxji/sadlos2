package com.ge.research.sadl.jena;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.xtext.diagnostics.Severity;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.errorgenerator.generator.SadlErrorMessages;
import com.ge.research.sadl.model.CircularDefinitionException;
import com.ge.research.sadl.model.ConceptIdentifier;
import com.ge.research.sadl.model.ConceptName;
import com.ge.research.sadl.model.ConceptName.ConceptType;
import com.ge.research.sadl.model.ConceptName.RangeValueType;
import com.ge.research.sadl.model.DeclarationExtensions;
import com.ge.research.sadl.model.OntConceptType;
import com.ge.research.sadl.model.PrefixNotFoundException;
import com.ge.research.sadl.model.SadlUnionClass;
import com.ge.research.sadl.model.gp.Junction;
import com.ge.research.sadl.model.gp.NamedNode;
import com.ge.research.sadl.model.gp.NamedNode.NodeType;
import com.ge.research.sadl.model.gp.Node;
import com.ge.research.sadl.model.gp.ProxyNode;
import com.ge.research.sadl.model.gp.Rule;
import com.ge.research.sadl.model.gp.VariableNode;
import com.ge.research.sadl.processing.ISadlModelValidator;
import com.ge.research.sadl.processing.SadlConstants;
import com.ge.research.sadl.processing.SadlModelProcessor;
import com.ge.research.sadl.processing.ValidationAcceptor;
import com.ge.research.sadl.processing.SadlModelProcessor.RulePart;
import com.ge.research.sadl.reasoner.CircularDependencyException;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.ITranslator;
import com.ge.research.sadl.reasoner.InvalidNameException;
import com.ge.research.sadl.reasoner.InvalidTypeException;
import com.ge.research.sadl.reasoner.TranslationException;
import com.ge.research.sadl.reasoner.utils.SadlUtils;
import com.ge.research.sadl.sADL.AskExpression;
import com.ge.research.sadl.sADL.BinaryOperation;
import com.ge.research.sadl.sADL.BooleanLiteral;
import com.ge.research.sadl.sADL.Constant;
import com.ge.research.sadl.sADL.ConstructExpression;
import com.ge.research.sadl.sADL.Declaration;
import com.ge.research.sadl.sADL.ElementInList;
import com.ge.research.sadl.sADL.EquationStatement;
import com.ge.research.sadl.sADL.Expression;
import com.ge.research.sadl.sADL.ExternalEquationStatement;
import com.ge.research.sadl.sADL.Name;
import com.ge.research.sadl.sADL.NumberLiteral;
import com.ge.research.sadl.sADL.PropOfSubject;
import com.ge.research.sadl.sADL.QueryStatement;
import com.ge.research.sadl.sADL.SadlBooleanLiteral;
import com.ge.research.sadl.sADL.SadlClassOrPropertyDeclaration;
import com.ge.research.sadl.sADL.SadlConstantLiteral;
import com.ge.research.sadl.sADL.SadlDataType;
import com.ge.research.sadl.sADL.SadlInstance;
import com.ge.research.sadl.sADL.SadlIntersectionType;
import com.ge.research.sadl.sADL.SadlModel;
import com.ge.research.sadl.sADL.SadlModelElement;
import com.ge.research.sadl.sADL.SadlMustBeOneOf;
import com.ge.research.sadl.sADL.SadlNestedInstance;
import com.ge.research.sadl.sADL.SadlNumberLiteral;
import com.ge.research.sadl.sADL.SadlParameterDeclaration;
import com.ge.research.sadl.sADL.SadlPrimitiveDataType;
import com.ge.research.sadl.sADL.SadlProperty;
import com.ge.research.sadl.sADL.SadlPropertyCondition;
import com.ge.research.sadl.sADL.SadlPropertyInitializer;
import com.ge.research.sadl.sADL.SadlResource;
import com.ge.research.sadl.sADL.SadlSimpleTypeReference;
import com.ge.research.sadl.sADL.SadlStringLiteral;
import com.ge.research.sadl.sADL.SadlTypeReference;
import com.ge.research.sadl.sADL.SadlUnaryExpression;
import com.ge.research.sadl.sADL.SadlUnionType;
import com.ge.research.sadl.sADL.SelectExpression;
import com.ge.research.sadl.sADL.StringLiteral;
import com.ge.research.sadl.sADL.SubjHasProp;
import com.ge.research.sadl.sADL.Sublist;
import com.ge.research.sadl.sADL.TestStatement;
import com.ge.research.sadl.sADL.UnaryExpression;
import com.ge.research.sadl.sADL.UnitExpression;
import com.ge.research.sadl.sADL.ValueTable;
import com.ge.research.sadl.sADL.impl.ExternalEquationStatementImpl;
import com.ge.research.sadl.sADL.impl.NumberLiteralImpl;
import com.ge.research.sadl.sADL.impl.TestStatementImpl;
import com.ge.research.sadl.utils.SadlASTUtils;
import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.ontology.AllValuesFromRestriction;
import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.IntersectionClass;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public class JenaBasedSadlModelValidator implements ISadlModelValidator {
	private static final int MIN_INT = -2147483648;
	private static final int MAX_INT = 2147483647;
	private static final long MIN_LONG = -9223372036854775808L;
	private static final long MAX_LONG = 9223372036854775807L;
	protected ValidationAcceptor issueAcceptor = null;
	protected OntModel theJenaModel = null;
	protected DeclarationExtensions declarationExtensions = null;
	private EObject defaultContext;
	
	protected Map<EObject, TypeCheckInfo> expressionsValidated = new HashMap<EObject,TypeCheckInfo>();
	private Map<EObject, Property> impliedPropertiesUsed = null;
	
	private IMetricsProcessor metricsProcessor = null;
	protected JenaBasedSadlModelProcessor modelProcessor = null;
	private List<ConceptName> binaryOpLeftImpliedProperties;
	private List<ConceptName> binaryOpRightImpliedProperties;
	protected Object lastSuperCallExpression = null;
	private List<EObject> eObjectsValidated = null; 

   	public enum ExplicitValueType {RESTRICTION, VALUE}
   	
   	public enum ImplicitPropertySide {LEFT, RIGHT, NONE}

	/**
	 * This inner class captures the information about the left or right hand side of an expression that is subject
	 * to type checking, e.g., and assignment, a comparison, etc.
	 * 
	 */
	public class TypeCheckInfo {
		private EObject context = null;							// the parsetree element from which this is derived, 
																//	used to add an error, warning, or info so that a marker can be placed in the editor
    	private ConceptIdentifier expressionType = null;		// the identity, type, etc. of the concept that determines the type, 
    															//	e.g., the property of a "<property> of <subject>" expression
    	private Node typeCheckType = null;						// the type of the TypeCheckInfo which must match the other side of the expression,
    															//	e.g., the range of the property of a "<property> of <subject>" expression
    															// Note: this is a ProxyNode can be used for Unions and Intersections
    	private RDFNode explicitValue = null;					// the explicit value that is allowed, as in a hasValue restriction
    	private ExplicitValueType explicitValueType;			// The type of the explicit value
     	private RangeValueType rangeValueType = RangeValueType.CLASS_OR_DT;	
    															// the range type, one of RangeValueType.CLASS_OR_DT (Class or RDFDataType)
    															//	or LIST (a subclass of http://sadl.org/sadllistmodel#List)

     	private List<ConceptName> implicitProperties = null;	// Implied properties, if any, that apply to this expressionType
 
    	private List<TypeCheckInfo> compoundTypes = null;		// If this is a disjunction of multiple types, this contains the next
    															//	lower level of TypeCheckInfos in the hierarchy, e.g., a property whose
    															//	range is a union of classes or which is given range in multiple imports
    	
    	private String typeToExprRelationship = "range";		// the relationship between the typeCheckType and the expressionType, e.g., range (the default)
    															//	for explicit UnittedQuantity this will be the units 
    	private String additionalInformation = null;			// any additional information to explain the contents of the instance of the class.
    	private Severity severity = null;						// Guidance offered on the severity of any type mismatch involving this instance of the class.
    	
    	public TypeCheckInfo(ConceptIdentifier eType) {
    		setExpressionType(eType);
    	}
    	
    	/* Constructor for compound types (union, e.g. range) */
    	public TypeCheckInfo(ConceptIdentifier eType, JenaBasedSadlModelValidator validator, EObject ctx) {
    		setExpressionType(eType);
    		setContext(validator, ctx);
    		if (ctx != null && this.getTypeCheckType() != null) {
    			validator.expressionsValidated.put(ctx,  this);
    		}
    	}
    	
    	public TypeCheckInfo(ConceptIdentifier eType, Node tcType, JenaBasedSadlModelValidator validator, EObject ctx) {
    		setExpressionType(eType);
    		setTypeCheckType(tcType);
    		setContext(validator, ctx);
    		if (ctx != null) {
    			validator.expressionsValidated.put(ctx,  this);
    		}
    	}
    	 	
		public TypeCheckInfo(ConceptIdentifier eType, Node tcType, JenaBasedSadlModelValidator validator, List<ConceptName> impliedProps, EObject ctx) {
			this(eType, tcType, validator, ctx);
    		if (impliedProps != null) {
   				implicitProperties = impliedProps;
    		}
		}

    	public TypeCheckInfo(ConceptName eType, RDFNode valueRestriction, ExplicitValueType valueType, JenaBasedSadlModelValidator validator, EObject ctx) {
    		setExpressionType(eType);
    		setExplicitValueType(valueType);
    		setContext(validator, ctx);
    		if (ctx != null && this.getTypeCheckType() != null) {
    			validator.expressionsValidated.put(ctx,  this);
    		}
    		setExplicitValue(valueRestriction);
    		if (valueType.equals(ExplicitValueType.RESTRICTION)) {
    			setTypeToExprRelationship("restriction to");
    		}
    		else {
    			setTypeToExprRelationship("explicit value");
    		}
		}

    	public TypeCheckInfo(ConceptIdentifier eType, Node tcType, List<ConceptName> impliedProps, JenaBasedSadlModelValidator validator, EObject ctx) {
    		setExpressionType(eType);
    		setTypeCheckType(tcType);
			implicitProperties = impliedProps;
    		setContext(validator, ctx);
    		if (ctx != null) {
    			validator.expressionsValidated.put(ctx,  this);
    		}
    	}
    	
		public boolean equals(Object o) {
			if (o instanceof TypeCheckInfo) {
				TypeCheckInfo other = (TypeCheckInfo) o;
				return context == other.context // Identity check should be fine for EObjects
					&& Objects.equals(getExpressionType(), other.getExpressionType())
					&& Objects.equals(getRangeValueType(), other.getRangeValueType())
					&& Objects.equals(getTypeCheckType(), other.getTypeCheckType());
				
			}
    		return false;
    	}
		
		@Override
		public int hashCode() {
			return Objects.hash(context, getExpressionType(), getRangeValueType(), getTypeCheckType());
		}

		public ConceptIdentifier getExpressionType() {
			return expressionType;
		}

		public void setExpressionType(ConceptIdentifier expressionType) {
			this.expressionType = expressionType;
		}

		public Node getTypeCheckType() {
			return typeCheckType;
		}

		public void setTypeCheckType(Node typeCheckType) {
			this.typeCheckType = typeCheckType;
			if (typeCheckType instanceof NamedNode && ((NamedNode)typeCheckType).isList()) {
				setRangeValueType(RangeValueType.LIST);
			}
		}
		
		public RangeValueType getRangeValueType() {
			if(this.getCompoundTypes() != null){
				return getCompoundRangeValueType(this);
			}else{
				return rangeValueType;
			}
		}
		
		private RangeValueType getCompoundRangeValueType(TypeCheckInfo tci){
			List<TypeCheckInfo> types = tci.getCompoundTypes();
			Iterator<TypeCheckInfo> iter = types.iterator();
			RangeValueType rvt = null;
			while(iter.hasNext()){
				TypeCheckInfo type = iter.next();
				RangeValueType rvt2 = null;
				if(type.getCompoundTypes() != null){
					rvt2 = getCompoundRangeValueType(type);
				}else{
					rvt2 = type.getRangeValueType();
				}
				
				if(rvt != null){
					if(rvt != rvt2){
						issueAcceptor.addError("Property '" + tci.getExpressionType() + "' has incompatable Range Types, '" + rvt.toString() + "' and '" + rvt2.toString() + "'", tci.context); //TODO add new error message
					}
				}else{
					rvt = rvt2;
				}
			}
			
			return rvt;
		}
		
		protected void setContext(JenaBasedSadlModelValidator validator, EObject ctx) {
			this.context = ctx;
			if (ctx != null) {
				validator.expressionsValidated.put(ctx, this);
			}
		}

		public void setRangeValueType(RangeValueType rangeValueType) {
			this.rangeValueType = rangeValueType;
		}
		
		public String toString() {
			if (compoundTypes != null) {
				StringBuffer sb = new StringBuffer("Compound TypeCheckInfo([");
				for (int i = 0; i < compoundTypes.size(); i++) {
					if (i > 0) {
						sb.append(",");
					}
					sb.append(compoundTypes.get(i).toString());
				}
				sb.append("]");
				return sb.toString();
			}
			else {
				StringBuffer sb = new StringBuffer("TypeCheckInfo(");
				if (getRangeValueType() != null && !getRangeValueType().equals(RangeValueType.CLASS_OR_DT)) {
					sb.append(getRangeValueType().toString());
					sb.append(" of values of type, ");
				}
				sb.append(expressionType.toString());
				sb.append(", ");
				sb.append(typeCheckType != null ? typeCheckType.toString() : "unknown type");
				if (getExplicitValue() != null) {
					if (getExplicitValueType().equals(ExplicitValueType.RESTRICTION)) {
						sb.append(", restricted to explicit value '");
					}
					else {
						sb.append(", is the explicit value '");
					}
					sb.append(getExplicitValue().toString());
					sb.append("'");
				}
				sb.append(")");
				if (getImplicitProperties() != null) {
					if (getImplicitProperties().size() > 1)
						sb.append(" (has implied properties ");
					else 
						sb.append(" (has implied property ");
					sb.append(implicitPropertiesToString(getImplicitProperties()));
					sb.append(")");
				}
				return sb.toString();
			}
		}
		
		private Object implicitPropertiesToString(List<ConceptName> iprops) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < iprops.size(); i++) {
				if (i > 0) sb.append(", ");
				sb.append(getImplicitProperties().get(i).toFQString());
			}
			return sb.toString();
		}

		public void addImplicitProperty(ConceptName implicitProp) {
			if (implicitProp != null) {
				if (implicitProperties == null) {
					implicitProperties = new ArrayList<ConceptName>();
				}
				if (!implicitProperties.contains(implicitProp)) {
					implicitProperties.add(implicitProp);
				}
			}
		}
		
		public void addImplicitProperties(List<ConceptName> implicitProps) {
			if (implicitProperties == null) {
				implicitProperties = implicitProps;
			}
			else {
				implicitProperties.addAll(implicitProps);
			}
		}
		
		public List<ConceptName> getImplicitProperties() {
			return implicitProperties;
		}

		public List<TypeCheckInfo> getCompoundTypes() {
			return compoundTypes;
		}

		private void addCompoundType(TypeCheckInfo additionalType) {
			if (compoundTypes == null) {
				compoundTypes= new ArrayList<TypeCheckInfo>();
			}
			if (!compoundTypes.contains(additionalType)) {
				compoundTypes.add(additionalType);
			}
		}

		private String getTypeToExprRelationship() {
			return typeToExprRelationship;
		}

		public void setTypeToExprRelationship(String typeToExprRelationship) {
			this.typeToExprRelationship = typeToExprRelationship;
		}

		public RDFNode getExplicitValue() {
			return explicitValue;
		}

		public void setExplicitValue(RDFNode explicitValue) {
			this.explicitValue = explicitValue;
		}

		public ExplicitValueType getExplicitValueType() {
			return explicitValueType;
		}

		public void setExplicitValueType(ExplicitValueType explicitValueType) {
			this.explicitValueType = explicitValueType;
		}

		private String getAdditionalInformation() {
			return additionalInformation;
		}

		private void setAdditionalInformation(String additionalInformation) {
			this.additionalInformation = additionalInformation;
		}

		public Severity getSeverity() {
			return severity;
		}

		public void setSeverity(Severity severity) {
			this.severity = severity;
		}
    }
	
	public JenaBasedSadlModelValidator(ValidationAcceptor issueAcceptor, OntModel theJenaModel, JenaBasedSadlModelProcessor processor) {
		this(issueAcceptor, theJenaModel, new DeclarationExtensions(), processor, null);
	}
	
	public JenaBasedSadlModelValidator(ValidationAcceptor issueAcceptor, OntModel theJenaModel, DeclarationExtensions declarationExtensions, JenaBasedSadlModelProcessor processor, IMetricsProcessor metricsProcessor){
		this.issueAcceptor = issueAcceptor;
		this.theJenaModel = theJenaModel;
		this.declarationExtensions = declarationExtensions;
		this.setModelProcessor(processor) ;
		this.metricsProcessor  = metricsProcessor;
	}
	
	public boolean validate(Expression expr, String xsdType, String op, StringBuilder errorMessageBuilder) {
		List<String> operations = Arrays.asList(op.split("\\s+"));
		TypeCheckInfo exprTypeCheckInfo;
		try {
			exprTypeCheckInfo = getType(expr);
			NamedNode tctype = new NamedNode(XSD.xint.getURI(), NodeType.DataTypeNode);
			ConceptName numberLiteralConceptName = getModelProcessor().namedNodeToConceptName(tctype);
			numberLiteralConceptName.setType(ConceptType.RDFDATATYPE);
			TypeCheckInfo xsdTypeCheckInfo =  new TypeCheckInfo(numberLiteralConceptName, tctype, this, null);				
			if(!compareTypes(operations, expr, null, exprTypeCheckInfo, xsdTypeCheckInfo, ImplicitPropertySide.NONE)){
				if (createErrorMessage(errorMessageBuilder, exprTypeCheckInfo, xsdTypeCheckInfo, op, true, expr)) {
					return false;
				}
			}
		} catch (Throwable t) {
			return handleValidationException(expr, t);
		}
		return true;
	}
	
	public boolean validate(BinaryOperation expression, StringBuilder errorMessageBuilder) {
		setDefaultContext(expression);
		Expression leftExpression = expression.getLeft();
		Expression rightExpression = expression.getRight();
		String op = expression.getOp();
		
		return validateBinaryOperationByParts(expression, leftExpression, rightExpression, op,
				errorMessageBuilder);
	}

	public boolean validateBinaryOperationByParts(EObject expression, Expression leftExpression,
			Expression rightExpression, String op, StringBuilder errorMessageBuilder) {
		List<String> operations = Arrays.asList(op.split("\\s+"));
		boolean errorsFound = false;
		if (!registerEObjectValidateCalled(expression)) {
			// if there were errors they were reported on the first call
			return !errorsFound;
		}
		
		if(skipOperations(operations)){
			return true;
		}
		if (skipNonCheckedExpressions(leftExpression, rightExpression)) {
			return true;
		}
		if (!passLiteralConstantComparisonCheck(expression, leftExpression, rightExpression, op, errorMessageBuilder)) {
			errorsFound = true;
		}
		try {	
			boolean dontTypeCheck = false;
			TypeCheckInfo leftTypeCheckInfo = null;
			try {
				leftTypeCheckInfo = getType(leftExpression);
				if (getModelProcessor().isConjunction(op)) {
					// this can be treated as a boolean only (maybe even larger criteria?)
					leftTypeCheckInfo = createBooleanTypeCheckInfo(leftExpression);						
				}
			} catch (DontTypeCheckException e) {
				dontTypeCheck = true;
			}
			if (useImpliedProperties(op)) {
				setBinaryOpLeftImpliedProperties(leftTypeCheckInfo != null ? leftTypeCheckInfo.getImplicitProperties() : null);
			}
			
			TypeCheckInfo rightTypeCheckInfo = null;
			try {
				rightTypeCheckInfo = getType(rightExpression);
				if (getModelProcessor().isConjunction(op)) {
					// this can be treated as a boolean only (maybe even larger criteria?)
					rightTypeCheckInfo = createBooleanTypeCheckInfo(leftExpression);
				}
			} catch (DontTypeCheckException e) {
				dontTypeCheck = true;
			}
			if (useImpliedProperties(op)) {
				setBinaryOpRightImpliedProperties(rightTypeCheckInfo != null ? rightTypeCheckInfo.getImplicitProperties() : null);
			}
			
			if (leftTypeCheckInfo == null && rightTypeCheckInfo == null) {
				// this condition happens when a file is loaded in the editor and clean/build is invoked
				return true;
			}
			if (modelProcessor.isComparisonOperator(op) && (rightExpression instanceof NumberLiteral || 
					rightExpression instanceof UnaryExpression && ((UnaryExpression)rightExpression).getExpr() instanceof NumberLiteral)) {
				checkNumericRangeLimits(op, leftTypeCheckInfo, rightTypeCheckInfo);
			}
			if(!dontTypeCheck && !compareTypes(operations, leftExpression, rightExpression, leftTypeCheckInfo, rightTypeCheckInfo, ImplicitPropertySide.NONE)){
				if (expression.eContainer() instanceof TestStatement && isQuery(leftExpression)) {
					// you can't tell what type a query will return
					return !errorsFound;
				}
				if (!rulePremiseVariableAssignment(operations, leftTypeCheckInfo,rightTypeCheckInfo)) {
					String effectiveOp = op;
					if (leftExpression instanceof Constant && ((Constant)leftExpression).getConstant().equals("value")) {
						effectiveOp = "matching value";
//						leftTypeCheckInfo.setRangeValueType(RangeValueType.CLASS_OR_DT);
					}
					if (createErrorMessage(errorMessageBuilder, leftTypeCheckInfo, rightTypeCheckInfo, effectiveOp, false, null)) {
						return false;
					}
				}
			}
			//It's possible there may be a local type restriction
			handleLocalRestriction(leftExpression,rightExpression, (leftTypeCheckInfo != null ? leftTypeCheckInfo : leftTypeCheckInfo),
					(rightTypeCheckInfo != null ? rightTypeCheckInfo : rightTypeCheckInfo));
			return !errorsFound;
		} catch (Throwable t) {
			return handleValidationException(expression, t);
		}
	}
	
	private boolean registerEObjectValidateCalled(EObject expression) {
		if (expression == null) return true;	// for ease in debugging--set expression to null to allow multiple passes in debugger
		if (eObjectsValidated  == null) {
			eObjectsValidated = new ArrayList<EObject>();
			eObjectsValidated.add(expression);
			return true;
		}
		if (eObjectsValidated.contains(expression)) {
			return false;
		}
		eObjectsValidated.add(expression);
		return true;
	}

	private boolean passLiteralConstantComparisonCheck(EObject expression, Expression leftExpression,
			Expression rightExpression, String op, StringBuilder errorMessageBuilder) {
		if (modelProcessor.canBeNumericOperator(op)) {
			if (isLiteralOrConstant(leftExpression) && isLiteralOrConstant(rightExpression)) {
				errorMessageBuilder.append(SadlErrorMessages.COMPARISON_LITERALS_CONSTANTS.get(op));
				return false;
			}
		}
		return true;
	}

	private boolean isLiteralOrConstant(Expression expr) {
		if ((expr instanceof Constant && 
				(((Constant)expr).getConstant().equals("PI") || ((Constant)expr).getConstant().equals("e"))) 
				|| expr instanceof NumberLiteral) {
			return true;
		}
		return false;
	}

	private boolean skipNonCheckedExpressions(Expression leftExpression, Expression rightExpression) {
		Expression rExpr = rightExpression;
		if (rightExpression instanceof UnaryExpression && ((UnaryExpression)rightExpression).getOp().equals("not")) {
			rExpr = ((UnaryExpression)rightExpression).getExpr();
		}
		if (rExpr instanceof Constant && ((Constant)rExpr).getConstant().equals("known")) {
			return true;
		}
		return false;
	}

	protected void handleLocalRestriction(Expression leftExpression, Expression rightExpression, TypeCheckInfo leftTypeCheckInfo, TypeCheckInfo rightTypeCheckInfo) throws InvalidNameException, TranslationException, URISyntaxException, IOException, ConfigurationException, DontTypeCheckException, CircularDefinitionException, InvalidTypeException, CircularDependencyException, PropertyWithoutRangeException {
		if (leftExpression instanceof PropOfSubject && rightExpression instanceof Declaration) {
			TypeCheckInfo subjtype = getType(((PropOfSubject)leftExpression).getRight());
			Node subject = subjtype.getTypeCheckType();
			if (subject != null) {
				String key = generateLocalRestrictionKey(getModelProcessor().processExpression(leftExpression));
				addLocalRestriction(key, leftTypeCheckInfo, rightTypeCheckInfo, leftExpression.eContainer());
			}
		}
	}

	private boolean useImpliedProperties(String op) {
		if (op.equals("contains") || op.equals("does not contain")) {
			return false;
		}
		return true;
	}

	private boolean rulePremiseVariableAssignment(List<String> operations, TypeCheckInfo leftTypeCheckInfo, TypeCheckInfo rightTypeCheckInfo) throws InvalidTypeException {
		if (possibleAssignment(operations)) {
			if (getModelProcessor().getRulePart().equals(SadlModelProcessor.RulePart.PREMISE)) {
				if (isVariable(leftTypeCheckInfo)) {
					if (!isAlreadyReferenced(leftTypeCheckInfo.getExpressionType())) {
						return true;
					}
				}
				else if (isVariable(rightTypeCheckInfo)) {
					if (!isAlreadyReferenced(rightTypeCheckInfo.getExpressionType())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean isAlreadyReferenced(ConceptIdentifier expressionType) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean possibleAssignment(List<String> operations) {
		if (operations.contains("is") || operations.contains("=")) {
			return true;
		}
		return false;
	}

	private boolean isQuery(Expression expr) {
		if (expr instanceof StringLiteral) {
			String val = ((StringLiteral)expr).getValue();
			if (val.contains("select") && val.contains("where")) {
				return true;
			}
		}
		return false;
	}

	public void addLocalRestriction(String subjuri, TypeCheckInfo leftTypeCheckInfo, TypeCheckInfo rightTypeCheckInfo, EObject ref) throws InvalidTypeException {
		try {
			throw new Exception("addLocalRestriction not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected boolean handleValidationException(EObject expr, Throwable t) {
		try {
			if (t instanceof InvalidNameException) {
				getModelProcessor().addIssueToAcceptor(SadlErrorMessages.TYPE_CHECK_EXCEPTION.get("Invalid Name"), expr);
				if (metricsProcessor != null) {
					metricsProcessor.addMarker(null, MetricsProcessor.ERROR_MARKER_URI, MetricsProcessor.TYPE_CHECK_FAILURE_URI);
				}
				t.printStackTrace();
			} else if (t instanceof TranslationException) {
				getModelProcessor().addIssueToAcceptor(SadlErrorMessages.TYPE_CHECK_EXCEPTION.get("Translation"), expr);
				if (metricsProcessor != null) {
					metricsProcessor.addMarker(null, MetricsProcessor.ERROR_MARKER_URI, MetricsProcessor.TYPE_CHECK_FAILURE_URI);
				}
				t.printStackTrace();
			} else if (t instanceof URISyntaxException) {
				getModelProcessor().addIssueToAcceptor(SadlErrorMessages.TYPE_CHECK_EXCEPTION.get("URI Syntax"), expr);
				if (metricsProcessor != null) {
					metricsProcessor.addMarker(null, MetricsProcessor.ERROR_MARKER_URI, MetricsProcessor.TYPE_CHECK_FAILURE_URI);
				}
				if (metricsProcessor != null) {
					metricsProcessor.addMarker(null, MetricsProcessor.ERROR_MARKER_URI, MetricsProcessor.TYPE_CHECK_FAILURE_URI);
				}
				t.printStackTrace();
			} else if (t instanceof IOException) {
				getModelProcessor().addIssueToAcceptor(SadlErrorMessages.TYPE_CHECK_EXCEPTION.get("IO"), expr);
				if (metricsProcessor != null) {
					metricsProcessor.addMarker(null, MetricsProcessor.ERROR_MARKER_URI, MetricsProcessor.TYPE_CHECK_FAILURE_URI);
				}
				t.printStackTrace();
			} else if (t instanceof ConfigurationException) {
				getModelProcessor().addIssueToAcceptor(SadlErrorMessages.TYPE_CHECK_EXCEPTION.get("Configuration"), expr);
				if (metricsProcessor != null) {
					metricsProcessor.addMarker(null, MetricsProcessor.ERROR_MARKER_URI, MetricsProcessor.TYPE_CHECK_FAILURE_URI);
				}
				t.printStackTrace();
			} else if (t instanceof NullPointerException){
				getModelProcessor().addIssueToAcceptor(SadlErrorMessages.TYPE_CHECK_EXCEPTION.get("Null Pointer"), expr);
			} else if (t instanceof DontTypeCheckException) {
				return true;
			} else if (t instanceof PropertyWithoutRangeException) {
				getModelProcessor().addIssueToAcceptor(SadlErrorMessages.PROPERTY_WITHOUT_RANGE.get(((PropertyWithoutRangeException)t).getPropID()), expr);
				return true;
			} else if (t instanceof CircularDefinitionException) {
				t.printStackTrace();
			}
			else {
				t.printStackTrace();
			}
		}
		catch (Throwable t2) {
			issueAcceptor.addError("Unexpected exception (" + t.getClass().getCanonicalName() + ": " + 
					t2.getMessage() + ") displaying validation error : " + t.getMessage(), expr);
		}
		return false;
	}

	public boolean validate(EObject leftExpression, EObject rightExpression, String op, StringBuilder errorMessageBuilder) throws TranslationException {
		List<String> operations = Arrays.asList(op.split("\\s+"));
		TypeCheckInfo leftTypeCheckInfo = null;
		TypeCheckInfo rightTypeCheckInfo = null;
		try {	
			leftTypeCheckInfo = getType(leftExpression);
		} catch (Throwable t) {
			if (handleValidationException(leftExpression, t)) {
				return true;
			}
		} 
		try {	
			rightTypeCheckInfo = getType(rightExpression);
		} catch (Throwable t) {
			if (handleValidationException(rightExpression, t)) {
				return true;
			}
		} 
		try {
			if (leftTypeCheckInfo == null && rightTypeCheckInfo == null) {
				// this condition happens when a file is loaded in the editor and clean/build is invoked
				return true;
			}
			if(!compareTypes(operations, leftExpression, rightExpression, leftTypeCheckInfo, rightTypeCheckInfo, ImplicitPropertySide.NONE)){
				if (createErrorMessage(errorMessageBuilder, leftTypeCheckInfo, rightTypeCheckInfo, op, false, null)) {
					return false;
				}
			}
		} catch (InvalidNameException e) {
			try {
				getModelProcessor().addIssueToAcceptor(SadlErrorMessages.TYPE_CHECK_EXCEPTION.get("Invalid Name"), rightExpression);
			} catch (InvalidTypeException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (metricsProcessor != null) {
				metricsProcessor.addMarker(null, MetricsProcessor.ERROR_MARKER_URI, MetricsProcessor.INVALID_EXPRESSION_URI);
			}
			e.printStackTrace();
		} catch (DontTypeCheckException e) {
			return true;
		} catch (InvalidTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	protected boolean createErrorMessage(StringBuilder errorMessageBuilder, TypeCheckInfo leftTypeCheckInfo, TypeCheckInfo rightTypeCheckInfo, String operation, boolean addToIssueAcceptor, EObject expr) throws InvalidTypeException {
		Severity specifiedSeverity = null;
		if (leftTypeCheckInfo != null && leftTypeCheckInfo.getSeverity() != null) {
			if (leftTypeCheckInfo.getSeverity().equals(Severity.IGNORE)) {
				return false;
			}
			else {
				specifiedSeverity = leftTypeCheckInfo.getSeverity();
			}
		}
		if (rightTypeCheckInfo != null && rightTypeCheckInfo.getSeverity() != null) {
			if (rightTypeCheckInfo.getSeverity().equals(Severity.IGNORE)) {
				return false;
			}
			else {
				if (specifiedSeverity == null) {
					specifiedSeverity = rightTypeCheckInfo.getSeverity();
				}
				else if (rightTypeCheckInfo.getSeverity().compareTo(specifiedSeverity) > 0) {
					specifiedSeverity = rightTypeCheckInfo.getSeverity();
				}
			}
		}
		String[] leftDesc = getTypeCheckInfoDescription(leftTypeCheckInfo);
		String[] rightDesc = getTypeCheckInfoDescription(rightTypeCheckInfo);
		
		String leftSide = "";
		String rightSide = "";
		String op = "";
		
		if (leftDesc == null) {
			leftSide = "Undetermined left type";
		}
		else {
			if (leftDesc.length > 0) {
				leftSide = leftDesc[0];
			}
			if (leftDesc.length > 1) {
				leftSide = leftSide + " " + leftDesc[1];
			}
		}
		
		if (getModelProcessor().isComparisonOperator(operation)) {
			op = "be compared (" + operation + ")";
		}
		else {
			op = "operate (" + operation + ")";
		}
		
		if (rightDesc == null) {
			rightSide = "Undetermined right type";
		}
		else {
			if (rightDesc.length > 0) {
				rightSide = rightDesc[0];
			}
			if (rightDesc.length > 1) {
				rightSide = rightSide + " " + rightDesc[1];
			}
		}
		if (leftSide.contains(",")) leftSide = leftSide + ",";
		errorMessageBuilder.append(SadlErrorMessages.VALIDATE_BIN_OP_ERROR.get(leftSide, op, rightSide));
		if (addToIssueAcceptor) {
			if (specifiedSeverity != null) {
				getModelProcessor().addIssueToAcceptor(errorMessageBuilder.toString(), specifiedSeverity, expr);
			}
			else {
				getModelProcessor().addIssueToAcceptor(errorMessageBuilder.toString(), expr);
			}
		}
		return true;
	}

	private String[] getTypeCheckInfoDescription(TypeCheckInfo typeCheckInfo) throws InvalidTypeException {
		if (typeCheckInfo == null) {
			String[] result = new String[1];
			result[0] = "No type check info generated";
			return result;
		}
		else {
			StringBuilder sb1 = new StringBuilder();;
			StringBuilder sb2 = null;
			ConceptIdentifier typeExpr = typeCheckInfo.getExpressionType();
			if (typeExpr != null) {
				sb1.append(getModelProcessor().conceptIdentifierToString(typeExpr));
			}
			if (typeExpr instanceof ConceptName) {
				ConceptType ct = ((ConceptName)typeExpr).getType();
				if (ct == null) {
					sb1.append(", type unknown ");
				}
				else if (ct.equals(ConceptType.INDIVIDUAL)) {
					sb1.append(", an instance of type ");
					sb2 = new StringBuilder();				}
				else if (ct.equals(ConceptType.ONTCLASS)) {
					sb1.append(", a class ");
				}
				else if (ct.equals(ConceptType.RDFDATATYPE)) {
					sb1.append(", an RDF datatype ");
					sb2= new StringBuilder();				}
				else if (ct.equals(ConceptType.RDFPROPERTY)) {
					sb1.append(", an RDF property ");
					if (rdfPropertyTypeCheckInfoHasRange(typeCheckInfo)) {
						sb1.append("with ");
						sb1.append(typeCheckInfo.getTypeToExprRelationship());
						sb1.append(" ");
					}
					sb2= new StringBuilder();				}
				else if (ct.equals(ConceptType.ANNOTATIONPROPERTY)) {
					sb1.append(", an  annotation property ");
				}
				else if (ct.equals(ConceptType.DATATYPEPROPERTY)) {
					sb1.append(", a datatype property with ");
					sb1.append(typeCheckInfo.getTypeToExprRelationship());
					sb1.append(" ");
					sb2 = new StringBuilder();				}
				else if (ct.equals(ConceptType.OBJECTPROPERTY)) {
					sb1.append(", an object property with ");
					sb1.append(typeCheckInfo.getTypeToExprRelationship());
					sb1.append(" ");
					sb2= new StringBuilder();				}
				else if (ct.equals(ConceptType.VARIABLE)) {
					sb1.append(", a variable of type ");
					sb2 = new StringBuilder();				}
			}
			if (typeCheckInfo.getCompoundTypes() != null) {
				List<String> compoundTypeList = new ArrayList<String>();
				for (int i = 0; i < typeCheckInfo.getCompoundTypes().size(); i++) {
					TypeCheckInfo tci = typeCheckInfo.getCompoundTypes().get(i);
					String[] tciresult = getTypeCheckInfoDescription(tci);
					if (tciresult != null && tciresult.length > 1 && !compoundTypeList.toString().contains(tciresult[1])) {
						compoundTypeList.add(tciresult[1]);
					}
				}
				if (sb2 == null) sb2 = new StringBuilder();
				for (int i = 0; i < compoundTypeList.size(); i++) {
					if (i > 0) sb2.append(" or ");
					sb2.append(compoundTypeList.get(i));
				}
			}
			else {
				if (typeCheckInfo.getRangeValueType().equals(RangeValueType.LIST)) {
					if (sb2 == null) sb2 = new StringBuilder();
					sb2.append("a List of values of type ");
				}
				if (sb2 != null && typeCheckInfo.getTypeCheckType() != null) {
					sb2.append(getModelProcessor().nodeToString(typeCheckInfo.getTypeCheckType()));
				}
				else if (typeCheckInfo.getExplicitValue() != null) {
					RDFNode ev = typeCheckInfo.getExplicitValue();
					if (typeCheckInfo.getExplicitValueType().equals(ExplicitValueType.VALUE)) {
						sb1.replace(0, sb1.length(), "explicit value ");
					}
					if (ev.isLiteral()) {
						try {
							sb2.append(getModelProcessor().rdfNodeToString(ev.asLiteral()));
						}
						catch (DatatypeFormatException e) {
							getModelProcessor().addIssueToAcceptor(SadlErrorMessages.TYPE_CHECK_EXCEPTION.get("Datatype Format"), typeCheckInfo.context);
							//addError(e.getMessage(), typeCheckInfo.context);
						}
					}
					else {
						sb2.append(getModelProcessor().rdfNodeToString(ev));
					}
				}
			}
			String[] result = sb2 != null ? new String[2] : new String[1];
			result[0] = sb1.toString();
			if (sb2 != null) {
				result[1] = sb2.toString();
			}
			return result;
		}
//		String leftName = leftTypeCheckInfo != null ? leftTypeCheckInfo.expressionType != null ? leftTypeCheckInfo.expressionType.toString() : "UNIDENTIFIED" : "UNIDENTIFIED";
//		String rightName = rightTypeCheckInfo != null ? rightTypeCheckInfo.expressionType != null ? rightTypeCheckInfo.expressionType.toString() : "UNIDENTIFIED" : "UNIDENTIFIED";
//		String leftType;
//		String leftRange;
//		if (leftTypeCheckInfo == null) {
//			leftType = "UNIDENTIFIED";
//			leftRange = "UNIDENTIFIED";
//		}
//		else if (leftTypeCheckInfo.compoundTypes == null) {
//			leftType = leftTypeCheckInfo.typeCheckType != null ? leftTypeCheckInfo.typeCheckType.toString() : "UNIDENTIFIED";
//			leftRange = leftTypeCheckInfo.rangeValueType != null ? leftTypeCheckInfo.rangeValueType.toString() : "UNIDENTIFIED";
//		}
//		else {
//			StringBuilder sb = new StringBuilder();
//			for (int i = 0; i < leftTypeCheckInfo.getCompoundType().size(); i++) {
//				if (i > 0) {
//					sb.append(" or ");
//				}
//				TypeCheckInfo tic = leftTypeCheckInfo.getCompoundType().get(i);
//				sb.append(tic != null ? tic.typeCheckType != null ? tic.typeCheckType.toString() : "UNIDENTIFIED" : "UNIDENTIFIED");
//			}
//			leftType = sb.toString();
//			sb = new StringBuilder();
//			for (int i = 0; i < leftTypeCheckInfo.getCompoundType().size(); i++) {
//				if (i > 0) {
//					sb.append(" or ");
//				}
//				TypeCheckInfo tic = leftTypeCheckInfo.getCompoundType().get(i);
//				sb.append(tic != null ? tic.rangeValueType != null ? tic.rangeValueType.toString() : "UNIDENTIFIED" : "UNIDENTIFIED");
//			}
//			leftRange = sb.toString();
//		}
//		return null;
	}

	private boolean rdfPropertyTypeCheckInfoHasRange(TypeCheckInfo typeCheckInfo) {
		if (typeCheckInfo.getTypeCheckType() != null) {
			return true;
		}
		if (typeCheckInfo.getCompoundTypes() != null) {
			for (int i = 0; i < typeCheckInfo.getCompoundTypes().size(); i++) {
				TypeCheckInfo next = typeCheckInfo.getCompoundTypes().get(i);
				boolean b = rdfPropertyTypeCheckInfoHasRange(next);
				if (b) {
					return true;
				}
			}
		}
		return false;
	}

	protected boolean skipOperations(List<String> operations) {
		if(operations.contains("and") || operations.contains("or")){
			return true;
		}
		return false;
	}

	protected TypeCheckInfo getType(EObject expression) throws InvalidNameException, TranslationException, URISyntaxException, IOException, ConfigurationException, DontTypeCheckException, CircularDefinitionException, InvalidTypeException, CircularDependencyException, PropertyWithoutRangeException{
//		if (expressionsValidated.containsKey(expression)) {
//			return expressionsValidated.get(expression);
//		}
		if(expression instanceof Name){
			return getType((Name)expression);
		}else if(expression instanceof SadlResource){
			return getType((SadlResource)expression);
		}
		else if(expression instanceof Declaration){
			SadlTypeReference decltype = ((Declaration)expression).getType();
			if (decltype != null) {
				return getType(decltype);
			}
			else {
				getModelProcessor().addIssueToAcceptor(SadlErrorMessages.NULL_TYPE.toString(), expression);
			}
			//Need to return passing case for time being
//			ConceptName declarationConceptName = new ConceptName("todo");
//			return new TypeCheckInfo(declarationConceptName, declarationConceptName);
		}
		else if(expression instanceof StringLiteral || expression instanceof SadlStringLiteral) {
			NamedNode tctype = new NamedNode(XSD.xstring.getURI(), NodeType.DataTypeNode);
			ConceptName stringLiteralConceptName = getModelProcessor().namedNodeToConceptName(tctype);
			return new TypeCheckInfo(stringLiteralConceptName, tctype, this, expression);
		}
		else if(expression instanceof NumberLiteral || expression instanceof SadlNumberLiteral){
			BigDecimal value = null;
			Literal litval = null;
			if (expression instanceof NumberLiteral) {
				value = ((NumberLiteral)expression).getValue();
			}
			else {	// SadlNumberLiteral
				if (((SadlNumberLiteral)expression).getUnit() != null && !getModelProcessor().ignoreUnittedQuantities) {
					return getUnittedQuantityTypeCheckInfo(expression, ((SadlNumberLiteral)expression).getUnit());
				}
				String strval = ((SadlNumberLiteral)expression).getLiteralNumber().toPlainString();
				if (strval.indexOf('.') >= 0) {
					value = BigDecimal.valueOf(Double.parseDouble(strval));
				}
				else {
					value = BigDecimal.valueOf(Long.parseLong(strval));
				}
			}
			if (expression.eContainer() != null && expression.eContainer() instanceof UnaryExpression && ((UnaryExpression)expression.eContainer()).getOp().equals("-")) {
				value = value.negate();
			}
			ConceptName numberLiteralConceptName = null;
			Node tctype = null;
			if (value.stripTrailingZeros().scale() <= 0 || value.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
				if (value.compareTo(BigDecimal.valueOf(MAX_INT)) <= 0 && value.compareTo(BigDecimal.valueOf(MIN_INT)) >= 0) {
					tctype = new NamedNode(XSD.xint.getURI(), NodeType.DataTypeNode);
					numberLiteralConceptName = getModelProcessor().namedNodeToConceptName((NamedNode) tctype);
					litval = theJenaModel.createTypedLiteral(value.intValue());
				}
				else {
					numberLiteralConceptName = new ConceptName(XSD.xlong.getURI());
					tctype = new NamedNode(XSD.xlong.getURI(), NodeType.DataTypeNode);
					if (value.compareTo(BigDecimal.valueOf(MAX_LONG)) > 0 || value.compareTo(BigDecimal.valueOf(MIN_LONG)) < 0) {
						issueAcceptor.addError("Error converting to a number", expression);
					}
					else {
						litval = theJenaModel.createTypedLiteral(value.longValue());
					}
				}
			}
			else {
				numberLiteralConceptName = new ConceptName(XSD.decimal.getURI());
				litval = theJenaModel.createTypedLiteral(value.doubleValue());
				tctype = new NamedNode(XSD.decimal.getURI(), NodeType.DataTypeNode);
			}
			numberLiteralConceptName.setType(ConceptType.RDFDATATYPE);
			TypeCheckInfo litTci;
			if (litval != null) {
				litTci = new TypeCheckInfo(numberLiteralConceptName, litval, ExplicitValueType.VALUE, this, expression); 
			}
			else {
				litTci = new TypeCheckInfo(numberLiteralConceptName, createNamedNode(numberLiteralConceptName.toFQString(), OntConceptType.LITERAL), this, expression);
			}
			litTci.setTypeCheckType(tctype);
			return litTci;
		}
		else if (expression instanceof UnitExpression) {
			return getUnittedQuantityTypeCheckInfo(((UnitExpression)expression).getLeft(), ((UnitExpression)expression).getUnit());
		}
		else if(expression instanceof BooleanLiteral || expression instanceof SadlBooleanLiteral){
			return createBooleanTypeCheckInfo(expression);
		}
		else if(expression instanceof Constant){
			return getType((Constant)expression);
		}
		else if (expression instanceof SadlConstantLiteral) {
			return getType((SadlConstantLiteral)expression);
		}
		else if(expression instanceof ValueTable){
			ConceptName declarationConceptName = new ConceptName("TODO");
			return new TypeCheckInfo(declarationConceptName, null, this, expression);
		}
		else if(expression instanceof PropOfSubject){
			return getType((PropOfSubject)expression);
		}
		else if(expression instanceof SubjHasProp){
			return getType((SubjHasProp)expression);
		}
		else if (SadlASTUtils.isCommaSeparatedAbbreviatedExpression(expression)) {
			// validate the property initializations within
			validateCommaSeparatedAbreviatedExpression((SubjHasProp) expression);
			return getType(((SubjHasProp)expression).getLeft());
		}
		else if(expression instanceof UnaryExpression){
			return getType(((UnaryExpression) expression).getExpr());
		}
		else if (expression instanceof SadlUnaryExpression) {
			return getType(((SadlUnaryExpression)expression).getValue());
		}
		else if(expression instanceof ElementInList){
			return getType((ElementInList)expression);
		}
		else if(expression instanceof BinaryOperation){
			return getType((BinaryOperation)expression);
		}
		else if (expression instanceof Sublist) {
			// the type is the type of the list
			TypeCheckInfo listtype = getType((((Sublist)expression).getList()));
			if (listtype != null && !listtype.getRangeValueType().equals(RangeValueType.LIST)) {
				getModelProcessor().addIssueToAcceptor(SadlErrorMessages.IS_NOT_A.get("this","list"), ((Sublist)expression).getList());
			}
			return listtype;
		}
		else if (expression instanceof SadlPrimitiveDataType)  {
			return getType((SadlPrimitiveDataType)expression);
		}
		else if (expression instanceof SadlSimpleTypeReference) {
			return getType((SadlSimpleTypeReference)expression);
		}
		else if (expression instanceof SadlIntersectionType) {
			return getType((SadlIntersectionType)expression);
		}
		else if (expression instanceof SadlPropertyCondition) {
			return getType((SadlPropertyCondition)expression);
		}
		else if (expression instanceof SadlResource) {
			return getType((SadlResource)expression);
		}
		else if (expression instanceof SadlTypeReference) {
			return getType((SadlTypeReference)expression);
		}
		else if (expression instanceof SadlUnionType) {
			return getType((SadlUnionType)expression);
		}
		else if (expression instanceof SadlInstance) {
			return getType((SadlInstance)expression);
		}
		else if (expression != null) {
			throw new TranslationException("Unhandled expression type: " + expression.getClass().getCanonicalName());
		}
		if (expression != null) {
			getModelProcessor().addIssueToAcceptor(SadlErrorMessages.DECOMPOSITION_ERROR.get(expression.toString()), expression);
			if (metricsProcessor != null) {
				metricsProcessor.addMarker(null, MetricsProcessor.ERROR_MARKER_URI, MetricsProcessor.UNCLASSIFIED_FAILURE_URI);
			}
		}
		return null;
	}

	private TypeCheckInfo getType(SadlInstance expression) throws InvalidTypeException, DontTypeCheckException,
			CircularDefinitionException, InvalidNameException, TranslationException, URISyntaxException, IOException,
			ConfigurationException, CircularDependencyException, PropertyWithoutRangeException {
		SadlResource inst = expression.getInstance();
		if (inst == null) {
			SadlTypeReference typ = expression.getType();
			if (typ != null && typ instanceof SadlSimpleTypeReference) {
				inst = ((SadlSimpleTypeReference)typ).getType();
				if (declarationExtensions.getConceptUri(inst).equals(SadlConstants.SADL_IMPLICIT_MODEL_UNITTEDQUANTITY_URI) && getModelProcessor().ignoreUnittedQuantities) {
					if (expression instanceof SadlNestedInstance) {
						Iterator<SadlPropertyInitializer> pinititr = ((SadlNestedInstance)expression).getPropertyInitializers().iterator();
						while (pinititr.hasNext()) {
							SadlPropertyInitializer spinit = pinititr.next();
							if (declarationExtensions.getConceptUri(spinit.getProperty()).equals(SadlConstants.SADL_IMPLICIT_MODEL_VALUE_URI)) {
								return getType(spinit.getProperty());
							}
						}
					}
				}
			}
		}
		if (inst != null) {
			TypeCheckInfo insttci = getType(inst);
			if (insttci != null && insttci.getTypeCheckType() == null) {
				SadlTypeReference typ = expression.getType();
				if (typ != null && typ instanceof SadlSimpleTypeReference) {
					SadlResource typsr = ((SadlSimpleTypeReference)typ).getType();
					insttci.setTypeCheckType(new NamedNode(declarationExtensions.getConceptUri(typsr), NodeType.ClassNode));
				}
			}
			return insttci;
		}
		else {
			issueAcceptor.addError("Unhandled condition of SadlInstance", expression);
			return null;
		}
	}

	private TypeCheckInfo getType(ElementInList expression)
			throws InvalidNameException, TranslationException, URISyntaxException, IOException, ConfigurationException,
			DontTypeCheckException, CircularDefinitionException, InvalidTypeException, CircularDependencyException,
			PropertyWithoutRangeException {
		Expression el = expression.getElement();
		if (el instanceof PropOfSubject) {
			TypeCheckInfo listtype = getType(((PropOfSubject)el).getRight());
			if (listtype == null) {
				getModelProcessor().addIssueToAcceptor("Unable to get the List type", el);
			}
			else if (listtype.getRangeValueType() != RangeValueType.LIST) {
				getModelProcessor().addIssueToAcceptor("Expected a List", el);
			}
			else {
				// the element's type is the type of the list but not necessarily a list
				listtype = convertListTypeToElementOfListType(listtype);
			}
			return listtype;
		}
		else {
			return getType(el);
		}
//		else {
//			getModelProcessor().addIssueToAcceptor(SadlErrorMessages.UNHANDLED.get("element type in element in list construct. ", el.getClass().getCanonicalName() + "; please report"), expression);
//			if (metricsProcessor != null) {
//				metricsProcessor.addMarker(null, MetricsProcessor.ERROR_MARKER_URI, MetricsProcessor.TYPE_CHECK_ERROR_URI);
//			}
//			return null;
//		}
	}

	private TypeCheckInfo getType(SubjHasProp expression) throws CircularDefinitionException, DontTypeCheckException,
			InvalidNameException, TranslationException, URISyntaxException, IOException, ConfigurationException,
			InvalidTypeException, CircularDependencyException, PropertyWithoutRangeException {
		if (SadlASTUtils.isUnitExpression(expression)) {
			NamedNode tctype = new NamedNode(SadlConstants.SADL_IMPLICIT_MODEL_UNITTEDQUANTITY_URI, NodeType.ClassNode);
			ConceptName uqcn = getModelProcessor().namedNodeToConceptName(tctype);
			TypeCheckInfo tci = new TypeCheckInfo(uqcn, tctype, this, expression);
			tci.setTypeToExprRelationship(SadlASTUtils.getUnitAsString(expression));
			return tci;
		}
		else if (expression.isComma() && expression.getRight() == null) {
			issueAcceptor.addError("Invalid subject-has-property construct with a comma", expression);
			return null;
		}
		else if (!getModelProcessor().isDeclaration(expression) && expression.eContainer() instanceof BinaryOperation || 
				expression.eContainer() instanceof SelectExpression ||
				expression.eContainer() instanceof AskExpression ||
				expression.eContainer() instanceof ConstructExpression) {
			// we are comparing or assigning this to something else so we want the type of the root (if there is a chain) property
			if (expression.getProp() instanceof SadlResource) {
				SadlResource prop = expression.getProp();
				return getType(prop);
			}
			else {
				issueAcceptor.addError("This subject-has-property construct isn't properly validated, please report.", expression);
				return null;
			}
		}
		else {
			if (modelProcessor.getTarget() instanceof Rule) {
				return getType(expression.getProp());
			}
			else {
				Declaration subjHasPropInDeclaration = subjHasPropIsDeclaration((SubjHasProp) expression);  // are we in a Declaration (a real declaration--the type is a class)
				if (subjHasPropInDeclaration != null) {
					return getType(subjHasPropInDeclaration);
				}
				else {
					issueAcceptor.addError("This appears to be a declaration that isn't fully supported; should it be nested (in parentheses)", expression);
					return null;
				}
			}
		}
	}

	private TypeCheckInfo getType(BinaryOperation expression) throws CircularDependencyException,
			InvalidNameException, TranslationException, URISyntaxException, IOException, ConfigurationException,
			DontTypeCheckException, CircularDefinitionException, InvalidTypeException, PropertyWithoutRangeException {
		List<String> operations = Arrays.asList(expression.getOp().split("\\s+"));
		TypeCheckInfo leftTypeCheckInfo = unifyCompoundTypeCheckInfos(getType(expression.getLeft()));
		TypeCheckInfo rightTypeCheckInfo = unifyCompoundTypeCheckInfos(getType(expression.getRight()));
//			if (leftTypeCheckInfo != null && isVariable(leftTypeCheckInfo) && expression.getRight() instanceof Declaration) {
//				return rightTypeCheckInfo;
//			}
		if (expression.getLeft() instanceof PropOfSubject && ((BinaryOperation)expression).getRight() instanceof Declaration) {
			TypeCheckInfo subjtype = getType(((PropOfSubject)expression.getLeft()).getRight());
			if (subjtype != null) {
				Node subject = subjtype.getTypeCheckType();
				if (subject != null) {
//					addLocalRestriction(subjtype.getTypeCheckType().toString(), leftTypeCheckInfo, rightTypeCheckInfo);
					Object keytrans = getModelProcessor().processExpression(((PropOfSubject)expression.getLeft()));
					if (keytrans != null) {
						String key = generateLocalRestrictionKey(keytrans);
						addLocalRestriction(key, leftTypeCheckInfo, rightTypeCheckInfo, expression);
					}
				}
			}
		}

		TypeCheckInfo binopreturn = combineTypes(operations, expression.getLeft(), expression.getRight(), 
				leftTypeCheckInfo, rightTypeCheckInfo, ImplicitPropertySide.NONE);
		if (getModelProcessor().isNumericOperator(expression.getOp())) {
			TypeCheckInfo uqTci = getTypeCompatibleWithOperationOnUnittedQuantities(((BinaryOperation)expression).getOp(), leftTypeCheckInfo, rightTypeCheckInfo);
			if (uqTci != null) {
				return uqTci;
			}
			if (leftTypeCheckInfo != null && !isNumeric(leftTypeCheckInfo) && !isNumericWithImpliedProperty(leftTypeCheckInfo, ((BinaryOperation)expression).getLeft())) {
				getModelProcessor().addIssueToAcceptor("Numeric operator requires numeric arguments", ((BinaryOperation)expression).getLeft());
			}
			if (rightTypeCheckInfo != null && !isNumeric(rightTypeCheckInfo) && !isNumericWithImpliedProperty(rightTypeCheckInfo, ((BinaryOperation)expression).getRight())) {
				getModelProcessor().addIssueToAcceptor("Numeric operator requires numeric arguments", ((BinaryOperation)expression).getRight());
			}
			NamedNode tctype = new NamedNode(XSD.decimal.getURI(), NodeType.DataTypeNode);
			ConceptName decimalLiteralConceptName = getModelProcessor().namedNodeToConceptName(tctype);
			decimalLiteralConceptName.setType(ConceptType.RDFDATATYPE);
			return new TypeCheckInfo(decimalLiteralConceptName, tctype, this, expression);
		}
		if (binopreturn != null) {
			return binopreturn;
		}
		else {
			return createBooleanTypeCheckInfo(expression);
		}
	}

	private TypeCheckInfo createBooleanTypeCheckInfo(EObject expression) throws TranslationException, InvalidNameException, InvalidTypeException {
		NamedNode tctype = new NamedNode(XSD.xboolean.getURI(), NodeType.DataTypeNode);
		ConceptName booleanLiteralConceptName = getModelProcessor().namedNodeToConceptName(tctype);
		return new TypeCheckInfo(booleanLiteralConceptName, tctype, this, expression);
	}

	private TypeCheckInfo getTypeCompatibleWithOperationOnUnittedQuantities(String op, TypeCheckInfo leftTypeCheckInfo,
			TypeCheckInfo rightTypeCheckInfo) {
		if (!modelProcessor.isNumericOperator(op)) {
			return null;
		}
		if (leftTypeCheckInfo == null || rightTypeCheckInfo == null) {
			return null;
		}
		if (!leftTypeCheckInfo.getTypeCheckType().toFullyQualifiedString().equals(SadlConstants.SADL_IMPLICIT_MODEL_UNITTEDQUANTITY_URI) ||
				!rightTypeCheckInfo.getTypeCheckType().toFullyQualifiedString().equals(SadlConstants.SADL_IMPLICIT_MODEL_UNITTEDQUANTITY_URI)) {
			return null;
		}
		if (op.equals("+") || op.equals("-")) {
			if (leftTypeCheckInfo.getTypeToExprRelationship() != null && rightTypeCheckInfo.getTypeToExprRelationship() != null &&
					leftTypeCheckInfo.getTypeToExprRelationship().equals(rightTypeCheckInfo.getTypeToExprRelationship())) {
				return leftTypeCheckInfo;
			}
			if (leftTypeCheckInfo.getTypeToExprRelationship().equals("range") && !rightTypeCheckInfo.getTypeToExprRelationship().equals("range")) {
				// could issue warning that units may not match
				return leftTypeCheckInfo;
			}
			if (!leftTypeCheckInfo.getTypeToExprRelationship().equals("range") && rightTypeCheckInfo.getTypeToExprRelationship().equals("range")) {
				// could issue warning that units may not match
				return rightTypeCheckInfo;
			}
		}
		if (op.equals("/") || op.equals("*") || op.equals("^")) {
			if (leftTypeCheckInfo.getTypeToExprRelationship() != null && rightTypeCheckInfo.getTypeToExprRelationship() != null) {
				leftTypeCheckInfo.setTypeToExprRelationship(leftTypeCheckInfo.getTypeToExprRelationship() + op + rightTypeCheckInfo.getTypeToExprRelationship());
				return leftTypeCheckInfo;
			}
		}
		return null;
	}

	private Declaration getEmbeddedDeclaration(EObject expr) {
		if (expr instanceof SubjHasProp) {
			return getEmbeddedDeclaration(((SubjHasProp)expr).getLeft());
		}
		else if (expr instanceof BinaryOperation) {
			Declaration decl = getEmbeddedDeclaration(((BinaryOperation)expr).getLeft());
			if (decl != null) {
				return decl;
			}
			decl = getEmbeddedDeclaration(((BinaryOperation)expr).getRight());
			if (decl != null) {
				return decl;
			}
		}
		else if (expr instanceof UnaryExpression && ((UnaryExpression)expr).getExpr() instanceof Declaration) {
			return (Declaration) ((UnaryExpression)expr).getExpr();
		}
		else if (expr instanceof Declaration) {
			return (Declaration) expr;
		}
		return null;
	}

	private TypeCheckInfo getUnittedQuantityTypeCheckInfo(EObject expression, String unit)
			throws InvalidTypeException, InvalidNameException, TranslationException {
		NamedNode tctype = new NamedNode(SadlConstants.SADL_IMPLICIT_MODEL_UNITTEDQUANTITY_URI, NodeType.ClassNode);
		ConceptName uqcn = getModelProcessor().namedNodeToConceptName(tctype);
		List<ConceptName> impliedProperties = getImpliedProperties(theJenaModel.getOntResource(uqcn.getUri()));
		if (impliedProperties != null) {
			TypeCheckInfo tci = new TypeCheckInfo(uqcn, tctype, impliedProperties, this, expression);
			tci.setTypeToExprRelationship(unit);
			return tci;
		}
		else {
			TypeCheckInfo tci = new TypeCheckInfo(uqcn, tctype, this, expression);
			tci.setTypeToExprRelationship(unit);
			return tci;
		}
	}
	
	protected void validateCommaSeparatedAbreviatedExpression(SubjHasProp expression) throws DontTypeCheckException, CircularDefinitionException, 
		InvalidNameException, TranslationException, URISyntaxException, IOException, ConfigurationException, InvalidTypeException, CircularDependencyException, PropertyWithoutRangeException {
		TypeCheckInfo proptct = getType(expression.getProp());
		TypeCheckInfo rghttct = getType(expression.getRight());
		if (!compareTypesUsingImpliedProperties(Arrays.asList("is"), expression.getProp(), expression.getRight(), proptct, rghttct, ImplicitPropertySide.NONE)) {
			StringBuilder errorMessageBuilder = new StringBuilder();
			createErrorMessage(errorMessageBuilder, proptct, rghttct, "property initialization", true, expression);
		}
	}

	protected Declaration subjHasPropIsDeclaration(SubjHasProp expression) throws DontTypeCheckException, CircularDefinitionException, InvalidNameException, TranslationException, URISyntaxException, IOException, ConfigurationException, InvalidTypeException, CircularDependencyException, PropertyWithoutRangeException {
		if (expression.getLeft() instanceof Declaration) {
			TypeCheckInfo declType = getType(((Declaration)expression.getLeft()).getType());
			if (declType == null) {
//				throw new TranslationException("Declaration has no type");
				return null;	// this happens when a file is open in the editor and there is a clean/build
			}
			Node tct = declType.getTypeCheckType();
			if (tct instanceof NamedNode && ((NamedNode)tct).getNodeType() != null && ((NamedNode)tct).getNodeType().equals(NodeType.ClassNode)) {
				return (Declaration)expression.getLeft();
			}
		}
		if (expression.getLeft() instanceof SubjHasProp) {
			return subjHasPropIsDeclaration((SubjHasProp)expression.getLeft());
		}
		return null;
	}

	private TypeCheckInfo unifyCompoundTypeCheckInfos(TypeCheckInfo tci) throws CircularDependencyException, InvalidNameException, TranslationException, InvalidTypeException {
		// if this is a compound TypeCheckInfo, look to make sure that one isn't a subclass of another and eliminate lower classes
		if (tci != null && tci.getCompoundTypes() != null) {
			int size = tci.getCompoundTypes().size();
			if (size == 1) {
				return tci.getCompoundTypes().get(0);
			}
			List<TypeCheckInfo> considered = new ArrayList<TypeCheckInfo>();
			considered.add(tci.getCompoundTypes().get(0));
			List<TypeCheckInfo> toEliminate = null;
			int index = 1;
			for (int i = index; i < size; i++) {
				TypeCheckInfo newTci = tci.getCompoundTypes().get(i);
				RangeValueType newRvt = newTci.getRangeValueType();
				if (newRvt.equals(RangeValueType.CLASS_OR_DT)) {
					Node newTct = newTci.getTypeCheckType();
					if (newTct instanceof NamedNode) {
						OntClass newOr = theJenaModel.getOntClass(((NamedNode)newTct).toFullyQualifiedString());
						if (newOr != null) {
							for (int j = 0; j < considered.size(); j++) {
								if (newRvt.equals(considered.get(j).getRangeValueType())) {
									Node consideredTct = considered.get(j).getTypeCheckType();
									if (consideredTct instanceof NamedNode) {
										OntClass consideredOr = theJenaModel.getOntClass(((NamedNode)consideredTct).toFullyQualifiedString());
										if (consideredOr != null) {
											if (SadlUtils.classIsSubclassOf(newOr, consideredOr, true, null)) {
												if (toEliminate == null) toEliminate = new ArrayList<TypeCheckInfo>();
												toEliminate.add(newTci);
											}
											else if (SadlUtils.classIsSubclassOf(consideredOr, newOr, true, null)) {
												if (toEliminate == null) toEliminate = new ArrayList<TypeCheckInfo>();
												toEliminate.add(considered.get(j));
											}
										}
									}
									else {
										throw new TranslationException("Non-NamedNode type not handled yet");
									}
								}
							}
						}
					}
					else {
						throw new TranslationException("Non-NamedNode type not handled yet");
					}
				}
			}
			if (toEliminate != null) {
				for (int i = 0; i < toEliminate.size(); i++) {
					if (tci.getCompoundTypes().contains(toEliminate.get(i))) {		
						tci.getCompoundTypes().remove(toEliminate.get(i));
					}
				}
			}
			size = tci.getCompoundTypes().size();
			if (size == 1) {
				return tci.getCompoundTypes().get(0);
			}
		}
		return tci;
	}

	private boolean isNumericWithImpliedProperty(TypeCheckInfo tci, Expression expr) throws DontTypeCheckException, InvalidNameException, InvalidTypeException, TranslationException {
		if (tci.getImplicitProperties() != null) {
			Iterator<ConceptName> litr = tci.getImplicitProperties().iterator();
			while (litr.hasNext()) {
				ConceptName cn = litr.next();
				Property prop = theJenaModel.getProperty(cn.getUri());
				if (prop.canAs(ObjectProperty.class)) {
					cn.setType(ConceptType.OBJECTPROPERTY);
				}
				else if (prop.canAs(DatatypeProperty.class)) {
					cn.setType(ConceptType.DATATYPEPROPERTY);
				}
				else {
					cn.setType(ConceptType.RDFPROPERTY);
				}
				TypeCheckInfo newtci = getTypeInfoFromRange(cn, prop, expr);
				if (isNumeric(newtci)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isNumeric(TypeCheckInfo tci) throws InvalidTypeException {
		Node ci;
		if (tci.getTypeCheckType() != null) {
			ci = tci.getTypeCheckType();
			if (ci instanceof NamedNode) {
				return getModelProcessor().isNumericType(((NamedNode)ci).toFullyQualifiedString());
			}
		}
		else if (tci.getExplicitValueType() != null) {
//TODO this is incomplete; more examples needed AWC 12/19/2016				
			ExplicitValueType evt = tci.getExplicitValueType();
			if (evt.equals(ExplicitValueType.RESTRICTION)) {
				issueAcceptor.addWarning("Explicit value type is RESTRICITON, which isn't yet handled. Please report with use case.", tci.context);
			}
			else if (tci.getExpressionType() instanceof ConceptName){
				return getModelProcessor().isNumericType((ConceptName) tci.getExpressionType());
			}
		}
		return false;
	}

	private TypeCheckInfo convertListTypeToElementOfListType(TypeCheckInfo listtype) {
		Node tctype = listtype.getTypeCheckType();
		listtype.setRangeValueType((tctype != null && tctype instanceof NamedNode && ((NamedNode)tctype).isList()) ? RangeValueType.LIST : RangeValueType.CLASS_OR_DT);
		
		if(listtype.getCompoundTypes() == null){
			//not compound
			Node tct = listtype.getTypeCheckType();
			if(tct instanceof NamedNode){
				((NamedNode) tct).setNodeType(NodeType.ClassNode); 
			}
		}else{
			  Iterator<TypeCheckInfo> tci_iter = listtype.getCompoundTypes().iterator();
			  while(tci_iter.hasNext()){
				  convertListTypeToElementOfListType(tci_iter.next());
			  }
		}
		
		return listtype;
	}

	protected TypeCheckInfo getType(Constant expression) throws DontTypeCheckException, InvalidNameException, TranslationException, URISyntaxException, IOException, ConfigurationException, CircularDefinitionException, InvalidTypeException, CircularDependencyException, PropertyWithoutRangeException {
		//What do we do about the rest of the constants?
		/*'--' | 'a'? 'type' ;*/
		String constant = expression.getConstant();	
		if(constant.equals("PI") || constant.equals("e")){
			NamedNode tctype = new NamedNode(XSD.decimal.getURI(), NodeType.DataTypeNode);
			ConceptName constantConceptName = getModelProcessor().namedNodeToConceptName(tctype);
			return new TypeCheckInfo(constantConceptName, tctype, this, expression);
		}
		else if(constant.equals("length") || constant.equals("count") ||
				   constant.equals("index")){
					NamedNode tctype = new NamedNode(XSD.xint.getURI(), NodeType.DataTypeNode);
					ConceptName constantConceptName = getModelProcessor().namedNodeToConceptName(tctype);
					return new TypeCheckInfo(constantConceptName, tctype, this, expression);
				}
		else if(constant.endsWith("element") && (constant.startsWith("first") || constant.startsWith("last"))){
			//Handle list types???
			ConceptName declarationConceptName = new ConceptName(constant);
			return new TypeCheckInfo(declarationConceptName, null, this, expression);
		}
//		else if (constant.endsWith("value")) {
//			throw new DontTypeCheckException();
//		}
		else if (expression instanceof Constant && (((Constant)expression).getConstant().equals("value")
				|| ((Constant)expression).getConstant().equals("type"))) {
			Sublist slexpr = getSublistContainer(expression);
			if (slexpr != null) {
				TypeCheckInfo matchTci = getType(slexpr.getList());
//				matchTci.setRangeValueType(RangeValueType.CLASS_OR_DT);
				return matchTci;
			}
			issueAcceptor.addError("Unable to get sublist type", expression);
			return getType(expression);
		}
		else if (constant.equals("a type")) {
			NamedNode tctype = new NamedNode(RDFS.subClassOf.getURI(), NodeType.ClassNode);
			ConceptName rdfType = getModelProcessor().namedNodeToConceptName(tctype);
			return new TypeCheckInfo(rdfType, tctype, this, expression);
		}
		else if(constant.equals("None")){
			NamedNode tctype = new NamedNode(constant, NodeType.InstanceNode);
			ConceptName constantConceptName = getModelProcessor().namedNodeToConceptName(tctype);
			return new TypeCheckInfo(constantConceptName, tctype, this, expression);
		}
		else if (constant.equals("known")) {
			NamedNode tctype = new NamedNode(constant, NodeType.InstanceNode);
			ConceptName constantConceptName = getModelProcessor().namedNodeToConceptName(tctype);
			return new TypeCheckInfo(constantConceptName, tctype, this, expression);
		}
		else {
			// let any subclass validators do their thing
			lastSuperCallExpression = expression;
			return getType((Constant)expression);
		}

	}

	private TypeCheckInfo getType(SadlConstantLiteral expression) throws TranslationException, InvalidNameException, InvalidTypeException {
		String term = expression.getTerm();
		Literal litval = null;
		if (term.equals("PI")) {
			litval = theJenaModel.createTypedLiteral(Math.PI);
		}
		else if (term.equals("e")) {
			litval = theJenaModel.createTypedLiteral(Math.E);
		}
		else {
			throw new TranslationException("Unhandled SadlConstantLiteral type: " + expression.getClass().getCanonicalName());
		}
		NamedNode tctype = new NamedNode(XSD.decimal.getURI(), NodeType.DataTypeNode);
		ConceptName numberLiteralConceptName = getModelProcessor().namedNodeToConceptName(tctype);
		TypeCheckInfo litTci = new TypeCheckInfo(numberLiteralConceptName, litval, ExplicitValueType.VALUE, this, expression); 
		litTci.setTypeCheckType(tctype);
		return litTci;
	}

	private boolean isVariable(TypeCheckInfo tci) {
		if (tci == null) return false;
		Node ci = tci.getTypeCheckType();
		if (ci instanceof NamedNode && ((NamedNode)ci).getNodeType() != null && ((NamedNode)ci).getNodeType().equals(NodeType.VariableNode)) {
			return true;
		}
		return false;
	}

	private TypeCheckInfo getType(SadlTypeReference expression) throws DontTypeCheckException, CircularDefinitionException, InvalidNameException, TranslationException, URISyntaxException, IOException, ConfigurationException, InvalidTypeException, CircularDependencyException, PropertyWithoutRangeException {
		if (expression instanceof SadlIntersectionType) {
			return getType((SadlIntersectionType)expression);
		}
		else if (expression instanceof SadlPrimitiveDataType) {
			return getType((SadlPrimitiveDataType)expression);			
		}
		else if (expression instanceof SadlPropertyCondition) {
			return getType((SadlPropertyCondition)expression);
		}
		else if (expression instanceof SadlSimpleTypeReference) {
			return getType((SadlSimpleTypeReference)expression);
		}
		else if (expression instanceof SadlUnionType) {
			return getType((SadlUnionType)expression);
		}
		getModelProcessor().addIssueToAcceptor(SadlErrorMessages.TYPE_REFERENCE_ERROR.get(expression.getClass().getCanonicalName()), expression);
		if (metricsProcessor != null) {
			metricsProcessor.addMarker(null, MetricsProcessor.ERROR_MARKER_URI, MetricsProcessor.TYPE_CHECK_FAILURE_URI);
		}
		ConceptName declarationConceptName = new ConceptName("TODO");
		return new TypeCheckInfo(declarationConceptName, null, this, expression);
	}
	
	private TypeCheckInfo getType(SadlIntersectionType expression) {
		ConceptName declarationConceptName = new ConceptName("TODO");
		return new TypeCheckInfo(declarationConceptName, null, this, expression);		
	}

	private TypeCheckInfo getType(SadlPrimitiveDataType expression) throws TranslationException, InvalidNameException, InvalidTypeException {
		TypeCheckInfo tci = getType(expression.getPrimitiveType());
		tci.setContext(this, expression);
		return tci;
	}

	private TypeCheckInfo getType(SadlDataType primitiveType) throws TranslationException, InvalidNameException, InvalidTypeException {
		String nm = primitiveType.getName();
		NamedNode tctype = new NamedNode(XSD.getURI() + nm, NodeType.DataTypeNode);
		ConceptName cn = getModelProcessor().namedNodeToConceptName(tctype);
		return new TypeCheckInfo(cn, tctype, this, null);
	}

	private TypeCheckInfo getType(SadlPropertyCondition expression) {
		ConceptName declarationConceptName = new ConceptName("TODO");
		return new TypeCheckInfo(declarationConceptName, null, this, expression);		
	}
	
	private TypeCheckInfo getType(SadlSimpleTypeReference expression) throws DontTypeCheckException, CircularDefinitionException, InvalidNameException, TranslationException, URISyntaxException, IOException, ConfigurationException, InvalidTypeException, CircularDependencyException, PropertyWithoutRangeException {
		TypeCheckInfo tci = getType(expression.getType());
		if (expression.isList()) {
			tci.setRangeValueType(RangeValueType.LIST);
			int[] lenRest = getModelProcessor().getLengthRestrictions(expression.eContainer());
			Node tctype = tci.getTypeCheckType();
			if (lenRest != null && tctype instanceof NamedNode) {
				if (lenRest.length == 1) {
					((NamedNode)tctype).setListLength(lenRest[0]);
				}
				else if (lenRest.length == 2) {
					((NamedNode)tctype).setMinListLength(lenRest[0]);
					((NamedNode)tctype).setMaxListLength(lenRest[1]);
				}
			}
		}
		return tci;
	}

	private TypeCheckInfo getType(SadlUnionType expression) {
		ConceptName declarationConceptName = new ConceptName("TODO");
		return new TypeCheckInfo(declarationConceptName, null, this, expression);		
	}

	private TypeCheckInfo getType(PropOfSubject expression) throws InvalidNameException, TranslationException, URISyntaxException, IOException, ConfigurationException, DontTypeCheckException, CircularDefinitionException, InvalidTypeException, CircularDependencyException, PropertyWithoutRangeException {
		String ofOp = expression.getOf();
		Expression predicate = expression.getLeft();
		Expression subject = expression.getRight();
		boolean isNegated = false;
		if (predicate instanceof UnaryExpression && ((UnaryExpression)predicate).getOp().equals("not")) {
			predicate = ((UnaryExpression)predicate).getExpr();
			isNegated = true;
		}
		
		if (predicate instanceof Constant) {
			String cnstval = ((Constant)predicate).getConstant();
			TypeCheckInfo subjtype = null;
			if (constantRequiresListNext(cnstval)) {
				subjtype = getType(subject);
				if (subjtype != null && !subjtype.getRangeValueType().equals(RangeValueType.LIST)) {
					getModelProcessor().addIssueToAcceptor(SadlErrorMessages.MUST_BE_APPLIED_TO_LIST.get(cnstval, getTypeCheckTypeString(subjtype)), subject);
//					getModelProcessor().addIssueToAcceptor("'" + cnstval + "' must be applied to a List ('" + getTypeCheckTypeString(subjtype) + "' is not a List)", subject);
				}
			}
			else if (constantFollowedByIntThenList(cnstval)) {
				subjtype = getType(subject);
				
			}
			else if (constantFollowedByElementThenList(cnstval)) {
				subjtype = getType(subject);
				
			}
			if (cnstval.endsWith("length") || cnstval.equals("count") || cnstval.endsWith("index")) {
				NamedNode tctype = new NamedNode(XSD.xint.getURI(), NodeType.DataTypeNode);
				ConceptName nlcn = getModelProcessor().namedNodeToConceptName(tctype);
				return new TypeCheckInfo(nlcn, tctype, this, expression);
			}
			else if (subjtype != null && (cnstval.endsWith(" element"))) {
				if (subjtype != null && (cnstval.equals("first element") || cnstval.equals("last element")) ) {
					subjtype.setRangeValueType(RangeValueType.CLASS_OR_DT);   	// keep type but change from List to reflect this is an element of the list
					return subjtype;
				}
				String article = cnstval.substring(0, cnstval.indexOf(" "));
				subjtype.setRangeValueType(RangeValueType.CLASS_OR_DT);   	// keep type but change from List to reflect this is an element of the list
				return subjtype;
			
			}
			else if (cnstval.equals("a type")) {
				NamedNode tctype = new NamedNode(RDFS.subClassOf.getURI(), NodeType.ClassNode);
				ConceptName rdfType = getModelProcessor().namedNodeToConceptName(tctype);
				return new TypeCheckInfo(rdfType, tctype, this, expression);
			}
			else {
				getModelProcessor().addIssueToAcceptor(SadlErrorMessages.UNHANDLED.get("Constant Property", cnstval), expression);
				if (metricsProcessor != null) {
					metricsProcessor.addMarker(null, MetricsProcessor.ERROR_MARKER_URI, MetricsProcessor.TYPE_CHECK_FAILURE_URI);
				}
			}
		}
		else if (predicate instanceof ElementInList) {
			// this is like the constant "element"
			TypeCheckInfo subjtype = getType(subject);
			subjtype.setRangeValueType(RangeValueType.CLASS_OR_DT);
			return subjtype;
		}
		if (predicate instanceof Name) {
			try {
				OntConceptType predtype = declarationExtensions.getOntConceptType(((Name)predicate).getName());
				if (ofOp != null && ofOp.equals("in")) {
					// this is a list construct: element in list
				}
				else if (!predtype.equals(OntConceptType.CLASS_PROPERTY) && !predtype.equals(OntConceptType.DATATYPE_PROPERTY) && 
						!predtype.equals(OntConceptType.RDF_PROPERTY) && !predtype.equals(OntConceptType.ANNOTATION_PROPERTY)) {
					getModelProcessor().addIssueToAcceptor(SadlErrorMessages.EXPECTED_A.get("property in property chain"), predicate);
					//String preduri = declarationExtensions.getConceptUri(((Name)predicate).getName());
				}
			} catch (CircularDefinitionException e) {
				e.printStackTrace();
			}
		}
		boolean validSubject = true;
		if (subject instanceof Name) {
			// check for applicable local restriction first
			TypeCheckInfo predicateType;
			try {
				predicateType = getApplicableLocalRestriction(subject, predicate);
				if (predicateType != null) {
					if (subject instanceof PropOfSubject) {
						checkEmbeddedPropOfSubject(subject, predicate);
					}
					//
					if(predicateType.getTypeCheckType() != null){
						addEffectiveRange(predicateType, subject);
					}
					return predicateType;
				}
			} catch (PrefixNotFoundException e) {
				e.printStackTrace();
			} catch (InvalidTypeException e) {
				e.printStackTrace();
			}
			// check for AllValuesFrom restriction before defaulting to checking property range
			predicateType = getTypeFromRestriction(subject, predicate);
			if (predicateType != null) {
				if (subject instanceof PropOfSubject) {
					checkEmbeddedPropOfSubject(subject, predicate);
				}
				//
				if(predicateType.getTypeCheckType() != null){
					addEffectiveRange(predicateType, subject);
				}
				return predicateType;
			}
		}
		else {
//			getModelProcessor().addIssueToAcceptor("Property of subject has unexpected subject type '" + subject.getClass().getCanonicalName() + "'", expression);
			// we don't care about any other types?
			validSubject = false;
		}
		if (predicate != null) {
			TypeCheckInfo predicateType = getType(predicate);
			if (subject instanceof PropOfSubject) {
				//TODO figure out how to check local restrictions before general check
				TypeCheckInfo subjtci = getType(((PropOfSubject)subject).getRight());
				Object transobj = getModelProcessor().processExpression(((PropOfSubject)subject));
				if (transobj != null) {
					TypeCheckInfo lr = getApplicableLocalRestriction(generateLocalRestrictionKey(transobj));
					if (lr != null) {
						return lr;
					}
				}
				checkEmbeddedPropOfSubject(subject, predicate);	
			}else if(validSubject && predicateType != null && predicateType.getTypeCheckType() != null){
				//add interface range
				addEffectiveRange(predicateType, subject);
			}
			else if (subject instanceof SubjHasProp && SadlASTUtils.isUnitExpression(subject)) {
				issueAcceptor.addWarning("Units are associated with the subject of this expression; should the expression be in parentheses?", subject);
			}
			return predicateType;
		}
		return null;
	}
	
	protected String generateLocalRestrictionKey(Object transobj) {
		String key = transobj instanceof NamedNode ? ((NamedNode)transobj).toFullyQualifiedString() : transobj.toString();
		return key;
	}

	private void addEffectiveRange(TypeCheckInfo predicateType, Expression subject) throws CircularDefinitionException, InvalidTypeException, CircularDependencyException{
		if(metricsProcessor != null){
			try {
				if (subject instanceof Name) {
					String className = declarationExtensions.getConceptUri(((Name) subject).getName());
					SadlResource cls = ((Name) subject).getName();
					if (!declarationExtensions.getOntConceptType(cls).equals(OntConceptType.CLASS)) {
						// need to convert this to the Class representing the type; use existing type checking functionality
						TypeCheckInfo subjTCI = getType(cls);
						if (subjTCI != null /* && !subjTCI.getTypeCheckType().toString().equals("TODO")*/) {
							addEffectiveRangeByTypeCheckInfo(predicateType, subjTCI);
						}
					}
					else {
	//					cls = ((Name)subject).getName();
						addEffectiveRangeUnit(className, predicateType);
					}
				}
				else if (subject instanceof ElementInList) {
					TypeCheckInfo tci = getType(((ElementInList)subject));
					addEffectiveRangeByTypeCheckInfo(predicateType, tci);
				}
				else if (subject instanceof SubjHasProp) {
					TypeCheckInfo tci;
					tci = getType(((SubjHasProp)subject).getLeft());
					addEffectiveRangeByTypeCheckInfo(predicateType, tci);
				}
				else {
					throw new InvalidNameException("addEffectiveRange given a subject of type '" + subject.getClass().getCanonicalName() + "', not yet handled.");
				}
			} catch (TranslationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DontTypeCheckException e) {

			} catch (InvalidNameException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PropertyWithoutRangeException e) {

			} 
		}
	}

	private void addEffectiveRangeByTypeCheckInfo(TypeCheckInfo predicateType, TypeCheckInfo subjTCI)
			throws InvalidNameException, CircularDefinitionException {
		if (subjTCI.getCompoundTypes() != null) {
			Iterator<TypeCheckInfo> itr = subjTCI.getCompoundTypes().iterator();
			while (itr.hasNext()) {
				TypeCheckInfo nexttci = itr.next();
				addEffectiveRangeByTypeCheckInfo(predicateType, nexttci);
			}
		}
		else {
			Node ci = subjTCI.getTypeCheckType();
			if (ci instanceof NamedNode) {
				// this should be the class name
				String className = ((NamedNode) ci).toFullyQualifiedString();
				addEffectiveRangeUnit(className, predicateType);
			}
			else {
				throw new InvalidNameException("addEffectiveRangeByTypeCheckInfo called with TypeCheckInfo '" + subjTCI.toString() + ", which isn't handled.");
			}
		}
	}

	private void addEffectiveRangeUnit(String className, TypeCheckInfo predicateType) {
		String propertyName = predicateType.getExpressionType().toString();
		String rangeStr = predicateType.getTypeCheckType().toString();
		boolean isList = predicateType.getRangeValueType().equals(RangeValueType.LIST);
		metricsProcessor.addEffectiveRangeAndDomain(null, className, propertyName, rangeStr, isList);
	}
	
	private String getTypeCheckTypeString(TypeCheckInfo tci) {
		if (tci != null) {
			if (tci.typeCheckType != null) {
				return tci.typeCheckType.toString();
			}
			if (tci.getCompoundTypes() != null) {
				StringBuilder sb = new StringBuilder();
				Iterator<TypeCheckInfo> itr = tci.getCompoundTypes().iterator();
				int cntr = 0;
				while (itr.hasNext()) {
					if (cntr > 0) sb.append(" or ");
					sb.append(getTypeCheckTypeString(itr.next()));
					cntr++;
				}
				return sb.toString();
			}
		}
		return "(null)";
	}

	protected TypeCheckInfo getApplicableLocalRestriction(Expression subject, Expression predicate) throws IOException, PrefixNotFoundException, InvalidNameException, InvalidTypeException, TranslationException, ConfigurationException {
		return null;
	}

	private boolean constantFollowedByElementThenList(String cnstval) {
		if (cnstval.equals("index") ||
				cnstval.equals("count")) {
			return true;
		}
		return false;
	}
	
	private boolean constantFollowedByIntThenList(String cnstval) {
		if (cnstval.equals("element")) {
			return true;
		}
		return false;
	}

	private boolean constantRequiresListNext(String cnstval) {
		if (cnstval.equals("length") ||
				cnstval.endsWith(" element") ||
				cnstval.equals("first element") ||
				cnstval.equals("last element")) {
			return true;
		}
		return false;
	}

	private void checkEmbeddedPropOfSubject(Expression subject, Expression predicate) throws InvalidNameException, TranslationException, URISyntaxException, IOException, ConfigurationException, DontTypeCheckException, CircularDefinitionException, InvalidTypeException, CircularDependencyException, PropertyWithoutRangeException {
		if (predicate instanceof Name) {
			String propuri = declarationExtensions.getConceptUri(((Name)predicate).getName());
			OntConceptType oct = declarationExtensions.getOntConceptType(((Name)predicate).getName());
			if (oct.equals(OntConceptType.ANNOTATION_PROPERTY)) {
				issueAcceptor.addWarning(SadlErrorMessages.DOMAIN_MATCHING.get("annotation property"), predicate);
				return;
			}
			Property prop = theJenaModel.getProperty(propuri);
			TypeCheckInfo subjType = getType(subject);
			List<OntClass> subjClasses = subjType != null ? getTypeCheckTypeClasses(subjType) : null;
			StmtIterator domainItr = prop.listProperties(RDFS.domain);
			boolean domainMatched = false;
			List<Resource> domainList = null;
			while (domainItr.hasNext() && !domainMatched) {
				RDFNode dmn = domainItr.next().getObject();
				if (dmn instanceof Resource) {
					if (dmn.isURIResource()) {
						for (int i = 0; subjClasses != null && i < subjClasses.size(); i++) {
							if (subjClasses.get(i).getURI().equals(((Resource) dmn).getURI())) {
								domainItr.close();
								domainMatched = true;		// this is a direct match
								break;
							}
						}
					}
					if (!domainMatched) {
						if (domainList == null) domainList = new ArrayList<Resource>();
						domainList.add((Resource) dmn);
					}
				}
			}
			if (!domainMatched) {
				// there was no direct match
				for (int i = 0; domainList != null && i < domainList.size(); i++) {
					Resource dmn = domainList.get(i);
					if ((dmn instanceof OntResource || dmn.canAs(OntResource.class)) && subjType != null && subjType.getTypeCheckType() != null) {
						try {
							for (int j = 0; subjClasses != null && j < subjClasses.size(); j++) {
								OntClass subj = subjClasses.get(j);
//								if ( SadlUtils.classIsSubclassOf(subj, dmn.as(OntResource.class),true, null)) {
								if (dmn.canAs(OntClass.class)) { 
									if ( SadlUtils.classIsSubclassOf(dmn.as(OntClass.class), subj,true, null)) {
										domainMatched = true;
										break;
									}
								}
							}
						} catch (CircularDependencyException e) {
							e.printStackTrace();
						}
					}
				}
			}
			if (!domainMatched && domainList != null) {
				// there wasn't any subclass match either so create an appropriate error message
				try {
					oct = declarationExtensions.getOntConceptType(((Name)predicate).getName());
					StringBuilder errorMessageBuilder = new StringBuilder();
					TypeCheckInfo leftTypeCheckInfo;
					if (domainList.size() == 1) {
						ConceptName cn1 = createTypedConceptName(propuri, oct);
						NamedNode cn2 = null;
						if (domainList.get(0).isURIResource()) {
							cn2 = new NamedNode(domainList.get(0).getURI(), NodeType.ClassNode);
							leftTypeCheckInfo = new TypeCheckInfo(createTypedConceptName(propuri, oct), cn2, this, predicate);
						}
						else {						
							leftTypeCheckInfo = createTypeCheckInfoForNonUriPropertyRange(domainList.get(0), cn1, predicate, cn1.getType());
						}
						leftTypeCheckInfo.setTypeToExprRelationship("domain");
					}
					else {
						leftTypeCheckInfo = new TypeCheckInfo(createTypedConceptName(propuri, oct), this, predicate);
						for (int i = 0; i < domainList.size(); i++) {
							Resource dmn = domainList.get(i);
							if (dmn.isURIResource()) {
								TypeCheckInfo tci = new TypeCheckInfo(createTypedConceptName(propuri, oct), new NamedNode(domainList.get(i).getURI(), NodeType.ClassNode), this, predicate);
								leftTypeCheckInfo.addCompoundType(tci);
								leftTypeCheckInfo.setTypeToExprRelationship("domain");
							}
							else {
								ConceptName cn = createTypedConceptName(propuri, oct);
								TypeCheckInfo tci = createTypeCheckInfoForNonUriPropertyRange(dmn, cn, predicate, cn.getType());
								leftTypeCheckInfo.addCompoundType(tci);
								leftTypeCheckInfo.setTypeToExprRelationship("domain");
							}
						}
					}
					createErrorMessage(errorMessageBuilder, leftTypeCheckInfo, subjType, "chained property", true, predicate);
				} catch (CircularDefinitionException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	protected List<OntClass> getTypeCheckTypeClasses(TypeCheckInfo tci) {
		List<OntClass> results = null;
		if (tci.compoundTypes != null) {
			for (int i = 0; i < tci.compoundTypes.size(); i++) {
				TypeCheckInfo subtci = tci.compoundTypes.get(i);
				List<OntClass> subresults = getTypeCheckTypeClasses(subtci);
				if (results == null) {
					results = subresults;
				}
				else {
					results.addAll(subresults);
				}
			}
		}
		else {
			if (tci.getTypeCheckType() != null && tci.getTypeCheckType().toFullyQualifiedString() != null) {
				OntClass result = theJenaModel.getOntClass(tci.getTypeCheckType().toFullyQualifiedString());
				if (result != null) {
					results = new ArrayList<OntClass>();
					results.add(result);
				}
			}
		}
		return results;
	}

	protected TypeCheckInfo getTypeFromRestriction(Expression subject, Expression predicate) throws CircularDefinitionException, InvalidTypeException, DontTypeCheckException, InvalidNameException, TranslationException, URISyntaxException, IOException, ConfigurationException, CircularDependencyException, PropertyWithoutRangeException {
		if (subject instanceof Name && predicate instanceof Name) {
			String subjuri = declarationExtensions.getConceptUri(((Name)subject).getName());
			OntConceptType subjtype = declarationExtensions.getOntConceptType(((Name)subject).getName());
			if (subjtype.equals(OntConceptType.VARIABLE)) {
				TypeCheckInfo varTci = getType(((Name)subject).getName());
				if (varTci != null && varTci.getTypeCheckType() != null) {
					Node varci = varTci.getTypeCheckType();
					if (varci instanceof NamedNode) {
						if (((NamedNode) varci).toString().equals("TODO")) {
							return null;
						}
						if (((NamedNode) varci).getName() == null || ((NamedNode) varci).getNamespace() == null) {
							return null;
						}
						subjuri = ((NamedNode)varci).toFullyQualifiedString();
					}
				}
			}
			String propuri = declarationExtensions.getConceptUri(((Name)predicate).getName());
			if (propuri == null) {
				getModelProcessor().addIssueToAcceptor("Predicate name could not be resolved", predicate);
			}
			else {
				OntConceptType proptype = declarationExtensions.getOntConceptType(((Name)predicate).getName());
				return getTypeFromRestriction(subjuri, propuri, proptype, predicate);
			}
		}
		return null;
	}
	
	protected TypeCheckInfo getTypeFromRestriction(String subjuri, String propuri, OntConceptType proptype, Expression predicate) throws InvalidTypeException, TranslationException {
		Resource subj = theJenaModel.getResource(subjuri);
		if (subj != null) {
			if (!(subj instanceof OntClass || subj.canAs(OntClass.class)) && subj.canAs(Individual.class)) {
				ExtendedIterator<Resource> subjects = subj.as(Individual.class).listRDFTypes(true);
				while(subjects.hasNext()) {
					TypeCheckInfo type = getTypeFromRestriction(subjects.next(), propuri, proptype, predicate);
					if(type != null) {
						return type;
					}
				}
			}else {
				return getTypeFromRestriction(subj, propuri, proptype, predicate);
			}
		}
		return null;
	}
	
	protected TypeCheckInfo getTypeFromRestriction(Resource subj, String propuri, OntConceptType proptype, Expression predicate) throws InvalidTypeException, TranslationException {
		if (subj != null && subj.canAs(OntClass.class)){ 
			Property prop = theJenaModel.getProperty(propuri);
			// look for restrictions on "range"
			StmtIterator sitr = theJenaModel.listStatements(null, OWL.onProperty, prop);
			while (sitr.hasNext()) {
				Statement stmt = sitr.nextStatement();
				Resource sr = stmt.getSubject();
				if (sr.canAs(OntClass.class) && subj.as(OntClass.class).hasSuperClass(sr.as(OntClass.class))) {
					if (sr.as(OntClass.class).asRestriction().isAllValuesFromRestriction()) {
						Resource avf = sr.as(OntClass.class).asRestriction().asAllValuesFromRestriction().getAllValuesFrom();
						if (avf.isLiteral()) {
							NamedNode tctype = new NamedNode(avf.getURI(), NodeType.ClassNode);
							TypeCheckInfo avftci =  new TypeCheckInfo(createTypedConceptName(propuri, proptype), tctype, this, predicate);
							avftci.setTypeToExprRelationship("restriction to");
							return avftci;
						}
						else if (avf.isURIResource()){
							List<ConceptName> impliedProperties = getImpliedProperties(avf);
							NamedNode tctype = new NamedNode(avf.getURI(), NodeType.ClassNode);
							TypeCheckInfo avftci = new TypeCheckInfo(createTypedConceptName(propuri, proptype), tctype, impliedProperties, this, predicate);
							avftci.setTypeToExprRelationship("restriction to");
							if (isTypedListSubclass(avf)) {
								avftci.setTypeCheckType(getTypedListType(avf));
								avftci.setRangeValueType(RangeValueType.LIST);
							}
							return avftci;
						}
					}
					else if (sr.as(OntClass.class).asRestriction().isHasValueRestriction()) {
						RDFNode hvr = sr.as(OntClass.class).asRestriction().asHasValueRestriction().getHasValue();
						TypeCheckInfo hvtci = new TypeCheckInfo(createTypedConceptName(propuri, proptype), 
							hvr, ExplicitValueType.RESTRICTION, this, predicate);
						if (isTypedListSubclass(hvr)) {
							hvtci.setTypeCheckType(getTypedListType(hvr));
							hvtci.setRangeValueType(RangeValueType.LIST);
						}
						return hvtci;
					}
				}
			}
		}
		return null;
	}
	
	private NamedNode getTypedListType(RDFNode node) throws TranslationException {
		return modelProcessor.getTypedListType(node);
	}

	private boolean isTypedListSubclass(RDFNode rest) {
		if (rest.isResource()) {
			Resource lstcls = theJenaModel.getResource(SadlConstants.SADL_LIST_MODEL_LIST_URI);
			if (lstcls != null && rest.asResource().hasProperty(RDFS.subClassOf, lstcls)) {	// if there are no lists the list model will not have been imported
				return true;
			}
		}
		return false;
	}

	private TypeCheckInfo getType(Name expression) throws InvalidNameException, TranslationException, URISyntaxException, IOException, ConfigurationException, DontTypeCheckException, CircularDefinitionException, InvalidTypeException, CircularDependencyException, PropertyWithoutRangeException {
		SadlResource qnm =expression.getName();
		if (qnm.eIsProxy()) {
			handleUndefinedFunctions(expression);
		}
		
		//If the expression is a function, find equation definition from name and get the return type
		if(expression.isFunction()){
			try {
				TypeCheckInfo ftci = getFunctionType(qnm);
				if (qnm.eContainer() instanceof ExternalEquationStatement) {
					EList<Expression> args = expression.getArglist();
					EList<SadlParameterDeclaration> params = ((ExternalEquationStatementImpl)qnm.eContainer()).getParameter();
					checkFunctionArguments(params, args, expression);
				}
				else if (qnm.eContainer() instanceof EquationStatement) {
					EList<Expression> args = expression.getArglist();
					EList<SadlParameterDeclaration> params = ((EquationStatement)qnm.eContainer()).getParameter();
					checkFunctionArguments(params, args, expression);
				}
				if (ftci != null) {
					return ftci;
				}
			}
			catch (DontTypeCheckException e) {
				getModelProcessor().addIssueToAcceptor("External equation declaration does not provide type information; can't type check.", expression);
				throw e;
			}
			handleUndefinedFunctions(expression);
		}
		return getType(qnm, expression);
	}

	private void checkFunctionArguments(EList<SadlParameterDeclaration> params, EList<Expression> args, Name expression)
			throws InvalidTypeException, TranslationException {
		boolean variableNumArgs = false;
		int minNumArgs = 0;
		if (args.size() != params.size() || (params.size() > 1 && params.get(params.size() - 1).getEllipsis() != null)) {
			boolean wrongNumArgs = true;
			if (params.get(params.size() - 1).getEllipsis() != null) {
				minNumArgs = params.size() - 1;
				variableNumArgs = true;
				if (args.size() >= minNumArgs) {
					wrongNumArgs = false;
				}
			}
			if (wrongNumArgs) {
				getModelProcessor().addIssueToAcceptor("Number of arguments does not match function declaration", expression);
			}
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < args.size(); i++) {
			Expression arg = args.get(i);
			SadlParameterDeclaration param = null;
			if (variableNumArgs) {
				param = (i >= minNumArgs) ? params.get(minNumArgs - 1) : params.get(i);
			}
			else if (i < params.size()) {
				param = params.get(i);
			}
			if (param != null) {
				validate(arg, param.getType(), "argument", sb);
				if (sb.length() > 0) {
					getModelProcessor().addIssueToAcceptor(sb.toString(), expression);
				}
			}
		}
	}
	
	private TypeCheckInfo getFunctionType(SadlResource fsr) throws DontTypeCheckException, CircularDefinitionException, InvalidNameException, TranslationException, URISyntaxException, IOException, ConfigurationException, InvalidTypeException, CircularDependencyException, PropertyWithoutRangeException {
		if(fsr.eContainer() instanceof EquationStatement){
			EquationStatement es = (EquationStatement) fsr.eContainer();
			if(es != null){
				return getType(es.getReturnType());
			}
		}else if(fsr.eContainer() instanceof ExternalEquationStatement){
			ExternalEquationStatement ees = (ExternalEquationStatement)fsr.eContainer();
			if(ees != null) {
				if (ees.getUnknown() == null){
					return getType(ees.getReturnType());
				}
				else {
					throw new DontTypeCheckException();
				}
			}
		}
		return null;
	}
	
	private boolean isInQuery(EObject expression) {
		EObject cntr = expression.eContainer();
		if (cntr != null) {
			if (cntr instanceof QueryStatement) {
				return true;
			}
			return isInQuery(cntr);
		}
		return false;
	}

	protected void handleUndefinedFunctions(Name expression) throws ConfigurationException, DontTypeCheckException, InvalidTypeException{
		String expressionName = declarationExtensions.getConcreteName(expression);
		ITranslator translator = getModelProcessor().getTranslator();
		//If only names for built-in functions are available, check the name matches a built-in functions. If not, error.
		if (translator == null) {
			issueAcceptor.addWarning(SadlErrorMessages.TYPE_CHECK_TRANSLATOR_CLASS_NOT_FOUND.get(getModelProcessor().getConfigMgr().getTranslatorClassName()), expression);
			if (metricsProcessor != null) {
				metricsProcessor.addMarker(null, MetricsProcessor.WARNING_MARKER_URI, MetricsProcessor.UNDEFINED_FUNCTION_URI);
			}
		}
		else if(translator.isBuiltinFunctionTypeCheckingAvailable() == SadlConstants.SADL_BUILTIN_FUNCTIONS_TYPE_CHECKING_AVAILABILITY.NAME_ONLY){	
			if(translator.isBuiltinFunction(expressionName)){
				issueAcceptor.addWarning(SadlErrorMessages.TYPE_CHECK_BUILTIN_EXCEPTION.get(expressionName), expression);
				if (metricsProcessor != null) {
					metricsProcessor.addMarker(null, MetricsProcessor.WARNING_MARKER_URI, MetricsProcessor.UNDEFINED_FUNCTION_URI);
				}
			}else{
				getModelProcessor().addIssueToAcceptor(SadlErrorMessages.RETURN_TYPE_WARNING.get("Function " + expressionName), expression);
				if (metricsProcessor != null) {
					metricsProcessor.addMarker(null, MetricsProcessor.ERROR_MARKER_URI, MetricsProcessor.UNDEFINED_FUNCTION_URI);
				}
			}				
		}
		//Either the Reasoner/Translator provides full built-in information or provides nothing. 
		//Regardless, if this point is reached, error.
		else {
			getModelProcessor().addIssueToAcceptor(SadlErrorMessages.RETURN_TYPE_WARNING.get("Function " + expressionName), expression);
			if (metricsProcessor != null) {
				metricsProcessor.addMarker(null, MetricsProcessor.ERROR_MARKER_URI, MetricsProcessor.UNDEFINED_FUNCTION_URI);
			}
		}
		throw new DontTypeCheckException();
	}
	
	protected TypeCheckInfo getType(SadlResource sr) throws DontTypeCheckException, CircularDefinitionException, InvalidNameException, TranslationException, URISyntaxException, IOException, ConfigurationException, InvalidTypeException, CircularDependencyException, PropertyWithoutRangeException {
		return getType(sr, sr);
	}
	
	protected TypeCheckInfo getType(SadlResource sr, EObject reference) throws DontTypeCheckException, CircularDefinitionException, InvalidNameException, TranslationException, URISyntaxException, IOException, ConfigurationException, InvalidTypeException, CircularDependencyException, PropertyWithoutRangeException{
		String conceptUri = declarationExtensions.getConceptUri(sr);
		EObject expression = sr.eContainer();
		if (conceptUri == null) {
			getModelProcessor().addIssueToAcceptor(SadlErrorMessages.UNIDENTIFIED.toString(), (reference != null ? reference : sr));
			if (metricsProcessor != null) {
				metricsProcessor.addMarker(null, MetricsProcessor.ERROR_MARKER_URI, MetricsProcessor.TYPE_CHECK_FAILURE_URI);
			}
		}
		
		OntConceptType conceptType;
		try {
			conceptType = declarationExtensions.getOntConceptType(sr);
		} catch (CircularDefinitionException e) {
			conceptType = e.getDefinitionType();
			getModelProcessor().addIssueToAcceptor(e.getMessage(), reference);
			if (metricsProcessor != null) {
				metricsProcessor.addMarker(null, MetricsProcessor.ERROR_MARKER_URI, MetricsProcessor.TYPE_CHECK_FAILURE_URI);
			}
		}
		if(conceptType.equals(OntConceptType.CLASS) || conceptType.equals(OntConceptType.DATATYPE)){
			NamedNode tctype = createNamedNode(conceptUri, conceptType);
			ConceptName conceptName = getModelProcessor().namedNodeToConceptName(tctype);
			if (conceptName.getUri().equals(SadlConstants.SADL_IMPLICIT_MODEL_UNITTEDQUANTITY_URI) && getModelProcessor().ignoreUnittedQuantities) {
				if (expression instanceof SadlClassOrPropertyDeclaration) {
					Iterator<SadlProperty> spitr = ((SadlClassOrPropertyDeclaration)expression).getDescribedBy().iterator();
					while (spitr.hasNext()) {
						SadlProperty sp = spitr.next();
						if (declarationExtensions.getConceptUri(sp.getProperty()).equals(SadlConstants.SADL_IMPLICIT_MODEL_VALUE_URI)) {
							return getType(sr, declarationExtensions.getDeclaration(sp.getProperty()));
						}
					}
				}
				else {
					getModelProcessor().addIssueToAcceptor("Can't handle this expression type when ignoring UnittedQuantities",expression);
				}
			}
			else {
				List<ConceptName> impliedProps = getImpliedProperties(theJenaModel.getResource(conceptUri));
				TypeCheckInfo tci = new TypeCheckInfo(conceptName, tctype, this, impliedProps, reference);
				if (conceptType.equals(OntConceptType.CLASS)) {
 					tci.setTypeToExprRelationship("self");
 				}
				return tci;
			}
		}
		else if(conceptType.equals(OntConceptType.DATATYPE_PROPERTY)){
			TypeCheckInfo propcheckinfo = getNameProperty(sr, ConceptType.DATATYPEPROPERTY, conceptUri, reference);
			if (propcheckinfo != null) {
				return propcheckinfo;
			}
			throw new PropertyWithoutRangeException(declarationExtensions.getConcreteName(sr));
		}
		else if(conceptType.equals(OntConceptType.CLASS_PROPERTY)){
			TypeCheckInfo propcheckinfo =  getNameProperty(sr, ConceptType.OBJECTPROPERTY, conceptUri, reference);
			if (propcheckinfo != null) {
				return propcheckinfo;
			}
			throw new PropertyWithoutRangeException(declarationExtensions.getConcreteName(sr));
		}
		else if (conceptType.equals(OntConceptType.RDF_PROPERTY)) {
			TypeCheckInfo rdfpropcheckinfo = getNameProperty(sr, ConceptType.RDFPROPERTY, conceptUri, reference);
			if (rdfpropcheckinfo != null) {
				return rdfpropcheckinfo;
			}
			throw new PropertyWithoutRangeException(declarationExtensions.getConcreteName(sr));
		}
		else if(conceptType.equals(OntConceptType.INSTANCE)){
			// this is an instance--if it is already in the ontology we can get its type. If not maybe we can get it from its declaration
			Individual individual = theJenaModel.getIndividual(conceptUri);
			if(individual == null){
				SadlResource qnmDecl = declarationExtensions.getDeclaration(sr);
				if (qnmDecl != null) {
					if (qnmDecl.eContainer() instanceof SadlInstance) {
						SadlTypeReference typeref = ((SadlInstance)qnmDecl.eContainer()).getType();
						if (typeref != null) {
							return getType(typeref);
						}
						else {
							SadlResource nor = ((SadlInstance)qnmDecl.eContainer()).getNameOrRef();
							if (!nor.equals(qnmDecl)) {
								return getType(nor);
							}
						}
					}
					else if (qnmDecl.eContainer() instanceof SadlMustBeOneOf) {
						if (qnmDecl.eContainer().eContainer() instanceof SadlClassOrPropertyDeclaration) {
							return getType(((SadlClassOrPropertyDeclaration)qnmDecl.eContainer().eContainer()).getClassOrProperty().get(0));
						}
					}
				}
				getModelProcessor().addIssueToAcceptor(SadlErrorMessages.UNIDENTIFIED.toString(), reference);
				if (metricsProcessor != null) {
					metricsProcessor.addMarker(null, MetricsProcessor.ERROR_MARKER_URI, MetricsProcessor.TYPE_CHECK_FAILURE_URI);
				}
				return null;
			}
			ConceptName instConceptName = new ConceptName(conceptUri);
			instConceptName.setType(ConceptType.INDIVIDUAL);
			// TODO could belong to multiple classes
			ExtendedIterator<Resource> typeitr = individual.listRDFTypes(true);
			TypeCheckInfo compoundTci = null;
			TypeCheckInfo tci = null;
			while (typeitr.hasNext()) {
				Resource ontResource = typeitr.next();
				if(!ontResource.isURIResource()){
					if (isSadlTypedList(ontResource) && ontResource.canAs(OntClass.class)) {
						tci = getSadlTypedListTypeCheckInfo(ontResource.as(OntClass.class), null, reference, null);
					}
					else {
						ConceptName declarationConceptName = new ConceptName("TODO");
						declarationConceptName.setType(ConceptType.ONTCLASS);
						tci =  new TypeCheckInfo(instConceptName, null, this, reference);
					}
				}
				else {
					String uriOfTypeToBeReturned = ontResource.getURI();
					NamedNode tctype = new NamedNode(uriOfTypeToBeReturned, NodeType.ClassNode);
					ConceptName conceptName = getModelProcessor().namedNodeToConceptName(tctype);
					List<ConceptName> impliedProperties = getImpliedProperties(ontResource);
					tci = new TypeCheckInfo(instConceptName, tctype, this, impliedProperties, reference);
				}
				if (typeitr.hasNext() && compoundTci == null) {
					compoundTci = new TypeCheckInfo(instConceptName, this, reference);
				}
				if (compoundTci != null) {
					if (compoundTci.getCompoundTypes() == null || !compoundTci.getCompoundTypes().contains(tci)) {
						compoundTci.addCompoundType(tci);
					}
				}
			}
			if (compoundTci != null) {
				return compoundTci;
			}
			if (tci == null) {
				tci = new TypeCheckInfo(instConceptName);
			}
			return tci;
		}
		else if(conceptType.equals(OntConceptType.VARIABLE)){
			String nm = declarationExtensions.getConcreteName(sr);
			String uri = declarationExtensions.getConceptUri(sr);
			TypeCheckInfo tci = getVariableType(ConceptType.VARIABLE, sr, nm, uri, reference);
			if (tci != null) {				// will be null on invalid input, e.g., during clean
				ConceptName et = new ConceptName(uri);
				et.setType(ConceptType.VARIABLE);
				tci.setExpressionType(et);
			}
			return tci;
		}
		else if(conceptType.equals(OntConceptType.ANNOTATION_PROPERTY)){
			//This matches any type.
			ConceptName declarationConceptName = new ConceptName("TODO");
			TypeCheckInfo tci = new TypeCheckInfo(declarationConceptName, null, this, reference);
			tci.setAdditionalInformation("" + conceptUri + "' is an annotation property and has no range");
			tci.setSeverity(Severity.IGNORE);
			return tci;
		}
		else if (conceptType.equals(OntConceptType.FUNCTION_DEFN)) {
			return getFunctionType(sr);
		}
		else if (conceptType.equals(OntConceptType.CLASS_LIST)) {
			if (conceptUri != null) {
				OntClass ontcls = theJenaModel.getOntClass(conceptUri);
				ConceptName typecn = getListClassType(ontcls);
				conceptUri = typecn.toFQString();
			}
			SadlResource declsr = declarationExtensions.getDeclaration(sr);
			NamedNode tctype = new NamedNode(conceptUri, NodeType.ClassListNode);
			ConceptName conceptName = getModelProcessor().namedNodeToConceptName(tctype);
			if (conceptName.getUri().equals(SadlConstants.SADL_IMPLICIT_MODEL_UNITTEDQUANTITY_URI) && getModelProcessor().ignoreUnittedQuantities) {
				if (expression instanceof SadlClassOrPropertyDeclaration) {
					Iterator<SadlProperty> spitr = ((SadlClassOrPropertyDeclaration)expression).getDescribedBy().iterator();
					while (spitr.hasNext()) {
						SadlProperty sp = spitr.next();
						if (declarationExtensions.getConceptUri(sp.getProperty()).equals(SadlConstants.SADL_IMPLICIT_MODEL_VALUE_URI)) {
							return getType(sp.getProperty(), declarationExtensions.getDeclaration(sp.getProperty()));
						}
					}
				}
				else {
					getModelProcessor().addIssueToAcceptor("Can't handle this expression type when ignoring UnittedQuantities",expression);
				}
			}
			else {
				List<ConceptName> impliedProps = getImpliedProperties(theJenaModel.getResource(conceptUri));
				TypeCheckInfo tci = new TypeCheckInfo(conceptName, tctype, this, impliedProps, reference);
				return tci;
			}
		}
		
		ConceptName declarationConceptName = new ConceptName("TODO");
		return new TypeCheckInfo(declarationConceptName, null, this, reference);
	}
	
	private String getSadlModelUri(EObject eobj) {
		if (eobj.eContainer() instanceof SadlModel) {
			return ((SadlModel)eobj.eContainer()).getBaseUri();
		}
		else if (eobj.eContainer() != null) {
			return getSadlModelUri(eobj.eContainer());
		}
		return null;
	}

	private ConceptName createTypedConceptName(String conceptUri, OntConceptType conceptType) {
		return modelProcessor.createTypedConceptName(conceptUri, conceptType);
	}
	
	private NamedNode createNamedNode(String conceptUri, OntConceptType conceptType) {
		return modelProcessor.createNamedNode(conceptUri, conceptType);
	}

	protected TypeCheckInfo getNameProperty(SadlResource qnm, ConceptType propertyType, String conceptUri, EObject expression) throws DontTypeCheckException, InvalidTypeException, TranslationException, InvalidNameException {
		Property property = theJenaModel.getProperty(conceptUri);

		if(property == null){
			getModelProcessor().addIssueToAcceptor(SadlErrorMessages.UNIDENTIFIED.toString(), qnm != null ? qnm : expression);
			if (metricsProcessor != null) {
				metricsProcessor.addMarker(null, MetricsProcessor.ERROR_MARKER_URI, MetricsProcessor.UNCLASSIFIED_FAILURE_URI);
			}
			return null;
		}
		ConceptName propConceptName = new ConceptName(conceptUri);
		propConceptName.setType(propertyType);
		return getTypeInfoFromRange(propConceptName, property, expression);
	}

	private TypeCheckInfo getPropertyDomainType(SadlResource propsr, EObject expr) throws InvalidTypeException {
		String propuri = declarationExtensions.getConceptUri(propsr);
		Property prop = theJenaModel.getProperty(propuri);
		ConceptName pcn = new ConceptName(propuri);
		getModelProcessor().setPropertyConceptNameType(pcn, prop);
		return getTypeInfoFromDomain(pcn, prop, expr);
	}
	
	private TypeCheckInfo getPropertyRangeType(SadlResource propsr, EObject expr) throws DontTypeCheckException, InvalidTypeException, TranslationException, InvalidNameException {
		String propuri = declarationExtensions.getConceptUri(propsr);
		Property prop = theJenaModel.getProperty(propuri);
		ConceptName pcn = new ConceptName(propuri);
		getModelProcessor().setPropertyConceptNameType(pcn, prop);
		return getTypeInfoFromRange(pcn, prop, expr);
	}

	public TypeCheckInfo getTypeInfoFromDomain(ConceptName propConceptName, Property property, EObject expression) throws InvalidTypeException {
		StmtIterator stmtitr = theJenaModel.listStatements(property, RDFS.domain, (RDFNode)null);
		while (stmtitr.hasNext()) {
			RDFNode obj = stmtitr.nextStatement().getObject();
			if (obj.canAs(Resource.class)) {
				try {
					return createTypeCheckInfoForPropertyDomain(obj.asResource(), propConceptName, expression);
				} catch (DontTypeCheckException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
			}
		}
		return null;
	}

	public TypeCheckInfo getTypeInfoFromRange(ConceptName propConceptName, Property property,
			EObject expression) throws DontTypeCheckException, InvalidTypeException, TranslationException, InvalidNameException {
		ConceptType propertyType = propConceptName.getType();
		StmtIterator sitr = theJenaModel.listStatements(property, RDFS.range, (RDFNode)null);
		if (sitr.hasNext()) {
			TypeCheckInfo compoundTci = null;
			TypeCheckInfo tci = null;
			while (sitr.hasNext()) {
				RDFNode first = sitr.next().getObject();
				boolean isList = false;
				if(first.isURIResource()){
					tci = createTypeCheckInfoForPropertyRange(first, propConceptName, expression, propertyType);
				}
				else if (isSadlTypedList(first)) {
					// get type restriction on "first" property--this is the type
					tci = getSadlTypedListTypeCheckInfo(first.as(OntClass.class), propConceptName, expression, propertyType);
					isList = true;
				}
				else {
					tci = createTypeCheckInfoForNonUriPropertyRange(first, propConceptName, expression, propertyType);
				}
				if (tci != null) {
					if (sitr.hasNext() && compoundTci == null) {
						compoundTci = new TypeCheckInfo(propConceptName, this, expression);
						if (isList) {
							compoundTci.setRangeValueType(RangeValueType.LIST);
						}
					}
					if (compoundTci != null) {
						if (tci.getCompoundTypes() != null) {
							for (TypeCheckInfo subtci:tci.getCompoundTypes()) {
								compoundTci.addCompoundType(subtci);
							}
						}
						else {
							compoundTci.addCompoundType(tci);
						}
					}
				}
			}
			sitr.close();
			if (compoundTci != null) {
				return compoundTci;
			}
			return tci;
		}
		else {
			// no range on this property, check super properties
			StmtIterator sitr2 = theJenaModel.listStatements(property, RDFS.subPropertyOf, (RDFNode)null);
			while (sitr2.hasNext()) {
				RDFNode psuper = sitr2.next().getObject();
				if (psuper.isResource()) {
					TypeCheckInfo superTCInfo = getNameProperty(null, propertyType, psuper.asResource().getURI(), expression);
					if (superTCInfo != null) {
						sitr2.close();
						return superTCInfo;
					}
				}
			}
		}
		return null;
	}

	private TypeCheckInfo createTypeCheckInfoForNonUriPropertyRange(RDFNode rng, ConceptName propConceptName,
			EObject expression, ConceptType propertyType) throws InvalidTypeException, TranslationException, InvalidNameException {
		TypeCheckInfo tci = null;
		if (isSadlTypedList(rng)) {
			// get type restriction on "first" property--this is the type
			tci = getSadlTypedListTypeCheckInfo(rng.as(OntClass.class), propConceptName, expression, propertyType);
		}
		 else if (rng.canAs(UnionClass.class)){
			UnionClass ucls = rng.as(UnionClass.class);
			try {
				ExtendedIterator<? extends OntClass> eitr = ucls.listOperands();
				if (eitr.hasNext()) {
					tci = new TypeCheckInfo(propConceptName, this, expression);
					while (eitr.hasNext()) {
						OntClass uclsmember = eitr.next();
						if (uclsmember.isURIResource()) {
							TypeCheckInfo utci = createTypeCheckInfoForPropertyRange(uclsmember, propConceptName, expression, propertyType);
							tci.addCompoundType(utci);
						}
						else {
							TypeCheckInfo utci = createTypeCheckInfoForNonUriPropertyRange(uclsmember, propConceptName, expression, propertyType);
							tci.addCompoundType(utci);
						}
					}
				}
			}
			catch (Exception e) {
				getModelProcessor().addIssueToAcceptor(SadlErrorMessages.UNEXPECTED_TYPE_CHECK_ERROR.get("union range", e.getMessage() ), getDefaultContext());
			}
		}
		else if (rng.canAs(IntersectionClass.class)){
			issueAcceptor.addWarning(SadlErrorMessages.TYPE_CHECK_HANDLE_WARNING.get("intersections"), expression);
		}
		else if (rng.canAs(Restriction.class)){
			issueAcceptor.addWarning(SadlErrorMessages.TYPE_CHECK_HANDLE_WARNING.get("restrictions"), expression);
		}
		return tci;
	}

	private TypeCheckInfo createTypeCheckInfoForNonUriPropertyDomain(Resource domain, ConceptName propConceptName,
			EObject expression) throws InvalidTypeException {
		TypeCheckInfo tci = null;
		if (domain.canAs(UnionClass.class)){
			UnionClass ucls = domain.as(UnionClass.class);
			try {
				ExtendedIterator<? extends OntClass> eitr = ucls.listOperands();
				if (eitr.hasNext()) {
					tci = new TypeCheckInfo(propConceptName, this, expression);
					while (eitr.hasNext()) {
						OntClass uclsmember = eitr.next();
						if (uclsmember.isURIResource()) {
							TypeCheckInfo utci = createTypeCheckInfoForPropertyDomain(uclsmember, propConceptName, expression);
							tci.addCompoundType(utci);
						}
						else {
							TypeCheckInfo utci = createTypeCheckInfoForNonUriPropertyDomain(uclsmember, propConceptName, expression);
							tci.addCompoundType(utci);
						}
					}
				}
			}
			catch (Exception e) {
				getModelProcessor().addIssueToAcceptor(SadlErrorMessages.UNEXPECTED_TYPE_CHECK_ERROR.get("union range", e.getMessage() ), getDefaultContext());
			}
		}
		else if (domain.canAs(IntersectionClass.class)){
			issueAcceptor.addWarning(SadlErrorMessages.TYPE_CHECK_HANDLE_WARNING.get("intersections"), expression);
		}
		else {
			issueAcceptor.addError("Unhandled type of non-URI domain", expression);
		}
		return tci;
	}

	private TypeCheckInfo getSadlTypedListTypeCheckInfo(OntClass lst, ConceptName propConceptName, EObject expression, ConceptType propertyType) throws InvalidTypeException, TranslationException, InvalidNameException {
		Resource avf = getSadlTypedListType(lst);
		if (avf != null && avf.isURIResource()) {
//							List<ConceptName> impliedProperties = getImpliedProperties(avf.asResource());
			List<ConceptName> impliedProperties = null;		// don't impute implied properties when the range is a List
			NamedNode tctype = new NamedNode(avf.getURI());
			if (propertyType != null && propertyType.equals(ConceptType.DATATYPEPROPERTY)) {
				tctype.setNodeType(NodeType.DataTypeListNode);
			}
			else {
				tctype.setNodeType(NodeType.ClassListNode);
			}
			ConceptName rangeConceptName = getModelProcessor().namedNodeToConceptName(tctype);
			TypeCheckInfo tci = new TypeCheckInfo(propConceptName, tctype, impliedProperties, this, expression);
			tci.setRangeValueType(RangeValueType.LIST);
			return tci;
		}
		return null;
	}

	public boolean isSadlTypedList(RDFNode node) {
		if (node instanceof Resource && ((Resource)node).canAs(OntClass.class)) {
			OntClass cls = ((Resource)node).as(OntClass.class);
			OntResource lstrsrc = theJenaModel.getOntResource(SadlConstants.SADL_LIST_MODEL_LIST_URI);
			if (lstrsrc != null && cls.hasSuperClass(lstrsrc)) {		// if the model doesn't have any lists the list model will not have been imported
				return true;
			}
		}
		return false;
	}
	
	public Resource getSadlTypedListType(OntClass lstcls) {
		ExtendedIterator<OntClass> eitr = ((OntClass)lstcls.as(OntClass.class)).listSuperClasses(true);
		while (eitr.hasNext()) {
			OntClass cls = eitr.next();
			if (cls.isRestriction()) {
				if (cls.canAs(AllValuesFromRestriction.class)) {
					if (((AllValuesFromRestriction)cls.as(AllValuesFromRestriction.class)).onProperty(theJenaModel.getProperty(SadlConstants.SADL_LIST_MODEL_FIRST_URI))) {
						Resource avf = ((AllValuesFromRestriction)cls.as(AllValuesFromRestriction.class)).getAllValuesFrom();
						eitr.close();
						return avf;
					}
				}
			}
		}
		return null;
	}
	
	private TypeCheckInfo createTypeCheckInfoForPropertyDomain(Resource domain, ConceptName propConceptName, EObject expression) throws DontTypeCheckException, InvalidTypeException {
		TypeCheckInfo tci = null;
		if (!domain.isURIResource()) {
			return createTypeCheckInfoForNonUriPropertyDomain(domain, propConceptName, expression);
		}
		NamedNode tctype = new NamedNode(domain.getURI(), NodeType.ClassNode);
		if (tci == null) {
			tci = new TypeCheckInfo(propConceptName, tctype, this, expression);
			tci.setTypeToExprRelationship("domain");
		}
		if (isTypedListSubclass(domain)) {
			tci.setRangeValueType(RangeValueType.LIST);
		}
		return tci;
	}

	private TypeCheckInfo createTypeCheckInfoForPropertyRange(RDFNode first, ConceptName propConceptName,
			EObject expression, ConceptType propertyType) throws InvalidTypeException, TranslationException, InvalidNameException {
		TypeCheckInfo tci = null;
		NamedNode rangeNamedNode = new NamedNode(first.asResource().getURI(), NodeType.DataTypeNode);
//		ConceptName rangeConceptName = new ConceptName(first.asResource().getURI());
		if (propertyType.equals(ConceptType.DATATYPEPROPERTY)) {
//			rangeConceptName.setType(ConceptType.RDFDATATYPE);
			OntResource range;
			range = theJenaModel.getOntResource(rangeNamedNode.toFullyQualifiedString());
			if (theJenaModel.listStatements(range, RDF.type, RDFS.Datatype).hasNext()) {
				// this is a user-defined datatype
				RDFNode rngEC = range.listPropertyValues(OWL.equivalentClass).next();
				if (rngEC != null && rngEC.canAs(OntResource.class)) {
					RDFNode baseType = rngEC.as(OntResource.class).listPropertyValues(OWL2.onDatatype).next();
					if (baseType != null && baseType.isURIResource()) {
						NamedNode tctype = new NamedNode(baseType.asResource().getURI(), NodeType.DataTypeNode);
						tci = new TypeCheckInfo(propConceptName, tctype, this, expression);
					}
				}
			}
			else {
//					rangeConceptName.setRangeValueType(propConceptName.getRangeValueType());
				if (propConceptName.getRangeValueType().equals(RangeValueType.LIST)) {
					rangeNamedNode.setNodeType(NodeType.DataTypeListNode);
				}
			}
		}
		else {
			rangeNamedNode.setNodeType(NodeType.ClassNode);
		}
		List<ConceptName> impliedProperties = getImpliedProperties(first.asResource());
		if (tci == null) {
			tci = new TypeCheckInfo(propConceptName, rangeNamedNode, impliedProperties, this, expression);
		}
		else if (impliedProperties != null){
			tci.addImplicitProperties(impliedProperties);
		}
		if (isTypedListSubclass(first)) {
			tci.setRangeValueType(RangeValueType.LIST);
			if (first.isURIResource()) {
				// looks like a named list in which case we probably have the wrong type
				if (!first.asResource().canAs(OntClass.class)){
					issueAcceptor.addError("Unexpected non-OntClass named list, please report."	, expression); 
				}
				return getSadlTypedListTypeCheckInfo(first.asResource().as(OntClass.class), propConceptName, expression, propertyType);
			}
		}
		return tci;
	}

	protected List<ConceptName> getImpliedProperties(Resource first) throws InvalidTypeException {
		return getModelProcessor().getImpliedProperties(first);
	}

//	private boolean isRangeKlugyDATASubclass(OntResource rsrc) {
//		if (rsrc.getURI().endsWith("#DATA")) {
//			return true;
//		}
//		if (rsrc.canAs(OntClass.class)){
//			ExtendedIterator<OntClass> itr = rsrc.as(OntClass.class).listSuperClasses();
//			while (itr.hasNext()) {
//				OntClass spr = itr.next();
//				if (spr.isURIResource() && spr.getURI().endsWith("#DATA")) {
//					return true;
//				}
//			}
//		}
//		return false;
//	}

	protected TypeCheckInfo getVariableType(ConceptType variable, SadlResource sr, String conceptNm, String conceptUri, EObject reference) throws DontTypeCheckException, CircularDefinitionException, InvalidNameException, TranslationException, URISyntaxException, IOException, ConfigurationException, InvalidTypeException, CircularDependencyException, PropertyWithoutRangeException {
		if (sr == null) {
			return null;	// this might happen when cleaning with editor open
		}
		//  1) get the Name of the SadlResource for the variable
		//	2) get the definition of the Name
		//  3) if the container of the reference is a SubjHasProp:
		//		3a) look at the SubjHasProp left and right
		//			3aa) if the Name matches left, get the type as the domain of the SubjHasProp prop
		//			3ab) if the Name matches right, get the type as the range of the SubjHasProp prop
		
		VariableNode var = getModelProcessor().getVariable(conceptNm);
		if (var != null && var.getType() != null) {
			NamedNode tctype = new NamedNode(var.getType().toFullyQualifiedString(), NodeType.VariableNode);
			ConceptName et = getModelProcessor().namedNodeToConceptName(tctype);
			TypeCheckInfo tci = new TypeCheckInfo(et, var.getType(), this, reference);
			return tci;
		}
		SadlResource name = sr.getName();
		if (name != null) {
			SadlResource def = declarationExtensions.getDeclaration(name);
			EObject defContainer = def.eContainer();
			if (defContainer instanceof SubjHasProp) {
				SadlResource propsr = ((SubjHasProp)defContainer).getProp();
				if (((SubjHasProp)defContainer).getLeft().equals(name)) {
					return getPropertyDomainType(propsr, reference);
				}
				else if (((SubjHasProp)defContainer).getRight().equals(name)) {
					return getPropertyRangeType(propsr, reference);
				}
			}
			else if (defContainer instanceof SadlParameterDeclaration) {
				SadlTypeReference exprType = ((SadlParameterDeclaration)defContainer).getType();
				return getType(exprType);
			}
			else if (defContainer instanceof BinaryOperation) {
				if (((BinaryOperation)defContainer).getLeft() instanceof Name) { // && !((BinaryOperation)defContainer).getLeft().equals(reference)) {
					if (getModelProcessor().isDeclaration(((BinaryOperation)defContainer).getRight())) {
						Declaration decl = getEmbeddedDeclaration(defContainer);  // are we in a Declaration (a real declaration--the type is a class)
						if (decl != null) {
							return getType(decl);
						}
					}
					else {
						TypeCheckInfo ptci = getType(((BinaryOperation)defContainer).getRight());
						return ptci;
					}
				}
				else if (((BinaryOperation)defContainer).getLeft() instanceof PropOfSubject && ((BinaryOperation)defContainer).getRight() instanceof Name) {
					TypeCheckInfo tci = getType(((BinaryOperation)defContainer).getLeft());
					return tci;
				}
				else {
					issueAcceptor.addError("Unhandled variable container for variable '" + conceptNm + "'.", defContainer);
				}
			}
			else if (defContainer instanceof SadlParameterDeclaration) {
				SadlTypeReference exprType = ((SadlParameterDeclaration)defContainer).getType();
				return getType(exprType);
			}
			else if (defContainer instanceof BinaryOperation) {
				if (((BinaryOperation)defContainer).getLeft() instanceof Name) { // && !((BinaryOperation)defContainer).getLeft().equals(expression)) { // this causes failure in some cases awc 9/27/17
					if (getModelProcessor().isDeclaration(((BinaryOperation)defContainer).getRight())) {
						Declaration decl = getEmbeddedDeclaration(defContainer);  // are we in a Declaration (a real declaration--the type is a class)
						if (decl != null) {
							return getType(decl);
						}
					}
					else {
						TypeCheckInfo ptci = getType(((BinaryOperation)defContainer).getRight());
						return ptci;
					}
				}
			}
		}
		
		if (conceptUri == null) {
			return null;
		}
		EObject refContainer = reference.eContainer();
		if (refContainer instanceof SadlParameterDeclaration) {
			SadlTypeReference exprType = ((SadlParameterDeclaration)refContainer).getType();
			return getType(exprType);
		}
		else if (refContainer instanceof SubjHasProp) {
			SadlResource psr = ((SubjHasProp)refContainer).getProp();
			if (psr.equals(sr)) {
				ConceptName tci = new ConceptName("TODO");
				return new TypeCheckInfo(tci, null, this, refContainer);
			}
			TypeCheckInfo ptci = getType(psr);
			return ptci;
		}
		else if (refContainer instanceof PropOfSubject) {
			Expression pred = ((PropOfSubject)refContainer).getLeft();
			if (pred instanceof SadlResource && ((PropOfSubject)refContainer).getRight().equals(name)) {
				return getPropertyDomainType((SadlResource) pred, reference);
			}
		}
		else if (refContainer instanceof BinaryOperation) {
			if (((BinaryOperation)refContainer).getLeft() instanceof Name && !((BinaryOperation)refContainer).getLeft().equals(reference)) {
				if (getModelProcessor().isDeclaration(((BinaryOperation)refContainer).getRight())) {
					Declaration decl = getEmbeddedDeclaration(refContainer);  // are we in a Declaration (a real declaration--the type is a class)
					if (decl != null) {
						return getType(decl);
					}
				}
				else {
					TypeCheckInfo ptci = getType(((BinaryOperation)refContainer).getRight());
					return ptci;
				}
			}
		}
		else if (refContainer instanceof SelectExpression) {
			// find name in expression and get type from there
			TypeCheckInfo tci = getTypeFromWhereExpression(sr, conceptUri, ((SelectExpression)refContainer).getWhereExpression());
			if (tci == null) {
				throw new DontTypeCheckException();
			}
		}
		NamedNode tctype = new NamedNode(conceptUri, NodeType.VariableNode);
		ConceptName declarationConceptName = getModelProcessor().namedNodeToConceptName(tctype);
		return new TypeCheckInfo(declarationConceptName, tctype, this, reference);
	}
	
	private TypeCheckInfo getTypeFromWhereExpression(SadlResource sr, String uri, Expression expr) throws InvalidNameException, TranslationException, URISyntaxException, IOException, ConfigurationException, DontTypeCheckException, CircularDefinitionException, InvalidTypeException, CircularDependencyException, PropertyWithoutRangeException {
		if (expr instanceof SubjHasProp) {
			Expression sexpr = findDefiningExpression(uri, ((SubjHasProp)expr).getLeft());
			if (sexpr != null && !sexpr.equals(sr)  && !(sexpr instanceof Name && ((Name)sexpr).getName().equals(sr))) {
				return getType(sexpr);
			}
			sexpr = findDefiningExpression(uri, ((SubjHasProp)expr).getProp());
			if (sexpr != null && !sexpr.equals(sr)  && !(sexpr instanceof Name && ((Name)sexpr).getName().equals(sr))) {
				return getType(sexpr);
			}
			sexpr = findDefiningExpression(uri, ((SubjHasProp)expr).getRight());
			if (sexpr != null && !sexpr.equals(sr)  && !(sexpr instanceof Name && ((Name)sexpr).getName().equals(sr))) {
				return getType(sexpr);
			}
		} else if (expr instanceof Name) {
			String nuri = declarationExtensions.getConceptUri(((Name)expr).getName());
			if (nuri != null && nuri.equals(uri)) {
				if (expr != null && !expr.equals(sr)  && !(expr instanceof Name && ((Name)expr).getName().equals(sr))) {
					return getType(expr);
				}
			}
		}
		else if (expr instanceof SadlResource) {
			String nuri = declarationExtensions.getConceptUri((SadlResource)expr);
			if (expr != null && !expr.equals(sr)  && !(expr instanceof Name && ((Name)expr).getName().equals(sr))) {
				return getType(expr);
			}
		}
		return null;
	}

	private Expression findDefiningExpression(String uri, Expression expr) {
		if (expr instanceof SubjHasProp) {
			Expression sexpr = findDefiningExpression(uri, ((SubjHasProp)expr).getLeft());
			if (sexpr != null) {
				return sexpr;
			}
			sexpr = findDefiningExpression(uri, ((SubjHasProp)expr).getProp());
			if (sexpr != null) {
				return sexpr;
			}
			sexpr = findDefiningExpression(uri, ((SubjHasProp)expr).getRight());
			if (sexpr != null) {
				return ((SubjHasProp)expr).getProp();
			}
		} else if (expr instanceof Name) {
			String nuri = declarationExtensions.getConceptUri(((Name)expr).getName());
			if (nuri != null && nuri.equals(uri)) {
				return expr;
			}
		}
		else if (expr instanceof SadlResource) {
			String nuri = declarationExtensions.getConceptUri((SadlResource)expr);
			if (nuri != null && nuri.equals(uri)) {
				return expr;
			}
		}
		return null;
	}

	private TypeCheckInfo combineTypes(List<String> operations, Expression leftExpression, Expression rightExpression,
			TypeCheckInfo leftTypeCheckInfo, TypeCheckInfo rightTypeCheckInfo, ImplicitPropertySide side) throws InvalidNameException, DontTypeCheckException, InvalidTypeException, TranslationException {
		if(!compareTypes(operations, leftExpression, rightExpression, leftTypeCheckInfo, rightTypeCheckInfo, side)){
			return null;
		}
		if (getModelProcessor().isBooleanComparison(operations)) {
			NamedNode tctype = new NamedNode(XSD.xboolean.getURI(), NodeType.DataTypeProperty);
			ConceptName booleanLiteralConceptName = getModelProcessor().namedNodeToConceptName(tctype);
			return new TypeCheckInfo(booleanLiteralConceptName, tctype, this, leftExpression.eContainer());
		}
		else if (getModelProcessor().isNumericOperator(operations)) {
			NamedNode lcn = getTypeCheckInfoType(leftTypeCheckInfo);
			NamedNode rcn = getTypeCheckInfoType(rightTypeCheckInfo);
			if (lcn == null || lcn.getNamespace() == null) {
				return leftTypeCheckInfo;
			}
			if (rcn == null || rcn.getNamespace() == null) {
				return rightTypeCheckInfo;
			}
			if (rcn.equals(lcn)) {
				return leftTypeCheckInfo;
			}
			ConceptName cn = numericalPrecedenceType(getModelProcessor().namedNodeToConceptName(lcn), getModelProcessor().namedNodeToConceptName(rcn));
			return new TypeCheckInfo(cn, getModelProcessor().conceptNameToNamedNode(cn), this, leftExpression.eContainer());
		}
		else{
			return leftTypeCheckInfo;
		}
	}

	private ConceptName numericalPrecedenceType(ConceptName lcn, ConceptName rcn) throws InvalidNameException {
		if (lcn.getUri().equals(XSD.decimal.getURI())) {
			return lcn;
		} else if (rcn.getUri().equals(XSD.decimal.getURI())) {
			return rcn;
		}
		else if (lcn.getUri().equals(XSD.xdouble.getURI())) {
			return lcn;
		}
		else if (rcn.getUri().equals(XSD.xdouble.getURI())) {
			return rcn;
		}
		else if (lcn.getUri().equals(XSD.xfloat.getURI())) {
			return lcn;
		}
		else if (rcn.getUri().equals(XSD.xfloat.getURI())) {
			return rcn;
		}
		else if (lcn.getUri().equals(XSD.xlong.getURI())) {
			return lcn;
		}
		else if (rcn.getUri().equals(XSD.xlong.getURI())) {
			return rcn;
		}
		else if (lcn.getUri().equals(XSD.integer.getURI())) {
			return lcn;
		}
		else if (rcn.getUri().equals(XSD.integer.getURI())) {
			return rcn;
		}
		else if (lcn.getUri().equals(XSD.xint.getURI())) {
			return lcn;
		}
		else if (rcn.getUri().equals(XSD.xint.getURI())) {
			return rcn;
		}
		return rcn;
	}

	private NamedNode getTypeCheckInfoType(TypeCheckInfo tci) throws InvalidNameException, TranslationException, InvalidTypeException {
		if (tci.getExplicitValueType() != null && tci.getExplicitValueType().equals(ExplicitValueType.VALUE)) {
			if (tci.getExpressionType() instanceof ConceptName) {
				return getModelProcessor().conceptNameToNamedNode((ConceptName) tci.getExpressionType());
			}
		}
		if (tci.getTypeCheckType() instanceof NamedNode) {
			return (NamedNode) tci.getTypeCheckType();
		}
		if (tci.compoundTypes != null) {
			return null;
		}
		throw new InvalidNameException("Failed to get TypeCheckInfoType");
	}

	/**
	 * Compare two TypeCheckInfo structures
	 * @param operations
	 * @param leftExpression
	 * @param rightExpression
	 * @param leftTypeCheckInfo
	 * @param rightTypeCheckInfo
	 * @return return true if they pass type check comparison else false
	 * @throws InvalidNameException
	 * @throws DontTypeCheckException 
	 * @throws InvalidTypeException 
	 * @throws TranslationException 
	 */
	protected boolean compareTypes(List<String> operations, EObject leftExpression, EObject rightExpression,
			TypeCheckInfo leftTypeCheckInfo, TypeCheckInfo rightTypeCheckInfo, ImplicitPropertySide side) throws InvalidNameException, DontTypeCheckException, InvalidTypeException, TranslationException {
		boolean listTemporarilyDisabled = false;
		try {
			if (rightExpression instanceof Constant && ((Constant)rightExpression).getConstant().equals("known")) {
				//	everything matches "known"
				return true;
			}
			if (leftExpression instanceof Constant && ((Constant)leftExpression).getConstant().equals("known")) {
				//	everything matches "known"
				return true;
			}
			if (leftExpression instanceof Constant && 
					(((Constant)leftExpression).getConstant().equals("value") || ((Constant)leftExpression).getConstant().equals("type"))) {
				listTemporarilyDisabled = true;
				leftTypeCheckInfo.setRangeValueType(RangeValueType.CLASS_OR_DT);
				Node leftTct = leftTypeCheckInfo.getTypeCheckType();
				if (leftTct instanceof NamedNode) {
					if (((NamedNode)leftTct).getNodeType().equals(NodeType.ClassListNode)) {
						((NamedNode)leftTct).setNodeType(NodeType.ClassNode);
					}
					else if (((NamedNode)leftTct).getNodeType().equals(NodeType.DataTypeListNode)) {
						((NamedNode)leftTct).setNodeType(NodeType.DataTypeNode);
					}
				}
			}
			
			//Compare literal types
			if(compareTypesRecursively(operations, leftExpression, rightExpression, leftTypeCheckInfo, rightTypeCheckInfo)){
				return true;
			}
			
			//Compare implied property types
			if(compareTypesUsingImpliedPropertiesRecursively(operations, leftExpression, rightExpression, leftTypeCheckInfo, rightTypeCheckInfo, side)){
				return true;
			}
				
			return false;
		}
		finally {
			if (listTemporarilyDisabled) {
				leftTypeCheckInfo.setRangeValueType(RangeValueType.LIST);
			}
		}
	}
	
	private boolean compareTypesUsingImpliedPropertiesRecursively(List<String> operations, EObject leftExpression,
			EObject rightExpression, TypeCheckInfo leftTypeCheckInfo, TypeCheckInfo rightTypeCheckInfo, ImplicitPropertySide side) throws InvalidNameException, DontTypeCheckException, InvalidTypeException, TranslationException {
		List<TypeCheckInfo> ltciCompound = leftTypeCheckInfo != null ? leftTypeCheckInfo.getCompoundTypes() : null;
		if(ltciCompound != null){
			for (int i = 0; i < ltciCompound.size(); i++) {
				boolean thisResult = compareTypesUsingImpliedProperties(operations, leftExpression, rightExpression, ltciCompound.get(i), rightTypeCheckInfo, side);
				if (thisResult) {
					return true;
				}
			}
			if (rightTypeCheckInfo.getCompoundTypes() == null) {
				return false;
			}
		}
		
		List<TypeCheckInfo> rtciCompound = rightTypeCheckInfo != null ? rightTypeCheckInfo.getCompoundTypes() : null;
		if(rtciCompound != null){
			for (int j = 0; j < rtciCompound.size(); j++) {
				boolean thisResult = compareTypesUsingImpliedProperties(operations, leftExpression, rightExpression, leftTypeCheckInfo, rtciCompound.get(j), side);
				if (thisResult) {
					return true;
				}
			}
			if (leftTypeCheckInfo.getCompoundTypes() == null) {
				return false;
			}
		}
		
		if(compareTypesUsingImpliedProperties(operations, leftExpression, rightExpression, leftTypeCheckInfo, rightTypeCheckInfo, side)){
			return true;
		}
		
		return false;
	}

	public boolean compareTypesRecursively(List<String> operations, EObject leftExpression, EObject rightExpression,
			TypeCheckInfo leftTypeCheckInfo, TypeCheckInfo rightTypeCheckInfo) throws InvalidNameException, DontTypeCheckException, InvalidTypeException, TranslationException {

		List<TypeCheckInfo> ltciCompound = leftTypeCheckInfo != null ? leftTypeCheckInfo.getCompoundTypes() : null;
		if(ltciCompound != null){
			for (int i = 0; i < ltciCompound.size(); i++) {
				boolean thisResult = compareTypesRecursively(operations, leftExpression, rightExpression, ltciCompound.get(i), rightTypeCheckInfo);
				if (thisResult) {
					return true;
				}
			}
			if (rightTypeCheckInfo.getCompoundTypes() == null) {
				return false;
			}
		}
		
		List<TypeCheckInfo> rtciCompound = rightTypeCheckInfo != null ? rightTypeCheckInfo.getCompoundTypes() : null;
		if(rtciCompound != null){
			for (int j = 0; j < rtciCompound.size(); j++) {
				boolean thisResult = compareTypesRecursively(operations, leftExpression, rightExpression, leftTypeCheckInfo, rtciCompound.get(j));
				if (thisResult) {
					return true;
				}
			}
			if (leftTypeCheckInfo.getCompoundTypes() == null) {
				return false;
			}
		}
		
		ConceptIdentifier leftConceptIdentifier = leftTypeCheckInfo != null ? getConceptIdentifierFromTypeCheckInfo(leftTypeCheckInfo): null;
		ConceptIdentifier rightConceptIdentifier = rightTypeCheckInfo != null ? getConceptIdentifierFromTypeCheckInfo(rightTypeCheckInfo) : null; 
		if ((leftConceptIdentifier != null && leftConceptIdentifier.toString().equals("None")) || 
				(rightConceptIdentifier != null && rightConceptIdentifier.toString().equals("None")) ||
				(leftConceptIdentifier != null && leftConceptIdentifier.toString().equals("TODO")) || 
				(rightConceptIdentifier != null && rightConceptIdentifier.toString().equals("TODO"))) {
			// Can't type-check on "None" as it represents that it doesn't exist.
			return true;
		}
		else if (leftConceptIdentifier == null) {
			if (leftTypeCheckInfo != null && leftTypeCheckInfo.getSeverity() != null) {
				getModelProcessor().addIssueToAcceptor(SadlErrorMessages.TYPE_COMPARISON.toString(), leftTypeCheckInfo.getSeverity(), leftExpression);
			}
			else {
				getModelProcessor().addIssueToAcceptor(SadlErrorMessages.TYPE_COMPARISON.toString(), leftExpression);
			}
			if (metricsProcessor != null) {
				metricsProcessor.addMarker(null, MetricsProcessor.ERROR_MARKER_URI, MetricsProcessor.TYPE_CHECK_FAILURE_URI);
			}
			return false;
		}
		else if(rightConceptIdentifier == null){
			if (rightTypeCheckInfo != null && rightTypeCheckInfo != null && rightTypeCheckInfo.getSeverity() != null) {
				getModelProcessor().addIssueToAcceptor(SadlErrorMessages.TYPE_COMPARISON.toString(), rightTypeCheckInfo.getSeverity(), rightExpression);
			}
			else {
				getModelProcessor().addIssueToAcceptor(SadlErrorMessages.TYPE_COMPARISON.toString(), rightExpression);
			}
			if (metricsProcessor != null) {
				metricsProcessor.addMarker(null, MetricsProcessor.ERROR_MARKER_URI, MetricsProcessor.TYPE_CHECK_FAILURE_URI);
			}
			return false;
		}
		else if (!compatibleTypes(operations, leftExpression, rightExpression, leftTypeCheckInfo, rightTypeCheckInfo)) {
			if (isConjunctiveLocalRestriction(leftExpression, rightExpression)) {
				return true;
			}
			return false;
		}
		
		return true;
	}
	
	private boolean isConjunctiveLocalRestriction(EObject leftExpression, EObject rightExpression) {
		if (rightExpression instanceof Declaration && rightExpression.eContainer() instanceof BinaryOperation && 
		((BinaryOperation)rightExpression.eContainer()).getOp().equals("is") && rightExpression.eContainer().eContainer() instanceof BinaryOperation &&
		((BinaryOperation)rightExpression.eContainer().eContainer()).getOp().equals("or") && contains(rightExpression.eContainer().eContainer(), leftExpression)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if eobj1 contains eobj2 else false
	 * 
	 * @param eobj1
	 * @param eobj2
	 * @return
	 */
	protected boolean contains(EObject eobj1, EObject eobj2) {
		if (eobj2.eContainer() != null) {
			if (eobj2.eContainer().equals(eobj1)) {
				return true;
			}
			else {
				return contains(eobj1, eobj2.eContainer());
			}
		}
		return false;
	}
	
	protected ConceptIdentifier getConceptIdentifierFromTypeCheckInfo(TypeCheckInfo tci) throws TranslationException, InvalidNameException, InvalidTypeException {
		
		if (tci.getRangeValueType().equals(RangeValueType.LIST)) {
			ConceptName cn = getListType(tci);
			if (cn != null) {
				return cn;
			}
		}
		if (tci.getExplicitValue() != null) {
			RDFNode val = tci.getExplicitValue();
			if (val.isURIResource()) {
				ConceptName cn = new ConceptName(val.asResource().getURI());
				cn.setType(ConceptType.INDIVIDUAL);
				return cn;
			}
			else if (val.isLiteral()) {
				ConceptName literalConceptName = new ConceptName(val.asLiteral().getDatatype().getURI());
				literalConceptName.setType(ConceptType.RDFDATATYPE);
				return literalConceptName;
			}
		}
		return getModelProcessor().namedNodeToConceptName((NamedNode) tci.getTypeCheckType());
	}

	private boolean compareTypesUsingImpliedProperties(List<String> operations, EObject leftExpression,
			EObject rightExpression, TypeCheckInfo leftTypeCheckInfo, TypeCheckInfo rightTypeCheckInfo, ImplicitPropertySide side) throws InvalidNameException, DontTypeCheckException, InvalidTypeException, TranslationException {
		
		String opstr = (operations != null && operations.size() > 0) ? operations.get(0) : null;
		if (leftTypeCheckInfo != null && !side.equals(ImplicitPropertySide.RIGHT) && leftTypeCheckInfo.getImplicitProperties() != null) {
			Iterator<ConceptName> litr = leftTypeCheckInfo.getImplicitProperties().iterator();
			while (litr.hasNext()) {
				ConceptName cn = litr.next();
				Property prop = theJenaModel.getProperty(cn.getUri());
				if (prop.canAs(ObjectProperty.class)) {
					cn.setType(ConceptType.OBJECTPROPERTY);
				}
				else if (prop.canAs(DatatypeProperty.class)) {
					cn.setType(ConceptType.DATATYPEPROPERTY);
				}
				else {
					cn.setType(ConceptType.RDFPROPERTY);
				}
				TypeCheckInfo newltci = getTypeInfoFromRange(cn, prop, leftExpression);
				if (compareTypes(operations, leftExpression, rightExpression, newltci, rightTypeCheckInfo, ImplicitPropertySide.LEFT)) {
					issueAcceptor.addInfo("Implied property '" + getModelProcessor().conceptIdentifierToString(cn) + "' used (left side" + (opstr != null ? (" of '" + opstr + "'") : "") + ") to pass type check", leftExpression);
					addImpliedPropertiesUsed(leftExpression, prop);
					return true;
				}
			}
		}
		else if (rightTypeCheckInfo != null && !side.equals(ImplicitPropertySide.LEFT) && rightTypeCheckInfo.getImplicitProperties() != null) {
			Iterator<ConceptName> ritr = rightTypeCheckInfo.getImplicitProperties().iterator();
			while (ritr.hasNext()) {
				ConceptName cn = ritr.next();
				Property prop = theJenaModel.getProperty(cn.getUri());
				if (prop.canAs(ObjectProperty.class)) {
					cn.setType(ConceptType.OBJECTPROPERTY);
				}
				else if (prop.canAs(DatatypeProperty.class)) {
					cn.setType(ConceptType.DATATYPEPROPERTY);
				}
				else {
					cn.setType(ConceptType.RDFPROPERTY);
				}
				TypeCheckInfo newrtci = getTypeInfoFromRange(cn, prop, rightExpression);
				if (compareTypes(operations, leftExpression, rightExpression, leftTypeCheckInfo, newrtci, ImplicitPropertySide.RIGHT)) {
					issueAcceptor.addInfo("Implied property '" + getModelProcessor().conceptIdentifierToString(cn) + "' used (right side" + (opstr != null ? (" of '" + opstr + "'") : "") + ") to pass type check", rightExpression);
					addImpliedPropertiesUsed(rightExpression, prop);
					return true;
				}
			}
		}
		return false;
	}

	private boolean compatibleTypes(List<String> operations, EObject leftExpression, EObject rightExpression,
									TypeCheckInfo leftTypeCheckInfo, TypeCheckInfo rightTypeCheckInfo) throws InvalidNameException, InvalidTypeException, DontTypeCheckException, TranslationException{
		
		if ((leftTypeCheckInfo.getRangeValueType() == null && rightTypeCheckInfo.getRangeValueType() != null && !rightTypeCheckInfo.getRangeValueType().equals(RangeValueType.CLASS_OR_DT)) || 
			(leftTypeCheckInfo.getRangeValueType() != null && !leftTypeCheckInfo.getRangeValueType().equals(RangeValueType.CLASS_OR_DT) && rightTypeCheckInfo.getRangeValueType() == null) ||
			(leftTypeCheckInfo.getRangeValueType() != null && rightTypeCheckInfo.getRangeValueType() != null && !(leftTypeCheckInfo.getRangeValueType().equals(rightTypeCheckInfo.getRangeValueType())))) {
			if (!isQualifyingListOperation(operations, leftTypeCheckInfo, rightTypeCheckInfo)) {
				if (isCompatibleListTypes(operations, leftTypeCheckInfo, rightTypeCheckInfo)) {
					return true;
				}
				return false;
			}
		}
		
		if (getModelProcessor().isDeclaration(leftExpression) && getModelProcessor().isDeclaration(rightExpression) && 
				leftTypeCheckInfo.getTypeToExprRelationship().equals("self") && 
				rightTypeCheckInfo.getTypeToExprRelationship().equals("self")) {
			// this is a test for class membership to be resolved by the reasoner
			return true;
		}
		
		ConceptIdentifier leftConceptIdentifier = getConceptIdentifierFromTypeCheckInfo(leftTypeCheckInfo);
		ConceptIdentifier rightConceptIdentifier = getConceptIdentifierFromTypeCheckInfo(rightTypeCheckInfo);
		if (leftConceptIdentifier == null || rightConceptIdentifier == null) {
			return false;
		}
		List<ConceptIdentifier> leftCIs = null;
		if (leftConceptIdentifier instanceof SadlUnionClass) {
			leftCIs = ((SadlUnionClass)leftConceptIdentifier).getClasses();
		}
		else {
			leftCIs = new ArrayList<ConceptIdentifier>(1);
			leftCIs.add(leftConceptIdentifier);
		}
		List<ConceptIdentifier> rightCIs = null;
		if (rightConceptIdentifier instanceof SadlUnionClass) {
			rightCIs = ((SadlUnionClass)rightConceptIdentifier).getClasses();
		}
		else {
			rightCIs = new ArrayList<ConceptIdentifier>(1);
			rightCIs.add(rightConceptIdentifier);
		}
		for (int leftIdx = 0; leftIdx < leftCIs.size(); leftIdx++) {
			for (int rightIdx = 0; rightIdx < rightCIs.size(); rightIdx++) {
				leftConceptIdentifier = leftCIs.get(leftIdx);
				rightConceptIdentifier = rightCIs.get(rightIdx);
				if (leftConceptIdentifier instanceof ConceptName && rightConceptIdentifier instanceof ConceptName) {
					ConceptName leftConceptName = (ConceptName) leftConceptIdentifier;
					ConceptName rightConceptName = (ConceptName) rightConceptIdentifier;
					
					if (getModelProcessor().isNumericOperator(operations) && ((!isNumeric(leftTypeCheckInfo) && !isNumericWithImpliedProperty(leftTypeCheckInfo,(Expression)leftExpression))||
																			  (!isNumeric(rightTypeCheckInfo) && !isNumericWithImpliedProperty(rightTypeCheckInfo,(Expression)rightExpression)))) {
						if (!leftConceptName.getUri().equals(SadlConstants.SADL_IMPLICIT_MODEL_UNITTEDQUANTITY_URI) || 
								!rightConceptName.getUri().equals(SadlConstants.SADL_IMPLICIT_MODEL_UNITTEDQUANTITY_URI)) {
							return false;
						}
					}
					if (leftConceptName.equals(rightConceptName)) {
						return true;
					}
					else if ((getModelProcessor().isNumericOperator(operations) || getModelProcessor().canBeNumericOperator(operations)) && 
						(getModelProcessor().isNumericType(leftConceptName) && getModelProcessor().isNumericType(rightConceptName))) {
						return true;
					}
					else if (leftConceptName.getType() == null || rightConceptName.getType() == null) {
						if (rightConceptName.getType() == null && leftConceptName.getType() == null) {
							return true;
						}
//						else {
//							return false;
//						}
					}
					else if (leftConceptName.getType().equals(ConceptType.RDFDATATYPE) &&
							  rightConceptName.getType().equals(ConceptType.RDFDATATYPE)) {
						if(leftConceptName.getUri().equals(rightConceptName.getUri())){
							return true;
						}
						else if (isInteger(leftConceptName) && isInteger(rightConceptName)) {
							return true;
						}
						else if(isDecimal(leftConceptName) && isInteger(rightConceptName)){
							return true;
						}
						else if(isInteger(leftConceptName) && isDecimal(rightConceptName)){
							// TODO does this need to be restricted to certain operators? This should work for numerical comparison...
							return true;
						}
						else if(isDecimal(leftConceptName) && isDecimal(rightConceptName)){
							return true;
						}
						else if (rightConceptName.getUri().equals(XSD.xstring.getURI()) && 
								(leftConceptName.getUri().equals(XSD.dateTime.getURI()) || leftConceptName.getUri().equals(XSD.date.getURI()))) {
							return true;
						}
						else {
							// maybe one or both is a user-defined datatype
							
						}
					}
					else if (leftConceptName.getType().equals(ConceptType.DATATYPEPROPERTY) &&
							  rightConceptName.getType().equals(ConceptType.DATATYPEPROPERTY)) {
						if(leftConceptName.getUri().equals(rightConceptName.getUri())){
							return true;
						}
					}
					else if(leftConceptName.getType().equals(ConceptType.OBJECTPROPERTY) &&
							 rightConceptName.getType().equals(ConceptType.OBJECTPROPERTY)){
						if(leftConceptName.getUri().equals(rightConceptName.getUri())){
							return true;
						}
					}
					else if (leftConceptName.getType().equals(ConceptType.ONTCLASS) &&
							rightConceptName.getType().equals(ConceptType.ONTCLASS)) {
						if (partOfTest(leftExpression, rightExpression)) {
							// if we're in a test we don't want to type check as it may fail when not using the inferred model.
							return true;
						}
						//How do we determine if either is a sub/super class of the other?
						if(leftConceptName.getUri().equals(rightConceptName.getUri())){
							return true;
						}
						// these next two ifs are a little loose, but not clear how to determine which way the comparison should be? May need tightening... AWC 5/11/2016
						try {
//							if (rightTypeCheckInfo.getExpressionType() instanceof ConceptName && ((ConceptName)rightTypeCheckInfo.getExpressionType()).getType().equals(ConceptType.INDIVIDUAL)) {
//								// here we can do a tighter check
//								if (!getModelProcessor().instanceBelongsToClass(theJenaModel, 
//										theJenaModel.getOntResource(((ConceptName)rightTypeCheckInfo.getExpressionType()).getUri()),
//										theJenaModel.getOntResource(leftConceptName.getUri()))) {
//									return false;
//								}
//							}
							OntClass subcls = theJenaModel.getOntClass(leftConceptName.getUri());
							OntResource supercls = theJenaModel.getOntResource(rightConceptName.getUri());
							if (SadlUtils.classIsSubclassOf(subcls, supercls, true, null)) {
								return true;
							}
							if (SadlUtils.classIsSubclassOf(theJenaModel.getOntClass(rightConceptName.getUri()), theJenaModel.getOntResource(leftConceptName.getUri()), true, null)) {
								return true;
							}
		// TODO handle equivalent classes.					
		//					StmtIterator sitr = theJenaModel.listStatements(theJenaModel.getOntClass(rightConceptName.getUri()), OWL.equivalentClass, (RDFNode)null);
		//					if (sitr.hasNext()) {
		//						System.out.println(sitr.nextStatement().toString());
		//					}
		//					else {
		//						theJenaModel.write(System.out, "N-TRIPLE");
		//					}
						} catch (CircularDependencyException e) {
							e.printStackTrace();
//						} catch (JenaProcessorException e) {
//							e.printStackTrace();
						}
					}
					//Case: a class is being type-checked against a decomposition which is a sub/super-class of the former or
					// a decomposition variable is being type-checked against a class
					else if ((leftConceptName.getType().equals(ConceptType.ONTCLASS) &&
							rightConceptName.getType().equals(ConceptType.VARIABLE)) || 
							(leftConceptName.getType().equals(ConceptType.VARIABLE) &&
							rightConceptName.getType().equals(ConceptType.ONTCLASS))) {
						try {
							if (SadlUtils.classIsSubclassOf(theJenaModel.getOntClass(leftConceptName.getUri()), theJenaModel.getOntResource(rightConceptName.getUri()), true, null)) {
								return true;
							}
							if (SadlUtils.classIsSubclassOf(theJenaModel.getOntClass(rightConceptName.getUri()), theJenaModel.getOntResource(leftConceptName.getUri()), true, null)) {
								return true;
							}
						} catch (CircularDependencyException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else if ((leftConceptName.getType().equals(ConceptType.INDIVIDUAL) && rightConceptName.getType().equals(ConceptType.ONTCLASS))) {
						if (instanceBelongsToClass(theJenaModel.getIndividual(leftConceptName.getUri()), theJenaModel.getOntClass(rightConceptName.getUri()))) {
							return true;
						}
					}
					else if ((leftConceptName.getType().equals(ConceptType.ONTCLASS) && rightConceptName.getType().equals(ConceptType.INDIVIDUAL))){
						if (instanceBelongsToClass(theJenaModel.getIndividual(rightConceptName.getUri()), theJenaModel.getOntClass(leftConceptName.getUri()))) {
							return true;
						}
					}
					else if ((leftConceptName.getType().equals(ConceptType.INDIVIDUAL) && rightConceptName.getType().equals(ConceptType.INDIVIDUAL))){
						// TODO Is this the right way to compare for two individuals? 
						if (instancesHaveCommonType(theJenaModel.getIndividual(leftConceptName.getUri()), theJenaModel.getIndividual(rightConceptName.getUri()))) {
							return true;
						}
					}
					else if (leftConceptName.getType().equals(ConceptType.VARIABLE) && getModelProcessor().isDeclaration(rightExpression)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean isCompatibleListTypes(List<String> operations, TypeCheckInfo leftTypeCheckInfo,
			TypeCheckInfo rightTypeCheckInfo) {
		// TODO Auto-generated method stub
		return false;
	}

	private ConceptName getListType(TypeCheckInfo tci) throws TranslationException, InvalidTypeException {
		Node tct = tci.getTypeCheckType();
		if (tct != null) {
			if (tct instanceof NamedNode) {
				try {
					OntResource cls = theJenaModel.getOntResource(((NamedNode)tct).toFullyQualifiedString());
					if (tci.getTypeToExprRelationship().equals("range") || tci.getTypeToExprRelationship().equals("restriction to")) {
						if (cls.isURIResource()) {
//							return new ConceptName(cls.getURI());
							return getModelProcessor().namedNodeToConceptName((NamedNode) tct);
						}
					}
					if (cls != null && cls.canAs(OntClass.class)){
						ConceptName listcn = getListClassType(cls);
						if (listcn != null) {
							return listcn;
						}
					}
				} catch (InvalidNameException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	protected ConceptName getListClassType(OntResource cls) {
		ExtendedIterator<OntClass> eitr = cls.as(OntClass.class).listSuperClasses();
		while (eitr.hasNext()) {
			OntClass supercls = eitr.next();
			if (supercls.isRestriction() && supercls.hasProperty(OWL.onProperty, theJenaModel.getProperty(SadlConstants.SADL_LIST_MODEL_FIRST_URI))) {
				RDFNode avf = supercls.getPropertyValue(OWL.allValuesFrom);
				if (avf.canAs(Resource.class)) {
					Resource r = avf.as(Resource.class);
					if (r.isURIResource()) {
						eitr.close();
						return new ConceptName(r.getURI());
					}
				}
			}
		}
		return null;
	}

	private boolean partOfTest(EObject leftExpression, EObject rightExpression) {
		if (checkForContainer(leftExpression, TestStatementImpl.class)) {
			return true;
		}
		return checkForContainer(rightExpression, TestStatement.class);
	}
	
	private <X extends EObject> boolean checkForContainer(EObject expr, Class<X> t ) {
		if (expr.eContainer() == null) {
			return false;
		}
		if (expr.eContainer().getClass().equals(t)) {
			return true;
		}
		return checkForContainer(expr.eContainer(), t);
	}

	protected boolean isQualifyingListOperation(List<String> operations, TypeCheckInfo leftTypeCheckInfo, TypeCheckInfo rightTypeCheckInfo) {
		if ((operations.contains("contain") || operations.contains("contains")) && leftTypeCheckInfo != null && 
				leftTypeCheckInfo.getRangeValueType().equals(RangeValueType.LIST)) {
			return true;
		}
		if (operations.contains("unique") && operations.contains("in") && rightTypeCheckInfo != null &&
				rightTypeCheckInfo.getRangeValueType().equals(RangeValueType.LIST)) {
			return true;
		}
		return false;
	}
	
	private boolean instancesHaveCommonType(Individual individualL, Individual individualR) {
		ExtendedIterator<Resource> lcitr = individualL.listRDFTypes(true);
		ExtendedIterator<Resource> rcitr = individualR.listRDFTypes(true);
		while (lcitr.hasNext()) {
			Resource lr = lcitr.next();
			while (rcitr.hasNext()) {
				Resource rr = rcitr.next();
				if (lr.equals(rr)) {
					lcitr.close();
					rcitr.close();
					return true;
				}
			}
		}
		return false;
	}

	private boolean instanceBelongsToClass(Individual individual, OntClass ontClass) {
		ExtendedIterator<Resource> citr = individual.listRDFTypes(false);
		while (citr.hasNext()) {
			Resource cls = citr.next();
			if (cls.isURIResource() && cls.getURI().equals(ontClass.getURI())) {
				return true;
			}
			else {
				// this may be a union or intersection class; how should this be handled?
				// TODO
			}
		}
		return false;
	}
	
	private boolean isInteger(ConceptIdentifier type) throws InvalidNameException {
		if (type instanceof ConceptName) {
			String uri = ((ConceptName)type).getUri();
			if (uri.equals(XSD.integer.getURI())) {
				return true;
			}
			else if (uri.equals(XSD.xint.getURI())) {
				return true;
			}
			else if (uri.equals(XSD.xlong.getURI())) {
				return true;
			}
		}
		return false;
	}

	private boolean isDecimal(ConceptIdentifier type) throws InvalidNameException {
		if (type instanceof ConceptName) {
			String uri = ((ConceptName)type).getUri();
			if (uri.equals(XSD.xfloat.getURI()) || uri.equals(XSD.xdouble.getURI()) || uri.equals(XSD.decimal.getURI())) {
				return true;
			}
		}
		return false;
	}

	private EObject getDefaultContext() {
		return defaultContext;
	}

	private void setDefaultContext(EObject defaultContext) {
		this.defaultContext = defaultContext;
	}

	public Map<EObject, Property> getImpliedPropertiesUsed() {
		return impliedPropertiesUsed;
	}

	protected boolean addImpliedPropertiesUsed(EObject context, Property impliedPropertyUsed) {
		if (impliedPropertiesUsed == null) {
			impliedPropertiesUsed = new HashMap<EObject, Property>();
			impliedPropertiesUsed.put(context, impliedPropertyUsed);
			return true;
		}
		else {
			if (!impliedPropertiesUsed.containsKey(context)) {
				impliedPropertiesUsed.put(context, impliedPropertyUsed);
				return true;
			}
		}
		return false;
	}

	public void checkPropertyDomain(OntModel ontModel, Expression subject, SadlResource predicate, Expression target, boolean propOfSubjectCheck) throws InvalidTypeException {
		OntConceptType ptype = null;
		try {
			ptype = declarationExtensions.getOntConceptType(predicate);
		} catch (CircularDefinitionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			getModelProcessor().addIssueToAcceptor("Unable to get type. This should not happen. Please report.", predicate);
			return;
		}
		boolean checkDomain = true;
		if (ptype.equals(OntConceptType.VARIABLE) && declarationExtensions.getDeclaration(predicate).equals(predicate)) {
			getModelProcessor().addIssueToAcceptor(SadlErrorMessages.VARIABLE_INSTEAD_OF_PROP2.get(declarationExtensions.getConcreteName(predicate)), predicate);
			checkDomain = false;
		}
		if (subject instanceof SadlResource) {
			org.eclipse.emf.ecore.resource.Resource rsrc = subject.eResource();
			if (rsrc != null) {
				if (ontModel != null) {
					OntConceptType stype;
					try {
						stype = declarationExtensions.getOntConceptType((SadlResource)subject);
						OntResource subj = null;
						String varName = null;
						if (stype.equals(OntConceptType.VARIABLE)) {
							TypeCheckInfo stci;
							// for now don't do any checking--may be able to do so later with variable definitions
							try {
								varName = declarationExtensions.getConcreteName((SadlResource)subject);
								stci = getType(subject);
								//It's possible that there are local restrictions
								if (stci != null) {
									TypeCheckInfo lr = getApplicableLocalRestriction(generateLocalRestrictionKey(varName));
									if (lr != null && lr.getTypeCheckType() != null) {
										stci = lr;
									}
								}
								if (stci != null && stci.getTypeCheckType() != null) {
									subj = ontModel.getOntResource(stci.getTypeCheckType().toFullyQualifiedString());
									if (subj != null) {
										Property prop = ontModel.getProperty(declarationExtensions.getConceptUri(predicate));
										if (prop != null && checkDomain) {
											checkPropertyDomain(ontModel, subj, prop, target, propOfSubjectCheck, varName);
										}
									}
								}
							} catch (InvalidNameException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (TranslationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (URISyntaxException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (ConfigurationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (DontTypeCheckException e) {
								
							} catch (CircularDependencyException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (PropertyWithoutRangeException e) {

							}
						}
						else if (stype.equals(OntConceptType.CLASS_PROPERTY)) {
							OntProperty propsubj = ontModel.getOntProperty(declarationExtensions.getConceptUri((SadlResource)subject));
							if (propsubj != null) {
								Property prop = ontModel.getProperty(declarationExtensions.getConceptUri(predicate));
								if (prop != null && checkDomain) {
									checkPropertyDomain(ontModel, propsubj, prop, target, propOfSubjectCheck, varName);
								}
							}
						}
						else {
							subj = ontModel.getOntResource(declarationExtensions.getConceptUri((SadlResource)subject));
							if (subj != null) {
								String preduri = declarationExtensions.getConceptUri(predicate);
								if (preduri == null) {
									getModelProcessor().addIssueToAcceptor("Unable to resolve name", predicate);
								}
								else {
									Property prop = ontModel.getProperty(preduri);
									if (prop != null && checkDomain) {
										checkPropertyDomain(ontModel, subj, prop, target, propOfSubjectCheck, varName);
									}
								}
							}
						}
					} catch (CircularDefinitionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	protected TypeCheckInfo getApplicableLocalRestriction(String lrstr) throws InvalidTypeException, IOException, InvalidNameException, TranslationException, ConfigurationException {
		return null;	
	}

	public void checkPropertyDomain(OntModel ontModel, OntResource subj, Property prop, Expression target, boolean propOfSubjectCheck, String varName) throws InvalidTypeException {
		if(prop == null || prop.canAs(AnnotationProperty.class)){
			return;
		}
		StmtIterator stmtitr = ontModel.listStatements(prop, RDFS.domain, (RDFNode)null);
		boolean matchFound = false;
		while (stmtitr.hasNext()) {
			RDFNode obj = stmtitr.nextStatement().getObject();
			if (obj.isResource()) {
				matchFound = checkForPropertyDomainMatch(subj, prop, obj.asResource());
			}
			if (matchFound) {
				stmtitr.close();
				return;
			}
		}
		//Check for super properties
		if (prop.canAs(OntProperty.class)) {
			ExtendedIterator<? extends OntProperty> spropitr = prop.as(OntProperty.class).listSuperProperties();
			while (spropitr.hasNext()) {
				OntProperty sprop = spropitr.next();
				stmtitr = ontModel.listStatements(sprop, RDFS.domain, (RDFNode)null);
				while (stmtitr.hasNext()) {
					RDFNode obj = stmtitr.nextStatement().getObject();
					if (obj.isResource()) {
						matchFound = checkForPropertyDomainMatch(subj, prop, obj.asResource());
					}
					if (matchFound) {
						stmtitr.close();
						break;
					}
				}
			}
		}
		if (subj != null && !matchFound) {
			if (varName != null) {
				if(propOfSubjectCheck){
					getModelProcessor().addIssueToAcceptor(SadlErrorMessages.VARIABLE_NOT_IN_DOMAIN_OF_PROPERTY.get(varName, subj.getURI(),prop.getURI()), target);
				}else{
					issueAcceptor.addWarning(SadlErrorMessages.VARIABLE_NOT_IN_DOMAIN_OF_PROPERTY.get(varName, getModelProcessor().rdfNodeToString(subj),getModelProcessor().rdfNodeToString(prop)), target);
				}
			}
			else {
				String msg;
				if (subj instanceof OntProperty) {
					msg = SadlErrorMessages.RANGE_OF_NOT_IN_DOMAIN_OF.get(getModelProcessor().rdfNodeToString(subj),getModelProcessor().rdfNodeToString(prop));
				}
				else {
					msg = SadlErrorMessages.SUBJECT_NOT_IN_DOMAIN_OF_PROPERTY.get(getModelProcessor().rdfNodeToString(subj),getModelProcessor().rdfNodeToString(prop));
				}
				if(propOfSubjectCheck){
					getModelProcessor().addIssueToAcceptor(msg, target);
				}else{
					issueAcceptor.addWarning(msg, target);
				}
			}
		}
	}
	
	private boolean checkForPropertyDomainMatch(Resource subj, Property prop, Resource obj) throws InvalidTypeException {
		if (obj.isResource()) {
			if (obj.canAs(UnionClass.class)){
				List<OntResource> uclsMembers = getModelProcessor().getOntResourcesInUnionClass(theJenaModel, obj.as(UnionClass.class));
				if (uclsMembers.contains(subj)) {
					return true;
				}
				if (subj.canAs(OntClass.class)){ 
					for (int i = 0; i < uclsMembers.size(); i++) {
						OntResource member = uclsMembers.get(i);
						try {
							if (SadlUtils.classIsSubclassOf(subj.as(OntClass.class), member, true, null)) {
								return true;
							}
						} catch (CircularDependencyException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				else if (subj.canAs(Individual.class)){
					ExtendedIterator<Resource> titr = subj.as(Individual.class).listRDFTypes(false);
					while (titr.hasNext()) {
						Resource type = titr.next();
						if (uclsMembers.contains(type)) {
							titr.close();
							return true;
						}
					}
				}
			}
			else if (subj != null && obj.isURIResource() && obj.asResource().getURI().equals(subj.getURI())) {
				return true;	
			}
			else {
				if (subj.canAs(OntClass.class)){
					try {
						if ( SadlUtils.classIsSubclassOf(subj.as(OntClass.class), obj.as(OntResource.class),true, null)) {
							return true;
						}
//						if (obj.canAs(OntClass.class) &&  SadlUtils.classIsSuperClassOf(obj.as(OntClass.class), subj.as(OntClass.class))) {
//							return true;
//						}
					} catch (CircularDependencyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if (subj instanceof Property || subj.canAs(OntProperty.class)) {
					// this is a property chain missing an intermediate class or instance: get the range of the property
					StmtIterator stmtitr = getModelProcessor().getTheJenaModel().listStatements(subj, RDFS.range, (RDFNode)null);
					boolean matchFound = false;
					while (stmtitr.hasNext()) {
						RDFNode missingSubject = stmtitr.nextStatement().getObject();
						if (missingSubject.isResource() && missingSubject.canAs(OntClass.class)) {
							try {
								if ( SadlUtils.classIsSubclassOf(missingSubject.as(OntClass.class), obj.as(OntResource.class),true, null)) {
									stmtitr.close();
									return true;
								}
							} catch (CircularDependencyException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
				else if (subj.canAs(Individual.class)){
					Individual inst = subj.as(Individual.class);
					ExtendedIterator<Resource> itr = inst.listRDFTypes(false);
					while (itr.hasNext()) {
						Resource cls = itr.next();
						boolean match = checkForPropertyDomainMatch(cls, prop, obj);
						if (match) {
							itr.close();
							return true;
						}
					}
					
				}
			}
		}
		return false;
	}

	public List<ConceptName> getBinaryOpLeftImplliedProperties() {
		return binaryOpLeftImpliedProperties;
	}

	protected void setBinaryOpLeftImpliedProperties(List<ConceptName> binaryLeftTypeCheckInfo) {
		this.binaryOpLeftImpliedProperties = binaryLeftTypeCheckInfo;
	}

	public List<ConceptName> getBinaryOpRightImpliedProperties() {
		return binaryOpRightImpliedProperties;
	}

	protected void setBinaryOpRightImpliedProperties(List<ConceptName> binaryRightTypeCheckInfo) {
		this.binaryOpRightImpliedProperties = binaryRightTypeCheckInfo;
	}

	/**
	 * Clear old values of left and right impliedProperties 
	 */
	public void clearImpliedProperties() {
		setBinaryOpLeftImpliedProperties(null);
		setBinaryOpRightImpliedProperties(null);
	}

	protected JenaBasedSadlModelProcessor getModelProcessor() throws InvalidTypeException {
		return modelProcessor;
	}

	protected void setModelProcessor(JenaBasedSadlModelProcessor modelProcessor) {
		this.modelProcessor = modelProcessor;
	}

	public void resetValidatorState(SadlModelElement element) {
		if (impliedPropertiesUsed != null) {
			impliedPropertiesUsed.clear();
		}
		if (binaryOpLeftImpliedProperties != null) {
			binaryOpLeftImpliedProperties.clear();
		}
		if (binaryOpRightImpliedProperties != null) {
			binaryOpRightImpliedProperties.clear();
		}
	}

	private Sublist getSublistContainer(EObject expression) {
		if (expression instanceof Sublist) {
			return (Sublist) expression;
		}
		if (expression.eContainer() != null) {
			return getSublistContainer(expression.eContainer());
		}
		return null;
	}

	public boolean checkPropertyValueInRange(OntModel theJenaModel, Expression subj, SadlResource pred, EObject val, StringBuilder errorMessageBuilder) throws CircularDefinitionException, DontTypeCheckException, InvalidNameException, TranslationException, URISyntaxException, IOException, ConfigurationException, InvalidTypeException, CircularDependencyException, PropertyWithoutRangeException {
		TypeCheckInfo predType = getType(pred);
		if (val == null && isInQuery(pred)) {
			return true;	// this is OK
		}
		TypeCheckInfo valType = getType(val);
		List<String> operations = Arrays.asList("is");
		if (declarationExtensions.getOntConceptType(pred).equals(OntConceptType.DATATYPE_PROPERTY)) {
			if (!checkNumericRangeLimits("=", predType, valType)) {
				return true;  // return true so as to not generate error at higher level; failure has already created marker
			}
		}
		if (compareTypes(operations , pred, val, predType, valType, ImplicitPropertySide.NONE)) {
			return true;
		}
//		// TODO check for restrictions on subj class
//		OntConceptType predtype = declarationExtensions.getOntConceptType(pred);
//		String preduri = declarationExtensions.getConceptUri(pred);
//		Property prop = theJenaModel.getProperty(preduri);
//		if (prop == null) {
//			throw new JenaProcessorException("Unable to find property '" + preduri + "' in model.");
//		}
//		RDFNode val;
//		if (value instanceof SadlResource) {
//			OntConceptType srType = declarationExtensions.getOntConceptType((SadlResource)value);
//			SadlResource srValue = (SadlResource) value;
//			if (srType == null) {
//				srValue = ((SadlResource)value).getName();
//				srType = declarationExtensions.getOntConceptType(srValue);
//			}
//			if (srType == null) {
//				throw new JenaProcessorException("Unable to resolve SadlResource value");
//			}
//			if (srType.equals(OntConceptType.INSTANCE)) {
//				String valUri = declarationExtensions.getConceptUri(srValue);
//				if (valUri == null) {
//					throw new JenaProcessorException("Failed to find SadlResource in Xtext model");
//				}
//				val = theJenaModel.getIndividual(valUri);
//				if (val == null) {
//					throw new JenaProcessorException("Failed to retrieve instance '" + valUri + "' from Jena model");
//				}
//				return checkObjectPropertyValueInRange(prop, (Individual)val);
//			}
//			else {
//				return false;
//			}
//		}
//		else if (value instanceof SadlExplicitValue) {
//			if (prop.canAs(OntProperty.class)) {
//				val = modelProcessor.sadlExplicitValueToLiteral((SadlExplicitValue) value, prop.as(OntProperty.class).getRange());
//			}
//			else {
//				val = modelProcessor.sadlExplicitValueToLiteral((SadlExplicitValue) value, null);
//			}
//			return checkDataPropertyValueInRange(theJenaModel, null, prop, val);
//		}
		
		if (createErrorMessage(errorMessageBuilder, predType, valType, operations.get(0), false, val.eContainer())) {
			return false;
		}
		else {
			return true;
		}
	}

	private boolean checkNumericRangeLimits(String op, TypeCheckInfo predType, TypeCheckInfo valType) throws TranslationException {
		if (valType == null || predType == null) {
			return false;	// return as error
		}
		if (valType.getExplicitValue() instanceof Literal) {
			 Object value = ((Literal)valType.getExplicitValue()).getValue();
			  Node rngType = predType.getTypeCheckType();
			  if (!(rngType instanceof NamedNode)) {
				  throw new TranslationException("Unexpected non-NamedNode TypeCheckInfo typeCheckType");
			  }
			  boolean outOfRange = false;
			  if (rngType.toFullyQualifiedString().equals(XSD.xint.getURI())) {
				  if (op.equals(">") || op.equals("<")) {
					  if (value instanceof Long && ((Long)value >= MAX_INT || (Long)value <= MIN_INT)) {
						  outOfRange = true;
					  }
					  else if (value instanceof Integer && ((Integer)value >= MAX_INT || (Integer)value <= MIN_INT)) {
						  outOfRange = true;
					  }
				  }
				  else {
					  if (value instanceof Long && ((Long)value > MAX_INT || (Long)value < MIN_INT)) {
						  outOfRange = true;
					  }
					  else if (value instanceof Integer && ((Integer)value > MAX_INT || (Integer)value < MIN_INT)) {
						  outOfRange = true;
					  }
				  }
				  if (outOfRange) {
					  modelProcessor.addIssueToAcceptor("Value is not in range of property", valType.context);
					  return false;
				  }
			  }
			  else if (rngType.toFullyQualifiedString().equals(XSD.xlong.getURI())) {
				  if (op.equals(">") || op.equals("<")) {
					  if (value instanceof Long && ((Long)value >= MAX_LONG || (Long)value <= MIN_LONG)) {
						  outOfRange = true;
					  }
					  else if (value instanceof Integer && ((Integer)value >= MAX_LONG || (Integer)value <= MIN_LONG)) {
						  outOfRange = true;
					  }
				  }
				  else {
					  if (value instanceof Long && ((Long)value > MAX_LONG || (Long)value < MIN_LONG)) {
						  outOfRange = true;
					  }
					  else if (value instanceof Integer && ((Integer)value > MAX_LONG || (Integer)value < MIN_LONG)) {
						  outOfRange = true;
					  }
				  }
				  if (outOfRange) {
					  modelProcessor.addIssueToAcceptor("Value is not in range of property", valType.context);
					  return false;
				  }
			  }
			 
		}
		return true;	// if we don't detect a problem assume that there isn't one
	}

	public boolean checkDataPropertyValueInRange(OntModel theJenaModel2, Resource subj, OntProperty prop, Literal val) {
		OntResource rng = prop.getRange();
		if (rng == null) {
			return true;
		}
		String ptype = prop.getRange().getURI();
		if (ptype == null) {
			return true;
		}
		String dtype = val.getDatatypeURI();
		if (dtype.equals(ptype)) {
			return true;
		}
		if (dtype.equals(XSD.xint.getURI())) {	// the literal is an integer
			if (ptype.equals(XSD.integer.getURI())) return true;
			if (ptype.equals(XSD.xlong.getURI())) return true;
		}
		return false;
	}

	public boolean checkObjectPropertyRange(OntModel theJenaModel2, OntProperty pred, OntResource obj, boolean isList, EObject expr) throws CircularDependencyException {
		if (pred.isObjectProperty()) {
			if (checkRangeForMatch(theJenaModel2, pred, obj, isList)) {
				return true;
			}
			ExtendedIterator<? extends OntProperty> propitr = pred.listSuperProperties(false);
			while (propitr.hasNext()) {
				OntProperty sprop = propitr.next();
				if (checkRangeForMatch(theJenaModel2, sprop, obj, isList)) {
					propitr.close();
					return true;
				}
			}
			return false;
		}
		return true;
	}
	
	private boolean checkRangeForMatch(OntModel theJenaModel2, OntProperty pred, OntResource obj, boolean isList) throws CircularDependencyException {
		StmtIterator rngitr = theJenaModel2.listStatements(pred, RDFS.range, (RDFNode)null);
		while (rngitr.hasNext()) {
			RDFNode rng = rngitr.nextStatement().getObject();
			if (isTypedListSubclass(rng)) {
				if (!isList) {
					rngitr.close();
					return false;
				}
				if (rng.canAs(OntClass.class )) {
					rng = getSadlTypedListType(rng.as(OntClass.class));
				}
				else {
// TODO this is an unhandled case					
				}
			}
			if (obj instanceof Individual) {
				ExtendedIterator<Resource> institr = obj.asIndividual().listRDFTypes(true);
				while (institr.hasNext()) {
					Resource typ = institr.next();
					if (rng.canAs(OntClass.class)) {
						if (SadlUtils.classIsSubclassOf(typ.as(OntClass.class), rng.asResource().as(OntClass.class), true, null)) {
							institr.close();
							rngitr.close();
							return true;
						}
					}
				}
			}
			else if (obj.canAs(OntClass.class) && rng.isResource() && rng.asResource().canAs(OntClass.class)) {
				if (SadlUtils.classIsSubclassOf(obj.as(OntClass.class), rng.asResource().as(OntClass.class), true, null)) {
					rngitr.close();
					return true;
				}
			}
		}
		return false;
	}
}
