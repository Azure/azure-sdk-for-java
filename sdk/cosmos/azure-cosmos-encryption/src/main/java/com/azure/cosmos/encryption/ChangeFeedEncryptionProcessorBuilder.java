// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.ChangeFeedProcessorBuilder;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Helper class to build a {@link ChangeFeedProcessor} instance.
 *
 * {@codesnippet com.azure.cosmos.changeFeedProcessor.builder}
 */
public class ChangeFeedEncryptionProcessorBuilder {

    private String hostName;
    private CosmosEncryptionAsyncContainer feedContainer;
    private CosmosAsyncContainer leaseContainer;
    private ChangeFeedProcessorOptions changeFeedProcessorOptions;
    private Consumer<List<JsonNode>> encryptionConsumer;

    /**
     * Helper class to build a encryption supported {@link ChangeFeedProcessor} instance.
     *
     */
    public ChangeFeedEncryptionProcessorBuilder() {
    }

    /**
     * Sets the host name.
     *
     * @param hostName the name to be used for the host. When using multiple hosts, each host must have a unique
     * name.
     * @return current Builder.
     */
    public ChangeFeedEncryptionProcessorBuilder hostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    /**
     * Sets an existing {@link CosmosEncryptionAsyncContainer} to be used to read from the monitored container.
     *
     * @param feedContainer the instance of {@link CosmosEncryptionAsyncContainer} to be used.
     * @return current Builder.
     */
    public ChangeFeedEncryptionProcessorBuilder feedContainer(CosmosEncryptionAsyncContainer feedContainer) {
        this.feedContainer = feedContainer;

        return this;
    }

    /**
     * Sets an existing {@link CosmosAsyncContainer} to be used to read from the leases container.
     *
     * @param leaseContainer the instance of {@link CosmosAsyncContainer} to use.
     * @return current Builder.
     */
    public ChangeFeedEncryptionProcessorBuilder leaseContainer(CosmosAsyncContainer leaseContainer) {
        this.leaseContainer = leaseContainer;

        return this;
    }

    /**
     * Sets a consumer function which will be called to process changes.
     *
     * {@codesnippet com.azure.cosmos.changeFeedProcessor.handleChanges}
     *
     * @param consumer the {@link Consumer} to call for handling the feeds.
     * @return current Builder.
     */
    public ChangeFeedEncryptionProcessorBuilder handleChanges(Consumer<List<JsonNode>> consumer) {
        this.encryptionConsumer = jsonNodes -> {
            List<byte[]> byteArrayList = jsonNodes.stream()
                .map(node -> feedContainer.cosmosSerializerToStream(node))
                .collect(Collectors.toList());
            List<Mono<byte[]>> byteArrayMonoList =
                byteArrayList.stream().map(bytes -> feedContainer.decryptResponse(bytes)).collect(Collectors.toList());
            Flux.concat(byteArrayMonoList).map(
                item -> feedContainer.getItemDeserializer().parseFrom(JsonNode.class, item)
            ).collectList().doOnSuccess(consumer).subscribe();
        };

        return this;
    }

    /**
     * Sets the {@link ChangeFeedProcessorOptions} to be used.
     * Unless specifically set the default values that will be used are:
     * <ul>
     * <li>maximum items per page or FeedResponse: 100</li>
     * <li>lease renew interval: 17 seconds</li>
     * <li>lease acquire interval: 13 seconds</li>
     * <li>lease expiration interval: 60 seconds</li>
     * <li>feed poll delay: 5 seconds</li>
     * <li>maximum scale count: unlimited</li>
     * </ul>
     *
     * @param changeFeedProcessorOptions the change feed processor options to use.
     * @return current Builder.
     */
    public ChangeFeedEncryptionProcessorBuilder options(ChangeFeedProcessorOptions changeFeedProcessorOptions) {
        this.changeFeedProcessorOptions = changeFeedProcessorOptions;

        return this;
    }

    /**
     * Builds a new instance of the {@link ChangeFeedProcessor} with the specified configuration.
     *
     * @return an instance of {@link ChangeFeedProcessor}.
     */
    public ChangeFeedProcessor buildChangeFeedProcessor() {
        ChangeFeedProcessorBuilder changeFeedProcessorBuilder = new ChangeFeedProcessorBuilder()
            .hostName(this.hostName)
            .feedContainer(this.feedContainer.getCosmosAsyncContainer())
            .leaseContainer(this.leaseContainer)
            .handleChanges(this.encryptionConsumer)
            .options(this.changeFeedProcessorOptions);

        return changeFeedProcessorBuilder.buildChangeFeedProcessor();
    }
}
