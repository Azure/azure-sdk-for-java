/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.cosmos.implementation;

import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyBuilder;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKeyDefinitionVersion;
import com.azure.cosmos.models.PartitionKind;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadManyByPartitionKeyQueryHelperTest {

    //region Single PK (HASH) tests

    @Test(groups = { "unit" })
    public void singlePk_defaultQuery_singleValue() {
        PartitionKeyDefinition pkDef = createSinglePkDefinition("/mypk");
        List<String> selectors = createSelectors(pkDef);
        List<PartitionKey> pkValues = Collections.singletonList(new PartitionKey("pk1"));

        SqlQuerySpec result = ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT * FROM c", new ArrayList<>(), pkValues, selectors, pkDef);

        assertThat(result.getQueryText()).contains("SELECT * FROM c WHERE");
        assertThat(result.getQueryText()).contains("IN (");
        assertThat(result.getQueryText()).contains("@__rmPk_0");
        assertThat(result.getParameters()).hasSize(1);
        assertThat(result.getParameters().get(0).getValue(Object.class)).isEqualTo("pk1");
    }

    @Test(groups = { "unit" })
    public void singlePk_defaultQuery_multipleValues() {
        PartitionKeyDefinition pkDef = createSinglePkDefinition("/mypk");
        List<String> selectors = createSelectors(pkDef);
        List<PartitionKey> pkValues = Arrays.asList(
            new PartitionKey("pk1"),
            new PartitionKey("pk2"),
            new PartitionKey("pk3"));

        SqlQuerySpec result = ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT * FROM c", new ArrayList<>(), pkValues, selectors, pkDef);

        assertThat(result.getQueryText()).contains("IN (");
        assertThat(result.getQueryText()).contains("@__rmPk_0");
        assertThat(result.getQueryText()).contains("@__rmPk_1");
        assertThat(result.getQueryText()).contains("@__rmPk_2");
        assertThat(result.getParameters()).hasSize(3);
    }

    @Test(groups = { "unit" })
    public void singlePk_customQuery_noWhere() {
        PartitionKeyDefinition pkDef = createSinglePkDefinition("/mypk");
        List<String> selectors = createSelectors(pkDef);
        List<PartitionKey> pkValues = Arrays.asList(new PartitionKey("pk1"), new PartitionKey("pk2"));

        SqlQuerySpec result = ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT c.name, c.age FROM c", new ArrayList<>(), pkValues, selectors, pkDef);

        assertThat(result.getQueryText()).startsWith("SELECT c.name, c.age FROM c WHERE");
        assertThat(result.getQueryText()).contains("IN (");
    }

    @Test(groups = { "unit" })
    public void singlePk_customQuery_withExistingWhere() {
        PartitionKeyDefinition pkDef = createSinglePkDefinition("/mypk");
        List<String> selectors = createSelectors(pkDef);
        List<PartitionKey> pkValues = Collections.singletonList(new PartitionKey("pk1"));

        List<SqlParameter> baseParams = new ArrayList<>();
        baseParams.add(new SqlParameter("@minAge", 18));

        SqlQuerySpec result = ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT * FROM c WHERE c.age > @minAge", baseParams, pkValues, selectors, pkDef);

        // Should AND the PK filter to the existing WHERE clause
        assertThat(result.getQueryText()).contains("WHERE (c.age > @minAge) AND (");
        assertThat(result.getQueryText()).contains("IN (");
        assertThat(result.getParameters()).hasSize(2); // @minAge + @__rmPk_0
        assertThat(result.getParameters().get(0).getName()).isEqualTo("@minAge");
    }

    //endregion

    //region HPK (MULTI_HASH) tests

    @Test(groups = { "unit" })
    public void hpk_fullPk_defaultQuery() {
        PartitionKeyDefinition pkDef = createMultiHashPkDefinition("/city", "/zipcode");
        List<String> selectors = createSelectors(pkDef);

        PartitionKey pk = new PartitionKeyBuilder().add("Redmond").add("98052").build();
        List<PartitionKey> pkValues = Collections.singletonList(pk);

        SqlQuerySpec result = ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT * FROM c", new ArrayList<>(), pkValues, selectors, pkDef);

        assertThat(result.getQueryText()).contains("SELECT * FROM c WHERE");
        // Should use OR/AND pattern, not IN
        assertThat(result.getQueryText()).doesNotContain("IN (");
        assertThat(result.getQueryText()).contains("c[\"city\"] = @__rmPk_0");
        assertThat(result.getQueryText()).contains("AND");
        assertThat(result.getQueryText()).contains("c[\"zipcode\"] = @__rmPk_1");
        assertThat(result.getParameters()).hasSize(2);
        assertThat(result.getParameters().get(0).getValue(Object.class)).isEqualTo("Redmond");
        assertThat(result.getParameters().get(1).getValue(Object.class)).isEqualTo("98052");
    }

    @Test(groups = { "unit" })
    public void hpk_fullPk_multipleValues() {
        PartitionKeyDefinition pkDef = createMultiHashPkDefinition("/city", "/zipcode");
        List<String> selectors = createSelectors(pkDef);

        List<PartitionKey> pkValues = Arrays.asList(
            new PartitionKeyBuilder().add("Redmond").add("98052").build(),
            new PartitionKeyBuilder().add("Seattle").add("98101").build());

        SqlQuerySpec result = ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT * FROM c", new ArrayList<>(), pkValues, selectors, pkDef);

        assertThat(result.getQueryText()).contains("OR");
        assertThat(result.getQueryText()).contains("c[\"city\"] = @__rmPk_0");
        assertThat(result.getQueryText()).contains("c[\"zipcode\"] = @__rmPk_1");
        assertThat(result.getQueryText()).contains("c[\"city\"] = @__rmPk_2");
        assertThat(result.getQueryText()).contains("c[\"zipcode\"] = @__rmPk_3");
        assertThat(result.getParameters()).hasSize(4);
    }

    @Test(groups = { "unit" })
    public void hpk_partialPk_singleLevel() {
        PartitionKeyDefinition pkDef = createMultiHashPkDefinition("/city", "/zipcode", "/areaCode");
        List<String> selectors = createSelectors(pkDef);

        // Partial PK — only first level
        PartitionKey partialPk = new PartitionKeyBuilder().add("Redmond").build();
        List<PartitionKey> pkValues = Collections.singletonList(partialPk);

        SqlQuerySpec result = ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT * FROM c", new ArrayList<>(), pkValues, selectors, pkDef);

        assertThat(result.getQueryText()).contains("c[\"city\"] = @__rmPk_0");
        // Should NOT include zipcode or areaCode since it's partial
        assertThat(result.getQueryText()).doesNotContain("zipcode");
        assertThat(result.getQueryText()).doesNotContain("areaCode");
        assertThat(result.getParameters()).hasSize(1);
    }

    @Test(groups = { "unit" })
    public void hpk_partialPk_twoLevels() {
        PartitionKeyDefinition pkDef = createMultiHashPkDefinition("/city", "/zipcode", "/areaCode");
        List<String> selectors = createSelectors(pkDef);

        // Partial PK — first two levels
        PartitionKey partialPk = new PartitionKeyBuilder().add("Redmond").add("98052").build();
        List<PartitionKey> pkValues = Collections.singletonList(partialPk);

        SqlQuerySpec result = ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT * FROM c", new ArrayList<>(), pkValues, selectors, pkDef);

        assertThat(result.getQueryText()).contains("c[\"city\"] = @__rmPk_0");
        assertThat(result.getQueryText()).contains("c[\"zipcode\"] = @__rmPk_1");
        assertThat(result.getQueryText()).doesNotContain("areaCode");
        assertThat(result.getParameters()).hasSize(2);
    }

    @Test(groups = { "unit" })
    public void hpk_customQuery_withWhere() {
        PartitionKeyDefinition pkDef = createMultiHashPkDefinition("/city", "/zipcode");
        List<String> selectors = createSelectors(pkDef);

        List<SqlParameter> baseParams = new ArrayList<>();
        baseParams.add(new SqlParameter("@status", "active"));

        PartitionKey pk = new PartitionKeyBuilder().add("Redmond").add("98052").build();
        List<PartitionKey> pkValues = Collections.singletonList(pk);

        SqlQuerySpec result = ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT c.name FROM c WHERE c.status = @status", baseParams, pkValues, selectors, pkDef);

        assertThat(result.getQueryText()).contains("WHERE (c.status = @status) AND (");
        assertThat(result.getQueryText()).contains("c[\"city\"] = @__rmPk_0");
        assertThat(result.getParameters()).hasSize(3); // @status + 2 pk params
    }

    //endregion

    //region findTopLevelWhereIndex tests

    @Test(groups = { "unit" })
    public void findWhere_simpleQuery() {
        int idx = ReadManyByPartitionKeyQueryHelper.findTopLevelWhereIndex("SELECT * FROM C WHERE C.ID = 1");
        assertThat(idx).isEqualTo(16);
    }

    @Test(groups = { "unit" })
    public void findWhere_noWhere() {
        int idx = ReadManyByPartitionKeyQueryHelper.findTopLevelWhereIndex("SELECT * FROM C");
        assertThat(idx).isEqualTo(-1);
    }

    @Test(groups = { "unit" })
    public void findWhere_whereInSubquery() {
        // WHERE inside parentheses (subquery) should be ignored
        int idx = ReadManyByPartitionKeyQueryHelper.findTopLevelWhereIndex(
            "SELECT * FROM C WHERE EXISTS(SELECT VALUE T FROM T IN C.TAGS WHERE T = 'FOO')");
        // Should find the outer WHERE, not the inner one
        assertThat(idx).isEqualTo(16);
    }

    @Test(groups = { "unit" })
    public void findWhere_caseInsensitive() {
        int idx = ReadManyByPartitionKeyQueryHelper.findTopLevelWhereIndex("SELECT * FROM C WHERE C.X = 1");
        assertThat(idx).isGreaterThan(0);
    }

    @Test(groups = { "unit" })
    public void findWhere_whereNotKeyword() {
        // "ELSEWHERE" should not match
        int idx = ReadManyByPartitionKeyQueryHelper.findTopLevelWhereIndex("SELECT * FROM ELSEWHERE");
        assertThat(idx).isEqualTo(-1);
    }

    //endregion

    //region Custom alias tests

    @Test(groups = { "unit" })
    public void singlePk_customAlias() {
        PartitionKeyDefinition pkDef = createSinglePkDefinition("/mypk");
        List<String> selectors = createSelectors(pkDef);
        List<PartitionKey> pkValues = Arrays.asList(new PartitionKey("pk1"), new PartitionKey("pk2"));

        SqlQuerySpec result = ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT x.id, x.mypk FROM x", new ArrayList<>(), pkValues, selectors, pkDef);

        assertThat(result.getQueryText()).startsWith("SELECT x.id, x.mypk FROM x WHERE");
        assertThat(result.getQueryText()).contains("x[\"mypk\"] IN (");
        assertThat(result.getQueryText()).doesNotContain("c[\"mypk\"]");
    }

    @Test(groups = { "unit" })
    public void singlePk_customAlias_withWhere() {
        PartitionKeyDefinition pkDef = createSinglePkDefinition("/mypk");
        List<String> selectors = createSelectors(pkDef);
        List<PartitionKey> pkValues = Collections.singletonList(new PartitionKey("pk1"));

        List<SqlParameter> baseParams = new ArrayList<>();
        baseParams.add(new SqlParameter("@cat", "HelloWorld"));

        SqlQuerySpec result = ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT x.id, x.mypk FROM x WHERE x.category = @cat", baseParams, pkValues, selectors, pkDef);

        assertThat(result.getQueryText()).contains("WHERE (x.category = @cat) AND (x[\"mypk\"] IN (");
    }

    @Test(groups = { "unit" })
    public void hpk_customAlias() {
        PartitionKeyDefinition pkDef = createMultiHashPkDefinition("/city", "/zipcode");
        List<String> selectors = createSelectors(pkDef);

        PartitionKey pk = new PartitionKeyBuilder().add("Redmond").add("98052").build();
        List<PartitionKey> pkValues = Collections.singletonList(pk);

        SqlQuerySpec result = ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT r.name FROM root r", new ArrayList<>(), pkValues, selectors, pkDef);

        assertThat(result.getQueryText()).contains("r[\"city\"] = @__rmPk_0");
        assertThat(result.getQueryText()).contains("r[\"zipcode\"] = @__rmPk_1");
        assertThat(result.getQueryText()).doesNotContain("c[\"");
    }

    //endregion

    //region extractTableAlias tests

    @Test(groups = { "unit" })
    public void extractAlias_defaultC() {
        assertThat(ReadManyByPartitionKeyQueryHelper.extractTableAlias("SELECT * FROM c")).isEqualTo("c");
    }

    @Test(groups = { "unit" })
    public void extractAlias_customX() {
        assertThat(ReadManyByPartitionKeyQueryHelper.extractTableAlias("SELECT x.id FROM x WHERE x.age > 5")).isEqualTo("x");
    }

    @Test(groups = { "unit" })
    public void extractAlias_rootWithAlias() {
        assertThat(ReadManyByPartitionKeyQueryHelper.extractTableAlias("SELECT r.name FROM root r")).isEqualTo("r");
    }

    @Test(groups = { "unit" })
    public void extractAlias_rootNoAlias() {
        assertThat(ReadManyByPartitionKeyQueryHelper.extractTableAlias("SELECT * FROM root")).isEqualTo("root");
    }

    @Test(groups = { "unit" })
    public void extractAlias_containerWithWhere() {
        assertThat(ReadManyByPartitionKeyQueryHelper.extractTableAlias("SELECT * FROM items WHERE items.status = 'active'")).isEqualTo("items");
    }

    @Test(groups = { "unit" })
    public void extractAlias_caseInsensitive() {
        assertThat(ReadManyByPartitionKeyQueryHelper.extractTableAlias("select * from MyContainer where MyContainer.id = '1'")).isEqualTo("MyContainer");
    }

    //endregion


    //region String literal handling tests (#1)

    @Test(groups = { "unit" })
    public void findWhere_ignoresWhereInsideStringLiteral() {
        // WHERE inside a string literal should be ignored
        int idx = ReadManyByPartitionKeyQueryHelper.findTopLevelWhereIndex(
            "SELECT * FROM c WHERE c.msg = 'use WHERE clause here'");
        // Should find the outer WHERE at position 16, not the one inside the string
        assertThat(idx).isEqualTo(16);
    }

    @Test(groups = { "unit" })
    public void findWhere_ignoresParenthesesInsideStringLiteral() {
        // Parentheses inside string literal should not affect depth tracking
        int idx = ReadManyByPartitionKeyQueryHelper.findTopLevelWhereIndex(
            "SELECT * FROM c WHERE c.name = 'foo(bar)' AND c.x = 1");
        assertThat(idx).isEqualTo(16);
    }

    @Test(groups = { "unit" })
    public void findWhere_handlesUnbalancedParenInStringLiteral() {
        // Unbalanced paren inside string literal must not corrupt depth
        int idx = ReadManyByPartitionKeyQueryHelper.findTopLevelWhereIndex(
            "SELECT * FROM c WHERE c.val = 'open(' AND c.active = true");
        assertThat(idx).isEqualTo(16);
    }

    @Test(groups = { "unit" })
    public void findWhere_handlesStringLiteralBeforeWhere() {
        // String literal in SELECT before WHERE
        int idx = ReadManyByPartitionKeyQueryHelper.findTopLevelWhereIndex(
            "SELECT 'WHERE' as label FROM c WHERE c.id = '1'");
        // The WHERE inside quotes should be ignored; the real WHERE is further along
        assertThat(idx).isGreaterThan(30);
    }

    @Test(groups = { "unit" })
    public void singlePk_customQuery_withStringLiteralContainingParens() {
        PartitionKeyDefinition pkDef = createSinglePkDefinition("/mypk");
        List<String> selectors = createSelectors(pkDef);
        List<PartitionKey> pkValues = Collections.singletonList(new PartitionKey("pk1"));

        List<SqlParameter> baseParams = new ArrayList<>();
        baseParams.add(new SqlParameter("@msg", "hello"));

        SqlQuerySpec result = ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT * FROM c WHERE c.msg = 'test(value)WHERE'", baseParams, pkValues, selectors, pkDef);

        // Should correctly AND the PK filter to the real WHERE clause
        assertThat(result.getQueryText()).contains("WHERE (c.msg = 'test(value)WHERE') AND (");
    }

    //endregion

    //region OFFSET/LIMIT/HAVING alias detection tests (#9)

    @Test(groups = { "unit" })
    public void extractAlias_containerWithOffset() {
        assertThat(ReadManyByPartitionKeyQueryHelper.extractTableAlias(
            "SELECT * FROM c OFFSET 10 LIMIT 5")).isEqualTo("c");
    }

    @Test(groups = { "unit" })
    public void extractAlias_containerWithLimit() {
        assertThat(ReadManyByPartitionKeyQueryHelper.extractTableAlias(
            "SELECT * FROM c LIMIT 10")).isEqualTo("c");
    }

    @Test(groups = { "unit" })
    public void extractAlias_containerWithHaving() {
        assertThat(ReadManyByPartitionKeyQueryHelper.extractTableAlias(
            "SELECT c.status, COUNT(1) FROM c GROUP BY c.status HAVING COUNT(1) > 1")).isEqualTo("c");
    }

    //endregion

    //region helpers

    private PartitionKeyDefinition createSinglePkDefinition(String path) {
        PartitionKeyDefinition pkDef = new PartitionKeyDefinition();
        pkDef.setKind(PartitionKind.HASH);
        pkDef.setVersion(PartitionKeyDefinitionVersion.V2);
        pkDef.setPaths(Collections.singletonList(path));
        return pkDef;
    }

    private PartitionKeyDefinition createMultiHashPkDefinition(String... paths) {
        PartitionKeyDefinition pkDef = new PartitionKeyDefinition();
        pkDef.setKind(PartitionKind.MULTI_HASH);
        pkDef.setVersion(PartitionKeyDefinitionVersion.V2);
        pkDef.setPaths(Arrays.asList(paths));
        return pkDef;
    }

    private List<String> createSelectors(PartitionKeyDefinition pkDef) {
        return pkDef.getPaths()
            .stream()
            .map(pathPart -> pathPart.substring(1)) // skip starting /
            .map(pathPart -> pathPart.replace("\"", "\\"))
            .map(part -> "[\"" + part + "\"]")
            .collect(Collectors.toList());
    }

    //endregion
}
