// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.CommonsBridgeInternal;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.PartitionKind;
import com.azure.data.cosmos.internal.Undefined;
import com.azure.data.cosmos.internal.RMResources;
import com.azure.data.cosmos.internal.routing.PartitionKeyInternal;
import com.azure.data.cosmos.internal.routing.PartitionKeyInternalHelper;
import com.azure.data.cosmos.internal.routing.PartitionKeyInternalUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.function.BiFunction;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

public class PartitionKeyInternalTest {

    /**
     * Tests serialization of empty partition key.
     */
    @Test(groups="unit")
    public void emptyPartitionKey() {
        String json = "[]";
        PartitionKeyInternal partitionKey = PartitionKeyInternal.fromJsonString(json);
        assertThat(partitionKey).isEqualTo(PartitionKeyInternal.getEmpty());
        assertThat(partitionKey.toJson()).isEqualTo("[]");
    }

    /**
     * Tests serialization of various types.
     */
    @Test(groups="unit")
    public void variousTypes() {
        String json = "[\"aa\", null, true, false, {}, 5, 5.5]";
        PartitionKeyInternal partitionKey = PartitionKeyInternal.fromJsonString(json);
        assertThat(partitionKey).isEqualTo(
                PartitionKeyInternal.fromObjectArray(
                        Lists.newArrayList(new Object[]{"aa", null, true, false, Undefined.Value(), 5, 5.5}), true));

        assertThat(partitionKey.toJson()).isEqualTo("[\"aa\",null,true,false,{},5.0,5.5]");
    }

    /**
     * Tests deserialization of empty string
     */
    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void deserializeEmptyString() {
        PartitionKeyInternal.fromJsonString("");
    }

    /**
     * Tests deserialization of null
     */
    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void deserializeNull() {
        PartitionKeyInternal.fromJsonString(null);
    }

    /**
     * Tests deserialization of invalid partition key
     */
    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void invalidString() {
        PartitionKeyInternal.fromJsonString("[aa]");
    }


    /**
     * Tests deserialization of invalid partition key
     */
    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void invalidNumber() {
        PartitionKeyInternal.fromJsonString("[1.a]");
    }

    /**
     * Tests deserialization of invalid partition key
     */
    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void missingBraces() {
        PartitionKeyInternal.fromJsonString("[{]");
    }

    /**
     * Missing Value
     */
    @Test(groups = "unit")
    public void missingValue() {
        try {
            PartitionKeyInternal.fromJsonString("");
            fail("should throw");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo(
                    String.format(
                            RMResources.UnableToDeserializePartitionKeyValue, ""));
        }
    }

    /**
     * Tests serialization of infinity value.
     */
    @Test(groups = "unit")
    public void maxValue() {
        String json = "\"Infinity\"";
        PartitionKeyInternal partitionKey = PartitionKeyInternal.fromJsonString(json);
        assertThat(partitionKey).isEqualTo(PartitionKeyInternal.ExclusiveMaximum);
    }

    /**
     * Tests serialization of minimum value.
     */
    @Test(groups = "unit")
    public void minValue() {
        String json = "[]";
        PartitionKeyInternal partitionKey = PartitionKeyInternal.fromJsonString(json);
        assertThat(partitionKey).isEqualTo(PartitionKeyInternal.InclusiveMinimum);
    }

    /**
     * Tests serialization of undefined value.
     */
    @Test(groups = "unit")
    public void undefinedValue() {
        String json = "[]";
        PartitionKeyInternal partitionKey = PartitionKeyInternal.fromJsonString(json);
        assertThat(partitionKey).isEqualTo(PartitionKeyInternal.Empty);
    }

    /**
     * Tests JsonConvert.DefaultSettings that could cause indentation.
     */
    @Test(groups="unit")
    public void jsonConvertDefaultSettings() {
        String json = "[123.0]";
        PartitionKeyInternal partitionKey = PartitionKeyInternal.fromJsonString(json);
        assertThat(partitionKey.toJson()).isEqualTo(json);
    }

    /**
     * Tests unicode characters in partition key
     */
    @Test(groups="unit")
    public void unicodeCharacters() {
        String json = "[\"电脑\"]";
        PartitionKeyInternal partitionKey = PartitionKeyInternal.fromJsonString(json);
        assertThat(partitionKey.toJson()).isEqualTo("[\"\u7535\u8111\"]");
    }

    /**
     * Tests partition key value comparisons.
     */
    @Test(groups="unit")
    public void comparison() {
        verifyComparison("[]", "[]", 0);
        verifyComparison("[]", "[{}]", -1);
        verifyComparison("[]", "[false]", -1);
        verifyComparison("[]", "[true]", -1);
        verifyComparison("[]", "[null]", -1);
        verifyComparison("[]", "[2]", -1);
        verifyComparison("[]", "[\"aa\"]", -1);
        verifyComparison("[]", "\"Infinity\"", -1);

        verifyComparison("[{}]", "[]", 1);
        verifyComparison("[{}]", "[{}]", 0);
        verifyComparison("[{}]", "[false]", -1);
        verifyComparison("[{}]", "[true]", -1);
        verifyComparison("[{}]", "[null]", -1);
        verifyComparison("[{}]", "[2]", -1);
        verifyComparison("[{}]", "[\"aa\"]", -1);
        verifyComparison("[{}]", "\"Infinity\"", -1);

        verifyComparison("[false]", "[]", 1);
        verifyComparison("[false]", "[{}]", 1);
        verifyComparison("[false]", "[null]", 1);
        verifyComparison("[false]", "[false]", 0);
        verifyComparison("[false]", "[true]", -1);
        verifyComparison("[false]", "[2]", -1);
        verifyComparison("[false]", "[\"aa\"]", -1);
        verifyComparison("[false]", "\"Infinity\"", -1);

        verifyComparison("[true]", "[]", 1);
        verifyComparison("[true]", "[{}]", 1);
        verifyComparison("[true]", "[null]", 1);
        verifyComparison("[true]", "[false]", 1);
        verifyComparison("[true]", "[true]", 0);
        verifyComparison("[true]", "[2]", -1);
        verifyComparison("[true]", "[\"aa\"]", -1);
        verifyComparison("[true]", "\"Infinity\"", -1);

        verifyComparison("[null]", "[]", 1);
        verifyComparison("[null]", "[{}]", 1);
        verifyComparison("[null]", "[null]", 0);
        verifyComparison("[null]", "[false]", -1);
        verifyComparison("[null]", "[true]", -1);
        verifyComparison("[null]", "[2]", -1);
        verifyComparison("[null]", "[\"aa\"]", -1);
        verifyComparison("[null]", "\"Infinity\"", -1);

        verifyComparison("[2]", "[]", 1);
        verifyComparison("[2]", "[{}]", 1);
        verifyComparison("[2]", "[null]", 1);
        verifyComparison("[2]", "[false]", 1);
        verifyComparison("[2]", "[true]", 1);
        verifyComparison("[1]", "[2]", -1);
        verifyComparison("[2]", "[2]", 0);
        verifyComparison("[3]", "[2]", 1);
        verifyComparison("[2.1234344]", "[2]", 1);
        verifyComparison("[2]", "[\"aa\"]", -1);
        verifyComparison("[2]", "\"Infinity\"", -1);

        verifyComparison("[\"aa\"]", "[]", 1);
        verifyComparison("[\"aa\"]", "[{}]", 1);
        verifyComparison("[\"aa\"]", "[null]", 1);
        verifyComparison("[\"aa\"]", "[false]", 1);
        verifyComparison("[\"aa\"]", "[true]", 1);
        verifyComparison("[\"aa\"]", "[2]", 1);
        verifyComparison("[\"\"]", "[\"aa\"]", -1);
        verifyComparison("[\"aa\"]", "[\"aa\"]", 0);
        verifyComparison("[\"b\"]", "[\"aa\"]", 1);
        verifyComparison("[\"aa\"]", "\"Infinity\"", -1);

        verifyComparison("\"Infinity\"", "[]", 1);
        verifyComparison("\"Infinity\"", "[{}]", 1);
        verifyComparison("\"Infinity\"", "[null]", 1);
        verifyComparison("\"Infinity\"", "[false]", 1);
        verifyComparison("\"Infinity\"", "[true]", 1);
        verifyComparison("\"Infinity\"", "[2]", 1);
        verifyComparison("\"Infinity\"", "[\"aa\"]", 1);
        verifyComparison("\"Infinity\"", "\"Infinity\"", 0);
    }

    /**
     * Tests that invalid partition key value will throw an exception.
     */
    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void invalidPartitionKeyValue() {
        PartitionKeyInternal.fromObjectArray(
                Lists.newArrayList(new Object[]{2, true, new StringBuilder()}), true);
    }

    /**
     * Tests {@link PartitionKeyInternal#contains(PartitionKeyInternal)} method.
     */
    @Test(groups="unit")
    public void contains() {
        BiFunction<String, String, Boolean> verifyContains = (parentPartitionKey, childPartitionKey) ->
                PartitionKeyInternal.fromJsonString(parentPartitionKey)
                        .contains(PartitionKeyInternal.fromJsonString(childPartitionKey));

        assertThat(verifyContains.apply("[]", "[]")).isTrue();
        assertThat(verifyContains.apply("[]", "[{}]")).isTrue();
        assertThat(verifyContains.apply("[]", "[null]")).isTrue();
        assertThat(verifyContains.apply("[]", "[true]")).isTrue();
        assertThat(verifyContains.apply("[]", "[false]")).isTrue();
        assertThat(verifyContains.apply("[]", "[2]")).isTrue();
        assertThat(verifyContains.apply("[]", "[\"fdfd\"]")).isTrue();

        assertThat(verifyContains.apply("[2]", "[]")).isFalse();
        assertThat(verifyContains.apply("[2]", "[2]")).isTrue();
        assertThat(verifyContains.apply("[2]", "[2, \"USA\"]")).isTrue();
        assertThat(verifyContains.apply("[1]", "[2, \"USA\"]")).isFalse();
    }

    @Test(groups="unit")
    public void invalidPartitionKeyValueNonStrict() {
        assertThat(PartitionKeyInternal.fromObjectArray(new Object[]{2, true, Undefined.Value()}, true))
                .isEqualTo(
                        PartitionKeyInternal.fromObjectArray(new Object[]{2, true, new StringBuilder()}, false));
    }

    /**
     * Tests constructing effective partition key value.
     */
    @Test(groups="unit")
    public void hashEffectivePartitionKey() {

        assertThat(PartitionKeyInternalHelper.getEffectivePartitionKeyString(PartitionKeyInternal.InclusiveMinimum, new PartitionKeyDefinition()))
                .isEqualTo(PartitionKeyInternalHelper.MinimumInclusiveEffectivePartitionKey);

        assertThat(
                PartitionKeyInternalHelper.getEffectivePartitionKeyString(PartitionKeyInternal.ExclusiveMaximum, new PartitionKeyDefinition()))
                .isEqualTo(PartitionKeyInternalHelper.MaximumExclusiveEffectivePartitionKey);

        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        partitionKeyDefinition.paths(Lists.newArrayList("/A", "/B", "/C", "/E", "/F", "/G"));

        PartitionKeyInternal partitionKey = PartitionKeyInternal.fromObjectArray(
                new Object[]{2, true, false, null, Undefined.Value(), "Привет!"}, true);
        String effectivePartitionKey = PartitionKeyInternalHelper.getEffectivePartitionKeyString(partitionKey, partitionKeyDefinition);

        assertThat(effectivePartitionKey).isEqualTo("05C1D19581B37C05C0000302010008D1A0D281D1B9D1B3D1B6D2832200");
    }

    @DataProvider(name = "v2ParamProvider")
    public Object[][] v2ParamProvider() {
        return new Object[][] {
                {"[5.0]", "19C08621B135968252FB34B4CF66F811"},
                { "[5.12312419050912359123]", "0EF2E2D82460884AF0F6440BE4F726A8"},
                {"[\"redmond\"]", "22E342F38A486A088463DFF7838A5963"},
                {"[true]", "0E711127C5B5A8E4726AC6DD306A3E59"},
                {"[false]", "2FE1BE91E90A3439635E0E9E37361EF2"},
                {"[]", ""},
                {"[null]", "378867E4430E67857ACE5C908374FE16"},
                {"[{}]", "11622DAA78F835834610ABE56EFF5CB5"},
                {"[5.0, \"redmond\", true, null]", "3032DECBE2AB1768D8E0AEDEA35881DF"},
                {"[\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"]",
                        "36375D21568760E891C9CB7002D5E059"},
        };
    }

    /**
     * Tests binary encoding of partition key
     */
    @Test(groups="unit", dataProvider = "v2ParamProvider")
    public void partitionKeyBinaryEncodingV2(String partitionKeyRangeJson, String expectedHexEncoding) {
        validateEffectivePartitionKeyV2(partitionKeyRangeJson, expectedHexEncoding);
    }

    /**
     * Tests that effective partition key produced by us and the backend is the same.
     */
    @Test(groups="unit")
    public void managedNativeCompatibility() {
        PartitionKeyInternal partitionKey =
                PartitionKeyInternal.fromJsonString("[\"по-русски\",null,true,false,{},5.5]");

        PartitionKeyDefinition pkDefinition = new PartitionKeyDefinition();
        pkDefinition.paths(ImmutableList.of("/field1", "/field2", "/field3", "/field4", "/field5", "/field6"));

        String effectivePartitionKey = PartitionKeyInternalHelper.getEffectivePartitionKeyString(partitionKey, pkDefinition);
        assertThat("05C1D39FA55F0408D1C0D1BF2ED281D284D282D282D1BBD1B9000103020005C016").isEqualTo(effectivePartitionKey);

        String latin = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz";
        String nonLatin = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяабвгдеёжзийклмнопрстуфхцчшщъыьэюяабвгдеёжзийклмнопрстуфхцчшщъыьэюяабвгдеёжзийклмнопрстуфхцчшщъыьэюя";

        verifyEffectivePartitionKeyEncoding(latin, 99, "05C19B2DC38FC00862636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F7071727374757600", false);
        verifyEffectivePartitionKeyEncoding(latin, 99, "072D8FA3228DD2A6C0A7129C845700E6", true);

        verifyEffectivePartitionKeyEncoding(latin, 100, "05C1DD5D8149640862636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767700", false);
        verifyEffectivePartitionKeyEncoding(latin, 100, "023D5F0B62EBEF22A43564F267193B4D", true);

        verifyEffectivePartitionKeyEncoding(latin, 101, "05C1DD5D8149640862636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767700", false);
        verifyEffectivePartitionKeyEncoding(latin, 101, "357D83181DB32D35F58CDA3C9F2E0742", true);

        verifyEffectivePartitionKeyEncoding(latin, 102, "05C1DD5D8149640862636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767700", false);
        verifyEffectivePartitionKeyEncoding(latin, 102, "12B320F72959AB449FD8E090C6B23B88", true);

        verifyEffectivePartitionKeyEncoding(latin, 103, "05C1DD5D8149640862636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767700", false);
        verifyEffectivePartitionKeyEncoding(latin, 103, "25FD21A31C69A8C8AD994F7FAC2B2B9F", true);

        verifyEffectivePartitionKeyEncoding(latin, 104, "05C1DD5D8149640862636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767700", false);
        verifyEffectivePartitionKeyEncoding(latin, 104, "1DC6FB1CF6E1228C506AA6C8735023C4", true);

        verifyEffectivePartitionKeyEncoding(latin, 105, "05C1DD5D8149640862636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767700", false);
        verifyEffectivePartitionKeyEncoding(latin, 105, "308E1E7870956CE5D9BDAD01200E09BD", true);

        verifyEffectivePartitionKeyEncoding(latin, 106, "05C1DD5D8149640862636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767700", false);
        verifyEffectivePartitionKeyEncoding(latin, 106, "362E21ABDEA7179DBDF7BF549DD8303B", true);

        verifyEffectivePartitionKeyEncoding(latin, 107, "05C1DD5D8149640862636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767700", false);
        verifyEffectivePartitionKeyEncoding(latin, 107, "1EBE932ECEFA4F53CE339D31B6BF53FD", true);

        verifyEffectivePartitionKeyEncoding(latin, 108, "05C1DD5D8149640862636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767700", false);
        verifyEffectivePartitionKeyEncoding(latin, 108, "3BFA3A6E9CBABA0EF756AEDEC66B1B3C", true);

        verifyEffectivePartitionKeyEncoding(latin, 109, "05C1DD5D8149640862636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767700", false);
        verifyEffectivePartitionKeyEncoding(latin, 109, "2880BF78DE0CE2CD1B0120EDA22601C4", true);

        verifyEffectivePartitionKeyEncoding(latin, 110, "05C1DD5D8149640862636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767700", false);
        verifyEffectivePartitionKeyEncoding(latin, 110, "1F3577D1D9CA7FC56100AED11F4DC646", true);

        verifyEffectivePartitionKeyEncoding(latin, 111, "05C1DD5D8149640862636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767700", false);
        verifyEffectivePartitionKeyEncoding(latin, 111, "205A9EB61F3B063E61C6ED655C9220E6", true);

        verifyEffectivePartitionKeyEncoding(latin, 112, "05C1DD5D8149640862636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767700", false);
        verifyEffectivePartitionKeyEncoding(latin, 112, "1152A43F1A852AFDDD4518C9CDD48616", true);

        verifyEffectivePartitionKeyEncoding(latin, 113, "05C1DD5D8149640862636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767700", false);
        verifyEffectivePartitionKeyEncoding(latin, 113, "38E2EB2EF54012B5CA40CDA34F1C7736", true);

        verifyEffectivePartitionKeyEncoding(latin, 114, "05C1DD5D8149640862636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767700", false);
        verifyEffectivePartitionKeyEncoding(latin, 114, "19BCC416843B9085DBBC18E8C7C80D72", true);

        verifyEffectivePartitionKeyEncoding(latin, 115, "05C1DD5D8149640862636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767700", false);
        verifyEffectivePartitionKeyEncoding(latin, 115, "03F1BB89FD8E9747B047281E80FA2E84", true);

        verifyEffectivePartitionKeyEncoding(latin, 116, "05C1DD5D8149640862636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767778797A7B62636465666768696A6B6C6D6E6F707172737475767700", false);
        verifyEffectivePartitionKeyEncoding(latin, 116, "2BA0757B833F3922A3CBBB6DDA3803B4", true);

        verifyEffectivePartitionKeyEncoding(nonLatin, 49, "05C1C1BD37FE08D1B1D1B2D1B3D1B4D1B5D1B6D292D1B7D1B8D1B9D1BAD1BBD1BCD1BDD1BED1BFD1C0D281D282D283D284D285D286D287D288D289D28AD28BD28CD28DD28ED28FD290D1B1D1B2D1B3D1B4D1B5D1B6D292D1B7D1B8D1B9D1BAD1BBD1BCD1BDD1BED1BF00", false);
        verifyEffectivePartitionKeyEncoding(nonLatin, 49, "3742C1AF65AFA809282539F4BCDF2F6F", true);

        verifyEffectivePartitionKeyEncoding(nonLatin, 50, "05C1B339EF472008D1B1D1B2D1B3D1B4D1B5D1B6D292D1B7D1B8D1B9D1BAD1BBD1BCD1BDD1BED1BFD1C0D281D282D283D284D285D286D287D288D289D28AD28BD28CD28DD28ED28FD290D1B1D1B2D1B3D1B4D1B5D1B6D292D1B7D1B8D1B9D1BAD1BBD1BCD1BDD1BED1BFD1C000", false);
        verifyEffectivePartitionKeyEncoding(nonLatin, 50, "399CF1F141E066E09CC7557EA7F0977A", true);

        verifyEffectivePartitionKeyEncoding(nonLatin, 51, "05C1EB1F29DBFA08D1B1D1B2D1B3D1B4D1B5D1B6D292D1B7D1B8D1B9D1BAD1BBD1BCD1BDD1BED1BFD1C0D281D282D283D284D285D286D287D288D289D28AD28BD28CD28DD28ED28FD290D1B1D1B2D1B3D1B4D1B5D1B6D292D1B7D1B8D1B9D1BAD1BBD1BCD1BDD1BED1BFD1C0D2", false);
        verifyEffectivePartitionKeyEncoding(nonLatin, 51, "2D63C2F5FDAC6EFE5660CD509A723A90", true);

        verifyEffectivePartitionKeyEncoding(nonLatin, 99, "05C1E72F79C71608D1B1D1B2D1B3D1B4D1B5D1B6D292D1B7D1B8D1B9D1BAD1BBD1BCD1BDD1BED1BFD1C0D281D282D283D284D285D286D287D288D289D28AD28BD28CD28DD28ED28FD290D1B1D1B2D1B3D1B4D1B5D1B6D292D1B7D1B8D1B9D1BAD1BBD1BCD1BDD1BED1BFD1C0D2", false);
        verifyEffectivePartitionKeyEncoding(nonLatin, 99, "1E9836D9BCB67FDB2B5C984BD40AFAF9", true);

        verifyEffectivePartitionKeyEncoding(nonLatin, 100, "05C1E3653D9F3E08D1B1D1B2D1B3D1B4D1B5D1B6D292D1B7D1B8D1B9D1BAD1BBD1BCD1BDD1BED1BFD1C0D281D282D283D284D285D286D287D288D289D28AD28BD28CD28DD28ED28FD290D1B1D1B2D1B3D1B4D1B5D1B6D292D1B7D1B8D1B9D1BAD1BBD1BCD1BDD1BED1BFD1C0D2", false);
        verifyEffectivePartitionKeyEncoding(nonLatin, 100, "16102F19448867537E51BB4377962AF9", true);

        verifyEffectivePartitionKeyEncoding(nonLatin, 101, "05C1E3653D9F3E08D1B1D1B2D1B3D1B4D1B5D1B6D292D1B7D1B8D1B9D1BAD1BBD1BCD1BDD1BED1BFD1C0D281D282D283D284D285D286D287D288D289D28AD28BD28CD28DD28ED28FD290D1B1D1B2D1B3D1B4D1B5D1B6D292D1B7D1B8D1B9D1BAD1BBD1BCD1BDD1BED1BFD1C0D2", false);
        verifyEffectivePartitionKeyEncoding(nonLatin, 101, "0B6D25D07748AB9CA0F523D4BAD146C8", true);
    }

    private static void validateEffectivePartitionKeyV2(String partitionKeyRangeJson, String expectedHexEncoding) {
        PartitionKeyInternal partitionKey = PartitionKeyInternal.fromJsonString(partitionKeyRangeJson);

        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        partitionKeyDefinition.kind(PartitionKind.HASH);
        CommonsBridgeInternal.setV2(partitionKeyDefinition);
        ArrayList<String> paths = new ArrayList<String>();
        for (int i = 0; i < partitionKey.getComponents().size(); i++) {
            paths.add("/path" + i);
        }

        if (paths.size() > 0) {
            partitionKeyDefinition.paths(paths);
        }

        String hexEncodedEffectivePartitionKey = PartitionKeyInternalHelper.getEffectivePartitionKeyString(partitionKey, partitionKeyDefinition);
        assertThat(hexEncodedEffectivePartitionKey).isEqualTo(expectedHexEncoding);
    }

    private void verifyComparison(String leftKey, String rightKey, int result) {
        assertThat(PartitionKeyInternal.fromJsonString(leftKey).
                compareTo(PartitionKeyInternal.fromJsonString(rightKey))).isEqualTo(result);
    }

    private static void verifyEffectivePartitionKeyEncoding(String buffer, int length, String expectedValue, boolean v2) {
        PartitionKeyDefinition pkDefinition = new PartitionKeyDefinition();
        pkDefinition.paths(ImmutableList.of("/field1"));
        if (v2) {
            CommonsBridgeInternal.setV2(pkDefinition);
        }

        PartitionKeyInternal pk = PartitionKeyInternalUtils.createPartitionKeyInternal(buffer.substring(0, length));
        assertThat(PartitionKeyInternalHelper.getEffectivePartitionKeyString(pk, pkDefinition)).isEqualTo(expectedValue);
    }
}