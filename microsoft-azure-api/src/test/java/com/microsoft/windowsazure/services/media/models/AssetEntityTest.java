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

package com.microsoft.windowsazure.services.media.models;

import static org.junit.Assert.*;

import java.net.URLEncoder;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.content.AssetType;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityGetOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityListOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityUpdateOperation;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Tests for the methods and factories of the Asset entity.
 */
public class AssetEntityTest {
    static final String sampleAssetId = "nb:cid:UUID:1151b8bd-9ada-4e7f-9787-8dfa49968eab";
    private final String expectedUri = String.format("Assets('%s')", URLEncoder.encode(sampleAssetId, "UTF-8"));

    public AssetEntityTest() throws Exception {
    }

    @Test
    public void assetCreateReturnsDefaultCreatePayload() {
        AssetType payload = (AssetType) Asset.create().getRequestContents();

        assertNotNull(payload);
        assertNull(payload.getId());
        assertNull(payload.getState());
        assertNull(payload.getCreated());
        assertNull(payload.getLastModified());
        assertNull(payload.getAlternateId());
        assertNull(payload.getName());
        assertNull(payload.getOptions());
    }

    @Test
    public void assetCreateCanSetAssetName() {
        String name = "assetCreateCanSetAssetName";

        Asset.Creator creator = Asset.create().setName("assetCreateCanSetAssetName");

        AssetType payload = (AssetType) creator.getRequestContents();

        assertNotNull(payload);
        assertNull(payload.getId());
        assertNull(payload.getState());
        assertNull(payload.getCreated());
        assertNull(payload.getLastModified());
        assertNull(payload.getAlternateId());
        assertEquals(name, payload.getName());
        assertNull(payload.getOptions());
    }

    @Test
    public void assetGetReturnsExpectedUri() throws Exception {
        String expectedUri = String.format("Assets('%s')", URLEncoder.encode(sampleAssetId, "UTF-8"));

        EntityGetOperation<AssetInfo> getter = Asset.get(sampleAssetId);

        assertEquals(expectedUri, getter.getUri());
    }

    @Test
    public void assetListReturnsExpectedUri() {
        EntityListOperation<AssetInfo> lister = Asset.list();

        assertEquals("Assets", lister.getUri());
        assertNotNull(lister.getQueryParameters());
        assertEquals(0, lister.getQueryParameters().size());
    }

    @Test
    public void assetListCanTakeQueryParameters() {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("$top", "10");
        queryParams.add("$skip", "2");

        EntityListOperation<AssetInfo> lister = Asset.list(queryParams);

        assertEquals("10", lister.getQueryParameters().getFirst("$top"));
        assertEquals("2", lister.getQueryParameters().getFirst("$skip"));
        assertEquals(2, lister.getQueryParameters().size());
    }

    @Test
    public void assetUpdateReturnsExpectedUri() throws Exception {
        EntityUpdateOperation updater = Asset.update(sampleAssetId);
        assertEquals(expectedUri, updater.getUri());
    }

    @Test
    public void assetUpdateCanSetNameAndAltId() throws Exception {

        String expectedName = "newAssetName";
        String expectedAltId = "newAltId";

        EntityUpdateOperation updater = Asset.update(sampleAssetId).setName(expectedName).setAlternateId(expectedAltId);

        AssetType payload = (AssetType) updater.getRequestContents();

        assertEquals(expectedName, payload.getName());
        assertEquals(expectedAltId, payload.getAlternateId());
    }

    @Test
    public void assetDeleteReturnsExpectedUri() throws Exception {
        EntityDeleteOperation deleter = Asset.delete(sampleAssetId);

        assertEquals(expectedUri, deleter.getUri());
    }

}
