// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch.hybridrow;

import com.azure.cosmos.models.CosmosItemOperationType;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public final class HybridRowBatchCodecTest {
    private static final String OPERATION =
        "817054e17f4300000000020000004a80ea472c95687962726964726f772d676f6c64656e2d6974656d"
            + "8573686172649a687962726964726f772d676f6c64656e2d706172746974696f6e3c86676f6c64656e"
            + "8374746cc92c01";
    private static final String RECORD_IO =
        "81f0d8ff7f010a00000081f1d8ff7f590000009d24a5cc" + OPERATION;

    @Test(groups = "unit")
    public void matchesDotNetOperationAndRecordIo() {
        byte[] body = hex("80ea472c95687962726964726f772d676f6c64656e2d6974656d8573686172649a687962"
            + "726964726f772d676f6c64656e2d706172746974696f6e3c86676f6c64656e8374746cc92c01");

        byte[] operation = BatchOperationCodec.encode(
            new BatchOperationCodec.Operation(CosmosItemOperationType.CREATE).resourceBody(body));

        assertThat(operation).isEqualTo(hex(OPERATION));
        assertThat(RecordIoCodec.encode(Collections.singletonList(operation))).isEqualTo(hex(RECORD_IO));
        assertThat(RecordIoCodec.decode(hex(RECORD_IO), 1)).singleElement().isEqualTo(operation);
    }

    @Test(groups = "unit")
    public void matchesDotNetReadAndSparseOptions() {
        String expected = "817054e17f1302000000020000001d687962726964726f772d6f7074696f6e732d676f6c64656e"
            + "2d6974656d140a09726561642d65746167";
        BatchOperationCodec.Options options = new BatchOperationCodec.Options(null, null, "read-etag", false);

        byte[] operation = BatchOperationCodec.encode(new BatchOperationCodec.Operation(CosmosItemOperationType.READ)
            .id("hybridrow-options-golden-item").options(options));

        assertThat(operation).isEqualTo(hex(expected));
    }

    @Test(groups = "unit")
    public void decodesDotNetBatchResult() {
        String response = "81f0d8ff7f010a00000081f1d8ff7f3f000000d5437d92817154e17f07c800000000000000"
            + "262230303030613034312d303030302d303830302d303030302d366135373631623130303030221006"
            + "5555555555552540";

        List<byte[]> rows = RecordIoCodec.decode(hex(response), 1);
        BatchResultCodec.Result result = BatchResultCodec.decode(rows.get(0));

        assertThat(result.getStatusCode()).isEqualTo(200);
        assertThat(result.getSubStatusCode()).isZero();
        assertThat(result.getETag()).isEqualTo("\"0000a041-0000-0800-0000-6a5761b10000\"");
        assertThat(result.getRequestCharge()).isEqualTo(10.666666666666666d);
    }

    @Test(groups = "unit")
    public void rejectsMoreRecordsThanRequestedOperations() {
        byte[] payload = RecordIoCodec.encode(java.util.Arrays.asList(new byte[0], new byte[0]));

        assertThatThrownBy(() -> RecordIoCodec.decode(payload, 1))
            .isInstanceOf(CorruptedFrameException.class)
            .hasMessageContaining("record count");
    }

    @Test(groups = "unit")
    public void rejectsDuplicateSparseResultFields() {
        byte[] row = hex("817154e17f03c8000000000000000b05010000000b0502000000");

        assertThatThrownBy(() -> BatchResultCodec.decode(row))
            .isInstanceOf(CorruptedFrameException.class)
            .hasMessageContaining("Duplicate");
    }

    @Test(groups = "unit")
    public void rejectsMalformedUtf8InVariableStrings() {
        HybridRowWireReader reader = new HybridRowWireReader(
            Unpooled.wrappedBuffer(new byte[] { 0x01, (byte) 0x80 }));

        assertThatThrownBy(reader::readVariableString)
            .isInstanceOf(CorruptedFrameException.class)
            .hasMessageContaining("UTF-8");
    }

    @Test(groups = "unit")
    public void rejectsTruncatedAndCorruptRecordIo() {
        byte[] valid = hex(RECORD_IO);
        assertThatThrownBy(() -> RecordIoCodec.decode(java.util.Arrays.copyOf(valid, valid.length - 1), 1))
            .isInstanceOf(CorruptedFrameException.class);
        valid[19] ^= 1;
        assertThatThrownBy(() -> RecordIoCodec.decode(valid, 1)).isInstanceOf(CorruptedFrameException.class)
            .hasMessageContaining("CRC");
    }

    private static byte[] hex(String value) {
        byte[] bytes = new byte[value.length() / 2];
        for (int index = 0; index < bytes.length; index++) {
            bytes[index] = (byte) Integer.parseInt(value.substring(index * 2, index * 2 + 2), 16);
        }
        return bytes;
    }
}
