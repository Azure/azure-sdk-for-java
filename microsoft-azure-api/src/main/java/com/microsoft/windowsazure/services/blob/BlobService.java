package com.microsoft.windowsazure.services.blob;

import com.microsoft.windowsazure.services.core.Configuration;

public class BlobService {
    private BlobService() {
    }

    public static BlobContract create() {
        return create(null, Configuration.getInstance());
    }

    public static BlobContract create(Configuration config) {
        return create(null, config);
    }

    public static BlobContract create(String profile) {
        return create(profile, Configuration.getInstance());
    }

    public static BlobContract create(String profile, Configuration config) {
        return config.create(profile, BlobContract.class);
    }
}