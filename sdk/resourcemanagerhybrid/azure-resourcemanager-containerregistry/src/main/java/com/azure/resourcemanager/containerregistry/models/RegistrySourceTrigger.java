// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;
import java.util.List;

/** An immutable client-side representation of a Container Registry source trigger. */
@Fluent()
public interface RegistrySourceTrigger extends HasInnerModel<SourceTrigger> {

    /** @return Returns the type of source control this trigger uses. I.e., Github, AzureDevOps etc. */
    SourceControlType sourceControlType();

    /** @return the URL of the repository used as source control. */
    String sourceControlRepositoryUrl();

    /** @return the list of actions that trigger an event. I.e., a commit, a pull request etc. */
    List<SourceTriggerEvent> sourceTriggerEvents();

    /** @return the branch of the repository that is being used as source control. I.e., master. */
    String sourceControlBranch();

    /** @return the source trigger status. I.e., enabled, disabled. */
    TriggerStatus status();

    /** Container interface for all of the definitions related to a container registry source trigger. */
    interface Definition
        extends RegistrySourceTrigger.DefinitionStages.Blank,
            RegistrySourceTrigger.DefinitionStages.RepositoryUrl,
            RegistrySourceTrigger.DefinitionStages.TriggerEventsDefinition,
            RegistrySourceTrigger.DefinitionStages.RepositoryBranchAndAuth,
            RegistrySourceTrigger.DefinitionStages.TriggerStatusDefinition,
            RegistrySourceTrigger.DefinitionStages.SourceTriggerAttachable {
    }

    /** Container interface for all of the updates related to a container registry source trigger. */
    interface Update
        extends RegistrySourceTrigger.UpdateStages.SourceControlType,
            RegistrySourceTrigger.UpdateStages.RepositoryUrl,
            RegistrySourceTrigger.UpdateStages.TriggerEventsDefinition,
            RegistrySourceTrigger.UpdateStages.RepositoryBranchAndAuth,
            RegistrySourceTrigger.UpdateStages.TriggerStatusDefinition,
            Settable<RegistryTask.Update> {
    }

    /** Container interface for defining a new trigger during a task update. */
    interface UpdateDefinition
        extends RegistrySourceTrigger.UpdateDefinitionStages.Blank,
            RegistrySourceTrigger.UpdateDefinitionStages.RepositoryUrl,
            RegistrySourceTrigger.UpdateDefinitionStages.TriggerEventsDefinition,
            RegistrySourceTrigger.UpdateDefinitionStages.RepositoryBranchAndAuth,
            RegistrySourceTrigger.UpdateDefinitionStages.TriggerStatusDefinition,
            RegistrySourceTrigger.UpdateDefinitionStages.SourceTriggerAttachable,
            Settable<RegistryTask.Update> {
    }

    /** Grouping of source trigger definition stages. */
    interface DefinitionStages {

        /** The first stage of a source trigger definition. */
        interface Blank {
            /**
             * The function that specifies Github will be used as the type of source control.
             *
             * @return the next stage of the container registry source trigger definition.
             */
            RepositoryUrl withGithubAsSourceControl();

            /**
             * The function that specifies Azure DevOps will be used as the type of source control.
             *
             * @return the next stage of the container registry source trigger definition.
             */
            RepositoryUrl withAzureDevOpsAsSourceControl();

            /**
             * The function that allows the user to input their own kind of source control.
             *
             * @param sourceControl the source control the user wishes to use.
             * @return the next stage of the container registry source trigger definition.
             */
            RepositoryUrl withSourceControl(SourceControlType sourceControl);
        }

        /**
         * The stage of the container registry source trigger definition allowing to specify the URL of the source
         * control repository.
         */
        interface RepositoryUrl {
            /**
             * The function that specifies the URL of the source control repository.
             *
             * @param sourceControlRepositoryUrl the URL of the source control repository.
             * @return the next stage of the container registry source trigger definition.
             */
            TriggerEventsDefinition withSourceControlRepositoryUrl(String sourceControlRepositoryUrl);
        }

        /**
         * The stage of the container registry source trigger definition allowing to specify the type of actions that
         * will trigger a run.
         */
        interface TriggerEventsDefinition {
            /**
             * The function that specifies a commit action will trigger a run.
             *
             * @return the next stage of the container registry source trigger definition.
             */
            SourceTriggerAttachable withCommitTriggerEvent();

            /**
             * The function that specifies a pull action will trigger a run.
             *
             * @return the next stage of the container registry source trigger definition.
             */
            SourceTriggerAttachable withPullTriggerEvent();

            /**
             * The function that allows the user to specify an action that will trigger a run when it is executed.
             *
             * @param sourceTriggerEvent the action that will trigger a run.
             * @return the next stage of the container registry source trigger definition.
             */
            SourceTriggerAttachable withTriggerEvent(SourceTriggerEvent sourceTriggerEvent);
        }

        /**
         * The stage of the container registry source trigger definition allowing to specify the branch of the
         * repository and authentication credentials if needed to interact with the source control repository.
         */
        interface RepositoryBranchAndAuth {
            /**
             * The function that specifies the branch of the respository to use.
             *
             * @param branch the repository branch.
             * @return the next stage of the container registry source trigger definition.
             */
            SourceTriggerAttachable withRepositoryBranch(String branch);

            /**
             * The function that allows the user to input the type of the token used for authentication and the token
             * itself to authenticate to the source control repository.
             *
             * @param tokenType the type of the token used to authenticate to the source control repository.
             * @param token the token used to authenticate to the source control repository.
             * @return the next stage of the container registry source trigger definition.
             */
            SourceTriggerAttachable withRepositoryAuthentication(TokenType tokenType, String token);

            /**
             * The function that allows the user to input the type of the token used for authentication and the token
             * itself to authenticate to the source control repository.
             *
             * @param tokenType the type of the token used to authenticate to the source control repository.
             * @param token the token used to authenticate to the source control repository.
             * @param refreshToken the token that is used to refresh the access token.
             * @param scope the scope of the access token.
             * @param expiresIn time in seconds that the token remains valid.
             * @return the next stage of the container registry source trigger definition.
             */
            SourceTriggerAttachable withRepositoryAuthentication(
                TokenType tokenType, String token, String refreshToken, String scope, int expiresIn);
        }

        /**
         * The stage of the container registry source trigger definition allowing to specify the status of the trigger.
         */
        interface TriggerStatusDefinition {
            /**
             * The function that sets the trigger status to be enabled.
             *
             * @return the next stage of the container registry source trigger definition.
             */
            SourceTriggerAttachable withTriggerStatusEnabled();

            /**
             * The function that sets the trigger status to be disabled.
             *
             * @return the next stage of the container registry source trigger definition.
             */
            SourceTriggerAttachable withTriggerStatusDisabled();

            /**
             * The function that allows the user to input the state of the trigger status.
             *
             * @param triggerStatus the user's choice for the trigger status.
             * @return the next stage of the container registry source trigger definition.
             */
            SourceTriggerAttachable withTriggerStatus(TriggerStatus triggerStatus);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be attached,
         * but also allows for any other optional settings to be specified.
         */
        interface SourceTriggerAttachable
            extends RepositoryBranchAndAuth,
                TriggerEventsDefinition,
                TriggerStatusDefinition,
                Attachable.InDefinition<RegistryTask.DefinitionStages.TaskCreatable> {
        }
    }

    /** Grouping of source trigger update stages. */
    interface UpdateStages {
        /** The stage of the container registry source trigger update allowing to specify the type of source control. */
        interface SourceControlType {
            /**
             * The function that specifies Github will be used as the type of source control.
             *
             * @return the next stage of the container registry source trigger definition.
             */
            Update withGithubAsSourceControl();

            /**
             * The function that specifies Azure DevOps will be used as the type of source control.
             *
             * @return the next stage of the container registry source trigger definition.
             */
            Update withAzureDevOpsAsSourceControl();

            /**
             * The function that allows the user to input their own kind of source control.
             *
             * @param sourceControl the source control the user wishes to use.
             * @return the next stage of the container registry source trigger definition.
             */
            Update withSourceControl(
                com.azure.resourcemanager.containerregistry.models.SourceControlType sourceControl);
        }

        /**
         * The stage of the container registry source trigger update allowing to specify the URL of the source control
         * repository.
         */
        interface RepositoryUrl {
            /**
             * The function that specifies the URL of the source control repository.
             *
             * @param sourceControlRepositoryUrl the URL of the source control repository.
             * @return the next stage of the container registry source trigger definition.
             */
            Update withSourceControlRepositoryUrl(String sourceControlRepositoryUrl);
        }

        /**
         * The stage of the container registry source trigger update allowing to specify the type of actions that will
         * trigger a run.
         */
        interface TriggerEventsDefinition {
            /**
             * The function that specifies a commit action will trigger a run.
             *
             * @return the next stage of the container registry source trigger definition.
             */
            Update withCommitTriggerEvent();

            /**
             * The function that specifies a pull action will trigger a run.
             *
             * @return the next stage of the container registry source trigger definition.
             */
            Update withPullTriggerEvent();

            /**
             * The function that allows the user to specify an action that will trigger a run when it is executed.
             *
             * @param sourceTriggerEvent the action that will trigger a run.
             * @return the next stage of the container registry source trigger definition.
             */
            Update withTriggerEvent(SourceTriggerEvent sourceTriggerEvent);
        }

        /**
         * The stage of the container registry source trigger update allowing to specify the branch of the repository
         * and authentication credentials if needed to interact with the source control repository.
         */
        interface RepositoryBranchAndAuth {
            /**
             * The function that specifies the branch of the respository to use.
             *
             * @param branch the repository branch.
             * @return the next stage of the container registry source trigger definition.
             */
            Update withRepositoryBranch(String branch);

            /**
             * The function that allows the user to input the type of the token used for authentication and the token
             * itself to authenticate to the source control repository.
             *
             * @param tokenType the type of the token used to authenticate to the source control repository.
             * @param token the token used to authenticate to the source control repository.
             * @return the next stage of the container registry source trigger definition.
             */
            Update withRepositoryAuthentication(TokenType tokenType, String token);

            /**
             * The function that allows the user to input the type of the token used for authentication and the token
             * itself to authenticate to the source control repository.
             *
             * @param tokenType the type of the token used to authenticate to the source control repository.
             * @param token the token used to authenticate to the source control repository.
             * @param refreshToken the token that is used to refresh the access token.
             * @param scope the scope of the access token.
             * @param expiresIn time in seconds that the token remains valid.
             * @return the next stage of the container registry source trigger definition.
             */
            Update withRepositoryAuthentication(
                TokenType tokenType, String token, String refreshToken, String scope, int expiresIn);
        }

        /** The stage of the container registry source trigger update allowing to specify the status of the trigger. */
        interface TriggerStatusDefinition {
            /**
             * The function that sets the trigger status to be enabled.
             *
             * @return the next stage of the container registry source trigger definition.
             */
            Update withTriggerStatusEnabled();

            /**
             * The function that sets the trigger status to be disabled.
             *
             * @return the next stage of the container registry source trigger definition.
             */
            Update withTriggerStatusDisabled();

            /**
             * The function that allows the user to input the state of the trigger status.
             *
             * @param triggerStatus the user's choice for the trigger status.
             * @return the next stage of the container registry source trigger definition.
             */
            Update withTriggerStatus(TriggerStatus triggerStatus);
        }
    }

    /** Grouping of source trigger update definition stages. */
    interface UpdateDefinitionStages {

        /** The first stage of a source trigger definition. */
        interface Blank {
            /**
             * The function that specifies Github will be used as the type of source control.
             *
             * @return the next stage of the container registry source trigger definition.
             */
            RepositoryUrl withGithubAsSourceControl();

            /**
             * The function that specifies Azure DevOps will be used as the type of source control.
             *
             * @return the next stage of the container registry source trigger definition.
             */
            RepositoryUrl withAzureDevOpsAsSourceControl();

            /**
             * The function that allows the user to input their own kind of source control.
             *
             * @param sourceControl the source control the user wishes to use.
             * @return the next stage of the container registry source trigger definition.
             */
            RepositoryUrl withSourceControl(SourceControlType sourceControl);
        }

        /**
         * The stage of the container registry source trigger definition allowing to specify the URL of the source
         * control repository.
         */
        interface RepositoryUrl {
            /**
             * The function that specifies the URL of the source control repository.
             *
             * @param sourceControlRepositoryUrl the URL of the source control repository.
             * @return the next stage of the container registry source trigger definition.
             */
            TriggerEventsDefinition withSourceControlRepositoryUrl(String sourceControlRepositoryUrl);
        }

        /**
         * The stage of the container registry source trigger definition allowing to specify the type of actions that
         * will trigger a run.
         */
        interface TriggerEventsDefinition {
            /**
             * The function that specifies a commit action will trigger a run.
             *
             * @return the next stage of the container registry source trigger definition.
             */
            SourceTriggerAttachable withCommitTriggerEvent();

            /**
             * The function that specifies a pull action will trigger a run.
             *
             * @return the next stage of the container registry source trigger definition.
             */
            SourceTriggerAttachable withPullTriggerEvent();

            /**
             * The function that allows the user to specify an action that will trigger a run when it is executed.
             *
             * @param sourceTriggerEvent the action that will trigger a run.
             * @return the next stage of the container registry source trigger definition.
             */
            SourceTriggerAttachable withTriggerEvent(SourceTriggerEvent sourceTriggerEvent);
        }

        /**
         * The stage of the container registry source trigger definition allowing to specify the branch of the
         * repository and authentication credentials if needed to interact with the source control repository.
         */
        interface RepositoryBranchAndAuth {
            /**
             * The function that specifies the branch of the respository to use.
             *
             * @param branch the repository branch.
             * @return the next stage of the container registry source trigger definition.
             */
            SourceTriggerAttachable withRepositoryBranch(String branch);

            /**
             * The function that allows the user to input the type of the token used for authentication and the token
             * itself to authenticate to the source control repository.
             *
             * @param tokenType the type of the token used to authenticate to the source control repository.
             * @param token the token used to authenticate to the source control repository.
             * @return the next stage of the container registry source trigger definition.
             */
            SourceTriggerAttachable withRepositoryAuthentication(TokenType tokenType, String token);

            /**
             * The function that allows the user to input the type of the token used for authentication and the token
             * itself to authenticate to the source control repository.
             *
             * @param tokenType the type of the token used to authenticate to the source control repository.
             * @param token the token used to authenticate to the source control repository.
             * @param refreshToken the token that is used to refresh the access token.
             * @param scope the scope of the access token.
             * @param expiresIn time in seconds that the token remains valid.
             * @return the next stage of the container registry source trigger definition.
             */
            SourceTriggerAttachable withRepositoryAuthentication(
                TokenType tokenType, String token, String refreshToken, String scope, int expiresIn);
        }

        /**
         * The stage of the container registry source trigger definition allowing to specify the status of the trigger.
         */
        interface TriggerStatusDefinition {
            /**
             * The function that sets the trigger status to be enabled.
             *
             * @return the next stage of the container registry source trigger definition.
             */
            SourceTriggerAttachable withTriggerStatusEnabled();

            /**
             * The function that sets the trigger status to be disabled.
             *
             * @return the next stage of the container registry source trigger definition.
             */
            SourceTriggerAttachable withTriggerStatusDisabled();

            /**
             * The function that allows the user to input the state of the trigger status.
             *
             * @param triggerStatus the user's choice for the trigger status.
             * @return the next stage of the container registry source trigger definition.
             */
            SourceTriggerAttachable withTriggerStatus(TriggerStatus triggerStatus);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be attached,
         * but also allows for any other optional settings to be specified.
         */
        interface SourceTriggerAttachable
            extends RepositoryBranchAndAuth,
                TriggerEventsDefinition,
                TriggerStatusDefinition,
                Attachable.InUpdate<RegistryTask.Update> {
        }
    }
}
