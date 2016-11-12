/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.codec.binary.Base64;

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
        this.parent().withSslCertificate(this);
        return this.parent();
    }


    // Withers

    @Override
    public ApplicationGatewaySslCertificateImpl withPfxContent(byte[] pfxData) {
        String encoded = new String(Base64.encodeBase64(pfxData));
        this.inner().withData(encoded);
        return this;
    }

    @Override
    public ApplicationGatewaySslCertificateImpl withPfxFile(File pfxFile) {
        if (pfxFile == null) {
            return null;
        }

        byte[] content;
        try {
            content = Files.readAllBytes(pfxFile.toPath());
            return withPfxContent(content);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ApplicationGatewaySslCertificateImpl withPassword(String password) {
        this.inner().withPassword(password);
        return this;
    }

}
