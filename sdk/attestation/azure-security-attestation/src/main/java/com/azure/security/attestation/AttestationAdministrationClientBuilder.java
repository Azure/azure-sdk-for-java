// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.security.attestation.implementation.AttestationClientImpl;
import com.azure.security.attestation.implementation.AttestationClientImplBuilder;
import com.azure.security.attestation.models.AttestationTokenValidationOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/** A builder for creating a new instance of the AttestationClient type. */
@ServiceClientBuilder(
        serviceClients = {
            AttestationAdministrationClient.class,
            AttestationAdministrationAsyncClient.class,
            PolicyCertificatesClient.class,
            PolicyCertificatesAsyncClient.class,
        })
public final class AttestationAdministrationClientBuilder {
    private static final String SDK_NAME = "name";

    private static final String SDK_VERSION = "version";

    private final String[] dataplaneScope = new String[] {"https://attest.azure.net/.default"};

    private final AttestationClientImplBuilder clientImplBuilder;
    private final ClientLogger logger = new ClientLogger(AttestationAdministrationClientBuilder.class);

    private AttestationServiceVersion serviceVersion;
    private AttestationTokenValidationOptions tokenValidationOptions;
    private TokenCredential tokenCredential = null;

    /**
     * Creates a new instance of the AttestationClientBuilder class.
     */
    public AttestationAdministrationClientBuilder() {

        clientImplBuilder = new AttestationClientImplBuilder();
        serviceVersion = AttestationServiceVersion.V2020_10_01;
        tokenValidationOptions = new AttestationTokenValidationOptions();
    }

    /*
     * The attestation instance base URI, for example
     * https://mytenant.attest.azure.net.
     */
    private String endpoint;

    /**
     * Sets The attestation endpoint URI, for example https://mytenant.attest.azure.net.
     *
     * @param endpoint The endpoint to connect to.
     * @return the AttestationClientBuilder.
     */
    public AttestationAdministrationClientBuilder endpoint(String endpoint) {
        Objects.requireNonNull(endpoint);
        try {
            new URL(endpoint);
        } catch (MalformedURLException ex) {
            logger.logExceptionAsError(new IllegalArgumentException(ex));
        }
        clientImplBuilder.instanceUrl(endpoint);
        return this;
    }

    /**
     * Sets the desired API version for this attestation client.
     * @param serviceVersion Specifies the API version to use in the outgoing API calls.
     * @return the AttestationClientBuilder.
     */
    public AttestationAdministrationClientBuilder serviceVersion(AttestationServiceVersion serviceVersion) {
        Objects.requireNonNull(serviceVersion);
        this.serviceVersion = serviceVersion;
        return this;
    }
    /**
     * Sets the credential to be used for communicating with the service.
     * @param credential Specifies the credential to be used for authentication.
     * @return the AttestationClientBuilder.
     */
    public AttestationAdministrationClientBuilder credential(TokenCredential credential) {
        Objects.requireNonNull(credential);
        this.tokenCredential = credential;
        return this;
    }

    /**
     * Sets The HTTP pipeline to send requests through.
     *
     * @param pipeline the pipeline value.
     * @return the AttestationClientBuilder.
     */
    public AttestationAdministrationClientBuilder pipeline(HttpPipeline pipeline) {
        clientImplBuilder.pipeline(pipeline);
        return this;
    }

    /**
     * Sets The serializer to serialize an object into a string.
     *
     * @param serializerAdapter the serializerAdapter value.
     * @return the AttestationClientBuilder.
     */
    public AttestationAdministrationClientBuilder serializerAdapter(SerializerAdapter serializerAdapter) {
        clientImplBuilder.serializerAdapter(serializerAdapter);
        return this;
    }

    /**
     * Sets The HTTP client used to send the request.
     *
     * @param httpClient the httpClient value.
     * @return the AttestationClientBuilder.
     */
    public AttestationAdministrationClientBuilder httpClient(HttpClient httpClient) {
        clientImplBuilder.httpClient(httpClient);
        return this;
    }

    /**
     * Sets The configuration store that is used during construction of the service client.
     *
     * @param configuration the configuration value.
     * @return the AttestationClientBuilder.
     */
    public AttestationAdministrationClientBuilder configuration(Configuration configuration) {
        clientImplBuilder.configuration(configuration);
        return this;
    }

    /**
     * Sets The logging configuration for HTTP requests and responses.
     *
     * @param httpLogOptions the httpLogOptions value.
     * @return the AttestationClientBuilder.
     */
    public AttestationAdministrationClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        clientImplBuilder.httpLogOptions(httpLogOptions);
        return this;
    }

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     *
     * @param retryPolicy the retryPolicy value.
     * @return the AttestationClientBuilder.
     */
    public AttestationAdministrationClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        clientImplBuilder.retryPolicy(retryPolicy);
        return this;
    }

    /**
     * Adds a custom Http pipeline policy.
     *
     * @param customPolicy The custom Http pipeline policy to add.
     * @return this {@link AttestationAdministrationClientBuilder}.
     */
    public AttestationAdministrationClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        clientImplBuilder.addPolicy(customPolicy);
        return this;
    }

    /**
     * Sets {@link com.azure.security.attestation.models.AttestationToken} validation options for clients created from this builder.
     * @param tokenValidationOptions - Validation options to use on APIs which interact with the attestation service.
     * @return this {@link AttestationAdministrationClientBuilder}
     */
    public AttestationAdministrationClientBuilder tokenValidationOptions(AttestationTokenValidationOptions tokenValidationOptions) {
        this.tokenValidationOptions = tokenValidationOptions;
        return this;
    }

    /**
     * Builds an instance of AttestationClient sync client.
     *
     * Instantiating a synchronous Attestation client:
     * <br>
     * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClientBuilder.buildClient -->
     * <pre>
     * AttestationAdministrationClient client = new AttestationAdministrationClientBuilder&#40;&#41;
     *     .endpoint&#40;endpoint&#41;
     *     .buildClient&#40;&#41;;
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationAdministrationClientBuilder.buildClient -->
     * @return an instance of {@link AttestationClient}.
     */
    public AttestationAdministrationClient buildClient() {
        return new AttestationAdministrationClient(buildAsyncClient());
    }

    /**
     * Builds an instance of AttestationAsyncClient async client.
     *
     * Instantiating a synchronous Attestation client:
     * <br>
     * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClientBuilder.buildAsyncClient -->
     * <pre>
     * AttestationAdministrationAsyncClient asyncClient = new AttestationAdministrationClientBuilder&#40;&#41;
     *     .endpoint&#40;endpoint&#41;
     *     .buildAsyncClient&#40;&#41;;
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationAdministrationClientBuilder.buildAsyncClient -->
     * @return an instance of {@link AttestationClient}.
     */
    public AttestationAdministrationAsyncClient buildAsyncClient() {
        return new AttestationAdministrationAsyncClient(buildInnerClient(), this.tokenValidationOptions);
    }

    /**
     * Legacy API surface which will be removed shortly.
     */


    /**
     * Builds an instance of AttestationClientImpl with the provided parameters.
     *
     * @return an instance of AttestationClientImpl.
     */
    private AttestationClientImpl buildInnerClient() {
        AttestationServiceVersion version = serviceVersion != null ? serviceVersion : AttestationServiceVersion.getLatest();
        clientImplBuilder.apiVersion(version.getVersion());
        if (tokenCredential != null) {
            clientImplBuilder.addPolicy(new BearerTokenAuthenticationPolicy(tokenCredential, dataplaneScope));
        }
        return clientImplBuilder.buildClient();
    }

    /**
     * Builds an instance of PolicyCertificatesAsyncClient async client.
     *
     * @return an instance of PolicyCertificatesAsyncClient.
     */
    public PolicyCertificatesAsyncClient buildPolicyCertificatesAsyncClient() {
        return new PolicyCertificatesAsyncClient(buildInnerClient().getPolicyCertificates());
    }

    /**
     * Builds an instance of PolicyCertificatesClient sync client.
     *
     * @return an instance of PolicyCertificatesClient.
     */
    public PolicyCertificatesClient buildPolicyCertificatesClient() {
        return new PolicyCertificatesClient(buildInnerClient().getPolicyCertificates());
    }

}
