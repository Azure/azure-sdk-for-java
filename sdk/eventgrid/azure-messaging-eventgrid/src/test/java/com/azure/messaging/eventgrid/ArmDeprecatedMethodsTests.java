// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.messaging.eventgrid.systemevents.ResourceAuthorization;
import com.azure.messaging.eventgrid.systemevents.ResourceDeleteCancelEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceHttpRequest;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    Map<String, String> evidenceMap = Collections.singletonMap(EVIDENCE_KEY, EVIDENCE_VALUE);
    ResourceAuthorization resourceAuthorization = new ResourceAuthorization()
                                                      .setScope(SCOPE)
                                                      .setAction(ACTION)
                                                      .setEvidence(evidenceMap);
    ResourceHttpRequest resourceHttpRequest = new ResourceHttpRequest()
                                                  .setClientRequestId(REQUEST_ID)
                                                  .setClientIpAddress(IP_ADDRESS)
                                                  .setMethod(METHOD)
                                                  .setUrl(HTTP_LOCAL_HOST);

    @Test
    public void deprecatedAuthorization() {
        final ResourceDeleteCancelEventData resourceDeleteCancelEventData = new ResourceDeleteCancelEventData();
        // Deprecated getter/setter methods
        resourceDeleteCancelEventData.setAuthorization(EXPECTED_AUTHORIZATION);
        assertEquals(EXPECTED_AUTHORIZATION, resourceDeleteCancelEventData.getAuthorization());
        // New getter/setter methods
        resourceDeleteCancelEventData.setResourceAuthorization(resourceAuthorization);
        final ResourceAuthorization resourceAuthorization = resourceDeleteCancelEventData.getResourceAuthorization();
        assertEquals(SCOPE, resourceAuthorization.getScope());
        assertEquals(ACTION, resourceAuthorization.getAction());
        assertEquals(evidenceMap, resourceAuthorization.getEvidence());
    }

    @Test
    public void deprecatedClaims() {
        final ResourceDeleteCancelEventData resourceDeleteCancelEventData = new ResourceDeleteCancelEventData();
        // Deprecated getter/setter methods
        resourceDeleteCancelEventData.setClaims(EXPECTED_CLAIMS);
        assertEquals(EXPECTED_CLAIMS, resourceDeleteCancelEventData.getClaims());
        // New getter/setter methods
        resourceDeleteCancelEventData.setResourceClaims(CLAIM_MAP);
        final Map<String, String> resourceClaims = resourceDeleteCancelEventData.getResourceClaims();
        assertEquals(CLAIM_VALUE_1, resourceClaims.get(CLAIM_KEY_1));
        assertEquals(CLAIM_VALUE_2, resourceClaims.get(CLAIM_KEY_2));
    }

    @Test
    public void deprecatedHttpRequest() {
        final ResourceDeleteCancelEventData resourceDeleteCancelEventData = new ResourceDeleteCancelEventData();
        // Deprecated getter/setter methods
        resourceDeleteCancelEventData.setHttpRequest(EXPECTED_HTTP_REQUEST);
        final String httpRequest = resourceDeleteCancelEventData.getHttpRequest();
        assertEquals(EXPECTED_HTTP_REQUEST, httpRequest);
        // New getter/setter methods
        resourceDeleteCancelEventData.setResourceHttpRequest(resourceHttpRequest);
        final ResourceHttpRequest resourceHttpRequest = resourceDeleteCancelEventData.getResourceHttpRequest();
        assertEquals(REQUEST_ID, resourceHttpRequest.getClientRequestId());
        assertEquals(IP_ADDRESS, resourceHttpRequest.getClientIpAddress());
        assertEquals(METHOD, resourceHttpRequest.getMethod());
        assertEquals(HTTP_LOCAL_HOST, resourceHttpRequest.getUrl());
    }
}
