package com.microsoft.windowsazure.services.media;

import static org.junit.Assert.*;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.entities.Asset;

public class EntityProxyTest extends IntegrationTestBase {

    @Test
    public void canCreateEntityProxy() {
        MediaEntityContract proxy = config.create(MediaEntityContract.class);

        assertNotNull(proxy);
    }

    @Test
    public void canCreateDefaultAssetEntity() throws Exception {

        MediaEntityContract proxy = config.create(MediaEntityContract.class);

        Asset.Info asset = proxy.create(Asset.create());

        assertNotNull(asset.getId());
    }
}
