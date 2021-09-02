// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.storage;

import com.azure.core.http.rest.Response;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.models.ShareServiceProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import static com.azure.spring.cloud.actuate.storage.Constants.POLL_TIMEOUT;
import static com.azure.spring.cloud.actuate.storage.Constants.URL_FIELD;

/**
 * Health indicator for file storage.
 */
public class StorageFileHealthIndicator implements HealthIndicator {

    private final ShareServiceAsyncClient internalClient;

    public StorageFileHealthIndicator(ShareServiceAsyncClient shareServiceAsyncClient) {
        internalClient = shareServiceAsyncClient;
    }

    @Override
    public Health health() {
        Health.Builder healthBuilder = new Health.Builder();

        try {
            healthBuilder.withDetail(URL_FIELD, internalClient.getFileServiceUrl());
            Response<ShareServiceProperties> infoResponse;
            try {
                infoResponse = internalClient.getPropertiesWithResponse().block(POLL_TIMEOUT);
                if (infoResponse != null) {
                    healthBuilder.up();
                }
            } catch (Exception e) {
                healthBuilder.down(e);
            }
        } catch (Exception e) {
            healthBuilder.status("Could not complete health check.").down(e);
        }

        return healthBuilder.build();
    }
}
