package com.microsoft.windowsazure.services.queue.models;

import java.util.HashMap;

public class CreateQueueOptions extends QueueServiceOptions {
    private HashMap<String, String> metadata = new HashMap<String, String>();

    @Override
    public CreateQueueOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    public CreateQueueOptions setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public CreateQueueOptions addMetadata(String key, String value) {
        this.metadata.put(key, value);
        return this;
    }
}
