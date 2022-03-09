// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.guava25.collect.Lists;
import com.azure.cosmos.models.SqlParameter;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class SqlParameterTest {

    @Test(groups = {"unit"})
    public void validateSqlParameterEquals() {
        ArrayList<SqlParameter> sqlParameters = Lists.newArrayList(new SqlParameter("@param1", 3), new SqlParameter(
            "@param2", 4));

        assertThat(sqlParameters.contains(new SqlParameter("@param1", 3))).isTrue();
        assertThat(sqlParameters.contains(new SqlParameter("@param2", 4))).isTrue();
    }

    @Test(groups = {"unit"})
    public void validateSqlParameterHashcode() {
        SqlParameter sqlParameter1 = new SqlParameter("@param1", 3);
        SqlParameter sqlParameter2 = new SqlParameter("@param2", 4);

        assertThat(sqlParameter1.hashCode()).isEqualTo(new SqlParameter("@param1", 3).hashCode());
        assertThat(sqlParameter2.hashCode()).isEqualTo(new SqlParameter("@param2", 4).hashCode());
    }
}
