/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.appservice.RepositoryType;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.appservice.WebAppBase;
import com.microsoft.azure.management.appservice.WebAppSourceControl;
import rx.Observable;

/**
 *  Implementation for {@link WebAppSourceControl} and its create and update interfaces.
 *  @param <FluentT> the fluent interface of the parent web app
 *  @param <FluentImplT> the fluent implementation of the parent web app
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
class WebAppSourceControlImpl<
        FluentT extends WebAppBase,
        FluentImplT extends WebAppBaseImpl<FluentT, FluentImplT>>
    extends IndexableWrapperImpl<SiteSourceControlInner>
    implements
        WebAppSourceControl,
        WebAppSourceControl.Definition<WebAppBase.DefinitionStages.WithCreate<FluentT>>,
        WebAppSourceControl.UpdateDefinition<WebAppBase.Update<FluentT>> {

    private final WebAppBaseImpl<FluentT, FluentImplT> parent;
    private final WebSiteManagementClientImpl serviceClient;
    private String githubAccessToken;

    WebAppSourceControlImpl(SiteSourceControlInner inner, WebAppBaseImpl<FluentT, FluentImplT> parent, WebSiteManagementClientImpl serviceClient) {
        super(inner);
        this.parent = parent;
        this.serviceClient = serviceClient;
    }

    @Override
    public String name() {
        return inner().name();
    }

    @Override
    public String repositoryUrl() {
        return inner().repoUrl();
    }

    @Override
    public String branch() {
        return inner().branch();
    }

    @Override
    public boolean isManualIntegration() {
        return Utils.toPrimitiveBoolean(inner().isManualIntegration());
    }

    @Override
    public boolean deploymentRollbackEnabled() {
        return Utils.toPrimitiveBoolean(inner().deploymentRollbackEnabled());
    }

    @Override
    public RepositoryType repositoryType() {
        if (inner().isMercurial() == null) {
            return null;
        } else {
            return inner().isMercurial() ? RepositoryType.MERCURIAL : RepositoryType.GIT;
        }
    }

    @Override
    public FluentImplT attach() {
        parent().withSourceControl(this);
        return parent();
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT parent() {
        return (FluentImplT) parent;
    }

    @Override
    public WebAppSourceControlImpl<FluentT, FluentImplT> withBranch(String branch) {
        inner().withBranch(branch);
        return this;
    }

    @Override
    public WebAppSourceControlImpl<FluentT, FluentImplT> withPublicGitRepository(String url) {
        inner().withIsManualIntegration(true).withIsMercurial(false).withRepoUrl(url);
        return this;
    }

    @Override
    public WebAppSourceControlImpl<FluentT, FluentImplT> withPublicMercurialRepository(String url) {
        inner().withIsManualIntegration(true).withIsMercurial(true).withRepoUrl(url);
        return this;
    }

    @Override
    public WebAppSourceControlImpl<FluentT, FluentImplT> withContinuouslyIntegratedGitHubRepository(String organization, String repository) {
        return withContinuouslyIntegratedGitHubRepository(String.format("https://github.com/%s/%s", organization, repository));
    }

    @Override
    public WebAppSourceControlImpl<FluentT, FluentImplT> withContinuouslyIntegratedGitHubRepository(String url) {
        inner().withRepoUrl(url).withIsMercurial(false).withIsManualIntegration(false);
        return this;
    }

    @Override
    public WebAppSourceControlImpl<FluentT, FluentImplT> withGitHubAccessToken(String personalAccessToken) {
        this.githubAccessToken = personalAccessToken;
        return this;
    }

    Observable<SourceControlInner> registerGithubAccessToken() {
        if (githubAccessToken == null) {
            return Observable.just(null);
        }
        SourceControlInner sourceControlInner = new SourceControlInner().withToken(githubAccessToken);
        sourceControlInner.withLocation(parent().regionName());
        return serviceClient.updateSourceControlAsync("Github", sourceControlInner);
    }
}
