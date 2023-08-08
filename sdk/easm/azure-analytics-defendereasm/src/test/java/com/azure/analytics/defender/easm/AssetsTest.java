package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.generated.EasmDefenderClientTestBase;
import com.azure.analytics.defender.easm.models.*;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;


public class AssetsTest extends EasmDefenderClientTestBase {
    
    private String assetName = "ku.edu";
    private String assetKind = "domain";
    private String filter = "name = " + assetName + " and type = " + assetKind;
    private String assetId = assetKind + "$$" + assetName;
    private String observationAssetId = "71830a02-2037-5b7f-c644-8c940b89ceea";

    private Class<?> getAssetResourceClass(String kind){
        switch(kind){
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
        }
        return null;
    }


    @Test
    public void testAssetsListWithResponse(){
        AssetPageResponse assetPageResponse = assetsClient.list(filter, "lastSeen", 0, 25, null);
        AssetResource AssetResource = assetPageResponse.getValue().get(0);
        assertEquals(assetName, AssetResource.getName());
        assertInstanceOf(getAssetResourceClass(assetKind), AssetResource);
        // assertTrue(AssetResource.getUuid().matches(UUID_REGEX));

    }

    @Test
    public void testAssetsUpdateWithResponse(){
        AssetUpdateData assetUpdateRequest = new AssetUpdateData().setExternalId("new_external_id");
        Task taskResponse = assetsClient.update(filter, assetUpdateRequest);
        assertEquals(TaskState.COMPLETE, taskResponse.getState());
        assertEquals(TaskPhase.COMPLETE, taskResponse.getPhase());
        // assertTrue(taskResponse.getId().matches(UUID_REGEX));

    }

    @Test
    public void testAssetsGetWithResponse(){
        AssetResource AssetResource = assetsClient.get(assetId);
        assertEquals(assetName, AssetResource.getName());
        assertInstanceOf(getAssetResourceClass(assetKind), AssetResource);
       // assertTrue(AssetResource.getUuid().matches(UUID_REGEX));
    }

}