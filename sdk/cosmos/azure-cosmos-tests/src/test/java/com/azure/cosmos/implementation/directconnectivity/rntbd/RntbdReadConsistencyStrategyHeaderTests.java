// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.ReadConsistencyStrategy;
import com.azure.cosmos.implementation.HttpConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class RntbdReadConsistencyStrategyHeaderTests {

    @DataProvider(name = "readConsistencyStrategyValues")
    public Object[][] readConsistencyStrategyValues() {
        return new Object[][] {
            { ReadConsistencyStrategy.EVENTUAL, "Eventual" },
            { ReadConsistencyStrategy.SESSION, "Session" },
            { ReadConsistencyStrategy.LATEST_COMMITTED, "LatestCommitted" },
            { ReadConsistencyStrategy.GLOBAL_STRONG, "GlobalStrong" },
            { ReadConsistencyStrategy.DEFAULT, "Default" },
        };
    }

    @Test(groups = { "unit" }, dataProvider = "readConsistencyStrategyValues")
    public void readConsistencyStrategyTokenEncodesCorrectly(
        ReadConsistencyStrategy strategy,
        String expectedOverWireValue) {

        RntbdToken token = RntbdToken.create(
            RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy);
        assertThat(token).isNotNull();
        assertThat(token.isPresent()).isFalse();

        token.setValue(expectedOverWireValue);
        assertThat(token.isPresent()).isTrue();
        assertThat(token.getValue()).isEqualTo(expectedOverWireValue);
    }

    @Test(groups = { "unit" })
    public void readConsistencyStrategyHeaderId() {
        assertThat(RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy.id())
            .isEqualTo((short) 0x00F0);
    }

    @Test(groups = { "unit" })
    public void readConsistencyStrategyHeaderType() {
        assertThat(RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy.type())
            .isEqualTo(RntbdTokenType.String);
    }

    @Test(groups = { "unit" })
    public void readConsistencyStrategyHeaderNotRequired() {
        assertThat(RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy.isRequired())
            .isFalse();
    }

    @Test(groups = { "unit" })
    public void readConsistencyStrategyTokenNotPresentWhenNotSet() {
        RntbdToken token = RntbdToken.create(
            RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy);
        assertThat(token.isPresent()).isFalse();
    }

    @Test(groups = { "unit" }, dataProvider = "readConsistencyStrategyValues")
    public void readConsistencyStrategyTokenRoundTrips(
        ReadConsistencyStrategy strategy,
        String expectedOverWireValue) {

        // Encode
        RntbdToken token = RntbdToken.create(
            RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy);
        token.setValue(expectedOverWireValue);

        ByteBuf buffer = Unpooled.buffer(256);
        try {
            token.encode(buffer);

            // Decode
            RntbdToken decodedToken = RntbdToken.create(
                RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy);
            // skip 3 bytes: 2 for header id + 1 for token type
            buffer.readerIndex(3);
            decodedToken.decode(buffer);

            assertThat(decodedToken.isPresent()).isTrue();
            assertThat(decodedToken.getValue().toString()).isEqualTo(expectedOverWireValue);
        } finally {
            buffer.release();
        }
    }

    @Test(groups = { "unit" })
    public void readConsistencyStrategyOverWireValuesMatchEnum() {
        assertThat(ReadConsistencyStrategy.EVENTUAL.toString()).isEqualTo("Eventual");
        assertThat(ReadConsistencyStrategy.SESSION.toString()).isEqualTo("Session");
        assertThat(ReadConsistencyStrategy.LATEST_COMMITTED.toString()).isEqualTo("LatestCommitted");
        assertThat(ReadConsistencyStrategy.GLOBAL_STRONG.toString()).isEqualTo("GlobalStrong");
        assertThat(ReadConsistencyStrategy.DEFAULT.toString()).isEqualTo("Default");
    }

    @Test(groups = { "unit" })
    public void readConsistencyStrategyHttpHeaderConstant() {
        assertThat(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY)
            .isEqualTo("x-ms-cosmos-read-consistency-strategy");
    }
}
