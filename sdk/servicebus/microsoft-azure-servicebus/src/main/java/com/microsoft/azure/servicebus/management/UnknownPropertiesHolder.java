package com.microsoft.azure.servicebus.management;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/*
 * In future, servicebus may add new properties to entity description.
 * This class helps to hold such properties and send them back 'as is' in UpdateEntity call.
 * Order of xml elements is important. Maintain properties in the same order they were
 * received and send them back in the same order in UpdateEntity call.
 */
class UnknownPropertiesHolder {
	
	private List<Element> unknownProperties;
	
	synchronized void addUnknownProperty(Element property) {
		if (this.unknownProperties == null)
		{
			this.unknownProperties = new ArrayList<Element>();
		}
		
		this.unknownProperties.add(property);
	}
	
	List<Element> getUnknownProperties() {
		return this.unknownProperties;
	}
	
	synchronized void appendUnknownPropertiesToDescriptionElement(Element descriptionElement) {
		if (this.unknownProperties != null) {
			for (Element unknownElement : this.unknownProperties) {
				Node importedElement = descriptionElement.getOwnerDocument().importNode(unknownElement, true);
				descriptionElement.appendChild(importedElement);
			}
		}
	}
	
}
