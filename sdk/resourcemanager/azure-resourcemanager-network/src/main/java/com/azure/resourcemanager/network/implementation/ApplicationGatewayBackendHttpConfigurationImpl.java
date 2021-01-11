// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.resourcemanager.network.models.ApplicationGatewayAuthenticationCertificate;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendHttpConfiguration;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendHttpSettings;
import com.azure.resourcemanager.network.models.ApplicationGatewayConnectionDraining;
import com.azure.resourcemanager.network.models.ApplicationGatewayCookieBasedAffinity;
import com.azure.resourcemanager.network.models.ApplicationGatewayProbe;
import com.azure.resourcemanager.network.models.ApplicationGatewayProtocol;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Base implementation for ApplicationGatewayBackendConfiguration. */
class ApplicationGatewayBackendHttpConfigurationImpl
    extends ChildResourceImpl<ApplicationGatewayBackendHttpSettings, ApplicationGatewayImpl, ApplicationGateway>
    implements ApplicationGatewayBackendHttpConfiguration,
        ApplicationGatewayBackendHttpConfiguration.Definition<ApplicationGateway.DefinitionStages.WithCreate>,
        ApplicationGatewayBackendHttpConfiguration.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewayBackendHttpConfiguration.Update {

    ApplicationGatewayBackendHttpConfigurationImpl(
        ApplicationGatewayBackendHttpSettings inner, ApplicationGatewayImpl parent) {
        super(inner, parent);
    }

    // Getters

    @Override
    public Map<String, ApplicationGatewayAuthenticationCertificate> authenticationCertificates() {
        Map<String, ApplicationGatewayAuthenticationCertificate> certs = new TreeMap<>();
        if (this.innerModel().authenticationCertificates() == null) {
            return Collections.unmodifiableMap(certs);
        } else {
            for (SubResource ref : this.innerModel().authenticationCertificates()) {
                ApplicationGatewayAuthenticationCertificate cert =
                    this.parent().authenticationCertificates().get(ResourceUtils.nameFromResourceId(ref.id()));
                if (cert != null) {
                    certs.put(cert.name(), cert);
                }
            }
        }
        return Collections.unmodifiableMap(certs);
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public ApplicationGatewayProbe probe() {
        if (this.parent().probes() != null && this.innerModel().probe() != null) {
            return this.parent().probes().get(ResourceUtils.nameFromResourceId(this.innerModel().probe().id()));
        } else {
            return null;
        }
    }

    @Override
    public String hostHeader() {
        return this.innerModel().hostname();
    }

    @Override
    public boolean isHostHeaderFromBackend() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().pickHostnameFromBackendAddress());
    }

    @Override
    public boolean isProbeEnabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().probeEnabled());
    }

    @Override
    public int connectionDrainingTimeoutInSeconds() {
        if (this.innerModel().connectionDraining() == null) {
            return 0;
        } else if (!this.innerModel().connectionDraining().enabled()) {
            return 0;
        } else {
            return this.innerModel().connectionDraining().drainTimeoutInSec();
        }
    }

    @Override
    public String affinityCookieName() {
        return this.innerModel().affinityCookieName();
    }

    @Override
    public String path() {
        return this.innerModel().path();
    }

    @Override
    public int port() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().port());
    }

    @Override
    public ApplicationGatewayProtocol protocol() {
        return this.innerModel().protocol();
    }

    @Override
    public boolean cookieBasedAffinity() {
        return this.innerModel().cookieBasedAffinity().equals(ApplicationGatewayCookieBasedAffinity.ENABLED);
    }

    @Override
    public int requestTimeout() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().requestTimeout());
    }

    // Verbs

    public ApplicationGatewayImpl attach() {
        this.parent().withBackendHttpConfiguration(this);
        return this.parent();
    }

    // Withers

    public ApplicationGatewayBackendHttpConfigurationImpl withPort(int port) {
        this.innerModel().withPort(port);
        return this;
    }

    public ApplicationGatewayBackendHttpConfigurationImpl withCookieBasedAffinity() {
        this.innerModel().withCookieBasedAffinity(ApplicationGatewayCookieBasedAffinity.ENABLED);
        return this;
    }

    public ApplicationGatewayBackendHttpConfigurationImpl withoutCookieBasedAffinity() {
        this.innerModel().withCookieBasedAffinity(ApplicationGatewayCookieBasedAffinity.DISABLED);
        return this;
    }

    private ApplicationGatewayBackendHttpConfigurationImpl withProtocol(ApplicationGatewayProtocol protocol) {
        this.innerModel().withProtocol(protocol);
        return this;
    }

    public ApplicationGatewayBackendHttpConfigurationImpl withRequestTimeout(int seconds) {
        this.innerModel().withRequestTimeout(seconds);
        return this;
    }

    public ApplicationGatewayBackendHttpConfigurationImpl withProbe(String name) {
        if (name == null) {
            return this.withoutProbe();
        } else {
            SubResource probeRef = new SubResource().withId(this.parent().futureResourceId() + "/probes/" + name);
            this.innerModel().withProbe(probeRef);
            return this;
        }
    }

    public ApplicationGatewayBackendHttpConfigurationImpl withoutProbe() {
        this.innerModel().withProbe(null);
        return this;
    }

    public ApplicationGatewayBackendHttpConfigurationImpl withHostHeaderFromBackend() {
        this.innerModel().withPickHostnameFromBackendAddress(true).withHostname(null);
        return this;
    }

    public ApplicationGatewayBackendHttpConfigurationImpl withHostHeader(String hostHeader) {
        this.innerModel().withHostname(hostHeader).withPickHostnameFromBackendAddress(false);
        return this;
    }

    public ApplicationGatewayBackendHttpConfigurationImpl withoutHostHeader() {
        this.innerModel().withHostname(null).withPickHostnameFromBackendAddress(false);
        return this;
    }

    public ApplicationGatewayBackendHttpConfigurationImpl withConnectionDrainingTimeoutInSeconds(int seconds) {
        if (this.innerModel().connectionDraining() == null) {
            this.innerModel().withConnectionDraining(new ApplicationGatewayConnectionDraining());
        }
        if (seconds > 0) {
            this.innerModel().connectionDraining().withDrainTimeoutInSec(seconds).withEnabled(true);
        }
        return this;
    }

    public ApplicationGatewayBackendHttpConfigurationImpl withoutConnectionDraining() {
        this.innerModel().withConnectionDraining(null);
        return this;
    }

    public ApplicationGatewayBackendHttpConfigurationImpl withAffinityCookieName(String name) {
        this.innerModel().withAffinityCookieName(name);
        return this;
    }

    public ApplicationGatewayBackendHttpConfigurationImpl withPath(String path) {
        if (path != null) {
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            if (!path.endsWith("/")) {
                path += "/";
            }
        }
        this.innerModel().withPath(path);
        return this;
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl withAuthenticationCertificate(String name) {
        if (name == null) {
            return this;
        }
        SubResource certRef =
            new SubResource().withId(this.parent().futureResourceId() + "/authenticationCertificates/" + name);
        List<SubResource> refs = this.innerModel().authenticationCertificates();
        if (refs == null) {
            refs = new ArrayList<>();
            this.innerModel().withAuthenticationCertificates(refs);
        }
        for (SubResource ref : refs) {
            if (ref.id().equalsIgnoreCase(certRef.id())) {
                return this;
            }
        }
        refs.add(certRef);
        return this.withHttps();
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl withAuthenticationCertificateFromBytes(byte[] derData) {
        if (derData == null) {
            return this;
        }

        String encoded = new String(Base64.getEncoder().encode(derData), StandardCharsets.UTF_8);
        return this.withAuthenticationCertificateFromBase64(encoded);
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl withAuthenticationCertificateFromBase64(String base64Data) {
        if (base64Data == null) {
            return this;
        }

        String certName = null;
        for (ApplicationGatewayAuthenticationCertificate cert : this.parent().authenticationCertificates().values()) {
            if (cert.data().contentEquals(base64Data)) {
                certName = cert.name();
                break;
            }
        }

        // If matching cert reference not found, create a new one
        if (certName == null) {
            certName = this.parent().manager().resourceManager().internalContext().randomResourceName("cert", 20);
            this.parent().defineAuthenticationCertificate(certName).fromBase64(base64Data).attach();
        }

        return this.withAuthenticationCertificate(certName).withHttps();
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl withAuthenticationCertificateFromFile(File certificateFile)
        throws IOException {
        if (certificateFile == null) {
            return this;
        } else {
            byte[] content = Files.readAllBytes(certificateFile.toPath());
            return this.withAuthenticationCertificateFromBytes(content);
        }
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl withoutAuthenticationCertificate(String name) {
        if (name == null) {
            return this;
        }
        for (SubResource ref : this.innerModel().authenticationCertificates()) {
            if (ResourceUtils.nameFromResourceId(ref.id()).equalsIgnoreCase(name)) {
                this.innerModel().authenticationCertificates().remove(ref);
                break;
            }
        }
        return this;
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl withHttps() {
        return this.withProtocol(ApplicationGatewayProtocol.HTTPS);
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl withHttp() {
        return this.withoutAuthenticationCertificates().withProtocol(ApplicationGatewayProtocol.HTTP);
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl withoutAuthenticationCertificates() {
        this.innerModel().withAuthenticationCertificates(null);
        return this;
    }
}
