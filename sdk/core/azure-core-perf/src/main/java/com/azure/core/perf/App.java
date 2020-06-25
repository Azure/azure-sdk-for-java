// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf;

import com.azure.perf.test.core.PerfStressProgram;

/**
 * Runs the Storage performance test.
 *
 * <p>To run from command line. Package the project into a jar with dependencies via mvn clean package.
 * Then run the program via java -jar 'compiled-jar-with-dependencies-path' </p>
 *
 * <p> To run from IDE, set all the required environment variables in IntelliJ via Run -&gt; EditConfigurations
 * section.
 * Then run the App's main method via IDE.</p>
 */
public class App {
    public static void main(String[] args) {
        Class<?>[] testClasses;

        try {
            testClasses = new Class<?>[]{
                Class.forName("com.azure.core.perf.ByteBufferReceiveTest"),
                Class.forName("com.azure.core.perf.JsonReceiveTest"),
                Class.forName("com.azure.core.perf.JsonSendTest"),
                Class.forName("com.azure.core.perf.XmlReceiveTest"),
                Class.forName("com.azure.core.perf.XmlSendTest"),
                Class.forName("com.azure.core.perf.ByteBufferSendTest")
            };
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        PerfStressProgram.run(testClasses, args);
    }
}
