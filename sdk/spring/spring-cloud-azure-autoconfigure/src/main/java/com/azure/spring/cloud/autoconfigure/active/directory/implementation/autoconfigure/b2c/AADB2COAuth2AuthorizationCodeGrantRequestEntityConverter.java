// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.active.directory.implementation.autoconfigure.b2c;

import com.azure.spring.cloud.autoconfigure.active.directory.implementation.aad.AbstractOAuth2AuthorizationCodeGrantRequestEntityConverter;
import com.azure.spring.core.AzureSpringIdentifier;

/**
 * Used to set azure service header tag when use "auth-code" to get "access_token".
 */
public class AADB2COAuth2AuthorizationCodeGrantRequestEntityConverter
    extends AbstractOAuth2AuthorizationCodeGrantRequestEntityConverter {

    @Override
    protected String getApplicationId() {
        return AzureSpringIdentifier.AZURE_SPRING_B2C;
    }
}
