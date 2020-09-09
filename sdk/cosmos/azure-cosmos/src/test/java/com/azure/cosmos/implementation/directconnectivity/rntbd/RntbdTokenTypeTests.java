// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import org.testng.annotations.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class RntbdTokenTypeTests {

    @Test(groups = { "unit" })
    public void allTokenTypes() {
        RntbdTokenTypeTests.testById(RntbdTokenType.Byte);
        RntbdTokenTypeTests.testById(RntbdTokenType.UShort);
        RntbdTokenTypeTests.testById(RntbdTokenType.ULong);
        RntbdTokenTypeTests.testById(RntbdTokenType.Long);
        RntbdTokenTypeTests.testById(RntbdTokenType.ULongLong);
        RntbdTokenTypeTests.testById(RntbdTokenType.LongLong);
        RntbdTokenTypeTests.testById(RntbdTokenType.Guid);
        RntbdTokenTypeTests.testById(RntbdTokenType.SmallString);
        RntbdTokenTypeTests.testById(RntbdTokenType.String);
        RntbdTokenTypeTests.testById(RntbdTokenType.ULongString);
        RntbdTokenTypeTests.testById(RntbdTokenType.SmallBytes);
        RntbdTokenTypeTests.testById(RntbdTokenType.Bytes);
        RntbdTokenTypeTests.testById(RntbdTokenType.ULongBytes);
        RntbdTokenTypeTests.testById(RntbdTokenType.Float);
        RntbdTokenTypeTests.testById(RntbdTokenType.Double);
        RntbdTokenTypeTests.testById(RntbdTokenType.Invalid);
    }

    private static void testById(RntbdTokenType rntbdTokenType) {
        RntbdTokenType returnedTokenType = RntbdTokenType.fromId(rntbdTokenType.id());

        assertThat(rntbdTokenType).isEqualTo(returnedTokenType);
    }
}
