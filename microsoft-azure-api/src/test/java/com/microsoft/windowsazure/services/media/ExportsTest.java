package com.microsoft.windowsazure.services.media;

import static org.junit.Assert.*;

import java.net.URI;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.RedirectFilter;
import com.microsoft.windowsazure.services.media.implementation.ResourceLocationManager;

/**
 * Tests to verify the items exported from media services
 * can be resolved from configuration.
 * 
 */
public class ExportsTest extends IntegrationTestBase {

    @Test
    public void canResolveLocationManagerFromConfig() throws Exception {
        ResourceLocationManager rlm = config.create(ResourceLocationManager.class);
        URI rootUri = new URI((String) config.getProperty(MediaConfiguration.URI));

        assertEquals(rootUri, rlm.getBaseURI());
    }

    @Test
    public void canResolveRedirectFilterFromConfig() throws Exception {
        RedirectFilter filter = config.create(RedirectFilter.class);
        assertNotNull(filter);
    }
}
