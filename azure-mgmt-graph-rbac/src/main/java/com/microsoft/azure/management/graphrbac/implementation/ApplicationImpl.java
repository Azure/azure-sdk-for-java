/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.google.common.io.BaseEncoding;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.Application;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import org.joda.time.LocalDate;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for ServicePrincipal and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Fluent.Graph.RBAC")
class ApplicationImpl
        extends CreatableUpdatableImpl<Application, ApplicationInner, ApplicationImpl>
        implements
            Application,
            Application.Definition,
            Application.Update {
    private GraphRbacManager manager;
    private ApplicationCreateParametersInner createParameters;
    private ApplicationUpdateParametersInner updateParameters;

    ApplicationImpl(ApplicationInner innerObject, GraphRbacManager manager) {
        super(innerObject.displayName(), innerObject);
        this.manager = manager;
        this.createParameters = new ApplicationCreateParametersInner().withDisplayName(innerObject.displayName());
        this.updateParameters = new ApplicationUpdateParametersInner().withDisplayName(innerObject.displayName());
    }

    @Override
    public boolean isInCreateMode() {
        return false;
    }

    @Override
    public Observable<Application> createResourceAsync() {
        return manager.inner().applications().createAsync(createParameters)
                .map(innerToFluentMap(this));
    }

    @Override
    public String id() {
        return inner().objectId();
    }

    @Override
    public String objectType() {
        return inner().objectType();
    }

    @Override
    public String appId() {
        return inner().appId();
    }

    @Override
    public List<String> appPermissions() {
        return inner().appPermissions();
    }

    @Override
    public boolean availableToOtherTenants() {
        return inner().availableToOtherTenants();
    }

    @Override
    public List<String> identifierUris() {
        return inner().identifierUris();
    }

    @Override
    public List<String> replyUrls() {
        return inner().replyUrls();
    }

    @Override
    public String homepage() {
        return inner().homepage();
    }

    @Override
    protected Observable<ApplicationInner> getInnerAsync() {
        return manager.inner().applications().getAsync(id());
    }

    @Override
    public ApplicationImpl withSignOnUrl(String signOnUrl) {
        createParameters.withHomepage(signOnUrl);
        return withReplyUrl(signOnUrl);
    }

    @Override
    public ApplicationImpl withReplyUrl(String replyUrl) {
        if (createParameters.replyUrls() == null) {
            createParameters.withReplyUrls(new ArrayList<String>());
        }
        createParameters.replyUrls().add(replyUrl);
        return this;
    }

    @Override
    public ApplicationImpl withIdentifierUrl(String identifierUrl) {
        if (createParameters.identifierUris() == null) {
            createParameters.withIdentifierUris(new ArrayList<String>());
        }
        createParameters.identifierUris().add(identifierUrl);
        return this;
    }

    @Override
    public ApplicationImpl withPassword(String password) {
        if (createParameters.passwordCredentials() == null) {
            createParameters.withPasswordCredentials(new ArrayList<PasswordCredentialInner>());
        }
        createParameters.passwordCredentials().add(new PasswordCredentialInner().withValue(password));
        return this;
    }

    @Override
    public ApplicationImpl withCertificate(byte[] pfxBlob) {
        if (createParameters.keyCredentials() == null) {
            createParameters.withKeyCredentials(new ArrayList<KeyCredentialInner>());
        }
        createParameters.keyCredentials().add(new KeyCredentialInner().withValue(BaseEncoding.base64().encode(pfxBlob)));
        return this;
    }

    @Override
    public CredentialImpl<DefinitionStages.WithCreate> defineKey() {
        return new CredentialImpl<DefinitionStages.WithCreate>();
    }

    @Override
    public ApplicationImpl withMultiTenant(boolean availableToOtherTenants) {
        return null;
    }

    @Override
    public ApplicationImpl withStartDate(LocalDate startDate) {
        return null;
    }

    @Override
    public ApplicationImpl withEndDate(LocalDate endDate) {
        return null;
    }
}
