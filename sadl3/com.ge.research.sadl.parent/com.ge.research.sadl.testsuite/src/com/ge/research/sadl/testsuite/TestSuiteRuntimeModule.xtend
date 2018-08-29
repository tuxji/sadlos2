/************************************************************************
 * Copyright 2007-2016- General Electric Company, All Rights Reserved
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
package com.ge.research.sadl.testsuite

import com.ge.research.sadl.ValueConverterService
import com.ge.research.sadl.scoping.SadlQualifiedNameProvider
import org.eclipse.xtext.generator.IOutputConfigurationProvider
import com.ge.research.sadl.generator.SADLOutputConfigurationProvider
import org.eclipse.xtext.generator.IContextualOutputConfigurationProvider
import org.eclipse.xtext.naming.IQualifiedNameConverter
import com.ge.research.sadl.scoping.SadlQualifiedNameConverter
import org.eclipse.xtext.linking.impl.LinkingDiagnosticMessageProvider
import com.ge.research.sadl.validation.SoftLinkingMessageProvider
import org.eclipse.xtext.validation.ResourceValidatorImpl
import com.ge.research.sadl.validation.ResourceValidator
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionStrategy
import com.ge.research.sadl.resource.SadlResourceDescriptionStrategy
import org.eclipse.xtext.linking.impl.DefaultLinkingService
import com.ge.research.sadl.scoping.ErrorAddingLinkingService
import org.eclipse.xtext.parsetree.reconstr.IParseTreeConstructor
import org.eclipse.xtext.linking.impl.ImportedNamesAdapter
import com.ge.research.sadl.scoping.SilencedImportedNamesAdapter
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.parsetree.reconstr.ITokenStream
import java.io.IOException

/**
 * Use this class to register components to be used at runtime / without the Equinox extension registry.
 */
class TestSuiteRuntimeModule extends AbstractTestSuiteRuntimeModule {
	
		
	override bindIValueConverterService() {
		return ValueConverterService;
	}
	
	override bindIQualifiedNameProvider() {
		return SadlQualifiedNameProvider;
	}
	
	def Class<? extends IOutputConfigurationProvider> bindIOutputConfigurationProvider() {
		return SADLOutputConfigurationProvider;	
	}
	
	def Class<? extends IContextualOutputConfigurationProvider> bindIContextualOutputConfigurationProvider() {
		return SADLOutputConfigurationProvider;
	}
	
	def Class<? extends IQualifiedNameConverter> bindIQualifiedNameCoverter() {
		return SadlQualifiedNameConverter;
	}
	
	def Class<? extends LinkingDiagnosticMessageProvider> bindILinkingDiagnosticMessageProvider() {
		return SoftLinkingMessageProvider;
	}
	
	def Class<? extends ResourceValidatorImpl> bindResourceValidatorImpl() {
		return ResourceValidator;
	}
	
	def Class<? extends DefaultResourceDescriptionStrategy> bindResourceDescritpionStrategy() {
		return SadlResourceDescriptionStrategy;
	}
	
	def Class<? extends DefaultLinkingService> bindDefaultLinkingService() {
		return ErrorAddingLinkingService;
	}
	
	def Class<? extends IParseTreeConstructor> bindIParseTreeConstructor() {
		NoImplParseTreeConstructor
	}
	
	def Class<? extends ImportedNamesAdapter> bindImportedNamesAdapter() {
		return SilencedImportedNamesAdapter; 
	}
	
	static class NoImplParseTreeConstructor implements IParseTreeConstructor {
		
		override serializeSubtree(EObject object, ITokenStream out) throws IOException {
			throw new UnsupportedOperationException("TODO: auto-generated method stub")
		}
		
	}
}
