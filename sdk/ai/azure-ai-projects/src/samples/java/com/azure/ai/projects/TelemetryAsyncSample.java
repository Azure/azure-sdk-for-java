// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
//package com.azure.ai.projects;
//
//import com.azure.core.util.Configuration;
//import com.azure.identity.DefaultAzureCredentialBuilder;
//import reactor.core.publisher.Mono;
//import java.time.Duration;
//
//public class TelemetryAsyncSample {
//
//    private static TelemetryAsyncClient telemetryAsyncClient
//        = new AIProjectClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
//        .credential(new DefaultAzureCredentialBuilder().build())
//        .buildTelemetryAsyncClient();
//
//    public static void main(String[] args) {
//
//        getConnectionString()
//            .block(Duration.ofMinutes(1));
//    }
//
//    public static Mono<Void> getConnectionString() {
//        // BEGIN:com.azure.ai.projects.TelemetryAsyncSample.getConnectionString
//
//        return telemetryAsyncClient.getConnectionString()
//            .doOnNext(connectionString ->
//                System.out.println("Connection string (async): " + connectionString))
//            .doOnError(error ->
//                System.err.println("Error retrieving connection string: " + error.getMessage()))
//            .then();
//
//        // END:com.azure.ai.projects.TelemetryAsyncSample.getConnectionString
//    }
//}
