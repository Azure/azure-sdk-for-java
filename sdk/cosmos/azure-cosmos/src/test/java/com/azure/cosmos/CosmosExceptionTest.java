// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.implementation.ConflictException;
import com.azure.cosmos.implementation.ForbiddenException;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InternalServerErrorException;
import com.azure.cosmos.implementation.InvalidPartitionException;
import com.azure.cosmos.implementation.LockedException;
import com.azure.cosmos.implementation.MethodNotAllowedException;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.implementation.PartitionIsMigratingException;
import com.azure.cosmos.implementation.PartitionKeyRangeGoneException;
import com.azure.cosmos.implementation.PartitionKeyRangeIsSplittingException;
import com.azure.cosmos.implementation.PreconditionFailedException;
import com.azure.cosmos.implementation.RequestEntityTooLargeException;
import com.azure.cosmos.implementation.RequestRateTooLargeException;
import com.azure.cosmos.implementation.RequestTimeoutException;
import com.azure.cosmos.implementation.RetryWithException;
import com.azure.cosmos.implementation.ServiceUnavailableException;
import com.azure.cosmos.implementation.UnauthorizedException;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.CosmosError;
import com.azure.cosmos.implementation.guava25.collect.ImmutableMap;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static com.azure.cosmos.implementation.HttpConstants.StatusCodes.BADREQUEST;
import static com.azure.cosmos.implementation.HttpConstants.StatusCodes.CONFLICT;
import static com.azure.cosmos.implementation.HttpConstants.StatusCodes.FORBIDDEN;
import static com.azure.cosmos.implementation.HttpConstants.StatusCodes.GONE;
import static com.azure.cosmos.implementation.HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR;
import static com.azure.cosmos.implementation.HttpConstants.StatusCodes.LOCKED;
import static com.azure.cosmos.implementation.HttpConstants.StatusCodes.METHOD_NOT_ALLOWED;
import static com.azure.cosmos.implementation.HttpConstants.StatusCodes.NOTFOUND;
import static com.azure.cosmos.implementation.HttpConstants.StatusCodes.PRECONDITION_FAILED;
import static com.azure.cosmos.implementation.HttpConstants.StatusCodes.REQUEST_ENTITY_TOO_LARGE;
import static com.azure.cosmos.implementation.HttpConstants.StatusCodes.REQUEST_TIMEOUT;
import static com.azure.cosmos.implementation.HttpConstants.StatusCodes.RETRY_WITH;
import static com.azure.cosmos.implementation.HttpConstants.StatusCodes.SERVICE_UNAVAILABLE;
import static com.azure.cosmos.implementation.HttpConstants.StatusCodes.TOO_MANY_REQUESTS;
import static com.azure.cosmos.implementation.HttpConstants.StatusCodes.UNAUTHORIZED;
import static com.azure.cosmos.implementation.guava27.Strings.lenientFormat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

public class CosmosExceptionTest {

    @Test(groups = { "unit" })
    public void sdkVersionPresent() {
        CosmosException dce = BridgeInternal.createCosmosException(0);
        assertThat(dce.toString()).contains("\"userAgent\":\"" + Utils.getUserAgent());
    }

    @Test(groups = { "unit" })
    public void headerNotNull1() {
        CosmosException dce = BridgeInternal.createCosmosException(0);
        assertThat(dce.getResponseHeaders()).isNotNull();
        assertThat(dce.getResponseHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void headerNotNull2() {
        CosmosException dce = BridgeInternal.createCosmosException(0, "dummy");
        assertThat(dce.getResponseHeaders()).isNotNull();
        assertThat(dce.getResponseHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void headerNotNull3() {
        CosmosException dce = BridgeInternal.createCosmosException(null, 0, new RuntimeException());
        assertThat(dce.getResponseHeaders()).isNotNull();
        assertThat(dce.getResponseHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void headerNotNull4() {
        CosmosException dce = BridgeInternal.createCosmosException(null, 0, (CosmosError) null, (Map<String, String>) null);
        assertThat(dce.getResponseHeaders()).isNotNull();
        assertThat(dce.getResponseHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void headerNotNull5() {
        CosmosException dce = BridgeInternal.createCosmosException((String) null, 0, (CosmosError) null, (Map<String, String>) null);
        assertThat(dce.getResponseHeaders()).isNotNull();
        assertThat(dce.getResponseHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void headerNotNull6() {
        CosmosException dce = BridgeInternal.createCosmosException((String) null, (Exception) null, (Map<String, String>) null, 0, (String) null);
        assertThat(dce.getResponseHeaders()).isNotNull();
        assertThat(dce.getResponseHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void headerNotNull7() {
        ImmutableMap<String, String> respHeaders = ImmutableMap.of("key", "getValue");
        CosmosException dce = BridgeInternal.createCosmosException((String) null, (Exception) null, respHeaders, 0, (String) null);
        assertThat(dce.getResponseHeaders()).isNotNull();
        assertThat(dce.getResponseHeaders()).contains(respHeaders.entrySet().iterator().next());
    }

    @Test(groups = { "unit" }, dataProvider = "subTypes")
    public void statusCodeIsCorrect(Class<CosmosException> type, int expectedStatusCode) {
        try {
            Constructor<CosmosException> constructor = type.getDeclaredConstructor(String.class, HttpHeaders.class, String.class);
            constructor.setAccessible(true);
            final CosmosException instance = constructor.newInstance("some-message", null, "some-uri");
            assertEquals(instance.getStatusCode(), expectedStatusCode);
            assertThat(instance.toString()).contains("\"userAgent\":\"" + Utils.getUserAgent());
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
