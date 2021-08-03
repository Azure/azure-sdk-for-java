// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.perf;

import com.azure.perf.test.core.PerfStressProgram;

/**
 * Runs Azure MetricsAdvisor performance tests.
 * <p>
 * Test scenarios:
 * <ul>
 *     <li>Lists the anomalies in an alert</li>
 *     <li>Lists the incidents in an alert</li>
 *     <li>Lists the root causes of an incident</li>
 * </ul>
 * To run performance tests from the command line, package the project into an uber jar using {@code mvn clean
 * package}. Then run the program using {@code java -jar compiled-jar-with-dependencies-path}
 * <p>
 * To run performance tests in IntelliJ, set all the required environment variables via {@code Run -&gt; Edit
 * Configurations} section. Then run the App's main method.
 */
public class App {
    /**
     * Runs the performance tests for Azure MetricsAdvisor SDK for Java.
     *
     * <p>To run from command line. Package the project into a jar with dependencies via mvn clean package.
     * Then run the program via java -jar 'compiled-jar-with-dependencies-path' </p>
     *
     * <p> To run from IDE, set all the required environment variables in IntelliJ via Run -&gt; EditConfigurations
     * section.
     * Then run the App's main method via IDE.</p>
     *
     * @param args the command line arguments ro run performance tests with.
     */
    public static void main(String[] args) {
        PerfStressProgram.run(
            new Class<?>[] {
                AnomaliesListTest.class,
                IncidentsListTest.class,
                RootCauseListTest.class
            }, args);
    }
}
