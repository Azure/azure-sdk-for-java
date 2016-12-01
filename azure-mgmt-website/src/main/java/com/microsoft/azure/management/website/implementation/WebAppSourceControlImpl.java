/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.website.WebAppBase;
import com.microsoft.azure.management.website.WebAppSourceControl;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 *  Implementation for {@link WebAppSourceControl} and its create and update interfaces.
 */
@LangDefinition
class WebAppSourceControlImpl<
        FluentT extends WebAppBase<FluentT>,
        FluentImplT extends WebAppBaseImpl<FluentT, FluentImplT>>
    extends IndexableWrapperImpl<SiteSourceControlInner>
    implements
        WebAppSourceControl,
        WebAppSourceControl.Definition<WebAppBase.DefinitionStages.WithCreate<FluentT>>,
        WebAppSourceControl.UpdateDefinition<WebAppBase.Update<FluentT>> {

    private WebAppBaseImpl<FluentT, FluentImplT> parent;
    private String githubAccessToken;
    private SourceControlService sourceControlService;

    WebAppSourceControlImpl(SiteSourceControlInner inner, WebAppBaseImpl<FluentT, FluentImplT> parent, AppServiceManager manager) {
        super(inner);
        this.parent = parent;
        this.sourceControlService = manager.restClient().retrofit().create(SourceControlService.class);
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
    public WebAppSourceControlImpl<FluentT, FluentImplT> withGit(String url) {
        inner().withIsMercurial(false).withRepoUrl(url);
        return this;
    }

    @Override
    public WebAppSourceControlImpl<FluentT, FluentImplT> withMercurial(String url) {
        inner().withIsMercurial(true).withRepoUrl(url);
        return this;
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
    public WebAppSourceControlImpl<FluentT, FluentImplT> withPublicExternalRepository() {
        inner().withIsManualIntegration(true);
        return this;
    }

    @Override
    public WebAppSourceControlImpl<FluentT, FluentImplT> withGitHubRepository(String organization, String repository) {
        return withGitHubRepository(String.format("https://github.com/%s/%s", organization, repository));
    }

    @Override
    public WebAppSourceControlImpl<FluentT, FluentImplT> withGitHubRepository(String url) {
        inner().withRepoUrl(url).withIsMercurial(false).withIsManualIntegration(false);
        return this;
    }

    @Override
    public WebAppSourceControlImpl<FluentT, FluentImplT> withLocalGitRepository() {
        inner().withRepoUrl(null).withBranch(null).withIsMercurial(false).withIsManualIntegration(false);
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
        return sourceControlService.updateSourceControl("Github", new SourceControlInner().withToken(githubAccessToken), "2016-03-01");
    }

    private interface SourceControlService {
        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("/providers/Microsoft.Web/sourcecontrols/{sourceControlIdentifier}")
        Observable<SourceControlInner> updateSourceControl(@Path("sourceControlIdentifier") String sourceControlIdentifier,  @Body SourceControlInner sourceControl, @Query("api-version") String apiVersion);
    }
}
