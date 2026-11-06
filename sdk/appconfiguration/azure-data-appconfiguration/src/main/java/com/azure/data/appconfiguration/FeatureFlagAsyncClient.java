// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.MatchConditions;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.util.FluxUtil;
import com.azure.data.appconfiguration.implementation.ConfigurationClientImpl;
import com.azure.data.appconfiguration.implementation.FeatureFlagClientsImpl;
import com.azure.data.appconfiguration.implementation.FeatureFlagSupport;
import com.azure.data.appconfiguration.implementation.SyncTokenPolicy;
import com.azure.data.appconfiguration.models.FeatureFlag;
import com.azure.data.appconfiguration.models.FeatureFlagSelector;
import com.azure.data.appconfiguration.models.SettingLabel;
import com.azure.data.appconfiguration.models.SettingLabelSelector;
import java.time.OffsetDateTime;
import reactor.core.publisher.Mono;

/**
 * Asynchronous client for dedicated feature flag resources, available with API version {@code 2026-05-01-preview}.
 *
 * <p>These resources are separate from the key-value-backed
 * {@link com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting feature flag settings}
 * managed by {@link ConfigurationAsyncClient}. Names are used directly, without a key prefix.</p>
 *
 * <pre>
 * FeatureFlagAsyncClient client = new FeatureFlagClientBuilder().connectionString(connectionString).buildAsyncClient();
 * client.addFeatureFlag(new FeatureFlag("MyFeature", true)).subscribe(flag -&gt; System.out.println(flag.getName()));
 * </pre>
 */
@ServiceClient(
    builder = FeatureFlagClientBuilder.class,
    isAsync = true,
    serviceInterfaces = FeatureFlagClientsImpl.FeatureFlagClientsService.class)
public final class FeatureFlagAsyncClient {
    private final ConfigurationClientImpl serviceClient;
    private final SyncTokenPolicy syncTokenPolicy;

    FeatureFlagAsyncClient(ConfigurationClientImpl serviceClient, SyncTokenPolicy syncTokenPolicy) {
        this.serviceClient = serviceClient;
        this.syncTokenPolicy = syncTokenPolicy;
    }

    /**
     * Gets the service endpoint.
     * @return the service endpoint.
     */
    public String getEndpoint() {
        return serviceClient.getEndpoint();
    }

    /**
     * Creates a feature flag only if its name and label do not already exist.
     * @param featureFlag the resource to create.
     * @return the created resource.
     * @throws HttpResponseException if the resource exists or the service rejects the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<FeatureFlag> addFeatureFlag(FeatureFlag featureFlag) {
        return addFeatureFlagWithResponse(featureFlag).flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Creates a feature flag only if its name and label do not already exist.
     * @param featureFlag the resource to create.
     * @return the created resource and HTTP response metadata.
     * @throws HttpResponseException if the resource exists or the service rejects the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<FeatureFlag>> addFeatureFlagWithResponse(FeatureFlag featureFlag) {
        return Mono.defer(() -> {
            FeatureFlagSupport.updateConditions(featureFlag, false);
            return FluxUtil.withContext(context -> serviceClient.getFeatureFlagClients()
                .putFeatureFlagWithResponseAsync(featureFlag.getName(),
                    FeatureFlagSupport.putOptions(featureFlag, new MatchConditions().setIfNoneMatch("*"), context))
                .map(FeatureFlagSupport::toResponse));
        });
    }

    /**
     * Creates or replaces a feature flag unconditionally.
     * @param featureFlag the resource to write.
     * @return the resource returned by the service.
     * @throws HttpResponseException if the service rejects the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<FeatureFlag> setFeatureFlag(FeatureFlag featureFlag) {
        return setFeatureFlagWithResponse(featureFlag, false)
            .flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Creates or replaces a feature flag, optionally requiring its ETag to be unchanged.
     * @param featureFlag the resource to write.
     * @param ifUnchanged whether to require the resource's ETag to match.
     * @return the resource and HTTP response metadata.
     * @throws IllegalArgumentException if {@code ifUnchanged} is true but no ETag was supplied.
     * @throws HttpResponseException if the condition fails or the service rejects the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<FeatureFlag>> setFeatureFlagWithResponse(FeatureFlag featureFlag, boolean ifUnchanged) {
        return Mono.defer(() -> {
            MatchConditions conditions = FeatureFlagSupport.updateConditions(featureFlag, ifUnchanged);
            return FluxUtil.withContext(context -> serviceClient.getFeatureFlagClients()
                .putFeatureFlagWithResponseAsync(featureFlag.getName(),
                    FeatureFlagSupport.putOptions(featureFlag, conditions, context))
                .map(FeatureFlagSupport::toResponse));
        });
    }

    /**
     * Gets a feature flag without a label.
     * @param name the resource name, without a key prefix.
     * @return the resource.
     * @throws HttpResponseException if the resource does not exist or the service rejects the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<FeatureFlag> getFeatureFlag(String name) {
        return getFeatureFlag(name, null);
    }

    /**
     * Gets a feature flag by name and label.
     * @param name the resource name.
     * @param label the label, or null for an unlabeled resource.
     * @return the resource.
     * @throws HttpResponseException if the resource does not exist or the service rejects the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<FeatureFlag> getFeatureFlag(String name, String label) {
        return getFeatureFlagWithResponse(name, label, null, null)
            .flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Gets a feature flag, optionally conditionally or as it existed at a given time.
     * @param name the resource name.
     * @param label the label, or null for an unlabeled resource.
     * @param conditions the ETag conditions, or null for an unconditional read.
     * @param acceptDateTime the point in time, or null for the current resource.
     * @return the resource and response metadata; a 304 response has a null value.
     * @throws HttpResponseException if the resource does not exist or the service rejects the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<FeatureFlag>> getFeatureFlagWithResponse(String name, String label, MatchConditions conditions,
        OffsetDateTime acceptDateTime) {
        return Mono.defer(() -> {
            FeatureFlagSupport.validateName(name);
            return FluxUtil.withContext(context -> serviceClient.getFeatureFlagClients()
                .getFeatureFlagWithResponseAsync(name,
                    FeatureFlagSupport.options(label, conditions, acceptDateTime, context))
                .map(FeatureFlagSupport::toResponse)
                .onErrorResume(HttpResponseException.class, error -> Mono.just(FeatureFlagSupport.notModified(error))));
        });
    }

    /**
     * Deletes a feature flag without a label.
     * @param name the resource name.
     * @return the deleted resource, or an empty publisher if no resource was deleted.
     * @throws HttpResponseException if the service rejects the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<FeatureFlag> deleteFeatureFlag(String name) {
        return deleteFeatureFlag(name, null);
    }

    /**
     * Deletes a feature flag by name and label.
     * @param name the resource name.
     * @param label the label, or null for an unlabeled resource.
     * @return the deleted resource, or an empty publisher if no resource was deleted.
     * @throws HttpResponseException if the service rejects the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<FeatureFlag> deleteFeatureFlag(String name, String label) {
        return Mono.defer(() -> deleteFeatureFlagWithResponse(new FeatureFlag(name, false).setLabel(label), false))
            .flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Deletes a feature flag, optionally requiring its ETag to be unchanged.
     * @param featureFlag the resource whose name, label, and ETag identify the deletion.
     * @param ifUnchanged whether to require the ETag to match.
     * @return the deleted resource and response metadata, with a null value for an empty response.
     * @throws IllegalArgumentException if {@code ifUnchanged} is true but no ETag was supplied.
     * @throws HttpResponseException if the condition fails or the service rejects the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<FeatureFlag>> deleteFeatureFlagWithResponse(FeatureFlag featureFlag, boolean ifUnchanged) {
        return Mono.defer(() -> {
            MatchConditions conditions = FeatureFlagSupport.updateConditions(featureFlag, ifUnchanged);
            return FluxUtil.withContext(context -> serviceClient.getFeatureFlagClients()
                .deleteFeatureFlagWithResponseAsync(featureFlag.getName(),
                    FeatureFlagSupport.options(featureFlag.getLabel(), conditions, null, context))
                .map(FeatureFlagSupport::toResponse));
        });
    }

    /**
     * Lists all dedicated feature flags.
     * @return the paged resources.
     * @throws HttpResponseException if the service rejects a request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<FeatureFlag> listFeatureFlags() {
        return listFeatureFlags(null);
    }

    /**
     * Lists dedicated feature flags matching a selector.
     * @param selector the optional filters, fields, date, and per-page match conditions.
     * @return the paged resources; unchanged pages have status 304 and no items.
     * @throws HttpResponseException if the service rejects a request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<FeatureFlag> listFeatureFlags(FeatureFlagSelector selector) {
        return FeatureFlagSupport.listAsync(serviceClient, selector, false);
    }

    /**
     * Lists all dedicated feature flag revisions.
     * @return the paged revisions.
     * @throws HttpResponseException if the service rejects a request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<FeatureFlag> listFeatureFlagRevisions() {
        return listFeatureFlagRevisions(null);
    }

    /**
     * Lists dedicated feature flag revisions matching a selector.
     * @param selector the optional selector.
     * @return the paged revisions.
     * @throws HttpResponseException if the service rejects a request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<FeatureFlag> listFeatureFlagRevisions(FeatureFlagSelector selector) {
        return FeatureFlagSupport.listAsync(serviceClient, selector, true);
    }

    /**
     * Lists labels belonging only to dedicated feature flag resources.
     * @return the paged labels.
     * @throws HttpResponseException if the service rejects a request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SettingLabel> listLabels() {
        return listLabels(null);
    }

    /**
     * Lists labels belonging only to dedicated feature flag resources.
     * @param selector the optional label selector.
     * @return the paged labels.
     * @throws HttpResponseException if the service rejects a request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SettingLabel> listLabels(SettingLabelSelector selector) {
        return FeatureFlagSupport.labelsAsync(serviceClient, selector);
    }

    /**
     * Adds an external synchronization token for subsequent requests.
     * @param token the token.
     * @throws NullPointerException if the token is null.
     */
    public void updateSyncToken(String token) {
        syncTokenPolicy.updateSyncToken(token);
    }
}
