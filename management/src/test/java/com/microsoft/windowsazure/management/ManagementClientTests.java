/*
 * Copyright 2013 Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.management;

import java.net.URI;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.core.Builder;
import com.microsoft.windowsazure.credentials.SubscriptionCloudCredentials;
import com.microsoft.windowsazure.management.models.AffinityGroupListResponse;
import com.microsoft.windowsazure.management.util.TestRequestFilter;
import com.microsoft.windowsazure.management.util.TestResponseFilter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.LoggingFilter;

public class ManagementClientTests extends ManagementIntegrationTestBase { 
    @Test
    public void createWithRequestFilterLast() throws Exception {
        // reinitialize configuration from known state
        Configuration config = createConfiguration();

        managementClient = ManagementService.create(config);
        
        TestRequestFilter testFilter = new TestRequestFilter("filter1a");
        ManagementClient filteredService = managementClient.withRequestFilterLast(testFilter);
        
        // Executing operation on the filtered service should execute the filter
        AffinityGroupListResponse response = filteredService.getAffinityGroupsOperations().list();
        
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(1, testFilter.getCalled());
        
        // Make sure the filter executes twice
        response = filteredService.getAffinityGroupsOperations().list();
        
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(2, testFilter.getCalled());
    }
    
    @Test
    public void createWithRequestLastRespectsOrder() throws Exception {
        // reinitialize configuration from known state
        Configuration config = createConfiguration();

        // add LoggingFilter to any pipeline that is created
        Builder.Registry builder = (Builder.Registry) config.getBuilder();
        builder.alter(ManagementClient.class, Client.class, new Builder.Alteration<Client>() {
            @Override
            public Client alter(String profile, Client client, Builder builder, Map<String, Object> properties) {
                client.addFilter(new LoggingFilter());
                return client;
            }
        });

        managementClient = ManagementService.create(config);
        
        TestRequestFilter testFilter1 = new TestRequestFilter("filter1b");
        TestRequestFilter testFilter2 = new TestRequestFilter("filter2b");
        ManagementClient filteredService = managementClient.withRequestFilterLast(testFilter1);
        filteredService = filteredService.withRequestFilterLast(testFilter2);
        
        // Executing operation on the filtered service should execute the filter
        AffinityGroupListResponse response = filteredService.getAffinityGroupsOperations().list();
        
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(0, testFilter1.getCalled());
        Assert.assertEquals(1, testFilter2.getCalled());
        
        // Make sure the filter executes twice
        response = filteredService.getAffinityGroupsOperations().list();
        
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(0, testFilter1.getCalled());
        Assert.assertEquals(2, testFilter2.getCalled());
    }
    
    @Test
    public void createWithRequestFirstRespectsOrder() throws Exception {
        // reinitialize configuration from known state
        Configuration config = createConfiguration();

        // add LoggingFilter to any pipeline that is created
        Builder.Registry builder = (Builder.Registry) config.getBuilder();
        builder.alter(ManagementClient.class, Client.class, new Builder.Alteration<Client>() {
            @Override
            public Client alter(String profile, Client client, Builder builder, Map<String, Object> properties) {
                client.addFilter(new LoggingFilter());
                return client;
            }
        });

        managementClient = ManagementService.create(config);
        
        TestRequestFilter testFilter1 = new TestRequestFilter("filter1c");
        TestRequestFilter testFilter2 = new TestRequestFilter("filter2c");
        ManagementClient filteredService = managementClient.withRequestFilterFirst(testFilter1);
        filteredService = filteredService.withRequestFilterFirst(testFilter2);
        
        // Executing operation on the filtered service should execute the filter
        AffinityGroupListResponse response = filteredService.getAffinityGroupsOperations().list();
        
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(1, testFilter1.getCalled());
        Assert.assertEquals(0, testFilter2.getCalled());
        
        // Make sure the filter executes twice
        response = filteredService.getAffinityGroupsOperations().list();
        
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(2, testFilter1.getCalled());
        Assert.assertEquals(0, testFilter2.getCalled());
    }
    
    @Test
    public void createWithResponseLastRespectsOrder() throws Exception {
        // reinitialize configuration from known state
        Configuration config = createConfiguration();

        // add LoggingFilter to any pipeline that is created
        Builder.Registry builder = (Builder.Registry) config.getBuilder();
        builder.alter(ManagementClient.class, Client.class, new Builder.Alteration<Client>() {
            @Override
            public Client alter(String profile, Client client, Builder builder, Map<String, Object> properties) {
                client.addFilter(new LoggingFilter());
                return client;
            }
        });

        managementClient = ManagementService.create(config);
        
        TestResponseFilter testFilter1 = new TestResponseFilter("filter1b");
        TestResponseFilter testFilter2 = new TestResponseFilter("filter2b");
        ManagementClient filteredService = managementClient.withResponseFilterLast(testFilter1);
        filteredService = filteredService.withResponseFilterLast(testFilter2);
        
        // Executing operation on the filtered service should execute the filter
        AffinityGroupListResponse response = filteredService.getAffinityGroupsOperations().list();
        
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(0, testFilter1.getCalled());
        Assert.assertEquals(1, testFilter2.getCalled());
        
        // Make sure the filter executes twice
        response = filteredService.getAffinityGroupsOperations().list();
        
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(0, testFilter1.getCalled());
        Assert.assertEquals(2, testFilter2.getCalled());
    }
    
    @Test
    public void createWithResponseFirstRespectsOrder() throws Exception {
        // reinitialize configuration from known state
        Configuration config = createConfiguration();

        // add LoggingFilter to any pipeline that is created
        Builder.Registry builder = (Builder.Registry) config.getBuilder();
        builder.alter(ManagementClient.class, Client.class, new Builder.Alteration<Client>() {
            @Override
            public Client alter(String profile, Client client, Builder builder, Map<String, Object> properties) {
                client.addFilter(new LoggingFilter());
                return client;
            }
        });

        managementClient = ManagementService.create(config);
        
        TestResponseFilter testFilter1 = new TestResponseFilter("filter1c");
        TestResponseFilter testFilter2 = new TestResponseFilter("filter2c");
        ManagementClient filteredService = managementClient.withResponseFilterFirst(testFilter1);
        filteredService = filteredService.withResponseFilterFirst(testFilter2);
        
        // Executing operation on the filtered service should execute the filter
        AffinityGroupListResponse response = filteredService.getAffinityGroupsOperations().list();
        
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(1, testFilter1.getCalled());
        Assert.assertEquals(0, testFilter2.getCalled());
        
        // Make sure the filter executes twice
        response = filteredService.getAffinityGroupsOperations().list();
        
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(2, testFilter1.getCalled());
        Assert.assertEquals(0, testFilter2.getCalled());
    }
    
    @Test
    public void getCredential() throws Exception {
        // reinitialize configuration from known state
        Configuration config = createConfiguration();

        // add LoggingFilter to any pipeline that is created
        Builder.Registry builder = (Builder.Registry) config.getBuilder();
        builder.alter(ManagementClient.class, Client.class, new Builder.Alteration<Client>() {
            @Override
            public Client alter(String profile, Client client, Builder builder, Map<String, Object> properties) {
                client.addFilter(new LoggingFilter());
                return client;
            }
        });

        managementClient = ManagementService.create(config);
        
        SubscriptionCloudCredentials subscriptionCloudCredentials = managementClient.getCredentials();      
        
        Assert.assertNotNull(subscriptionCloudCredentials.getSubscriptionId());          
    }
    
    @Test
    public void getUri() throws Exception {
        // reinitialize configuration from known state
        Configuration config = createConfiguration();

        // add LoggingFilter to any pipeline that is created
        Builder.Registry builder = (Builder.Registry) config.getBuilder();
        builder.alter(ManagementClient.class, Client.class, new Builder.Alteration<Client>() {
            @Override
            public Client alter(String profile, Client client, Builder builder, Map<String, Object> properties) {
                client.addFilter(new LoggingFilter());
                return client;
            }
        });

        managementClient = ManagementService.create(config);
        
        URI uri = managementClient.getBaseUri(); 
        URI expectUri = new URI("https://management.core.windows.net");
        
        Assert.assertEquals(expectUri.getHost(), uri.getHost());     
    }
}