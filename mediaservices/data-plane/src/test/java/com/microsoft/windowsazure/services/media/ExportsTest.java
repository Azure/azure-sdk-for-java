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

package com.microsoft.windowsazure.services.media;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.RedirectFilter;
import com.microsoft.windowsazure.services.media.implementation.ResourceLocationManager;

/**
 * Tests to verify the items exported from media services can be resolved from
 * configuration.
 * 
 */
public class ExportsTest extends IntegrationTestBase {

    @Test
    public void canResolveLocationManagerFromConfig() throws Exception {
        ResourceLocationManager rlm = config
                .create(ResourceLocationManager.class);
        URI rootUri = new URI(
                (String) config.getProperty(MediaConfiguration.AZURE_AD_API_SERVER));

        assertEquals(rootUri, rlm.getBaseURI());
    }

    @Test
    public void canResolveRedirectFilterFromConfig() throws Exception {
        RedirectFilter filter = config.create(RedirectFilter.class);
        assertNotNull(filter);
    }
}
