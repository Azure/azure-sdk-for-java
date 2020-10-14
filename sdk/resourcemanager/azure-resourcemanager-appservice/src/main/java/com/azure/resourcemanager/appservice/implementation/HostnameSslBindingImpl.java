// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.models.AppServiceCertificate;
import com.azure.resourcemanager.appservice.models.AppServiceCertificateOrder;
import com.azure.resourcemanager.appservice.models.HostnameSslBinding;
import com.azure.resourcemanager.appservice.models.HostnameSslState;
import com.azure.resourcemanager.appservice.models.SslState;
import com.azure.resourcemanager.appservice.models.WebAppBase;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import reactor.core.publisher.Mono;

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
 * Implementation for {@link HostnameSslBinding} and its create and update interfaces.
 *
 * @param <FluentT> the fluent interface of the parent web app
 * @param <FluentImplT> the fluent implementation of the parent web app
 */
class HostnameSslBindingImpl<FluentT extends WebAppBase, FluentImplT extends WebAppBaseImpl<FluentT, FluentImplT>>
    extends IndexableWrapperImpl<HostnameSslState>
    implements HostnameSslBinding,
        HostnameSslBinding.Definition<WebAppBase.DefinitionStages.WithCreate<FluentT>>,
        HostnameSslBinding.UpdateDefinition<WebAppBase.Update<FluentT>> {

    private final ClientLogger logger = new ClientLogger(getClass());

    private Mono<AppServiceCertificate> newCertificate;
    private AppServiceCertificateOrder.DefinitionStages.WithKeyVault certificateInDefinition;

    private final FluentImplT parent;

    HostnameSslBindingImpl(HostnameSslState inner, FluentImplT parent) {
        super(inner);
        this.parent = parent;
    }

    @Override
    public String name() {
        return innerModel().name();
    }

    @Override
    public SslState sslState() {
        return innerModel().sslState();
    }

    @Override
    public String virtualIp() {
        return innerModel().virtualIp();
    }

    @Override
    public String thumbprint() {
        return innerModel().thumbprint();
    }

    @Override
    public FluentImplT attach() {
        parent.withNewHostNameSslBinding(this);
        return parent;
    }

    @Override
    public HostnameSslBindingImpl<FluentT, FluentImplT> withPfxCertificateToUpload(
        final File pfxFile, final String password) {
        String thumbprint = getCertificateThumbprint(pfxFile.getPath(), password);
        newCertificate =
            this
                .parent()
                .manager()
                .certificates()
                .define(getCertificateUniqueName(thumbprint, parent().region()))
                .withRegion(parent().region())
                .withExistingResourceGroup(parent().resourceGroupName())
                .withPfxFile(pfxFile)
                .withPfxPassword(password)
                .createAsync();
        return this;
    }

    @Override
    public HostnameSslBindingImpl<FluentT, FluentImplT> withExistingCertificate(
        final String certificateNameOrThumbprint) {
        newCertificate =
            this
                .parent()
                .manager()
                .certificates()
                .listByResourceGroupAsync(parent().resourceGroupName())
                .collectList()
                .map(
                    appServiceCertificates -> {
                        for (AppServiceCertificate certificate : appServiceCertificates) {
                            if (certificate.name().equals(certificateNameOrThumbprint)
                                || certificate.thumbprint().equalsIgnoreCase(certificateNameOrThumbprint)) {
                                return certificate;
                            }
                        }
                        return null;
                    })
                .map(
                    appServiceCertificate -> {
                        if (appServiceCertificate != null) {
                            withCertificateThumbprint(certificateNameOrThumbprint);
                        }
                        return appServiceCertificate;
                    });
        return this;
    }

    @Override
    public HostnameSslBindingImpl<FluentT, FluentImplT> withNewStandardSslCertificateOrder(
        final String certificateOrderName) {
        this.certificateInDefinition =
            this
                .parent()
                .manager()
                .certificateOrders()
                .define(certificateOrderName)
                .withExistingResourceGroup(parent().resourceGroupName())
                .withHostName(name())
                .withStandardSku()
                .withWebAppVerification(parent());
        return this;
    }

    @Override
    public HostnameSslBindingImpl<FluentT, FluentImplT> withExistingAppServiceCertificateOrder(
        final AppServiceCertificateOrder certificateOrder) {
        newCertificate =
            this
                .parent()
                .manager()
                .certificates()
                .define(getCertificateUniqueName(certificateOrder.signedCertificate().thumbprint(), parent().region()))
                .withRegion(parent().region())
                .withExistingResourceGroup(parent().resourceGroupName())
                .withExistingCertificateOrder(certificateOrder)
                .createAsync();
        return this;
    }

    private HostnameSslBindingImpl<FluentT, FluentImplT> withCertificateThumbprint(String thumbprint) {
        innerModel().withThumbprint(thumbprint);
        return this;
    }

    @Override
    public HostnameSslBindingImpl<FluentT, FluentImplT> withSniBasedSsl() {
        innerModel().withSslState(SslState.SNI_ENABLED);
        return this;
    }

    @Override
    public HostnameSslBindingImpl<FluentT, FluentImplT> withIpBasedSsl() {
        innerModel().withSslState(SslState.IP_BASED_ENABLED);
        return this;
    }

    Mono<AppServiceCertificate> newCertificate() {
        return newCertificate
            .doOnNext(
                appServiceCertificate -> {
                    if (appServiceCertificate != null) {
                        withCertificateThumbprint(appServiceCertificate.thumbprint());
                    }
                });
    }

    @Override
    public WebAppBase parent() {
        return parent;
    }

    @Override
    public HostnameSslBindingImpl<FluentT, FluentImplT> forHostname(String hostname) {
        innerModel().withName(hostname);
        return this;
    }

    @Override
    public HostnameSslBindingImpl<FluentT, FluentImplT> withExistingKeyVault(final Vault vault) {
        Mono<AppServiceCertificateOrder> appServiceCertificateOrderObservable =
            certificateInDefinition.withExistingKeyVault(vault).createAsync();
        final AppServiceManager manager = this.parent().manager();
        this.newCertificate =
            appServiceCertificateOrderObservable
                .flatMap(
                    appServiceCertificateOrder ->
                        manager
                            .certificates()
                            .define(appServiceCertificateOrder.name())
                            .withRegion(parent().regionName())
                            .withExistingResourceGroup(parent().resourceGroupName())
                            .withExistingCertificateOrder(appServiceCertificateOrder)
                            .createAsync());
        return this;
    }

    @Override
    public HostnameSslBindingImpl<FluentT, FluentImplT> withNewKeyVault(String vaultName) {
        Mono<AppServiceCertificateOrder> appServiceCertificateOrderObservable =
            certificateInDefinition.withNewKeyVault(vaultName, parent().region()).createAsync();
        final AppServiceManager manager = this.parent().manager();
        this.newCertificate =
            appServiceCertificateOrderObservable
                .flatMap(
                    appServiceCertificateOrder ->
                        manager
                            .certificates()
                            .define(appServiceCertificateOrder.name())
                            .withRegion(parent().regionName())
                            .withExistingResourceGroup(parent().resourceGroupName())
                            .withExistingCertificateOrder(appServiceCertificateOrder)
                            .createAsync());
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
            return com
                .azure
                .resourcemanager
                .appservice
                .implementation
                .Utils
                .base16Encode(sha.digest(certificate.getEncoded()));
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException ex) {
            throw logger.logExceptionAsError(new RuntimeException(ex));
        }
    }

    private String getCertificateUniqueName(String thumbprint, Region region) {
        return String.format("%s##%s#", thumbprint, region.label());
    }
}
