// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.msalextensions;

import java.io.File;
import java.io.FileOutputStream;

public class FileWriter {

    public static void main(String[] args) throws Exception {
        File file;
        String lockfile;

        if (args.length == 3) {
            lockfile = args[1];
            file = new File(args[2]);
        } else {
            System.out.println("wrong number of args lol????");
            return;
        }
        CacheLock lock = new CacheLock(lockfile, args[0]);

        try {
            lock.lock();

            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream os = new FileOutputStream(file, true);

            os.write(("< " + args[0] + "\n").getBytes());
            Thread.sleep(1000);
            os.write(("> " + args[0] + "\n").getBytes());

            os.close();

        } finally {
            lock.unlock();
        }


    }
}
