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

package com.microsoft.windowsazure.services.media.implementation.content;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * This type maps the XML returned in the odata ATOM serialization
 * for Asset entities.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class AssetType {

    @XmlElement(name = "Id", namespace = Constants.ODATA_DATA_NS)
    protected String id;

    @XmlElement(name = "State", namespace = Constants.ODATA_DATA_NS)
    protected int state;

    @XmlElement(name = "Created", namespace = Constants.ODATA_DATA_NS)
    protected XMLGregorianCalendar created;

    @XmlElement(name = "LastModified", namespace = Constants.ODATA_DATA_NS)
    protected XMLGregorianCalendar lastModified;

    @XmlElement(name = "AlternateId", namespace = Constants.ODATA_DATA_NS)
    protected String alternateId;

    @XmlElement(name = "Name", namespace = Constants.ODATA_DATA_NS)
    protected String name;

    @XmlElement(name = "Options", namespace = Constants.ODATA_DATA_NS)
    protected int options;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the state
     */
    public int getState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setState(int state) {
        this.state = state;
    }

    /**
     * @return the created
     */
    public XMLGregorianCalendar getCreated() {
        return created;
    }

    /**
     * @param created
     *            the created to set
     */
    public void setCreated(XMLGregorianCalendar created) {
        this.created = created;
    }

    /**
     * @return the lastModified
     */
    public XMLGregorianCalendar getLastModified() {
        return lastModified;
    }

    /**
     * @param lastModified
     *            the lastModified to set
     */
    public void setLastModified(XMLGregorianCalendar lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * @return the alternateId
     */
    public String getAlternateId() {
        return alternateId;
    }

    /**
     * @param alternateId
     *            the alternateId to set
     */
    public void setAlternateId(String alternateId) {
        this.alternateId = alternateId;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the options
     */
    public int getOptions() {
        return options;
    }

    /**
     * @param options
     *            the options to set
     */
    public void setOptions(int options) {
        this.options = options;
    }
}
