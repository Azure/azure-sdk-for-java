// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.digitaltwins;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.digitaltwins.models.CheckNameRequest;
import com.azure.resourcemanager.digitaltwins.models.CheckNameResult;
import com.azure.resourcemanager.digitaltwins.models.DigitalTwinsDescription;
import com.azure.resourcemanager.digitaltwins.models.Operation;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DigitalTwinsLifecycleTests extends TestBase {

    private static final String DEFAULT_INSTANCE_NAME = "DigitalTwinsSdk";
    private static final Region DEFAULT_REGION = Region.US_WEST_CENTRAL;
    private static final String DEFAULT_RESOURCE_GROUP_NAME = "rg2b9842374ecf8";

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void lifecycleTest() {
        String rgName = DEFAULT_RESOURCE_GROUP_NAME;

        ResourceManager resourceManager = ResourceManager
            .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureEnvironment.AZURE))
            .withDefaultSubscription();

        AzureDigitalTwinsManager digitalTwinsManager = AzureDigitalTwinsManager
            .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureEnvironment.AZURE));

        ResourceGroup group = resourceManager.resourceGroups()
            .define(rgName)
            .withRegion(DEFAULT_REGION)
            .create();

        Assertions.assertNotNull(group);

        try {
            CheckNameResult checkNameResult = digitalTwinsManager.digitalTwins()
                .checkNameAvailability(DEFAULT_REGION.toString(), new CheckNameRequest().withName(DEFAULT_INSTANCE_NAME));

            if (!checkNameResult.nameAvailable()) {
                PagedIterable<DigitalTwinsDescription> allDigitalTwins = digitalTwinsManager
                    .digitalTwins().list();

                for (DigitalTwinsDescription digitalTwin : allDigitalTwins) {
                    if (digitalTwin.name().equals(DEFAULT_INSTANCE_NAME)) {
                        digitalTwinsManager.digitalTwins().deleteById(digitalTwin.id());
                        break;
                    }
                }

                checkNameResult = digitalTwinsManager.digitalTwins()
                    .checkNameAvailability(rgName, new CheckNameRequest().withName(DEFAULT_INSTANCE_NAME));

                Assertions.assertTrue(checkNameResult.nameAvailable());
            }

            // Create DigitalTwins resource
            DigitalTwinsDescription instance = digitalTwinsManager
                .digitalTwins()
                .define(DEFAULT_INSTANCE_NAME)
                .withRegion(DEFAULT_REGION)
                .withExistingResourceGroup(rgName)
                .create();

            Assertions.assertNotNull(instance);
            Assertions.assertEquals(DEFAULT_INSTANCE_NAME, instance.name());
            Assertions.assertEquals(DEFAULT_REGION, instance.region());

            // Add and get tags
            final String key1 = "Key1";
            final String value1 = "Value1";
            final String key2 = "Key2";
            final String value2 = "Value2";

            Map<String, String> tags = new HashMap<>();
            tags.put(key1, value1);
            tags.put(key2, value2);
            instance = instance.update()
                .withTags(tags)
                .apply();

            Assertions.assertEquals(value1, instance.tags().get(key1));
            Assertions.assertEquals(value2, instance.tags().get(key2));

            List<DigitalTwinsDescription> list = digitalTwinsManager.digitalTwins()
                .listByResourceGroup(rgName)
                .stream().collect(Collectors.toList());

            Assertions.assertTrue(list.size() > 0);

            List<String> myOpNames = digitalTwinsManager.operations().list()
                .stream().map(Operation::name)
                .collect(Collectors.toList());

            Assertions.assertTrue(myOpNames.contains("Microsoft.DigitalTwins/digitalTwinsInstances/read"));
            Assertions.assertTrue(myOpNames.contains("Microsoft.DigitalTwins/digitalTwinsInstances/write"));
            Assertions.assertTrue(myOpNames.contains("Microsoft.DigitalTwins/digitalTwinsInstances/delete"));
            Assertions.assertTrue(myOpNames.contains("Microsoft.DigitalTwins/eventroutes/read"));
            Assertions.assertTrue(myOpNames.contains("Microsoft.DigitalTwins/digitaltwins/read"));
            Assertions.assertTrue(myOpNames.contains("Microsoft.DigitalTwins/models/read"));
        } finally {
            resourceManager.resourceGroups().beginDeleteByName(rgName);
        }
    }
}
