package com.azure.keyvault.certificates;

import com.azure.core.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.implementation.RestProxy;
import com.azure.keyvault.certificates.models.*;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class CertificateClient extends ServiceClient {
    static final String API_VERSION = "7.0";
    static final String ACCEPT_LANGUAGE = "en-US";
    static final int DEFAULT_MAX_PAGE_RESULTS = 25;
    static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    private String endpoint;
    private final CertificateService service;

    /**
     * Creates a CertificateClient that uses {@code pipeline} to service requests
     *
     * @param endpoint URL for the Azure KeyVault service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     */
    CertificateClient(URL endpoint, HttpPipeline pipeline) {
        super(pipeline);
        Objects.requireNonNull(endpoint, KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));
        this.endpoint = endpoint.toString();
        this.service = RestProxy.create(CertificateService.class, this);
    }

    /**
     * Creates a builder that can configure options for the CertificateAsyncClient before creating an instance of it.
     * @return A new builder to create a CertificateAsyncClient from.
     */
    public static CertificateClientBuilder builder() {
        return new CertificateClientBuilder();
    }


    public Response<CertificateOperation> createCertificate(Certificate certificate) {
        CertificateRequestParameters certificateRequestParameters = new CertificateRequestParameters()
                .withCertificateAttributes(new CertificateRequestAttributes(certificate))
                .withCertificatePolicy(new CertificatePolicyRequest(certificate.certificatePolicy()))
                .withTags(certificate.tags());
        return service.createCertificate(endpoint, certificate.name(), API_VERSION, ACCEPT_LANGUAGE, certificateRequestParameters, CONTENT_TYPE_HEADER_VALUE).block();
    }

    public Response<Certificate> getCertificate(String name) {
        return service.getCertificate(endpoint, name, "", API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).block();
    }

    public Response<Certificate> updateCertificate(CertificateBase certificateBase) {
        return  null;
    }


    public Response<Certificate> getSecret(String name, String version) {
        String certificateVersion = "";
        if (version != null) {
            certificateVersion = version;
        }
        return  null;
    }


    public Response<Certificate> getCertificate(CertificateBase certificateBase) {
        Objects.requireNonNull(certificateBase, "The Secret Base parameter cannot be null.");
        return  null;
    }

    public Response<Certificate> getCertifciate(String name) {
        //return getCertificate(name, "");
        return  null;
    }


    public Response<CertificatePolicy> updateCertificatePolicy(CertificatePolicy certificate) {
        Objects.requireNonNull(certificate, "The certificate input parameter cannot be null.");
        return  null;
    }


    public Response<DeletedCertificate> deleteCertificate(String name) {
        return  null;
    }


    public Response<DeletedCertificate> getDeletedCertificate(String name) {
        return  null;
    }


    public VoidResponse purgeDeletedCertificate(String name) {
        return  null;
    }


    public Response<Certificate> recoverDeletedCertificate(String name) {
        return  null;
    }


    public Response<byte[]> backupCertificate(String name) {
        return  null;
    }


    public Response<Certificate> restoreCertificate(byte[] backup) {
        return  null;
    }


    public List<CertificateBase> listSecrets() {
        return  null;
    }


    public List<DeletedCertificate> listDeletedSecrets() {
        return  null;
    }


    public List<CertificateBase> listSecretVersions(String name) {
        return  null;
    }


    private Flux<CertificateBase> listSecretsNext(String nextPageLink) {
        return  null;
    }

    private Publisher<CertificateBase> extractAndFetchSecrets(PagedResponse<CertificateBase> page) {
        return extractAndFetch(page, this::listSecretsNext);
    }


    private Flux<DeletedCertificate> listDeletedSecretsNext(String nextPageLink) {
        return  null;
    }

    private Publisher<DeletedCertificate> extractAndFetchDeletedSecrets(PagedResponse<DeletedCertificate> page) {
        return extractAndFetch(page, this::listDeletedSecretsNext);
    }

    public Response<byte[]> getPendingCertificateSigningRequest(String certificateName) {
        return null;
    }
    public Response<String> mergeCertificate(String name, List<byte[]> x509Certificates) {
        return null;
    }
    public Response<Certificate> mergeCertificate(MergeCertificateConfig mergeCertificateConfig){
        return null;
    }

    // Certificate Policy
    public Response<CertificateBase> getCertificatePolicy(String certificateName) {
        return null;
    }
    public Response<CertificateBase> updateCertificatePolicy(String certificateName, CertificateBase certificate) {
        return null;
    }



    // Certificate Issuer methods
    public Response<Issuer> createCertificateIssuer(String name, String provider) {
        return null;
    }
    public Response<Issuer> createCertificateIssuer(Issuer issuer) {
        return null;
    }

    public Response<Issuer> getCertificateIssuer(String name){
        return null;
    }

    public Response<Issuer> deleteCertificateIssuer(String name){
        return null;
    }

    public List<IssuerBase> listCertificateIssuers(){
        return null;
    }

    public Response<Issuer> updateIssuer(Issuer issuer){
        return null;
    }

    // Certificate Contacts methods
    public List<Contact> setCertificateContacts(List<Contact> contacts) {
        return null;
    }

    public List<Contact> listCertificateContacts() {
        return null;
    }

    public List<Contact> deleteCertificateContacts() {
        return null;
    }

    // Certificate Operation methods
    public Response<CertificateOperation> getCertificateOperation(String certificateName) {
        return null;
    }

    public Response<CertificateOperation> deleteCertificateOperation(String certificateName) {
        return null;
    }

    public Response<CertificateOperation> updateCertificateOperation(String certificateName, boolean cancellationRequested) {
        return null;
    }


    //TODO: Extract this in azure-core ImplUtils and use from there
    private <T> Publisher<T> extractAndFetch(PagedResponse<T> page, Function<String, Publisher<T>> content) {
        String nextPageLink = page.nextLink();
        if (nextPageLink == null) {
            return Flux.fromIterable(page.items());
        }
        return Flux.fromIterable(page.items()).concatWith(content.apply(nextPageLink));
    }
}
