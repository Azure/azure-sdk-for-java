// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.perf;

import com.azure.perf.test.core.PerfStressProgram;
import com.azure.resourcemanager.perf.dns.CreateDnsZonesTest;
import com.azure.resourcemanager.perf.resources.ListResourceGroupsTest;
import com.azure.resourcemanager.perf.resources.ListSubscriptionsTest;
import com.azure.resourcemanager.perf.resources.ListTenantsTest;
import com.azure.resourcemanager.perf.storage.CreateStorageAccountsTest;

/**
 * Runs the resource manager performance test.
 *
 * <p>To run from command line. Package the project into a jar with dependencies via mvn clean package.
 * Then run the program via java -jar 'compiled-jar-with-dependencies-path' </p>
 *
 * <p> To run from IDE, set all the required environment variables in IntelliJ via Run -&gt; EditConfigurations section.
 * Then run the App's main method via IDE.</p>
 */
public class App {
    public static void main(String[] args) {
        PerfStressProgram.run(new Class<?>[]{
            CreateDnsZonesTest.class,
            CreateStorageAccountsTest.class,
            ListResourceGroupsTest.class,
            ListSubscriptionsTest.class,
            ListTenantsTest.class
        }, args);
    }
}
