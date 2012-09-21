/**
 * Copyright 2012 Microsoft Corporation
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

import javax.xml.datatype.XMLGregorianCalendar;

import com.microsoft.windowsazure.services.media.implementation.ODataEntity;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.content.AssetType;

/**
 * Data about a Media Services Asset entity.
 * 
 */
public class AssetInfo extends ODataEntity<AssetType> {

    public AssetInfo(EntryType entry, AssetType content) {
        super(entry, content);
    }

    /**
     * Get the asset id
     * 
     * @return the id
     */
    public String getId() {
        return getContent().getId();
    }

    /**
     * Set the id
     * 
     * @param id
     *            the id
     */
    public void setId(String id) {
        getContent().setId(id);
    }

    /**
     * Get the asset name
     * 
     * @return the name
     */
    public String getName() {
        return this.getContent().getName();
    }

    /**
     * set the name
     * 
     * @param name
     *            the name
     */
    public void setName(String name) {
        this.getContent().setName(name);
    }

    /**
     * Get the asset state
     * 
     * @return the state
     */
    public int getState() {
        return getContent().getState();
    }

    /**
     * Set the state
     * 
     * @param state
     *            the state
     */
    public void setState(int state) {
        getContent().setState(state);
    }

    /**
     * Get the creation date
     * 
     * @return the date
     */
    public XMLGregorianCalendar getCreated() {
        return this.getContent().getCreated();
    }

    /**
     * Set creation date
     * 
     * @param created
     *            the date
     * 
     */
    public void setCreated(XMLGregorianCalendar created) {
        getContent().setCreated(created);
    }

    /**
     * Get last modified date
     * 
     * @return the date
     */
    public XMLGregorianCalendar getLastModified() {
        return getContent().getLastModified();
    }

    /**
     * Set last modified date
     * 
     * @param lastModified
     *            the date
     */
    public void setLastModified(XMLGregorianCalendar lastModified) {
        getContent().setLastModified(lastModified);
    }

    /**
     * Get the alternate id
     * 
     * @return the id
     */
    public String getAlternateId() {
        return getContent().getAlternateId();
    }

    /**
     * Set the alternate id
     * 
     * @param alternateId
     *            the id
     */
    public void setAlternateId(String alternateId) {
        getContent().setAlternateId(alternateId);
    }

    /**
     * Get the options
     * 
     * @return the options
     */
    public int getOptions() {
        return getContent().getOptions();
    }

    /**
     * Set the options
     * 
     * @param options
     *            the options
     */
    public void setOptions(int options) {
        getContent().setOptions(options);
    }
}
