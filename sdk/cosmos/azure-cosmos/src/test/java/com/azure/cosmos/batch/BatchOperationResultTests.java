// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.Utils;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

public class BatchOperationResultTests {

    private static final int TIMEOUT = 40000;

    private TransactionalBatchOperationResult<?> createTestResult() {
        TransactionalBatchOperationResult<?> result = new TransactionalBatchOperationResult<Object>(HttpResponseStatus.OK);
        result.setSubStatusCode(HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE);
        result.setETag("TestETag");
        result.setRequestCharge(1.4);
        result.setResourceObject(Utils.getSimpleObjectMapper().createObjectNode());
        result.setRetryAfter(Duration.ofMillis(1234));

        return result;
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void propertiesAreSetThroughCopyCtor() {
        TransactionalBatchOperationResult<?> other = createTestResult();
        TransactionalBatchOperationResult<?> result = new TransactionalBatchOperationResult<Object>(other);

        assertEquals(other.getStatus(), result.getStatus());
        assertEquals(other.getSubStatusCode(), result.getSubStatusCode());
        assertEquals(other.getETag(), result.getETag());
        assertEquals(other.getRequestCharge(), result.getRequestCharge());
        assertEquals(other.getRetryAfter(), result.getRetryAfter());
        assertSame(other.getResourceObject(), result.getResourceObject());
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void propertiesAreSetThroughGenericCtor() {
        TransactionalBatchOperationResult<?> other = createTestResult();
        Object testObject = new Object();
        TransactionalBatchOperationResult<Object> result = new TransactionalBatchOperationResult<Object>(other, testObject);

        assertEquals(other.getStatus(), result.getStatus());
        assertEquals(other.getSubStatusCode(), result.getSubStatusCode());
        assertEquals(other.getETag(), result.getETag());
        assertEquals(other.getRequestCharge(), result.getRequestCharge());
        assertEquals(other.getRetryAfter(), result.getRetryAfter());
        assertSame(other.getResourceObject(), result.getResourceObject());
        assertSame(testObject, result.getResource());
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void toResponseMessageHasPropertiesMapped() {
        TransactionalBatchOperationResult<?> result = createTestResult();

        RxDocumentServiceResponse response = result.toResponseMessage();

        assertEquals(result.getStatus().code(), response.getStatusCode());
        assertEquals(String.valueOf(result.getSubStatusCode()), response.getResponseHeaders().get(HttpConstants.HttpHeaders.SUB_STATUS));
        assertEquals(result.getETag(), response.getResponseHeaders().get(HttpConstants.HttpHeaders.E_TAG));
        assertEquals(String.valueOf(result.getRequestCharge()), response.getResponseHeaders().get(HttpConstants.HttpHeaders.REQUEST_CHARGE));
        assertEquals(String.valueOf(result.getRetryAfter().toMillis()), response.getResponseHeaders().get(HttpConstants.HttpHeaders.RETRY_AFTER_IN_MILLISECONDS));
        assertEquals(result.getResourceObject().toString().getBytes(StandardCharsets.UTF_8), response.getResponseBodyAsByteArray());
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void isSuccessStatusCodeTrueFor200To299() {
        for (int x = 100; x < 999; ++x) {
            TransactionalBatchOperationResult<?> result = new TransactionalBatchOperationResult<Object>(HttpResponseStatus.valueOf(x));
            boolean success = x >= 200 && x <= 299;
            assertEquals(success, result.isSuccessStatusCode());
        }
    }
}
