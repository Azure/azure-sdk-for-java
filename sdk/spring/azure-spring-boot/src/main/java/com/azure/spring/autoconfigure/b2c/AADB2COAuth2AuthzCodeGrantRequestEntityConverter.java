// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.b2c;

import com.azure.spring.common.OAuth2AuthzCodeGrantRequestEntityConverter;
import com.azure.spring.utils.ApplicationId;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;

/**
 * Used to set azure service header tag when use "auth-code" to get "access_token".
 */
public class AADB2COAuth2AuthzCodeGrantRequestEntityConverter
    extends OAuth2AuthzCodeGrantRequestEntityConverter {

    public AADB2COAuth2AuthzCodeGrantRequestEntityConverter() {
        this.azureModule = ApplicationId.AZURE_SPRING_B2C;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest request) {
        return super.convert(request);
    }
}
