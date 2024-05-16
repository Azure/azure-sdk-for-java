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
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.services.media.models.Asset;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.ListResult;

public class EntityProxyTest extends IntegrationTestBase {
    private static MediaContract entityService;

    @BeforeClass
    public static void entityProxyTestSetup() {
        entityService = config.create(MediaContract.class);
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

        AssetInfo asset = entityService.create(Asset.create().setName(name)
                .setAlternateId(altId));

        assertNotNull(asset.getId());
        assertEquals(name, asset.getName());
        assertEquals(altId, asset.getAlternateId());
    }

    @Test
    public void canRetrieveAssetById() throws Exception {
        AssetInfo createdAsset = entityService.create(Asset.create().setName(
                testAssetPrefix + "canRetrieveAssetById"));

        AssetInfo retrieved = entityService
                .get(Asset.get(createdAsset.getId()));

        assertEquals(createdAsset.getId(), retrieved.getId());
        assertEquals(createdAsset.getName(), retrieved.getName());

    }

    @Test
    public void canListAllAssets() throws Exception {
        int numAssetsToCreate = 4;
        Set<String> expectedAssets = createTestAssets(numAssetsToCreate,
                "canList");

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
        createTestAssets(4, "withQuery");

        ListResult<AssetInfo> assets = entityService.list(Asset.list()
                .setTop(2));

        assertEquals(2, assets.size());
    }

    @Test
    public void canUpdateAssetNameAndAltId() throws Exception {
        String newName = testAssetPrefix + "newName";
        String newAltId = "updated alt id";

        AssetInfo initialAsset = entityService.create(Asset.create().setName(
                testAssetPrefix + "originalName"));

        entityService.update(Asset.update(initialAsset.getId())
                .setName(newName).setAlternateId(newAltId));

        AssetInfo updatedAsset = entityService.get(Asset.get(initialAsset
                .getId()));

        assertEquals(initialAsset.getId(), updatedAsset.getId());
        assertEquals(newName, updatedAsset.getName());
        assertEquals(newAltId, updatedAsset.getAlternateId());
    }

    @Test
    public void canDeleteAssetsById() throws Exception {
        int numToDelete = 3;
        Set<String> assetsToDelete = createTestAssets(numToDelete, "toDelete");

        ListResult<AssetInfo> currentAssets = entityService.list(Asset.list());

        for (String id : assetsToDelete) {
            entityService.delete(Asset.delete(id));
        }

        ListResult<AssetInfo> afterDeleteAssets = entityService.list(Asset
                .list());

        assertEquals(currentAssets.size() - numToDelete,
                afterDeleteAssets.size());

        for (AssetInfo asset : afterDeleteAssets) {
            assetsToDelete.remove(asset.getId());
        }

        assertEquals(numToDelete, assetsToDelete.size());
    }

    private Set<String> createTestAssets(int numAssets, String namePart)
            throws Exception {
        Set<String> expectedAssets = new HashSet<String>();

        for (int i = 0; i < numAssets; ++i) {
            AssetInfo asset = entityService.create(Asset.create().setName(
                    testAssetPrefix + namePart + Integer.toString(i)));
            expectedAssets.add(asset.getId());
        }
        return expectedAssets;
    }
}
