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

package com.microsoft.windowsazure.services.media.entityoperations;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.InvalidParameterException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import com.microsoft.windowsazure.services.media.implementation.content.Constants;
import com.microsoft.windowsazure.services.media.implementation.content.MediaUriType;

/**
 * Generic implementation of $link operation of two entities.
 */
public class EntityLinkOperation extends DefaultActionOperation {

    /** The primary entity set. */
    private final String primaryEntitySet;

    /** The primary entity id. */
    private final String primaryEntityId;

    /** The secondary entity set. */
    private final String secondaryEntitySet;

    /** The secondary entity uri. */
    private final URI secondaryEntityUri;

    /** The jaxb context. */
    private final JAXBContext jaxbContext;

    /** The marshaller. */
    private final Marshaller marshaller;

    /** The document builder. */
    private final DocumentBuilder documentBuilder;

    /** The document builder factory. */
    private final DocumentBuilderFactory documentBuilderFactory;

    /**
     * Instantiates a new entity link operation.
     * 
     * @param primaryEntitySet
     *            the primary entity set
     * @param primaryEntityId
     *            the primary entity id
     * @param secondaryEntitySet
     *            the secondary entity set
     * @param secondaryEntityUri
     *            the secondary entity uri
     */
    public EntityLinkOperation(String primaryEntitySet, String primaryEntityId,
            String secondaryEntitySet, URI secondaryEntityUri) {
        super();
        this.primaryEntitySet = primaryEntitySet;
        this.primaryEntityId = primaryEntityId;
        this.secondaryEntitySet = secondaryEntitySet;
        this.secondaryEntityUri = secondaryEntityUri;
        try {
            jaxbContext = JAXBContext.newInstance(MediaUriType.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        try {
            marshaller = jaxbContext.createMarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.microsoft.windowsazure.services.media.entities.EntityDeleteOperation
     * #getUri()
     */
    @Override
    public String getUri() {
        String escapedEntityId;
        try {
            escapedEntityId = URLEncoder.encode(primaryEntityId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new InvalidParameterException(
                    "UTF-8 encoding is not supported.");
        }
        return String.format("%s('%s')/$links/%s", primaryEntitySet,
                escapedEntityId, secondaryEntitySet);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.microsoft.windowsazure.services.media.entityoperations.
     * DefaultActionOperation#getVerb()
     */
    @Override
    public String getVerb() {
        return "POST";
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.microsoft.windowsazure.services.media.entityoperations.
     * DefaultActionOperation#getRequestContents()
     */
    @Override
    public Object getRequestContents() {
        MediaUriType mediaUriType = new MediaUriType();
        mediaUriType.setUri(getProxyData().getServiceUri().toString()
                + this.secondaryEntityUri.toString());
        JAXBElement<MediaUriType> mediaUriTypeElement = new JAXBElement<MediaUriType>(
                new QName(Constants.ODATA_DATA_NS, "uri"), MediaUriType.class,
                mediaUriType);
        Document document = documentBuilder.newDocument();
        document.setXmlStandalone(true);
        try {
            marshaller.marshal(mediaUriTypeElement, document);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        return document;
    }
}
