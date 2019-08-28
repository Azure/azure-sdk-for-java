// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.azure.identity.implementation.msal_extensions;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.util.Random;

public class CrossPlatLock {

    private int LockfileRetryWait = 100;
    private int LockfileRetryCount = 60000 / LockfileRetryWait;

    private FileOutputStream fos;
    private FileChannel channel;
    private FileLock lock = null;
    private File lockFile;
    private File debugFile;

    private String debugFilename = java.nio.file.Paths.get(System.getProperty("user.home"), "Desktop", "debug").toString();

    private boolean DEBUG_FLAG = false;

    public CrossPlatLock(String lockfileName) {
        lockFile = new File(lockfileName);
    }

    // Use for debugging
    public CrossPlatLock(String lockfileName, String id) {
        lockFile = new File(lockfileName);
        debugFile = new File(debugFilename + id + ".txt");
        DEBUG_FLAG = true;
    }

    public void lock() throws CrossPlatLockNotObtainedException {
        try {
            for (int tryCount = 0; tryCount < LockfileRetryCount; tryCount++) {

                if (DEBUG_FLAG) {
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

        throw new CrossPlatLockNotObtainedException("Maximum retries used; could not obtain CrossPlatLock");
    }

    public boolean unlock() {
        try {
            lock.release();
            channel.close();
            Files.delete(java.nio.file.Paths.get(lockFile.getPath()));

            printToFileIfDebug("unlocked\n");

            return true;
        } catch (IOException e) {
            printToFileIfDebug("not unlocked... IOException\n");
            return false;
        }
    }

    /*
     * If DEBUG_FLAG is true, then this will print to the file, otherwise it will do nothing
     */
    public void printToFileIfDebug(String message) {
        if (DEBUG_FLAG && fos != null) {
            try {
                fos.write(message.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
