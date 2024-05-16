// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.loadtesting;

import org.junit.jupiter.api.Assertions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.loadtesting.models.CheckQuotaAvailabilityResponse;
import com.azure.resourcemanager.loadtesting.models.QuotaBucketRequest;
import com.azure.resourcemanager.loadtesting.models.QuotaBucketRequestPropertiesDimensions;
import com.azure.resourcemanager.loadtesting.models.QuotaResource;

public class QuotaOperations {

    private String location;
    private String quotaBucketName;

    public QuotaOperations(String location, String quotaBucketName) {
        this.location = location;
        this.quotaBucketName = quotaBucketName;
    }

    public void listBuckets(LoadTestManager manager) {
        PagedIterable<QuotaResource> resource = manager
            .quotas()
            .list(location);

        for (QuotaResource quotaResource : resource) {
            Assertions.assertNotNull(quotaResource.id());
            Assertions.assertNotNull(quotaResource.name());
            Assertions.assertNotNull(quotaResource.type());
            Assertions.assertNotNull(quotaResource.limit());
            Assertions.assertNotNull(quotaResource.usage());
        }
    }

    public void getBucket(LoadTestManager manager) {
        QuotaResource resource = getQuotaBucket(manager);

        Assertions.assertNotNull(resource.id());
        Assertions.assertEquals(quotaBucketName, resource.name());
        Assertions.assertNotNull(resource.type());
        Assertions.assertNotNull(resource.limit());
        Assertions.assertNotNull(resource.usage());
    }

    public void checkAvailability(LoadTestManager manager) {
        QuotaResource quotaResource = getQuotaBucket(manager);

        QuotaBucketRequestPropertiesDimensions dimensions = new QuotaBucketRequestPropertiesDimensions()
            .withLocation(location)
            .withSubscriptionId(manager.serviceClient().getSubscriptionId());

        QuotaBucketRequest request = new QuotaBucketRequest()
            .withCurrentQuota(quotaResource.limit())
            .withCurrentUsage(quotaResource.usage())
            .withNewQuota(quotaResource.limit())
            .withDimensions(dimensions);

        CheckQuotaAvailabilityResponse resource = manager
            .quotas()
            .checkAvailability(location, quotaBucketName, request);

        Assertions.assertNotNull(resource.id());
        Assertions.assertEquals(quotaBucketName, resource.name());
        Assertions.assertNotNull(resource.type());
        Assertions.assertNotNull(resource.isAvailable());
    }

    private QuotaResource getQuotaBucket(LoadTestManager manager) {
        QuotaResource resource = manager
            .quotas()
            .get(location, quotaBucketName);

        return resource;
    }
}
