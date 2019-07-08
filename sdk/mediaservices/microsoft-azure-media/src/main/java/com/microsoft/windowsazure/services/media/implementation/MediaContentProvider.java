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

package com.microsoft.windowsazure.services.media.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import com.microsoft.windowsazure.services.media.implementation.content.MediaServiceDTO;
import com.sun.jersey.core.provider.AbstractMessageReaderWriterProvider;

/**
 * Class to plug into Jersey to properly serialize raw Media Services DTO types.
 * 
 */
public class MediaContentProvider<T extends MediaServiceDTO> extends
        AbstractMessageReaderWriterProvider<T> {
    private final ODataAtomMarshaller marshaller;

    /**
     * Creates the instance
     * 
     * @throws JAXBException
     * @throws ParserConfigurationException
     */
    public MediaContentProvider() throws JAXBException,
            ParserConfigurationException {
        marshaller = new ODataAtomMarshaller();
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        // This class only does marshalling, not unmarshalling.
        return false;
    }

    @Override
    public T readFrom(Class<T> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return MediaServiceDTO.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(T t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        try {
            marshaller.marshalEntry(t, entityStream);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
