package com.microsoft.windowsazure.services.media.implementation;

import java.net.URISyntaxException;

import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Test;

import com.microsoft.windowsazure.services.core.utils.DefaultDateFactory;
import com.microsoft.windowsazure.services.media.IntegrationTestBase;
import com.microsoft.windowsazure.services.media.MediaConfiguration;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.MediaService;
import com.microsoft.windowsazure.services.media.implementation.content.CreateAssetRequest;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.json.JSONConfiguration;

public class ODataSerializationFromJerseyTest extends IntegrationTestBase {

    @Test
    public void canBuildJerseyClientToCreateAnAssetWhichIsProperlyDeserialized() throws Exception {
        // Build a jersey client object by hand; this is working up to the
        // full integration into the media services rest proxy, but we
        // need to go step by step to begin.

        ClientConfig cc = new DefaultClientConfig();
        cc.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, false);
        cc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
        cc.getSingletons().add(new ODataEntityProvider());
        Client c = Client.create(cc);

        c.addFilter(new LoggingFilter(System.out));
        c.addFilter(new RedirectFilter(createLocationManager()));
        c.addFilter(new OAuthFilter(createTokenManager()));
        c.addFilter(new VersionHeadersFilter());

        WebResource assetResource = c.resource("Assets");

        CreateAssetRequest requestData = new CreateAssetRequest("firstTestAsset");

        AssetInfo newAsset = assetResource.type("application/json;odata=verbose")
                .accept(MediaType.APPLICATION_ATOM_XML).post(AssetInfo.class, requestData);

        Assert.assertNotNull(newAsset);
        Assert.assertEquals("firstTestAsset", newAsset.getName());
    }

    private OAuthContract createOAuthContract() {
        return new OAuthRestProxy(Client.create());
    }

    private OAuthTokenManager createTokenManager() throws URISyntaxException {
        return new OAuthTokenManager(createOAuthContract(), new DefaultDateFactory(),
                (String) config.getProperty(MediaConfiguration.OAUTH_URI),
                (String) config.getProperty(MediaConfiguration.OAUTH_CLIENT_ID),
                (String) config.getProperty(MediaConfiguration.OAUTH_CLIENT_SECRET), "urn:WindowsAzureMediaServices");
    }

    private ResourceLocationManager createLocationManager() throws URISyntaxException {
        return new ResourceLocationManager((String) config.getProperty(MediaConfiguration.URI));
    }

    @Test
    public void canCreateAssetThroughMediaServiceAPI() throws Exception {
        MediaContract client = MediaService.create(config);
        AssetInfo newAsset = client.createAsset("secondTestAsset");

        Assert.assertEquals("secondTestAsset", newAsset.getName());
    }
}
