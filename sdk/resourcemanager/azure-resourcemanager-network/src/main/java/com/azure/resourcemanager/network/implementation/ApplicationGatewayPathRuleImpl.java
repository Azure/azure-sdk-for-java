// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackend;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendHttpConfiguration;
import com.azure.resourcemanager.network.models.ApplicationGatewayPathRule;
import com.azure.resourcemanager.network.models.ApplicationGatewayRedirectConfiguration;
import com.azure.resourcemanager.network.models.ApplicationGatewayUrlPathMap;
import com.azure.resourcemanager.network.fluent.models.ApplicationGatewayPathRuleInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Implementation for application gateway path rule. */
class ApplicationGatewayPathRuleImpl
    extends ChildResourceImpl<
        ApplicationGatewayPathRuleInner, ApplicationGatewayUrlPathMapImpl, ApplicationGatewayUrlPathMap>
    implements ApplicationGatewayPathRule,
        ApplicationGatewayPathRule.Definition<
            ApplicationGatewayUrlPathMap.DefinitionStages.WithAttach<
                ApplicationGateway.DefinitionStages.WithRequestRoutingRuleOrCreate>>,
        ApplicationGatewayPathRule.UpdateDefinition<
            ApplicationGatewayUrlPathMap.UpdateDefinitionStages.WithAttach<ApplicationGateway.Update>>,
        ApplicationGatewayPathRule.Update {

    ApplicationGatewayPathRuleImpl(ApplicationGatewayPathRuleInner inner, ApplicationGatewayUrlPathMapImpl parent) {
        super(inner, parent);
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public ApplicationGatewayUrlPathMapImpl attach() {
        return this.parent().withPathRule(this);
    }

    @Override
    public ApplicationGatewayPathRuleImpl toBackendHttpConfiguration(String name) {
        SubResource httpConfigRef =
            new SubResource()
                .withId(this.parent().parent().futureResourceId() + "/backendHttpSettingsCollection/" + name);
        this.innerModel().withBackendHttpSettings(httpConfigRef);
        return this;
    }

    @Override
    public ApplicationGatewayPathRuleImpl toBackend(String name) {
        this.innerModel().withBackendAddressPool(this.parent().parent().ensureBackendRef(name));
        return this;
    }

    @Override
    public ApplicationGatewayPathRuleImpl withRedirectConfiguration(String name) {
        if (name == null) {
            this.innerModel().withRedirectConfiguration(null);
        } else {
            SubResource ref =
                new SubResource().withId(this.parent().parent().futureResourceId() + "/redirectConfigurations/" + name);
            this.innerModel().withRedirectConfiguration(ref);
        }
        return this;
    }

    @Override
    public ApplicationGatewayBackend backend() {
        SubResource backendRef = this.innerModel().backendAddressPool();
        if (backendRef != null) {
            String backendName = ResourceUtils.nameFromResourceId(backendRef.id());
            return this.parent().parent().backends().get(backendName);
        } else {
            return null;
        }
    }

    @Override
    public ApplicationGatewayBackendHttpConfiguration backendHttpConfiguration() {
        SubResource configRef = this.innerModel().backendHttpSettings();
        if (configRef != null) {
            String configName = ResourceUtils.nameFromResourceId(configRef.id());
            return this.parent().parent().backendHttpConfigurations().get(configName);
        } else {
            return null;
        }
    }

    @Override
    public ApplicationGatewayRedirectConfiguration redirectConfiguration() {
        SubResource ref = this.innerModel().redirectConfiguration();
        if (ref == null) {
            return null;
        } else {
            return this.parent().parent().redirectConfigurations().get(ResourceUtils.nameFromResourceId(ref.id()));
        }
    }

    @Override
    public List<String> paths() {
        return Collections.unmodifiableList(innerModel().paths());
    }

    @Override
    public ApplicationGatewayPathRuleImpl withPath(String path) {
        if (innerModel().paths() == null) {
            innerModel().withPaths(new ArrayList<String>());
        }
        innerModel().paths().add(path);
        return this;
    }

    @Override
    public ApplicationGatewayPathRuleImpl withPaths(String... paths) {
        innerModel().withPaths(Arrays.asList(paths));
        return this;
    }
}
