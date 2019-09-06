// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.msalextensions.cachepersister;

import com.azure.identity.implementation.msalextensions.cachepersister.windows.WindowsDPAPICacheProtector;
import com.sun.jna.Platform;

import java.io.IOException;

/**
 * Wrapper class for CacheProtector
 * Determines the type of CacheProtector to use (if possible) and instantiates it
 * Also contains wrapper methods for read, write, and delete cache
 * */
public final class CachePersister {

    private CacheProtectorBase cacheProtector;

    /**
     * Default constructor
     * */
    private CachePersister() {
    }

    /**
     * Creates a new cacheProtector depending on which OS is currently being used
     *
     * @param cacheLocation
     * @param  lockfileLocation
     * @param  serviceName
     * @param  accountName
     *
     * @throws PlatformNotSupportedException if the current OS is not supported
     * */
    void createCacheProtector(String cacheLocation, String lockfileLocation, String serviceName, String accountName) throws RuntimeException {
        if (Platform.isWindows()) {
            try {
                cacheProtector = new WindowsDPAPICacheProtector(cacheLocation, lockfileLocation);
            } catch (IOException e) {
                throw new RuntimeException("IO Exception in creating the WindowsDPAPICacheProtector");
            }
        } else {
            throw new PlatformNotSupportedException("Platform is not supported");
        }
    }

    /**
     * Wrapper method for reading cache
     *
     * @return byte[] of cache contents
     * */
    public byte[] readCache() {
        return cacheProtector.readCache();
    }

    /**
     * Wrapper method for writing to the cache
     *
     * @param data Cache contents
     * */
    public void writeCache(byte[] data) {
        cacheProtector.writeCache(data);
    }

    public boolean deleteCache() {
        return cacheProtector.deleteCache();
    }

    /**
     * Builder for CachePersister class
     * Creates appropriate file paths and account and service names, and calls createCacheProtector
     * */
    public static class Builder {

        private String cacheLocation;
        private String lockfileLocation;
        private String serviceName;
        private String accountName;

        /**
         * Default builder based on platform for cache file, and default service and account names
         */
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

        /**
         * @return Builder with updated cacheLocation and lockfileLocation
         * */
        public Builder cacheLocation(String cacheLocation) {
            this.cacheLocation = cacheLocation;
            this.lockfileLocation = cacheLocation + ".lockfile";
            return this;
        }

        /**
         * @return Builder with updated lockfileLocation
         * */
        public Builder lockfileLocation(String lockfileLocation) {
            this.lockfileLocation = lockfileLocation;
            return this;
        }

        /**
         * @return Builder with updated serviceName
         * */
        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        /**
         * @return Builder with updated accountName
         * */
        public Builder accountName(String accountName) {
            this.accountName = accountName;
            return this;
        }

        /**
         * Builds CachePersister with all the information passed into the Builder
         *
         * @return newly instantiated CachePersister
         * */
        public CachePersister build() throws RuntimeException {
            CachePersister cachePersister = new CachePersister();
            cachePersister.createCacheProtector(cacheLocation, lockfileLocation, serviceName, accountName);

            return cachePersister;
        }
    }
}
