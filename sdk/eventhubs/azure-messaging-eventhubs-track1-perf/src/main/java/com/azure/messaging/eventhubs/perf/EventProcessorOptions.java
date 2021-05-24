package com.azure.messaging.eventhubs.perf;

import com.beust.jcommander.Parameter;

public class EventProcessorOptions extends EventHubsReceiveOptions {
    @Parameter(names = {"--output" }, description = "Name of a file to output results to. If null, then System.out.")
    private String outputFile;

    @Parameter(names = {"-scs", "--storageConnectionString"}, description = "Connection string for Storage account.",
        required = true)
    private String storageConnectionString;

    @Parameter(names = {"-se", "--storageEndpoint"}, description = "Endpoint for storage account",
        required = true)
    private String storageEndpoint;

    /**
     * Creates a new instance of the options.
     */
    public EventProcessorOptions() {
        super();
    }

    /**
     * Gets the name of the output file to write results to.
     *
     * @return The name of the output file to write results to. {@code null} to output to System.out.
     */
    public String getOutputFile() {
        return outputFile;
    }

    /**
     * Gets the connection string for the storage account.
     *
     * @return The connection string for the storage account.
     */
    public String getStorageConnectionString() {
        return storageConnectionString;
    }

    /**
     * Gets the endpoint for the storage account. For example: {@literal https://foo.blob.core.windows.net/}
     *
     * @return The endpoint for the storage account.
     */
    public String getStorageEndpoint() {
        return storageEndpoint;
    }
}
