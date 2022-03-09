// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.microsoft.aad.msal4j.AuthenticationErrorCode;
import com.microsoft.aad.msal4j.ClaimsRequest;
import com.microsoft.aad.msal4j.MsalClientException;
import com.microsoft.aad.msal4j.RequestedClaimAdditionalInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomClaimRequest extends ClaimsRequest {
    private static final JsonFactory JSON_FACTORY = JsonFactory.builder().build();

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
        try (JsonParser jsonClaims = JSON_FACTORY.createParser(claims)) {
            CustomClaimRequest cr = new CustomClaimRequest();

            if (jsonClaims.currentToken() == null) {
                jsonClaims.nextToken();
            }

            String fieldName;
            while ((fieldName = jsonClaims.nextFieldName()) != null) {
                switch (fieldName) {
                    case "id_token":
                    case "userinfo":
                    case "access_token":
                        jsonClaims.nextToken();
                        addClaimsFromJsonNode(jsonClaims, fieldName, cr);
                        break;

                    default:
                        if (jsonClaims.nextToken().isStructStart()) {
                            jsonClaims.skipChildren();
                        } else {
                            jsonClaims.nextToken();
                        }

                        break;
                }
            }

            return cr;
        } catch (IOException e) {
            throw new MsalClientException("Could not convert string to ClaimsRequest: " + e.getMessage(),
                AuthenticationErrorCode.INVALID_JSON);
        }
    }

    private static void addClaimsFromJsonNode(JsonParser claims, String group, CustomClaimRequest cr)
        throws IOException {

        // The following is an example of a claim.
        //
        // {
        //   "userinfo": {
        //     "given_name": {"essential": true},
        //     "nickname": null,
        //     "email": {"essential": true},
        //     "email_verified": {"essential": true},
        //     "picture": null,
        //     "http://example.info/claims/groups": null
        //   },
        //   "id_token": {
        //     "auth_time": {"essential": true},
        //     "acr": {"values": ["urn:mace:incommon:iap:silver"] }
        //   }
        // }

        // Loop through the claims in either the 'access_token', 'id_token', or 'userinfo' section and parse out the
        // field name and essential, value, and values properties.
        //
        // 'essential' is an optional boolean
        // 'value' is an optional String
        // 'values' is an optional array of Strings

        // 'claims' will begin on the START_OBJECT token of 'access_token', 'id_token', or 'userinfo'.
        //
        // Iterate until the 'access_token', 'id_token', or 'userinfo' object ends.
        while (claims.nextToken() != JsonToken.END_OBJECT) {
            // Should always begin on a field name.
            String claimName = claims.currentName();
            claims.nextToken();

            RequestedClaimAdditionalInfo claimInfo = null;

            // Claim should have one or more of 'essential', 'value', or 'values'.
            if (claims.currentToken() == JsonToken.START_OBJECT) {
                claimInfo = parseClaimAdditionalInfo(claims);
            }

            if ("id_token".equals(group)) {
                cr.requestClaimInIdToken(claimName, claimInfo);
            }

            if ("access_token".equals(group)) {
                cr.requestClaimInAccessToken(claimName, claimInfo);
            }
        }
    }

    private static RequestedClaimAdditionalInfo parseClaimAdditionalInfo(JsonParser parser)
        throws IOException {
        boolean essential = false;
        String value = null;
        List<String> values = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String claimPropertyName = parser.currentName();
            parser.nextToken();

            switch (claimPropertyName) {
                case "essential":
                    essential = parser.getBooleanValue();
                    break;

                case "value":
                    value = parser.getText();
                    break;

                case "values":
                    values = new ArrayList<>();
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        values.add(parser.getText());
                    }
                    break;

                default:
                    // If the current token is an array or object start skip all children tokens.
                    // 'skipChildren' halts on the end array or end object token, so the next token should either be
                    // a field name or completion of the additional claim info.
                    if (parser.currentToken().isStructStart()) {
                        parser.skipChildren();
                    }
                    break;

            }
        }

        return new RequestedClaimAdditionalInfo(essential, value, values);
    }
}
