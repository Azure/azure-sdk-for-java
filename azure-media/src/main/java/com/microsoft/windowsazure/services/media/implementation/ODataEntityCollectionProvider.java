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

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.sun.jersey.core.provider.AbstractMessageReaderWriterProvider;

/**
 * Jersey provider to unmarshal lists of entities from Media Services.
 * 
 */
public class ODataEntityCollectionProvider extends
        AbstractMessageReaderWriterProvider<ListResult<ODataEntity<?>>> {
    private final ODataAtomUnmarshaller unmarshaller;

    public ODataEntityCollectionProvider() throws JAXBException {
        unmarshaller = new ODataAtomUnmarshaller();
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return ODataEntity.isODataEntityCollectionType(type, genericType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ListResult<ODataEntity<?>> readFrom(
            Class<ListResult<ODataEntity<?>>> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {

        String responseType = mediaType.getParameters().get("type");
        try {
            if (responseType == null || responseType.equals("feed")) {
                return unmarshaller.unmarshalFeed(entityStream,
                        (Class<ODataEntity<?>>) ODataEntity
                                .getCollectedType(genericType));
            } else {
                throw new RuntimeException();
            }
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Can this type be written by this provider?
     * 
     * @return false - we don't support writing
     */
    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return false;
    }

    /**
     * Write the given object to the stream. This method implementation throws,
     * we don't support writing.
     * 
     * @throws UnsupportedOperationException
     */
    @Override
    public void writeTo(ListResult<ODataEntity<?>> t, Class<?> type,
            Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {

        throw new UnsupportedOperationException();
    }

}
