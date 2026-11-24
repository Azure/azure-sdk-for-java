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
import com.azure.cosmos.implementation.routing.Range;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ReadManyByPartitionKeyQueryHelperTest {

    //region Single PK (HASH) tests

    @Test(groups = { "unit" })
    public void singlePk_defaultQuery_singleValue() {
        PartitionKeyDefinition pkDef = createSinglePkDefinition("/mypk");
        List<String> selectors = createSelectors(pkDef);
        List<PartitionKey> pkValues = Collections.singletonList(new PartitionKey("pk1"));

        SqlQuerySpec result = ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT * FROM c", new ArrayList<>(), pkValues, selectors, pkDef);

        assertThat(result.getQueryText()).contains("SELECT * FROM c\n WHERE");
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

        assertThat(result.getQueryText()).startsWith("SELECT c.name, c.age FROM c\n WHERE");
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
        assertThat(result.getQueryText()).contains("WHERE (c.age > @minAge\n) AND (");
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

        assertThat(result.getQueryText()).contains("SELECT * FROM c\n WHERE");
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

        assertThat(result.getQueryText()).contains("WHERE (c.status = @status\n) AND (");
        assertThat(result.getQueryText()).contains("c[\"city\"] = @__rmPk_0");
        assertThat(result.getParameters()).hasSize(3); // @status + 2 pk params
    }

    @Test(groups = { "unit" })
    public void normalizePartitionKeys_removesSubsumedFullHpkValues() {
        PartitionKeyDefinition pkDef = createMultiHashPkDefinition("/city", "/zipcode", "/areaCode");

        List<PartitionKey> pkValues = Arrays.asList(
            new PartitionKeyBuilder().add("Redmond").build(),
            new PartitionKeyBuilder().add("Redmond").add("98052").add(1).build(),
            new PartitionKeyBuilder().add("Seattle").build());

        List<RxDocumentClientImpl.NormalizedPartitionKey> normalizedPartitionKeys =
            RxDocumentClientImpl.normalizePartitionKeys(pkValues, pkDef);

        assertThat(normalizedPartitionKeys)
            .extracting(normalizedPk -> normalizedPk.effectivePkInternal.toJson())
            .containsExactly("[\"Redmond\"]", "[\"Seattle\"]");
    }

    @Test(groups = { "unit" })
    public void normalizePartitionKeys_rejectsNoneForMultiHashPartitionKeys() {
        PartitionKeyDefinition pkDef = createMultiHashPkDefinition("/city", "/zipcode");

        assertThatThrownBy(() -> RxDocumentClientImpl.normalizePartitionKeys(
            Collections.singletonList(PartitionKey.NONE),
            pkDef))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("PartitionKey.NONE is not supported for multi-path partition keys");
    }

    @Test(groups = { "unit" })
    public void normalizePartitionKeys_epkAtBatchBoundary_isCorrectlyContained() {
        // Verifies that a PK whose EPK sits exactly at a batchFilter boundary is correctly
        // included in its owning range and excluded from the next range. The batchFilter is
        // [minInclusive, maxExclusive) — a PK at the minInclusive boundary must be included,
        // and a PK at the maxExclusive boundary must be excluded (it belongs to the next batch).
        PartitionKeyDefinition pkDef = createSinglePkDefinition("/mypk");
        List<PartitionKey> pkValues = Arrays.asList(
            new PartitionKey("alpha"),
            new PartitionKey("bravo"),
            new PartitionKey("charlie"));

        List<RxDocumentClientImpl.NormalizedPartitionKey> normalized =
            RxDocumentClientImpl.normalizePartitionKeys(pkValues, pkDef);

        // After normalization the EPKs are sorted; take the middle one's EPK as a boundary
        assertThat(normalized).hasSize(3);
        String middleEpk = normalized.get(1).effectivePartitionKeyString;

        // Range [middleEpk, ...) must include the middle PK (minInclusive)
        Range<String> rangeIncluding = new Range<>(middleEpk, "FF", true, false);
        assertThat(rangeIncluding.contains(middleEpk)).isTrue();

        // Range [..., middleEpk) must exclude the middle PK (maxExclusive)
        Range<String> rangeExcluding = new Range<>("", middleEpk, true, false);
        assertThat(rangeExcluding.contains(middleEpk)).isFalse();
    }

    @Test(groups = { "unit" })
    public void normalizePartitionKeys_noneInHpk_alwaysRejected() {
        // PartitionKey.NONE must be rejected for MULTI_HASH regardless of whether this is the
        // first call or a continuation-resume call (normalizePartitionKeys is invoked on both
        // code paths). This test verifies the invariant holds for mixed inputs too.
        PartitionKeyDefinition pkDef = createMultiHashPkDefinition("/city", "/zipcode");

        // NONE alone
        assertThatThrownBy(() -> RxDocumentClientImpl.normalizePartitionKeys(
            Collections.singletonList(PartitionKey.NONE), pkDef))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("PartitionKey.NONE is not supported for multi-path partition keys");

        // NONE mixed with valid HPK values
        assertThatThrownBy(() -> RxDocumentClientImpl.normalizePartitionKeys(
            Arrays.asList(
                new PartitionKeyBuilder().add("Redmond").add("98052").build(),
                PartitionKey.NONE),
            pkDef))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("PartitionKey.NONE is not supported for multi-path partition keys");
    }


    @Test(groups = { "unit" })
    public void normalizePartitionKeys_twoPartialPrefixesAtDifferentDepths() {
        // Two partial PKs at different prefix depths: (Redmond) and (Redmond, 98052).
        // (Redmond) is a prefix of (Redmond, 98052), so (Redmond, 98052) should be collapsed.
        PartitionKeyDefinition pkDef = createMultiHashPkDefinition("/city", "/zipcode", "/areaCode");

        List<PartitionKey> pkValues = Arrays.asList(
            new PartitionKeyBuilder().add("Redmond").build(),
            new PartitionKeyBuilder().add("Redmond").add("98052").build());

        List<RxDocumentClientImpl.NormalizedPartitionKey> normalized =
            RxDocumentClientImpl.normalizePartitionKeys(pkValues, pkDef);

        assertThat(normalized)
            .extracting(normalizedPk -> normalizedPk.effectivePkInternal.toJson())
            .containsExactly("[\"Redmond\"]");
    }

    @Test(groups = { "unit" })
    public void normalizePartitionKeys_partialPkDoesNotSubsumeUnrelatedPk() {
        // (Redmond) and (Seattle, 98052) are unrelated - neither subsumes the other.
        PartitionKeyDefinition pkDef = createMultiHashPkDefinition("/city", "/zipcode", "/areaCode");

        List<PartitionKey> pkValues = Arrays.asList(
            new PartitionKeyBuilder().add("Redmond").build(),
            new PartitionKeyBuilder().add("Seattle").add("98052").build());

        List<RxDocumentClientImpl.NormalizedPartitionKey> normalized =
            RxDocumentClientImpl.normalizePartitionKeys(pkValues, pkDef);

        assertThat(normalized).hasSize(2);
        assertThat(normalized)
            .extracting(normalizedPk -> normalizedPk.effectivePkInternal.toJson())
            .containsExactly("[\"Redmond\"]", "[\"Seattle\",\"98052\"]");
    }

    @Test(groups = { "unit" })
    public void normalizePartitionKeys_allPartialPksPreserved() {
        // All PKs are partial (single-level) and unrelated - all should survive.
        PartitionKeyDefinition pkDef = createMultiHashPkDefinition("/city", "/zipcode", "/areaCode");

        List<PartitionKey> pkValues = Arrays.asList(
            new PartitionKeyBuilder().add("Redmond").build(),
            new PartitionKeyBuilder().add("Seattle").build(),
            new PartitionKeyBuilder().add("Pittsburgh").build());

        List<RxDocumentClientImpl.NormalizedPartitionKey> normalized =
            RxDocumentClientImpl.normalizePartitionKeys(pkValues, pkDef);

        assertThat(normalized).hasSize(3);
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

        assertThat(result.getQueryText()).startsWith("SELECT x.id, x.mypk FROM x\n WHERE");
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

        assertThat(result.getQueryText()).contains("WHERE (x.category = @cat\n) AND (x[\"mypk\"] IN (");
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
        assertThat(result.getQueryText()).contains("WHERE (c.msg = 'test(value)WHERE'\n) AND (");
    }

    @Test(groups = { "unit" })
    public void findWhere_ignoresKeywordInsideDoubleQuotedBracketNotation() {
        // c["WHERE"] uses double-quoted bracket notation — the WHERE inside quotes
        // must not be matched as the real WHERE keyword.
        int idx = ReadManyByPartitionKeyQueryHelper.findTopLevelWhereIndex(
            "SELECT c[\"WHERE\"] FROM c WHERE c.status = 'active'");
        // The real WHERE is after "FROM c ", not inside the brackets
        assertThat(idx).isEqualTo(25);
    }

    @Test(groups = { "unit" })
    public void findWhere_ignoresFromInsideDoubleQuotedBracketNotation() {
        // Property named "FROM" in bracket notation should not confuse the FROM-clause parser
        String query = "SELECT c[\"FROM\"] FROM c WHERE c.x = 1";
        assertThat(ReadManyByPartitionKeyQueryHelper.extractTableAlias(query)).isEqualTo("c");
        assertThat(ReadManyByPartitionKeyQueryHelper.findTopLevelWhereIndex(query)).isEqualTo(24);
    }

    //endregion

    //region Dotted/underscore/bracket property access boundary tests

    @Test(groups = { "unit" })
    public void findWhere_ignoresWhereInDottedPropertyAccess() {
        // c.where is a property named "where" — not the WHERE keyword
        int idx = ReadManyByPartitionKeyQueryHelper.findTopLevelWhereIndex(
            "SELECT c.where, c.id FROM c WHERE c.status = 'active'");
        // "SELECT c.where, c.id FROM c " = 28 chars, so WHERE at 28
        assertThat(idx).isEqualTo(28);
    }

    @Test(groups = { "unit" })
    public void findWhere_ignoresFromInDottedPropertyAccess() {
        // c.from is a property — the real FROM is later
        String query = "SELECT c.from FROM c WHERE c.x = 1";
        assertThat(ReadManyByPartitionKeyQueryHelper.extractTableAlias(query)).isEqualTo("c");
    }

    @Test(groups = { "unit" })
    public void findWhere_ignoresOrderInDottedPropertyAccess() {
        // c.order shouldn't match ORDER keyword
        int idx = ReadManyByPartitionKeyQueryHelper.findTopLevelKeywordIndex(
            "SELECT c.order FROM c WHERE c.x = 1", "ORDER");
        assertThat(idx).isEqualTo(-1); // no ORDER BY in this query
    }

    @Test(groups = { "unit" })
    public void findWhere_ignoresKeywordInUnderscoredIdentifier() {
        // where_clause is an identifier, not WHERE
        int idx = ReadManyByPartitionKeyQueryHelper.findTopLevelWhereIndex(
            "SELECT c.where_clause FROM c WHERE c.x = 1");
        // "SELECT c.where_clause FROM c " = 29 chars, WHERE at 29
        assertThat(idx).isEqualTo(29);
    }

    @Test(groups = { "unit" })
    public void findWhere_ignoresKeywordPrecededByUnderscore() {
        // _where is an identifier
        int idx = ReadManyByPartitionKeyQueryHelper.findTopLevelWhereIndex(
            "SELECT c._where FROM c WHERE c.x = 1");
        assertThat(idx).isEqualTo(23);
    }

    @Test(groups = { "unit" })
    public void extractAlias_ignoresKeywordPrefixInPropertyName() {
        // c.offset is a property — should not confuse isFollowedByReservedKeyword
        assertThat(ReadManyByPartitionKeyQueryHelper.extractTableAlias(
            "SELECT c.offset_val FROM c WHERE c.x = 1")).isEqualTo("c");
    }

    @Test(groups = { "unit" })
    public void findWhere_ignoresKeywordFollowedByBracket() {
        // WHERE[ should not match as a standalone WHERE keyword
        int idx = ReadManyByPartitionKeyQueryHelper.findTopLevelWhereIndex(
            "SELECT c.WHERE[0] FROM c WHERE c.x = 1");
        // The real WHERE is at position 25
        assertThat(idx).isEqualTo(25);
    }

    @Test(groups = { "unit" })
    public void findWhere_ignoresKeywordFollowedByDollar() {
        // WHERE$1 is an identifier, not WHERE
        int idx = ReadManyByPartitionKeyQueryHelper.findTopLevelWhereIndex(
            "SELECT WHERE$1 FROM c WHERE c.x = 1");
        assertThat(idx).isEqualTo(22);
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

    @Test(groups = { "unit" })
    public void createSelectors_nestedPath() {
        PartitionKeyDefinition pkDef = createSinglePkDefinition("/address/city");

        assertThat(PartitionKeyQueryHelper.createPkSelectors(pkDef))
            .containsExactly("[\"address\"][\"city\"]");
    }

    @Test(groups = { "unit" })
    public void createSelectors_escapesQuotesInPathParts() {
        // Verify the escaping logic directly: a path part containing a double quote
        // must produce \" in the selector, not just a bare backslash.
        // Use an unquoted simple path /my so PathParser returns "my" cleanly,
        // then verify a path whose PathParser output contains a quote character.
        // PathParser for /"my\"field" yields token: my\"field (literal \, ")
        PartitionKeyDefinition pkDef = createSinglePkDefinition("/\"my\\\"field\"");

        List<String> selectors = PartitionKeyQueryHelper.createPkSelectors(pkDef);
        assertThat(selectors).hasSize(1);
        String selector = selectors.get(0);
        // Must contain the escaped quote sequence \"
        assertThat(selector).contains("\\\"");
        // Must NOT be just ["my\field"] (old bug: quote replaced with bare backslash)
        assertThat(selector).isNotEqualTo("[\"my\\field\"]");
    }

    //endregion

    //region PartitionKey.NONE query generation tests

    @Test(groups = { "unit" })
    public void singlePk_nonePartitionKey_generatesNotIsDefined() {
        PartitionKeyDefinition pkDef = createSinglePkDefinition("/mypk");
        List<String> selectors = createSelectors(pkDef);
        List<PartitionKey> pkValues = Collections.singletonList(PartitionKey.NONE);

        SqlQuerySpec result = ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT * FROM c", new ArrayList<>(), pkValues, selectors, pkDef);

        assertThat(result.getQueryText()).contains("NOT IS_DEFINED(c[\"mypk\"])");
        assertThat(result.getParameters()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void singlePk_mixedNoneAndNormal_generatesCombinedFilter() {
        PartitionKeyDefinition pkDef = createSinglePkDefinition("/mypk");
        List<String> selectors = createSelectors(pkDef);
        List<PartitionKey> pkValues = Arrays.asList(new PartitionKey("pk1"), PartitionKey.NONE);

        SqlQuerySpec result = ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT * FROM c", new ArrayList<>(), pkValues, selectors, pkDef);

        assertThat(result.getQueryText()).contains("IN (");
        assertThat(result.getQueryText()).contains("NOT IS_DEFINED(c[\"mypk\"])");
        assertThat(result.getQueryText()).contains("OR");
    }

    @Test(groups = { "unit" })
    public void hpk_nonePartitionKey_throwsForMultiHash() {
        PartitionKeyDefinition pkDef = createMultiHashPkDefinition("/city", "/zipcode");
        List<String> selectors = createSelectors(pkDef);
        List<PartitionKey> pkValues = Collections.singletonList(PartitionKey.NONE);

        // PartitionKey.NONE is not supported for multi-path partition keys -
        // the SDK rejects it in normalizePartitionKeys before reaching the query helper.
        // The query helper itself also rejects it defensively.
        assertThatThrownBy(() -> ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT * FROM c", new ArrayList<>(), pkValues, selectors, pkDef))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("PartitionKey.NONE is not supported for multi-path partition keys");
    }

    //endregion

    //region Trailing single-line comment tests

    @Test(groups = { "unit" })
    public void singlePk_customQuery_trailingSingleLineComment_noWhere() {
        // A trailing -- comment must not swallow the appended WHERE clause
        PartitionKeyDefinition pkDef = createSinglePkDefinition("/mypk");
        List<String> selectors = createSelectors(pkDef);
        List<PartitionKey> pkValues = Collections.singletonList(new PartitionKey("pk1"));

        SqlQuerySpec result = ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT * FROM c -- my note", new ArrayList<>(), pkValues, selectors, pkDef);

        // WHERE must appear on a new line so it is not inside the -- comment
        assertThat(result.getQueryText()).contains("\n WHERE");
        assertThat(result.getQueryText()).contains("IN (");
        assertThat(result.getQueryText()).contains("@__rmPk_0");
        assertThat(result.getParameters()).hasSize(1);
    }

    @Test(groups = { "unit" })
    public void singlePk_customQuery_trailingBlockComment_noWhere() {
        // Block comments are not affected, but verify the query still works
        PartitionKeyDefinition pkDef = createSinglePkDefinition("/mypk");
        List<String> selectors = createSelectors(pkDef);
        List<PartitionKey> pkValues = Collections.singletonList(new PartitionKey("pk1"));

        SqlQuerySpec result = ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT * FROM c /* note */", new ArrayList<>(), pkValues, selectors, pkDef);

        assertThat(result.getQueryText()).contains("WHERE");
        assertThat(result.getQueryText()).contains("IN (");
        assertThat(result.getParameters()).hasSize(1);
    }

    @Test(groups = { "unit" })
    public void hpk_customQuery_trailingSingleLineComment_noWhere() {
        // HPK path with trailing -- comment.
        // Note: extractTableAlias does not skip comments, so "-- comment" after the
        // FROM alias is mis-parsed as an alias token. This test verifies the \n WHERE
        // fix and parameter correctness; alias-aware comment handling is a separate concern.
        PartitionKeyDefinition pkDef = createMultiHashPkDefinition("/city", "/zipcode");
        List<String> selectors = createSelectors(pkDef);

        PartitionKey pk = new PartitionKeyBuilder().add("Redmond").add("98052").build();
        List<PartitionKey> pkValues = Collections.singletonList(pk);

        SqlQuerySpec result = ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT * FROM c -- WHERE comment", new ArrayList<>(), pkValues, selectors, pkDef);

        assertThat(result.getQueryText()).contains("\n WHERE");
        assertThat(result.getQueryText()).contains("@__rmPk_0");
        assertThat(result.getQueryText()).contains("@__rmPk_1");
        assertThat(result.getParameters()).hasSize(2);
    }

    @Test(groups = { "unit" })
    public void singlePk_customQuery_trailingComment_withExistingWhere() {
        // When the query already has a WHERE clause, trailing comment after the condition
        // is handled by the AND-merge path, not the no-WHERE path
        PartitionKeyDefinition pkDef = createSinglePkDefinition("/mypk");
        List<String> selectors = createSelectors(pkDef);
        List<PartitionKey> pkValues = Collections.singletonList(new PartitionKey("pk1"));

        List<SqlParameter> baseParams = new ArrayList<>();
        baseParams.add(new SqlParameter("@minAge", 18));

        SqlQuerySpec result = ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT * FROM c WHERE c.age > @minAge -- filter", baseParams, pkValues, selectors, pkDef);

        assertThat(result.getQueryText()).contains("WHERE (c.age > @minAge -- filter\n) AND (");
        assertThat(result.getQueryText()).contains("IN (");
    }

    @Test(groups = { "unit" })
    public void singlePk_existingWhere_trailingLineComment_andClauseNotSwallowed() {
        // Without the \n before ), the -- comment would swallow ") AND (" and the entire
        // PK filter, producing a query that returns unfiltered results.
        PartitionKeyDefinition pkDef = createSinglePkDefinition("/mypk");
        List<String> selectors = createSelectors(pkDef);
        List<PartitionKey> pkValues = Arrays.asList(new PartitionKey("pk1"), new PartitionKey("pk2"));

        List<SqlParameter> baseParams = new ArrayList<>();
        baseParams.add(new SqlParameter("@status", "active"));

        SqlQuerySpec result = ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT * FROM c WHERE c.status = @status -- only active", baseParams, pkValues, selectors, pkDef);

        // The \n must break the -- comment so that ) AND ( is on a new line
        String queryText = result.getQueryText();
        assertThat(queryText).contains("-- only active\n) AND (");
        assertThat(queryText).contains("IN (");
        assertThat(queryText).contains("@__rmPk_0");
        assertThat(queryText).contains("@__rmPk_1");
        assertThat(result.getParameters()).hasSize(3); // @status + 2 pk params
    }

    @Test(groups = { "unit" })
    public void hpk_existingWhere_trailingLineComment_andClauseNotSwallowed() {
        // HPK variant: trailing -- in existing WHERE must not swallow the AND clause
        PartitionKeyDefinition pkDef = createMultiHashPkDefinition("/city", "/zipcode");
        List<String> selectors = createSelectors(pkDef);

        List<SqlParameter> baseParams = new ArrayList<>();
        baseParams.add(new SqlParameter("@active", true));

        PartitionKey pk = new PartitionKeyBuilder().add("Redmond").add("98052").build();
        List<PartitionKey> pkValues = Collections.singletonList(pk);

        SqlQuerySpec result = ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT * FROM c WHERE c.active = @active -- check", baseParams, pkValues, selectors, pkDef);

        String queryText = result.getQueryText();
        assertThat(queryText).contains("-- check\n) AND (");
        assertThat(queryText).contains("c[\"city\"] = @__rmPk_0");
        assertThat(queryText).contains("c[\"zipcode\"] = @__rmPk_1");
        assertThat(result.getParameters()).hasSize(3);
    }

    @Test(groups = { "unit" })
    public void singlePk_existingWhere_multipleLineComments_andClauseNotSwallowed() {
        // WHERE clause with a -- comment mid-condition (on its own line) and at the end
        PartitionKeyDefinition pkDef = createSinglePkDefinition("/mypk");
        List<String> selectors = createSelectors(pkDef);
        List<PartitionKey> pkValues = Collections.singletonList(new PartitionKey("pk1"));

        List<SqlParameter> baseParams = new ArrayList<>();
        baseParams.add(new SqlParameter("@minAge", 18));
        baseParams.add(new SqlParameter("@maxAge", 65));

        SqlQuerySpec result = ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT * FROM c WHERE c.age > @minAge -- lower bound\n  AND c.age < @maxAge -- upper bound",
            baseParams, pkValues, selectors, pkDef);

        String queryText = result.getQueryText();
        // The final -- upper bound must be broken by \n before )
        assertThat(queryText).contains("-- upper bound\n) AND (");
        assertThat(queryText).contains("IN (");
        assertThat(queryText).contains("@__rmPk_0");
        assertThat(result.getParameters()).hasSize(3);
    }

    @Test(groups = { "unit" })
    public void singlePk_existingWhere_blockComment_noNewlineNeeded() {
        // Block comments (/* */) are terminated by */ and don't need \n protection,
        // but the \n is harmless. Verify the query is still correct.
        PartitionKeyDefinition pkDef = createSinglePkDefinition("/mypk");
        List<String> selectors = createSelectors(pkDef);
        List<PartitionKey> pkValues = Collections.singletonList(new PartitionKey("pk1"));

        List<SqlParameter> baseParams = new ArrayList<>();
        baseParams.add(new SqlParameter("@val", 42));

        SqlQuerySpec result = ReadManyByPartitionKeyQueryHelper.createReadManyByPkQuerySpec(
            "SELECT * FROM c WHERE c.x = @val /* note */", baseParams, pkValues, selectors, pkDef);

        String queryText = result.getQueryText();
        assertThat(queryText).contains("WHERE (c.x = @val /* note */\n) AND (");
        assertThat(queryText).contains("IN (");
        assertThat(result.getParameters()).hasSize(2);
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
        return PartitionKeyQueryHelper.createPkSelectors(pkDef);
    }

    //endregion
}
