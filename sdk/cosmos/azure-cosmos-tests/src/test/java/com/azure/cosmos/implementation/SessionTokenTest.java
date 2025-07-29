// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.azure.cosmos.implementation.Utils.ValueHolder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class SessionTokenTest {

    @DataProvider(name = "isSessionTokenFalseProgressMergeEnabled")
    public Object[] isSessionTokenFalseProgressMergeEnabled() {
        return new Object[] { false, true };
    }

    @Test(groups = "unit")
    public void validateSuccessfulSessionTokenParsing() {
        // valid session token
        String sessionToken = "1#100#1=20#2=5#3=30";
        ValueHolder<ISessionToken> parsedSessionToken = new ValueHolder<>(null);

        assertThat(VectorSessionToken.tryCreate(sessionToken, parsedSessionToken)).isTrue();
    }

    @Test(groups = "unit")
    public void validateSessionTokenParsingWithInvalidVersion() {
        String sessionToken = "foo#100#1=20#2=5#3=30";
        ValueHolder<ISessionToken> parsedSessionToken = new ValueHolder<>(null);
        assertThat(VectorSessionToken.tryCreate(sessionToken, parsedSessionToken)).isFalse();
    }

    @Test(groups = "unit")
    public void validateSessionTokenParsingWithInvalidGlobalLsn() {
        String sessionToken = "1#foo#1=20#2=5#3=30";
        ValueHolder<ISessionToken> parsedSessionToken = new ValueHolder<>(null);
        assertThat(VectorSessionToken.tryCreate(sessionToken, parsedSessionToken)).isFalse();
    }

    @Test(groups = "unit")
    public void validateSessionTokenParsingWithInvalidRegionProgress() {
        String sessionToken = "1#100#1=20#2=x#3=30";
        ValueHolder<ISessionToken> parsedSessionToken = new ValueHolder<>(null);
        assertThat(VectorSessionToken.tryCreate(sessionToken, parsedSessionToken)).isFalse();

    }

    @Test(groups = "unit")
    public void validateSessionTokenParsingWithInvalidFormat() {
        String sessionToken = "1;100#1=20#2=40";
        ValueHolder<ISessionToken> parsedSessionToken = new ValueHolder<>(null);
        assertThat(VectorSessionToken.tryCreate(sessionToken, parsedSessionToken)).isFalse();
    }

    @Test(groups = "unit")
    public void validateSessionTokenParsingFromEmptyString() {
        String sessionToken = "";
        ValueHolder<ISessionToken> parsedSessionToken = new ValueHolder<>(null);
        assertThat(VectorSessionToken.tryCreate(sessionToken, parsedSessionToken)).isFalse();
    }

    @Test(groups = "unit")
    public void validateSessionTokenComparison() {
        // valid session token
        ValueHolder<ISessionToken> sessionToken1 = new ValueHolder<>(null);
        ValueHolder<ISessionToken> sessionToken2 = new ValueHolder<>(null);
        ValueHolder<ISessionToken> sessionTokenMerged = new ValueHolder<>(null);

        assertThat(VectorSessionToken.tryCreate("1#100#1=20#2=5#3=30", sessionToken1)).isTrue();
        assertThat(VectorSessionToken.tryCreate("2#105#4=10#2=5#3=30", sessionToken2)).isTrue();

        assertThat(sessionToken1.v).isNotEqualTo(sessionToken2.v);
        assertThat(sessionToken2.v).isNotEqualTo(sessionToken1.v);

        assertThat(sessionToken1.v.isValid(sessionToken2.v)).isTrue();
        assertThat(sessionToken2.v.isValid(sessionToken1.v)).isFalse();


        assertThat(VectorSessionToken.tryCreate("2#105#2=5#3=30#4=10", sessionTokenMerged)).isTrue();
        assertThat(sessionTokenMerged.v).isEqualTo(sessionToken1.v.merge(sessionToken2.v));

        assertThat(VectorSessionToken.tryCreate("1#100#1=20#2=5#3=30", sessionToken1)).isTrue();
        assertThat(VectorSessionToken.tryCreate("1#100#1=10#2=8#3=30", sessionToken2)).isTrue();

        assertThat(sessionToken1.v.equals(sessionToken2.v)).isFalse();
        assertThat(sessionToken2.v.equals(sessionToken1.v)).isFalse();
        assertThat(sessionToken1.v.isValid(sessionToken2.v)).isFalse();
        assertThat(sessionToken2.v.isValid(sessionToken1.v)).isFalse();

        assertThat(VectorSessionToken.tryCreate("1#100#1=20#2=8#3=30", sessionTokenMerged)).isTrue();
        assertThat(sessionTokenMerged.v.equals(sessionToken1.v.merge(sessionToken2.v))).isTrue();

        assertThat(VectorSessionToken.tryCreate("1#100#1=20#2=5#3=30", sessionToken1)).isTrue();
        assertThat(VectorSessionToken.tryCreate("1#102#1=100#2=8#3=30", sessionToken2)).isTrue();

        assertThat(sessionToken1.v.equals(sessionToken2.v)).isFalse();
        assertThat(sessionToken2.v.equals(sessionToken1.v)).isFalse();
        assertThat(sessionToken1.v.isValid(sessionToken2.v)).isTrue();
        assertThat(sessionToken2.v.isValid(sessionToken1.v)).isFalse();

        assertThat(VectorSessionToken.tryCreate("1#102#2=8#3=30#1=100", sessionTokenMerged)).isTrue();

        assertThat(sessionTokenMerged.v.equals(sessionToken1.v.merge(sessionToken2.v))).isTrue();

        assertThat(VectorSessionToken.tryCreate("1#101#1=20#2=5#3=30", sessionToken1)).isTrue();
        assertThat(VectorSessionToken.tryCreate("1#100#1=20#2=5#3=30#4=40", sessionToken2)).isTrue();


        try {
            sessionToken1.v.merge(sessionToken2.v);
            fail("Region progress can not be different when version is same");
        } catch (InternalServerErrorException e) {
        }

        try {
            sessionToken2.v.isValid(sessionToken1.v);
            fail("Region progress can not be different when version is same");
        } catch (InternalServerErrorException e) {
        }
    }

    @Test(groups = "unit", dataProvider = "isSessionTokenFalseProgressMergeEnabled")
    public void invalidRegionsInSessionTokenTests(boolean isSessionTokenFalseProgressMergeEnabled) {

        try {

            System.setProperty("COSMOS.IS_SESSION_TOKEN_FALSE_PROGRESS_MERGE_ENABLED", isSessionTokenFalseProgressMergeEnabled ? "true" : "false");

            // same version but different number of regions
            String sessionToken1 = "1#100#1=20#2=5#3=30";
            String sessionToken2 = "1#100#1=20#2=5";
            // same version but different regions
            String sessionToken3 = "1#100#1=20#2=5#3=30";
            String sessionToken4 = "1#100#4=20#5=5#6=6";

            for (Pair<String, String> testCase : Arrays.asList(Pair.of(sessionToken1, sessionToken2), Pair.of(sessionToken3, sessionToken4))) {
                try {
                    ValueHolder<ISessionToken> token1 = new ValueHolder<>(null);
                    ValueHolder<ISessionToken> token2 = new ValueHolder<>(null);

                    VectorSessionToken.tryCreate(testCase.getLeft(), token1);
                    VectorSessionToken.tryCreate(testCase.getRight(), token2);

                    token1.v.isValid(token2.v);
                    Assert.fail("Test should have failed due to invalid regions.");
                } catch (InternalServerErrorException exception) {
                    assertThat(exception.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR);
                    assertThat(exception.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.INVALID_REGIONS_IN_SESSION_TOKEN);
                    assertThat(exception.getMessage()).contains(
                        String.format(
                            RMResources.InvalidRegionsInSessionToken,
                            testCase.getLeft(),
                            testCase.getRight()));
                }
            }
        } finally {
            System.clearProperty("COSMOS.IS_SESSION_TOKEN_FALSE_PROGRESS_MERGE_ENABLED");
        }

    }

    @Test(groups = "unit", dataProvider = "isSessionTokenFalseProgressMergeEnabled")
    public void mergeWithInvalidToken(boolean isSessionTokenFalseProgressMergeEnabled) {

        try {

            System.setProperty("COSMOS.IS_SESSION_TOKEN_FALSE_PROGRESS_MERGE_ENABLED", isSessionTokenFalseProgressMergeEnabled ? "true" : "false");

            // same version but different number of regions
            String sessionToken1 = "1#100#1=20#2=5#3=30";
            String sessionToken2 = "1#100#1=20#2=5";
            // same version but different regions
            String sessionToken3 = "1#100#1=20#2=5#3=30";
            String sessionToken4 = "1#100#4=20#5=5#6=6";

            for (Pair<String, String> testCase : Arrays.asList(Pair.of(sessionToken1, sessionToken2), Pair.of(sessionToken3, sessionToken4))) {
                try {
                    ValueHolder<ISessionToken> token1 = new ValueHolder<>(null);
                    ValueHolder<ISessionToken> token2 = new ValueHolder<>(null);

                    VectorSessionToken.tryCreate(testCase.getLeft(), token1);
                    VectorSessionToken.tryCreate(testCase.getRight(), token2);

                    token1.v.merge(token2.v);
                    Assert.fail("Test should have failed due to invalid regions.");
                } catch (InternalServerErrorException exception) {
                    assertThat(exception.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR);
                    assertThat(exception.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.INVALID_REGIONS_IN_SESSION_TOKEN);
                    assertThat(exception.getMessage()).contains(
                        String.format(
                            RMResources.InvalidRegionsInSessionToken,
                            testCase.getLeft(),
                            testCase.getRight()));
                }
            }

        } finally {
            System.clearProperty("COSMOS.IS_SESSION_TOKEN_FALSE_PROGRESS_MERGE_ENABLED");
        }
    }

    @Test(groups = "unit")
    public void validateSessionTokenMergeDuringFalseProgress() {

        try {

            System.setProperty("COSMOS.IS_SESSION_TOKEN_FALSE_PROGRESS_MERGE_ENABLED", "true");

            // first seen session token at T1
            ValueHolder<ISessionToken> sessionToken1 = new ValueHolder<>(null);

            // first seen session token at T2
            // assume T2 > T1 (T1 and T2 are wall clock time)
            ValueHolder<ISessionToken> sessionToken2 = new ValueHolder<>(null);

            // same vector clock version with GLSN increasing monotonicity violation
            VectorSessionToken.tryCreate("1#100#1=20#2=5", sessionToken1);
            VectorSessionToken.tryCreate("1#97#1=20#2=5", sessionToken2);

            // if isValid is false, no need to test merge as merge flow won't be hit
            // isValid flow triggered in ConsistencyReader / StoreReader and can trigger 404:1002 replica loop through
            // before it reaches merge (outer layer)
            assertThat(sessionToken1.v.isValid(sessionToken2.v)).isFalse();

            // different vector clock version with GLSN compaction (causes GLSN after failover to have lower value) post failover
            // failover causes vector clock version to increment
            VectorSessionToken.tryCreate("1#100#1=20#2=5", sessionToken1);
            VectorSessionToken.tryCreate("2#97#1=20#2=5", sessionToken2);

            assertThat(sessionToken1.v.isValid(sessionToken2.v)).isTrue();

            // with false progress compatible merge - merge will choose higher vector clock version's GLSN post failover always
            assertThat(sessionToken1.v.merge(sessionToken2.v).convertToString()).isEqualTo("2#97#1=20#2=5");

            // same vector clock version with GLSN increase (no failover)
            VectorSessionToken.tryCreate("1#100#1=20#2=5", sessionToken1);
            VectorSessionToken.tryCreate("1#197#1=20#2=5", sessionToken2);

            assertThat(sessionToken1.v.isValid(sessionToken2.v)).isTrue();

            // with false progress compatible merge - merge will choose higher GLSN if no failover happened
            assertThat(sessionToken1.v.merge(sessionToken2.v).convertToString()).isEqualTo("1#197#1=20#2=5");

            // same vector clock version with GLSN increase and LLSN increase
            VectorSessionToken.tryCreate("1#100#1=20#2=5", sessionToken1);
            VectorSessionToken.tryCreate("1#197#1=23#2=15", sessionToken2);

            assertThat(sessionToken1.v.isValid(sessionToken2.v)).isTrue();
            assertThat(sessionToken1.v.merge(sessionToken2.v).convertToString()).isEqualTo("1#197#1=23#2=15");
        }  finally {
            System.clearProperty("COSMOS.IS_SESSION_TOKEN_FALSE_PROGRESS_MERGE_ENABLED");
        }
    }
}
