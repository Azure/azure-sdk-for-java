package com.azure.spring.cloud.integration.tests.jdbc.mysql;

import com.azure.core.credential.TokenRequestContext;
import com.azure.cosmos.implementation.guava25.base.Joiner;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.List;

@DisabledIfEnvironmentVariable(named = "AZURE_MYSQL_IT_SKIPRUNNING", matches = "skipRunning")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ActiveProfiles("jdbc-mysql-tc")
public class TestContainerSMysqlIT {
    String VALUE = "information_schema,test";

    @Autowired
    private JdbcTemplate jdbcTemplate;
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
//        registry.add("spring.datasource.password", mySQLContainer::getPassword);
//        registry.add("spring.datasource.username", mySQLContainer::getUsername);
    }
    @Test
    void testMysqlPassword() {
        String query = "show databases";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(query);

        List<String> list = new ArrayList<>();
        while (sqlRowSet.next()) {
            list.add(sqlRowSet.getString("Database"));
        }
        Assertions.assertEquals(Joiner.on(",").join(list), VALUE);
    }


}
