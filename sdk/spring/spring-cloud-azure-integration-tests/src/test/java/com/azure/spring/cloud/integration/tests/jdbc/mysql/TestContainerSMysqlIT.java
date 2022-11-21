package com.azure.spring.cloud.integration.tests.jdbc.mysql;

import com.azure.core.credential.TokenRequestContext;
import com.azure.cosmos.implementation.guava25.base.Joiner;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class TestContainerSMysqlIT {
    private DockerImageName dockerImageName = DockerImageName.parse("mysql");

    @Container
    public MySQLContainer mySQLContainer = new MySQLContainer(dockerImageName);


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

    @Test
    void testMysqlPassword() {
        // add your
        String query = "show databases";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(query);

        List<String> list = new ArrayList<>();
        while (sqlRowSet.next()) {
            list.add(sqlRowSet.getString("Database"));
        }
        Assertions.assertEquals(Joiner.on(",").join(list), VALUE);
    }


}
