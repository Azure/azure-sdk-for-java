// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.util.HashMap;

public class JsonNodeStorePayloadTests {
    @Test(groups = {"unit"})
    @Ignore("fallbackCharsetDecoder will only be initialized during the first time when JsonNodeStorePayload loaded," +
        " need to figure out a way to reload the class")
    public void parsingBytesWithInvalidUT8Bytes() throws Exception {
        // the hex string represents an json with invalid UTF-8 characters
        // json_obj = {
        //    "id": "example_id",
        //    "content": "\xff\n\t\x07"  # Invalid UTF-8 byte, newline, tab, and BEL
        //}
        String invalidHexString = "7b226964223a20226578616d706c655f6964222c2022636f6e74656e74223a2022ff0a0907227d";
        System.setProperty("COSMOS.CHARSET_DECODER_ERROR_ACTION_ON_MALFORMED_INPUT", "REPLACE");
        System.setProperty("COSMOS.CHARSET_DECODER_ERROR_ACTION_ON_UNMAPPED_CHARACTER", "REPLACE");

        try {
            byte[] bytes = hexStringToByteArray(invalidHexString);
            ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
            JsonNodeStorePayload jsonNodeStorePayload = new JsonNodeStorePayload(new ByteBufInputStream(byteBuf), bytes.length, new HashMap<>(), null);
            jsonNodeStorePayload.getPayload().toString();
        } finally {
            System.clearProperty("COSMOS.CHARSET_DECODER_ERROR_ACTION_ON_MALFORMED_INPUT");
            System.clearProperty("COSMOS.CHARSET_DECODER_ERROR_ACTION_ON_UNMAPPED_CHARACTER");
        }
    }

    private static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                + Character.digit(hex.charAt(i+1), 16));
        }

        return data;
    }
}
