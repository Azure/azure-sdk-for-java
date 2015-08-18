/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.servicebus.implementation;

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

    @Override
    public Marshaller getContext(Class<?> type) {
        Marshaller marshaller;
        try {
            marshaller = getJAXBContext(type).createMarshaller();
        } catch (JAXBException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
        com.sun.xml.bind.marshaller.NamespacePrefixMapper mapper = new NamespacePrefixMapperImpl();
        try {
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",
                    mapper);
        } catch (PropertyException e) {
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
