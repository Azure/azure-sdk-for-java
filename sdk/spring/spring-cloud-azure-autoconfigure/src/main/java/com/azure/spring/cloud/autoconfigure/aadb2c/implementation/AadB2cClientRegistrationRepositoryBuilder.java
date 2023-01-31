// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c.implementation;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesRegistrationAdapter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;

/**
 * A representation of a OAuth2 Client registration for Azure AD B2C.
 */
public class AadB2cClientRegistrationRepositoryBuilder extends AbstractClientRegistrationRepositoryBuilder<AadB2cClientRegistrationRepository> {

    public void oAuth2ClientRegistrations(OAuth2ClientProperties oAuth2ClientProperties) {
        Collection<ClientRegistration> registrations =
            OAuth2ClientPropertiesRegistrationAdapter.getClientRegistrations(oAuth2ClientProperties).values();
        this.addSignInClientRegistrations(filterSignUpOrSignInClientRegistrations(registrations));
        this.addClientRegistrations(Arrays.asList(registrations.toArray(new ClientRegistration[0])));
    }

    @NotNull
    private List<ClientRegistration> filterSignUpOrSignInClientRegistrations(Collection<ClientRegistration> clientRegistrations) {
        return clientRegistrations.stream()
                                  .filter(client -> AUTHORIZATION_CODE.equals(client.getAuthorizationGrantType()))
                                  .collect(Collectors.toList());
    }

    public AadB2cClientRegistrationRepositoryBuilderConfigurer b2cClientRegistration() {
        return new AadB2cClientRegistrationRepositoryBuilderConfigurer();
    }

    /**
     * 初始化AadB2cProperties中customer application level的配置，即不是user flow的配置
     * @throws Exception exception before initialization.
     */
    @Override
    protected void beforeInit() throws Exception {
        super.beforeInit();
//        AadB2cProperties b2cProperties = getApplicationContext().getBean(AadB2cProperties.class);
    }

    @Override
    protected AadB2cClientRegistrationRepository performBuild() {
        return new AadB2cClientRegistrationRepository(getSignInClientRegistrations(), getAllClientRegistrations());
    }
}
