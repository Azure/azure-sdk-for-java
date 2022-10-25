// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.loadtestservice;

import org.junit.jupiter.api.Assertions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.Region;
import com.azure.resourcemanager.loadtestservice.models.CheckQuotaAvailabilityResponse;
import com.azure.resourcemanager.loadtestservice.models.QuotaBucketRequest;
import com.azure.resourcemanager.loadtestservice.models.QuotaBucketRequestPropertiesDimensions;
import com.azure.resourcemanager.loadtestservice.models.QuotaResource;

public class QuotaOperations{

    private static final Region Location = Region.US_WEST2;
    private static final String QuotaBucketName = "maxEngineInstancesPerTestRun";
    
    public static void listBuckets(LoadTestManager manager) {
        
        PagedIterable<QuotaResource> resource = manager
        .quotas()
        .list(Location.toString());

        for (QuotaResource quotaResource : resource) {
            Assertions.assertNotNull(quotaResource.id());
            Assertions.assertNotNull(quotaResource.name());
            Assertions.assertNotNull(quotaResource.type());
            Assertions.assertNotNull(quotaResource.limit());
            Assertions.assertNotNull(quotaResource.usage());
        }
    }

    public static void getBucket(LoadTestManager manager) {
    
        QuotaResource resource = getQuotaBucket(manager);

        Assertions.assertNotNull(resource.id());
        Assertions.assertEquals(QuotaBucketName, resource.name());
        Assertions.assertNotNull(resource.type());
        Assertions.assertNotNull(resource.limit());
        Assertions.assertNotNull(resource.usage());
    }

    public static void checkAvailability(LoadTestManager manager) {

        QuotaResource quotaResource = getQuotaBucket(manager);

        QuotaBucketRequestPropertiesDimensions dimensions = new QuotaBucketRequestPropertiesDimensions()
        .withLocation(Location.toString())
        .withSubscriptionId(manager.serviceClient().getSubscriptionId());

        QuotaBucketRequest request = new QuotaBucketRequest()
        .withCurrentQuota(quotaResource.limit())
        .withCurrentUsage(quotaResource.usage())
        .withNewQuota(quotaResource.limit())
        .withDimensions(dimensions);

        CheckQuotaAvailabilityResponse resource = manager
        .quotas()
        .checkAvailability(Location.toString(), QuotaBucketName, request);

        Assertions.assertNotNull(resource.id());
        Assertions.assertEquals(QuotaBucketName, resource.name());
        Assertions.assertNotNull(resource.type());
        Assertions.assertNotNull(resource.isAvailable());
    }

    private static QuotaResource getQuotaBucket(LoadTestManager manager) {
    
        QuotaResource resource = manager
        .quotas()
        .get(Location.toString(), QuotaBucketName);

        return resource;
    }

}
