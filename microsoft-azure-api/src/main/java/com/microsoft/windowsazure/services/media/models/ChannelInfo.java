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

import java.io.IOException;
import java.net.URI;
import java.util.Date;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.microsoft.windowsazure.services.media.implementation.ODataEntity;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.content.ChannelType;

/**
 * Data about a Media Services Live Streaming Channel entity.
 * 
 */
public class ChannelInfo extends ODataEntity<ChannelType> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TypeReference<ChannelSettings> typeReference = new TypeReference<ChannelSettings>() {
    };

    /**
     * Instantiates a new channel info.
     * 
     * @param entry
     *            the entry
     * @param content
     *            the content
     */
    public ChannelInfo(EntryType entry, ChannelType content) {
        super(entry, content);
    }

    /**
     * Get the channel name.
     * 
     * @return the name
     */
    public String getName() {
        return this.getContent().getName();
    }

    /**
     * Get the channel description.
     * 
     * @return the description
     */
    public String getDescription() {
        return this.getContent().getDescription();
    }

    /**
     * Get the asset state.
     * 
     * @return the state
     */
    public ChannelState getState() {
        return ChannelState.fromCode(getContent().getState());
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

    public ChannelSize getSize() {
        return ChannelSize.fromCode(getContent().getSize());
    }

    public URI getPreviewUri() {
        return getContent().getPreviewUri();
    }

    public URI getIngestUri() {
        return getContent().getIngestUri();
    }

    public ChannelSettings getSettings() {

        try {
            return objectMapper.readValue(getContent().getSettings(), typeReference);
        }
        catch (JsonParseException e) {
            return null;
        }
        catch (JsonMappingException e) {
            return null;
        }
        catch (IOException e) {
            return null;
        }
    }
}
