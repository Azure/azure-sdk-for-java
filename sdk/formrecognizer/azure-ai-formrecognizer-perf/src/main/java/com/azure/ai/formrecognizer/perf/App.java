// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.perf;

import com.azure.perf.test.core.PerfStressProgram;

/**
 * Runs Azure FormRecognizer performance tests.
 * <p>
 * Test scenarios:
 * <ul>
 *     <li>Use labeled custom model for extracting details on a custom form</li>
 * </ul>
 * To run performance tests from the command line, package the project into an uber jar using {@code mvn clean
 * package}. Then run the program using {@code java -jar compiled-jar-with-dependencies-path}
 * <p>
 * To run performance tests in IntelliJ, set all the required environment variables via {@code Run -&gt; Edit
 * Configurations} section. Then run the App's main method.
 */
public class App {

    /**
     * Main method to invoke performance tests
     *
     * @param args Arguments to the performance tests.
     */
    public static void main(String[] args) {
        PerfStressProgram.run(
            new Class<?>[]{DocumentModelAnalysisTest.class}, args);
    }
}
