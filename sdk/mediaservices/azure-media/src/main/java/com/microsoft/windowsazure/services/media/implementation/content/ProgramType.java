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

package com.microsoft.windowsazure.services.media.implementation.content;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * This type maps the XML returned in the odata ATOM serialization for Asset
 * entities.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ProgramType implements MediaServiceDTO {

    /** The id. */
    @XmlElement(name = "Id", namespace = Constants.ODATA_DATA_NS)
    private String id;

    /** The name. */
    @XmlElement(name = "Name", namespace = Constants.ODATA_DATA_NS)
    private String name;

    /** The description. */
    @XmlElement(name = "Description", namespace = Constants.ODATA_DATA_NS)
    private String description;

    /** The created. */
    @XmlElement(name = "Created", namespace = Constants.ODATA_DATA_NS)
    private Date created;

    /** The last modified. */
    @XmlElement(name = "LastModified", namespace = Constants.ODATA_DATA_NS)
    private Date lastModified;

    /** The channel id. */
    @XmlElement(name = "ChannelId", namespace = Constants.ODATA_DATA_NS)
    private String channelId;

    /** The asset id. */
    @XmlElement(name = "AssetId", namespace = Constants.ODATA_DATA_NS)
    private String assetId;

    /** The dvr window length seconds. */
    @XmlElement(name = "DvrWindowLengthSeconds", namespace = Constants.ODATA_DATA_NS)
    private int dvrWindowLengthSeconds;

    /** The estimated duration seconds. */
    @XmlElement(name = "EstimatedDurationSeconds", namespace = Constants.ODATA_DATA_NS)
    private int estimatedDurationSeconds;

    /** The enable archive. */
    @XmlElement(name = "EnableArchive", namespace = Constants.ODATA_DATA_NS)
    private boolean enableArchive;

    /** The state. */
    @XmlElement(name = "State", namespace = Constants.ODATA_DATA_NS)
    private String state;

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets the id.
     * 
     * @param id
     *            the id
     * @return the channel type
     */
    public ProgramType setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * 
     * @param name
     *            the name to set
     * @return the channel type
     */
    public ProgramType setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the description.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     * 
     * @param description
     *            the description
     * @return the channel type
     */
    public ProgramType setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Gets the created.
     * 
     * @return the created
     */
    public Date getCreated() {
        return created;
    }

    /**
     * Sets the created.
     * 
     * @param created
     *            the created
     * @return the channel type
     */
    public ProgramType setCreated(Date created) {
        this.created = created;
        return this;
    }

    /**
     * Gets the last modified.
     * 
     * @return the lastModified
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Sets the last modified.
     * 
     * @param lastModified
     *            the last modified
     * @return the channel type
     */
    public ProgramType setLastModified(Date lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    /**
     * Sets the state.
     * 
     * @param state
     *            the state
     * @return the channel type
     */
    public ProgramType setState(String state) {
        this.state = state;
        return this;
    }

    /**
     * Gets the state.
     * 
     * @return the state
     */
    public String getState() {
        return this.state;
    }

    /**
     * Gets the channel id.
     * 
     * @return the channel id
     */
    public String getChannelId() {
        return this.channelId;
    }

    /**
     * Gets the asset id.
     * 
     * @return the asset id
     */
    public String getAssetId() {
        return this.assetId;
    }

    /**
     * Gets the dvr window length seconds.
     * 
     * @return the dvr window length seconds
     */
    public int getDvrWindowLengthSeconds() {
        return this.dvrWindowLengthSeconds;
    }

    /**
     * Gets the estimated duration seconds.
     * 
     * @return the estimated duration seconds
     */
    public int getEstimatedDurationSeconds() {
        return this.estimatedDurationSeconds;
    }

    /**
     * Checks if is enable archive.
     * 
     * @return true, if is enable archive
     */
    public boolean isEnableArchive() {
        return this.enableArchive;
    }

    /**
     * Sets the enable archive.
     * 
     * @param enableArchive
     *            the new enable archive
     */
    public void setEnableArchive(boolean enableArchive) {
        this.enableArchive = enableArchive;
    }

    /**
     * Sets the asset id.
     * 
     * @param assetId
     *            the new asset id
     */
    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    /**
     * Sets the channel id.
     * 
     * @param channelId
     *            the new channel id
     */
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    /**
     * Sets the estimated duration seconds.
     * 
     * @param estimatedDurationSeconds
     *            the new estimated duration seconds
     */
    public void setEstimatedDurationSeconds(int estimatedDurationSeconds) {
        this.estimatedDurationSeconds = estimatedDurationSeconds;
    }

    /**
     * Sets the dvr window length seconds.
     * 
     * @param dvrWindowLengthSeconds
     *            the new dvr window length seconds
     */
    public void setDvrWindowLengthSeconds(int dvrWindowLengthSeconds) {
        this.dvrWindowLengthSeconds = dvrWindowLengthSeconds;
    }
}
