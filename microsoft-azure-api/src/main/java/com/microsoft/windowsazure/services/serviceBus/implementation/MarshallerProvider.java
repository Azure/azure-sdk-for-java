package com.microsoft.windowsazure.services.serviceBus.implementation;

import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

@Provider
@Produces("application/atom+xml")
public class MarshallerProvider implements ContextResolver<Marshaller> {

    @Context
    private ContextResolver<JAXBContext> jaxbContextResolver;

    public Marshaller getContext(Class<?> type) {
        Marshaller marshaller;
        try {
            marshaller = getJAXBContext(type).createMarshaller();
        }
        catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        com.sun.xml.bind.marshaller.NamespacePrefixMapper mapper = new NamespacePrefixMapperImpl();
        try {
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", mapper);
        }
        catch (PropertyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return marshaller;
    }

    private JAXBContext getJAXBContext(Class<?> type) throws Exception {
        JAXBContext context = null;
        if (jaxbContextResolver != null) {
            context = jaxbContextResolver.getContext(type);
        }
        if (context == null) {
            context = JAXBContext.newInstance(type);
        }
        return context;
    }

}
