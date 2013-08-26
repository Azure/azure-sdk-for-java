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

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.util.Date;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.content.ChannelType;

public class ChannelInfoTest {

    @Test
    public void testGetSetName() {
        // Arrange
        String expectedName = "expectedName";
        ChannelInfo channelInfo = new ChannelInfo(null, new ChannelType().setName(expectedName));

        // Act 
        String actualName = channelInfo.getName();

        // Assert
        assertEquals(expectedName, actualName);

    }

    @Test
    public void testGetSetDescription() {
        // Arrange
        String expectedDescription = "expectedDescription";
        ChannelInfo channelInfo = new ChannelInfo(null, new ChannelType().setName(expectedDescription));

        // Act 
        String actualDescription = channelInfo.getDescription();

        // Assert
        assertEquals(expectedDescription, actualDescription);
    }

    @Test
    public void testGetSetCreated() throws Exception {
        // Arrange
        Date expectedCreated = new Date();
        ChannelInfo channelInfo = new ChannelInfo(null, new ChannelType().setCreated(expectedCreated));

        // Act 
        Date actualCreated = channelInfo.getCreated();

        // Assert
        assertEquals(expectedCreated, actualCreated);

    }

    @Test
    public void testGetSetLastModified() throws Exception {
        // Arrange
        Date expectedLastModified = new Date();
        ChannelInfo channelInfo = new ChannelInfo(null, new ChannelType().setLastModified(expectedLastModified));

        // Act
        Date actualLastModified = channelInfo.getLastModified();

        // Assert
        assertEquals(expectedLastModified, actualLastModified);
    }

    @Test
    public void testGetSetPreviewUri() {
        // Arrange
        URI expectedPreviewUri = URI.create("http://www.contoso.com/preview");
        ChannelInfo channelInfo = new ChannelInfo(null, new ChannelType().setPreviewUri(expectedPreviewUri));

        // Act
        URI actualPreviewUri = channelInfo.getPreviewUri();

        // Assert
        assertEquals(expectedPreviewUri, actualPreviewUri);
    }

    @Test
    public void testGetSetIngestUri() {
        // Arrange
        URI expectedIngestUri = URI.create("http://www.contoso.com/ingest");
        ChannelInfo channelInfo = new ChannelInfo(null, new ChannelType().setIngestUri(expectedIngestUri));

        // Act
        URI actualPreviewUri = channelInfo.getIngestUri();

        // Assert
        assertEquals(expectedIngestUri, actualPreviewUri);
    }

    @Test
    public void testGetSetState() {
        // Arrange
        ChannelState expectedState = ChannelState.Stopped;
        ChannelInfo channelInfo = new ChannelInfo(null, new ChannelType().setState(expectedState.toString()));

        // Act
        ChannelState actualState = channelInfo.getState();

        // Assert
        assertEquals(expectedState, actualState);
    }

    @Test
    public void testGetSetSize() {
        // Arrange
        ChannelSize expectedSize = ChannelSize.Medium;
        ChannelInfo channelInfo = new ChannelInfo(null, new ChannelType().setSize(expectedSize.toString()));

        // Act
        ChannelSize actualSize = channelInfo.getSize();

        // Assert
        assertEquals(expectedSize, actualSize);
    }

    @Test
    public void testGetSetSettings() throws JsonGenerationException, JsonMappingException, IOException {
        // Arrange
        ChannelSettings expectedSettings = new ChannelSettings();
        ObjectMapper objectMapper = new ObjectMapper();

        String expectedSettingsJson = objectMapper.writeValueAsString(expectedSettings);
        ChannelInfo channelInfo = new ChannelInfo(null, new ChannelType().setSettings(expectedSettingsJson));

        // Act
        ChannelSettings actualSettings = channelInfo.getSettings();

        // Assert
        assertEquals(expectedSettings, actualSettings);

    }

}
