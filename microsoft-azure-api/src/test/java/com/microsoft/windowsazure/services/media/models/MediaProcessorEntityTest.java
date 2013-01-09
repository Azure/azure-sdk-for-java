/**
 * Copyright 2013 Microsoft Corporation
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

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.entities.EntityListOperation;
import com.microsoft.windowsazure.services.media.models.MediaProcessor;
import com.microsoft.windowsazure.services.media.models.MediaProcessorInfo;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Tests for the MediaProcessor entity
 * 
 */
public class MediaProcessorEntityTest {

    @Test
    public void listMediaProcessorsReturnsExpectedUri() {
        assertEquals("MediaProcessors", MediaProcessor.list().getUri());
    }

    @Test
    public void listMediaProcessorsCanTakeQueryParmeters() {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("$top", "10");
        queryParams.add("$skip", "2");

        EntityListOperation<MediaProcessorInfo> lister = MediaProcessor.list(queryParams);

        assertEquals("10", lister.getQueryParameters().getFirst("$top"));
        assertEquals("2", lister.getQueryParameters().getFirst("$skip"));
        assertEquals(2, lister.getQueryParameters().size());
    }

}
