package com.microsoft.windowsazure.services.blob.implementation;

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/*
 * TODO: Move so some other common package?
 * 
 * JAXB adapter for a "not quite" ISO 8601 date time element
 */
public class ContainerACLDateAdapter extends XmlAdapter<String, Date> {

    @Override
    public Date unmarshal(String arg0) throws Exception {
        return new ContainerACLDateConverter().parse(arg0);
    }

    @Override
    public String marshal(Date arg0) throws Exception {
        return new ContainerACLDateConverter().format(arg0);
    }
}
