// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.json.CosmosBinaryJacksonCodec;
import com.fasterxml.jackson.databind.JsonNode;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public final class BinaryEncodingHelperTest {
    @AfterMethod(alwaysRun = true)
    public void clearConfiguration() {
        System.clearProperty(Configs.BINARY_ENCODING_ENABLED);
    }

    @Test(groups = "unit")
    public void enablesSupportedDirectPointOperations() {
        System.setProperty(Configs.BINARY_ENCODING_ENABLED, "true");

        assertThat(canUse(ConnectionMode.DIRECT, OperationType.Create, new RequestOptions())).isTrue();
        assertThat(canUse(ConnectionMode.DIRECT, OperationType.Upsert, new RequestOptions())).isTrue();
        assertThat(canUse(ConnectionMode.DIRECT, OperationType.Replace, new RequestOptions())).isTrue();
        assertThat(canUse(ConnectionMode.DIRECT, OperationType.Read, new RequestOptions())).isTrue();
        assertThat(canUse(ConnectionMode.DIRECT, OperationType.Delete, new RequestOptions())).isTrue();
    }

    @Test(groups = "unit")
    public void serializesPojoByteArrayAsNativeBinaryWhenEnabled() throws Exception {
        PayloadItem item = new PayloadItem();
        item.id = "one";
        item.payload = new byte[] { 0, 1, 2, 3, (byte) 0xFF };

        ByteBuffer buffer = Utils.serializeJsonToByteBuffer(
            CosmosItemSerializer.DEFAULT_SERIALIZER, item, null, true, true);
        byte[] encoded = new byte[buffer.remaining()];
        buffer.get(encoded);
        JsonNode decoded = CosmosBinaryJacksonCodec.decode(encoded);

        assertThat(encoded[0] & 0xFF).isEqualTo(0x80);
        assertThat(decoded.path("payload").isBinary()).isTrue();
        assertThat(decoded.path("payload").binaryValue()).isEqualTo(item.payload);
    }

    @Test(groups = "unit")
    public void enablesOnlyDirectDocumentQueries() {
        System.setProperty(Configs.BINARY_ENCODING_ENABLED, "true");
        RxDocumentServiceRequest query = RxDocumentServiceRequest.create(
            null, OperationType.Query, ResourceType.Document);
        RxDocumentServiceRequest sqlQuery = RxDocumentServiceRequest.create(
            null, OperationType.SqlQuery, ResourceType.Document);
        RxDocumentServiceRequest readFeed = RxDocumentServiceRequest.create(
            null, OperationType.ReadFeed, ResourceType.Document);
        RxDocumentServiceRequest changeFeed = RxDocumentServiceRequest.create(
            null, OperationType.ReadFeed, ResourceType.Document);
        changeFeed.getHeaders().put(
            HttpConstants.HttpHeaders.A_IM, HttpConstants.A_IMHeaderValues.INCREMENTAL_FEED);
        RxDocumentServiceRequest avadChangeFeed = RxDocumentServiceRequest.create(
            null, OperationType.ReadFeed, ResourceType.Document);
        avadChangeFeed.getHeaders().put(
            HttpConstants.HttpHeaders.A_IM, HttpConstants.A_IMHeaderValues.FULL_FIDELITY_FEED);
        RxDocumentServiceRequest thinQuery = RxDocumentServiceRequest.create(
            null, OperationType.Query, ResourceType.Document);
        thinQuery.useThinClientMode = true;

        assertThat(BinaryEncodingHelper.canUseBinaryQueryResponse(query)).isTrue();
        assertThat(BinaryEncodingHelper.canUseBinaryQueryResponse(sqlQuery)).isTrue();
        assertThat(BinaryEncodingHelper.canUseBinaryQueryResponse(readFeed)).isFalse();
        assertThat(BinaryEncodingHelper.canUseBinaryQueryResponse(thinQuery)).isFalse();
        assertThat(BinaryEncodingHelper.canUseBinaryChangeFeedResponse(changeFeed)).isTrue();
        assertThat(BinaryEncodingHelper.canUseBinaryChangeFeedResponse(avadChangeFeed)).isTrue();
        assertThat(BinaryEncodingHelper.canUseBinaryChangeFeedResponse(readFeed)).isFalse();
    }

    @Test(groups = "unit")
    public void remainsOffByDefault() {
        assertThat(canUse(ConnectionMode.DIRECT, OperationType.Read, new RequestOptions())).isFalse();
    }

    @Test(groups = "unit")
    public void excludesGatewayQueriesBatchesPatchesAndTriggers() {
        System.setProperty(Configs.BINARY_ENCODING_ENABLED, "true");
        RequestOptions triggered = new RequestOptions();
        triggered.setPreTriggerInclude(Collections.singletonList("pre"));

        assertThat(canUse(ConnectionMode.GATEWAY, OperationType.Read, new RequestOptions())).isFalse();
        assertThat(canUse(ConnectionMode.DIRECT, OperationType.Query, new RequestOptions())).isFalse();
        assertThat(canUse(ConnectionMode.DIRECT, OperationType.Batch, new RequestOptions())).isFalse();
        assertThat(canUse(ConnectionMode.DIRECT, OperationType.Patch, new RequestOptions())).isFalse();
        assertThat(canUse(ConnectionMode.DIRECT, OperationType.Create, triggered)).isFalse();
    }

    static final class PayloadItem {
        public String id;
        public byte[] payload;
    }

    private static boolean canUse(
        ConnectionMode connectionMode,
        OperationType operationType,
        RequestOptions options) {

        return BinaryEncodingHelper.canUseBinaryEncoding(
            connectionMode, ResourceType.Document, operationType, options);
    }
}
