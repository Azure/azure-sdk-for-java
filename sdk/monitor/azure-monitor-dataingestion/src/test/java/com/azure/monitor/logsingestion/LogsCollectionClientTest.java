package com.azure.monitor.logsingestion;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.logsingestion.implementation.DataCollectionRulesImpl;
import com.azure.monitor.logsingestion.implementation.IngestionUsingDataCollectionRulesImplBuilder;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class LogsCollectionClientTest {

    @Test
    public void testSendLogs() {
        DataCollectionRulesImpl dataCollectionRules = new IngestionUsingDataCollectionRulesImplBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://srnagar-logsingestion-dcr-lrz3.westus2-1.ingest.monitor.azure.com")
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .buildClient()
                .getDataCollectionRules();
        List<Object> dataList = new ArrayList<>();

        dataList.add(new LogData().setTime(OffsetDateTime.now()).setExtendedColumn("desktop").setAdditionalContext("new" +
                "-desktop-log"));
        dataList.add(new LogData().setTime(OffsetDateTime.now()).setExtendedColumn("laptop").setAdditionalContext("more" +
                "-laptop-log"));

        Response<Void> voidResponse = dataCollectionRules.ingestWithResponse("dcr-adec84661d05465f8532f32a04af6f98",
                "Custom-MyTableRawData", dataList, "", "", Context.NONE);
        System.out.println(voidResponse.getStatusCode());
    }
}
