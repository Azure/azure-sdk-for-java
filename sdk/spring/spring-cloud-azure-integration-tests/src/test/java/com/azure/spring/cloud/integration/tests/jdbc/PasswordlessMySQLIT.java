package com.azure.spring.cloud.integration.tests.jdbc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("jdbc")
public class PasswordlessMySQLIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testKeyVaultSecretOperation() {
        String query = "show databases";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(query);
        StringBuilder sb = new StringBuilder();

        while (sqlRowSet.next()) {
            String database = sqlRowSet.getString("Database");
            System.out.println("database = " + database);
            System.out.println("database.equals(\"db\") = " + database.equals("db"));
        }
    }
}
