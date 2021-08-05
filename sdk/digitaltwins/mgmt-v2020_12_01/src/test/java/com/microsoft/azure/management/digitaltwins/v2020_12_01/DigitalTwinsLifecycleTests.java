package com.microsoft.azure.management.digitaltwins.v2020_12_01;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.arm.core.TestBase;
import com.microsoft.azure.management.digitaltwins.v2020_12_01.implementation.CheckNameResultInner;
import com.microsoft.azure.management.digitaltwins.v2020_12_01.implementation.DigitalTwinsDescriptionInner;
import com.microsoft.azure.management.digitaltwins.v2020_12_01.implementation.DigitalTwinsManager;
import com.microsoft.azure.management.digitaltwins.v2020_12_01.implementation.OperationInner;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.rest.RestClient;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import org.junit.Assert;
import org.junit.Test;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class DigitalTwinsLifecycleTests extends TestBase {

    protected ResourceManager resourceManager;
    protected DigitalTwinsManager digitalTwinsManager;
    private static String rgName;
    protected String domain;
    private static String defaultInstanceName = "DigitalTwinsSdk";
    private static String defaultRegion = Region.US_WEST_CENTRAL.toString();
    private static String defaultResourceGroupName = "rg2b9842374ecf6";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) throws IOException {
        resourceManager = ResourceManager
            .authenticate(restClient)
            .withSubscription(defaultSubscription);

        digitalTwinsManager = DigitalTwinsManager
            .authenticate(restClient, defaultSubscription);

        this.domain = domain;
    }

    @Test
    public void lifecycleTest() throws InterruptedException {
        rgName = defaultResourceGroupName;

        ResourceGroup group = resourceManager.resourceGroups()
            .define(rgName)
            .withRegion(defaultRegion)
            .create();

        Assert.assertNotNull(group);

        try {
            CheckNameResultInner checkNameResult = digitalTwinsManager.inner().digitalTwins()
                .checkNameAvailability(defaultRegion, defaultInstanceName);

            if (!checkNameResult.nameAvailable()) {
                Iterator<DigitalTwinsDescription> allDigitalTwins = digitalTwinsManager
                    .digitalTwins()
                    .listAsync()
                    .toBlocking()
                    .getIterator();

                while (allDigitalTwins.hasNext()) {
                    DigitalTwinsDescription digitalTwin = allDigitalTwins.next();
                    if (digitalTwin.name().equals(defaultInstanceName)){
                        String existingResourceGroupName = digitalTwin.resourceGroupName();
                        digitalTwinsManager.inner()
                            .digitalTwins()
                            .delete(existingResourceGroupName, defaultInstanceName);

                        break;
                    }
                }

                checkNameResult = digitalTwinsManager.inner()
                    .digitalTwins()
                    .checkNameAvailability(defaultRegion, defaultInstanceName);

                Assert.assertTrue(checkNameResult.nameAvailable());
            }

            // Create DigitalTwins resource
            DigitalTwinsDescription instance = digitalTwinsManager
                .digitalTwins()
                .defineDigitalTwinsInstance(defaultInstanceName)
                .withRegion(defaultRegion)
                .withExistingResourceGroup(rgName)
                .create();

            Assert.assertNotNull(instance);
            Assert.assertEquals(defaultInstanceName, instance.name());
            Assert.assertEquals(defaultRegion, instance.region().toString());

            // Add and get tags
            final String key1 = "Key1";
            final String value1 = "Value1";
            final String key2 = "Key2";
            final String value2 = "Value2";

            DigitalTwinsDescriptionInner updatedDt = digitalTwinsManager.inner()
                .digitalTwins()
                .update(rgName, defaultInstanceName, new DigitalTwinsPatchDescription()
                    .withTags(new HashMap<String, String>(){{
                        put(key1, value1);
                        put(key2, value2);
                    }}));

            Assert.assertTrue(updatedDt.getTags().get(key1).equals(value1));
            Assert.assertTrue(updatedDt.getTags().get(key2).equals(value2));

            PagedList list =  digitalTwinsManager
                .inner()
                .digitalTwins().listByResourceGroup(rgName);

            Assert.assertTrue(list.size() > 0);

            Object[] array = digitalTwinsManager.inner().operations().list().toArray();

            ArrayList<String> myOpNames = new ArrayList<>();
            for (Object op : array) {
                myOpNames.add(((OperationInner) op).name());
            }

            Assert.assertTrue(myOpNames.contains("Microsoft.DigitalTwins/digitalTwinsInstances/read"));
            Assert.assertTrue(myOpNames.contains("Microsoft.DigitalTwins/digitalTwinsInstances/write"));
            Assert.assertTrue(myOpNames.contains("Microsoft.DigitalTwins/digitalTwinsInstances/delete"));
            Assert.assertTrue(myOpNames.contains("Microsoft.DigitalTwins/eventroutes/read"));
            Assert.assertTrue(myOpNames.contains("Microsoft.DigitalTwins/digitaltwins/read"));
            Assert.assertTrue(myOpNames.contains("Microsoft.DigitalTwins/models/read"));
        }
        finally {
            resourceManager.resourceGroups().beginDeleteByName(rgName);
        }
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(rgName);
    }
}
