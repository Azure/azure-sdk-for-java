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

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Element;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.implementation.atom.ContentType;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.atom.FeedType;
import com.microsoft.windowsazure.services.media.implementation.content.AssetType;
import com.microsoft.windowsazure.services.media.implementation.content.Constants;
import com.microsoft.windowsazure.services.media.implementation.content.ODataActionType;
import com.microsoft.windowsazure.services.media.models.ListResult;

/**
 * This class implements unmarshalling from OData over Atom into Java classes.
 * 
 */
public class ODataAtomUnmarshaller {
    private final JAXBContext atomContext;
    private final JAXBContext mediaContentContext;
    private final Unmarshaller atomUnmarshaller;
    private final Unmarshaller mediaContentUnmarshaller;

    /**
     * @throws JAXBException
     */
    public ODataAtomUnmarshaller() throws JAXBException {
        atomContext = JAXBContext.newInstance(FeedType.class.getPackage()
                .getName());
        atomUnmarshaller = atomContext.createUnmarshaller();
        mediaContentContext = JAXBContext.newInstance(AssetType.class
                .getPackage().getName());
        mediaContentUnmarshaller = mediaContentContext.createUnmarshaller();
    }

    /**
     * Given a stream that contains XML with an atom Feed element at the root,
     * unmarshal it into Java objects with the given content type in the
     * entries.
     * 
     * @param <T>
     * 
     * @param stream
     *            - stream containing the XML data
     * @param contentType
     *            - Java type to unmarshal the entry contents into
     * @return an instance of contentType that contains the unmarshalled data.
     * @throws JAXBException
     * @throws ServiceException
     */
    @SuppressWarnings("rawtypes")
    public <T extends ODataEntity> ListResult<T> unmarshalFeed(
            InputStream stream, Class<T> contentType) throws JAXBException,
            ServiceException {
        validateNotNull(stream, "stream");
        validateNotNull(contentType, "contentType");

        List<T> entries = new ArrayList<T>();
        FeedType feed = unmarshalFeed(stream);
        Class<?> marshallingContentType = getMarshallingContentType(contentType);

        for (Object feedChild : feed.getFeedChildren()) {
            EntryType entry = asEntry(feedChild);
            if (entry != null) {
                entries.add(contentFromEntry(contentType,
                        marshallingContentType, entry));
            }
        }
        return new ListResult<T>(entries);
    }

    /**
     * Given a stream containing XML with an Atom entry element at the root,
     * unmarshal it into an instance of contentType
     * 
     * @param stream
     *            - stream containing XML data
     * @param contentType
     *            - type of object to return
     * @return An instance of contentType
     * @throws JAXBException
     * @throws ServiceException
     */
    @SuppressWarnings("rawtypes")
    public <T extends ODataEntity> T unmarshalEntry(InputStream stream,
            Class<T> contentType) throws JAXBException, ServiceException {
        validateNotNull(stream, "stream");
        validateNotNull(contentType, "contentType");

        Class<?> marshallingContentType = getMarshallingContentType(contentType);

        EntryType entry = unmarshalEntry(stream);
        return contentFromEntry(contentType, marshallingContentType, entry);
    }

    @SuppressWarnings("rawtypes")
    private <T extends ODataEntity> T contentFromEntry(Class<T> contentType,
            Class<?> marshallingContentType, EntryType entry)
            throws JAXBException, ServiceException {
        unmarshalODataContent(entry, contentType);
        ContentType contentElement = getFirstOfType(ContentType.class,
                entry.getEntryChildren());
        Object contentObject = getFirstOfType(marshallingContentType,
                contentElement.getContent());
        return constructResultObject(contentType, entry, contentObject);
    }

    private EntryType asEntry(Object o) {
        if (o instanceof JAXBElement) {
            @SuppressWarnings("rawtypes")
            JAXBElement e = (JAXBElement) o;
            if (e.getDeclaredType() == EntryType.class) {
                return (EntryType) e.getValue();
            }
        }
        return null;
    }

    private void unmarshalODataContent(EntryType entry, Class<?> contentType)
            throws JAXBException {
        unmarshalEntryActions(entry);
        unmarshalEntryContent(entry, contentType);
    }

    private void unmarshalEntryActions(EntryType entry) throws JAXBException {
        List<Object> children = entry.getEntryChildren();
        for (int i = 0; i < children.size(); ++i) {
            Object child = children.get(i);
            if (child instanceof Element) {
                Element e = (Element) child;
                if (qnameFromElement(e).equals(
                        Constants.ODATA_ACTION_ELEMENT_NAME)) {
                    JAXBElement<ODataActionType> actionElement = mediaContentUnmarshaller
                            .unmarshal(e, ODataActionType.class);
                    children.set(i, actionElement);
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void unmarshalEntryContent(EntryType entry, Class<?> contentType)
            throws JAXBException {
        Class<?> marshallingContentType = getMarshallingContentType(contentType);
        ContentType contentElement = getFirstOfType(ContentType.class,
                entry.getEntryChildren());
        List<Object> contentChildren = contentElement.getContent();
        for (int i = 0; i < contentChildren.size(); ++i) {
            Object child = contentChildren.get(i);
            if (child instanceof Element) {
                Element e = (Element) child;
                if (qnameFromElement(e).equals(
                        Constants.ODATA_PROPERTIES_ELEMENT_NAME)) {
                    JAXBElement actualContentElement = mediaContentUnmarshaller
                            .unmarshal(e, marshallingContentType);
                    contentChildren.set(i, actualContentElement);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getFirstOfType(Class<T> targetType, List<Object> collection) {
        for (Object c : collection) {
            if (c instanceof JAXBElement) {
                @SuppressWarnings("rawtypes")
                JAXBElement e = (JAXBElement) c;
                if (e.getDeclaredType() == targetType) {
                    return (T) e.getValue();
                }
            }
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    private <T extends ODataEntity> T constructResultObject(
            Class<T> contentType, EntryType entry, Object contentObject)
            throws ServiceException {
        Class<?> marshallingType = getMarshallingContentType(contentType);
        try {
            Constructor<T> resultCtor = contentType.getConstructor(
                    EntryType.class, marshallingType);
            return resultCtor.newInstance(entry, contentObject);
        } catch (IllegalArgumentException e) {
            throw new ServiceException(e);
        } catch (SecurityException e) {
            throw new ServiceException(e);
        } catch (InstantiationException e) {
            throw new ServiceException(e);
        } catch (IllegalAccessException e) {
            throw new ServiceException(e);
        } catch (InvocationTargetException e) {
            throw new ServiceException(e);
        } catch (NoSuchMethodException e) {
            throw new ServiceException(e);
        }
    }

    public EntryType unmarshalEntry(InputStream stream) throws JAXBException {
        JAXBElement<EntryType> entryElement = atomUnmarshaller.unmarshal(
                new StreamSource(stream), EntryType.class);
        return entryElement.getValue();
    }

    private FeedType unmarshalFeed(InputStream stream) throws JAXBException {
        JAXBElement<FeedType> feedElement = atomUnmarshaller.unmarshal(
                new StreamSource(stream), FeedType.class);
        return feedElement.getValue();
    }

    private static QName qnameFromElement(Element e) {
        return new QName(e.getLocalName(), e.getNamespaceURI());
    }

    private static Class<?> getMarshallingContentType(Class<?> contentType) {
        ParameterizedType pt = (ParameterizedType) contentType
                .getGenericSuperclass();
        return (Class<?>) pt.getActualTypeArguments()[0];
    }

    private static void validateNotNull(Object param, String paramName) {
        if (param == null) {
            throw new IllegalArgumentException(paramName);
        }
    }
}
