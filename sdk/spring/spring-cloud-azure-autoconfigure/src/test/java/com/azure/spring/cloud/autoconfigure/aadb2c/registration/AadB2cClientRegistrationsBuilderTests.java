// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c.registration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class AadB2cClientRegistrationsBuilderTests {

    private AadB2cClientRegistrationsBuilder builder;
    @BeforeEach
    void init() {
        builder = new AadB2cClientRegistrationsBuilder();
    }
    @Test
    void setNullBaseUri() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> builder.build());
    }

    @Test
    void setInvalidBaseUri() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> builder.baseUri("https://xxx.b2clogin.com/xxx.onmicrosoft.com/?/").build());
    }

    @Test
    void setNullClientIdOrClientSecret() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> builder.baseUri("https://xxx.b2clogin.com/xxx.onmicrosoft.com/").build());
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> builder.baseUri("https://xxx.b2clogin.com/xxx.onmicrosoft.com/").clientId("xxx").build());
    }

    @Test
    void setNullSignInUserFlow() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> builder.baseUri("https://xxx.b2clogin.com/xxx.onmicrosoft.com/")
                         .clientId("xxx")
                         .clientSecret("xxx")
                         .build());
    }

    @Test
    void setDuplicateSignInUserFlowId() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> builder.baseUri("https://xxx.b2clogin.com/xxx.onmicrosoft.com/")
                         .clientId("xxx")
                         .clientSecret("xxx")
                         .signInUserFlow("xxx")
                         .userFlows("xxx", "yyy")
                         .build());
    }

    @Test
    void setAuthorizationClientsWithNonClientCredentialsGrantType() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> builder.baseUri("https://xxx.b2clogin.com/xxx.onmicrosoft.com/")
                         .clientId("xxx")
                         .clientSecret("xxx")
                         .signInUserFlow("xxx")
                         .userFlows("yyy")
                         .authorizationClient("zzz", AUTHORIZATION_CODE, "http://localhost/xxx/.default")
                         .build());
    }

    @Test
    void setAuthorizationClientsWithoutSettingTenantId() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> builder.baseUri("https://xxx.b2clogin.com/xxx.onmicrosoft.com/")
                         .clientId("xxx")
                         .clientSecret("xxx")
                         .signInUserFlow("xxx")
                         .userFlows("yyy")
                         .authorizationClient("zzz", CLIENT_CREDENTIALS, "http://localhost/xxx/.default")
                         .build());
    }

    @Test
    void buildClientRegistrations() {
        AadB2cClientRegistrations b2cClientRegistrations =
            builder.baseUri("https://xxx.b2clogin.com/xxx.onmicrosoft.com/")
                   .clientId("xxx")
                   .clientSecret("xxx")
                   .tenantId("xxx")
                   .signInUserFlow("xxx")
                   .userFlows("yyy")
                   .authorizationClient("zzz", CLIENT_CREDENTIALS, "http://localhost/xxx/.default")
                   .build();
        Assertions.assertEquals(b2cClientRegistrations.getClientRegistrations().size(), 3);
        Assertions.assertEquals(b2cClientRegistrations.getNonSignInClientRegistrationIds().size(), 2);
    }
}
