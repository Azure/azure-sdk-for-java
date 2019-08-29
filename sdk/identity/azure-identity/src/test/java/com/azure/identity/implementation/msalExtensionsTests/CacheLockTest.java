// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.azure.identity.implementation.msalExtensionsTests;

import com.azure.identity.implementation.msal_extensions.CacheLock;
import org.junit.*;

import java.io.*;
import java.util.Stack;

public class CacheLockTest {

    private static String FOLDER;
    private static String tester_filename;
    private static String lockfile;

    @BeforeClass
    public static void setup() {
        // get proper file paths
        String currDir = System.getProperty("user.dir");
        String home = System.getProperty("user.home");

        java.nio.file.Path classes = java.nio.file.Paths.get(currDir, "target", "classes");
        System.out.println(classes);
        java.nio.file.Path tests = java.nio.file.Paths.get(currDir, "target", "test-classes");

        tester_filename = java.nio.file.Paths.get(home, "Desktop", "tester.txt").toString();
        lockfile = java.nio.file.Paths.get(home, "Desktop", "testlock.lockfile").toString();

        FOLDER = classes.toString() + ";" + tests;  // TODO: ; for windows, but : for mac?
    }

    @Test
    public void tenThreadsWritingToFile() throws IOException {

        // make sure tester.json file doesn't already exist
        File tester = new File(tester_filename);
        tester.delete();

        // delete the lock file just in case before starting
        File lock = new File(lockfile);
        lock.delete();

        FileWriter a = new FileWriter("a", lockfile, tester_filename);
        FileWriter b = new FileWriter("b", lockfile, tester_filename);
        FileWriter c = new FileWriter("c", lockfile, tester_filename);
        FileWriter d = new FileWriter("d", lockfile, tester_filename);
        FileWriter e = new FileWriter("e", lockfile, tester_filename);
        FileWriter f = new FileWriter("f", lockfile, tester_filename);
        FileWriter g = new FileWriter("g", lockfile, tester_filename);
        FileWriter h = new FileWriter("h", lockfile, tester_filename);
        FileWriter i = new FileWriter("i", lockfile, tester_filename);
        FileWriter j = new FileWriter("j", lockfile, tester_filename);

        try {
            a.t.join();
            b.t.join();
            c.t.join();
            d.t.join();
            e.t.join();
            f.t.join();
            g.t.join();
            h.t.join();
            i.t.join();
            j.t.join();
        } catch (Exception ex) {
            System.out.printf("Error with threads");
        }

        Stack stack = new Stack<String>();
        int popped = 0;

        File file = new File(tester_filename);

        if (file.exists()) {
            FileReader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] tokens = line.split(" ");
                if (tokens[0].equals("<")) { // enter
                    stack.push(tokens[1]);
                } else if (tokens[0].equals(">")) {   // exit
                    if (stack.peek().equals(tokens[1])) {
                        stack.pop();
                        popped++;
                    } else
                        System.out.println("messed up: " + tokens[1]);
                }
            }
            reader.close();

            if (!stack.empty())
                Assert.fail();
        } else {
            Assert.fail("File does not exist");
        }

        Assert.assertEquals("10 processes didn't write", popped, 10);

    }

    @Test
    public void tenProcessesWritingToFile() throws IOException, InterruptedException {

        // make sure tester.json file doesn't already exist
        File tester = new File(tester_filename);
        tester.delete();

        // delete the lock file just in case before starting
        File lock = new File(lockfile);
        lock.delete();

        Process process1 = new ProcessBuilder(new String[]{"java", "-cp", FOLDER, "com.azure.identity.implementation.msalExtensionsTests.FileWriter", Integer.toString(1), lockfile, tester_filename}).start();
        Process process2 = new ProcessBuilder(new String[]{"java", "-cp", FOLDER, "com.azure.identity.implementation.msalExtensionsTests.FileWriter", Integer.toString(2), lockfile, tester_filename}).start();
        Process process3 = new ProcessBuilder(new String[]{"java", "-cp", FOLDER, "com.azure.identity.implementation.msalExtensionsTests.FileWriter", Integer.toString(3), lockfile, tester_filename}).start();
        Process process4 = new ProcessBuilder(new String[]{"java", "-cp", FOLDER, "com.azure.identity.implementation.msalExtensionsTests.FileWriter", Integer.toString(4), lockfile, tester_filename}).start();
        Process process5 = new ProcessBuilder(new String[]{"java", "-cp", FOLDER, "com.azure.identity.implementation.msalExtensionsTests.FileWriter", Integer.toString(5), lockfile, tester_filename}).start();
        Process process6 = new ProcessBuilder(new String[]{"java", "-cp", FOLDER, "com.azure.identity.implementation.msalExtensionsTests.FileWriter", Integer.toString(6), lockfile, tester_filename}).start();
        Process process7 = new ProcessBuilder(new String[]{"java", "-cp", FOLDER, "com.azure.identity.implementation.msalExtensionsTests.FileWriter", Integer.toString(7), lockfile, tester_filename}).start();
        Process process8 = new ProcessBuilder(new String[]{"java", "-cp", FOLDER, "com.azure.identity.implementation.msalExtensionsTests.FileWriter", Integer.toString(8), lockfile, tester_filename}).start();
        Process process9 = new ProcessBuilder(new String[]{"java", "-cp", FOLDER, "com.azure.identity.implementation.msalExtensionsTests.FileWriter", Integer.toString(9), lockfile, tester_filename}).start();
        Process process10 = new ProcessBuilder(new String[]{"java", "-cp", FOLDER, "com.azure.identity.implementation.msalExtensionsTests.FileWriter", Integer.toString(10), lockfile, tester_filename}).start();

        process1.waitFor();
        process2.waitFor();
        process3.waitFor();
        process4.waitFor();
        process5.waitFor();
        process6.waitFor();
        process7.waitFor();
        process8.waitFor();
        process9.waitFor();
        process10.waitFor();

        Stack stack = new Stack<String>();
        int popped = 0;

        File file = new File(tester_filename);
        if (file.exists()) {
            FileReader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] tokens = line.split(" ");
                if (tokens[0].equals("<")) { // enter
                    stack.push(tokens[1]);
                } else if (tokens[0].equals(">")) {   // exit
                    if (stack.peek().equals(tokens[1])) {
                        stack.pop();
                        popped++;
                    } else
                        System.out.println("messed up: " + tokens[1]);
                }
            }
            reader.close();

            if (!stack.empty())
                Assert.fail();
        } else {
            Assert.fail("File does not exist");
        }

        Assert.assertEquals("10 processes didn't write", popped, 10);
    }


    /*
     * Class to be used for testing threads
     * */
    class FileWriter implements Runnable {

        String threadName;
        File file;
        String lockfile;
        Thread t;

        FileWriter(String threadName, String lockfile, String filename) {
            this.threadName = threadName;
            this.lockfile = lockfile;
            this.file = new File(filename);

            t = new Thread(this, threadName);
            t.start();
        }

        public void run() {
            CacheLock lock = new CacheLock(lockfile);
            try {
                lock.lock();
                try {
                    if (!file.exists())
                        file.createNewFile();
                    FileOutputStream os = new FileOutputStream(file, true);

                    os.write(("< " + threadName + "\n").getBytes());
                    Thread.sleep(1000);
                    os.write(("> " + threadName + "\n").getBytes());

                    os.close();
                } catch (Exception ex) {

                }
            } catch (Exception ex) {
                System.out.println("Couldn't obtain lock");
            } finally {
                try {
                    lock.unlock();
                } catch (Exception ex) {
                    System.out.println("aljsdladsk");
                }
            }


        }
    }
}
