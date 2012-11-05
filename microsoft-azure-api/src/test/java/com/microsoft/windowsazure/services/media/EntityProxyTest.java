package com.microsoft.windowsazure.services.media;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.services.media.entities.Asset;
import com.microsoft.windowsazure.services.media.models.AssetInfo;

public class EntityProxyTest extends IntegrationTestBase {
    private static MediaEntityContract entityService;

    @BeforeClass
    public static void entityProxyTestSetup() {
        entityService = config.create(MediaEntityContract.class);
    }

    @Test
    public void canCreateEntityProxy() {
        assertNotNull(entityService);
    }

    @Test
    public void canCreateDefaultAssetEntity() throws Exception {

        AssetInfo asset = entityService.create(Asset.create());

        assertNotNull(asset.getId());
    }

    @Test
    public void canCreateAssetOnServerWithNameAndAltId() throws Exception {
        String name = testAssetPrefix + "AName";
        String altId = "unit test alt id";

        AssetInfo asset = entityService.create(Asset.create().name(name).alternateId(altId));

        assertNotNull(asset.getId());
        assertEquals(name, asset.getName());
        assertEquals(altId, asset.getAlternateId());
    }

    @Test
    public void canRetrieveAssetById() throws Exception {
        AssetInfo createdAsset = entityService.create(Asset.create().name(testAssetPrefix + "canRetrieveAssetById"));

        AssetInfo retrieved = entityService.get(Asset.get(createdAsset.getId()));

        assertEquals(createdAsset.getId(), retrieved.getId());
        assertEquals(createdAsset.getName(), retrieved.getName());

    }
}
