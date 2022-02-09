// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.authorization.fluent.models.MicrosoftGraphPasswordCredentialInner;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

import java.io.OutputStream;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.function.Consumer;

/** An immutable client-side representation of an Azure AD credential. */
@Fluent
public interface PasswordCredential extends Credential, HasInnerModel<MicrosoftGraphPasswordCredentialInner> {

    /**************************************************************
     * Fluent interfaces to attach a credential
     **************************************************************/

    /**
     * The entirety of a credential definition.
     *
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithSubscriptionInAuthFile<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of credential definition stages applicable as part of a application or service principal creation. */
    interface DefinitionStages {
        /**
         * The first stage of a credential definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithAttach<ParentT> {
        }

        /**
         * The credential definition stage allowing start date to be set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithStartDate<ParentT> {
            /**
             * Specifies the start date after which password or key would be valid. Default value is current time.
             *
             * @param startDate the start date for validity
             * @return the next stage in credential definition
             */
            WithAttach<ParentT> withStartDate(OffsetDateTime startDate);
        }

        /**
         * The credential definition stage allowing the duration of key validity to be set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithDuration<ParentT> {
            /**
             * Specifies the duration for which password or key would be valid. Default value is 1 year.
             *
             * @param duration the duration of validity
             * @return the next stage in credential definition
             */
            WithAttach<ParentT> withDuration(Duration duration);
        }

        /**
         * A credential definition stage allowing exporting the auth file for the service principal.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAuthFile<ParentT> {
            /**
             * Export the information of this service principal into an auth file.
             *
             * @param outputStream the output stream to export the file
             * @return the next stage in credential definition
             * @deprecated azure-identity doesn't accept auth file anymore. use {@link WithConsumer} to get the secret.
             */
            @Deprecated
            WithSubscriptionInAuthFile<ParentT> withAuthFileToExport(OutputStream outputStream);
        }

        /**
         * A credential definition stage allowing the subscription in the auth file to be set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSubscriptionInAuthFile<ParentT> {
            /**
             * Specifies the "subscription=" field in the auth file.
             *
             * @param subscriptionId the UUID of the subscription
             * @return the next stage in credential definition
             */
            WithAttach<ParentT> withSubscriptionId(String subscriptionId);
        }

        /**
         * A credential definition stage allowing consuming the credential after creation.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithConsumer<ParentT> {
            /**
             * Consumes the credential after creation.
             * Note: it is the only way to get secret from the credential.
             * @param passwordConsumer a consumer to consume password credential
             * @return the next stage in credential definition
             */
            WithAttach<ParentT> withPasswordConsumer(Consumer<? super PasswordCredential> passwordConsumer);
        }

        /**
         * The final stage of the credential definition.
         *
         * <p>At this stage, more settings can be specified, or the credential definition can be attached to the parent
         * application / service principal definition using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT>
            extends Attachable.InDefinition<ParentT>,
                WithStartDate<ParentT>,
                WithDuration<ParentT>,
                WithConsumer<ParentT>,
                WithAuthFile<ParentT> {
        }
    }
}
