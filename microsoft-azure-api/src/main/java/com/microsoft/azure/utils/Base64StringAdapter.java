package com.microsoft.azure.utils;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.sun.jersey.core.util.Base64;

/*
 * JAXB adapter for a Base64 encoded string element
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
