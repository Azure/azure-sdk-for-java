// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.rx.examples.multimaster.samples;

import com.azure.data.cosmos.rx.examples.multimaster.ConfigurationManager;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class Main {
    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            help();
            System.exit(1);
        }

        try (InputStream inputStream = new FileInputStream(args[0])) {
            ConfigurationManager.getAppSettings().load(inputStream);
            System.out.println("Using file " + args[0] + " for the setting.");
        }

        Main.runScenarios();
    }

    private static void runScenarios() throws Exception {
        MultiMasterScenario scenario = new MultiMasterScenario();
        scenario.initialize();

        scenario.runBasic();

        scenario.runManualConflict();
        scenario.runLWW();
        scenario.runUDP();

        System.out.println("Finished");

        //shutting down the active the resources
        scenario.shutdown();
    }

    private static void help() throws IOException {
        System.out.println("Provide the path to setting file in the following format: ");
        try (InputStream inputStream =
                     Main.class.getClassLoader()
                             .getResourceAsStream("multi-master-sample-config.properties")) {

            IOUtils.copy(inputStream, System.out);

            System.out.println();
        } catch (Exception e) {
            throw e;
        }
    }
}
