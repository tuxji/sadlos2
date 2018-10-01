/************************************************************************
 * Copyright © 2007-2018 - General Electric Company, All Rights Reserved
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
package com.ge.research.sadl.external;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.emf.common.EMFPlugin;

import java.util.Properties;

import com.ge.research.sadl.builder.ConfigurationManagerForIDE;
import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.utils.SadlUtils;
import com.ge.research.sadl.utils.NetworkProxySettingsProvider;
import com.ge.research.sadl.utils.ResourceManager;
import com.ge.research.sadl.utils.SadlProjectHelper;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * A stateless helper class for downloading the external SADL models from the
 * Internet.
 * 
 * <p>
 * <b>Note:</b> I have shamelessly copied the logic from the
 * {@code UrlListEditor} and got rid of the Eclipse and UI dependencies. I have
 * kept both the formatting and the logic as close to the original
 * implementation as possible.
 * 
 * @author akos.kitta
 */
@Singleton
public class ExternalEmfModelDownloader {
	
	private SadlProjectHelper projectHelper;

	@Inject
	public ExternalEmfModelDownloader(SadlProjectHelper projectHelper) {
		this.projectHelper = projectHelper;
	}

	public void downloadModels(URI modelDefinitionUri) {
		SadlUtils su = new SadlUtils();
		String editorText = readFileContent(modelDefinitionUri);
		List<String>[] urlsAndPrefixes = su.getUrlsAndPrefixesFromExternalUrlContent(editorText);
		List<String> urls = urlsAndPrefixes[0];
		Path modelsFolder = Paths.get(this.projectHelper.getRoot(modelDefinitionUri)).resolve(ResourceManager.OWLDIR);
		String modelFolderPath = modelsFolder.toString();
		IConfigurationManagerForIDE cm = null;
		try {
			cm = getConfigMgr(modelFolderPath, ConfigurationManagerForIDE.getOWLFormat());
		} catch (ConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String sFolder = su.getExternalModelRootFromUrlFilename(new File(modelDefinitionUri));
		Path outputPath = Paths.get(modelDefinitionUri).getParent().resolve(sFolder);
		SadlUtils.recursiveDelete(outputPath.toFile());
		List<String> uploadedFiles = new ArrayList<String>();
		for (int i = 0; i < urls.size(); i++) {
			try {
				String urlPath = su.externalUrlToRelativePath((String) urls.get(i));
				String filename = downloadURL((String) urls.get(i), outputPath, urlPath);
				if (filename != null) {
					uploadedFiles.add(filename);
					String publicUri = cm.getBaseUriFromOwlFile(filename);
					String altUrl = su.fileNameToFileUrl(filename);
					if (publicUri != null && altUrl != null) {
						cm.addMapping(altUrl, publicUri, null, false, "External Model");
					}
				}
				// get xml:base from the uploaded OWL file--this is the namespace to be mapped
				// to from the filename
				// add the mapping of filename ->xml:base to the policy file (here? or on build
				// of OWL files?
				// get the import URIs from this uploaded OWL file and save them to check when
				// all uploads are done
				// TODO
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private String readFileContent(URI modelDefinitionUri){
		try {
			return Files.toString(new File(modelDefinitionUri), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// The `downloadsRootFolder` is java.nio path instead of the
	// eclipse.core.runtime.path.
	public String downloadURL(String downloadUrl, Path downloadsRootFolder, String destinationRelativePath) {
		URL url;
		InputStream is = null;

		if (downloadUrl != null && !downloadUrl.isEmpty() && !downloadUrl.startsWith("--")) {
			try {
				Properties p = System.getProperties();
				Iterator<Object> pitr = p.keySet().iterator();
				while (pitr.hasNext()) {
					Object key = pitr.next();
					Object prop = p.get(key);
					// System.out.println("Key=" + key.toString() + ", value = " + prop.toString());
				}
				if (EMFPlugin.IS_ECLIPSE_RUNNING) {					
					for (Entry<String, String> entry : new NetworkProxySettingsProvider().getConfigurations().entrySet()) {
						p.put(entry.getKey(), entry.getValue());
					}
				}
				System.setProperties(p);
				url = new URL(downloadUrl);
				is = url.openStream(); // throws an IOException
				ReadableByteChannel rbc = Channels.newChannel(is);

				String outputPath = downloadsRootFolder.resolve(destinationRelativePath).toString();
				File file1 = new File(outputPath.substring(0, outputPath.lastIndexOf("/")));
				file1.mkdirs();
				FileOutputStream fos = new FileOutputStream(outputPath);
				long bytesTransferred = fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.close();
				if (bytesTransferred < 1) {
					System.err.println("Failed to get any content from external source '" + downloadUrl + "'");
				}
				return outputPath;

			} catch (MalformedURLException mue) {
				mue.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally {
				try {
					if (is != null)
						is.close();
				} catch (IOException ioe) {
					// nothing to see here
				}
			}
		}
		return null;
	}

	private IConfigurationManagerForIDE getConfigMgr(String modelFolder, String format) throws ConfigurationException {
		return ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(modelFolder, format);
	}

}