// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.confidentialledger;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.identity.DefaultAzureCredentialBuilder;

import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfidentialLedgerManagementTestBase extends TestBase {
    public static AzureProfile profile;
    public static TokenCredential credential;
    public static ResourceGroup testResourceGroup;
    public ConfidentialLedgerManagementOperations ledgerOperations;

    @BeforeAll
    public static void setup() {

        // Authenticate
        profile = new AzureProfile(AzureEnvironment.AZURE);
        credential = new DefaultAzureCredentialBuilder().build();

        // Create a resource group for testing in LIVE and RECORD modes only
        String testResourceGroupName = "acl-sdk-test-rg";
        if (!System.getenv("AZURE_TEST_MODE").equals("PLAYBACK")) {
            testResourceGroup = ResourceManager
                .authenticate(credential, profile)
                .withDefaultSubscription()
                .resourceGroups()
                .define(testResourceGroupName)
                .withRegion("eastus")
                .create();
        }
        else {
            // Mock the test resource group
            testResourceGroup = mock(ResourceGroup.class);
            when(testResourceGroup.name()).thenReturn(testResourceGroupName);
        }
    }

    @AfterAll
    public static void cleanUp() {
        // Delete the created resource group in LIVE and RECORD modes only
        if (!System.getenv("AZURE_TEST_MODE").equals("PLAYBACK")) {
            ResourceManager
                .authenticate(credential, profile)
                .withDefaultSubscription()
                .resourceGroups()
                .deleteByName(testResourceGroup.name());
        }
    }
    @BeforeEach
    public void setupManager() {
        ConfidentialLedgerManager ledgerManager = null;
        if (getTestMode() == TestMode.LIVE) {
            ledgerManager = ConfidentialLedgerManager
                .authenticate(credential, profile);
        }
        else if (getTestMode() == TestMode.RECORD) {
            ledgerManager = ConfidentialLedgerManager
                .configure()
                .withPolicy(interceptorManager.getRecordPolicy())
                .authenticate(credential, profile);
        }
        else if (getTestMode() == TestMode.PLAYBACK) {
            ledgerManager = ConfidentialLedgerManager
                .configure()
                .withHttpClient(interceptorManager.getPlaybackClient())
                .authenticate(credential, profile);
        }

        ledgerOperations = new ConfidentialLedgerManagementOperations(ledgerManager);
    }

    protected Map<String, String> mapOf(String... inputs) {
        Map<String, String> map = new HashMap<>();
        for(int i = 0; i < inputs.length; i += 2) {
            map.put(inputs[i], inputs[i + 1]);
        }
        return map;
    }

}
