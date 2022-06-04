// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.filter;

import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.aad.configuration.AadPropertiesConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindException;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.validation.ObjectError;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.support.TestPropertySourceUtils.addInlinedPropertiesToEnvironment;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AadAuthenticationFilterPropertiesTests {

    private static final String AAD_PROPERTY_PREFIX = "spring.cloud.azure.active-directory.";

    @Test
    public void canSetProperties() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            configureAllRequiredProperties(context);
            context.register(Config.class);
            context.refresh();

            final AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);

            assertThat(properties.getCredential().getClientId()).isEqualTo(TestConstants.CLIENT_ID);
            assertThat(properties.getCredential().getClientSecret()).isEqualTo(TestConstants.CLIENT_SECRET);
            assertThat(properties.getUserGroup().getAllowedGroupNames()
                                 .toString()).isEqualTo(TestConstants.TARGETED_GROUPS.toString());
        }
    }

    private void configureAllRequiredProperties(AnnotationConfigApplicationContext context) {
        addInlinedPropertiesToEnvironment(
            context,
            AAD_PROPERTY_PREFIX + "profile.tenant-id=demo-tenant-id",
            AAD_PROPERTY_PREFIX + "credential.client-id=" + TestConstants.CLIENT_ID,
            AAD_PROPERTY_PREFIX + "credential.client-secret=" + TestConstants.CLIENT_SECRET,
            AAD_PROPERTY_PREFIX + "user-group.allowed-group-names="
                + TestConstants.TARGETED_GROUPS.toString().replace("[", "").replace("]", "")
        );
    }

    @Disabled
    @Test
    //TODO (wepa) clientId and clientSecret can also be configured in oauth2 config, test to be refactored
    public void emptySettingsNotAllowed() {
        System.setProperty("spring.cloud.azure.active-directory.credential.client-id", "");
        System.setProperty("spring.cloud.azure.active-directory.credential.client-secret", "");

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            Exception exception = null;

            context.register(Config.class);

            try {
                context.refresh();
            } catch (Exception e) {
                exception = e;
            }

            assertThat(exception).isNotNull();
            assertThat(exception).isExactlyInstanceOf(ConfigurationPropertiesBindException.class);

            final BindValidationException bindException = (BindValidationException) exception.getCause().getCause();
            final List<ObjectError> errors = bindException.getValidationErrors().getAllErrors();

            final List<String> errorStrings = errors.stream().map(ObjectError::toString).collect(Collectors.toList());

            final List<String> errorStringsExpected = Arrays.asList(
                "Field error in object 'spring.cloud.azure.active-directory' on field 'activeDirectoryGroups': "
                    + "rejected value [null];",
                "Field error in object 'spring.cloud.azure.active-directory' on field 'clientId': rejected value [];",
                "Field error in object 'spring.cloud.azure.active-directory' on field 'clientSecret': rejected value [];"
            );

            Collections.sort(errorStrings);

            assertThat(errors.size()).isEqualTo(errorStringsExpected.size());

            for (int i = 0; i < errorStrings.size(); i++) {
                assertThat(errorStrings.get(i)).contains(errorStringsExpected.get(i));
            }
        }
    }

    @Configuration
    @Import({ AadPropertiesConfiguration.class, AzureGlobalPropertiesAutoConfiguration.class })
    static class Config {
    }
}

