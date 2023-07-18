// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.ChangeFeedMetaData;
import com.azure.cosmos.models.ChangeFeedOperationType;
import com.azure.cosmos.models.ChangeFeedProcessorItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.time.Instant;

public class ChangeFeedProcessorItemSerializerTest {

    private final ObjectMapper simpleObjectMapper = Utils.getSimpleObjectMapper();

    @Test(groups = { "unit" })
    public void testChangeFeedMetaDataDeSerializer() throws JsonProcessingException {
        String json = "{\"lsn\":68,\"crts\":1689555410,\"operationType\":\"replace\",\"previousImageLSN\":66}";
        ChangeFeedMetaData changeFeedMetaData = simpleObjectMapper.readValue(json, ChangeFeedMetaData.class);
        JsonNode jsonNode = simpleObjectMapper.readValue(json, JsonNode.class);
        Assertions.assertThat(changeFeedMetaData.getConflictResolutionTimestamp()).isEqualTo(Instant.ofEpochSecond(1689555410));
        Assertions.assertThat(jsonNode.get("crts").asText()).isEqualTo("1689555410");
        jsonNode = simpleObjectMapper.convertValue(changeFeedMetaData, JsonNode.class);
        Assertions.assertThat(jsonNode.get("crts").asText()).isEqualTo("1689555410");

        json = "{\"lsn\":68,\"crts\":1689555412,\"operationType\":\"replace\",\"previousImageLSN\":66}";
        changeFeedMetaData = simpleObjectMapper.readValue(json, ChangeFeedMetaData.class);
        jsonNode = simpleObjectMapper.readValue(json, JsonNode.class);
        Assertions.assertThat(changeFeedMetaData.getConflictResolutionTimestamp()).isEqualTo(Instant.ofEpochSecond(1689555412));
        Assertions.assertThat(jsonNode.get("crts").asText()).isEqualTo("1689555412");
        jsonNode = simpleObjectMapper.convertValue(changeFeedMetaData, JsonNode.class);
        Assertions.assertThat(jsonNode.get("crts").asText()).isEqualTo("1689555412");
    }

    @Test(groups = { "unit" })
    public void testCreateChangeFeedProcessorItemDeSerializer() throws JsonProcessingException {
        String json = "{\"current\":{\"id\":\"1946c04a-070b-48c0-8e36-517c8d2f92ed\",\"mypk\":\"mypk-1\",\"prop\":\"Johnson\",\"_rid\":\"NopBALG34lcBAAAAAAAAAA==\",\"_self\":\"dbs/NopBAA==/colls/NopBALG34lc=/docs/NopBALG34lcBAAAAAAAAAA==/\",\"_etag\":\"\\\"00000000-0000-0000-b857-fd9e73fb01d9\\\"\",\"_attachments\":\"attachments/\",\"_ts\":1689561604},\"metadata\":{\"lsn\":176,\"crts\":1689561600,\"operationType\":\"create\"}}";
        ChangeFeedProcessorItem changeFeedProcessorItem = simpleObjectMapper.readValue(json, ChangeFeedProcessorItem.class);
        JsonNode jsonNode = changeFeedProcessorItem.toJsonNode();
        Assertions.assertThat(jsonNode.get("metadata").get("crts").asText()).isEqualTo("1689561600");
        Assertions.assertThat(jsonNode.get("metadata").get("lsn").asText()).isEqualTo("176");
        Assertions.assertThat(jsonNode.get("metadata").get("operationType").asText()).isEqualTo("create");
        Assertions.assertThat(jsonNode.get("metadata").get("previousImageLSN")).isNull();
        Assertions.assertThat(jsonNode.get("metadata").get("timeToLiveExpired")).isNull();

        ChangeFeedMetaData changeFeedMetaDataCaseOne = changeFeedProcessorItem.getChangeFeedMetaData();

        Assertions.assertThat(changeFeedMetaDataCaseOne).isNotNull();
        Assertions.assertThat(changeFeedMetaDataCaseOne.getConflictResolutionTimestamp()).isEqualTo(Instant.ofEpochSecond(1689561600));
        Assertions.assertThat(changeFeedMetaDataCaseOne.getOperationType()).isEqualTo(ChangeFeedOperationType.CREATE);
        Assertions.assertThat(changeFeedMetaDataCaseOne.getLogSequenceNumber()).isEqualTo(176);
        Assertions.assertThat(changeFeedMetaDataCaseOne.getPreviousLogSequenceNumber()).isEqualTo(0);
        Assertions.assertThat(changeFeedMetaDataCaseOne.isTimeToLiveExpired()).isEqualTo(false);

        json = "{\"current\":{\"id\":\"1946c04a-070b-48c0-8e36-517c8d2f92ed\",\"mypk\":\"mypk-1\",\"prop\":\"Johnson\",\"_rid\":\"NopBALG34lcBAAAAAAAAAA==\",\"_self\":\"dbs/NopBAA==/colls/NopBALG34lc=/docs/NopBALG34lcBAAAAAAAAAA==/\",\"_etag\":\"\\\"00000000-0000-0000-b857-fd9e73fb01d9\\\"\",\"_attachments\":\"attachments/\",\"_ts\":1689561604},\"metadata\":{\"lsn\":176,\"crts\":1689561605,\"operationType\":\"create\"}}";
        changeFeedProcessorItem = simpleObjectMapper.readValue(json, ChangeFeedProcessorItem.class);
        jsonNode = changeFeedProcessorItem.toJsonNode();
        Assertions.assertThat(jsonNode.get("metadata").get("crts").asText()).isEqualTo("1689561605");
        Assertions.assertThat(jsonNode.get("metadata").get("lsn").asText()).isEqualTo("176");
        Assertions.assertThat(jsonNode.get("metadata").get("operationType").asText()).isEqualTo("create");
        Assertions.assertThat(jsonNode.get("metadata").get("previousImageLSN")).isNull();
        Assertions.assertThat(jsonNode.get("metadata").get("timeToLiveExpired")).isNull();

        ChangeFeedMetaData changeFeedMetaDataCaseTwo = changeFeedProcessorItem.getChangeFeedMetaData();

        Assertions.assertThat(changeFeedMetaDataCaseTwo).isNotNull();
        Assertions.assertThat(changeFeedMetaDataCaseTwo.getConflictResolutionTimestamp()).isEqualTo(Instant.ofEpochSecond(1689561605));
        Assertions.assertThat(changeFeedMetaDataCaseTwo.getOperationType()).isEqualTo(ChangeFeedOperationType.CREATE);
        Assertions.assertThat(changeFeedMetaDataCaseTwo.getLogSequenceNumber()).isEqualTo(176);
        Assertions.assertThat(changeFeedMetaDataCaseTwo.getPreviousLogSequenceNumber()).isEqualTo(0);
        Assertions.assertThat(changeFeedMetaDataCaseTwo.isTimeToLiveExpired()).isEqualTo(false);
    }

    @Test(groups = { "unit" })
    public void testReplaceChangeFeedProcessorItemDeSerializer() throws JsonProcessingException {
        String json = "{\"current\":{\"id\":\"1946c04a-070b-48c0-8e36-517c8d2f92ed\",\"mypk\":\"mypk-1\",\"prop\":\"Gates\",\"_rid\":\"NopBALG34lcBAAAAAAAAAA==\",\"_self\":\"dbs/NopBAA==/colls/NopBALG34lc=/docs/NopBALG34lcBAAAAAAAAAA==/\",\"_etag\":\"\\\"00000000-0000-0000-b857-fda256f201d9\\\"\",\"_attachments\":\"attachments/\",\"_ts\":1689561604},\"metadata\":{\"lsn\":178,\"crts\":1689561600,\"operationType\":\"replace\",\"previousImageLSN\":176,\"timeToLiveExpired\": true}}";
        ChangeFeedProcessorItem changeFeedProcessorItem = simpleObjectMapper.readValue(json, ChangeFeedProcessorItem.class);
        JsonNode jsonNode = changeFeedProcessorItem.toJsonNode();
        Assertions.assertThat(jsonNode.get("metadata").get("crts").asText()).isEqualTo("1689561600");
        Assertions.assertThat(jsonNode.get("metadata").get("lsn").asText()).isEqualTo("178");
        Assertions.assertThat(jsonNode.get("metadata").get("operationType").asText()).isEqualTo("replace");
        Assertions.assertThat(jsonNode.get("metadata").get("previousImageLSN").asText()).isEqualTo("176");
        Assertions.assertThat(jsonNode.get("metadata").get("timeToLiveExpired").asText()).isEqualTo("true");

        ChangeFeedMetaData changeFeedMetaDataCaseOne = changeFeedProcessorItem.getChangeFeedMetaData();

        Assertions.assertThat(changeFeedMetaDataCaseOne).isNotNull();
        Assertions.assertThat(changeFeedMetaDataCaseOne.getConflictResolutionTimestamp()).isEqualTo(Instant.ofEpochSecond(1689561600));
        Assertions.assertThat(changeFeedMetaDataCaseOne.getOperationType()).isEqualTo(ChangeFeedOperationType.REPLACE);
        Assertions.assertThat(changeFeedMetaDataCaseOne.getLogSequenceNumber()).isEqualTo(178);
        Assertions.assertThat(changeFeedMetaDataCaseOne.getPreviousLogSequenceNumber()).isEqualTo(176);
        Assertions.assertThat(changeFeedMetaDataCaseOne.isTimeToLiveExpired()).isEqualTo(true);

        json = "{\"current\":{\"id\":\"1946c04a-070b-48c0-8e36-517c8d2f92ed\",\"mypk\":\"mypk-1\",\"prop\":\"Gates\",\"_rid\":\"NopBALG34lcBAAAAAAAAAA==\",\"_self\":\"dbs/NopBAA==/colls/NopBALG34lc=/docs/NopBALG34lcBAAAAAAAAAA==/\",\"_etag\":\"\\\"00000000-0000-0000-b857-fda256f201d9\\\"\",\"_attachments\":\"attachments/\",\"_ts\":1689561604},\"metadata\":{\"lsn\":178,\"crts\":1689561608,\"operationType\":\"replace\",\"previousImageLSN\":176,\"timeToLiveExpired\": false}}";
        changeFeedProcessorItem = simpleObjectMapper.readValue(json, ChangeFeedProcessorItem.class);
        jsonNode = changeFeedProcessorItem.toJsonNode();
        Assertions.assertThat(jsonNode.get("metadata").get("crts").asText()).isEqualTo("1689561608");
        Assertions.assertThat(jsonNode.get("metadata").get("lsn").asText()).isEqualTo("178");
        Assertions.assertThat(jsonNode.get("metadata").get("operationType").asText()).isEqualTo("replace");
        Assertions.assertThat(jsonNode.get("metadata").get("previousImageLSN").asText()).isEqualTo("176");
        Assertions.assertThat(jsonNode.get("metadata").get("timeToLiveExpired").asText()).isEqualTo("false");

        ChangeFeedMetaData changeFeedMetaDataCaseTwo = changeFeedProcessorItem.getChangeFeedMetaData();

        Assertions.assertThat(changeFeedMetaDataCaseTwo).isNotNull();
        Assertions.assertThat(changeFeedMetaDataCaseTwo.getConflictResolutionTimestamp()).isEqualTo(Instant.ofEpochSecond(1689561608));
        Assertions.assertThat(changeFeedMetaDataCaseTwo.getOperationType()).isEqualTo(ChangeFeedOperationType.REPLACE);
        Assertions.assertThat(changeFeedMetaDataCaseTwo.getLogSequenceNumber()).isEqualTo(178);
        Assertions.assertThat(changeFeedMetaDataCaseTwo.getPreviousLogSequenceNumber()).isEqualTo(176);
        Assertions.assertThat(changeFeedMetaDataCaseTwo.isTimeToLiveExpired()).isEqualTo(false);
    }

    @Test(groups = { "unit" })
    public void testDeleteChangeFeedProcessorItemDeSerializer() throws JsonProcessingException {
        String json = "{\"current\":{\"id\":\"1946c04a-070b-48c0-8e36-517c8d2f92ed\",\"mypk\":\"mypk-1\",\"prop\":\"Gates\",\"_rid\":\"NopBALG34lcBAAAAAAAAAA==\",\"_self\":\"dbs/NopBAA==/colls/NopBALG34lc=/docs/NopBALG34lcBAAAAAAAAAA==/\",\"_etag\":\"\\\"00000000-0000-0000-b857-fda256f201d9\\\"\",\"_attachments\":\"attachments/\",\"_ts\":1689561604},\"metadata\":{\"lsn\":178,\"crts\":1689561600,\"operationType\":\"delete\",\"previousImageLSN\":176}}";
        ChangeFeedProcessorItem changeFeedProcessorItem = simpleObjectMapper.readValue(json, ChangeFeedProcessorItem.class);
        JsonNode jsonNode = changeFeedProcessorItem.toJsonNode();
        Assertions.assertThat(jsonNode.get("metadata").get("crts").asText()).isEqualTo("1689561600");
        Assertions.assertThat(jsonNode.get("metadata").get("lsn").asText()).isEqualTo("178");
        Assertions.assertThat(jsonNode.get("metadata").get("operationType").asText()).isEqualTo("delete");
        Assertions.assertThat(jsonNode.get("metadata").get("previousImageLSN").asText()).isEqualTo("176");
        Assertions.assertThat(jsonNode.get("metadata").get("timeToLiveExpired")).isNull();

        ChangeFeedMetaData changeFeedMetaDataCaseOne = changeFeedProcessorItem.getChangeFeedMetaData();

        Assertions.assertThat(changeFeedMetaDataCaseOne).isNotNull();
        Assertions.assertThat(changeFeedMetaDataCaseOne.getConflictResolutionTimestamp()).isEqualTo(Instant.ofEpochSecond(1689561600));
        Assertions.assertThat(changeFeedMetaDataCaseOne.getOperationType()).isEqualTo(ChangeFeedOperationType.DELETE);
        Assertions.assertThat(changeFeedMetaDataCaseOne.getLogSequenceNumber()).isEqualTo(178);
        Assertions.assertThat(changeFeedMetaDataCaseOne.getPreviousLogSequenceNumber()).isEqualTo(176);

        json = "{\"current\":{\"id\":\"1946c04a-070b-48c0-8e36-517c8d2f92ed\",\"mypk\":\"mypk-1\",\"prop\":\"Gates\",\"_rid\":\"NopBALG34lcBAAAAAAAAAA==\",\"_self\":\"dbs/NopBAA==/colls/NopBALG34lc=/docs/NopBALG34lcBAAAAAAAAAA==/\",\"_etag\":\"\\\"00000000-0000-0000-b857-fda256f201d9\\\"\",\"_attachments\":\"attachments/\",\"_ts\":1689561604},\"metadata\":{\"lsn\":178,\"crts\":1689561608,\"operationType\":\"delete\",\"previousImageLSN\":176}}";
        changeFeedProcessorItem = simpleObjectMapper.readValue(json, ChangeFeedProcessorItem.class);
        jsonNode = changeFeedProcessorItem.toJsonNode();
        Assertions.assertThat(jsonNode.get("metadata").get("crts").asText()).isEqualTo("1689561608");
        Assertions.assertThat(jsonNode.get("metadata").get("lsn").asText()).isEqualTo("178");
        Assertions.assertThat(jsonNode.get("metadata").get("operationType").asText()).isEqualTo("delete");
        Assertions.assertThat(jsonNode.get("metadata").get("previousImageLSN").asText()).isEqualTo("176");
        Assertions.assertThat(jsonNode.get("metadata").get("timeToLiveExpired")).isNull();

        ChangeFeedMetaData changeFeedMetaDataCaseTwo = changeFeedProcessorItem.getChangeFeedMetaData();

        Assertions.assertThat(changeFeedMetaDataCaseTwo).isNotNull();
        Assertions.assertThat(changeFeedMetaDataCaseTwo.getConflictResolutionTimestamp()).isEqualTo(Instant.ofEpochSecond(1689561608));
        Assertions.assertThat(changeFeedMetaDataCaseTwo.getOperationType()).isEqualTo(ChangeFeedOperationType.DELETE);
        Assertions.assertThat(changeFeedMetaDataCaseTwo.getLogSequenceNumber()).isEqualTo(178);
        Assertions.assertThat(changeFeedMetaDataCaseTwo.getPreviousLogSequenceNumber()).isEqualTo(176);
    }
}
