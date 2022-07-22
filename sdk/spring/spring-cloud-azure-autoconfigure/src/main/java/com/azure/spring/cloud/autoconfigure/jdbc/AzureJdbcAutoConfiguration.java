// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import javax.sql.DataSource;

import org.springframework.util.StringUtils;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AzureJdbcProperties.class)
@ConditionalOnProperty(value = "spring.datasource.url", matchIfMissing = true)
public class AzureJdbcAutoConfiguration {

    @Value("${spring.datasource.url}")
    private String jdbcurl;

    @Bean
    static JdbcPropertiesBeanPostProcessor jdbcConfigurationPropertiesBeanPostProcessor(
           AzureGlobalProperties azureGlobalProperties) {
        return new JdbcPropertiesBeanPostProcessor(azureGlobalProperties);
    }
    /***
     *
     * ActiveDirectoryMSI
     * ActiveDirectoryServicePrincipal
     * @param azureGlobalProperties
     * @return
     */
    @Bean
    @Primary
    @ConditionalOnClass(SQLServerDataSource.class)
    @ConditionalOnProperty(prefix = "spring.datasource", value = {"azure.credential.client-id", "url"})
    @ConditionalOnAnyProperty(prefix = "spring.datasource.azure.credential", name = {"client-secret", "managed-identity-enabled"})
    @ConditionalOnExpression("'${spring.datasource.url}'.contains('jdbc:sqlserver://')")
    DataSource dataSource(AzureJdbcProperties azureGlobalProperties) {
        JdbcConnectionString jdbcConnectionString = new JdbcConnectionString(jdbcurl);
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setServerName(jdbcConnectionString.getProperty("servername"));
        ds.setDatabaseName(jdbcConnectionString.getProperty("database"));
        ds.setUser(azureGlobalProperties.getCredential().getClientId());
        String port = jdbcConnectionString.getProperty("port");
        if (StringUtils.hasText(port)) {
            ds.setPortNumber(Integer.parseInt(port));
        }
        if (azureGlobalProperties.getCredential().isManagedIdentityEnabled()) {
            ds.setAuthentication("ActiveDirectoryMSI");
        } else {
            ds.setPassword(azureGlobalProperties.getCredential().getClientSecret());
            ds.setAuthentication("ActiveDirectoryServicePrincipal");
        }
        return ds;
    }

}
