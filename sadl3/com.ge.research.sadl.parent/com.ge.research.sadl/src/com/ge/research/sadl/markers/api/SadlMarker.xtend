/************************************************************************
 * Copyright © 2007-2017 - General Electric Company, All Rights Reserved
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
package com.ge.research.sadl.markers.api

/**
 * Representation of a SADL marker.
 * 
 * @author akos.kitta
 */
interface SadlMarker {

	/**
	 * Returns with the (EMF) URI of the resource where this marker belongs to. 
	 */
	def String getUri();

	/**
	 * Returns with the human readable message of this marker.
	 * Eventually, this is the description of the marker.
	 */
	def String getMessage();

	/**
	 * The name of the AST element this marker attached to in the resource.
	 */
	def String getName()

	/**
	 * The severity of the marker.
	 */
	def SadlMarkerSeverity getSeverity();

	/**
	 * The unique path or identifier of the error ({@code .err}) file where this
	 * marker is from.
	 */
	def String getOrigin();

}
