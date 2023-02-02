// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c.registration;

/**
 * The interface for {@link AadB2cClientRegistrationRepositoryBuilder} that can configure
 * client registrations for Azure AD B2C OAuth2 login.
 */
@FunctionalInterface
public interface AadB2cClientRegistrationRepositoryBuilderConfigurer {

    /**
     * Configure the {@link AadB2cClientRegistrationRepositoryBuilder} for Azure AD B2C OAuth2 login.
     * @return the Azure AD B2C client registration repository builder.
     */
    void configure(AadB2cClientRegistrationRepositoryBuilder builder);
}
