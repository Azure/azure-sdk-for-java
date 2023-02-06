// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c.registration;

/**
 * The interface for {@link AadB2cClientRegistrationRepositoryBuilder} that can customize
 * client registrations for Azure AD B2C OAuth2 login.
 */
@FunctionalInterface
public interface AadB2cClientRegistrationRepositoryBuilderCustomizer {

    /**
     * Customize the {@link AadB2cClientRegistrationRepositoryBuilder} for Azure AD B2C OAuth2 login.
     * @param builder the Azure AD client registration repository builder.
     */
    void customize(AadB2cClientRegistrationRepositoryBuilder builder);
}
