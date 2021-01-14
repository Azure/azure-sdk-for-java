// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.perf;

import com.azure.perf.test.core.PerfStressProgram;

/**
 * Runs the ServiceBus performance test.
 *
 * <p>To run from command line. Package the project into a jar with dependencies via mvn clean package.
 * Then run the program via java -jar 'compiled-jar-with-dependencies-path' </p>
 *
 * <p> To run from IDE, set all the required environment variables in IntelliJ via Run -&gt; EditConfigurations
 * section.
 * Then run the App's main method via IDE.</p>
 */
public class App {

    /**
     *  main function.
     * @param args args
     * @throws RuntimeException If not able to load test classes.
     */
    public static void main(String[] args) {
        Class<?>[] testClasses;

        testClasses = new Class<?>[]{
            ReceiveAndDeleteMessageTest.class,
            ReceiveAndLockMessageTest.class,
            SendMessageTest.class,
            SendMessagesTest.class
        };

        PerfStressProgram.run(testClasses, args);
    }
}
