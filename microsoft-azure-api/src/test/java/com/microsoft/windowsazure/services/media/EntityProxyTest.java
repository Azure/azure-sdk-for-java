package com.microsoft.windowsazure.services.media;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.services.media.entities.Asset;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.sun.jersey.core.util.MultivaluedMapImpl;

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

    @Test
    public void canListAllAssets() throws Exception {
        int numAssetsToCreate = 4;
        Set<String> expectedAssets = new HashSet<String>();

        for (int i = 0; i < numAssetsToCreate; ++i) {
            AssetInfo asset = entityService.create(Asset.create().name(
                    testAssetPrefix + "canList" + Integer.toString(i)));
            expectedAssets.add(asset.getId());
        }

        ListResult<AssetInfo> assets = entityService.list(Asset.list());

        assertTrue(assets.size() >= numAssetsToCreate);

        for (AssetInfo asset : assets) {
            if (expectedAssets.contains(asset.getId())) {
                expectedAssets.remove(asset.getId());
            }
        }
        assertEquals(0, expectedAssets.size());
    }

    @Test
    public void canListAssetsWithQueryParameters() throws Exception {
        int numAssetsToCreate = 4;
        Set<String> expectedAssets = new HashSet<String>();

        for (int i = 0; i < numAssetsToCreate; ++i) {
            AssetInfo asset = entityService.create(Asset.create().name(
                    testAssetPrefix + "withQuery" + Integer.toString(i)));
            expectedAssets.add(asset.getId());
        }

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("$top", "2");

        ListResult<AssetInfo> assets = entityService.list(Asset.list(params));

        assertEquals(2, assets.size());
    }

    @Test
    public void canUpdateAssetNameAndAltId() throws Exception {
        String newName = testAssetPrefix + "newName";
        String newAltId = "updated alt id";

        AssetInfo initialAsset = entityService.create(Asset.create().name(testAssetPrefix + "originalName"));

        entityService.update(Asset.update(initialAsset.getId()).name(newName).alternateId(newAltId));

        AssetInfo updatedAsset = entityService.get(Asset.get(initialAsset.getId()));

        assertEquals(initialAsset.getId(), updatedAsset.getId());
        assertEquals(newName, updatedAsset.getName());
        assertEquals(newAltId, updatedAsset.getAlternateId());
    }
}
