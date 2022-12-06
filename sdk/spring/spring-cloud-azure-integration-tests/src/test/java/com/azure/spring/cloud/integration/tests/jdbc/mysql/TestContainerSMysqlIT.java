package com.azure.spring.cloud.integration.tests.jdbc.mysql;

import com.azure.core.credential.TokenRequestContext;
import com.azure.cosmos.implementation.guava25.base.Joiner;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.jdbc.AzureJdbcAutoConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.List;

//@DisabledIfEnvironmentVariable(named = "AZURE_MYSQL_IT_SKIPRUNNING", matches = "skipRunning")
@Testcontainers
public class TestContainerSMysqlIT {
    String VALUE = "information_schema,test";

    private static DockerImageName dockerImageName = DockerImageName.parse("mysql:5.7");

    @Container
    public static MySQLContainer mySQLContainer = new MySQLContainer(dockerImageName);


    @BeforeEach
    void setUp() {
        DefaultAzureCredential build = new DefaultAzureCredentialBuilder().build();
        // the scopes vary in different Clouds
        String password = build.getToken(new TokenRequestContext().addScopes("https://ossrdbms-aad.database.windows.net/.default"))
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
    void test() {
        List<String> list = new ArrayList<>();
        list.add("25");
        System.out.println(Joiner.on(",").join(list));
    }
    @Test
    void testMysqlPassword() {
        System.out.println("111"+mySQLContainer.getJdbcUrl());
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("spring.datasource.azure.passwordless-enabled=true")
            .withConfiguration(
                AutoConfigurations.of(DataSourceAutoConfiguration.class, JdbcTemplateAutoConfiguration.class,
                    AzureJdbcAutoConfiguration.class, AzureGlobalPropertiesAutoConfiguration.class,
                    AzureTokenCredentialAutoConfiguration.class));
        contextRunner.run((context -> {
            String query1 = "CREATE TABLE Test ( PersonID int, Number int);";
            String query2 = "INSERT INTO Test VALUES (1,25);";
            String query3 = "SELECT * FROM Test;";
            JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
            jdbcTemplate.execute(query1);
            jdbcTemplate.execute(query2);
            SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(query3);
            while (sqlRowSet.next()) {
                Assertions.assertEquals(sqlRowSet.getString("Number"),"25");
            }
        }));
    }

}
