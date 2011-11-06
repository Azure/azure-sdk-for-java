package com.microsoft.azure.utils;

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;


/*
 * JAXB adapter for <Metadata> element
 */
public class  RFC1123DateAdapter extends XmlAdapter<String, Date> {

    @Override
    public Date unmarshal(String arg0) throws Exception {
        return new RFC1123DateMapper().parse(arg0);
    }

    @Override
    public String marshal(Date arg0) throws Exception {
        return new RFC1123DateMapper().format(arg0);
    }
}
