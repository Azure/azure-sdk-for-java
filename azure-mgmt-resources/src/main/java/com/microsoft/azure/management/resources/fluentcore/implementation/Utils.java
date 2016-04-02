/**
* Copyright (c) Microsoft Corporation
* 
* All rights reserved. 
* 
* MIT License
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
* (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
* publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
* subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
* ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH 
* THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.microsoft.azure.management.resources.fluentcore.implementation;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Utils {
	
	// Create a new self-signed public/private key pair for an X.509 certificate packaged inside a PKCS#12 (PFX) file
	public static File createCertPkcs12(
			File targetPfxFile, 
			File jdkDirectory, 
			String alias, 
			String password, 
			String cnName, 
			int daysValid) throws Exception {
		
		final File keytool = getKeytool(jdkDirectory);
		final String[] commandArgs = { 
				keytool.toString(), 
				"-genkey", 
				"-alias", alias, 
				"-storetype", "pkcs12", 
				"-keystore", targetPfxFile.toString(),
				"-storepass", password,
				"-validity", String.valueOf(daysValid),
				"-keyalg", "RSA",
				"-keysize", "2048",
				"-storetype", "pkcs12",
				"-dname", "CN=" + cnName 
			};
		
		invokeCommand(commandArgs, false);
		
		if(!targetPfxFile.exists()) {
			throw new IOException("Failed to create PFX file");
		} else {
			return targetPfxFile;
		}
	}
	
	
	// Extract the public X.509 certificate from a PFX file and save as a CER file
	public static File createCertPublicFromPkcs12(
			File sourcePfxFile, 
			File targetCerFile, 
			File jdkDirectory,
			String alias,
			String password) throws Exception {
		
		if(sourcePfxFile == null || !sourcePfxFile.exists()) {
			throw new IOException("Incorrect source PFX file path");
		} 
		
		final File keyTool = getKeytool(jdkDirectory);
		final String[] commandArgs = {
				keyTool.toString(),
				"-export", 
				"-alias", alias,
				"-storetype", "pkcs12",
				"-keystore", sourcePfxFile.toString(),
				"-storepass", password,
				"-rfc",
				"-file", targetCerFile.toString()
		};
				
		invokeCommand(commandArgs, true);
		
		if(!targetCerFile.exists()) {
			throw new IOException("Failed to create CER file");
		} else {
			return targetCerFile;
		}
	}
	
	
	/**
	 * Invoke a shell command.
	 * 
	 * @param command :Command line to invoke, including arguments
	 * @param ignoreErrorStream :Set to true if exception is to be thrown when the error stream is not empty.
	 * @return result :The text contents of the output of the invoked command
	 * @throws Exception
	 * @throws IOException
	 */
	private static String invokeCommand(String[] command, boolean ignoreErrorStream) throws Exception, IOException {
		String result, error;
		InputStream inputStream = null, errorStream = null;
		BufferedReader inputReader = null, errorReader = null;
		try {
			Process process = new ProcessBuilder(command).start();
			inputStream = process.getInputStream();
			errorStream = process.getErrorStream();
			inputReader = new BufferedReader(new InputStreamReader(inputStream));
			result = inputReader.readLine();
			process.waitFor();
			errorReader = new BufferedReader(new InputStreamReader(errorStream));
			error = errorReader.readLine();
			if (error != null && !error.isEmpty() && !ignoreErrorStream) {
				throw new Exception(error, null);
			}
		} catch (Exception e) {
			throw new Exception("Exception occurred while invoking command", e);
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
			if (errorStream != null) {
				errorStream.close();
			}
			if (inputReader != null) {
				inputReader.close();
			}
			if (errorReader != null) {
				errorReader.close();
			}
		}
		
		return result;
	}
	
	
	// Returns the file path to the keytool
	private static File getKeytool(File jdkDirectory) throws IOException {
		File binDirectory = new File(jdkDirectory, "bin");
		if(jdkDirectory == null || !jdkDirectory.isDirectory()) {
			throw new IOException("Incorrect JDK directory path");
		} else if(!binDirectory.isDirectory()) {
			throw new IOException("JDK directory is missing teh bin subdirectory");
		} else {
			return new File(binDirectory, "keytool");
		}
	}
	
	
	// Returns the first node matching the xpath in the xml
	static public Node findXMLNode(String xml, String xpath) throws XPathExpressionException {
		final InputSource parentSource = new InputSource(new StringReader(xml));
		final XPath xpathObject = XPathFactory.newInstance().newXPath();
		return (Node) xpathObject.evaluate(xpath, parentSource, XPathConstants.NODE);
	}
	
	
	// Deletes the XML element from the provided XML string based on the XPath
	public static String deleteXMLElement(String xml, String xpath) {
		try {
			final Node node = findXMLNode(xml, xpath);
			Node parent = node.getParentNode();
			parent.removeChild(node);
			return XMLtoString(parent.getOwnerDocument());
		} catch (XPathExpressionException e) {
			return null;
		}
	}
	
	
	// Returns the XML document as a string
	static String XMLtoString(Document doc) {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
			return writer.getBuffer().toString();
		} catch(Exception e) {
			return null;
		}
	}
	
	
	// Loads XML from a file
	public static Document loadXml(File file) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(file);
	}
	
	
	// Inserts XML string as a child node into another XML string based on the provided xpath
	public static String insertXMLElement(String parentXML, String childXMLElement, String parentXPath) {
		try {
			// Find parent node based on XPath
			final Node parentNode = Utils.findXMLNode(parentXML, parentXPath);

			// Parse child XML as Node to insert
			final InputSource insertionSource = new InputSource(new StringReader(childXMLElement));
			final Document childDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(insertionSource);

			// Insert as the last child of the parent
			final Document parentDoc = parentNode.getOwnerDocument();
			parentNode.appendChild(parentDoc.importNode(childDoc.getDocumentElement(), true));

			// Transform into a string
			return Utils.XMLtoString(parentDoc);
		} catch (Exception e) {
			return null;
		}
	}
}
