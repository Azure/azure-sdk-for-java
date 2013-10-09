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
public class ProgramInfo extends ODataEntity<ChannelType> {

    /** The object mapper. */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** The type reference. */
    private final TypeReference<ChannelSettings> typeReference = new TypeReference<ChannelSettings>() {
    };

    /**
     * Instantiates a new channel info instance.
     * 
     * @param entry
     *            the entry
     * @param content
     *            the content
     */
    public ProgramInfo(EntryType entry, ChannelType content) {
        super(entry, content);
    }

    public String getId() {
        return this.getContent().getId();
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
        return ChannelState.valueOf(getContent().getState());
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
     * Gets the size.
     * 
     * @return the size
     */
    public ChannelSize getSize() {
        return ChannelSize.valueOf(getContent().getSize());
    }

    /**
     * Gets the preview uri.
     * 
     * @return the preview uri
     */
    public URI getPreviewUri() {
        return getContent().getPreviewUri();
    }

    /**
     * Gets the ingest uri.
     * 
     * @return the ingest uri
     */
    public URI getIngestUri() {
        return getContent().getIngestUri();
    }

    /**
     * Gets the settings.
     * 
     * @return the settings
     */
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
