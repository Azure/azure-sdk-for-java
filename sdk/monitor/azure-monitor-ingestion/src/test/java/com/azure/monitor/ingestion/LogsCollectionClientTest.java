package com.azure.monitor.ingestion;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class LogsCollectionClientTest {
    @Test
    public void testUploadLogsAsync() {
        String dataCollectionEndpoint = Configuration.getGlobalConfiguration().get("DATA_COLLECTION_ENDPOINT");
        String dataCollectionRuleId = Configuration.getGlobalConfiguration().get("DATA_COLLECTION_RULE_ID");
        String streamName = Configuration.getGlobalConfiguration().get("DATA_COLLECTION_STREAM_NAME");
        LogsIngestionAsyncClient logsIngestionAsyncClient = new LogsIngestionClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(dataCollectionEndpoint)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS).addAllowedHeaderName("content-length").addAllowedHeaderName("content-encoding"))
                .buildAsyncClient();

        List<Object> dataList = new ArrayList<>();
        dataList.add(new LogData().setTime(OffsetDateTime.now()).setExtendedColumn("desktop").setAdditionalContext("new" +
                    "-desktop-log"));
        dataList.add(new LogData().setTime(OffsetDateTime.now()).setExtendedColumn("laptop").setAdditionalContext("more" +
                    "-laptop-log"));

        logsIngestionAsyncClient.upload(dataCollectionRuleId, streamName, dataList).block();
    }
}
