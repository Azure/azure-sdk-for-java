// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.appservice.fluent.inner.SiteSourceControlInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/** An immutable representation of a web app source control configuration in a web app. */
@Fluent
public interface WebAppSourceControl extends HasInner<SiteSourceControlInner>, ChildResource<WebAppBase> {
    /** @return the repository or source control url */
    String repositoryUrl();

    /** @return the name of the branch to use for deployment */
    String branch();

    /** @return whether to do manual or continuous integration */
    boolean isManualIntegration();

    /** @return whether deployment rollback is enabled */
    boolean deploymentRollbackEnabled();

    /** @return mercurial or Git repository type */
    RepositoryType repositoryType();

    /**
     * The entirety of a web app source control definition.
     *
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithAttach<ParentT>,
            DefinitionStages.GitHubWithAttach<ParentT>,
            DefinitionStages.WithRepositoryType<ParentT>,
            DefinitionStages.WithBranch<ParentT>,
            DefinitionStages.WithGitHubBranch<ParentT> {
    }

    /** Grouping of web app source control definition stages applicable as part of a web app creation. */
    interface DefinitionStages {
        /**
         * The first stage of a web app source control definition as part of a definition of a web app.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface Blank<ParentT> extends WithRepositoryType<ParentT> {
        }

        /**
         * A web app source control definition allowing repository type to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithRepositoryType<ParentT> {
            /**
             * Specifies the repository to be a public external repository, either Git or Mercurial. Continuous
             * integration will not be turned on.
             *
             * @param url the url of the Git repository
             * @return the next stage of the definition
             */
            WithBranch<ParentT> withPublicGitRepository(String url);

            /**
             * Specifies the repository to be a public external repository, either Git or Mercurial. Continuous
             * integration will not be turned on.
             *
             * @param url the url of the Mercurial repository
             * @return the next stage of the definition
             */
            WithBranch<ParentT> withPublicMercurialRepository(String url);

            /**
             * Specifies the repository to be a GitHub repository. Continuous integration will be turned on. This
             * repository can be either public or private, but your GitHub access token must have enough privileges to
             * add a webhook to the repository.
             *
             * @param organization the user name or organization name the GitHub repository belongs to, e.g. Azure
             * @param repository the name of the repository, e.g. azure-sdk-for-java
             * @return the next stage of the definition
             */
            WithGitHubBranch<ParentT> withContinuouslyIntegratedGitHubRepository(
                String organization, String repository);

            /**
             * Specifies the repository to be a GitHub repository. Continuous integration will be turned on. This
             * repository can be either public or private, but your GitHub access token must have enough privileges to
             * add a webhook to the repository.
             *
             * @param url the URL pointing to the repository, e.g. https://github.com/Azure/azure-sdk-for-java
             * @return the next stage of the definition
             */
            WithGitHubBranch<ParentT> withContinuouslyIntegratedGitHubRepository(String url);
        }

        /**
         * A web app source control definition allowing branch to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithBranch<ParentT> {
            /**
             * Specifies the branch in the repository to use.
             *
             * @param branch the branch to use
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withBranch(String branch);
        }

        /**
         * A web app source control definition allowing branch to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithGitHubBranch<ParentT> {
            /**
             * Specifies the branch in the repository to use.
             *
             * @param branch the branch to use
             * @return the next stage of the definition
             */
            GitHubWithAttach<ParentT> withBranch(String branch);
        }

        /**
         * A web app source control definition allowing GitHub access token to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithGitHubAccessToken<ParentT> {
            /**
             * Specifies the GitHub personal access token. You can acquire one from https://github.com/settings/tokens.
             *
             * @param personalAccessToken the personal access token from GitHub.
             * @return the next stage of the definition
             */
            GitHubWithAttach<ParentT> withGitHubAccessToken(String personalAccessToken);
        }

        /**
         * The final stage of the web app source control definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the web app source control definition
         * can be attached to the parent web app definition using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends Attachable.InDefinition<ParentT> {
        }

        /**
         * The final stage of the web app source control definition that binds to a GitHub account.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the web app source control definition
         * can be attached to the parent web app definition using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface GitHubWithAttach<ParentT> extends WithAttach<ParentT>, WithGitHubAccessToken<ParentT> {
        }
    }

    /**
     * The entirety of a web app source control definition as part of a web app update.
     *
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT>,
            UpdateDefinitionStages.GitHubWithAttach<ParentT>,
            UpdateDefinitionStages.WithRepositoryType<ParentT>,
            UpdateDefinitionStages.WithBranch<ParentT>,
            UpdateDefinitionStages.WithGitHubBranch<ParentT> {
    }

    /** Grouping of web app source control definition stages applicable as part of a web app update. */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a web app source control definition as part of an update of a web app.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface Blank<ParentT> extends WithRepositoryType<ParentT> {
        }

        /**
         * A web app source control definition allowing repository type to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithRepositoryType<ParentT> {
            /**
             * Specifies the repository to be a public external repository, either Git or Mercurial. Continuous
             * integration will not be turned on.
             *
             * @param url the url of the Git repository
             * @return the next stage of the definition
             */
            WithBranch<ParentT> withPublicGitRepository(String url);

            /**
             * Specifies the repository to be a public external repository, either Git or Mercurial. Continuous
             * integration will not be turned on.
             *
             * @param url the url of the Mercurial repository
             * @return the next stage of the definition
             */
            WithBranch<ParentT> withPublicMercurialRepository(String url);

            /**
             * Specifies the repository to be a GitHub repository. Continuous integration will be turned on. This
             * repository can be either public or private, but your GitHub access token must have enough privileges to
             * add a webhook to the repository.
             *
             * @param organization the user name or organization name the GitHub repository belongs to, e.g. Azure
             * @param repository the name of the repository, e.g. azure-sdk-for-java
             * @return the next stage of the definition
             */
            WithGitHubBranch<ParentT> withContinuouslyIntegratedGitHubRepository(
                String organization, String repository);

            /**
             * Specifies the repository to be a GitHub repository. Continuous integration will be turned on. This
             * repository can be either public or private, but your GitHub access token must have enough privileges to
             * add a webhook to the repository.
             *
             * @param url the URL pointing to the repository, e.g. https://github.com/Azure/azure-sdk-for-java
             * @return the next stage of the definition
             */
            WithGitHubBranch<ParentT> withContinuouslyIntegratedGitHubRepository(String url);
        }

        /**
         * A web app source control definition allowing branch to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithBranch<ParentT> {
            /**
             * Specifies the branch in the repository to use.
             *
             * @param branch the branch to use
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withBranch(String branch);
        }

        /**
         * A web app source control definition allowing branch to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithGitHubBranch<ParentT> {
            /**
             * Specifies the branch in the repository to use.
             *
             * @param branch the branch to use
             * @return the next stage of the definition
             */
            GitHubWithAttach<ParentT> withBranch(String branch);
        }

        /**
         * A web app source control definition allowing GitHub access token to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithGitHubAccessToken<ParentT> {
            /**
             * Specifies the GitHub personal access token. You can acquire one from https://github.com/settings/tokens.
             *
             * @param personalAccessToken the personal access token from GitHub.
             * @return the next stage of the definition
             */
            GitHubWithAttach<ParentT> withGitHubAccessToken(String personalAccessToken);
        }

        /**
         * The final stage of the web app source control definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the web app source control definition
         * can be attached to the parent web app update using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends Attachable.InUpdate<ParentT> {
        }

        /**
         * The final stage of the web app source control definition that binds to a GitHub account.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the web app source control definition
         * can be attached to the parent web app update using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface GitHubWithAttach<ParentT> extends WithAttach<ParentT>, WithGitHubAccessToken<ParentT> {
        }
    }
}
