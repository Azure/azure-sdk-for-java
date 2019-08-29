// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.azure.identity.implementation.msal_extensions.cachePersister;

import com.azure.identity.implementation.msal_extensions.CacheLock;
import com.azure.identity.implementation.msal_extensions.CacheLockNotObtainedException;

import java.io.IOException;

public abstract class CacheProtectorBase {

    private String LOCKFILE_LOCATION;
    private CacheLock lock;

    public CacheProtectorBase(String lockfileLocation) {
        this.LOCKFILE_LOCATION = lockfileLocation;
        lock = new CacheLock(LOCKFILE_LOCATION);
    }

    public byte[] readCache() {
        String contents = " ";

        try {
            lock.lock();
        } catch (CacheLockNotObtainedException ex) {
            System.out.println("issue in locking");
            return contents.getBytes();
        }

        try {
            contents = unprotect();
        } catch (IOException ex) {
            System.out.println("Issue in reading");
            ex.printStackTrace();
        }

        lock.unlock();
        return contents.getBytes();
    }

    public void writeCache(byte[] data) {

        try {
            lock.lock();
        } catch (CacheLockNotObtainedException e) {
            System.out.println("issue in locking");
            return;
        }

        try {
            protect(new String(data));
        } catch (IOException e) {
            System.out.println("issue in writing");
        }

        lock.unlock();
    }

    protected String unprotect() throws IOException {
        return null;
    }

    protected void protect(String data) throws IOException {
    }

    public boolean deleteCache() {
        try {
            lock.lock();
        } catch (CacheLockNotObtainedException e) {
            System.out.println("issue in locking");
            return false;
        }

        deleteCacheHelper();
        lock.unlock();

        return true;
    }

    protected void deleteCacheHelper() {
    }
}
