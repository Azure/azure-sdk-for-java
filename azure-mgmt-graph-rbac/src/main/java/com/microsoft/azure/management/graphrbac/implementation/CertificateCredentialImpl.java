/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.google.common.io.BaseEncoding;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.CertificateCredential;
import com.microsoft.azure.management.graphrbac.CertificateType;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableRefreshableWrapperImpl;
import com.microsoft.rest.RestClient;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import rx.Observable;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Implementation for ServicePrincipal and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
class CertificateCredentialImpl<T>
        extends IndexableRefreshableWrapperImpl<CertificateCredential, KeyCredentialInner>
        implements
        CertificateCredential,
        CertificateCredential.Definition<T>,
        CertificateCredential.UpdateDefinition<T> {

    private String name;
    private HasCredential<?> parent;
    private OutputStream authFile;
    private String privateKeyPath;
    private String privateKeyPassword;

    CertificateCredentialImpl(KeyCredentialInner keyCredential) {
        super(keyCredential);
        this.name = new String(BaseEncoding.base64().decode(keyCredential.customKeyIdentifier()));
    }

    CertificateCredentialImpl(String name, HasCredential<?> parent) {
        super(new KeyCredentialInner()
                .withUsage("Verify")
                .withCustomKeyIdentifier(BaseEncoding.base64().encode(name.getBytes()))
                .withStartDate(DateTime.now())
                .withEndDate(DateTime.now().plusYears(1)));
        this.name = name;
        this.parent = parent;
    }

    @Override
    public Observable<CertificateCredential> refreshAsync() {
        throw new UnsupportedOperationException("Cannot refresh credentials.");
    }

    @Override
    protected Observable<KeyCredentialInner> getInnerAsync() {
        throw new UnsupportedOperationException("Cannot refresh credentials.");
    }

    @Override
    public String id() {
        return inner().keyId();
    }

    @Override
    public DateTime startDate() {
        return inner().startDate();
    }

    @Override
    public DateTime endDate() {
        return inner().endDate();
    }

    @Override
    public String value() {
        return inner().value();
    }


    @Override
    @SuppressWarnings("unchecked")
    public T attach() {
        parent.withCertificateCredential(this);
        return (T) parent;
    }

    @Override
    public CertificateCredentialImpl<T> withStartDate(DateTime startDate) {
        DateTime original = startDate();
        inner().withStartDate(startDate);
        // Adjust end time
        withDuration(Duration.millis(endDate().getMillis() - original.getMillis()));
        return this;
    }

    @Override
    public CertificateCredentialImpl<T> withDuration(Duration duration) {
        inner().withEndDate(startDate().plus(duration.getMillis()));
        return this;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public CertificateCredentialImpl<T> withAsymmetricX509Certificate() {
        inner().withType(CertificateType.ASYMMETRIC_X509_CERT.toString());
        return this;
    }

    @Override
    public CertificateCredentialImpl<T> withSymmetricEncryption() {
        inner().withType(CertificateType.SYMMETRIC.toString());
        return this;
    }

    @Override
    public CertificateCredentialImpl<T> withPublicKey(byte[] certificate) {
        inner().withValue(BaseEncoding.base64().encode(certificate));
        return this;
    }

    @Override
    public CertificateCredentialImpl<T> withSecretKey(byte[] secret) {
        inner().withValue(BaseEncoding.base64().encode(secret));
        return this;
    }

    void exportAuthFile(ServicePrincipalImpl servicePrincipal) {
        if (authFile == null) {
            return;
        }
        RestClient restClient = servicePrincipal.manager().roleInner().restClient();
        AzureEnvironment environment = null;
        if (restClient.credentials() instanceof AzureTokenCredentials) {
            environment = ((AzureTokenCredentials) restClient.credentials()).environment();
        } else {
            String baseUrl = restClient.retrofit().baseUrl().toString();
            for (AzureEnvironment env : AzureEnvironment.knownEnvironments()) {
                if (env.resourceManagerEndpoint().toLowerCase().contains(baseUrl.toLowerCase())) {
                    environment = env;
                }
            }
            if (environment == null) {
                throw new IllegalArgumentException("Unknown resource manager endpoint " + baseUrl);
            }
        }

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("client=%s", servicePrincipal.applicationId())).append("\n");
        builder.append(String.format("certificate=%s", normalizeAuthFilePath(privateKeyPath))).append("\n");
        builder.append(String.format("certificatePassword=%s", privateKeyPassword)).append("\n");
        builder.append(String.format("tenant=%s", servicePrincipal.manager().tenantId())).append("\n");
        builder.append(String.format("subscription=%s", servicePrincipal.assignedSubscription)).append("\n");
        builder.append(String.format("authURL=%s", normalizeAuthFileUrl(environment.activeDirectoryEndpoint()))).append("\n");
        builder.append(String.format("baseURL=%s", normalizeAuthFileUrl(environment.resourceManagerEndpoint()))).append("\n");
        builder.append(String.format("graphURL=%s", normalizeAuthFileUrl(environment.graphEndpoint()))).append("\n");
        builder.append(String.format("managementURI=%s", normalizeAuthFileUrl(environment.managementEndpoint())));
        try {
            authFile.write(builder.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String normalizeAuthFileUrl(String url) {
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        return url.replace("://", "\\://");
    }

    private String normalizeAuthFilePath(String path) {
        return path.replace("\\", "\\\\").replace(":", "\\:");
    }

    @Override
    public CertificateCredentialImpl<T> withAuthFileToExport(OutputStream outputStream) {
        this.authFile = outputStream;
        return this;
    }

    @Override
    public CertificateCredentialImpl<T> withPrivateKeyFile(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
        return this;
    }

    @Override
    public CertificateCredentialImpl<T> withPrivateKeyPassword(String privateKeyPassword) {
        this.privateKeyPassword = privateKeyPassword;
        return this;
    }
}
