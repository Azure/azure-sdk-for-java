// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import org.testng.annotations.Test;

import java.util.UUID;

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

    @Test(groups = { "unit" })
    public void uuidConversion() {
        // Guid and expected byte-array generated with .Net Guid.ToByteArray is
        // the expected binary format in the backend
        UUID id = UUID.fromString("8f3322cc-1786-4db4-9b97-b229c2c6f0aa");
        byte[] uuidBlob = RntbdUUID.encode(id);

        assertThat(uuidBlob).hasSize(16);
        assertThat(Byte.toUnsignedInt(uuidBlob[0])).isEqualTo(204);
        assertThat(Byte.toUnsignedInt(uuidBlob[1])).isEqualTo(34);
        assertThat(Byte.toUnsignedInt(uuidBlob[2])).isEqualTo(51);
        assertThat(Byte.toUnsignedInt(uuidBlob[3])).isEqualTo(143);
        assertThat(Byte.toUnsignedInt(uuidBlob[4])).isEqualTo(134);
        assertThat(Byte.toUnsignedInt(uuidBlob[5])).isEqualTo(23);
        assertThat(Byte.toUnsignedInt(uuidBlob[6])).isEqualTo(180);
        assertThat(Byte.toUnsignedInt(uuidBlob[7])).isEqualTo(77);
        assertThat(Byte.toUnsignedInt(uuidBlob[8])).isEqualTo(155);
        assertThat(Byte.toUnsignedInt(uuidBlob[9])).isEqualTo(151);
        assertThat(Byte.toUnsignedInt(uuidBlob[10])).isEqualTo(178);
        assertThat(Byte.toUnsignedInt(uuidBlob[11])).isEqualTo(41);
        assertThat(Byte.toUnsignedInt(uuidBlob[12])).isEqualTo(194);
        assertThat(Byte.toUnsignedInt(uuidBlob[13])).isEqualTo(198);
        assertThat(Byte.toUnsignedInt(uuidBlob[14])).isEqualTo(240);
        assertThat(Byte.toUnsignedInt(uuidBlob[15])).isEqualTo(170);

        UUID decodedId = RntbdUUID.decode(uuidBlob);
        assertThat(decodedId.toString()).isEqualTo(id.toString());
    }

    private static void testById(RntbdTokenType rntbdTokenType) {
        RntbdTokenType returnedTokenType = RntbdTokenType.fromId(rntbdTokenType.id());

        assertThat(rntbdTokenType).isEqualTo(returnedTokenType);
    }
}
