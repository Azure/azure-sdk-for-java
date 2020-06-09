// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.models.AppServiceCertificate;
import com.azure.resourcemanager.appservice.models.AppServiceCertificateOrder;
import com.azure.resourcemanager.appservice.models.HostingEnvironmentProfile;
import com.azure.resourcemanager.appservice.fluent.inner.CertificateInner;
import com.azure.resourcemanager.appservice.fluent.CertificatesClient;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import reactor.core.publisher.Mono;

/** The implementation for AppServiceCertificate. */
class AppServiceCertificateImpl
    extends GroupableResourceImpl<AppServiceCertificate, CertificateInner, AppServiceCertificateImpl, AppServiceManager>
    implements AppServiceCertificate, AppServiceCertificate.Definition {

    private final ClientLogger logger = new ClientLogger(getClass());

    private String pfxFileUrl;
    private AppServiceCertificateOrder certificateOrder;

    AppServiceCertificateImpl(String name, CertificateInner innerObject, AppServiceManager manager) {
        super(name, innerObject, manager);
    }

    @Override
    public String friendlyName() {
        return inner().friendlyName();
    }

    @Override
    public String subjectName() {
        return inner().subjectName();
    }

    @Override
    public List<String> hostNames() {
        return Collections.unmodifiableList(inner().hostNames());
    }

    @Override
    public byte[] pfxBlob() {
        return inner().pfxBlob();
    }

    @Override
    public String siteName() {
        return inner().siteName();
    }

    @Override
    public String selfLink() {
        return inner().selfLink();
    }

    @Override
    public String issuer() {
        return inner().issuer();
    }

    @Override
    public OffsetDateTime issueDate() {
        return inner().issueDate();
    }

    @Override
    public OffsetDateTime expirationDate() {
        return inner().expirationDate();
    }

    @Override
    public String password() {
        return inner().password();
    }

    @Override
    public String thumbprint() {
        return inner().thumbprint();
    }

    @Override
    public Boolean valid() {
        return inner().valid();
    }

    @Override
    public byte[] certificateBlob() {
        return inner().cerBlob();
    }

    @Override
    public String publicKeyHash() {
        return inner().publicKeyHash();
    }

    @Override
    public HostingEnvironmentProfile hostingEnvironmentProfile() {
        return inner().hostingEnvironmentProfile();
    }

    @Override
    protected Mono<CertificateInner> getInnerAsync() {
        return this.manager().inner().getCertificates().getByResourceGroupAsync(resourceGroupName(), name());
    }

    @Override
    public Mono<AppServiceCertificate> createResourceAsync() {
        Mono<Void> pfxBytes = Mono.empty();
        if (pfxFileUrl != null) {
            pfxBytes =
                Utils
                    .downloadFileAsync(pfxFileUrl, this.manager().httpPipeline())
                    .map(
                        bytes -> {
                            inner().withPfxBlob(bytes);
                            return null;
                        });
        }
        Mono<Void> keyVaultBinding = Mono.empty();
        if (certificateOrder != null) {
            keyVaultBinding =
                certificateOrder
                    .getKeyVaultBindingAsync()
                    .map(
                        keyVaultBinding1 -> {
                            inner()
                                .withKeyVaultId(keyVaultBinding1.keyVaultId())
                                .withKeyVaultSecretName(keyVaultBinding1.keyVaultSecretName());
                            return null;
                        });
        }
        final CertificatesClient client = this.manager().inner().getCertificates();
        return pfxBytes
            .then(keyVaultBinding)
            .then(client.createOrUpdateAsync(resourceGroupName(), name(), inner()))
            .map(innerToFluentMap(this));
    }

    @Override
    public AppServiceCertificateImpl withPfxFile(File file) {
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return withPfxByteArray(fileContent);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
    }

    @Override
    public AppServiceCertificateImpl withPfxByteArray(byte[] pfxByteArray) {
        inner().withPfxBlob(pfxByteArray);
        return this;
    }

    @Override
    public AppServiceCertificateImpl withPfxFileFromUrl(String url) {
        this.pfxFileUrl = url;
        return this;
    }

    @Override
    public AppServiceCertificateImpl withExistingCertificateOrder(AppServiceCertificateOrder certificateOrder) {
        this.certificateOrder = certificateOrder;
        return this;
    }

    @Override
    public AppServiceCertificateImpl withPfxPassword(String password) {
        inner().withPassword(password);
        return this;
    }
}
