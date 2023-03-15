// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.confidentialledger;

import com.azure.core.credential.AccessToken;
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
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfidentialLedgerManagementTestBase extends TestBase {
    private static AzureProfile azureProfile;
    private static TokenCredential credential;
    private static ResourceGroup testResourceGroup;
    private ConfidentialLedgerManagementOperations ledgerOperationsInstance;
    @BeforeAll
    public static void setup() {
        // Authenticate
        setAzureProfile();
        setCredential();

        // Create a resource group for testing in LIVE and RECORD modes only
        String testResourceGroupName = "acl-sdk-test-rg";
        setTestResourceGroup(testResourceGroupName);
    }
    @AfterAll
    public static void cleanUp() {
        // If AZURE_TEST_MODE isn't set to a value, use PLAYBACK mode as a default
        String testMode = getTestModeForStaticMethods();

        // Delete the created resource group in LIVE and RECORD modes only
        if (!("PLAYBACK".equals(testMode))) {
            ResourceManager
                .authenticate(getCredential(), getAzureProfile())
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
                .authenticate(getCredential(), getAzureProfile());
        } else if (getTestMode() == TestMode.RECORD) {
            ledgerManager = ConfidentialLedgerManager
                .configure()
                .withPolicy(interceptorManager.getRecordPolicy())
                .authenticate(getCredential(), getAzureProfile());
        } else if (getTestMode() == TestMode.PLAYBACK) {
            ledgerManager = ConfidentialLedgerManager
                .configure()
                .withDefaultPollInterval(Duration.ofMillis(10))
                .withHttpClient(interceptorManager.getPlaybackClient())
                .authenticate(getCredential(), getAzureProfile());
        }

        ledgerOperationsInstance = new ConfidentialLedgerManagementOperations(ledgerManager);
    }
    public static ResourceGroup getTestResourceGroup() {
        return testResourceGroup;
    }
    public static void setTestResourceGroup(String testResourceGroupName) {
        String testMode = getTestModeForStaticMethods();

        // Create a resource group in LIVE and RECORD modes only, Mock it otherwise
        if (!("PLAYBACK".equals(testMode))) {
            testResourceGroup = ResourceManager
                .authenticate(getCredential(), getAzureProfile())
                .withDefaultSubscription()
                .resourceGroups()
                .define(testResourceGroupName)
                .withRegion("eastus")
                .create();
        } else {
            testResourceGroup = mock(ResourceGroup.class);
            when(testResourceGroup.name()).thenReturn(testResourceGroupName);
        }
    }
    public static AzureProfile getAzureProfile() {
        return azureProfile;
    }
    public static void setAzureProfile() {
        String testMode = getTestModeForStaticMethods();
        if ("PLAYBACK".equals(testMode)) {
            azureProfile = new AzureProfile(null, "027da7f8-2fc6-46d4-9be9-560706b60fec", AzureEnvironment.AZURE);
        } else {
            azureProfile = new AzureProfile(AzureEnvironment.AZURE);
        }
    }

    public static TokenCredential getCredential() {
        return credential;
    }

    public static void setCredential() {
        String testMode = getTestModeForStaticMethods();
        if ("PLAYBACK".equals(testMode)) {
            credential = (request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)));
        } else {
            credential = new DefaultAzureCredentialBuilder().build();
        }
    }

    public ConfidentialLedgerManagementOperations getLedgerOperationsInstance() {
        return ledgerOperationsInstance;
    }

    public static String getTestModeForStaticMethods() {
        String testMode = System.getenv("AZURE_TEST_MODE");

        // If AZURE_TEST_MODE isn't set to a value, use PLAYBACK mode as a default
        if (testMode == null) {
            testMode = "PLAYBACK";
        }
        return testMode;
    }
    protected Map<String, String> mapOf(String... inputs) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            map.put(inputs[i], inputs[i + 1]);
        }
        return map;
    }
}
