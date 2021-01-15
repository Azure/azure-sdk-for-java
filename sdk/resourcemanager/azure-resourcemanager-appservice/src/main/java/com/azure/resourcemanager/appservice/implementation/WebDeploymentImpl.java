// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.models.MSDeploy;
import com.azure.resourcemanager.appservice.models.MSDeployCore;
import com.azure.resourcemanager.appservice.models.WebAppBase;
import com.azure.resourcemanager.appservice.models.WebDeployment;
import com.azure.resourcemanager.appservice.fluent.models.MSDeployStatusInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.ExecutableImpl;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import reactor.core.publisher.Mono;

/**
 * Implementation of WebDeployment.
 *
 * @param <FluentT> the fluent interface, web app, function app, or deployment slot
 * @param <FluentImplT> the implementation class for FluentT
 */
public class WebDeploymentImpl<FluentT extends WebAppBase, FluentImplT extends WebAppBaseImpl<FluentT, FluentImplT>>
    extends ExecutableImpl<WebDeployment> implements WebDeployment, WebDeployment.Definition {
    private final WebAppBaseImpl<FluentT, FluentImplT> parent;
    private MSDeploy request;
    private MSDeployStatusInner result;

    WebDeploymentImpl(WebAppBaseImpl<FluentT, FluentImplT> parent) {
        this.parent = parent;
        this.request = new MSDeploy();
    }

    @Override
    public WebAppBase parent() {
        return parent;
    }

    @Override
    public String deployer() {
        return result.deployer();
    }

    @Override
    public OffsetDateTime startTime() {
        return result.startTime();
    }

    @Override
    public OffsetDateTime endTime() {
        return result.endTime();
    }

    @Override
    public boolean complete() {
        return result.complete();
    }

    @Override
    public WebDeploymentImpl<FluentT, FluentImplT> withPackageUri(String packageUri) {
        request.withAddOnPackages(new ArrayList<MSDeployCore>());
        request.addOnPackages().add(new MSDeployCore().withPackageUri(packageUri));
        return this;
    }

    @Override
    public Mono<WebDeployment> executeWorkAsync() {
        return parent
            .createMSDeploy(request)
            .map(
                msDeployStatusInner -> {
                    result = msDeployStatusInner;
                    return WebDeploymentImpl.this;
                });
    }

    @Override
    public WebDeploymentImpl<FluentT, FluentImplT> withExistingDeploymentsDeleted(boolean deleteExisting) {
        if (deleteExisting) {
            MSDeployCore first = request.addOnPackages().remove(0);
            request.withPackageUri(first.packageUri());
        }
        return this;
    }

    @Override
    public WebDeploymentImpl<FluentT, FluentImplT> withAddOnPackage(String packageUri) {
        request.addOnPackages().add(new MSDeployCore().withPackageUri(packageUri));
        return this;
    }

    @Override
    public WebDeploymentImpl<FluentT, FluentImplT> withSetParametersXmlFile(String fileUri) {
        request.withSetParametersXmlFileUri(fileUri);
        return this;
    }

    @Override
    public WebDeploymentImpl<FluentT, FluentImplT> withSetParameter(String name, String value) {
        if (request.setParameters() == null) {
            request.withSetParameters(new HashMap<String, String>());
        }
        request.setParameters().put(name, value);
        return this;
    }
}
