package com.microsoft.windowsazure.services.queue;

import com.microsoft.windowsazure.services.core.Configuration;

public class QueueService {
    private QueueService() {
    }

    public static QueueContract create() {
        return create(null, Configuration.getInstance());
    }

    public static QueueContract create(Configuration config) {
        return create(null, config);
    }

    public static QueueContract create(String profile) {
        return create(profile, Configuration.getInstance());
    }

    public static QueueContract create(String profile, Configuration config) {
        return config.create(profile, QueueContract.class);
    }
}