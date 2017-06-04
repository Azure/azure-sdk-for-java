/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.appservice.AppServiceCertificate;
import com.microsoft.azure.management.appservice.AppServiceCertificateKeyVaultBinding;
import com.microsoft.azure.management.appservice.AppServiceCertificateOrder;
import com.microsoft.azure.management.appservice.HostingEnvironmentProfile;
import org.joda.time.DateTime;
import rx.Observable;
import rx.functions.Func1;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

/**
 * The implementation for AppServiceCertificate.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
class AppServiceCertificateImpl
        extends
        GroupableResourceImpl<
                AppServiceCertificate,
                CertificateInner,
                AppServiceCertificateImpl,
                AppServiceManager>
        implements
        AppServiceCertificate,
        AppServiceCertificate.Definition {

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
    public DateTime issueDate() {
        return inner().issueDate();
    }

    @Override
    public DateTime expirationDate() {
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
    public String certificateBlob() {
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
    protected Observable<CertificateInner> getInnerAsync() {
        return this.manager().inner().certificates().getByResourceGroupAsync(resourceGroupName(), name());
    }

    @Override
    public Observable<AppServiceCertificate> createResourceAsync() {
        Observable<Void> pfxBytes = Observable.just(null);
        if (pfxFileUrl != null) {
            pfxBytes = Utils.downloadFileAsync(pfxFileUrl, this.manager().restClient().retrofit())
                    .map(new Func1<byte[], Void>() {
                        @Override
                        public Void call(byte[] bytes) {
                            inner().withPfxBlob(bytes);
                            return null;
                        }
                    });
        }
        Observable<Void> keyVaultBinding = Observable.just(null);
        if (certificateOrder != null) {
            keyVaultBinding = certificateOrder.getKeyVaultBindingAsync()
                    .map(new Func1<AppServiceCertificateKeyVaultBinding, Void>() {
                        @Override
                        public Void call(AppServiceCertificateKeyVaultBinding keyVaultBinding) {
                            inner().withKeyVaultId(keyVaultBinding.keyVaultId()).withKeyVaultSecretName(keyVaultBinding.keyVaultSecretName());
                            return null;
                        }
                    });
        }
        final CertificatesInner client = this.manager().inner().certificates();
        return pfxBytes.concatWith(keyVaultBinding).last()
                .flatMap(new Func1<Void, Observable<CertificateInner>>() {
                    @Override
                    public Observable<CertificateInner> call(Void aVoid) {
                        return client.createOrUpdateAsync(resourceGroupName(), name(), inner());
                    }
                }).map(innerToFluentMap(this));
    }

    @Override
    public AppServiceCertificateImpl withPfxFile(File file) {
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return withPfxByteArray(fileContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
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