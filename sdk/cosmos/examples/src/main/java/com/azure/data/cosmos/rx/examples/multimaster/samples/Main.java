/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
