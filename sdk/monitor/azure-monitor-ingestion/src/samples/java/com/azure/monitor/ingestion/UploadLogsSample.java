// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.ingestion.models.UploadLogsResult;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public final class UploadLogsSample {

    /**
     * @param args
     */
    public static void main(String[] args) {
        LogsIngestionClient client = new LogsIngestionClientBuilder()
                .endpoint("<data-collection-endpoint")
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        List<Object> dataList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            CustomLogData e = new CustomLogData()
                    .setTime(OffsetDateTime.now())
                    .setExtendedColumn("columndata" + i)
                    .setAdditionalContext("more logs context");
            dataList.add(e);
        }
        UploadLogsResult result = client.upload("<data-collection-rule-id", "stream-name", dataList);
        System.out.println(result.getStatus());
    }
}
