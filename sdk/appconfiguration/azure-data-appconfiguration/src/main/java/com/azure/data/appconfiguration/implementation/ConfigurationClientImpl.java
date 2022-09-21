// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Delete;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ReturnValueWireType;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.UnexpectedResponseExceptionType;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceExistsException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.ContentType;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingFields;
import com.azure.data.appconfiguration.models.SettingSelector;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * Initializes a new instance of the ConfigurationClient type.
 */
public final class ConfigurationClientImpl {
    // Please see <a href=https://docs.microsoft.com/en-us/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    private static final String ETAG_ANY = "*";

    private static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLE = "com.azure.core.http.restproxy.syncproxy.enable";

    private static final String APP_CONFIG_TRACING_NAMESPACE_VALUE = "Microsoft.AppConfiguration";

    /**
     * The proxy service used to perform REST calls.
     */
    private final ConfigurationService service;

    /**
     * Supported AppConfiguration endpoints
     */
    private final String endpoint;

    /**
     * Gets Supported AppConfiguration endpoints.
     *
     * @return the endpoint value.
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /**
     * Api Version.
     */
    private final String apiVersion;

    /**
     * Gets Api Version.
     *
     * @return the apiVersion value.
     */
    public String getApiVersion() {
        return this.apiVersion;
    }

    /**
     * The HTTP pipeline to send requests through.
     */
    private final HttpPipeline httpPipeline;

    /**
     * Gets The HTTP pipeline to send requests through.
     *
     * @return the httpPipeline value.
     */
    public HttpPipeline getHttpPipeline() {
        return this.httpPipeline;
    }

    /**
     * The serializer to serialize an object into a string.
     */
    private final SerializerAdapter serializerAdapter;

    private final ClientLogger logger = new ClientLogger(ConfigurationClientImpl.class);

    /**
     * Gets The serializer to serialize an object into a string.
     *
     * @return the serializerAdapter value.
     */
    public SerializerAdapter getSerializerAdapter() {
        return this.serializerAdapter;
    }

    /**
     * Initializes an instance of ConfigurationClientImpl.
     *
     * @param httpPipeline The HTTP pipeline to send requests through.
     * @param serializerAdapter The serializer to serialize an object into a string.
     * @param endpoint Supported Configuration Service endpoint.
     * @param apiVersion Api Version.
     */
    public ConfigurationClientImpl(
        HttpPipeline httpPipeline, SerializerAdapter serializerAdapter, String endpoint, String apiVersion) {
        this.httpPipeline = httpPipeline;
        this.serializerAdapter = serializerAdapter;
        this.endpoint = endpoint;
        this.apiVersion = apiVersion;
        this.service =
            RestProxy.create(ConfigurationService.class, this.getHttpPipeline(), this.getSerializerAdapter());
    }

    /**
     * The interface defining all the services for {@link ConfigurationAsyncClient} and {@link ConfigurationClient} to be used
     * by the proxy service to perform REST calls.
     * <p>
     * This is package-private so that these REST calls are transparent to the user.
     */
    @Host("{url}")
    @ServiceInterface(name = "ConfigurationClient")
    public interface ConfigurationService {
        @Get("kv/{key}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        Mono<Response<ConfigurationSetting>> getKeyValueAsync(
            @HostParam("url") String url,
            @PathParam("key") String key,
            @QueryParam("label") String label,
            @QueryParam("api-version") String apiVersion,
            @QueryParam("$select") String fields,
            @HeaderParam("Accept-Datetime") String acceptDatetime,
            @HeaderParam("If-Match") String ifMatch,
            @HeaderParam("If-None-Match") String ifNoneMatch,
            Context context);

        @Get("kv/{key}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        Response<ConfigurationSetting> getKeyValue(
            @HostParam("url") String url,
            @PathParam("key") String key,
            @QueryParam("label") String label,
            @QueryParam("api-version") String apiVersion,
            @QueryParam("$select") String fields,
            @HeaderParam("Accept-Datetime") String acceptDatetime,
            @HeaderParam("If-Match") String ifMatch,
            @HeaderParam("If-None-Match") String ifNoneMatch,
            Context context);

        @Put("kv/{key}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(code = {412}, value = ResourceExistsException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<ConfigurationSetting>> setKeyAsync(
            @HostParam("url") String url,
            @PathParam("key") String key,
            @QueryParam("label") String label,
            @QueryParam("api-version") String apiVersion,
            @BodyParam(ContentType.APPLICATION_JSON) ConfigurationSetting keyValueParameters,
            @HeaderParam("If-Match") String ifMatch,
            @HeaderParam("If-None-Match") String ifNoneMatch,
            Context context);

        @Put("kv/{key}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(code = {412}, value = ResourceExistsException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<ConfigurationSetting> setKey(
            @HostParam("url") String url,
            @PathParam("key") String key,
            @QueryParam("label") String label,
            @QueryParam("api-version") String apiVersion,
            @BodyParam(ContentType.APPLICATION_JSON) ConfigurationSetting keyValueParameters,
            @HeaderParam("If-Match") String ifMatch,
            @HeaderParam("If-None-Match") String ifNoneMatch,
            Context context);

        @Delete("kv/{key}")
        @ExpectedResponses({200, 204})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<ConfigurationSetting>> deleteAsync(
            @HostParam("url") String url,
            @PathParam("key") String key,
            @QueryParam("label") String label,
            @QueryParam("api-version") String apiVersion,
            @HeaderParam("If-Match") String ifMatch,
            @HeaderParam("If-None-Match") String ifNoneMatch,
            Context context);

        @Delete("kv/{key}")
        @ExpectedResponses({200, 204})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<ConfigurationSetting> delete(
            @HostParam("url") String url,
            @PathParam("key") String key,
            @QueryParam("label") String label,
            @QueryParam("api-version") String apiVersion,
            @HeaderParam("If-Match") String ifMatch,
            @HeaderParam("If-None-Match") String ifNoneMatch,
            Context context);

        @Put("locks/{key}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<ConfigurationSetting>> lockKeyValueAsync(
            @HostParam("url") String url,
            @PathParam("key") String key,
            @QueryParam("label") String label,
            @QueryParam("api-version") String apiVersion,
            @HeaderParam("If-Match") String ifMatch,
            @HeaderParam("If-None-Match") String ifNoneMatch,
            Context context);

        @Put("locks/{key}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<ConfigurationSetting> lockKeyValue(
            @HostParam("url") String url,
            @PathParam("key") String key,
            @QueryParam("label") String label,
            @QueryParam("api-version") String apiVersion,
            @HeaderParam("If-Match") String ifMatch,
            @HeaderParam("If-None-Match") String ifNoneMatch,
            Context context);

        @Delete("locks/{key}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<ConfigurationSetting>> unlockKeyValueAsync(
            @HostParam("url") String url,
            @PathParam("key") String key,
            @QueryParam("label") String label,
            @QueryParam("api-version") String apiVersion,
            @HeaderParam("If-Match") String ifMatch,
            @HeaderParam("If-None-Match") String ifNoneMatch,
            Context context);

        @Delete("locks/{key}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<ConfigurationSetting> unlockKeyValue(
            @HostParam("url") String url,
            @PathParam("key") String key,
            @QueryParam("label") String label,
            @QueryParam("api-version") String apiVersion,
            @HeaderParam("If-Match") String ifMatch,
            @HeaderParam("If-None-Match") String ifNoneMatch,
            Context context);

        @Get("kv")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(ConfigurationSettingPage.class)
        Mono<PagedResponse<ConfigurationSetting>> listKeyValuesAsync(
            @HostParam("url") String url,
            @QueryParam("key") String key,
            @QueryParam("label") String label,
            @QueryParam("api-version") String apiVersion,
            @QueryParam("$select") String fields,
            @HeaderParam("Accept-Datetime") String acceptDatetime,
            Context context);

        @Get("kv")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(ConfigurationSettingPage.class)
        PagedResponse<ConfigurationSetting> listKeyValues(
            @HostParam("url") String url,
            @QueryParam("key") String key,
            @QueryParam("label") String label,
            @QueryParam("api-version") String apiVersion,
            @QueryParam("$select") String fields,
            @HeaderParam("Accept-Datetime") String acceptDatetime,
            Context context);

        @Get("{nextUrl}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(ConfigurationSettingPage.class)
        Mono<PagedResponse<ConfigurationSetting>> listKeyValuesAsync(
            @HostParam("url") String url,
            @PathParam(value = "nextUrl", encoded = true) String nextUrl,
            Context context);

        @Get("{nextUrl}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(ConfigurationSettingPage.class)
        PagedResponse<ConfigurationSetting> listKeyValues(
            @HostParam("url") String url,
            @PathParam(value = "nextUrl", encoded = true) String nextUrl,
            Context context);

        @Get("revisions")
        @ExpectedResponses({200, 206})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(ConfigurationSettingPage.class)
        Mono<PagedResponse<ConfigurationSetting>> listKeyValueRevisionsAsync(
            @HostParam("url") String url,
            @QueryParam("key") String key,
            @QueryParam("label") String label,
            @QueryParam("api-version") String apiVersion,
            @QueryParam("$select") String fields,
            @HeaderParam("Accept-Datetime") String acceptDatetime,
            @HeaderParam("Range") String range,
            Context context);

        @Get("revisions")
        @ExpectedResponses({200, 206})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(ConfigurationSettingPage.class)
        PagedResponse<ConfigurationSetting> listKeyValueRevisions(
            @HostParam("url") String url,
            @QueryParam("key") String key,
            @QueryParam("label") String label,
            @QueryParam("api-version") String apiVersion,
            @QueryParam("$select") String fields,
            @HeaderParam("Accept-Datetime") String acceptDatetime,
            @HeaderParam("Range") String range,
            Context context);
    }

    public Mono<Response<ConfigurationSetting>> addConfigurationSettingWithResponseAsync(ConfigurationSetting setting,
        Context context) {
        // Validate that setting and key is not null. The key is used in the service URL, so it cannot be null.
        validateSetting(setting);
        context = context == null ? Context.NONE : context;

        // This service method call is similar to setConfigurationSetting except we're passing If-Not-Match = "*".
        // If the service finds any existing configuration settings, then its e-tag will match and the service will
        // return an error.
        return this.service.setKeyAsync(this.endpoint,
                setting.getKey(),
                setting.getLabel(),
                this.getApiVersion(),
                setting,
                null,
                getETagValue(ETAG_ANY),
                context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.verbose("Adding ConfigurationSetting - {}", setting))
            .doOnSuccess(response -> logger.verbose("Added ConfigurationSetting - {}", response.getValue()))
            .doOnError(error -> logger.warning("Failed to add ConfigurationSetting - {}", setting, error));
    }

    public Response<ConfigurationSetting> addConfigurationSettingWithResponse(ConfigurationSetting setting,
                                                                              Context context) {
        // Validate that setting and key is not null. The key is used in the service URL, so it cannot be null.
        validateSetting(setting);
        context = context == null ? Context.NONE : context;

        // This service method call is similar to setConfigurationSetting except we're passing If-Not-Match = "*".
        // If the service finds any existing configuration settings, then its e-tag will match and the service will
        // return an error.
        return this.service.setKey(this.endpoint,
            setting.getKey(),
            setting.getLabel(),
            this.getApiVersion(),
            setting,
            null,
            getETagValue(ETAG_ANY),
            context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE)
                .addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true));
    }

    public Mono<Response<ConfigurationSetting>> setConfigurationSettingWithResponseAsync(ConfigurationSetting setting,
                                                                                         boolean ifUnchanged,
                                                                                         Context context) {
        // Validate that setting and key is not null. The key is used in the service URL, so it cannot be null.
        validateSetting(setting);
        context = context == null ? Context.NONE : context;

        final String ifMatchETag = ifUnchanged ? getETagValue(setting.getETag()) : null;
        // This service method call is similar to addConfigurationSetting except it will create or update a
        // configuration setting.
        // If the user provides an ETag value, it is passed in as If-Match = "{ETag value}". If the current value in the
        // service has a matching ETag then it matches, then its value is updated with what the user passed in.
        // Otherwise, the service throws an exception because the current configuration value was updated, and we have an
        // old value locally.
        // If no ETag value was passed in, then the value is always added or updated.
        return this.service.setKeyAsync(this.getEndpoint(),
                setting.getKey(),
                setting.getLabel(),
                this.getApiVersion(),
                setting,
                ifMatchETag,
                null,
                context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.verbose("Setting ConfigurationSetting - {}", setting))
            .doOnSuccess(response -> logger.verbose("Set ConfigurationSetting - {}", response.getValue()))
            .doOnError(error -> logger.warning("Failed to set ConfigurationSetting - {}", setting, error));
    }

    public Response<ConfigurationSetting> setConfigurationSettingWithResponse(ConfigurationSetting setting,
                                                                              boolean ifUnchanged,
                                                                              Context context) {
        // Validate that setting and key is not null. The key is used in the service URL, so it cannot be null.
        validateSetting(setting);
        context = context == null ? Context.NONE : context;

        final String ifMatchETag = ifUnchanged ? getETagValue(setting.getETag()) : null;
        // This service method call is similar to addConfigurationSetting except it will create or update a
        // configuration setting.
        // If the user provides an ETag value, it is passed in as If-Match = "{ETag value}". If the current value in the
        // service has a matching ETag then it matches, then its value is updated with what the user passed in.
        // Otherwise, the service throws an exception because the current configuration value was updated, and we have an
        // old value locally.
        // If no ETag value was passed in, then the value is always added or updated.
        return this.service.setKey(this.getEndpoint(),
            setting.getKey(),
            setting.getLabel(),
            this.getApiVersion(),
            setting,
            ifMatchETag,
            null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE)
                .addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true));
    }

    public Mono<Response<ConfigurationSetting>> getConfigurationSettingWithResponseAsync(ConfigurationSetting setting,
                                                                                         OffsetDateTime acceptDateTime,
                                                                                         boolean onlyIfChanged,
                                                                                         Context context) {
        // Validate that setting and key is not null. The key is used in the service URL, so it cannot be null.
        validateSetting(setting);
        context = context == null ? Context.NONE : context;

        final String ifNoneMatchETag = onlyIfChanged ? getETagValue(setting.getETag()) : null;
        return this.service.getKeyValueAsync(this.getEndpoint(),
                setting.getKey(),
                setting.getLabel(),
                this.getApiVersion(),
                null,
                acceptDateTime == null ? null : acceptDateTime.toString(),
                null,
                ifNoneMatchETag,
                context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
            .onErrorResume(HttpResponseException.class,
                (Function<Throwable, Mono<Response<ConfigurationSetting>>>) throwable -> {
                    final HttpResponseException e = (HttpResponseException) throwable;
                    final HttpResponse httpResponse = e.getResponse();
                    if (httpResponse.getStatusCode() == 304) {
                        return Mono.just(new ResponseBase<Void, ConfigurationSetting>(httpResponse.getRequest(),
                            httpResponse.getStatusCode(), httpResponse.getHeaders(), null, null));
                    }
                    return Mono.error(throwable);
                })
            .doOnSubscribe(ignoredValue -> logger.verbose("Retrieving ConfigurationSetting - {}", setting))
            .doOnSuccess(response -> logger.verbose("Retrieved ConfigurationSetting - {}", response.getValue()))
            .doOnError(error -> logger.warning("Failed to get ConfigurationSetting - {}", setting, error));
    }

    public Response<ConfigurationSetting> getConfigurationSettingWithResponse(ConfigurationSetting setting,
                                                                              OffsetDateTime acceptDateTime,
                                                                              boolean onlyIfChanged, Context context) {
        // Validate that setting and key is not null. The key is used in the service URL, so it cannot be null.
        validateSetting(setting);
        context = context == null ? Context.NONE : context;

        final String ifNoneMatchETag = onlyIfChanged ? getETagValue(setting.getETag()) : null;
        try {
            return this.service.getKeyValue(this.getEndpoint(),
                setting.getKey(),
                setting.getLabel(),
                this.getApiVersion(),
                null,
                acceptDateTime == null ? null : acceptDateTime.toString(),
                null,
                ifNoneMatchETag,
                context.addData(AZ_TRACING_NAMESPACE_KEY,
                        APP_CONFIG_TRACING_NAMESPACE_VALUE)
                    .addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true));
        } catch (HttpResponseException ex) {
            final HttpResponse httpResponse = ex.getResponse();
            if (httpResponse.getStatusCode() == 304) {
                return new ResponseBase<Void, ConfigurationSetting>(httpResponse.getRequest(),
                    httpResponse.getStatusCode(), httpResponse.getHeaders(), null, null);
            }
            throw logger.logExceptionAsError(ex);
        }
    }

    public Mono<Response<ConfigurationSetting>> deleteConfigurationSettingWithResponseAsync(
        ConfigurationSetting setting, boolean ifUnchanged,
        Context context) {
        // Validate that setting and key is not null. The key is used in the service URL, so it cannot be null.
        validateSetting(setting);
        context = context == null ? Context.NONE : context;

        final String ifMatchETag = ifUnchanged ? getETagValue(setting.getETag()) : null;
        return this.service.deleteAsync(this.getEndpoint(),
                setting.getKey(),
                setting.getLabel(),
                this.getApiVersion(),
                ifMatchETag,
                null,
                context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.verbose("Deleting ConfigurationSetting - {}", setting))
            .doOnSuccess(response -> logger.verbose("Deleted ConfigurationSetting - {}", response.getValue()))
            .doOnError(error -> logger.warning("Failed to delete ConfigurationSetting - {}", setting, error));
    }

    public Response<ConfigurationSetting> deleteConfigurationSettingWithResponse(ConfigurationSetting setting,
                                                                                 boolean ifUnchanged, Context context) {
        // Validate that setting and key is not null. The key is used in the service URL, so it cannot be null.
        validateSetting(setting);
        context = context == null ? Context.NONE : context;

        final String ifMatchETag = ifUnchanged ? getETagValue(setting.getETag()) : null;
        return this.service.delete(
            this.getEndpoint(),
            setting.getKey(),
            setting.getLabel(),
            this.getApiVersion(),
            ifMatchETag,
            null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE)
                .addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true));
    }

    public Mono<Response<ConfigurationSetting>> setReadOnlyWithResponseAsync(ConfigurationSetting setting,
                                                                             boolean isReadOnly,
                                                                             Context context) {
        // Validate that setting and key is not null. The key is used in the service URL, so it cannot be null.
        validateSetting(setting);
        context = context == null ? Context.NONE : context;
        if (isReadOnly) {
            return this.service.lockKeyValueAsync(this.getEndpoint(),
                    setting.getKey(),
                    setting.getLabel(),
                    this.getApiVersion(),
                    null,
                    null,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
                .doOnSubscribe(ignoredValue -> logger.verbose("Setting read only ConfigurationSetting - {}", setting))
                .doOnSuccess(response -> logger.verbose("Set read only ConfigurationSetting - {}", response.getValue()))
                .doOnError(error -> logger.warning("Failed to set read only ConfigurationSetting - {}", setting,
                    error));
        } else {
            return this.service.unlockKeyValueAsync(this.getEndpoint(),
                    setting.getKey(),
                    setting.getLabel(),
                    this.getApiVersion(),
                    null,
                    null,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
                .doOnSubscribe(ignoredValue -> logger.verbose("Clearing read only ConfigurationSetting - {}", setting))
                .doOnSuccess(
                    response -> logger.verbose("Cleared read only ConfigurationSetting - {}", response.getValue()))
                .doOnError(
                    error -> logger.warning("Failed to clear read only ConfigurationSetting - {}", setting, error));
        }
    }

    public Response<ConfigurationSetting> setReadOnlyWithResponse(ConfigurationSetting setting, boolean isReadOnly,
                                                                  Context context) {
        // Validate that setting and key is not null. The key is used in the service URL, so it cannot be null.
        validateSetting(setting);
        context = context == null ? Context.NONE : context;
        if (isReadOnly) {
            return this.service.lockKeyValue(this.getEndpoint(),
                setting.getKey(),
                setting.getLabel(),
                this.getApiVersion(),
                null,
                null,
                context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE)
                    .addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true));
        } else {
            return this.service.unlockKeyValue(this.getEndpoint(), setting.getKey(), setting.getLabel(),
                this.getApiVersion(), null, null,
                context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE)
                    .addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true));
        }
    }

    public PagedFlux<ConfigurationSetting> listConfigurationSettingsAsync(SettingSelector selector) {
        try {
            return new PagedFlux<>(() ->
                withContext(context ->
                    listConfigurationSettingsSinglePageAsync(selector, context)),
                continuationToken ->
                    withContext(context -> listConfigurationSettingsNextPageAsync(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    public PagedIterable<ConfigurationSetting> listConfigurationSettings(SettingSelector selector, Context context) {
        return new PagedIterable<ConfigurationSetting>(() ->
            listConfigurationSettingsSinglePage(selector, context),
            continuationToken -> listConfigurationSettingsNextPage(continuationToken, context));
    }

    public PagedFlux<ConfigurationSetting> listRevisionsAsync(SettingSelector selector) {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> listRevisionsFirstPageAsync(selector, context)),
                continuationToken -> withContext(context -> listRevisionsNextPageAsync(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    public PagedIterable<ConfigurationSetting> listRevisions(SettingSelector selector, Context context) {
        return new PagedIterable<ConfigurationSetting>(() ->
            listRevisionsFirstPage(selector, context),
            continuationToken -> listRevisionsNextPage(continuationToken, context));
    }

    /*
     * Ensure that setting is not null. And, key cannot be null because it is part of the service REST URL.
     */
    private void validateSetting(ConfigurationSetting setting) {
        Objects.requireNonNull(setting);

        if (setting.getKey() == null) {
            throw new IllegalArgumentException("Parameter 'key' is required and cannot be null.");
        }
    }

    /*
     * Azure Configuration service requires that the ETag value is surrounded in quotation marks.
     *
     * @param ETag The ETag to get the value for. If null is pass in, an empty string is returned.
     * @return The ETag surrounded by quotations. (ex. "ETag")
     */
    private String getETagValue(String etag) {
        return (etag == null || "*".equals(etag)) ? etag : "\"" + etag + "\"";
    }

    private Flux<ConfigurationSetting> listConfigurationSettingsAsync(String nextPageLink, Context context) {
        Mono<PagedResponse<ConfigurationSetting>> result
            = this.service.listKeyValuesAsync(this.getEndpoint(),
                nextPageLink,
                context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {}", nextPageLink))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                error));

        return result.flatMapMany(r -> extractAndFetchConfigurationSettings(r, context));
    }

    private Publisher<ConfigurationSetting> extractAndFetchConfigurationSettings(
        PagedResponse<ConfigurationSetting> page, Context context) {
        return CoreUtils.extractAndFetch(page, context, this::listConfigurationSettingsAsync);
    }

    private Mono<PagedResponse<ConfigurationSetting>> listConfigurationSettingsSinglePageAsync(SettingSelector selector,
                                                                                               Context context) {
        try {
            if (selector == null) {
                return this.service.listKeyValuesAsync(this.getEndpoint(),
                        null,
                        null,
                        this.getApiVersion(),
                        null,
                        null,
                        context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
                    .doOnRequest(ignoredValue -> logger.verbose("Listing all ConfigurationSettings"))
                    .doOnSuccess(response -> logger.verbose("Listed all ConfigurationSettings"))
                    .doOnError(error -> logger.warning("Failed to list all ConfigurationSetting", error));
            }

            final String fields = CoreUtils.arrayToString(selector.getFields(), SettingFields::toStringMapper);
            final String keyFilter = selector.getKeyFilter();
            final String labelFilter = selector.getLabelFilter();

            return this.service.listKeyValuesAsync(this.getEndpoint(),
                    keyFilter,
                    labelFilter,
                    this.getApiVersion(),
                    fields,
                    selector.getAcceptDateTime(),
                    context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
                .doOnSubscribe(ignoredValue -> logger.verbose("Listing ConfigurationSettings - {}", selector))
                .doOnSuccess(response -> logger.verbose("Listed ConfigurationSettings - {}", selector))
                .doOnError(error -> logger.warning("Failed to list ConfigurationSetting - {}", selector, error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private Mono<PagedResponse<ConfigurationSetting>> listConfigurationSettingsNextPageAsync(String continuationToken,
                                                                                             Context context) {
        try {
            if (continuationToken == null || continuationToken.isEmpty()) {
                return Mono.empty();
            }

            return this.service.listKeyValuesAsync(this.getEndpoint(),
                    continuationToken,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
                .doOnSubscribe(
                    ignoredValue -> logger.verbose("Retrieving the next listing page - Page {}", continuationToken))
                .doOnSuccess(response -> logger.verbose("Retrieved the next listing page - Page {}", continuationToken))
                .doOnError(
                    error -> logger.warning("Failed to retrieve the next listing page - Page {}", continuationToken,
                        error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private PagedResponse<ConfigurationSetting> listConfigurationSettingsSinglePage(SettingSelector selector,
                                                                                    Context context) {
        if (selector == null) {
            return this.service.listKeyValues(this.getEndpoint(),
                    null,
                    null,
                    this.getApiVersion(),
                    null,
                    null,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE));
        }

        final String fields = CoreUtils.arrayToString(selector.getFields(), SettingFields::toStringMapper);
        final String keyFilter = selector.getKeyFilter();
        final String labelFilter = selector.getLabelFilter();

        return this.service.listKeyValues(this.getEndpoint(),
                keyFilter,
                labelFilter,
                this.getApiVersion(),
                fields,
                selector.getAcceptDateTime(),
                context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE));
    }

    private PagedResponse<ConfigurationSetting> listConfigurationSettingsNextPage(String continuationToken,
            Context context) {

        if (continuationToken == null || continuationToken.isEmpty()) {
            return null;
        }

        return this.service.listKeyValues(this.getEndpoint(), continuationToken,
            context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE));

    }

    private Mono<PagedResponse<ConfigurationSetting>> listRevisionsFirstPageAsync(SettingSelector selector,
                                                                                  Context context) {
        try {
            Mono<PagedResponse<ConfigurationSetting>> result;

            if (selector != null) {
                final String fields = CoreUtils.arrayToString(selector.getFields(), SettingFields::toStringMapper);
                final String keyFilter = selector.getKeyFilter();
                final String labelFilter = selector.getLabelFilter();

                result = this.service.listKeyValueRevisionsAsync(this.getEndpoint(),
                        keyFilter,
                        labelFilter,
                        this.getApiVersion(),
                        fields,
                        selector.getAcceptDateTime(),
                        null,
                        context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
                    .doOnRequest(ignoredValue -> logger.verbose("Listing ConfigurationSetting revisions - {}",
                        selector))
                    .doOnSuccess(response -> logger.verbose("Listed ConfigurationSetting revisions - {}", selector))
                    .doOnError(error ->
                        logger.warning("Failed to list ConfigurationSetting revisions - {}", selector, error));
            } else {
                result = this.service.listKeyValueRevisionsAsync(this.getEndpoint(),
                        null,
                        null,
                        this.getApiVersion(),
                        null,
                        null,
                        null,
                        context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
                    .doOnRequest(ignoredValue -> logger.verbose("Listing ConfigurationSetting revisions"))
                    .doOnSuccess(response -> logger.verbose("Listed ConfigurationSetting revisions"))
                    .doOnError(error -> logger.warning("Failed to list all ConfigurationSetting revisions", error));
            }

            return result;
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
    private Mono<PagedResponse<ConfigurationSetting>> listRevisionsNextPageAsync(String nextPageLink, Context context) {
        try {
            return this.service
                .listKeyValuesAsync(this.getEndpoint(),
                    nextPageLink,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
                .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
                .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {}", nextPageLink))
                .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                    error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private PagedResponse<ConfigurationSetting> listRevisionsFirstPage(SettingSelector selector,
                                                                       Context context) {
        PagedResponse<ConfigurationSetting> result;

        if (selector != null) {
            final String fields = CoreUtils.arrayToString(selector.getFields(), SettingFields::toStringMapper);
            final String keyFilter = selector.getKeyFilter();
            final String labelFilter = selector.getLabelFilter();

            result = this.service.listKeyValueRevisions(this.getEndpoint(),
                    keyFilter,
                    labelFilter,
                    this.getApiVersion(),
                    fields,
                    selector.getAcceptDateTime(),
                    null,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE));
        } else {
            result = this.service.listKeyValueRevisions(this.getEndpoint(),
                    null,
                    null,
                    this.getApiVersion(),
                    null,
                    null,
                    null,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE));
        }
        return result;
    }

    private PagedResponse<ConfigurationSetting> listRevisionsNextPage(String nextPageLink, Context context) {
        return this.service
            .listKeyValues(this.getEndpoint(),
                nextPageLink,
                context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE));

    }
}
