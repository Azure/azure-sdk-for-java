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

import java.util.Date;

import com.microsoft.windowsazure.services.media.implementation.ODataEntity;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.content.ProgramType;

/**
 * Data about a Media Services Live Streaming Program entity.
 * 
 */
public class ProgramInfo extends ODataEntity<ProgramType> {

    /**
     * Instantiates a new program info instance.
     * 
     * @param entry
     *            the entry
     * @param content
     *            the content
     */
    public ProgramInfo(EntryType entry, ProgramType content) {
        super(entry, content);
    }

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public String getId() {
        return this.getContent().getId();
    }

    /**
     * Get the program name.
     * 
     * @return the name
     */
    public String getName() {
        return this.getContent().getName();
    }

    /**
     * Get the program description.
     * 
     * @return the description
     */
    public String getDescription() {
        return this.getContent().getDescription();
    }

    /**
     * Get the creation date.
     * 
     * @return the date
     */
    public Date getCreated() {
        return this.getContent().getCreated();
    }

    /**
     * Get last modified date.
     * 
     * @return the date
     */
    public Date getLastModified() {
        return getContent().getLastModified();
    }

    /**
     * Gets the channel id.
     * 
     * @return the channel id
     */
    public String getChannelId() {
        return this.getContent().getChannelId();
    }

    /**
     * Gets the asset id.
     * 
     * @return the asset id
     */
    public String getAssetId() {
        return this.getContent().getAssetId();
    }

    /**
     * Gets the dvr window length seconds.
     * 
     * @return the dvr window length seconds
     */
    public int getDvrWindowLengthSeconds() {
        return this.getContent().getDvrWindowLengthSeconds();
    }

    /**
     * Gets the estimated duration seconds.
     * 
     * @return the estimated duration seconds
     */
    public int getEstimatedDurationSeconds() {
        return this.getContent().getEstimatedDurationSeconds();
    }

    /**
     * Checks if is enable archive.
     * 
     * @return true, if is enable archive
     */
    public boolean isEnableArchive() {
        return this.getContent().isEnableArchive();
    }

    /**
     * Get the program state.
     * 
     * @return the state
     */
    public ProgramState getState() {
        return ProgramState.valueOf(getContent().getState());
    }

}
