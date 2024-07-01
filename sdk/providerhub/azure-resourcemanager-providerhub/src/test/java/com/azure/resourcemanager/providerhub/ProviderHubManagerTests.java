// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.providerhub;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.resourcemanager.providerhub.fluent.models.OperationsDefinitionInner;
import com.azure.resourcemanager.providerhub.models.OperationsContent;
import com.azure.resourcemanager.providerhub.models.OperationsDefinitionDisplay;
import com.azure.resourcemanager.providerhub.models.OperationsPutContent;
import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

public class ProviderHubManagerTests extends TestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_WEST2;
    private String resourceGroupName = "rg" + randomPadding();
    private ProviderHubManager providerHubManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        providerHubManager = ProviderHubManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        resourceManager = ResourceManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile).withDefaultSubscription();

        // use AZURE_RESOURCE_GROUP_NAME if run in LIVE CI
        String testResourceGroup = Configuration.getGlobalConfiguration().get("AZURE_RESOURCE_GROUP_NAME");
        testEnv = !CoreUtils.isNullOrEmpty(testResourceGroup);
        if (testEnv) {
            resourceGroupName = testResourceGroup;
        } else {
            resourceManager.resourceGroups().define(resourceGroupName).withRegion(REGION).create();
        }
    }

    @Override
    protected void afterTest() {
        if (!testEnv) {
            resourceManager.resourceGroups().beginDeleteByName(resourceGroupName);
        }
    }

    @Test
    @LiveOnly
    public void testCreateOperation() {
        OperationsContent operationsContent = null;
        String spaceName = "Microsoft.Contoso" + randomPadding();
        String opeartionName = spaceName + "/Employees/Read";
        try {
            // @embedmeStart
            operationsContent = providerHubManager.operations()
                .createOrUpdate(spaceName,
                    new OperationsPutContent()
                        .withContents(
                            Arrays.asList(
                                new OperationsDefinitionInner()
                                    .withName(opeartionName)
                                    .withDisplay(new OperationsDefinitionDisplay()
                                        .withProvider(spaceName)
                                        .withResource("Employees")
                                        .withOperation("Gets/List employee resources")
                                        .withDescription("Read employees")))));
            // @embedmeEnd
            Assertions.assertTrue(
                providerHubManager.operations().listByProviderRegistration(spaceName)
                    .stream().filter(operationsDefinition ->
                        spaceName.equals(operationsDefinition.display().provider())
                            && opeartionName.equals(operationsDefinition.name()))
                    .findAny().isPresent());
        } finally {
            if (operationsContent != null) {
                providerHubManager.operations().delete(spaceName);
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
