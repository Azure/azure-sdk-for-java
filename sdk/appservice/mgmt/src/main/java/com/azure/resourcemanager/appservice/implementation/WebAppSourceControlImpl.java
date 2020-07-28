// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.models.RepositoryType;
import com.azure.resourcemanager.appservice.models.WebAppBase;
import com.azure.resourcemanager.appservice.models.WebAppSourceControl;
import com.azure.resourcemanager.appservice.fluent.inner.SiteSourceControlInner;
import com.azure.resourcemanager.appservice.fluent.inner.SourceControlInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import reactor.core.publisher.Mono;

/**
 * Implementation for WebAppSourceControl and its create and update interfaces.
 *
 * @param <FluentT> the fluent interface of the parent web app
 * @param <FluentImplT> the fluent implementation of the parent web app
 */
class WebAppSourceControlImpl<FluentT extends WebAppBase, FluentImplT extends WebAppBaseImpl<FluentT, FluentImplT>>
    extends IndexableWrapperImpl<SiteSourceControlInner>
    implements WebAppSourceControl,
        WebAppSourceControl.Definition<WebAppBase.DefinitionStages.WithCreate<FluentT>>,
        WebAppSourceControl.UpdateDefinition<WebAppBase.Update<FluentT>> {

    private final WebAppBaseImpl<FluentT, FluentImplT> parent;
    private String githubAccessToken;

    WebAppSourceControlImpl(SiteSourceControlInner inner, WebAppBaseImpl<FluentT, FluentImplT> parent) {
        super(inner);
        this.parent = parent;
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
        return (FluentImplT) this.parent;
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
    public WebAppSourceControlImpl<FluentT, FluentImplT> withContinuouslyIntegratedGitHubRepository(
        String organization, String repository) {
        return withContinuouslyIntegratedGitHubRepository(
            String.format("https://github.com/%s/%s", organization, repository));
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

    Mono<SourceControlInner> registerGithubAccessToken() {
        if (githubAccessToken == null) {
            return Mono.empty();
        }
        SourceControlInner sourceControlInner = new SourceControlInner().withToken(githubAccessToken);
        return this.parent().manager().inner().getResourceProviders()
            .updateSourceControlAsync("Github", sourceControlInner);
    }
}
