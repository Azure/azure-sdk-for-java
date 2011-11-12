package com.microsoft.windowsazure.services.queue;

import java.util.HashMap;

public class CreateQueueOptions extends QueueServiceOptions {
    private HashMap<String, String> metadata = new HashMap<String, String>();

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
