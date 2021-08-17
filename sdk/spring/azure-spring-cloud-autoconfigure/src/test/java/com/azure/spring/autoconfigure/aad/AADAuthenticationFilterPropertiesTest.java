// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.azure.identity.AzureAuthorityHosts;
import com.azure.spring.autoconfigure.unity.AzureProperties;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.ObjectError;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.support.TestPropertySourceUtils.addInlinedPropertiesToEnvironment;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AADAuthenticationFilterPropertiesTest {

    private static final String AAD_PROPERTY_PREFIX = "azure.activedirectory.";

    @Test
    public void canSetProperties() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            configureAllRequiredProperties(context);
            context.register(Config.class);
            context.refresh();

            final AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);

            assertThat(properties.getClientId()).isEqualTo(TestConstants.CLIENT_ID);
            assertThat(properties.getClientSecret()).isEqualTo(TestConstants.CLIENT_SECRET);
            assertThat(properties.getActiveDirectoryGroups()
                                 .toString()).isEqualTo(TestConstants.TARGETED_GROUPS.toString());
        }
    }

    @Test
    @Disabled
    public void loadPropertiesFromCredentialProperties() {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        addInlinedPropertiesToEnvironment(
            context,
            AzureProperties.PREFIX + ".tenant-id=azure-tenant-id",
            AzureProperties.PREFIX + ".client-id=azure-client-id",
            AzureProperties.PREFIX + ".authority-host=azure-authority-host",
            AzureProperties.PREFIX + ".environment=AzureGermany",
            AAD_PROPERTY_PREFIX + "client-id=" + TestConstants.CLIENT_ID,
            AAD_PROPERTY_PREFIX + "client-secret=" + TestConstants.CLIENT_SECRET
        );
        context.register(Config.class);
        context.refresh();

        final AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);

        assertThat(properties.getTenantId()).isEqualTo("azure-tenant-id");
        assertThat(properties.getClientId()).isEqualTo(TestConstants.CLIENT_ID);
        assertThat(properties.getClientSecret()).isEqualTo(TestConstants.CLIENT_SECRET);
        assertThat(properties.getBaseUri()).isEqualTo("azure-authority-host/");

    }

    @Test
    @Disabled
    public void testGetBaseUriFromEnvironment() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        addInlinedPropertiesToEnvironment(
            context,
            AzureProperties.PREFIX + ".environment=AzureGermany",
            AAD_PROPERTY_PREFIX + "tenant-id=azure-tenant-id",
            AAD_PROPERTY_PREFIX + "client-id=" + TestConstants.CLIENT_ID,
            AAD_PROPERTY_PREFIX + "client-secret=" + TestConstants.CLIENT_SECRET
        );
        context.register(Config.class);
        context.refresh();

        final AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);

        assertThat(properties.getBaseUri()).isEqualTo(AzureAuthorityHosts.AZURE_GERMANY);
    }

    private void configureAllRequiredProperties(AnnotationConfigApplicationContext context) {
        addInlinedPropertiesToEnvironment(
            context,
            AAD_PROPERTY_PREFIX + "tenant-id=demo-tenant-id",
            AAD_PROPERTY_PREFIX + "client-id=" + TestConstants.CLIENT_ID,
            AAD_PROPERTY_PREFIX + "client-secret=" + TestConstants.CLIENT_SECRET,
            AAD_PROPERTY_PREFIX + "user-group.allowed-groups="
                + TestConstants.TARGETED_GROUPS.toString().replace("[", "").replace("]", "")
        );
    }

    @Disabled
    @Test
    //TODO (wepa) clientId and clientSecret can also be configured in oauth2 config, test to be refactored
    public void emptySettingsNotAllowed() {
        System.setProperty("azure.activedirectory.client-id", "");
        System.setProperty("azure.activedirectory.client-secret", "");

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
                "Field error in object 'azure.activedirectory' on field 'activeDirectoryGroups': "
                    + "rejected value [null];",
                "Field error in object 'azure.activedirectory' on field 'clientId': rejected value [];",
                "Field error in object 'azure.activedirectory' on field 'clientSecret': rejected value [];"
            );

            Collections.sort(errorStrings);

            assertThat(errors.size()).isEqualTo(errorStringsExpected.size());

            for (int i = 0; i < errorStrings.size(); i++) {
                assertThat(errorStrings.get(i)).contains(errorStringsExpected.get(i));
            }
        }
    }

    @Configuration
    @EnableConfigurationProperties(AADAuthenticationProperties.class)
    static class Config {
    }
}

