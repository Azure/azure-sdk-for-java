// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.MatchConditions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.data.appconfiguration.implementation.ConfigurationClientImpl;
import com.azure.data.appconfiguration.implementation.FeatureFlagClientsImpl;
import com.azure.data.appconfiguration.implementation.FeatureFlagSupport;
import com.azure.data.appconfiguration.implementation.SyncTokenPolicy;
import com.azure.data.appconfiguration.models.FeatureFlag;
import com.azure.data.appconfiguration.models.FeatureFlagSelector;
import com.azure.data.appconfiguration.models.SettingLabel;
import com.azure.data.appconfiguration.models.SettingLabelSelector;
import java.time.OffsetDateTime;

/**
 * Synchronous client for dedicated feature flag resources, available with API version {@code 2026-05-01-preview}.
 *
 * <p>These resources are separate from the key-value-backed
 * {@link com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting feature flag settings}
 * managed by {@link ConfigurationClient}. Names are used directly, without a key prefix.</p>
 *
 * <pre>
 * FeatureFlagClient client = new FeatureFlagClientBuilder().connectionString(connectionString).buildClient();
 * FeatureFlag flag = client.addFeatureFlag(new FeatureFlag("MyFeature", true));
 * </pre>
 */
@ServiceClient(
    builder = FeatureFlagClientBuilder.class,
    serviceInterfaces = FeatureFlagClientsImpl.FeatureFlagClientsService.class)
public final class FeatureFlagClient {
    private final ConfigurationClientImpl serviceClient;
    private final SyncTokenPolicy syncTokenPolicy;

    FeatureFlagClient(ConfigurationClientImpl serviceClient, SyncTokenPolicy syncTokenPolicy) {
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
    public FeatureFlag addFeatureFlag(FeatureFlag featureFlag) {
        return addFeatureFlagWithResponse(featureFlag, Context.NONE).getValue();
    }

    /**
     * Creates a feature flag only if its name and label do not already exist.
     * @param featureFlag the resource to create.
     * @param context the request context.
     * @return the created resource and HTTP response metadata.
     * @throws HttpResponseException if the resource exists or the service rejects the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<FeatureFlag> addFeatureFlagWithResponse(FeatureFlag featureFlag, Context context) {
        FeatureFlagSupport.updateConditions(featureFlag, false);
        return FeatureFlagSupport.toResponse(serviceClient.getFeatureFlagClients()
            .putFeatureFlagWithResponse(featureFlag.getName(),
                FeatureFlagSupport.putOptions(featureFlag, new MatchConditions().setIfNoneMatch("*"), context)));
    }

    /**
     * Creates or replaces a feature flag unconditionally.
     * @param featureFlag the resource to write.
     * @return the resource returned by the service.
     * @throws HttpResponseException if the service rejects the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public FeatureFlag setFeatureFlag(FeatureFlag featureFlag) {
        return setFeatureFlagWithResponse(featureFlag, false, Context.NONE).getValue();
    }

    /**
     * Creates or replaces a feature flag, optionally requiring its ETag to be unchanged.
     * @param featureFlag the resource to write.
     * @param ifUnchanged whether to require the resource's ETag to match.
     * @param context the request context.
     * @return the resource and HTTP response metadata.
     * @throws IllegalArgumentException if {@code ifUnchanged} is true but no ETag was supplied.
     * @throws HttpResponseException if the condition fails or the service rejects the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<FeatureFlag> setFeatureFlagWithResponse(FeatureFlag featureFlag, boolean ifUnchanged,
        Context context) {
        MatchConditions conditions = FeatureFlagSupport.updateConditions(featureFlag, ifUnchanged);
        return FeatureFlagSupport.toResponse(serviceClient.getFeatureFlagClients()
            .putFeatureFlagWithResponse(featureFlag.getName(),
                FeatureFlagSupport.putOptions(featureFlag, conditions, context)));
    }

    /**
     * Gets a feature flag without a label.
     * @param name the resource name, without a key prefix.
     * @return the resource.
     * @throws HttpResponseException if the resource does not exist or the service rejects the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public FeatureFlag getFeatureFlag(String name) {
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
    public FeatureFlag getFeatureFlag(String name, String label) {
        return getFeatureFlagWithResponse(name, label, null, null, Context.NONE).getValue();
    }

    /**
     * Gets a feature flag, optionally conditionally or as it existed at a given time.
     * @param name the resource name.
     * @param label the label, or null for an unlabeled resource.
     * @param conditions the ETag conditions, or null for an unconditional read.
     * @param acceptDateTime the point in time, or null for the current resource.
     * @param context the request context.
     * @return the resource and response metadata; a 304 response has a null value.
     * @throws HttpResponseException if the resource does not exist or the service rejects the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<FeatureFlag> getFeatureFlagWithResponse(String name, String label, MatchConditions conditions,
        OffsetDateTime acceptDateTime, Context context) {
        FeatureFlagSupport.validateName(name);
        try {
            return FeatureFlagSupport.toResponse(serviceClient.getFeatureFlagClients()
                .getFeatureFlagWithResponse(name,
                    FeatureFlagSupport.options(label, conditions, acceptDateTime, context)));
        } catch (HttpResponseException error) {
            return FeatureFlagSupport.notModified(error);
        }
    }

    /**
     * Deletes a feature flag without a label.
     * @param name the resource name.
     * @return the deleted resource, or null if no resource was deleted.
     * @throws HttpResponseException if the service rejects the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public FeatureFlag deleteFeatureFlag(String name) {
        return deleteFeatureFlag(name, null);
    }

    /**
     * Deletes a feature flag by name and label.
     * @param name the resource name.
     * @param label the label, or null for an unlabeled resource.
     * @return the deleted resource, or null if no resource was deleted.
     * @throws HttpResponseException if the service rejects the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public FeatureFlag deleteFeatureFlag(String name, String label) {
        return deleteFeatureFlagWithResponse(new FeatureFlag(name, false).setLabel(label), false, Context.NONE)
            .getValue();
    }

    /**
     * Deletes a feature flag, optionally requiring its ETag to be unchanged.
     * @param featureFlag the resource whose name, label, and ETag identify the deletion.
     * @param ifUnchanged whether to require the ETag to match.
     * @param context the request context.
     * @return the deleted resource and response metadata, with a null value for an empty response.
     * @throws IllegalArgumentException if {@code ifUnchanged} is true but no ETag was supplied.
     * @throws HttpResponseException if the condition fails or the service rejects the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<FeatureFlag> deleteFeatureFlagWithResponse(FeatureFlag featureFlag, boolean ifUnchanged,
        Context context) {
        MatchConditions conditions = FeatureFlagSupport.updateConditions(featureFlag, ifUnchanged);
        return FeatureFlagSupport.toResponse(serviceClient.getFeatureFlagClients()
            .deleteFeatureFlagWithResponse(featureFlag.getName(),
                FeatureFlagSupport.options(featureFlag.getLabel(), conditions, null, context)));
    }

    /**
     * Lists all dedicated feature flags.
     * @return the paged resources.
     * @throws HttpResponseException if the service rejects a request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<FeatureFlag> listFeatureFlags() {
        return listFeatureFlags(null);
    }

    /**
     * Lists dedicated feature flags matching a selector.
     * @param selector the optional filters, fields, date, and per-page match conditions.
     * @return the paged resources.
     * @throws HttpResponseException if the service rejects a request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<FeatureFlag> listFeatureFlags(FeatureFlagSelector selector) {
        return listFeatureFlags(selector, Context.NONE);
    }

    /**
     * Lists dedicated feature flags matching a selector.
     * @param selector the optional filters, fields, date, and per-page match conditions.
     * @param context the request context.
     * @return the paged resources; unchanged pages have status 304 and no items.
     * @throws HttpResponseException if the service rejects a request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<FeatureFlag> listFeatureFlags(FeatureFlagSelector selector, Context context) {
        return FeatureFlagSupport.list(serviceClient, selector, false, context);
    }

    /**
     * Lists all dedicated feature flag revisions.
     * @return the paged revisions.
     * @throws HttpResponseException if the service rejects a request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<FeatureFlag> listFeatureFlagRevisions() {
        return listFeatureFlagRevisions(null);
    }

    /**
     * Lists dedicated feature flag revisions matching a selector.
     * @param selector the optional selector.
     * @return the paged revisions.
     * @throws HttpResponseException if the service rejects a request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<FeatureFlag> listFeatureFlagRevisions(FeatureFlagSelector selector) {
        return listFeatureFlagRevisions(selector, Context.NONE);
    }

    /**
     * Lists dedicated feature flag revisions matching a selector.
     * @param selector the optional selector.
     * @param context the request context.
     * @return the paged revisions.
     * @throws HttpResponseException if the service rejects a request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<FeatureFlag> listFeatureFlagRevisions(FeatureFlagSelector selector, Context context) {
        return FeatureFlagSupport.list(serviceClient, selector, true, context);
    }

    /**
     * Lists labels belonging only to dedicated feature flag resources.
     * @return the paged labels.
     * @throws HttpResponseException if the service rejects a request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SettingLabel> listLabels() {
        return listLabels(null);
    }

    /**
     * Lists labels belonging only to dedicated feature flag resources.
     * @param selector the optional label selector.
     * @return the paged labels.
     * @throws HttpResponseException if the service rejects a request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SettingLabel> listLabels(SettingLabelSelector selector) {
        return listLabels(selector, Context.NONE);
    }

    /**
     * Lists labels belonging only to dedicated feature flag resources.
     * @param selector the optional label selector.
     * @param context the request context.
     * @return the paged labels.
     * @throws HttpResponseException if the service rejects a request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SettingLabel> listLabels(SettingLabelSelector selector, Context context) {
        return FeatureFlagSupport.labels(serviceClient, selector, context);
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
