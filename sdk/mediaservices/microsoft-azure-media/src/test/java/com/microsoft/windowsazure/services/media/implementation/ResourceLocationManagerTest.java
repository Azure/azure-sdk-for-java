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

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.Test;

public class ResourceLocationManagerTest {

    @Test
    public void testCanCreateWithBaseUri() throws Exception {
        String baseUri = "https://base.uri.example";
        ResourceLocationManager m = new ResourceLocationManager(baseUri);

        assertEquals(baseUri, m.getBaseURI().toString());
    }

    @Test
    public void testWhenCallingGetRedirectedURI_shouldReturnURIWithBaseURIPreprended()
            throws Exception {
        String baseURI = "http://base.uri.example/path/";
        ResourceLocationManager m = new ResourceLocationManager(baseURI);

        URI originalURI = new URI("Assets");

        URI redirectedURI = m.getRedirectedURI(originalURI);

        assertEquals(baseURI + "Assets", redirectedURI.toString());
    }

    @Test
    public void settingBaseURIAfterRedirecting_shouldReturnURIWithNewBaseURI()
            throws Exception {
        String baseURI = "http://base.uri.example/path/";
        String redirectedBaseURI = "http://other.uri.example/API/";
        ResourceLocationManager m = new ResourceLocationManager(baseURI);

        URI targetURI = new URI("Assets");

        URI originalURI = m.getRedirectedURI(targetURI);
        m.setRedirectedURI(redirectedBaseURI);
        URI redirectedURI = m.getRedirectedURI(targetURI);

        assertEquals(baseURI + "Assets", originalURI.toString());
        assertEquals(redirectedBaseURI + "Assets", redirectedURI.toString());
    }
}
