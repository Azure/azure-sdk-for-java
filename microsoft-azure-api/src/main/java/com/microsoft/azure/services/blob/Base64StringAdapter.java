package com.microsoft.azure.services.blob;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.sun.jersey.core.util.Base64;

/*
 * JAXB adapter for <Metadata> element
 */
public class Base64StringAdapter extends XmlAdapter<String, String> {

    @Override
    public String marshal(String arg0) throws Exception {
        return new String(Base64.encode(arg0));
    }

    @Override
    public String unmarshal(String arg0) throws Exception {
        return Base64.base64Decode(arg0);
    }
}
