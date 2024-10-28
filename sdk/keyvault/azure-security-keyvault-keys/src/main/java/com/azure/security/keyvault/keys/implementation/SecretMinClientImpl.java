// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.implementation;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.annotation.UnexpectedResponseExceptionType;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.security.keyvault.keys.implementation.models.SecretKey;
import com.azure.security.keyvault.keys.implementation.models.SecretRequestAttributes;
import com.azure.security.keyvault.keys.implementation.models.SecretRequestParameters;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Initializes a new instance of a minimal Key Vault Secret client.
 */
public final class SecretMinClientImpl {
    /** The proxy service used to perform REST calls. */
    private final SecretMinClientService service;

    /** Api Version. */
    private final String apiVersion;

    /**
     * Gets Api Version.
     *
     * @return the apiVersion value.
     */
    public String getApiVersion() {
        return this.apiVersion;
    }

    /** The HTTP pipeline to send requests through. */
    private final HttpPipeline httpPipeline;

    /**
     * Gets The HTTP pipeline to send requests through.
     *
     * @return the httpPipeline value.
     */
    public HttpPipeline getHttpPipeline() {
        return this.httpPipeline;
    }

    /** The serializer to serialize an object into a string. */
    private final SerializerAdapter serializerAdapter;

    /**
     * Gets The serializer to serialize an object into a string.
     *
     * @return the serializerAdapter value.
     */
    public SerializerAdapter getSerializerAdapter() {
        return this.serializerAdapter;
    }

    /**
     * Initializes an instance of KeyClient client.
     *
     * @param httpPipeline The HTTP pipeline to send requests through.
     * @param apiVersion Api Version.
     */
    public SecretMinClientImpl(HttpPipeline httpPipeline, String apiVersion) {
        this.httpPipeline = httpPipeline;
        this.serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        this.apiVersion = apiVersion;
        this.service = RestProxy.create(SecretMinClientService.class, this.httpPipeline, this.getSerializerAdapter());
    }

    /**
     * The interface defining a minimal set of services for SecretMinClient to be used by the proxy service to perform
     * REST calls.
     */
    @Host("{vaultBaseUrl}")
    @ServiceInterface(name = "SecretMinClient")
    public interface SecretMinClientService {
        @Get("secrets/{secret-name}/{secret-version}")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(code = { 404 }, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(code = { 403 }, value = ResourceModifiedException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<SecretKey>> getSecret(@HostParam("vaultBaseUrl") String vaultBaseUrl,
            @PathParam("secret-name") String secretName, @PathParam("secret-version") String secretVersion,
            @QueryParam("api-version") String apiVersion, @HeaderParam("Accept") String accept, Context context);

        @Get("secrets/{secret-name}/{secret-version}")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(code = { 404 }, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(code = { 403 }, value = ResourceModifiedException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<SecretKey> getSecretSync(@HostParam("vaultBaseUrl") String vaultBaseUrl,
            @PathParam("secret-name") String secretName, @PathParam("secret-version") String secretVersion,
            @QueryParam("api-version") String apiVersion, @HeaderParam("Accept") String accept, Context context);

        @Put("secrets/{secret-name}")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(code = { 400 }, value = ResourceModifiedException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<SecretKey>> setSecret(@HostParam("vaultBaseUrl") String vaultBaseUrl,
            @PathParam("secret-name") String secretName, @QueryParam("api-version") String apiVersion,
            @BodyParam("application/json") SecretRequestParameters parameters, @HeaderParam("Accept") String accept,
            @HeaderParam("Content-Type") String contentType, Context context);

        @Put("secrets/{secret-name}")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(code = { 400 }, value = ResourceModifiedException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<SecretKey> setSecretSync(@HostParam("vaultBaseUrl") String vaultBaseUrl,
            @PathParam("secret-name") String secretName, @QueryParam("api-version") String apiVersion,
            @BodyParam("application/json") SecretRequestParameters parameters, @HeaderParam("Accept") String accept,
            @HeaderParam("Content-Type") String contentType, Context context);
    }

    /**
     * Get a specified secret from a given key vault.
     *
     * <p>The GET operation is applicable to any secret stored in Azure Key Vault. This operation requires the
     * secrets/get permission.
     *
     * @param vaultBaseUrl The vault name, for example https://myvault.vault.azure.net.
     * @param secretName The name of the secret.
     * @param secretVersion The version of the secret. This URI fragment is optional. If not specified, the latest
     *     version of the secret is returned.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a secret consisting of a value, id and its attributes along with {@link Response} on successful
     *     completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SecretKey>> getSecretWithResponseAsync(String vaultBaseUrl, String secretName,
        String secretVersion, Context context) {
        final String accept = "application/json";
        return service.getSecret(vaultBaseUrl, secretName, secretVersion, this.getApiVersion(), accept, context);
    }

    /**
     * Get a specified secret from a given key vault.
     *
     * <p>The GET operation is applicable to any secret stored in Azure Key Vault. This operation requires the
     * secrets/get permission.
     *
     * @param vaultBaseUrl The vault name, for example https://myvault.vault.azure.net.
     * @param secretName The name of the secret.
     * @param secretVersion The version of the secret. This URI fragment is optional. If not specified, the latest
     *     version of the secret is returned.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a secret consisting of a value, id and its attributes along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SecretKey> getSecretWithResponse(String vaultBaseUrl, String secretName, String secretVersion,
        Context context) {
        final String accept = "application/json";
        return service.getSecretSync(vaultBaseUrl, secretName, secretVersion, this.getApiVersion(), accept, context);
    }

    /**
     * Sets a secret in a specified key vault.
     *
     * <p>The SET operation adds a secret to the Azure Key Vault. If the named secret already exists, Azure Key Vault
     * creates a new version of that secret. This operation requires the secrets/set permission.
     *
     * @param vaultBaseUrl The vault name, for example https://myvault.vault.azure.net.
     * @param secretName The name of the secret. The value you provide may be copied globally for the purpose of running
     *     the service. The value provided should not include personally identifiable or sensitive information.
     * @param value The value of the secret.
     * @param tags Application specific metadata in the form of key-value pairs.
     * @param contentType Type of the secret value such as a password.
     * @param secretAttributes The secret management attributes.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a secret consisting of a value, id and its attributes along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SecretKey> setSecretWithResponse(String vaultBaseUrl, String secretName, String value,
        Map<String, String> tags, String contentType, SecretRequestAttributes secretAttributes, Context context) {
        final String accept = "application/json";
        SecretRequestParameters parameters = new SecretRequestParameters();
        parameters.setValue(value);
        parameters.setTags(tags);
        parameters.setContentType(contentType);
        parameters.setSecretAttributes(secretAttributes);
        return service.setSecretSync(vaultBaseUrl, secretName, this.getApiVersion(), parameters, accept, contentType,
            context);
    }

    /**
     * Sets a secret in a specified key vault.
     *
     * <p>The SET operation adds a secret to the Azure Key Vault. If the named secret already exists, Azure Key Vault
     * creates a new version of that secret. This operation requires the secrets/set permission.
     *
     * @param vaultBaseUrl The vault name, for example https://myvault.vault.azure.net.
     * @param secretName The name of the secret. The value you provide may be copied globally for the purpose of running
     *     the service. The value provided should not include personally identifiable or sensitive information.
     * @param value The value of the secret.
     * @param tags Application specific metadata in the form of key-value pairs.
     * @param contentType Type of the secret value such as a password.
     * @param secretAttributes The secret management attributes.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a secret consisting of a value, id and its attributes along with {@link Response} on successful
     *     completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SecretKey>> setSecretWithResponseAsync(String vaultBaseUrl, String secretName, String value,
        Map<String, String> tags, String contentType, SecretRequestAttributes secretAttributes, Context context) {
        final String accept = "application/json";
        SecretRequestParameters parameters = new SecretRequestParameters();
        parameters.setValue(value);
        parameters.setTags(tags);
        parameters.setContentType(contentType);
        parameters.setSecretAttributes(secretAttributes);
        return service.setSecret(vaultBaseUrl, secretName, this.getApiVersion(), parameters, accept, contentType,
            context);
    }
}
