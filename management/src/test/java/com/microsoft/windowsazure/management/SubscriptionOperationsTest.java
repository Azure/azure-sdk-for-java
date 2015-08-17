/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.management;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.management.models.SubscriptionGetResponse;
import com.microsoft.windowsazure.management.models.SubscriptionListOperationsParameters;
import com.microsoft.windowsazure.management.models.SubscriptionListOperationsResponse;

public class SubscriptionOperationsTest extends ManagementIntegrationTestBase { 
    @BeforeClass
    public static void setup() throws Exception {
        createService();
    }

    @Before
    public void beforeTest() throws Exception {
        setupTest();
    }

    @After
    public void afterTest() throws Exception {
        resetTest();
    }

    @Test
    public void getSubscriptionSuccess() throws Exception {
        // Act
        SubscriptionGetResponse subscriptionGetResponse = managementClient.getSubscriptionsOperations().get();
        // Assert
        Assert.assertEquals(200, subscriptionGetResponse.getStatusCode());
        Assert.assertNotNull(subscriptionGetResponse.getRequestId());
        Assert.assertNotNull(subscriptionGetResponse.getAccountAdminLiveEmailId());
        Assert.assertNotNull(subscriptionGetResponse.getSubscriptionID());

        Assert.assertNotNull(subscriptionGetResponse.getSubscriptionName());
        Assert.assertTrue(subscriptionGetResponse.getMaximumVirtualNetworkSites() > 0);
        Assert.assertTrue(subscriptionGetResponse.getMaximumLocalNetworkSites() > 0);
        Assert.assertTrue(subscriptionGetResponse.getMaximumDnsServers() > 0);
        Assert.assertTrue(subscriptionGetResponse.getMaximumStorageAccounts() > 0);
    }

    @Test
    public void listSubscriptionsSuccess() throws Exception {
        // Arrange  
        SubscriptionListOperationsParameters parameters = new SubscriptionListOperationsParameters();

        Calendar now = Calendar.getInstance();
        Calendar startTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        startTime.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH) , now.get(Calendar.DATE - 5));
        Calendar endTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        endTime.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH) , now.get(Calendar.DATE - 1));
        parameters.setStartTime(startTime);
        parameters.setEndTime(endTime);

        addRegexRule("StartTime=[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}%3A[0-9]{2}%3A[0-9]{2}\\.[0-9]+Z&EndTime=[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}%3A[0-9]{2}%3A[0-9]{2}\\.[0-9]+Z");
        SubscriptionListOperationsResponse subscriptionListOperationsResponse = managementClient.getSubscriptionsOperations().listOperations(parameters);

        Assert.assertEquals(200, subscriptionListOperationsResponse.getStatusCode());
        Assert.assertNotNull(subscriptionListOperationsResponse.getRequestId());
    }
}