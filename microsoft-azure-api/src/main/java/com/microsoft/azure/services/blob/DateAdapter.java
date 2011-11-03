package com.microsoft.azure.services.blob;

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/*
 * JAXB adapter for <Metadata> element
 */
public class DateAdapter extends XmlAdapter<String, Date> {

    @Override
    public Date unmarshal(String arg0) throws Exception {
        return new DateMapper().parse(arg0);
    }

    @Override
    public String marshal(Date arg0) throws Exception {
        return new DateMapper().format(arg0);
    }
}
