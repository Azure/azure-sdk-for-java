// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.SpringTokenCredentialProviderContextProvider;
import com.azure.spring.cloud.service.implementation.identity.AuthProperty;
import com.azure.spring.cloud.service.implementation.identity.AzureAuthenticationTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AzureJdbcAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                                            .withConfiguration(AutoConfigurations.of(AzureJdbcAutoConfiguration.class,
                                                AzureGlobalProperties.class));

    @Test
    void testHasSingleBean() {
        this.contextRunner
            .withBean(DataSourceProperties.class, () -> mock(DataSourceProperties.class))
            .run((context) -> {
                assertThat(context).hasSingleBean(DataSourceProperties.class);
                assertThat(context).hasSingleBean(JdbcPropertiesBeanPostProcessor.class);
                assertThat(context).hasSingleBean(SpringTokenCredentialProviderContextProvider.class);
            });
    }

    @Test
    void testNoAzureAuthenticationTemplate() {
        this.contextRunner
            .withBean(DataSourceProperties.class, () -> mock(DataSourceProperties.class))
            .withClassLoader(new FilteredClassLoader(AzureAuthenticationTemplate.class))
            .run((context) -> {
                assertThat(context).doesNotHaveBean(JdbcPropertiesBeanPostProcessor.class);
                assertThat(context).doesNotHaveBean(SpringTokenCredentialProviderContextProvider.class);
            });
    }

    @Test
    void testNoDataSourcePropertiesBean() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(AzureAuthenticationTemplate.class))
            .run((context) -> {
                assertThat(context).doesNotHaveBean(JdbcPropertiesBeanPostProcessor.class);
                assertThat(context).doesNotHaveBean(SpringTokenCredentialProviderContextProvider.class);
            });
    }

    @Test
    void testPostgreSqlPluginOnClassPath() throws Exception {
        DataSourceProperties properties = new DataSourceProperties();
        properties.setUrl("jdbc:postgresql://postgre:5432/test");
        properties.afterPropertiesSet();
        this.contextRunner
            .withBean(DataSourceProperties.class, () -> properties)
            .withClassLoader(new FilteredClassLoader("com.mysql.cj.protocol.AuthenticationPlugin"))
            .run((context) -> {
                DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);
                assertTrue(isEnhancedUrl(dataSourceProperties.getUrl()));
                assertNotEquals("jdbc:postgresql://postgre:5432/test", dataSourceProperties.getUrl());
            });
    }

    @Test
    void testNoPostgreSqlPluginOnClassPath() throws Exception {
        DataSourceProperties properties = new DataSourceProperties();
        properties.setUrl("jdbc:postgresql://postgre:5432/test");
        properties.afterPropertiesSet();
        this.contextRunner
            .withBean(DataSourceProperties.class, () -> properties)
            .withClassLoader(new FilteredClassLoader("org.postgresql.plugin.AuthenticationPlugin"))
            .run((context) -> {
                DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);
                assertFalse(isEnhancedUrl(dataSourceProperties.getUrl()));
                assertEquals("jdbc:postgresql://postgre:5432/test", dataSourceProperties.getUrl());
            });
    }

    @Test
    void testMySqlPluginOnClassPath() throws Exception {
        DataSourceProperties properties = new DataSourceProperties();
        properties.setUrl("jdbc:mysql://mysql:1234/test");
        properties.afterPropertiesSet();
        this.contextRunner
            .withBean(DataSourceProperties.class, () -> properties)
            .withClassLoader(new FilteredClassLoader("org.postgresql.plugin.AuthenticationPlugin"))
            .run((context) -> {
                DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);
                assertTrue(isEnhancedUrl(dataSourceProperties.getUrl()));
                assertNotEquals("jdbc:mysql://mysql:1234/test", dataSourceProperties.getUrl());
            });
    }

    @Test
    void testWrongPostgreSqlUrl() throws Exception {
        DataSourceProperties properties = new DataSourceProperties();
        properties.setUrl("jdbc:postgr://postgre:5432/test");
        properties.afterPropertiesSet();
        this.contextRunner
            .withBean(DataSourceProperties.class, () -> properties)
            .run((context) -> {
                DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);
                assertFalse(isEnhancedUrl(dataSourceProperties.getUrl()));
                assertEquals("jdbc:postgr://postgre:5432/test", dataSourceProperties.getUrl());
            });
    }

    @Test
    void testUnSupportDatabaseType() throws Exception {
        DataSourceProperties properties = new DataSourceProperties();
        properties.setUrl("jdbc:h2:~/test,sa,password");
        properties.afterPropertiesSet();
        this.contextRunner
            .withBean(DataSourceProperties.class, () -> properties)
            .run((context) -> {
                DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);
                assertFalse(isEnhancedUrl(dataSourceProperties.getUrl()));
                assertEquals("jdbc:h2:~/test,sa,password", dataSourceProperties.getUrl());
            });
    }

    @Test
    void testEnhancedUrl() throws Exception {
        DataSourceProperties properties = new DataSourceProperties();
        properties.setUrl("jdbc:postgresql://postgre:5432/test");
        properties.afterPropertiesSet();
        this.contextRunner
            .withBean(DataSourceProperties.class, () -> properties)
            .run((context) -> {
                DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);
                assertTrue(isEnhancedUrl(dataSourceProperties.getUrl()));
            });
    }

    private boolean isEnhancedUrl(String url) {
        if (!url.contains(AuthProperty.TENANT_ID.getPropertyKey())) {
            return false;
        }
        if (!url.contains(AuthProperty.MANAGED_IDENTITY_ENABLED.getPropertyKey())) {
            return false;
        }
        if (!url.contains(AuthProperty.CACHE_ENABLED.getPropertyKey())) {
            return false;
        }
        if (!url.contains(AuthProperty.AUTHORITY_HOST.getPropertyKey())) {
            return false;
        }
        return true;
    }
}
