// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.models.*;
import com.azure.core.http.rest.PagedFlux;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class AssetsAsyncTest extends EasmClientTestBase {
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
    public void testAssetsListAsync() {
        PagedFlux<AssetResource> assetPageResponse = easmAsyncClient.listAssetResource(filter, "lastSeen", 0, null);

        StepVerifier.create(assetPageResponse).assertNext(assetResource -> {
            assertEquals(assetName, assetResource.getName());
            assertInstanceOf(getAssetResourceClass(assetKind), assetResource);
        }).expectComplete().verify();
    }

    @Test
    public void testAssetsUpdateAsync() {
        AssetUpdateData assetUpdateData = new AssetUpdateData().setExternalId("new_external_id");
        Mono<Task> taskMono = easmAsyncClient.updateAssets(filter, assetUpdateData);
        StepVerifier.create(taskMono).assertNext(task -> {
            assertEquals(TaskState.COMPLETE, task.getState());
            assertEquals(TaskPhase.COMPLETE, task.getPhase());
        }).expectComplete().verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void testAssetsGetAsync() {
        Mono<AssetResource> assetMono = easmAsyncClient.getAssetResource(assetId);
        StepVerifier.create(assetMono).assertNext(assetResource -> {
            System.out.println("Asset name is: " + assetResource.getName());
            assertInstanceOf(getAssetResourceClass(assetKind), assetResource);
        }).expectComplete().verify(DEFAULT_TIMEOUT);
    }
}
