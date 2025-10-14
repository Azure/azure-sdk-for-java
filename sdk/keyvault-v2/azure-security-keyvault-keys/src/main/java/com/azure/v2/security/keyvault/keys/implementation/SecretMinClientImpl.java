// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.keys.implementation;

import com.azure.v2.security.keyvault.keys.implementation.models.KeyVaultError;
import com.azure.v2.security.keyvault.keys.implementation.models.SecretBundle;
import com.azure.v2.security.keyvault.keys.implementation.models.SecretSetParameters;
import io.clientcore.core.annotations.ReturnType;
import io.clientcore.core.annotations.ServiceInterface;
import io.clientcore.core.annotations.ServiceMethod;
import io.clientcore.core.http.annotations.BodyParam;
import io.clientcore.core.http.annotations.HeaderParam;
import io.clientcore.core.http.annotations.HostParam;
import io.clientcore.core.http.annotations.HttpRequestInformation;
import io.clientcore.core.http.annotations.PathParam;
import io.clientcore.core.http.annotations.QueryParam;
import io.clientcore.core.http.annotations.UnexpectedResponseExceptionDetail;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;

import java.lang.reflect.InvocationTargetException;

/**
 * Initializes a new instance of a minimal Key Vault Secret client.
 */
public final class SecretMinClientImpl {
    /** The proxy service used to perform REST calls. */
    private final SecretMinClientService service;

    /**
     */
    private final String endpoint;

    /**
     * Gets.
     *
     * @return the vaultBaseUrl value.
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /**
     * Service version.
     */
    private final String serviceVersion;

    /**
     * Gets Service version.
     *
     * @return the serviceVersion value.
     */
    public String getServiceVersion() {
        return this.serviceVersion;
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
     * Initializes an instance of SecretClient client.
     *
     * @param httpPipeline The HTTP pipeline to send requests through.
     * @param endpoint The endpoint of the key vault or managed HSM service.
     * @param serviceVersion The Azure Key Vault or Managed HSM service version.
     */
    public SecretMinClientImpl(HttpPipeline httpPipeline, String endpoint, String serviceVersion) {
        this.httpPipeline = httpPipeline;
        this.endpoint = endpoint;
        this.serviceVersion = serviceVersion;
        this.service = SecretMinClientService.getNewInstance(this.httpPipeline);
    }

    /**
     * The interface defining a minimal set of services for SecretMinClient to be used by the proxy service to perform
     * REST calls.
     */
    @ServiceInterface(name = "SecretClientMin", host = "{vaultBaseUrl}")
    public interface SecretMinClientService {
        static SecretMinClientService getNewInstance(HttpPipeline pipeline) {
            try {
                Class<?> clazz
                    = Class.forName("com.azure.v2.security.keyvault.keys.implementation.SecretMinClientServiceImpl");

                return (SecretMinClientService) clazz.getMethod("getNewInstance", HttpPipeline.class)
                    .invoke(null, pipeline);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        @HttpRequestInformation(
            method = HttpMethod.GET,
            path = "/secrets/{secret-name}/{secret-version}",
            expectedStatusCodes = { 200 })
        @UnexpectedResponseExceptionDetail(exceptionBodyClass = KeyVaultError.class)
        Response<SecretBundle> getSecret(@HostParam("vaultBaseUrl") String vaultBaseUrl,
            @QueryParam("api-version") String apiVersion, @PathParam("secret-name") String secretName,
            @PathParam("secret-version") String secretVersion, @HeaderParam("Accept") String accept,
            RequestContext requestContext);

        @HttpRequestInformation(method = HttpMethod.PUT, path = "/secrets/{secret-name}", expectedStatusCodes = { 200 })
        @UnexpectedResponseExceptionDetail(exceptionBodyClass = KeyVaultError.class)
        Response<SecretBundle> setSecret(@HostParam("vaultBaseUrl") String vaultBaseUrl,
            @QueryParam("api-version") String apiVersion, @PathParam("secret-name") String secretName,
            @HeaderParam("Content-Type") String contentType, @HeaderParam("Accept") String accept,
            @BodyParam("application/json") SecretSetParameters parameters, RequestContext requestContext);
    }

    /**
     * Get a specified secret from a given key vault.
     *
     * The GET operation is applicable to any secret stored in Azure Key Vault. This operation requires the secrets/get
     * permission.
     *
     * @param secretName The name of the secret.
     * @param secretVersion The version of the secret. This URI fragment is optional. If not specified, the latest
     * version of the secret is returned.
     * @param requestContext The options to configure the HTTP request before HTTP client sends it.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the service returns an error.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a specified secret from a given key vault.
     *
     * The GET operation is applicable to any secret stored in Azure Key Vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SecretBundle> getSecretWithResponse(String secretName, String secretVersion,
        RequestContext requestContext) {

        final String accept = "application/json";

        return service.getSecret(this.getEndpoint(), this.getServiceVersion(), secretName, secretVersion, accept,
            requestContext);
    }

    /**
     * Sets a secret in a specified key vault.
     *
     * The SET operation adds a secret to the Azure Key Vault. If the named secret already exists, Azure Key Vault
     * creates a new version of that secret. This operation requires the secrets/set permission.
     *
     * @param secretName The name of the secret. The value you provide may be copied globally for the purpose of running
     * the service. The value provided should not include personally identifiable or sensitive information.
     * @param parameters The parameters for setting the secret.
     * @param requestContext The options to configure the HTTP request before HTTP client sends it.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the service returns an error.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a secret consisting of a value, id and its attributes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SecretBundle> setSecretWithResponse(String secretName, SecretSetParameters parameters,
        RequestContext requestContext) {

        final String contentType = "application/json";
        final String accept = "application/json";

        return service.setSecret(this.getEndpoint(), this.getServiceVersion(), secretName, contentType, accept,
            parameters, requestContext);
    }
}
