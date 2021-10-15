// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.microsoft.aad.msal4j.AuthenticationErrorCode;
import com.microsoft.aad.msal4j.ClaimsRequest;
import com.microsoft.aad.msal4j.MsalClientException;
import com.microsoft.aad.msal4j.RequestedClaimAdditionalInfo;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class CustomClaimRequest extends ClaimsRequest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public CustomClaimRequest() {
        super();
    }

    @Override
    protected void requestClaimInAccessToken(String claim, RequestedClaimAdditionalInfo requestedClaimAdditionalInfo) {
        super.requestClaimInAccessToken(claim, requestedClaimAdditionalInfo);
    }


    /**
     * Creates an instance of ClaimsRequest from a JSON-formatted String which follows the specification for the OIDC claims request parameter
     *
     * @param claims a String following JSON formatting
     * @return a ClaimsRequest instance
     */
    public static ClaimsRequest formatAsClaimsRequest(String claims) {
        try {
            CustomClaimRequest cr = new CustomClaimRequest();

            ObjectReader reader = MAPPER.readerFor(new TypeReference<List<String>>() { });
            JsonNode jsonClaims = MAPPER.readTree(claims);
            addClaimsFromJsonNode(jsonClaims.get("id_token"), "id_token", cr, reader);
            addClaimsFromJsonNode(jsonClaims.get("userinfo"), "userinfo", cr, reader);
            addClaimsFromJsonNode(jsonClaims.get("access_token"), "access_token", cr, reader);

            return cr;
        } catch (IOException e) {
            throw new MsalClientException("Could not convert string to ClaimsRequest: " + e.getMessage(), AuthenticationErrorCode.INVALID_JSON);
        }
    }

    private static void addClaimsFromJsonNode(JsonNode claims, String group, CustomClaimRequest cr, ObjectReader reader) throws IOException {
        Iterator<String> claimsIterator;

        if (claims != null) {
            claimsIterator = claims.fieldNames();
            while (claimsIterator.hasNext()) {
                String claim = claimsIterator.next();
                Boolean essential = null;
                String value = null;
                List<String> values = null;
                RequestedClaimAdditionalInfo claimInfo = null;

                if (claims.get(claim).has("essential")) {
                    essential = claims.get(claim).get("essential").asBoolean();
                }
                if (claims.get(claim).has("value")) {
                    value = claims.get(claim).get("value").textValue();
                }
                if (claims.get(claim).has("values")) {
                    values = reader.readValue(claims.get(claim).get("values"));
                }

                //'null' is a valid value for RequestedClaimAdditionalInfo, so only initialize it if one of the parameters is not null
                if (essential != null || value != null || values != null) {
                    claimInfo = new RequestedClaimAdditionalInfo(essential == null ? false : essential, value, values);
                }

                if (group.equals("id_token")) {
                    cr.requestClaimInIdToken(claim, claimInfo);
                }
                if (group.equals("access_token")) {
                    cr.requestClaimInAccessToken(claim, claimInfo);
                }
            }
        }
    }

}
