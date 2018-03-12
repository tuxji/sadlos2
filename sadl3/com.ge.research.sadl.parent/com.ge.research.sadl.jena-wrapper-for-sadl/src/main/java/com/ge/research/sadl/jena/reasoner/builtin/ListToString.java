/************************************************************************
 * Copyright \u00a9 2007, 2008 - General Electric Company, All Rights Reserved
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

/***********************************************************************
 * $Author: crapo $ 
 * $Revision: 1.2 $ Last modified on   $Date: 2015/09/09 17:03:25 $
 ***********************************************************************/

package com.ge.research.sadl.jena.reasoner.builtin;

import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.rulesys.builtins.BaseBuiltin;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.graph.*;

/**
	This class converts an RDF list to a string of the form "{element1, element2, ....}". 
	String values are placed inside the optional string delimiter (2nd argument).
	 
 */
public class ListToString extends BaseBuiltin {

	private int argLength = 0;

    /**
     * Return a name for this builtin, normally this will be the name of the 
     * functor that will be used to invoke it.
     */
    public String getName() {
        return "listToString";
    }
    /**
     * Return the expected number of arguments for this functor or 0 if the number is flexible.
     */
    public int getArgLength() {
        return argLength;
    }
    
    /**
     * This method is invoked when the builtin is called in a rule body.
     * @param args the array of argument values for the builtin, this is an array 
     * of Nodes, some of which may be Node_RuleVariables.
     * @param length the length of the argument list, may be less than the length of the args array
     * for some rule engines
     * @param context an execution context giving access to other relevant data
     * @return return true if the buildin predicate is deemed to have succeeded in
     * the current environment
     */
    public boolean bodyCall(Node[] args, int length, RuleContext context) {
    	if (length < 2) {
    		throw new BuiltinException(this, context, "Too few arguments in call to listToString; first argument must be a list.");
    	}
    	else if (length > 3) {
    		throw new BuiltinException(this, context, "Too many arguments in call to listToString; expecting 1 or 2.");
    	}

        BindingEnvironment env = context.getEnv();
        Node n1 = getArg(0, args, context);
        String strDelim = "\"";
        if (length > 2) {
        	Node n2 = getArg(1, args, context);
        	Object v2 = n2.getLiteralValue();
        	if (v2 instanceof String) {
        		strDelim = v2.toString();
        	}
        	else {
        		throw new BuiltinException(this, context, "Second argument (string delimiter) is not a string (" + v2.toString() + ").");
        	}
        }
       	String s = listToString(context, n1, strDelim);
		Node retVal = ResourceFactory.createTypedLiteral(s).asNode();
        return env.bind(args[length - 1], retVal);
     }
    
    public static synchronized String listToString(RuleContext context, Node n1, String strDelim) {
		java.util.List<Node> l = Util.convertList(n1, context);
       	StringBuilder sb = new StringBuilder("{");
       	for (int i = 0; l != null && i < l.size(); i++) {
    		Node elt = (Node) l.get(i);
            if (elt != null) {
            	boolean quoteValue = false;
            	Object v1;
            	if (elt.isLiteral()) {
            		v1 = elt.getLiteralValue();
            		if (v1 instanceof String) {
            			quoteValue = true;
            		}
            	}
            	else if (elt.isURI()) {
            		v1 = elt.getLocalName();
            	}
            	else {
            		v1 = elt;
            	}
            	if (i > 0) {
            		sb.append(", ");
            	}
            	if (quoteValue) {
            		sb.append(strDelim);
            		sb.append(v1.toString());
            		sb.append(strDelim);
            	}
            	else {
            		sb.append(v1.toString());
            	}
            }
        }
        sb.append("}");
		return sb.toString();
	}
    
}