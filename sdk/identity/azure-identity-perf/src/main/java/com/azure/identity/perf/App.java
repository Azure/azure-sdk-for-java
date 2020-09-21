// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.perf;

import com.azure.perf.test.core.PerfStressProgram;

/**
 * Runs the Identity performance tests.
 *
 * Test scenarios: 1. Read cache from a single process 2. Read cache from multiple processes 3. Write cache from a
 * single process 4. Write cache from multiple processes
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
     * Entry point for running Azure Identity performance tests.
     *
     * @param args Arguments for running Azure Identity performance tests.
     */
    public static void main(String[] args) {
        PerfStressProgram.run(new Class<?>[]{
            ReadCache.class,
            WriteCache.class
        }, args);
    }
}
