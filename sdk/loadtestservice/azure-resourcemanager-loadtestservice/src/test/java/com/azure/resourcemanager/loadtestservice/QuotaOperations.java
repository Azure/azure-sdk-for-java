// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.loadtestservice;

import org.junit.jupiter.api.Assertions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.loadtestservice.models.CheckQuotaAvailabilityResponse;
import com.azure.resourcemanager.loadtestservice.models.QuotaBucketRequest;
import com.azure.resourcemanager.loadtestservice.models.QuotaBucketRequestPropertiesDimensions;
import com.azure.resourcemanager.loadtestservice.models.QuotaResource;

public class QuotaOperations{

    private String Location;
    private String QuotaBucketName;

    public QuotaOperations(String Location, String QuotaBucketName){
        this.Location = Location;
        this.QuotaBucketName = QuotaBucketName;
    }
    
    public void ListBuckets(LoadTestManager manager) {
        // Get a paged iterator for the list all quota buckets response
        PagedIterable<QuotaResource> resource = manager
        .quotas()
        .list(Location);

        // Iterate over the paged iterator and validate the fields
        for (QuotaResource quotaResource : resource) {
            Assertions.assertNotNull(quotaResource.id());
            Assertions.assertNotNull(quotaResource.name());
            Assertions.assertNotNull(quotaResource.type());
            Assertions.assertNotNull(quotaResource.limit());
            Assertions.assertNotNull(quotaResource.usage());
        }
    }

    public void GetBucket(LoadTestManager manager) {
        // Get quota bucket response
        QuotaResource resource = GetQuotaBucket(manager);

        // Validate the fields
        Assertions.assertNotNull(resource.id());
        Assertions.assertEquals(QuotaBucketName, resource.name());
        Assertions.assertNotNull(resource.type());
        Assertions.assertNotNull(resource.limit());
        Assertions.assertNotNull(resource.usage());
    }

    public void CheckAvailability(LoadTestManager manager) {
        // Get quota bucket response
        QuotaResource quotaResource = GetQuotaBucket(manager);

        // Populate the quota bucket request dimentions model
        QuotaBucketRequestPropertiesDimensions dimensions = new QuotaBucketRequestPropertiesDimensions()
        .withLocation(Location)
        .withSubscriptionId(manager.serviceClient().getSubscriptionId());

        // Populate the quota bucket request model
        QuotaBucketRequest request = new QuotaBucketRequest()
        .withCurrentQuota(quotaResource.limit())
        .withCurrentUsage(quotaResource.usage())
        .withNewQuota(quotaResource.limit())
        .withDimensions(dimensions);

        // Get quota bucket check availability response
        CheckQuotaAvailabilityResponse resource = manager
        .quotas()
        .checkAvailability(Location, QuotaBucketName, request);

        // Validate the fields
        Assertions.assertNotNull(resource.id());
        Assertions.assertEquals(QuotaBucketName, resource.name());
        Assertions.assertNotNull(resource.type());
        Assertions.assertNotNull(resource.isAvailable());
    }

    private QuotaResource GetQuotaBucket(LoadTestManager manager) {
        // Get quota bucket response
        QuotaResource resource = manager
        .quotas()
        .get(Location, QuotaBucketName);

        return resource;
    }

}
