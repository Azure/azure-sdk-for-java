// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.data.tables.perf.CreateEntityWithAllTypes;
import com.azure.data.tables.perf.CreateEntityWithAllTypesUsingTransaction;
import com.azure.data.tables.perf.CreateEntityWithStringsOnly;
import com.azure.data.tables.perf.CreateEntityWithStringsOnlyUsingTransaction;
import com.azure.data.tables.perf.ListEntitiesWithStringsOnlyTest;
import com.azure.data.tables.perf.ListEntitiesWithAllTypesTest;
import com.azure.perf.test.core.PerfStressProgram;

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
        PerfStressProgram.run(new Class<?>[]{
            CreateEntityWithAllTypes.class,
            CreateEntityWithStringsOnly.class,
            ListEntitiesWithAllTypesTest.class,
            ListEntitiesWithStringsOnlyTest.class,
            CreateEntityWithAllTypesUsingTransaction.class,
            CreateEntityWithStringsOnlyUsingTransaction.class
        }, args);
    }
}
