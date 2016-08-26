package com.ge.research.sadl.jena;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;

public class OntModelProvider {
	
	static class OntModelAdapter extends AdapterImpl  {
		String modelName;
		OntModel model;
		Object otherContent;
		Map<EObject, Property> impliedPropertiesUsed = null;
		boolean isLoading = false;
		
		@Override
		public boolean isAdapterForType(Object type) {
			return OntModel.class == type;
		}
	}
	
	public static void registerResource(Resource resource) {
		OntModelAdapter a = findAdapter(resource);
		if (a == null) {
			a = new OntModelAdapter();
			a.isLoading = true;
			resource.eAdapters().add(a);
		}
	}
	
	public static boolean checkForCircularImport(Resource resource) {
		OntModelAdapter a = findAdapter(resource);
		if (a != null) {
			if (a.isLoading) {
				return true;
			}
		}
		return false;
	}
	
	public static void attach(Resource resource, OntModel model, String modelName) {
		OntModelAdapter adapter = findAdapter(resource);
		if (adapter == null) {
			adapter = new OntModelAdapter();
			resource.eAdapters().add(adapter);
		}
		adapter.modelName = modelName;
		adapter.model = model;
		adapter.isLoading = false;  // loading is complete
	}
	
	public static void addOtherContent(Resource resource, Object otherContent) {
		OntModelAdapter adapter = findAdapter(resource);
		if (adapter != null) {
			adapter.otherContent = otherContent;
		}
	}
	
	public static Object getOtherContent(Resource resource) {
		OntModelAdapter a = findAdapter(resource);
		if (a != null) {
			return a.otherContent;
		}
		return null;
	}
	
	public static void addImpliedProperties(Resource resource, Map<EObject, Property> impliedProperties) {
		if (resource != null) {
			OntModelAdapter a = findAdapter(resource);
			if (a == null) {
				a = new OntModelAdapter();
				a.isLoading = true;
				resource.eAdapters().add(a);
			}
			a.impliedPropertiesUsed = impliedProperties;
		}
	}
	
	public static Property getImpliedProperty(Resource resource, EObject context) {
		OntModelAdapter a = findAdapter(resource);
		if (a != null && a.impliedPropertiesUsed != null) {
			return a.impliedPropertiesUsed.get(context);
		}
		return null;
	}
	
	public static OntModelAdapter findAdapter(Resource resource) {
		if (resource != null) {
			for (Adapter a : resource.eAdapters()) {
				if (a instanceof OntModelAdapter) {
					return ((OntModelAdapter) a);
				}
			}
		}
		return null;
	}
	
	public static OntModel find(Resource resource) {
		OntModelAdapter a = findAdapter(resource);
		if (a != null) {
			return a.model;
		}
		return null;
	}
	
	public static String getModelName(Resource resource) {
		OntModelAdapter a = findAdapter(resource);
		if (a != null) {
			return a.modelName;
		}
		return null;
	}
	
}
