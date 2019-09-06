// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.msalextensions;

import com.sun.jna.Platform;
import org.junit.*;

import java.io.*;
import java.util.Stack;

public class CacheLockTest {

    private static String folder;
    private static String testerFilename;
    private static String lockfile;

    @BeforeClass
    public static void setup() {
        // get proper file paths
        String currDir = System.getProperty("user.dir");
        String home = System.getProperty("user.home");

        java.nio.file.Path classes = java.nio.file.Paths.get(currDir, "target", "classes");
        java.nio.file.Path tests = java.nio.file.Paths.get(currDir, "target", "test-classes");

        testerFilename = java.nio.file.Paths.get(home, "Desktop", "tester.txt").toString();
        lockfile = java.nio.file.Paths.get(home, "Desktop", "testlock.lockfile").toString();

        String delimiter = ":";
        if (Platform.isWindows()) {
            delimiter = ";";
        }
        folder = classes.toString() + delimiter + tests;
    }

    @Test
    public void tenThreadsWritingToFile() throws IOException {

        // make sure tester.json file doesn't already exist
        File tester = new File(testerFilename);
        tester.delete();

        // delete the lock file just in case before starting
        File lock = new File(lockfile);
        lock.delete();

        FileWriter a = new FileWriter("a", lockfile, testerFilename);
        FileWriter b = new FileWriter("b", lockfile, testerFilename);
        FileWriter c = new FileWriter("c", lockfile, testerFilename);
        FileWriter d = new FileWriter("d", lockfile, testerFilename);
        FileWriter e = new FileWriter("e", lockfile, testerFilename);
        FileWriter f = new FileWriter("f", lockfile, testerFilename);
        FileWriter g = new FileWriter("g", lockfile, testerFilename);
        FileWriter h = new FileWriter("h", lockfile, testerFilename);
        FileWriter i = new FileWriter("i", lockfile, testerFilename);
        FileWriter j = new FileWriter("j", lockfile, testerFilename);

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

        Stack<String> stack = new Stack<>();
        int popped = 0;

        File file = new File(testerFilename);

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
                    } else {
                        System.out.println("messed up: " + tokens[1]);
                    }
                }
            }
            reader.close();

            if (!stack.empty()) {
                Assert.fail();
            }
        } else {
            Assert.fail("File does not exist");
        }

        Assert.assertEquals("10 processes didn't write", popped, 10);

    }

    @Test
    public void tenProcessesWritingToFile() throws IOException, InterruptedException {
        // make sure tester.json file doesn't already exist
        File tester = new File(testerFilename);
        tester.delete();

        // delete the lock file just in case before starting
        File lock = new File(lockfile);
        lock.delete();

        String mainClass = com.azure.identity.implementation.msalextensions.FileWriter.class.getName();
        Process process1 = new ProcessBuilder(new String[]{"java", "-cp", folder, mainClass, Integer.toString(1), lockfile, testerFilename}).start();
        Process process2 = new ProcessBuilder(new String[]{"java", "-cp", folder, mainClass, Integer.toString(2), lockfile, testerFilename}).start();
        Process process3 = new ProcessBuilder(new String[]{"java", "-cp", folder, mainClass, Integer.toString(3), lockfile, testerFilename}).start();
        Process process4 = new ProcessBuilder(new String[]{"java", "-cp", folder, mainClass, Integer.toString(4), lockfile, testerFilename}).start();
        Process process5 = new ProcessBuilder(new String[]{"java", "-cp", folder, mainClass, Integer.toString(5), lockfile, testerFilename}).start();
        Process process6 = new ProcessBuilder(new String[]{"java", "-cp", folder, mainClass, Integer.toString(6), lockfile, testerFilename}).start();
        Process process7 = new ProcessBuilder(new String[]{"java", "-cp", folder, mainClass, Integer.toString(7), lockfile, testerFilename}).start();
        Process process8 = new ProcessBuilder(new String[]{"java", "-cp", folder, mainClass, Integer.toString(8), lockfile, testerFilename}).start();
        Process process9 = new ProcessBuilder(new String[]{"java", "-cp", folder, mainClass, Integer.toString(9), lockfile, testerFilename}).start();
        Process process10 = new ProcessBuilder(new String[]{"java", "-cp", folder, mainClass, Integer.toString(10), lockfile, testerFilename}).start();

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

        Stack<String> stack = new Stack<>();
        int popped = 0;

        File file = new File(testerFilename);
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
                    } else {
                        System.out.println("messed up: " + tokens[1]);
                    }
                }
            }
            reader.close();

            if (!stack.empty()) {
                Assert.fail();
            }
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
                    if (!file.exists()) {
                        file.createNewFile();
                    }
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
