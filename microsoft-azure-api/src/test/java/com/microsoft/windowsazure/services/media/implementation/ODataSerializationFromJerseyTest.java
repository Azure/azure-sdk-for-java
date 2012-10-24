/**
 * Copyright 2012 Microsoft Corporation
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

import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Test;

import com.microsoft.windowsazure.services.core.utils.DefaultDateFactory;
import com.microsoft.windowsazure.services.media.IntegrationTestBase;
import com.microsoft.windowsazure.services.media.MediaConfiguration;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.MediaService;
import com.microsoft.windowsazure.services.media.implementation.content.AssetType;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.CreateAssetOptions;
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

        ODataAtomMarshaller m = new ODataAtomMarshaller();
        AssetType requestData = new AssetType();
        requestData.setName("firstTestAsset");
        requestData.setAlternateId("some external id");

        AssetInfo newAsset = assetResource.type(MediaType.APPLICATION_ATOM_XML).accept(MediaType.APPLICATION_ATOM_XML)
                .post(AssetInfo.class, m.marshalEntry(requestData));

        Assert.assertNotNull(newAsset);
        Assert.assertEquals("firstTestAsset", newAsset.getName());
        Assert.assertEquals("some external id", newAsset.getAlternateId());
    }

    private OAuthContract createOAuthContract() {
        return new OAuthRestProxy(Client.create());
    }

    private OAuthTokenManager createTokenManager() throws URISyntaxException {
        return new OAuthTokenManager(createOAuthContract(), new DefaultDateFactory(),
                (String) config.getProperty(MediaConfiguration.OAUTH_URI),
                (String) config.getProperty(MediaConfiguration.OAUTH_CLIENT_ID),
                (String) config.getProperty(MediaConfiguration.OAUTH_CLIENT_SECRET),
                (String) config.getProperty(MediaConfiguration.OAUTH_SCOPE));
    }

    private ResourceLocationManager createLocationManager() throws URISyntaxException {
        return new ResourceLocationManager((String) config.getProperty(MediaConfiguration.URI));
    }

    @Test
    public void canCreateAssetThroughMediaServiceAPI() throws Exception {
        MediaContract client = MediaService.create(config);
        CreateAssetOptions createAssetOptions = new CreateAssetOptions().setName("secondTestAsset");
        AssetInfo newAsset = client.createAsset(createAssetOptions);

        Assert.assertEquals("secondTestAsset", newAsset.getName());
    }

    @Test
    public void canRetrieveListOfAssets() throws Exception {
        MediaContract client = MediaService.create(config);
        List<AssetInfo> assets = client.listAssets();

        Assert.assertNotNull(assets);
    }

}
