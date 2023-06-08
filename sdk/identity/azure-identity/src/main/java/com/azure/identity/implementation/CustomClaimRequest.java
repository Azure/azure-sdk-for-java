// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.microsoft.aad.msal4j.AuthenticationErrorCode;
import com.microsoft.aad.msal4j.ClaimsRequest;
import com.microsoft.aad.msal4j.MsalClientException;
import com.microsoft.aad.msal4j.RequestedClaimAdditionalInfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CustomClaimRequest extends ClaimsRequest {
    public CustomClaimRequest() {
        super();
    }

    @Override
    protected void requestClaimInAccessToken(String claim, RequestedClaimAdditionalInfo requestedClaimAdditionalInfo) {
        super.requestClaimInAccessToken(claim, requestedClaimAdditionalInfo);
    }


    /**
     * Creates an instance of ClaimsRequest from a JSON-formatted String which follows the specification for the OIDC
     * claims request parameter
     *
     * @param claims a String following JSON formatting
     * @return a ClaimsRequest instance
     */
    public static ClaimsRequest formatAsClaimsRequest(String claims) {
        try (JsonReader jsonReader = JsonProviders.createReader(claims)) {
            CustomClaimRequest cr = new CustomClaimRequest();
            Map<String, Object> jsonClaims = jsonReader.readMap(JsonReader::readUntyped);

            addClaimsFromJsonNode(jsonClaims.get("id_token"), "id_token", cr);
            addClaimsFromJsonNode(jsonClaims.get("userinfo"), "userinfo", cr);
            addClaimsFromJsonNode(jsonClaims.get("access_token"), "access_token", cr);

            return cr;
        } catch (IOException e) {
            throw new MsalClientException("Could not convert string to ClaimsRequest: " + e.getMessage(), AuthenticationErrorCode.INVALID_JSON);
        }
    }

    @SuppressWarnings("unchecked")
    private static void addClaimsFromJsonNode(Object claims, String group, CustomClaimRequest cr) throws IOException {
        if (!(claims instanceof Map<?, ?>)) {
            return;
        }
        Map<String, Object> claimsMap = (Map<String, Object>) claims;
        for (Map.Entry<String, Object> claimEntry : claimsMap.entrySet()) {
            if (!(claimEntry.getValue() instanceof Map<?, ?>)) {
                continue;
            }
            Map<String, Object> claimMap = (Map<String, Object>) claimEntry.getValue();
            String claim = claimEntry.getKey();
            Boolean essential = (Boolean) claimMap.getOrDefault("essential", null);
            String value = Objects.toString(claimMap.get("value"), null);
            List<String> values = (List<String>) claimMap.get("values");
            RequestedClaimAdditionalInfo claimInfo = null;

            //'null' is a valid value for RequestedClaimAdditionalInfo, so only initialize it if one of the parameters is not null
            if (essential != null || value != null || values != null) {
                claimInfo = new RequestedClaimAdditionalInfo(essential != null && essential, value, values);
            }

            if ("id_token".equals(group)) {
                cr.requestClaimInIdToken(claim, claimInfo);
            }
            if ("access_token".equals(group)) {
                cr.requestClaimInAccessToken(claim, claimInfo);
            }
        }
    }

}
