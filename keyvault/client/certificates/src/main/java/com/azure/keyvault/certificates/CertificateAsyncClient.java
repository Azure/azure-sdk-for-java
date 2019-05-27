package com.azure.keyvault.certificates;

import com.azure.core.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.RestProxy;
import com.azure.keyvault.certificates.models.Certificate;
import com.azure.keyvault.certificates.models.CertificateBase;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.Objects;

public class CertificateAsyncClient extends ServiceClient {
    static final String API_VERSION = "7.0";
    static final String ACCEPT_LANGUAGE = "en-US";
    static final int DEFAULT_MAX_PAGE_RESULTS = 25;
    static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    private String endpoint;
    private final CertificateService service;

    /**
     * Creates a SecretAsyncClient that uses {@code pipeline} to service requests
     *
     * @param endpoint URL for the Azure KeyVault service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     */
    CertificateAsyncClient(URL endpoint, HttpPipeline pipeline) {
        super(pipeline);
        Objects.requireNonNull(endpoint, KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));
        this.endpoint = endpoint.toString();
        this.service = RestProxy.create(CertificateService.class, this);
    }

    /**
     * Creates a builder that can configure options for the SecretAsyncClient before creating an instance of it.
     * @return A new builder to create a SecretAsyncClient from.
     */
    public static CertificateAsyncClientBuilder builder() {
        return new CertificateAsyncClientBuilder();
    }


    public Mono<Response<CertificateOperation>> createCertificate(Certificate certificate) {
        CertificateRequestParameters certificateRequestParameters = new CertificateRequestParameters()
                .withCertificateAttributes(new CertificateRequestAttributes(certificate))
                .withCertificatePolicy(new CertificatePolicy(certificate))
                .withTags(certificate.tags());
        return service.createCertificate(endpoint, certificate.name(), API_VERSION, ACCEPT_LANGUAGE, certificateRequestParameters, CONTENT_TYPE_HEADER_VALUE);
    }

    public Mono<Response<Certificate>> getCertificate(String name) {
        return service.getCertificate(endpoint, name, "", API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
    }

    public Mono<Response<Certificate>> updateCertificateProperties(CertificateBase certificateBase) {
        CertificatePolicy policy = new CertificatePolicy(certificateBase);
        return service.updateCertificateProperties(endpoint,certificateBase.name(),API_VERSION,ACCEPT_LANGUAGE,policy,CONTENT_TYPE_HEADER_VALUE);
    }



}
