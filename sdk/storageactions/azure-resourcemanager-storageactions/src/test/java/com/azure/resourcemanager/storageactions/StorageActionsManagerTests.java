// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storageactions;

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
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.resourcemanager.storageactions.models.IfCondition;
import com.azure.resourcemanager.storageactions.models.ManagedServiceIdentity;
import com.azure.resourcemanager.storageactions.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.storageactions.models.OnFailure;
import com.azure.resourcemanager.storageactions.models.OnSuccess;
import com.azure.resourcemanager.storageactions.models.StorageTask;
import com.azure.resourcemanager.storageactions.models.StorageTaskAction;
import com.azure.resourcemanager.storageactions.models.StorageTaskOperation;
import com.azure.resourcemanager.storageactions.models.StorageTaskOperationName;
import com.azure.resourcemanager.storageactions.models.StorageTaskProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class StorageActionsManagerTests extends TestProxyTestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_WEST3;
    private String resourceGroupName = "rg" + randomPadding();
    private StorageActionsManager storageActionsManager = null;
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

        storageActionsManager = StorageActionsManager.configure()
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
    public void testCreateStorageTask() {
        StorageTask storageTask = null;
        try {
            String taskName = "task" + randomPadding();
            // @embedmeStart
            Map<String, String> operationMap = new LinkedHashMap<>();
            operationMap.put("tier", "Hot");
            storageTask = storageActionsManager.storageTasks()
                .define(taskName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
                .withProperties(new StorageTaskProperties()
                    .withAction(new StorageTaskAction()
                        .withIfProperty(new IfCondition().withCondition("[[[equals(AccessTier, 'Cool')]]")
                            .withOperations(Arrays
                                .asList(new StorageTaskOperation().withName(StorageTaskOperationName.SET_BLOB_TIER)
                                    .withParameters(operationMap)
                                    .withOnSuccess(OnSuccess.CONTINUE)
                                    .withOnFailure(OnFailure.BREAK)))))
                    .withDescription("Storage task")
                    .withEnabled(true))
                .create();
            // @embedmeEnd
            storageTask.refresh();
            Assertions.assertEquals(taskName, storageTask.name());
            Assertions.assertEquals(taskName, storageActionsManager.storageTasks().getById(storageTask.id()).name());
            Assertions.assertTrue(storageActionsManager.storageTasks()
                .listByResourceGroup(resourceGroupName)
                .stream()
                .findAny()
                .isPresent());
        } finally {
            if (storageTask != null) {
                storageActionsManager.storageTasks().deleteById(storageTask.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
