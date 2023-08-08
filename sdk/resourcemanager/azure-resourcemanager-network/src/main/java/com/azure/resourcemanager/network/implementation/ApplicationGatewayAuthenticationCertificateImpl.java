// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.resourcemanager.network.models.ApplicationGatewayAuthenticationCertificate;
import com.azure.resourcemanager.network.fluent.models.ApplicationGatewayAuthenticationCertificateInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

/** Implementation for ApplicationGatewayAuthenticationCertificate. */
class ApplicationGatewayAuthenticationCertificateImpl
    extends ChildResourceImpl<
        ApplicationGatewayAuthenticationCertificateInner, ApplicationGatewayImpl, ApplicationGateway>
    implements ApplicationGatewayAuthenticationCertificate,
        ApplicationGatewayAuthenticationCertificate.Definition<ApplicationGateway.DefinitionStages.WithCreate>,
        ApplicationGatewayAuthenticationCertificate.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewayAuthenticationCertificate.Update {

    ApplicationGatewayAuthenticationCertificateImpl(
        ApplicationGatewayAuthenticationCertificateInner inner, ApplicationGatewayImpl parent) {
        super(inner, parent);
    }

    // Helpers

    // Getters

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public String data() {
        return this.innerModel().data();
    }

    // Verbs

    @Override
    public ApplicationGatewayImpl attach() {
        return this.parent().withAuthenticationCertificate(this);
    }

    // Withers

    @Override
    public ApplicationGatewayAuthenticationCertificateImpl fromBytes(byte[] data) {
        String encoded = new String(Base64.getEncoder().encode(data), StandardCharsets.UTF_8);
        return this.fromBase64(encoded);
    }

    @Override
    public ApplicationGatewayAuthenticationCertificateImpl fromFile(File certificateFile) throws IOException {
        if (certificateFile == null) {
            return null;
        }

        byte[] content = Files.readAllBytes(certificateFile.toPath());
        return this.fromBytes(content);
    }

    @Override
    public ApplicationGatewayAuthenticationCertificateImpl fromBase64(String base64data) {
        if (base64data == null) {
            return this;
        }

        this.innerModel().withData(base64data);
        return this;
    }
}
