/**
 * Copyright 2011 Microsoft Corporation
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

import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Element;

import com.microsoft.windowsazure.services.media.implementation.atom.ContentType;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.atom.FeedType;
import com.microsoft.windowsazure.services.media.implementation.content.AssetType;
import com.microsoft.windowsazure.services.media.implementation.content.Constants;
import com.microsoft.windowsazure.services.media.implementation.content.ODataActionType;

public class ODataAtomUnmarshaller {
    private final JAXBContext atomContext;
    private final JAXBContext mediaContentContext;
    private final Unmarshaller atomUnmarshaller;
    private final Unmarshaller mediaContentUnmarshaller;

    public ODataAtomUnmarshaller() throws JAXBException {
        atomContext = JAXBContext.newInstance(FeedType.class.getPackage().getName());
        atomUnmarshaller = atomContext.createUnmarshaller();
        mediaContentContext = JAXBContext.newInstance(AssetType.class.getPackage().getName());
        mediaContentUnmarshaller = mediaContentContext.createUnmarshaller();
    }

    public ODataEntity<?> unmarshal(InputStream stream, Class<?> contentType) throws JAXBException {
        JAXBElement<FeedType> feedElement = atomUnmarshaller.unmarshal(new StreamSource(stream), FeedType.class);
        FeedType feed = feedElement.getValue();
        EntryType firstEntry = getFirstEntry(feed);

        Object content = getEntryContent(firstEntry, contentType);

        return new ODataEntity(firstEntry, content);
    }

    private Object getEntryContent(EntryType entry, Class<?> contentType) throws JAXBException {
        List<Object> entryChildren = entry.getEntryChildren();
        Object retval = null;

        for (int i = 0; i < entryChildren.size(); ++i) {
            Object child = entryChildren.get(i);
            if (child instanceof JAXBElement) {
                // It's a parsed element, if it's content unmarshal, fixup, and store return value
                JAXBElement e = (JAXBElement) child;
                if (e.getDeclaredType() == ContentType.class) {
                    retval = unmarshalEntryContent((ContentType) e.getValue(), contentType);
                }
            }
            else {
                // It's an arbitrary XML element. If it's an action, fix up element.
                Element e = (Element) child;
                if (qnameFromElement(e).equals(Constants.ODATA_ACTION_ELEMENT_NAME)) {
                    JAXBElement<ODataActionType> actionElement = mediaContentUnmarshaller.unmarshal(e,
                            ODataActionType.class);
                    entryChildren.set(i, actionElement);
                }
            }
        }
        return retval;
    }

    private EntryType getFirstEntry(FeedType feed) {
        for (Object child : feed.getFeedChildren()) {
            if (child instanceof JAXBElement) {
                JAXBElement e = (JAXBElement) child;

                if (e.getDeclaredType() == EntryType.class) {
                    return (EntryType) e.getValue();
                }
            }
        }
        return null;
    }

    private Object unmarshalEntryContent(ContentType content, Class<?> actualContentType) throws JAXBException {
        List<Object> contentChildren = content.getContent();
        for (int i = 0; i < contentChildren.size(); ++i) {
            Object child = contentChildren.get(i);
            if (child instanceof Element) {
                Element e = (Element) child;
                if (qnameFromElement(e).equals(Constants.ODATA_PROPERTIES_ELEMENT_NAME)) {
                    JAXBElement actualContentElement = mediaContentUnmarshaller.unmarshal(e, actualContentType);
                    contentChildren.set(i, actualContentElement);
                    return actualContentElement.getValue();
                }
            }
        }
        return null;
    }

    private QName qnameFromElement(Element e) {
        return new QName(e.getLocalName(), e.getNamespaceURI());
    }

}
