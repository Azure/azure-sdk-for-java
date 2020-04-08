// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.keyvault;

import com.azure.management.RestClient;
import com.azure.management.graphrbac.implementation.GraphRbacManager;
import com.azure.management.keyvault.implementation.KeyVaultManager;
import com.azure.management.resources.core.TestBase;
import com.azure.management.resources.implementation.ResourceManager;

/** The base for KeyVault manager tests. */
public class KeyVaultManagementTest extends TestBase {
    protected ResourceManager resourceManager;
    protected KeyVaultManager keyVaultManager;
    protected GraphRbacManager graphRbacManager;
    protected String rgName = "";
    protected String vaultName = "";

    public KeyVaultManagementTest() {
        super();
    }

    public KeyVaultManagementTest(RunCondition runCondition) {
        super(runCondition);
    }

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        rgName = generateRandomResourceName("javacsmrg", 15);
        vaultName = generateRandomResourceName("java-keyvault-", 20);

        resourceManager =
            ResourceManager.authenticate(restClient).withSdkContext(sdkContext).withSubscription(defaultSubscription);

        graphRbacManager = GraphRbacManager.authenticate(restClient, domain, sdkContext);

        keyVaultManager = KeyVaultManager.authenticate(restClient, domain, defaultSubscription, sdkContext);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }
}
