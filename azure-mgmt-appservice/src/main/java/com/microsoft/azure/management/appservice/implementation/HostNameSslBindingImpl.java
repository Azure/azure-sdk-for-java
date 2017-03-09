/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.appservice.implementation;

import com.google.common.io.BaseEncoding;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.appservice.AppServiceCertificate;
import com.microsoft.azure.management.appservice.AppServiceCertificateOrder;
import com.microsoft.azure.management.appservice.HostNameSslBinding;
import com.microsoft.azure.management.appservice.HostNameSslState;
import com.microsoft.azure.management.appservice.SslState;
import com.microsoft.azure.management.appservice.WebAppBase;
import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 *  Implementation for {@link HostNameSslBinding} and its create and update interfaces.
 *  @param <FluentT> the fluent interface of the parent web app
 *  @param <FluentImplT> the fluent implementation of the parent web app
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
class HostNameSslBindingImpl<
        FluentT extends WebAppBase,
        FluentImplT extends WebAppBaseImpl<FluentT, FluentImplT>>
    extends IndexableWrapperImpl<HostNameSslState>
    implements
        HostNameSslBinding,
        HostNameSslBinding.Definition<WebAppBase.DefinitionStages.WithCreate<FluentT>>,
        HostNameSslBinding.UpdateDefinition<WebAppBase.Update<FluentT>> {

    private Observable<AppServiceCertificate> newCertificate;
    private AppServiceCertificateOrder.DefinitionStages.WithKeyVault certificateInDefinition;

    private final FluentImplT parent;

    HostNameSslBindingImpl(HostNameSslState inner, FluentImplT parent) {
        super(inner);
        this.parent = parent;
    }

    @Override
    public String name() {
        return inner().name();
    }

    @Override
    public SslState sslState() {
        return inner().sslState();
    }

    @Override
    public String virtualIP() {
        return inner().virtualIP();
    }

    @Override
    public String thumbprint() {
        return inner().thumbprint();
    }

    @Override
    public FluentImplT attach() {
        parent.withNewHostNameSslBinding(this);
        return parent;
    }

    @Override
    public HostNameSslBindingImpl<FluentT, FluentImplT> withPfxCertificateToUpload(final File pfxFile, final String password) {
        String thumbprint = getCertificateThumbprint(pfxFile.getPath(), password);
        newCertificate = Utils.rootResource(this.parent().manager().certificates().define(
                getCertificateUniqueName(thumbprint, parent().region()))
                .withRegion(parent().region())
                .withExistingResourceGroup(parent().resourceGroupName())
                .withPfxFile(pfxFile)
                .withPfxPassword(password)
                .createAsync());
        return this;
    }

    @Override
    public HostNameSslBindingImpl<FluentT, FluentImplT> withNewStandardSslCertificateOrder(final String certificateOrderName) {
        this.certificateInDefinition = this.parent().manager().certificateOrders().define(certificateOrderName)
                .withExistingResourceGroup(parent().resourceGroupName())
                .withHostName(name())
                .withStandardSku()
                .withWebAppVerification(parent());
        return this;
    }

    @Override
    public HostNameSslBindingImpl<FluentT, FluentImplT> withExistingAppServiceCertificateOrder(final AppServiceCertificateOrder certificateOrder) {
        Observable<Indexable> resourceStream = this.parent().manager().certificates().define(getCertificateUniqueName(certificateOrder.signedCertificate().thumbprint(), parent().region()))
                .withRegion(parent().region())
                .withExistingResourceGroup(parent().resourceGroupName())
                .withExistingCertificateOrder(certificateOrder)
                .createAsync();
        newCertificate = Utils.rootResource(resourceStream);
        return this;
    }

    private HostNameSslBindingImpl<FluentT, FluentImplT> withCertificateThumbprint(String thumbprint) {
        inner().withThumbprint(thumbprint);
        return this;
    }

    @Override
    public HostNameSslBindingImpl<FluentT, FluentImplT> withSniBasedSsl() {
        inner().withSslState(SslState.SNI_ENABLED);
        return this;
    }

    @Override
    public HostNameSslBindingImpl<FluentT, FluentImplT> withIpBasedSsl() {
        inner().withSslState(SslState.IP_BASED_ENABLED);
        return this;
    }

    Observable<AppServiceCertificate> newCertificate() {
        return newCertificate.doOnNext(new Action1<AppServiceCertificate>() {
            @Override
            public void call(AppServiceCertificate appServiceCertificate) {
                withCertificateThumbprint(appServiceCertificate.thumbprint());
            }
        });
    }

    @Override
    public WebAppBase parent() {
        return parent;
    }

    @Override
    public HostNameSslBindingImpl<FluentT, FluentImplT> forHostname(String hostname) {
        inner().withName(hostname);
        return this;
    }

    @Override
    public HostNameSslBindingImpl<FluentT, FluentImplT> withExistingKeyVault(final Vault vault) {
        Observable<AppServiceCertificateOrder> appServiceCertificateOrderObservable = Utils.rootResource(certificateInDefinition
                .withExistingKeyVault(vault)
                .createAsync());
        final AppServiceManager manager = this.parent().manager();
        this.newCertificate = appServiceCertificateOrderObservable
                .flatMap(new Func1<AppServiceCertificateOrder, Observable<AppServiceCertificate>>() {
            @Override
            public Observable<AppServiceCertificate> call(AppServiceCertificateOrder appServiceCertificateOrder) {
                return Utils.rootResource(manager.certificates().define(appServiceCertificateOrder.name())
                        .withRegion(parent().regionName())
                        .withExistingResourceGroup(parent().resourceGroupName())
                        .withExistingCertificateOrder(appServiceCertificateOrder)
                        .createAsync());
            }
        });
        return this;
    }

    @Override
    public HostNameSslBindingImpl<FluentT, FluentImplT> withNewKeyVault(String vaultName) {
        Observable<AppServiceCertificateOrder> appServiceCertificateOrderObservable = Utils.rootResource(certificateInDefinition
                .withNewKeyVault(vaultName, parent().region())
                .createAsync());
        final AppServiceManager manager = this.parent().manager();
        this.newCertificate = appServiceCertificateOrderObservable
                .flatMap(new Func1<AppServiceCertificateOrder, Observable<AppServiceCertificate>>() {
                    @Override
                    public Observable<AppServiceCertificate> call(AppServiceCertificateOrder appServiceCertificateOrder) {
                        return Utils.rootResource(manager.certificates().define(appServiceCertificateOrder.name())
                                .withRegion(parent().regionName())
                                .withExistingResourceGroup(parent().resourceGroupName())
                                .withExistingCertificateOrder(appServiceCertificateOrder)
                                .createAsync());
                    }
                });
        return this;
    }

    private String getCertificateThumbprint(String pfxPath, String password) {
        try {
            InputStream inStream = new FileInputStream(pfxPath);

            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(inStream, password.toCharArray());

            String alias = ks.aliases().nextElement();
            X509Certificate certificate = (X509Certificate) ks.getCertificate(alias);
            inStream.close();
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            return BaseEncoding.base16().encode(sha.digest(certificate.getEncoded()));
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String getCertificateUniqueName(String thumbprint, Region region) {
        return String.format("%s##%s#", thumbprint, region.label());
    }
}
