// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.management.network.implementation;

import com.azure.management.network.ApplicationGateway;
import com.azure.management.network.ApplicationGatewaySslCertificate;
import com.azure.management.network.models.ApplicationGatewaySslCertificateInner;
import com.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

/** Implementation for ApplicationGatewaySslCertificate. */
class ApplicationGatewaySslCertificateImpl
    extends ChildResourceImpl<ApplicationGatewaySslCertificateInner, ApplicationGatewayImpl, ApplicationGateway>
    implements ApplicationGatewaySslCertificate,
        ApplicationGatewaySslCertificate.Definition<ApplicationGateway.DefinitionStages.WithCreate>,
        ApplicationGatewaySslCertificate.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewaySslCertificate.Update {

    ApplicationGatewaySslCertificateImpl(ApplicationGatewaySslCertificateInner inner, ApplicationGatewayImpl parent) {
        super(inner, parent);
    }

    // Helpers

    // Getters

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String publicData() {
        return this.inner().publicCertData();
    }

    @Override
    public String keyVaultSecretId() {
        return this.inner().keyVaultSecretId();
    }

    // Verbs

    @Override
    public ApplicationGatewayImpl attach() {
        return this.parent().withSslCertificate(this);
    }

    // Withers

    @Override
    public ApplicationGatewaySslCertificateImpl withPfxFromBytes(byte[] pfxData) {
        String encoded = new String(Base64.getEncoder().encode(pfxData), StandardCharsets.UTF_8);
        this.inner().withData(encoded);
        return this;
    }

    @Override
    public ApplicationGatewaySslCertificateImpl withPfxFromFile(File pfxFile) throws IOException {
        if (pfxFile == null) {
            return null;
        }

        byte[] content = Files.readAllBytes(pfxFile.toPath());
        return withPfxFromBytes(content);
    }

    @Override
    public ApplicationGatewaySslCertificateImpl withPfxPassword(String password) {
        this.inner().withPassword(password);
        return this;
    }

    @Override
    public ApplicationGatewaySslCertificateImpl withKeyVaultSecretId(String keyVaultSecretId) {
        this.inner().withKeyVaultSecretId(keyVaultSecretId);
        return this;
    }
}
