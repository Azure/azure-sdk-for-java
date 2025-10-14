// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import org.testng.annotations.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link HexConvert}
 */
public class HexConvertTest {
    @Test(groups = "unit")
    public void testHexToBytes_ValidInput() {
        // Test basic conversion
        assertThat(HexConvert.hexToBytes("00")).isEqualTo(new byte[]{0x00});
        assertThat(HexConvert.hexToBytes("0F")).isEqualTo(new byte[]{0x0F});
        assertThat(HexConvert.hexToBytes("FF")).isEqualTo(new byte[]{(byte) 0xFF});

        // Test multi-byte conversion
        assertThat(HexConvert.hexToBytes("123456ABCDEF"))
            .isEqualTo(new byte[]{0x12, 0x34, 0x56, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF});

        // Test case insensitivity
        assertThat(HexConvert.hexToBytes("0A0B0C")).isEqualTo(new byte[]{0x0a, 0x0b, 0x0c});
        assertThat(HexConvert.hexToBytes("0a0b0c")).isEqualTo(new byte[]{0x0a, 0x0b, 0x0c});
    }

    @Test(groups = "unit")
    public void testHexToBytes_EmptyString() {
        // Test empty string
        assertThat(HexConvert.hexToBytes("")).isEqualTo(new byte[0]);
    }

    @Test(groups = "unit")
    public void testHexToBytes_OddLength() {
        // Test odd length string throws exception
        assertThatThrownBy(() -> HexConvert.hexToBytes("0"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid hexadecimal String");

        assertThatThrownBy(() -> HexConvert.hexToBytes("ABC"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid hexadecimal String");
    }

    @Test(groups = "unit")
    public void testHexToBytes_InvalidCharacters() {
        // Test non-hex characters throw exception
        assertThatThrownBy(() -> HexConvert.hexToBytes("0G"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid Hexadecimal Character");

        assertThatThrownBy(() -> HexConvert.hexToBytes("XY"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid Hexadecimal Character");
    }

    @Test(groups = "unit")
    public void testHexToBytes_SpecialValues() {
        // Test boundary values
        assertThat(HexConvert.hexToBytes("7F")).isEqualTo(new byte[]{0x7F});
        assertThat(HexConvert.hexToBytes("80")).isEqualTo(new byte[]{(byte) 0x80});

        // Test full range byte values
        byte[] expected = new byte[256];
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < 256; i++) {
            expected[i] = (byte) i;
            hexString.append(String.format("%02X", i));
        }
        assertThat(HexConvert.hexToBytes(hexString.toString())).isEqualTo(expected);
    }

    @Test(groups = "unit")
    public void testBytesToHex_ByteBuffer_EmptyBuffer() {
        ByteBuffer emptyBuffer = ByteBuffer.wrap(new byte[0]);
        assertThat(HexConvert.bytesToHex(emptyBuffer)).isEqualTo("");
    }

    @Test(groups = "unit")
    public void testBytesToHex_ByteBuffer_SingleByte() {
        // Test single byte values
        ByteBuffer buffer1 = ByteBuffer.wrap(new byte[]{0x00});
        assertThat(HexConvert.bytesToHex(buffer1)).isEqualTo("00");

        ByteBuffer buffer2 = ByteBuffer.wrap(new byte[]{0x0F});
        assertThat(HexConvert.bytesToHex(buffer2)).isEqualTo("0F");

        ByteBuffer buffer3 = ByteBuffer.wrap(new byte[]{(byte)0xFF});
        assertThat(HexConvert.bytesToHex(buffer3)).isEqualTo("FF");
    }

    @Test(groups = "unit")
    public void testBytesToHex_ByteBuffer_MultipleByte() {
        // Test multiple byte conversion
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{0x12, 0x34, 0x56, (byte)0xAB, (byte)0xCD, (byte)0xEF});
        assertThat(HexConvert.bytesToHex(buffer)).isEqualTo("123456ABCDEF");
    }

    @Test(groups = "unit")
    public void testBytesToHex_ByteBuffer_AllByteValues() {
        // Test all possible byte values
        byte[] allBytes = new byte[256];
        StringBuilder expected = new StringBuilder();

        for (int i = 0; i < 256; i++) {
            allBytes[i] = (byte)i;
            expected.append(String.format("%02X", i));
        }

        ByteBuffer buffer = ByteBuffer.wrap(allBytes);
        assertThat(HexConvert.bytesToHex(buffer)).isEqualTo(expected.toString());
    }
}
