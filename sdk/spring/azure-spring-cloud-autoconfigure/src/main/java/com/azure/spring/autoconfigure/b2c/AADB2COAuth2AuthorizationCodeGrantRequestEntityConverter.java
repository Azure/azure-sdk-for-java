// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.b2c;

import com.azure.spring.aad.AbstractOAuth2AuthorizationCodeGrantRequestEntityConverter;
import com.azure.spring.core.ApplicationId;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;

/**
 * Used to set azure service header tag when use "auth-code" to get "access_token".
 */
public class AADB2COAuth2AuthorizationCodeGrantRequestEntityConverter
    extends AbstractOAuth2AuthorizationCodeGrantRequestEntityConverter {

    @Override
    protected String getApplicationId() {
        return ApplicationId.AZURE_SPRING_B2C;
    }
}
