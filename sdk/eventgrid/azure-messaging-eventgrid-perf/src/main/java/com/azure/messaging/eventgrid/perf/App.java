// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.perf;

import com.azure.perf.test.core.PerfStressProgram;

/**
 * Runs the Event Grid performance test.
 *
 * <p>To run from command line. Package the project into a jar with dependencies via mvn clean package.
 * Then run the program with 'java -jar azure-messaging-eventgrid-perf-1.0.0-beta.1-jar-with-dependencies.jar' </p>
 *
 * <p> To run from IDE, set all the required environment variables in IntelliJ via Run -&gt; EditConfigurations section.
 * Then run the App's main method via IDE.</p>
 */
public class App {
    /**
     * Run the EventGrid perf test
     * @param args the arguments
     */
    public static void main(String[] args) {
        PerfStressProgram.run(new Class<?>[] {
            SendCloudEventsTest.class,
            SendEventGridEventsTest.class,
            SendCustomEventsTest.class
        }, args);
    }
}
