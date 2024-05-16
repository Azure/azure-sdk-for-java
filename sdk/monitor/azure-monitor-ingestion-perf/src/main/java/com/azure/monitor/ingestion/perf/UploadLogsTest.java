// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.perf;

import com.azure.core.util.Configuration;
import com.azure.monitor.ingestion.perf.core.ServiceTest;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Performance test to upload logs to Azure Monitor using a list of custom model logs.
 */
public class UploadLogsTest extends ServiceTest<PerfStressOptions> {

    private final String dataCollectionRuleId;
    private final String streamName;
    private final List<Object> logs;

    /**
     * The base class for Azure Monitor Ingestion performance tests.
     * @param options the configurable options for performing perf testing on this class.
     */
    public UploadLogsTest(PerfStressOptions options) {
        super(options);
        dataCollectionRuleId = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_DCR");
        streamName = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_STREAM_NAME");
        if (dataCollectionRuleId == null) {
            throw new IllegalStateException(String.format(CONFIGURATION_ERROR, "AZURE_MONITOR_DCR"));
        }

        if (streamName == null) {
            throw new IllegalStateException(String.format(CONFIGURATION_ERROR, "AZURE_MONITOR_STREAM_NAME"));
        }

        int logsCount = options.getCount();
        logs = new ArrayList<>(getObjects(logsCount));
    }

    @Override
    public void run() {
        logsIngestionClient.upload(dataCollectionRuleId, streamName, logs);
    }

    @Override
    public Mono<Void> runAsync() {
        return logsIngestionAsyncClient.upload(dataCollectionRuleId, streamName, logs).then();
    }

    private List<Object> getObjects(int logsCount) {
        List<Object> logs = new ArrayList<>();

        for (int i = 0; i < logsCount; i++) {
            LogData logData = new LogData()
                    .setTime(OffsetDateTime.parse("2022-01-01T00:00:00+07:00"))
                    .setExtendedColumn("test" + i)
                    .setAdditionalContext("additional logs context");
            logs.add(logData);
        }
        return logs;
    }
}
