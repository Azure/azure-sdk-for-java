// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.perf;

import com.azure.perf.test.core.PerfStressProgram;

/**
 * Runs the Azure Container Registry performance test.
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
     * Runs the Container Registry performance tests.
     *
     * @param args Performance test configurations.
     */
    public static void main(String[] args) {
        PerfStressProgram.run(new Class<?>[]{
            GetManifestPropertiesTest.class,
            ListRepositoryTests.class,
            UploadBlobTests.class,
            DownloadBlobTests.class
        }, args);
    }
}
