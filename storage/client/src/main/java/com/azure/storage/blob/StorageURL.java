// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;//// Copyright (c) Microsoft Corporation. All rights reserved.
//// Licensed under the MIT License.
//
//package com.azure.storage.blob;
//
//import com.azure.core.http.HttpPipeline;
//import com.azure.core.implementation.http.UrlBuilder;
//import com.microsoft.rest.v2.http.HttpPipeline;
//import com.microsoft.rest.v2.http.HttpPipelineOptions;
//import com.microsoft.rest.v2.http.UrlBuilder;
//import com.microsoft.rest.v2.policy.DecodingPolicyFactory;
//import com.microsoft.rest.v2.policy.RequestPolicyFactory;
//
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.ArrayList;
//
///**
// * Represents a URL to a Azure storage object. Typically this class is only needed to generate a new pipeline. In most
// * cases, one of the other URL types will be more useful.
// */
//public abstract class StorageURL {
//
//    protected final GeneratedStorageClient storageClient;
//
//    protected StorageURL(URL url, HttpPipeline pipeline) {
//        if (url == null) {
//            throw new IllegalArgumentException("url cannot be null.");
//        }
//        if (pipeline == null) {
//            throw new IllegalArgumentException("Pipeline cannot be null. Create a pipeline by calling"
//                    + " StorageURL.createPipeline.");
//        }
//
//        this.storageClient = new GeneratedStorageClient(pipeline)
//                .withVersion(Constants.HeaderConstants.TARGET_STORAGE_VERSION);
//        this.storageClient.withUrl(url.toString());
//    }
//
//
//
//    /**
//     * Creates an pipeline to process the HTTP requests and Responses.
//     *
//     * @apiNote
//     * ## Sample Code \n
//     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=service_url "Sample code for StorageURL.createPipeline")] \n
//     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
//     *
//     * @return The pipeline.
//     */
//    public static HttpPipeline createPipeline() {
//        return createPipeline(new AnonymousCredentials(), new PipelineOptions());
//    }
//
//    /**
//     * Creates an pipeline to process the HTTP requests and Responses.
//     *
//     * @apiNote
//     * ## Sample Code \n
//     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=service_url "Sample code for StorageURL.createPipeline")] \n
//     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
//     *
//     * @param credentials
//     *         The credentials the pipeline will use to authenticate the requests.
//     *
//     * @return The pipeline.
//     */
//    public static HttpPipeline createPipeline(ICredentials credentials) {
//        return createPipeline(credentials, new PipelineOptions());
//    }
//
//    /**
//     * Creates an pipeline to process the HTTP requests and Responses.
//     *
//     * @apiNote
//     * ## Sample Code \n
//     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=service_url "Sample code for StorageURL.createPipeline")] \n
//     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
//     *
//     * @param pipelineOptions
//     *         Configurations for each policy in the pipeline.
//     * @return The pipeline.
//     */
//    public static HttpPipeline createPipeline(PipelineOptions pipelineOptions) {
//        return createPipeline(new AnonymousCredentials(), pipelineOptions);
//    }
//
//    /**
//     * Creates an pipeline to process the HTTP requests and Responses.
//     *
//     * @apiNote
//     * ## Sample Code \n
//     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=service_url "Sample code for StorageURL.createPipeline")] \n
//     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
//     *
//     * @param credentials
//     *         The credentials the pipeline will use to authenticate the requests.
//     * @param pipelineOptions
//     *         Configurations for each policy in the pipeline.
//     *
//     * @return The pipeline.
//     *
//     * @apiNote ## Sample Code \n
//     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=service_url "Sample code for StorageURL.createPipeline")] \n
//     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
//     */
//    public static HttpPipeline createPipeline(ICredentials credentials, PipelineOptions pipelineOptions) {
//        /*
//        PipelineOptions is mutable, but its fields refer to immutable objects. This method can pass the fields to other
//        methods, but the PipelineOptions object itself can only be used for the duration of this call; it must not be
//        passed to anything with a longer lifetime.
//         */
//        if (credentials == null) {
//            throw new IllegalArgumentException(
//                    "Credentials cannot be null. For anonymous access use Anonymous Credentials.");
//        }
//        if (pipelineOptions == null) {
//            throw new IllegalArgumentException("pipelineOptions cannot be null. You must at least specify a client.");
//        }
//
//        // Closest to API goes first, closest to wire goes last.
//        ArrayList<RequestPolicyFactory> factories = new ArrayList<>();
//        factories.add(new TelemetryPolicy(pipelineOptions.telemetryOptions()));
//        factories.add(new RequestIDPolicy());
//        factories.add(new RequestRetryPolicy(pipelineOptions.requestRetryOptions()));
//        if (!(credentials instanceof AnonymousCredentials)) {
//            factories.add(credentials);
//        }
//        factories.add(new SetResponseFieldPolicy());
//        factories.add(new DecodingPolicyFactory());
//        factories.add(new LoggingPolicy(pipelineOptions.loggingOptions()));
//
//        return HttpPipeline.build(new HttpPipelineOptions().withHttpClient(pipelineOptions.client())
//                        .withLogger(pipelineOptions.logger()),
//                factories.toArray(new RequestPolicyFactory[factories.size()]));
//    }
//
//    @Override
//    public String toString() {
//        return this.storageClient.url();
//    }
//
//    /**
//     * @return The underlying url to the resource.
//     */
//    public URL toURL() {
//        try {
//            return new URL(this.storageClient.url());
//        } catch (MalformedURLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}
