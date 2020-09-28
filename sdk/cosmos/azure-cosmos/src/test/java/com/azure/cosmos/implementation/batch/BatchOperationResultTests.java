// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.TransactionalBatchOperationResult;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Utils;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.Test;

import java.time.Duration;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

public class BatchOperationResultTests {

    private static final int TIMEOUT = 40000;

    private TransactionalBatchOperationResult<?> createTestResult() {
        TransactionalBatchOperationResult<?> result = new TransactionalBatchOperationResult<Object>(HttpResponseStatus.OK.code());
        result.setSubStatusCode(HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE);
        result.setETag("TestETag");
        result.setRequestCharge(1.4);
        result.setResourceObject(Utils.getSimpleObjectMapper().createObjectNode());
        result.setRetryAfter(Duration.ofMillis(1234));

        return result;
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void propertiesAreSetThroughCopyCtor() {
        TransactionalBatchOperationResult<?> other = createTestResult();
        TransactionalBatchOperationResult<?> result = new TransactionalBatchOperationResult<Object>(other);

        assertEquals(other.getResponseStatus(), result.getResponseStatus());
        assertEquals(other.getSubStatusCode(), result.getSubStatusCode());
        assertEquals(other.getETag(), result.getETag());
        assertEquals(other.getRequestCharge(), result.getRequestCharge());
        assertEquals(other.getRetryAfter(), result.getRetryAfter());
        assertSame(other.getResourceObject(), result.getResourceObject());
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void propertiesAreSetThroughGenericCtor() {
        TransactionalBatchOperationResult<?> other = createTestResult();
        Object testObject = new Object();
        TransactionalBatchOperationResult<Object> result = new TransactionalBatchOperationResult<Object>(other, testObject);

        assertEquals(other.getResponseStatus(), result.getResponseStatus());
        assertEquals(other.getSubStatusCode(), result.getSubStatusCode());
        assertEquals(other.getETag(), result.getETag());
        assertEquals(other.getRequestCharge(), result.getRequestCharge());
        assertEquals(other.getRetryAfter(), result.getRetryAfter());
        assertSame(other.getResourceObject(), result.getResourceObject());
        assertSame(testObject, result.getItem());
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void isSuccessStatusCodeTrueFor200To299() {
        for (int x = 100; x < 999; ++x) {
            TransactionalBatchOperationResult<?> result = new TransactionalBatchOperationResult<Object>(x);
            boolean success = x >= 200 && x <= 299;
            assertEquals(success, result.isSuccessStatusCode());
        }
    }
}
