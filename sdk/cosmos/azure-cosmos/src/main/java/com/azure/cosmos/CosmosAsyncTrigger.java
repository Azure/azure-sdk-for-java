// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.util.Context;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.Trigger;
import com.azure.cosmos.models.CosmosTriggerResponse;
import com.azure.cosmos.models.CosmosTriggerProperties;
import com.azure.cosmos.models.ModelBridgeInternal;
import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.withContext;

/**
 * The type Cosmos async trigger. This contains methods to operate on a cosmos trigger asynchronously
 */
public class CosmosAsyncTrigger {

    private final CosmosAsyncContainer container;

    @SuppressWarnings("EnforceFinalFields")
    private String id;

    CosmosAsyncTrigger(String id, CosmosAsyncContainer container) {
        this.id = id;
        this.container = container;
    }

    /**
     * Get the id of the {@link CosmosAsyncTrigger}
     *
     * @return the id of the {@link CosmosAsyncTrigger}
     */
    public String getId() {
        return id;
    }

    /**
     * Set the id of the {@link CosmosAsyncTrigger}
     *
     * @param id the id of the {@link CosmosAsyncTrigger}
     * @return the same {@link CosmosAsyncTrigger} that had the id set
     */
    CosmosAsyncTrigger setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Reads a cosmos trigger
     * <!-- src_embed com.azure.cosmos.CosmosAsyncTrigger.read -->
     * <pre>
     * String id = &quot;ID&quot;;
     * CosmosAsyncTrigger trigger =
     *      new CosmosAsyncTrigger&#40;id, cosmosAsyncContainer&#41;;
     *
     * Mono&lt;CosmosTriggerResponse&gt; response = trigger.read&#40;&#41;;
     * <pre>
     * <!-- end com.azure.cosmos.CosmosAsyncTrigger.read -->
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response for the read trigger.
     * In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single resource response for the read cosmos trigger or an error.
     */
    public Mono<CosmosTriggerResponse> read() {
        return withContext(this::readInternal);
    }

    /**
     * Replaces a cosmos trigger.
     * <!-- src_embed com.azure.cosmos.CosmosAsyncTrigger.replace -->
     * <pre>
     * CosmosAsyncTrigger trigger =
     *      new CosmosAsyncTrigger&#40;id, cosmosAsyncContainer&#41;;
     * CosmosTriggerProperties properties =
     *      new CosmosTriggerProperties&#40;id, body&#41;;
     *
     * Mono&lt;CosmosTriggerResponse&gt; response =
     *      trigger.replace&#40;properties&#41;;
     * <pre>
     * <!-- end com.azure.cosmos.CosmosAsyncTrigger.replace -->
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the replaced trigger.
     * In case of failure the {@link Mono} will error.
     *
     * @param triggerProperties the cosmos trigger properties.
     * @return an {@link Mono} containing the single resource response with the replaced cosmos trigger or an error.
     */
    public Mono<CosmosTriggerResponse> replace(CosmosTriggerProperties triggerProperties) {
        return withContext(context -> replaceInternal(triggerProperties, context));
    }

    /**
     * Deletes a cosmos trigger.
     * <!-- src_embed com.azure.cosmos.CosmosAsyncTrigger.delete -->
     * <pre>
     * String id = &quot;ID&quot;;
     * CosmosAsyncTrigger trigger =
     *      new CosmosAsyncTrigger&#40;id, cosmosAsyncContainer&#41;;
     *
     * Mono&lt;CosmosTriggerResponse&gt; response = trigger.delete&#40;&#41;;
     * <pre>
     * <!-- end com.azure.cosmos.CosmosAsyncTrigger.delete -->
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response for the deleted trigger.
     * In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single resource response for the deleted cosmos trigger or an error.
     */
    public Mono<CosmosTriggerResponse> delete() {
        return withContext(this::deleteInternal);
    }

    String getURIPathSegment() {
        return Paths.TRIGGERS_PATH_SEGMENT;
    }

    String getParentLink() {
        return container.getLink();
    }

    String getLink() {
        return getParentLink()
            + "/"
            + getURIPathSegment()
            + "/"
            + getId();
    }

    private Mono<CosmosTriggerResponse> readInternal(Context context) {
        String spanName = "readTrigger." + container.getId();
        Mono<CosmosTriggerResponse> responseMono = container.getDatabase()
            .getDocClientWrapper()
            .readTrigger(getLink(), null)
            .map(ModelBridgeInternal::createCosmosTriggerResponse)
            .single();
        CosmosAsyncClient client = container.getDatabase().getClient();
        return client.getDiagnosticsProvider().traceEnabledCosmosResponsePublisher(
            responseMono,
            context,
            spanName,
            container.getDatabase().getId(),
            container.getId(),
            client,
            null,
            OperationType.Read,
            ResourceType.Trigger,
            client.getEffectiveDiagnosticsThresholds(null));
    }

    private Mono<CosmosTriggerResponse> replaceInternal(CosmosTriggerProperties triggerSettings, Context context) {
        String spanName = "replaceTrigger." + container.getId();
        Mono<CosmosTriggerResponse> responseMono = container.getDatabase()
            .getDocClientWrapper()
            .replaceTrigger(new Trigger(ModelBridgeInternal.toJsonFromJsonSerializable(
                ModelBridgeInternal.getResource(triggerSettings))), null)
            .map(ModelBridgeInternal::createCosmosTriggerResponse)
            .single();
        CosmosAsyncClient client = container.getDatabase().getClient();

        return client.getDiagnosticsProvider().traceEnabledCosmosResponsePublisher(
            responseMono,
            context,
            spanName,
            container.getDatabase().getId(),
            container.getId(),
            client,
            null,
            OperationType.Replace,
            ResourceType.Trigger,
            client.getEffectiveDiagnosticsThresholds(null));
    }

    private Mono<CosmosTriggerResponse> deleteInternal(Context context) {
        String spanName = "deleteTrigger." + container.getId();
        Mono<CosmosTriggerResponse> responseMono = container.getDatabase()
            .getDocClientWrapper()
            .deleteTrigger(getLink(), null)
            .map(ModelBridgeInternal::createCosmosTriggerResponse)
            .single();
        CosmosAsyncClient client = container.getDatabase().getClient();
        return client.getDiagnosticsProvider().traceEnabledCosmosResponsePublisher(
            responseMono,
            context,
            spanName,
            container.getDatabase().getId(),
            container.getId(),
            client,
            null,
            OperationType.Delete,
            ResourceType.Trigger,
            client.getEffectiveDiagnosticsThresholds(null));
    }
}
