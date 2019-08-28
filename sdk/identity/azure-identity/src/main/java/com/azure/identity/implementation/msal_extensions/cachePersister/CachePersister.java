// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.azure.identity.implementation.msal_extensions.cachePersister;

import com.azure.identity.implementation.msal_extensions.cachePersister.windows.WindowsDPAPICacheProtector;
import com.sun.jna.Platform;

import java.io.IOException;

public class CachePersister {

    private CacheProtectorBase cacheProtector;

    private CachePersister() {
    }

    private void createCacheProtector(String cacheLocation, String lockfileLocation, String serviceName, String accountName) throws IOException, PlatformNotSupportedException {
        if (Platform.isWindows()) {
            cacheProtector = new WindowsDPAPICacheProtector(cacheLocation, lockfileLocation);
        } else {
            throw new PlatformNotSupportedException("Platform is not supported");
        }
    }

    public byte[] readCache() {
        return cacheProtector.readCache();
    }

    public void writeCache(byte[] data) {
        cacheProtector.writeCache(data);
    }

    public boolean deleteCache() {
        return cacheProtector.deleteCache();
    }

    public static class Builder {

        private String cacheLocation;
        private String lockfileLocation;
        private String serviceName;
        private String accountName;

        // default builder based on platform for cache file, and default service and account names
        public Builder() {

            // determine platform and create cache file location
            if (Platform.isWindows()) {
                cacheLocation = java.nio.file.Paths.get(System.getProperty("user.home"), "AppData", "Local", ".IdentityService", "msal.cache").toString();
            } else {
                cacheLocation = java.nio.file.Paths.get(System.getProperty("user.home"), "msal.cache").toString();
            }
            lockfileLocation = cacheLocation + ".lockfile";

            serviceName = "Microsoft.Developer.IdentityService";
            accountName = "MSALCache";
        }

        public Builder cacheLocation(String cacheLocation) {
            this.cacheLocation = cacheLocation;
            this.lockfileLocation = cacheLocation + ".lockfile";
            return this;
        }

        public Builder lockfileLocation(String lockfileLocation) {
            this.lockfileLocation = lockfileLocation;
            return this;
        }

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder accountName(String accountName) {
            this.accountName = accountName;
            return this;
        }

        public CachePersister build() throws PlatformNotSupportedException, IOException {
            CachePersister cachePersister = new CachePersister();
            cachePersister.createCacheProtector(cacheLocation, lockfileLocation, serviceName, accountName);

            return cachePersister;
        }
    }
}
