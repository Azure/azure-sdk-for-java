// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.azure.identity.implementation.msal_extensions;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.util.Random;

/**
 * Cache lock for the persistent shared MSAL token cache
 *
 * Needed to maintain the integrity of the cache if multiple processes are trying to access it at the same time.
 * */
public class CacheLock {

    private int LockfileRetryWait = 100;
    private int LockfileRetryCount = 60000 / LockfileRetryWait;

    private File LOCK_FILE;

    private FileOutputStream fos;
    private FileChannel channel;
    private FileLock lock = null;

    private File debugFile;
    private String debugFilename = java.nio.file.Paths.get(System.getProperty("user.home"), "Desktop", "debug").toString();
    private boolean DEBUG_FLAG = false;

    /**
     * Default constructor to be used to initialize CacheLock
     *
     * @param lockfileName path of the lock file to be used
     * */
    public CacheLock(String lockfileName) {
        LOCK_FILE = new File(lockfileName);
    }

    /**
     * Constructor to be used for debugging purposes
     * Enables printing the actions for each process while using the cache lock
     *
     * @param lockfileName path of the lock file to be used
     * @param id name of the current process so
     * */
    public CacheLock(String lockfileName, String id) {
        LOCK_FILE = new File(lockfileName);
        debugFile = new File(debugFilename + id + ".txt");
        DEBUG_FLAG = true;
    }

    /**
     * Tries to obtain the lock by creating a file lock on the provided LOCK_FILE
     * If it cannot be obtained right away, it retries LockfileRetryCount = 60000 / LockfileRetryWait times
     *
     * @throws CacheLockNotObtainedException if the lock cannot be obtained after all these tries.
     * */
    public void lock() throws CacheLockNotObtainedException {
        try {
            for (int tryCount = 0; tryCount < LockfileRetryCount; tryCount++) {

                if (DEBUG_FLAG) {
                    try {
                        fos = new FileOutputStream(debugFile, true);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                if (!LOCK_FILE.exists()) {    // file doesn't already exist so now you have to make a new one
                    if (LOCK_FILE.createNewFile()) {
                        LOCK_FILE.deleteOnExit();

                        try {
                            channel = new RandomAccessFile(LOCK_FILE, "rw").getChannel();
                            lock = channel.tryLock();

                            printToFileIfDebug("Locked!\n");
                            return; //success

                        } catch (OverlappingFileLockException e) {
                            printToFileIfDebug("overlap error\n");
                        } catch (Exception e) {
                            printToFileIfDebug("something else went wrong.. general exception\n");
                        }

                    } else {
                        printToFileIfDebug("lockfile already exists\n");
                    }
                } else {
                    printToFileIfDebug("create new file failed");
                }

                printToFileIfDebug("retry\n");

                try {
                    Random rand = new Random(System.currentTimeMillis());
                    int offset = rand.nextInt(10);
                    // slight offset in case multiple threads/processes have the same wait time
                    Thread.sleep(LockfileRetryWait + offset);
                } catch (InterruptedException ex) {
                    printToFileIfDebug("thread sleep issue");
                }
            }

        } catch (Exception e) {
            printToFileIfDebug("general exception, not sure what happened here...no retries\n");
        }

        throw new CacheLockNotObtainedException("Maximum retries used; could not obtain CacheLock");
    }

    /**
     * Tries to unlock the file lock
     *
     * @return true if the file was unlocked, false otherwise
     * */
    public boolean unlock() {
        try {
            lock.release();
            channel.close();
            Files.delete(java.nio.file.Paths.get(LOCK_FILE.getPath()));

            printToFileIfDebug("unlocked\n");

            return true;
        } catch (IOException e) {
            printToFileIfDebug("not unlocked... IOException\n");
            return false;
        }
    }

    /**
     * If DEBUG_FLAG is true, then this will print logs to the file, otherwise it will do nothing
     */
    private void printToFileIfDebug(String message) {
        if (DEBUG_FLAG && fos != null) {
            try {
                fos.write(message.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
