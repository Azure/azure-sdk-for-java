// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.b2c;

import com.azure.spring.aad.AbstractOAuth2AuthorizationCodeGrantRequestEntityConverter;
import com.azure.spring.utils.ApplicationId;

/**
 * Used to set azure service header tag when use "auth-code" to get "access_token".
 *
 * @deprecated All Azure AD B2C features supported by Spring security, please refer to https://github.com/zhichengliu12581/azure-spring-boot-samples/blob/add-samples-for-aad-b2c-with-only-spring-security/aad/aad-b2c-with-spring-security/README.adoc
 */
@Deprecated
public class AADB2COAuth2AuthorizationCodeGrantRequestEntityConverter
    extends AbstractOAuth2AuthorizationCodeGrantRequestEntityConverter {

    @Override
    protected String getApplicationId() {
        return ApplicationId.AZURE_SPRING_B2C;
    }
}
