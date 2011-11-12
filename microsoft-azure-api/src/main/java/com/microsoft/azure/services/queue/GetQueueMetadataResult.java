package com.microsoft.azure.services.queue;

import java.util.HashMap;

public class GetQueueMetadataResult {
    private long approximateMessageCount;
    private HashMap<String, String> metadata;

    public long getApproximateMessageCount() {
        return approximateMessageCount;
    }

    public void setApproximateMessageCount(long approximateMessageCount) {
        this.approximateMessageCount = approximateMessageCount;
    }

    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
    }
}
