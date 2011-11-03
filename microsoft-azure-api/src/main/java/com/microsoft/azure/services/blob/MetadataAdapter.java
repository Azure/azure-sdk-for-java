package com.microsoft.azure.services.blob;

import java.util.HashMap;

import javax.naming.OperationNotSupportedException;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.w3c.dom.Element;

/*
 * JAXB adapter for <Metadata> element
 */
public class MetadataAdapter extends XmlAdapter<MetadataHashMapType, HashMap<String, String>> {

    @Override
    public HashMap<String, String> unmarshal(MetadataHashMapType arg0) throws Exception {
        HashMap<String, String> result = new HashMap<String, String>();
        for (Element entry : arg0.getEntries()) {
            result.put(entry.getLocalName(), entry.getFirstChild().getNodeValue());
        }
        return result;
    }

    @Override
    public MetadataHashMapType marshal(HashMap<String, String> arg0) throws Exception {
        // We don't need marshaling for blob/container metadata
        throw new OperationNotSupportedException();
    }
}
