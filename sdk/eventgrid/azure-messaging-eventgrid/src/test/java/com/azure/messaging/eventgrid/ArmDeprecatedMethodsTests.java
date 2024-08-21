// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.messaging.eventgrid.systemevents.ResourceActionCancelEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceActionFailureEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceActionSuccessEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceAuthorization;
import com.azure.messaging.eventgrid.systemevents.ResourceDeleteCancelEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceDeleteFailureEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceDeleteSuccessEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceHttpRequest;
import com.azure.messaging.eventgrid.systemevents.ResourceWriteCancelEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceWriteFailureEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceWriteSuccessEventData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArmDeprecatedMethodsTests {
    static final String REQUEST_ID = "requestID";
    static final String IP_ADDRESS = "IpAddress";
    static final String METHOD = "method";
    static final String HTTP_LOCAL_HOST = "http://localhost";
    static final String SCOPE = "scope";
    static final String ACTION = "action";
    static final String EVIDENCE_KEY = "username1";
    static final String EVIDENCE_VALUE = "password1";
    static final String CLAIM_KEY_1 = "claimKey1";
    static final String CLAIM_KEY_2 = "claimKey2";
    static final String CLAIM_VALUE_1 = "claimValue1";
    static final String CLAIM_VALUE_2 = "claimValue2";
    static final String EXPECTED_HTTP_REQUEST = String.format(
        "{\"clientRequestId\":\"%s\",\"clientIpAddress\":\"%s\",\"method\":\"%s\",\"url\":\"%s\"}",
        REQUEST_ID, IP_ADDRESS, METHOD, HTTP_LOCAL_HOST);
    static final String EXPECTED_CLAIMS = "{\"claimKey1\":\"claimValue1\",\"claimKey2\":\"claimValue2\"}";
    static final String EXPECTED_AUTHORIZATION = String.format(
        "{\"scope\":\"%s\",\"action\":\"%s\",\"evidence\":{\"%s\":\"%s\"}}",
        SCOPE, ACTION, EVIDENCE_KEY, EVIDENCE_VALUE);
    static final HashMap<String, String> CLAIM_MAP = new HashMap<>();
    static {
        CLAIM_MAP.put(CLAIM_KEY_1, CLAIM_VALUE_1);
        CLAIM_MAP.put(CLAIM_KEY_2, CLAIM_VALUE_2);
    }

    static final Map<String, String> EVIDENCE_MAP = Collections.singletonMap(EVIDENCE_KEY, EVIDENCE_VALUE);
    static final ResourceAuthorization RESOURCE_AUTHORIZATION = new ResourceAuthorization()
                                                      .setScope(SCOPE)
                                                      .setAction(ACTION)
                                                      .setEvidence(EVIDENCE_MAP);
    static final ResourceHttpRequest RESOURCE_HTTP_REQUEST = new ResourceHttpRequest()
                                                  .setClientRequestId(REQUEST_ID)
                                                  .setClientIpAddress(IP_ADDRESS)
                                                  .setMethod(METHOD)
                                                  .setUrl(HTTP_LOCAL_HOST);

    static Stream<Arguments> provideArmEventData() {
        return Stream.of(
            Arguments.of(new ResourceActionCancelEventData()),
            Arguments.of(new ResourceActionFailureEventData()),
            Arguments.of(new ResourceActionSuccessEventData()),
            Arguments.of(new ResourceDeleteCancelEventData()),
            Arguments.of(new ResourceDeleteFailureEventData()),
            Arguments.of(new ResourceDeleteSuccessEventData()),
            Arguments.of(new ResourceWriteCancelEventData()),
            Arguments.of(new ResourceWriteFailureEventData()),
            Arguments.of(new ResourceWriteSuccessEventData()));
    }

    @ParameterizedTest
    @MethodSource("provideArmEventData")
    public void deprecatedAuthorization(Object eventData)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Deprecated getter/setter methods
        eventData.getClass().getMethod("setAuthorization", String.class).invoke(eventData, EXPECTED_AUTHORIZATION);
        assertEquals(EXPECTED_AUTHORIZATION, eventData.getClass().getMethod("getAuthorization").invoke(eventData));
        // New getter/setter methods
        eventData.getClass().getMethod("setResourceAuthorization",
            ResourceAuthorization.class).invoke(eventData, RESOURCE_AUTHORIZATION);
        ResourceAuthorization resourceAuthorization =
            (ResourceAuthorization) eventData.getClass().getMethod("getResourceAuthorization").invoke(eventData);
        assertEquals(SCOPE, resourceAuthorization.getScope());
        assertEquals(ACTION, resourceAuthorization.getAction());
        assertEquals(EVIDENCE_MAP, resourceAuthorization.getEvidence());
    }

    @ParameterizedTest
    @MethodSource("provideArmEventData")
    @SuppressWarnings("unchecked")
    public void deprecatedClaims(Object eventData)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Deprecated getter/setter methods
        eventData.getClass().getMethod("setClaims", String.class)
            .invoke(eventData, EXPECTED_CLAIMS);
        assertEquals(EXPECTED_CLAIMS, eventData.getClass().getMethod("getClaims")
                                                 .invoke(eventData));
        // New getter/setter methods
        eventData.getClass().getMethod("setResourceClaims", Map.class).invoke(eventData, CLAIM_MAP);
        Method getResourceClaimsMethod = eventData.getClass().getMethod("getResourceClaims");

        Map<String, String> resourceClaims = (Map<String, String>)getResourceClaimsMethod.invoke(eventData);

        assertEquals(CLAIM_VALUE_1, resourceClaims.get(CLAIM_KEY_1));
        assertEquals(CLAIM_VALUE_2, resourceClaims.get(CLAIM_KEY_2));
    }

    @ParameterizedTest
    @MethodSource("provideArmEventData")
    public void deprecatedHttpRequest(Object eventData)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Deprecated getter/setter methods
        eventData.getClass().getMethod("setHttpRequest", String.class).invoke(eventData, EXPECTED_HTTP_REQUEST);
        assertEquals(EXPECTED_HTTP_REQUEST, eventData.getClass().getMethod("getHttpRequest").invoke(eventData));
        // New getter/setter methods
        eventData.getClass().getMethod("setResourceHttpRequest", ResourceHttpRequest.class)
            .invoke(eventData, RESOURCE_HTTP_REQUEST);
        ResourceHttpRequest resourceHttpRequest =
            (ResourceHttpRequest) eventData.getClass().getMethod("getResourceHttpRequest").invoke(eventData);
        assertEquals(REQUEST_ID, resourceHttpRequest.getClientRequestId());
        assertEquals(IP_ADDRESS, resourceHttpRequest.getClientIpAddress());
        assertEquals(METHOD, resourceHttpRequest.getMethod());
        assertEquals(HTTP_LOCAL_HOST, resourceHttpRequest.getUrl());
    }
}
