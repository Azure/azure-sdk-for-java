// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.msalextensions.cachepersister;

import com.azure.identity.implementation.msalextensions.CacheLock;
import com.azure.identity.implementation.msalextensions.CacheLockNotObtainedException;

import java.io.IOException;

/**
 * Abstract class for Cache Protectors
 * Provides methods to read and write cache while using a CacheLock
 * */
public abstract class CacheProtectorBase {

    private String lockfileLocation;
    private CacheLock lock;


    /**
     * Constructor
     * initializes cacheLock
     * */
    public CacheProtectorBase(String lockfileLocation) {
        this.lockfileLocation = lockfileLocation;
        lock = new CacheLock(this.lockfileLocation);
    }

    /**
     * Obtains lock and uses unprotect() to read and decrypt contents of the cache
     *
     * @return byte[] contents of cache
     * */
    public byte[] readCache() {
        byte[] contents = null;

        try {
            lock.lock();
        } catch (CacheLockNotObtainedException ex) {
            System.out.println("issue in locking");
            return contents;
        }

        try {
            contents = unprotect();
        } catch (IOException ex) {
            System.out.println("Issue in reading");
            ex.printStackTrace();
        }

        lock.unlock();
        return contents;
    }

    /**
     * Obtains lock and uses protect() to read and encrypt contents of the cache
     *
     * @param data data to write to cache
     * */
    public void writeCache(byte[] data) {

        try {
            lock.lock();
        } catch (CacheLockNotObtainedException e) {
            System.out.println("issue in locking");
            return;
        }

        try {
            protect(data);
        } catch (IOException e) {
            System.out.println("issue in writing");
        }

        lock.unlock();
    }

    /**
     * Decrypts data from cache
     *
     * @return byte[] of cache contents
     *
     * Overwritten by subclasses; each OS handles differently
     * */
    protected byte[] unprotect() throws IOException {
        return null;
    }

    /**
     * Encrypts data and writes to cache
     *
     * @param data
     *
     * Overwritten by subclasses; each OS handles differently
     * */
    protected void protect(byte[] data) throws IOException {
    }

    /**
     * Obtains lock and deletes cache using deleteCacheHelper()
     *
     * @return true if cache is deleted, false otherwise
     * */
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

    /**
     * Overwritten by subclasses; each OS handles differently
     * */
    protected void deleteCacheHelper() {
    }
}
