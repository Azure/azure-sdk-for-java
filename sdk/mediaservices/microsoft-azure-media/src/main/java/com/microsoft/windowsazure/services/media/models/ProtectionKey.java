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

package com.microsoft.windowsazure.services.media.models;

import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.microsoft.windowsazure.core.pipeline.PipelineHelpers;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultEntityTypeActionOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityTypeActionOperation;
import com.microsoft.windowsazure.services.media.implementation.content.ProtectionKeyIdType;
import com.microsoft.windowsazure.services.media.implementation.content.ProtectionKeyRestType;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Class for creating operations to manipulate protection key pseudo-entities.
 * 
 */
public final class ProtectionKey {

    private ProtectionKey() {
    }

    /**
     * Gets the protection key id.
     * 
     * @param contentKeyType
     *            the content key type
     * @return the protection key id
     */
    public static EntityTypeActionOperation<String> getProtectionKeyId(
            ContentKeyType contentKeyType) {
        return new GetProtectionKeyIdActionOperation("GetProtectionKeyId")
                .addQueryParameter("contentKeyType",
                        String.format("%d", contentKeyType.getCode()))
                .setAcceptType(MediaType.APPLICATION_XML_TYPE);
    }

    /**
     * Gets the protection key.
     * 
     * @param protectionKeyId
     *            the protection key id
     * @return the protection key
     */
    public static EntityTypeActionOperation<String> getProtectionKey(
            String protectionKeyId) {
        return new GetProtectionKeyActionOperation("GetProtectionKey")
                .addQueryParameter("ProtectionKeyId",
                        String.format("'%s'", protectionKeyId)).setAcceptType(
                        MediaType.APPLICATION_XML_TYPE);
    }

    /**
     * The Class GetProtectionKeyIdActionOperation.
     * 
     * @param <T>
     */
    private static class GetProtectionKeyIdActionOperation extends
            DefaultEntityTypeActionOperation<String> {

        /** The jaxb context. */
        private final JAXBContext jaxbContext;

        /** The unmarshaller. */
        private final Unmarshaller unmarshaller;

        /**
         * Instantiates a new gets the protection key id action operation.
         * 
         * @param name
         *            the name
         */
        public GetProtectionKeyIdActionOperation(String name) {
            super(name);
            try {
                jaxbContext = JAXBContext
                        .newInstance(ProtectionKeyIdType.class);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }

            try {
                unmarshaller = jaxbContext.createUnmarshaller();
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.microsoft.windowsazure.services.media.entityoperations.
         * DefaultActionOperation
         * #processResponse(com.sun.jersey.api.client.ClientResponse)
         */
        @Override
        public String processTypeResponse(ClientResponse clientResponse) {
            PipelineHelpers.throwIfNotSuccess(clientResponse);
            ProtectionKeyIdType protectionKeyIdType;
            try {
                protectionKeyIdType = parseResponse(clientResponse);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }

            return protectionKeyIdType.getProtectionKeyId();
        }

        /**
         * Parses the response.
         * 
         * @param clientResponse
         *            the client response
         * @return the protection key id type
         * @throws JAXBException
         *             the jAXB exception
         */
        private ProtectionKeyIdType parseResponse(ClientResponse clientResponse)
                throws JAXBException {

            InputStream inputStream = clientResponse.getEntityInputStream();
            JAXBElement<ProtectionKeyIdType> protectionKeyIdTypeJaxbElement = unmarshaller
                    .unmarshal(new StreamSource(inputStream),
                            ProtectionKeyIdType.class);
            return protectionKeyIdTypeJaxbElement.getValue();

        }

    }

    /**
     * The Class GetProtectionKeyActionOperation.
     */
    private static class GetProtectionKeyActionOperation extends
            DefaultEntityTypeActionOperation<String> {

        /** The jaxb context. */
        private final JAXBContext jaxbContext;

        /** The unmarshaller. */
        private final Unmarshaller unmarshaller;

        /**
         * Instantiates a new gets the protection key action operation.
         * 
         * @param name
         *            the name
         */
        public GetProtectionKeyActionOperation(String name) {
            super(name);
            try {
                jaxbContext = JAXBContext
                        .newInstance(ProtectionKeyRestType.class);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }

            try {
                unmarshaller = jaxbContext.createUnmarshaller();
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.microsoft.windowsazure.services.media.entityoperations.
         * DefaultActionOperation
         * #processResponse(com.sun.jersey.api.client.ClientResponse)
         */
        @Override
        public String processTypeResponse(ClientResponse clientResponse) {
            PipelineHelpers.throwIfNotSuccess(clientResponse);
            ProtectionKeyRestType protectionKeyRestType;
            try {
                protectionKeyRestType = parseResponse(clientResponse);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }

            return protectionKeyRestType.getProtectionKey();
        }

        /**
         * Parses the response.
         * 
         * @param clientResponse
         *            the client response
         * @return the protection key rest type
         * @throws JAXBException
         *             the jAXB exception
         */
        private ProtectionKeyRestType parseResponse(
                ClientResponse clientResponse) throws JAXBException {
            InputStream inputStream = clientResponse.getEntityInputStream();
            JAXBElement<ProtectionKeyRestType> protectionKeyTypeJaxbElement = unmarshaller
                    .unmarshal(new StreamSource(inputStream),
                            ProtectionKeyRestType.class);
            return protectionKeyTypeJaxbElement.getValue();

        }
    }
}
