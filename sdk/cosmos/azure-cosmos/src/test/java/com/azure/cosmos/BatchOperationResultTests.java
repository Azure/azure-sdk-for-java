// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Utils;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.Test;

import java.time.Duration;
import static org.assertj.core.api.Assertions.assertThat;

public class BatchOperationResultTests {

    private static final int TIMEOUT = 40000;

    private TransactionalBatchOperationResult<?> createTestResult() {
        TransactionalBatchOperationResult<?> result = BridgeInternal.createTransactionBatchResult(
            "TestETag",
            1.4,
            Utils.getSimpleObjectMapper().createObjectNode(),
            HttpResponseStatus.OK.code(),
            Duration.ofMillis(1234),
            HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE
        );

        return result;
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void propertiesAreSetThroughCopyCtor() {
        TransactionalBatchOperationResult<?> other = createTestResult();
        TransactionalBatchOperationResult<?> result = new TransactionalBatchOperationResult<Object>(other);

        assertThat(other.getResponseStatus()).isEqualTo(result.getResponseStatus());
        assertThat(other.getSubStatusCode()).isEqualTo(result.getSubStatusCode());
        assertThat(other.getETag()).isEqualTo(result.getETag());
        assertThat(other.getRequestCharge()).isEqualTo(result.getRequestCharge());
        assertThat(other.getRetryAfter()).isEqualTo(result.getRetryAfter());
        assertThat(other.getResourceObject()).isSameAs(result.getResourceObject());
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void propertiesAreSetThroughGenericCtor() {
        TransactionalBatchOperationResult<?> other = createTestResult();
        Object testObject = new Object();
        TransactionalBatchOperationResult<Object> result = new TransactionalBatchOperationResult<Object>(other, testObject);

        assertThat(other.getResponseStatus()).isEqualTo(result.getResponseStatus());
        assertThat(other.getSubStatusCode()).isEqualTo(result.getSubStatusCode());
        assertThat(other.getETag()).isEqualTo(result.getETag());
        assertThat(other.getRequestCharge()).isEqualTo(result.getRequestCharge());
        assertThat(other.getRetryAfter()).isEqualTo(result.getRetryAfter());
        assertThat(other.getResourceObject()).isSameAs(result.getResourceObject());
        assertThat(testObject).isSameAs(result.getItem());
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void isSuccessStatusCodeTrueFor200To299() {
        for (int x = 100; x < 999; ++x) {
            TransactionalBatchOperationResult<?> result = new TransactionalBatchOperationResult<Object>(x);
            boolean success = x >= 200 && x <= 299;
            assertThat(result.isSuccessStatusCode()).isEqualTo(success);
        }
    }
}
