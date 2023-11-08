// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aadb2c.implementation;

import com.azure.spring.cloud.autoconfigure.aad.implementation.oauth2.AbstractOAuth2AuthorizationCodeGrantRequestEntityConverter;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;

/**
 * Used to set azure service header tag when use "auth-code" to get "access_token".
 */
public class AadB2cOAuth2AuthorizationCodeGrantRequestEntityConverter
    extends AbstractOAuth2AuthorizationCodeGrantRequestEntityConverter {

    @Override
    protected String getApplicationId() {
        return AzureSpringIdentifier.AZURE_SPRING_B2C;
    }
}
