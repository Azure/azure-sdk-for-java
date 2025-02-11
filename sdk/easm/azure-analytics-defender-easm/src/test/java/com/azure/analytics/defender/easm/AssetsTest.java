// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.models.*;
import com.azure.core.http.rest.PagedIterable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class AssetsTest extends EasmClientTestBase {
    private String assetName = "kumed.com";
    private String assetKind = "domain";
    private String filter = "name = " + assetName + " and type = " + assetKind;
    private String assetId = assetKind + "$$" + "kumc.edu";

    private Class<?> getAssetResourceClass(String kind) {
        switch (kind) {
            case "as":
                return AsAssetResource.class;

            case "contact":
                return ContactAssetResource.class;

            case "domain":
                return DomainAssetResource.class;

            case "host":
                return HostAssetResource.class;

            case "ipAddress":
                return IpAddressAssetResource.class;

            case "ipBlock":
                return IpBlockAssetResource.class;

            case "page":
                return PageAssetResource.class;

            case "sslCert":
                return SslCertAssetResource.class;

            default:
                return null;

        }
    }

    @Test
    public void testAssetsListWithResponse() {

        PagedIterable<AssetResource> assetPageResponse = easmClient.listAssetResource(filter, "lastSeen", 0, null);
        AssetResource assetResponse = assetPageResponse.iterator().next();
        assertEquals(assetName, assetResponse.getName());
        assertInstanceOf(getAssetResourceClass(assetKind), assetResponse);
    }

    @Test
    public void testAssetsUpdateWithResponse() {
        AssetUpdateData assetUpdateData = new AssetUpdateData().setExternalId("new_external_id");
        Task taskResponse = easmClient.updateAssets(filter, assetUpdateData);
        assertEquals(TaskState.COMPLETE, taskResponse.getState());
        assertEquals(TaskPhase.COMPLETE, taskResponse.getPhase());
        //assertTrue(taskResponse.getId().matches(UUID_REGEX));
    }

    //
    @Test
    public void testAssetsGetWithResponse() {
        AssetResource assetResponse = easmClient.getAssetResource(assetId);
        assertInstanceOf(getAssetResourceClass(assetKind), assetResponse);
        //assertTrue(assetResponse.getUuid().matches(UUID_REGEX));
    }
}
