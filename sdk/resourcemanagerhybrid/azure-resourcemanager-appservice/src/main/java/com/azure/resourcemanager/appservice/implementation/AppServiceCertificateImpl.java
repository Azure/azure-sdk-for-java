// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.models.AppServiceCertificate;
import com.azure.resourcemanager.appservice.models.AppServiceCertificateOrder;
import com.azure.resourcemanager.appservice.models.HostingEnvironmentProfile;
import com.azure.resourcemanager.appservice.fluent.models.CertificateInner;
import com.azure.resourcemanager.appservice.fluent.CertificatesClient;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;

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
        return innerModel().friendlyName();
    }

    @Override
    public String subjectName() {
        return innerModel().subjectName();
    }

    @Override
    public List<String> hostNames() {
        return Collections.unmodifiableList(innerModel().hostNames());
    }

    @Override
    public byte[] pfxBlob() {
        return innerModel().pfxBlob();
    }

    @Override
    public String siteName() {
        return innerModel().siteName();
    }

    @Override
    public String selfLink() {
        return innerModel().selfLink();
    }

    @Override
    public String issuer() {
        return innerModel().issuer();
    }

    @Override
    public OffsetDateTime issueDate() {
        return innerModel().issueDate();
    }

    @Override
    public OffsetDateTime expirationDate() {
        return innerModel().expirationDate();
    }

    @Override
    public String password() {
        return innerModel().password();
    }

    @Override
    public String thumbprint() {
        return innerModel().thumbprint();
    }

    @Override
    public Boolean valid() {
        return innerModel().valid();
    }

    @Override
    public byte[] certificateBlob() {
        return innerModel().cerBlob();
    }

    @Override
    public String publicKeyHash() {
        return innerModel().publicKeyHash();
    }

    @Override
    public HostingEnvironmentProfile hostingEnvironmentProfile() {
        return innerModel().hostingEnvironmentProfile();
    }

    @Override
    protected Mono<CertificateInner> getInnerAsync() {
        return this.manager().serviceClient().getCertificates().getByResourceGroupAsync(resourceGroupName(), name());
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
                            innerModel().withPfxBlob(bytes);
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
                            innerModel()
                                .withKeyVaultId(keyVaultBinding1.keyVaultId())
                                .withKeyVaultSecretName(keyVaultBinding1.keyVaultSecretName());
                            return null;
                        });
        }
        final CertificatesClient client = this.manager().serviceClient().getCertificates();
        return pfxBytes
            .then(keyVaultBinding)
            .then(client.createOrUpdateAsync(resourceGroupName(), name(), innerModel()))
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
        innerModel().withPfxBlob(pfxByteArray);
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
        innerModel().withPassword(password);
        return this;
    }
}
