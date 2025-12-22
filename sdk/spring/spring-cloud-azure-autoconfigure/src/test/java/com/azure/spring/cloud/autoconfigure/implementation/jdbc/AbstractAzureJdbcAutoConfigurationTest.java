// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.extensions.implementation.credential.TokenCredentialProviderOptions;
import com.azure.identity.extensions.implementation.enums.AuthProperty;
import com.azure.identity.extensions.implementation.template.AzureAuthenticationTemplate;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.context.SpringTokenCredentialProviderApplicationRunListener;
import com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

abstract class AbstractAzureJdbcAutoConfigurationTest {

    public static final String PUBLIC_AUTHORITY_HOST_STRING = AuthProperty.AUTHORITY_HOST.getPropertyKey() + "=" + "https://login.microsoftonline.com/";
    public static final String PUBLIC_TOKEN_CREDENTIAL_BEAN_NAME_STRING = AuthProperty.TOKEN_CREDENTIAL_BEAN_NAME.getPropertyKey() + "=" + "passwordlessTokenCredential";
    abstract void pluginNotOnClassPath();
    abstract void wrongJdbcUrl();
    abstract void enhanceUrlWithDefaultCredential();
    abstract void enhanceUrlWithCustomCredential();

    protected final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureJdbcAutoConfiguration.class,
            AzureTokenCredentialAutoConfiguration.class,
            AzureGlobalPropertiesAutoConfiguration.class,
            DataSourceAutoConfiguration.class));

    @Test
    void testEnhanceUrlDefaultCredential() {
        enhanceUrlWithDefaultCredential();
    }

    @Test
    void testEnhanceUrlWithCustomCredential() {
        enhanceUrlWithCustomCredential();
    }

    @Test
    void testJdbcPluginNotOnClassPath() {
        pluginNotOnClassPath();
    }

    @Test
    void testWrongJdbcUrl() {
        wrongJdbcUrl();
    }

    @Test
    void testHasSingleBean() {
        try (MockedStatic<SpringTokenCredentialProvider> mockedStatic = Mockito.mockStatic(SpringTokenCredentialProvider.class)) {
            mockedStatic.when(() -> SpringTokenCredentialProvider.setGlobalApplicationContext(any(ApplicationContext.class)))
                        .thenAnswer(invocation -> null);
            this.contextRunner
                .withBean("test", DefaultAzureCredential.class)
                .withInitializer((context) -> new SpringTokenCredentialProviderApplicationRunListener().contextPrepared(context))
                .run((context) -> {
                    assertThat(context).hasSingleBean(JdbcPropertiesBeanPostProcessor.class);
                    mockedStatic.verify(() -> SpringTokenCredentialProvider.setGlobalApplicationContext(any()));
                });
        }
    }

    @Test
    void testNoAzureAuthenticationTemplate() {
        this.contextRunner
            .withBean("test", DefaultAzureCredential.class)
            .withClassLoader(new FilteredClassLoader(AzureAuthenticationTemplate.class))
            .withInitializer((context) -> new SpringTokenCredentialProviderApplicationRunListener().contextPrepared(context))
            .run((context) -> {
                assertThat(context).doesNotHaveBean(JdbcPropertiesBeanPostProcessor.class);

                TokenCredentialProviderOptions options = new TokenCredentialProviderOptions();
                options.setTokenCredentialBeanName("test");
                options.setTokenCredentialProviderClassName(SpringTokenCredentialProvider.class.getName());
                SpringTokenCredentialProvider springTokenCredentialProvider = new SpringTokenCredentialProvider(options);
                assertThrows(NullPointerException.class, springTokenCredentialProvider::get);
            });
    }

    @Test
    void testNoDataSourcePropertiesBean() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(AzureAuthenticationTemplate.class))
            .run((context) -> {
                assertThat(context).doesNotHaveBean(JdbcPropertiesBeanPostProcessor.class);
            });
    }

    @Test
    void testUnSupportDatabaseType() {
        this.contextRunner
            .withPropertyValues("spring.datasource.url = jdbc:h2:~/test,sa,password")
            .run((context) -> {
                DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);
                assertEquals("jdbc:h2:~/test,sa,password", dataSourceProperties.getUrl());
            });
    }

}
