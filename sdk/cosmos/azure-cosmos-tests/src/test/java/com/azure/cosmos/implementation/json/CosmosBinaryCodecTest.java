// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public final class CosmosBinaryCodecTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test(groups = "unit")
    public void roundTripsCoreValuesAndNativeBinary() {
        byte[] payload = sequence(300);
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("empty", null);
        value.put("enabled", true);
        value.put("negative", -42L);
        value.put("ratio", 1.25d);
        value.put("values", Arrays.asList("one", false, 9L));
        value.put("payload", CosmosBinary.fromBytes(payload));

        Object decoded = CosmosBinaryReader.decode(CosmosBinaryWriter.encode(value));

        assertThat(decoded).isInstanceOf(Map.class);
        Map<?, ?> result = (Map<?, ?>) decoded;
        assertThat(result.get("empty")).isNull();
        assertThat(result.get("enabled")).isEqualTo(true);
        assertThat(result.get("negative")).isEqualTo(-42L);
        assertThat(result.get("ratio")).isEqualTo(1.25d);
        assertThat(result.get("values")).isEqualTo(Arrays.asList("one", false, 9L));
        assertThat(result.get("payload")).isEqualTo(CosmosBinary.fromBytes(payload));
    }

    @Test(groups = "unit")
    public void roundTripsLargeNestedScopesWithFourByteLengths() {
        byte[] payload = sequence(70_000);
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("payload", CosmosBinary.fromBytes(payload));
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("items", Arrays.asList(nested, "tail"));
        root.put("enabled", true);

        Object decoded = CosmosBinaryReader.decode(CosmosBinaryWriter.encode(root));

        Map<?, ?> decodedRoot = (Map<?, ?>) decoded;
        List<?> items = (List<?>) decodedRoot.get("items");
        Map<?, ?> decodedNested = (Map<?, ?>) items.get(0);
        assertThat(((CosmosBinary) decodedNested.get("payload")).toByteArray()).isEqualTo(payload);
        assertThat(items.get(1)).isEqualTo("tail");
    }

    @Test(groups = "unit")
    public void pojoByteArrayBecomesNativeBinaryInsteadOfBase64String() throws Exception {
        PayloadItem item = new PayloadItem();
        item.id = "jackson-probe";
        item.payload = new byte[] { 0, 1, 2, 3, (byte) 0xFF };
        JsonNode itemTree = MAPPER.valueToTree(item);

        byte[] encoded = CosmosBinaryJacksonCodec.encode(itemTree);
        JsonNode decoded = CosmosBinaryJacksonCodec.decode(encoded);

        assertThat(itemTree.path("payload").isBinary()).isTrue();
        assertThat(decoded.path("payload").isBinary()).isTrue();
        assertThat(decoded.path("payload").binaryValue()).isEqualTo(item.payload);
        assertThat(decoded.path("id").textValue()).isEqualTo(item.id);
        assertThat(encoded).contains((byte) 0xDD);
        assertThat(new String(encoded, StandardCharsets.ISO_8859_1))
            .doesNotContain(java.util.Base64.getEncoder().encodeToString(item.payload));
    }

    @Test(groups = "unit")
    public void roundTripsUnsigned64BitValues() {
        BigInteger value = new BigInteger("18446744073709551615");

        byte[] encoded = CosmosBinaryWriter.encode(value);

        assertThat(encoded[1] & 0xFF).isEqualTo(0xC7);
        assertThat(CosmosBinaryReader.decode(encoded)).isEqualTo(value);
        assertThat(CosmosBinaryJacksonCodec.decode(encoded).bigIntegerValue()).isEqualTo(value);
    }

    @Test(groups = "unit")
    public void decodesFloat16AndGuidScalars() {
        assertThat(CosmosBinaryReader.decode(new byte[] { (byte) 0x80, (byte) 0xCF, 0x00, 0x3C }))
            .isEqualTo(1.0d);
        assertThat(CosmosBinaryReader.decode(new byte[] { (byte) 0x80, (byte) 0xCF, 0x00, (byte) 0xC0 }))
            .isEqualTo(-2.0d);
        assertThat(CosmosBinaryReader.decode(new byte[] {
            (byte) 0x80, (byte) 0xD3,
            0x33, 0x22, 0x11, 0x00, 0x55, 0x44, 0x77, 0x66,
            (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB,
            (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF
        })).isEqualTo("00112233-4455-6677-8899-aabbccddeeff");
    }

    @Test(groups = "unit")
    public void rejectsExcessiveNestingAndMalformedUtf8() {
        byte[] nested = new byte[131];
        nested[0] = (byte) 0x80;
        java.util.Arrays.fill(nested, 1, 130, (byte) 0xE1);
        nested[130] = (byte) 0xD0;
        assertThatThrownBy(() -> CosmosBinaryReader.decode(nested))
            .hasMessageContaining("Maximum nesting depth");

        List<Object> value = new java.util.ArrayList<>();
        List<Object> root = value;
        for (int index = 0; index < 130; index++) {
            List<Object> child = new java.util.ArrayList<>();
            value.add(child);
            value = child;
        }
        assertThatThrownBy(() -> CosmosBinaryWriter.encode(root))
            .hasMessageContaining("Maximum nesting depth");

        assertThatThrownBy(() -> CosmosBinaryReader.decode(new byte[] {
            (byte) 0x80, (byte) 0x82, (byte) 0xC3, 0x28
        })).hasMessageContaining("Invalid UTF-8");
    }

    @Test(groups = "unit")
    public void rejectsCountsThatExceedAvailablePayloadBeforeAllocating() {
        assertThatThrownBy(() -> CosmosBinaryReader.decode(new byte[] {
            (byte) 0x80, (byte) 0xE7,
            0x00, 0x00, 0x00, 0x00,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x7F
        })).hasMessageContaining("Array count exceeds payload");
        assertThatThrownBy(() -> CosmosBinaryReader.decode(new byte[] {
            (byte) 0x80, (byte) 0xF3, (byte) 0xF1, (byte) 0xD7,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        })).hasMessageContaining("Uniform nested array count exceeds input");
    }

    @Test(groups = "unit")
    public void rejectsTruncatedAndOverflowingValues() {
        assertThatThrownBy(() -> CosmosBinaryReader.decode(new byte[] {
            (byte) 0x80, (byte) 0xDF, 0x01, 0x00, 0x00, 0x00
        })).hasMessageContaining("Unexpected end");
        assertThatThrownBy(() -> CosmosBinaryReader.decode(new byte[] {
            (byte) 0x80, (byte) 0xEC, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x7F
        })).hasMessageContaining("Scope exceeds input");
        assertThatThrownBy(() -> CosmosBinaryReader.decode(new byte[] {
            (byte) 0x80, (byte) 0xC3, (byte) 0xFF
        })).hasMessageContaining("target exceeds input");
    }

    @Test(groups = "unit")
    public void decodesCompressedStrings() {
        assertThat(CosmosBinaryReader.decode(new byte[] {
            (byte) 0x80, 0x78, 0x08, 0x10, 0x32, (byte) 0xBA, (byte) 0xDC
        })).isEqualTo("0123abcd");
        assertThat(CosmosBinaryReader.decode(new byte[] {
            (byte) 0x80, 0x7B, 0x05, 0x41, 0x10, 0x32, 0x04
        })).isEqualTo("ABCDE");
        assertThat(CosmosBinaryReader.decode(new byte[] {
            (byte) 0x80, 0x7A, 0x04, 0x21, 0x43
        })).isEqualTo("0123");
    }

    @Test(groups = "unit")
    public void decodesEncodedGuidAndBase64Strings() {
        assertThat(CosmosBinaryReader.decode(new byte[] {
            (byte) 0x80, 0x75,
            0x10, 0x32, 0x54, 0x76, (byte) 0x98, (byte) 0xBA, (byte) 0xDC, (byte) 0xFE,
            0x10, 0x32, 0x54, 0x76, (byte) 0x98, (byte) 0xBA, (byte) 0xDC, (byte) 0xFE
        })).isEqualTo("01234567-89ab-cdef-0123-456789abcdef");
        assertThat(CosmosBinaryReader.decode(new byte[] {
            (byte) 0x80, 0x71, 0x02, 0x01, 'h', 'e', 'l', 'l', 'o'
        })).isEqualTo("aGVsbG8=");
        assertThat(CosmosBinaryReader.decode(new byte[] {
            (byte) 0x80, 0x71, 0x02, (byte) 0xFE, 'h', 'e', 'l', 'l', 'o'
        })).isEqualTo("aGVsbG8");
    }

    @Test(groups = "unit")
    public void decodesUniformNumericArrays() {
        assertThat(CosmosBinaryReader.decode(new byte[] {
            (byte) 0x80, (byte) 0xF0, (byte) 0xD7, 0x03, 0x01, 0x02, (byte) 0xFF
        })).isEqualTo(Arrays.asList(1L, 2L, 255L));
        assertThat(CosmosBinaryReader.decode(new byte[] {
            (byte) 0x80, (byte) 0xF2, (byte) 0xF0, (byte) 0xD8,
            0x02, 0x02, 0x01, (byte) 0xFF, 0x02, (byte) 0xFE
        })).isEqualTo(Arrays.asList(Arrays.asList(1L, -1L), Arrays.asList(2L, -2L)));
    }

    @Test(groups = "unit")
    public void countedObjectAllowsDuplicateKeys() {
        byte[] document = new byte[] {
            (byte) 0x80, (byte) 0xED, 0x06, 0x02,
            (byte) 0x81, 'a', 0x01,
            (byte) 0x81, 'a', 0x02
        };

        assertThat(CosmosBinaryReader.decode(document))
            .isEqualTo(java.util.Collections.singletonMap("a", 2L));
    }

    @Test(groups = "unit")
    public void decodesReferenceStrings() {
        byte[] document = concat(
            new byte[] { (byte) 0x80, (byte) 0xEA, 0x16, (byte) 0x85 },
            "first".getBytes(StandardCharsets.UTF_8),
            new byte[] { (byte) 0x86 },
            "repeat".getBytes(StandardCharsets.UTF_8),
            new byte[] { (byte) 0x86 },
            "second".getBytes(StandardCharsets.UTF_8),
            new byte[] { (byte) 0xC3, 0x09 });

        Object decoded = CosmosBinaryReader.decode(document);

        assertThat(decoded).isInstanceOf(Map.class);
        Map<?, ?> values = (Map<?, ?>) decoded;
        assertThat(values.get("first")).isEqualTo("repeat");
        assertThat(values.get("second")).isEqualTo("repeat");
    }

    @Test(groups = "unit")
    public void rejectsReferenceToAnotherReference() {
        byte[] document = new byte[] {
            (byte) 0x80, (byte) 0xE1, (byte) 0xC3, 0x04, (byte) 0xC3, 0x02
        };

        assertThatThrownBy(() -> CosmosBinaryReader.decode(document))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("another reference");
    }

    @Test(groups = "unit")
    public void everyMarkerEitherDecodesOrFailsWithRuntimeException() {
        for (int marker = 0; marker <= 0xFF; marker++) {
            try {
                CosmosBinaryReader.decode(new byte[] { (byte) 0x80, (byte) marker });
            } catch (RuntimeException expected) {
                // Unsupported or incomplete one-byte values must fail without Error subclasses or hangs.
            }
        }
    }

    @Test(groups = "unit")
    public void rejectsDocumentTruncatedAtEveryBoundary() {
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("payload", CosmosBinary.fromBytes(sequence(300)));
        nested.put("values", Arrays.asList(-1L, 0L, 256L, "multibyte-€"));
        byte[] document = CosmosBinaryWriter.encode(Collections.singletonMap("nested", nested));

        for (int length = 0; length < document.length; length++) {
            int truncationLength = length;
            byte[] truncated = Arrays.copyOf(document, truncationLength);
            assertThatThrownBy(() -> CosmosBinaryReader.decode(truncated))
                .as("truncation at byte %s", truncationLength)
                .isInstanceOf(RuntimeException.class);
        }
    }

    @Test(groups = "unit")
    public void rejectsAmbiguousByteArrays() {
        assertThatThrownBy(() -> CosmosBinaryWriter.encode(new byte[] { 1, 2, 3 }))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("CosmosBinary.fromBytes");
    }

    private static byte[] concat(byte[]... values) {
        int length = 0;
        for (byte[] value : values) {
            length += value.length;
        }
        byte[] result = new byte[length];
        int offset = 0;
        for (byte[] value : values) {
            System.arraycopy(value, 0, result, offset, value.length);
            offset += value.length;
        }
        return result;
    }

    private static byte[] sequence(int length) {
        byte[] value = new byte[length];
        for (int index = 0; index < value.length; index++) {
            value[index] = (byte) index;
        }
        return value;
    }

    static final class PayloadItem {
        public String id;
        public byte[] payload;
    }
}
