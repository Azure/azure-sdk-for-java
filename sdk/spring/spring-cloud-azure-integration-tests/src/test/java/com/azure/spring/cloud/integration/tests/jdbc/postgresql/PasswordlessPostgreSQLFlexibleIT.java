// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.integration.tests.jdbc.postgresql;

import com.azure.cosmos.implementation.guava25.base.Joiner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("jdbc-postgresql-flexible")
class PasswordlessPostgreSQLFlexibleIT {
    String VALUE = "azure_maintenance,postgres,azure_sys,db";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testJdbcQuery() {
        String query = "SELECT datname FROM pg_database";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(query);

        List<String> list = new ArrayList<>();
        while (sqlRowSet.next()) {
            String datname = sqlRowSet.getString("datname");
            if (datname.contains("template")) {
                list.add(sqlRowSet.getString("datname"));
            }
        }
        Assertions.assertEquals(Joiner.on(",").join(list), VALUE);
    }
}
