package com.azure.monitor.ingestion.models;

import java.util.List;

/**
 *
 */
public final class SendLogsResult {

    SendLogsResult() {

    }

    /**
     * @return
     */
    public SendLogsStatus getStatus() {
        return SendLogsStatus.SUCCESS;
    }

    /**
     * @return
     */
    public List<SendLogsError> getErrors() {
        return null;
    }
}
