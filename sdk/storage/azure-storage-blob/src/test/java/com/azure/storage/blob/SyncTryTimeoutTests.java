// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.util.Context;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class SyncTryTimeoutTests {
    @Test
    public void syncClientHonorsTryTimeout() {
        AtomicInteger attempts = new AtomicInteger();
        BlobServiceClient client = new BlobServiceClientBuilder().endpoint("http://localhost/")
            .credential(new StorageSharedKeyCredential("account", "dGVzdA=="))
            .retryOptions(new RequestRetryOptions(RetryPolicyType.FIXED, 1, 1, 1L, 1L, null))
            .httpClient(new NoOpHttpClient() {
                @Override
                public HttpResponse sendSync(HttpRequest request, Context context) {
                    attempts.incrementAndGet();
                    return Mono.<HttpResponse>never().block(Duration.ofSeconds(10));
                }
            })
            .buildClient();

        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            RuntimeException error = Assertions.assertThrows(RuntimeException.class, client::getAccountInfo);
            Assertions.assertInstanceOf(TimeoutException.class, error.getCause());
            Assertions.assertEquals(1, attempts.get());
        });
    }
}
