package com.microsoft.windowsazure.services.media.implementation;

import static org.junit.Assert.*;

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
    public void testWhenCallingGetRedirectedURI_shouldReturnURIWithBaseURIPreprended() throws Exception {
        String baseURI = "http://base.uri.example/path/";
        ResourceLocationManager m = new ResourceLocationManager(baseURI);

        URI originalURI = new URI("Assets");

        URI redirectedURI = m.getRedirectedURI(originalURI);

        assertEquals(baseURI + "Assets", redirectedURI.toString());
    }

    @Test
    public void settingBaseURIAfterRedirecting_shouldReturnURIWithNewBaseURI() throws Exception {
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
