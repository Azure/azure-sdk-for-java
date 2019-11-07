// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.http.HttpHeaders;
import com.google.common.collect.ImmutableMap;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static com.azure.data.cosmos.internal.HttpConstants.StatusCodes.BADREQUEST;
import static com.azure.data.cosmos.internal.HttpConstants.StatusCodes.CONFLICT;
import static com.azure.data.cosmos.internal.HttpConstants.StatusCodes.FORBIDDEN;
import static com.azure.data.cosmos.internal.HttpConstants.StatusCodes.GONE;
import static com.azure.data.cosmos.internal.HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR;
import static com.azure.data.cosmos.internal.HttpConstants.StatusCodes.LOCKED;
import static com.azure.data.cosmos.internal.HttpConstants.StatusCodes.METHOD_NOT_ALLOWED;
import static com.azure.data.cosmos.internal.HttpConstants.StatusCodes.NOTFOUND;
import static com.azure.data.cosmos.internal.HttpConstants.StatusCodes.PRECONDITION_FAILED;
import static com.azure.data.cosmos.internal.HttpConstants.StatusCodes.REQUEST_ENTITY_TOO_LARGE;
import static com.azure.data.cosmos.internal.HttpConstants.StatusCodes.REQUEST_TIMEOUT;
import static com.azure.data.cosmos.internal.HttpConstants.StatusCodes.RETRY_WITH;
import static com.azure.data.cosmos.internal.HttpConstants.StatusCodes.SERVICE_UNAVAILABLE;
import static com.azure.data.cosmos.internal.HttpConstants.StatusCodes.TOO_MANY_REQUESTS;
import static com.azure.data.cosmos.internal.HttpConstants.StatusCodes.UNAUTHORIZED;
import static com.google.common.base.Strings.lenientFormat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

public class CosmosClientExceptionTest {

    @Test(groups = { "unit" })
    public void headerNotNull1() {
        CosmosClientException dce = BridgeInternal.createCosmosClientException(0);
        assertThat(dce.responseHeaders()).isNotNull();
        assertThat(dce.responseHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void headerNotNull2() {
        CosmosClientException dce = BridgeInternal.createCosmosClientException(0, "dummy");
        assertThat(dce.responseHeaders()).isNotNull();
        assertThat(dce.responseHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void headerNotNull3() {
        CosmosClientException dce = BridgeInternal.createCosmosClientException(0, new RuntimeException());
        assertThat(dce.responseHeaders()).isNotNull();
        assertThat(dce.responseHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void headerNotNull4() {
        CosmosClientException dce = BridgeInternal.createCosmosClientException(0, (CosmosError) null, (Map) null);
        assertThat(dce.responseHeaders()).isNotNull();
        assertThat(dce.responseHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void headerNotNull5() {
        CosmosClientException dce = BridgeInternal.createCosmosClientException((String) null, 0, (CosmosError) null, (Map) null);
        assertThat(dce.responseHeaders()).isNotNull();
        assertThat(dce.responseHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void headerNotNull6() {
        CosmosClientException dce = BridgeInternal.createCosmosClientException((String) null, (Exception) null, (Map) null, 0, (String) null);
        assertThat(dce.responseHeaders()).isNotNull();
        assertThat(dce.responseHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void headerNotNull7() {
        ImmutableMap<String, String> respHeaders = ImmutableMap.of("key", "value");
        CosmosClientException dce = BridgeInternal.createCosmosClientException((String) null, (Exception) null, respHeaders, 0, (String) null);
        assertThat(dce.responseHeaders()).isNotNull();
        assertThat(dce.responseHeaders()).contains(respHeaders.entrySet().iterator().next());
    }

    @Test(groups = { "unit" }, dataProvider = "subTypes")
    public void statusCodeIsCorrect(Class<CosmosClientException> type, int expectedStatusCode) {
        try {
            Constructor<CosmosClientException> constructor = type.getDeclaredConstructor(String.class, HttpHeaders.class, String.class);
            constructor.setAccessible(true);
            final CosmosClientException instance = constructor.newInstance("some-message", null, "some-uri");
            assertEquals(instance.statusCode(), expectedStatusCode);
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException error) {
            String message = lenientFormat("could not create instance of %s due to %s", type, error);
            throw new AssertionError(message, error);
        }
    }

    @DataProvider(name = "subTypes")
    private static Object[][] subTypes() {
        return new Object[][] {
            { BadRequestException.class, BADREQUEST },
            { GoneException.class, GONE },
            { InternalServerErrorException.class, INTERNAL_SERVER_ERROR },
            { RequestTimeoutException.class, REQUEST_TIMEOUT },
            { ConflictException.class, CONFLICT },
            { ForbiddenException.class, FORBIDDEN },
            { InvalidPartitionException.class, GONE },
            { LockedException.class, LOCKED },
            { MethodNotAllowedException.class, METHOD_NOT_ALLOWED },
            { NotFoundException.class, NOTFOUND },
            { PartitionIsMigratingException.class, GONE },
            { PartitionKeyRangeGoneException.class, GONE },
            { PartitionKeyRangeIsSplittingException.class, GONE },
            { PreconditionFailedException.class, PRECONDITION_FAILED },
            { RequestEntityTooLargeException.class, REQUEST_ENTITY_TOO_LARGE },
            { RequestRateTooLargeException.class, TOO_MANY_REQUESTS },
            { RetryWithException.class, RETRY_WITH },
            { ServiceUnavailableException.class, SERVICE_UNAVAILABLE },
            { UnauthorizedException.class, UNAUTHORIZED }
        };
    }
}
