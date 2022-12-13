// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureTokenCredentialAutoConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@EnabledOnOs(OS.LINUX)
@SuppressWarnings("rawtypes")
public class PasswordlessMySQLTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withPropertyValues("spring.datasource.azure.passwordless-enabled=true")
        .withConfiguration(AutoConfigurations.of(
            DataSourceAutoConfiguration.class,
            JdbcTemplateAutoConfiguration.class,
            AzureJdbcAutoConfiguration.class,
            AzureGlobalPropertiesAutoConfiguration.class,
            AzureTokenCredentialAutoConfiguration.class
        ));

    @Container
    private static MySQLContainer mySQLContainer = new MySQLContainer("mysql:5.7");

    @BeforeEach
    void setUp() {
        DefaultAzureCredential build = new DefaultAzureCredentialBuilder().build();
        // the scopes vary in different Clouds
        String password = build.getToken(new TokenRequestContext().addScopes("https://ossrdbms-aad.database.windows"
                                   + ".net/.default"))
                               .block()
                               .getToken();
        mySQLContainer.withUsername("passwordless-test")
                      .withPassword(password);
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
    }

    @Test
    void testMysqlPasswordless() {
        this.contextRunner
            .run(context -> {
                String query1 = "CREATE TABLE Test ( PersonID int, Number int);";
                String query2 = "INSERT INTO Test VALUES (1,25);";
                String query3 = "SELECT * FROM Test;";
                JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
                jdbcTemplate.execute(query1);
                jdbcTemplate.execute(query2);
                SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(query3);
                while (sqlRowSet.next()) {
                    Assertions.assertEquals(sqlRowSet.getString("Number"), "25");
                }
            });
    }
}
