// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.azure.identity.implementation.msal_extensions;

import com.azure.identity.implementation.msal_extensions.cacheProtector.CacheProtector;
import com.azure.identity.implementation.msal_extensions.cacheProtector.PlatformNotSupportedException;
import com.sun.jna.Platform;

import java.io.File;
import java.io.IOException;

/*
 * Deals with the actual cache file: read/write/create/clear file
 * */
public class MsalCacheStorage {

    private String CACHE_LOCATION;
    private String LOCKFILE_LOCATION;
    private File file;

    private CacheProtector cacheProtector;
    private String SERVICE_NAME;
    private String ACCOUNT_NAME;

    private MsalCacheStorage() {
    }

    private void setupCache() throws IOException {
        file = new File(CACHE_LOCATION);

        if (!cacheExists()) {
            createCache();
            cacheProtector.protect(SERVICE_NAME, ACCOUNT_NAME, " ");
        } else {
            try {
                // if no entry exists then make an empty string as the entry
                cacheProtector.unprotect(SERVICE_NAME, ACCOUNT_NAME);
            } catch (Exception e) {
                cacheProtector.protect(SERVICE_NAME, ACCOUNT_NAME, " ");
            }
        }
    }

    public boolean cacheExists() {
        return file.exists();
    }

    public boolean deleteCache() {
        CrossPlatLock lock = new CrossPlatLock(LOCKFILE_LOCATION);

        try {
            lock.lock();
        } catch (CrossPlatLockNotObtainedException e) {
            System.out.println("issue in locking");
            return false;
        }

        if (Platform.isWindows()) {
            file.delete();
            lock.unlock();
            return true;
        } else {
            lock.unlock();
            return false;   // doesn't delete for any other platform yet
        }
    }

    public void createCache() throws IOException {
        file.createNewFile();
    }

    /*
     * Read file and return contents as byte[]
     * */
    public byte[] readCache() {

        String contents = " ";

        CrossPlatLock lock = new CrossPlatLock(LOCKFILE_LOCATION);

        try {
            lock.lock();
        } catch (CrossPlatLockNotObtainedException ex) {
            System.out.println("issue in locking");
            return contents.getBytes();
        }

        try {
            contents = cacheProtector.unprotect(SERVICE_NAME, ACCOUNT_NAME);
        } catch (IOException ex) {
            System.out.println("Issue in reading");
        } finally {
            lock.unlock();
        }

        return contents.getBytes();
    }

    /*
     * Write byte[] to file
     * */
    public void writeCache(byte[] data) {

        CrossPlatLock lock = new CrossPlatLock(LOCKFILE_LOCATION);
        try {
            lock.lock();
        } catch (CrossPlatLockNotObtainedException e) {
            System.out.println("issue in locking");
            return;
        }

        try {
            cacheProtector.protect(SERVICE_NAME, ACCOUNT_NAME, new String(data));
        } catch (IOException e) {
            System.out.println("issue in writing");
        } finally {
            lock.unlock();
        }
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

        public MsalCacheStorage build() throws PlatformNotSupportedException, IOException {
            MsalCacheStorage storage = new MsalCacheStorage();
            storage.CACHE_LOCATION = cacheLocation;
            storage.LOCKFILE_LOCATION = lockfileLocation;
            storage.SERVICE_NAME = serviceName;
            storage.ACCOUNT_NAME = accountName;

            storage.cacheProtector = new CacheProtector(storage.CACHE_LOCATION);
            storage.setupCache();

            return storage;
        }
    }
}
