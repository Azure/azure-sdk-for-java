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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import com.microsoft.windowsazure.services.media.implementation.atom.ContentType;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.atom.FeedType;
import com.microsoft.windowsazure.services.media.implementation.content.AccessPolicyType;
import com.microsoft.windowsazure.services.media.implementation.content.AssetDeliveryPolicyRestType;
import com.microsoft.windowsazure.services.media.implementation.content.AssetFileType;
import com.microsoft.windowsazure.services.media.implementation.content.AssetType;
import com.microsoft.windowsazure.services.media.implementation.content.ChannelType;
import com.microsoft.windowsazure.services.media.implementation.content.Constants;
import com.microsoft.windowsazure.services.media.implementation.content.ContentKeyAuthorizationPolicyOptionType;
import com.microsoft.windowsazure.services.media.implementation.content.ContentKeyAuthorizationPolicyType;
import com.microsoft.windowsazure.services.media.implementation.content.ContentKeyRestType;
import com.microsoft.windowsazure.services.media.implementation.content.EncodingReservedUnitRestType;
import com.microsoft.windowsazure.services.media.implementation.content.JobNotificationSubscriptionType;
import com.microsoft.windowsazure.services.media.implementation.content.JobType;
import com.microsoft.windowsazure.services.media.implementation.content.LocatorRestType;
import com.microsoft.windowsazure.services.media.implementation.content.NotificationEndPointType;
import com.microsoft.windowsazure.services.media.implementation.content.OperationType;
import com.microsoft.windowsazure.services.media.implementation.content.ProgramType;
import com.microsoft.windowsazure.services.media.implementation.content.StorageAccountType;
import com.microsoft.windowsazure.services.media.implementation.content.StreamingEndpointType;
import com.microsoft.windowsazure.services.media.implementation.content.TaskType;

/**
 * A class to manage marshalling of request parameters into ATOM entry elements
 * for sending to the Media Services REST endpoints.
 * 
 */
public class ODataAtomMarshaller {
    private final Marshaller marshaller;
    private final DocumentBuilder documentBuilder;

    public ODataAtomMarshaller() throws JAXBException,
            ParserConfigurationException {
        JAXBContext context = JAXBContext.newInstance(getMarshalledClasses(),
                null);
        marshaller = context.createMarshaller();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        documentBuilder = dbf.newDocumentBuilder();
    }

    /**
     * Convert the given content object into an ATOM entry (represented as a DOM
     * document) suitable for sending up to the Media Services service.
     * 
     * @param content
     *            The content object to send
     * @return The generated DOM
     * @throws JAXBException
     *             if content is malformed/not marshallable
     */
    public Document marshalEntry(Object content) throws JAXBException {
        JAXBElement<EntryType> entryElement = createEntry(content);

        Document doc = documentBuilder.newDocument();
        doc.setXmlStandalone(true);

        marshaller.marshal(entryElement, doc);

        return doc;

    }

    /**
     * Convert the given content into an ATOM entry and write it to the given
     * stream.
     * 
     * @param content
     *            Content object to send
     * @param stream
     *            Stream to write to
     * @throws JAXBException
     *             if content is malformed/not marshallable
     */
    public void marshalEntry(Object content, OutputStream stream)
            throws JAXBException {
        marshaller.marshal(createEntry(content), stream);
    }

    public void marshalEntryType(EntryType entryType, OutputStream stream)
            throws JAXBException {
        marshaller.marshal(new JAXBElement<EntryType>(new QName(
                Constants.ATOM_NS, "entry"), EntryType.class, entryType),
                stream);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private JAXBElement<EntryType> createEntry(Object content) {
        ContentType atomContent = new ContentType();
        EntryType atomEntry = new EntryType();

        atomContent.setType("application/xml");
        atomContent.getContent().add(
                new JAXBElement(new QName(Constants.ODATA_METADATA_NS,
                        "properties"), content.getClass(), content));

        atomEntry.getEntryChildren().add(
                new JAXBElement(new QName(Constants.ATOM_NS, "content"),
                        ContentType.class, atomContent));

        JAXBElement<EntryType> entryElement = new JAXBElement<EntryType>(
                new QName(Constants.ATOM_NS, "entry"), EntryType.class,
                atomEntry);

        return entryElement;
    }

    private static Class<?>[] getMarshalledClasses() {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(AccessPolicyType.class);
        classes.add(AssetDeliveryPolicyRestType.class);
        classes.add(AssetType.class);
        classes.add(AssetFileType.class);
        classes.add(ChannelType.class);
        classes.add(ContentKeyAuthorizationPolicyType.class);
        classes.add(ContentKeyAuthorizationPolicyOptionType.class);
        classes.add(ContentKeyRestType.class);
        classes.add(EncodingReservedUnitRestType.class);
        classes.add(EntryType.class);
        classes.add(FeedType.class);
        classes.add(JobNotificationSubscriptionType.class);
        classes.add(JobType.class);
        classes.add(LocatorRestType.class);
        classes.add(NotificationEndPointType.class);
        classes.add(OperationType.class);
        classes.add(ProgramType.class);
        classes.add(StorageAccountType.class);
        classes.add(StreamingEndpointType.class);
        classes.add(TaskType.class);
        return classes.toArray(new Class<?>[0]);
    }
}
