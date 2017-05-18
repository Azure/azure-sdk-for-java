/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Beta.SinceVersion;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.graphrbac.implementation.KeyCredentialInner;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 * An immutable client-side representation of an Azure AD credential.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.Fluent.Graph.RBAC")
@Beta(SinceVersion.V1_1_0)
public interface CertificateCredential extends
        Credential,
        HasInner<KeyCredentialInner> {

    /**************************************************************
     * Fluent interfaces to attach a credential
     **************************************************************/

    /**
     * The entirety of a credential definition.
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT> extends
            DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithCertificateType<ParentT>,
            DefinitionStages.WithPublicKey<ParentT>,
            DefinitionStages.WithSymmetricKey<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of credential definition stages applicable as part of a application or service principal creation.
     */
    interface DefinitionStages {
        /**
         * The first stage of a credential definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithCertificateType<ParentT> {
        }

        /**
         * The credential definition stage allowing the certificate type to be set.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithCertificateType<ParentT> {
            /**
             * Specifies the type of the certificate to be Asymmetric X509.
             * @return the next stage in credential definition
             */
            WithPublicKey<ParentT> withAsymmetricX509Certificate();

            /**
             * Specifies the type of the certificate to be symmetric.
             * @return the next stage in credential definition
             */
            WithSymmetricKey<ParentT> withSymmetricEncryption();
        }

        /**
         * The credential definition stage allowing the public key to be set.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithPublicKey<ParentT> {
            /**
             * Specifies the public key for an asymmetric X509 certificate.
             * @param certificate the certificate content
             * @return the next stage in credential definition
             */
            WithAttach<ParentT> withPublicKey(byte[] certificate);
        }

        /**
         * The credential definition stage allowing the secret key to be set.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSymmetricKey<ParentT> {
            /**
             * Specifies the secret key for a symmetric encryption.
             * @param secret the secret key content
             * @return the next stage in credential definition
             */
            WithAttach<ParentT> withSecretKey(byte[] secret);
        }

        /**
         * The credential definition stage allowing start date to be set.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithStartDate<ParentT> {
            /**
             * Specifies the start date after which password or key would be valid. Default value is current time.
             * @param startDate the start date for validity
             * @return the next stage in credential definition
             */
            WithAttach<ParentT> withStartDate(DateTime startDate);
        }

        /**
         * The credential definition stage allowing the duration of key validity to be set.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithDuration<ParentT> {
            /**
             * Specifies the duration for which password or key would be valid. Default value is 1 year.
             * @param duration the duration of validity
             * @return the next stage in credential definition
             */
            WithAttach<ParentT> withDuration(Duration duration);
        }

        /** The final stage of the credential definition.
         * <p>
         * At this stage, more settings can be specified, or the credential definition can be
         * attached to the parent application / service principal definition
         * using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
                Attachable.InDefinition<ParentT>,
                WithStartDate<ParentT>,
                WithDuration<ParentT> {
        }
    }

    /**
     * The entirety of a credential definition as part of a application or service principal update.
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT> extends
            UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithCertificateType<ParentT>,
            UpdateDefinitionStages.WithPublicKey<ParentT>,
            UpdateDefinitionStages.WithSymmetricKey<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of credential definition stages applicable as part of a application or service principal update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a credential definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithCertificateType<ParentT> {
        }

        /**
         * The credential definition stage allowing the certificate type to be set.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithCertificateType<ParentT> {
            /**
             * Specifies the type of the certificate to be asymmetric X509.
             * @return the next stage in credential definition
             */
            WithPublicKey<ParentT> withAsymmetricX509Certificate();

            /**
             * Specifies the type of the certificate to be symmetric.
             * @return the next stage in credential definition
             */
            WithSymmetricKey<ParentT> withSymmetricEncryption();
        }

        /**
         * The credential definition stage allowing the public key to be set.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithPublicKey<ParentT> {
            /**
             * Specifies the public key for an asymmetric X509 certificate.
             * @param certificate the certificate content
             * @return the next stage in credential definition
             */
            WithAttach<ParentT> withPublicKey(byte[] certificate);
        }

        /**
         * The credential definition stage allowing the secret key to be set.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSymmetricKey<ParentT> {
            /**
             * Specifies the secret key for a symmetric encryption.
             * @param secret the secret key content
             * @return the next stage in credential definition
             */
            WithAttach<ParentT> withSecretKey(byte[] secret);
        }

        /**
         * The credential definition stage allowing start date to be set.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithStartDate<ParentT> {
            /**
             * Specifies the start date after which password or key would be valid. Default value is current time.
             * @param startDate the start date for validity
             * @return the next stage in credential definition
             */
            WithAttach<ParentT> withStartDate(DateTime startDate);
        }

        /**
         * The credential definition stage allowing the duration of key validity to be set.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithDuration<ParentT> {
            /**
             * Specifies the duration for which password or key would be valid. Default value is 1 year.
             * @param duration the duration of validity
             * @return the next stage in credential definition
             */
            WithAttach<ParentT> withDuration(Duration duration);
        }

        /** The final stage of the credential definition.
         * <p>
         * At this stage, more settings can be specified, or the credential definition can be
         * attached to the parent application / service principal definition
         * using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
                Attachable.InUpdate<ParentT>,
                WithStartDate<ParentT>,
                WithDuration<ParentT> {
        }
    }
}
