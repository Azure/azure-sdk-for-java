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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.xml.bind.JAXBElement;

import com.microsoft.windowsazure.services.media.implementation.atom.ContentType;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.atom.LinkType;
import com.microsoft.windowsazure.services.media.implementation.content.Constants;
import com.microsoft.windowsazure.services.media.models.LinkInfo;
import com.microsoft.windowsazure.services.media.models.ListResult;

/**
 * Class wrapping deserialized OData entities. Allows easy access to entry and
 * content types.
 * 
 */
public abstract class ODataEntity<T> {

    private final EntryType entry;
    private final T content;

    protected ODataEntity(EntryType entry, T content) {
        this.entry = entry;
        this.content = content;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected ODataEntity(T content) {
        this.content = content;

        ContentType contentElement = new ContentType();
        contentElement.getContent().add(
                new JAXBElement(Constants.ODATA_PROPERTIES_ELEMENT_NAME,
                        content.getClass(), content));
        entry = new EntryType();
        entry.getEntryChildren().add(
                new JAXBElement(Constants.ATOM_CONTENT_ELEMENT_NAME,
                        ContentType.class, contentElement));
    }

    /**
     * @return the entry
     */
    protected EntryType getEntry() {
        return entry;
    }

    /**
     * @return the content
     */
    protected T getContent() {
        return content;
    }

    /**
     * Test if the entity contains a link with the given rel attribute.
     * 
     * @param rel
     *            Rel of link to check for
     * @return True if link is found, false if not.
     */
    public boolean hasLink(String rel) {
        return getLink(rel) != null;
    }

    /**
     * Get the link with the given rel attribute
     * 
     * @param rel
     *            rel of link to retrieve
     * @return The link if found, null if not.
     */
    public <U extends ODataEntity<?>> LinkInfo<U> getLink(String rel) {
        for (Object child : entry.getEntryChildren()) {

            LinkType link = linkFromChild(child);
            if (link != null && link.getRel().equals(rel)) {
                return new LinkInfo<U>(link);
            }
        }
        return null;
    }

    /**
     * Get the link to navigate an OData relationship
     * 
     * @param relationName
     *            name of the OData relationship
     * @return the link if found, null if not.
     */
    public <U extends ODataEntity<?>> LinkInfo<U> getRelationLink(
            String relationName) {
        return this.<U> getLink(Constants.ODATA_DATA_NS + "/related/"
                + relationName);
    }

    @SuppressWarnings("rawtypes")
    private static LinkType linkFromChild(Object child) {
        if (child instanceof JAXBElement) {
            return linkFromElement((JAXBElement) child);
        }
        return null;
    }

    private static LinkType linkFromElement(
            @SuppressWarnings("rawtypes") JAXBElement element) {
        if (element.getDeclaredType() == LinkType.class) {
            return (LinkType) element.getValue();
        }
        return null;
    }

    /**
     * Is the given type inherited from ODataEntity
     * 
     * @param type
     *            Type to check
     * @return true if derived from ODataEntity
     */
    public static boolean isODataEntityType(Class<?> type) {
        return ODataEntity.class.isAssignableFrom(type);
    }

    /**
     * Is the given type a collection of ODataEntity
     * 
     * @param type
     *            Base type
     * @param genericType
     *            Generic type
     * @return true if it's List&lt;OEntity> or derive from.
     */
    public static boolean isODataEntityCollectionType(Class<?> type,
            Type genericType) {
        if (ListResult.class != type) {
            return false;
        }

        ParameterizedType pt = (ParameterizedType) genericType;

        if (pt.getActualTypeArguments().length != 1) {
            return false;
        }

        Class<?> typeClass = getCollectedType(genericType);

        return isODataEntityType(typeClass);
    }

    /**
     * Reflection helper to pull out the type parameter for a List<T>, where T
     * is a ODataEntity<?> derived type.
     * 
     * @param genericType
     *            type object for collection
     * @return The class object for the type parameter.
     */
    public static Class<?> getCollectedType(Type genericType) {
        ParameterizedType pt = (ParameterizedType) genericType;
        if (pt.getActualTypeArguments().length != 1) {
            throw new IllegalArgumentException("genericType");
        }
        return (Class<?>) pt.getActualTypeArguments()[0];
    }
}
