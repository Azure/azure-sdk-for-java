// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

/**
 * Runs the Azure Data Tables performance test.
 *
 * <p>To run from command line. Package the project into a jar with dependencies via mvn clean package.
 * Then run the program via java -jar 'compiled-jar-with-dependencies-path' </p>
 *
 * <p> To run from IDE, set all the required environment variables in IntelliJ via Run -&gt; EditConfigurations section.
 * Then run the App's main method via IDE.</p>
 */
public class App {
    public static void main(String[] args) {
        Class<?>[] testClasses;

        try {
            testClasses = new Class<?>[] {
                Class.forName("com.azure.<service>.<sub-package>.perf.<APIName>Test"),
                Class.forName("com.azure.<service>.<sub-package>.perf.<APIName>Test")
            };
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        PerfStressProgram.run(testClasses, args);
    }
}
