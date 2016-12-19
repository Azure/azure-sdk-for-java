/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.google.common.io.BaseEncoding;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewaySslCertificate;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 *  Implementation for ApplicationGatewaySslCertificate.
 */
@LangDefinition
class ApplicationGatewaySslCertificateImpl
    extends ChildResourceImpl<ApplicationGatewaySslCertificateInner, ApplicationGatewayImpl, ApplicationGateway>
    implements
        ApplicationGatewaySslCertificate,
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

    // Verbs

    @Override
    public ApplicationGatewayImpl attach() {
        return this.parent().withSslCertificate(this);
    }


    // Withers

    @Override
    public ApplicationGatewaySslCertificateImpl withPfxFromBytes(byte[] pfxData) {
        String encoded = new String(BaseEncoding.base64().encode(pfxData));
        this.inner().withData(encoded);
        return this;
    }

    @Override
    public ApplicationGatewaySslCertificateImpl withPfxFromFile(File pfxFile) throws IOException {
        if (pfxFile == null) {
            return null;
        }

        byte[] content = Files.readAllBytes(pfxFile.toPath());
        return (content != null) ? withPfxFromBytes(content) : null;
    }

    @Override
    public ApplicationGatewaySslCertificateImpl withPfxPassword(String password) {
        this.inner().withPassword(password);
        return this;
    }

}
