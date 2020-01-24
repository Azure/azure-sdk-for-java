// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.logging;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientLoggerPerformance {


    public static void main(String[] args) throws IOException {
        System.in.read();
        int repetitions = 1;
        long numberOfLogs = 1_000_000;

        ClientLogger clientLogger = new ClientLogger(ClientLoggerPerformance.class);
        Logger logbackLogger = LoggerFactory.getLogger(ClientLoggerPerformance.class);

        for (int r = 0; r < repetitions; r++) {
//      long startLogbackTimer = System.nanoTime();
//      for (long i = 0; i < numberOfLogs; i++) {
//        logbackLogger.debug("Hello world");
//      }
//      long logbackTimeTaken = System.nanoTime() - startLogbackTimer;

            long startCientLoggerTimer = System.nanoTime();
            for (long i = 0; i < numberOfLogs; i++) {
                clientLogger.info("Hello world");
            }
            long clientLoggerTimeTaken = System.nanoTime() - startCientLoggerTimer;
//      System.out.println("Logback time in ms = " + TimeUnit.NANOSECONDS.toMillis(logbackTimeTaken));
            System.out.println("ClientLogger time in ms = " + TimeUnit.NANOSECONDS.toMillis(clientLoggerTimeTaken));
        }
    }

}
