// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.providerhub;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.test.utils.TestUtilities;
import com.azure.resourcemanager.providerhub.fluent.models.OperationsPutContentInner;
import com.azure.resourcemanager.providerhub.models.LocalizedOperationDefinition;
import com.azure.resourcemanager.providerhub.models.LocalizedOperationDefinitionDisplay;
import com.azure.resourcemanager.providerhub.models.LocalizedOperationDisplayDefinitionDefault;
import com.azure.resourcemanager.providerhub.models.OperationsPutContent;
import com.azure.resourcemanager.providerhub.models.OperationsPutContentProperties;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.policy.ProviderRegistrationPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ProviderHubManagerTests extends TestProxyTestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_WEST2;
    private String resourceGroupName = "rg" + randomPadding();
    private ProviderHubManager providerHubManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = TestUtilities.getTokenCredentialForTest(getTestMode());
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        resourceManager = ResourceManager.configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile)
            .withDefaultSubscription();

        providerHubManager = ProviderHubManager.configure()
            .withPolicy(new ProviderRegistrationPolicy(resourceManager))
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

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
    @SuppressWarnings("rawtypes")
    public void testCreateOperation() {
        OperationsPutContent operationsContent = null;
        String spaceName = "Microsoft.Contoso" + randomPadding();
        String opeartionName = spaceName + "/Employees/Read";
        try {
            // @embedmeStart
            operationsContent = providerHubManager.operations()
                .createOrUpdate(spaceName,
                    new OperationsPutContentInner().withProperties(new OperationsPutContentProperties()
                        .withContents(Arrays.asList(new LocalizedOperationDefinition().withName(opeartionName)
                            .withDisplay(new LocalizedOperationDefinitionDisplay().withDefaultProperty(
                                new LocalizedOperationDisplayDefinitionDefault().withProvider(spaceName)
                                    .withResource("Employees")
                                    .withOperation("Gets/List employee resources")
                                    .withDescription("Read employees")))))));
            // @embedmeEnd
            Assertions.assertTrue(providerHubManager.operations()
                .listByProviderRegistration(spaceName)
                .stream()
                .anyMatch(operationsDefinition -> {
                    if (Objects.nonNull(operationsDefinition.properties())) {
                        LinkedHashMap properties = (LinkedHashMap) operationsDefinition.properties();
                        if (Objects.nonNull(properties.get("contents"))) {
                            List contents = (ArrayList) properties.get("contents");
                            if (!contents.isEmpty()) {
                                for (int i = 0; i < contents.size(); i++) {
                                    LinkedHashMap content = (LinkedHashMap) contents.get(i);
                                    LinkedHashMap display = (LinkedHashMap) content.get("display");
                                    LinkedHashMap defaultProperty = (LinkedHashMap) display.get("default");
                                    if (opeartionName.equals(content.get("name"))
                                        && spaceName.equals(defaultProperty.get("provider"))) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                    return false;
                }));
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
