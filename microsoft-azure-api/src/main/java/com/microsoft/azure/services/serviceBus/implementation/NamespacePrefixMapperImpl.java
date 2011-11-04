package com.microsoft.azure.services.serviceBus.implementation;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class NamespacePrefixMapperImpl extends NamespacePrefixMapper {

	@Override
	public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
		if (namespaceUri == "http://www.w3.org/2005/Atom") {
			return "atom";
		}
		else if (namespaceUri == "http://schemas.microsoft.com/netservices/2010/10/servicebus/connect") {
			return "";
		}
		return suggestion;
	}

}
