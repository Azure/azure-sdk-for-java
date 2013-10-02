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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import com.microsoft.windowsazure.services.media.models.ChannelSettings;

/**
 * The Class ChannelSettingsMapper.
 */
public class ChannelSettingsMapper {

    /** The mapper. */
    ObjectMapper mapper;

    /**
     * Instantiates a new channel settings mapper.
     */
    public ChannelSettingsMapper() {
        mapper = new ObjectMapper();
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * From string.
     * 
     * @param value
     *            the value
     * @return the channel settings
     * @throws IllegalArgumentException
     *             the illegal argument exception
     */
    public ChannelSettings fromString(String value) throws IllegalArgumentException {
        try {
            return mapper.readValue(value.getBytes("UTF-8"), ChannelSettings.class);
        }
        catch (JsonParseException e) {
            throw new IllegalArgumentException(e);
        }
        catch (JsonMappingException e) {
            throw new IllegalArgumentException(e);
        }
        catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * To string.
     * 
     * @param value
     *            the value
     * @return the string
     */
    public String toString(ChannelSettings value) {
        Writer writer = new StringWriter();
        try {
            mapper.writeValue(writer, value);
        }
        catch (JsonGenerationException e) {
            throw new RuntimeException(e);
        }
        catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return writer.toString();
    }

}
