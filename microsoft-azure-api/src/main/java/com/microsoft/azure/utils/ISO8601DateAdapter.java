package com.microsoft.azure.utils;

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;


/*
 * JAXB adapter for an ISO 8601 date time element
 */
public class  ISO8601DateAdapter extends XmlAdapter<String, Date> {

    @Override
    public Date unmarshal(String arg0) throws Exception {
        return new ISO8601DateConverter().parse(arg0);
    }

    @Override
    public String marshal(Date arg0) throws Exception {
        return new ISO8601DateConverter().format(arg0);
    }
}
