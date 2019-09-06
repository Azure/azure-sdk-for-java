// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.msalextensions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
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

    private int lockfileRetryWait = 100;
    private int lockfileRetryCount = 60000 / lockfileRetryWait;

    private File lockFile;

    private FileOutputStream fos;
    private FileChannel channel;
    private FileLock lock = null;

    private File debugFile;
    private String debugFilename = java.nio.file.Paths.get(System.getProperty("user.home"), "Desktop", "debug").toString();
    private final boolean debugFlag;

    /**
     * Default constructor to be used to initialize CacheLock
     *
     * @param lockfileName path of the lock file to be used
     * */
    public CacheLock(String lockfileName) {
        lockFile = new File(lockfileName);
        debugFlag = false;
    }

    /**
     * Constructor to be used for debugging purposes
     * Enables printing the actions for each process while using the cache lock
     *
     * @param lockfileName path of the lock file to be used
     * @param id name of the current process so
     * */
    public CacheLock(String lockfileName, String id) {
        lockFile = new File(lockfileName);
        debugFile = new File(debugFilename + id + ".txt");
        debugFlag = true;
    }

    /**
     * Tries to obtain the lock by creating a file lock on the provided lockFile
     * If it cannot be obtained right away, it retries lockfileRetryCount = 60000 / lockfileRetryWait times
     *
     * @throws CacheLockNotObtainedException if the lock cannot be obtained after all these tries.
     * */
    public void lock() throws CacheLockNotObtainedException {
        try {
            for (int tryCount = 0; tryCount < lockfileRetryCount; tryCount++) {

                if (debugFlag) {
                    try {
                        fos = new FileOutputStream(debugFile, true);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                if (!lockFile.exists()) {    // file doesn't already exist so now you have to make a new one
                    if (lockFile.createNewFile()) {
                        lockFile.deleteOnExit();

                        try {
                            channel = new RandomAccessFile(lockFile, "rw").getChannel();
                            lock = channel.lock();

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
                    Thread.sleep(lockfileRetryWait + offset);
                } catch (InterruptedException ex) {
                    printToFileIfDebug("thread sleep issue");
                }
            }

        } catch (IOException e) {
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
            Files.delete(java.nio.file.Paths.get(lockFile.getPath()));

            printToFileIfDebug("unlocked\n");

            return true;
        } catch (IOException e) {
            printToFileIfDebug("not unlocked... IOException: " + e.getMessage());
            return false;
        }
    }

    /**
     * If debugFlag is true, then this will print logs to the file, otherwise it will do nothing
     */
    private void printToFileIfDebug(String message) {
        if (debugFlag && fos != null) {
            try {
                fos.write(message.getBytes("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
