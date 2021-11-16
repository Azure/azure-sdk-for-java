// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.storage;

import com.azure.core.http.rest.Response;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.models.ShareServiceProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.time.Duration;

import static com.azure.spring.cloud.actuate.storage.StorageHealthConstants.URL_FIELD;
import static com.azure.spring.cloud.actuate.util.Constants.DEFAULT_TIMEOUT_SECONDS;

/**
 * Health indicator for file storage.
 */
public class StorageFileHealthIndicator implements HealthIndicator {

    private final ShareServiceAsyncClient shareServiceAsyncClient;
    private int timeout = DEFAULT_TIMEOUT_SECONDS;

    public StorageFileHealthIndicator(ShareServiceAsyncClient shareServiceAsyncClient) {
        this.shareServiceAsyncClient = shareServiceAsyncClient;
    }

    @Override
    public Health health() {
        Health.Builder healthBuilder = new Health.Builder();
        try {
            healthBuilder.withDetail(URL_FIELD, shareServiceAsyncClient.getFileServiceUrl());
            Response<ShareServiceProperties> infoResponse;
            try {
                infoResponse = shareServiceAsyncClient.getPropertiesWithResponse().block(Duration.ofSeconds(timeout));
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

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
