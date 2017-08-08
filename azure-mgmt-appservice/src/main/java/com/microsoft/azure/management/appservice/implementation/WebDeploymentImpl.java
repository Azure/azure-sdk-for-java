/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.appservice.MSDeployCore;
import com.microsoft.azure.management.appservice.MSDeployParameterEntry;
import com.microsoft.azure.management.appservice.WebAppBase;
import com.microsoft.azure.management.appservice.WebDeployment;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.ExecutableImpl;
import org.joda.time.DateTime;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;

/**
 * Implementation of WebDeployment.
 *
 * @param <FluentT> the fluent interface, web app, function app, or deployment slot
 * @param <FluentImplT> the implementation class for FluentT
 */
@LangDefinition
public class WebDeploymentImpl<
        FluentT extends WebAppBase,
        FluentImplT extends WebAppBaseImpl<FluentT, FluentImplT>>
        extends ExecutableImpl<WebDeployment>
        implements WebDeployment, WebDeployment.Definition {
    private final WebAppBaseImpl<FluentT, FluentImplT> parent;
    private MSDeployInner request;
    private MSDeployStatusInner result;

    WebDeploymentImpl(WebAppBaseImpl<FluentT, FluentImplT> parent) {
        this.parent = parent;
        this.request = new MSDeployInner();
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
    public DateTime startTime() {
        return result.startTime();
    }

    @Override
    public DateTime endTime() {
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
    public Observable<WebDeployment> executeWorkAsync() {
        return parent.createMSDeploy(request)
                .map(new Func1<MSDeployStatusInner, WebDeployment>() {
                    @Override
                    public WebDeployment call(MSDeployStatusInner msDeployStatusInner) {
                        result = msDeployStatusInner;
                        return WebDeploymentImpl.this;
                    }
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
            request.withSetParameters(new ArrayList<MSDeployParameterEntry>());
        }
        request.setParameters().add(new MSDeployParameterEntry().withName(name).withValue(value));
        return this;
    }
}
