// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.authorization.fluent.inner.KeyCredentialInner;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import java.io.OutputStream;
import java.time.Duration;
import java.time.OffsetDateTime;

/** An immutable client-side representation of an Azure AD credential. */
@Fluent
public interface CertificateCredential extends Credential, HasInner<KeyCredentialInner> {

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
            DefinitionStages.WithCertificateType<ParentT>,
            DefinitionStages.WithPublicKey<ParentT>,
            DefinitionStages.WithSymmetricKey<ParentT>,
            DefinitionStages.WithAttach<ParentT>,
            DefinitionStages.WithAuthFileCertificate<ParentT>,
            DefinitionStages.WithAuthFileCertificatePassword<ParentT> {
    }

    /** Grouping of credential definition stages applicable as part of a application or service principal creation. */
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
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithCertificateType<ParentT> {
            /**
             * Specifies the type of the certificate to be Asymmetric X509.
             *
             * @return the next stage in credential definition
             */
            WithPublicKey<ParentT> withAsymmetricX509Certificate();

            /**
             * Specifies the type of the certificate to be symmetric.
             *
             * @return the next stage in credential definition
             */
            WithSymmetricKey<ParentT> withSymmetricEncryption();
        }

        /**
         * The credential definition stage allowing the public key to be set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithPublicKey<ParentT> {
            /**
             * Specifies the public key for an asymmetric X509 certificate.
             *
             * @param certificate the certificate content
             * @return the next stage in credential definition
             */
            WithAttach<ParentT> withPublicKey(byte[] certificate);
        }

        /**
         * The credential definition stage allowing the secret key to be set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSymmetricKey<ParentT> {
            /**
             * Specifies the secret key for a symmetric encryption.
             *
             * @param secret the secret key content
             * @return the next stage in credential definition
             */
            WithAttach<ParentT> withSecretKey(byte[] secret);
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

        /** A credential definition stage allowing exporting the auth file for the service principal. */
        interface WithAuthFile<ParentT> {
            /**
             * Export the information of this service principal into an auth file.
             *
             * @param outputStream the output stream to export the file
             * @return the next stage in credential definition
             */
            WithAuthFileCertificate<ParentT> withAuthFileToExport(OutputStream outputStream);
        }

        /** A credential definition stage allowing specifying the private key for exporting an auth file. */
        interface WithAuthFileCertificate<ParentT> {
            /**
             * Export the information of this service principal into an auth file.
             *
             * @param privateKeyPath the path to the private key file
             * @return the next stage in credential definition
             */
            WithAuthFileCertificatePassword<ParentT> withPrivateKeyFile(String privateKeyPath);
        }

        /**
         * A credential definition stage allowing specifying the password for the private key for exporting an auth
         * file.
         */
        interface WithAuthFileCertificatePassword<ParentT> {
            /**
             * Export the information of this service principal into an auth file.
             *
             * @param privateKeyPassword the password for the private key
             * @return the next stage in credential definition
             */
            WithAttach<ParentT> withPrivateKeyPassword(String privateKeyPassword);
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
                WithAuthFile<ParentT> {
        }
    }

    /**
     * The entirety of a credential definition as part of a application or service principal update.
     *
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithCertificateType<ParentT>,
            UpdateDefinitionStages.WithPublicKey<ParentT>,
            UpdateDefinitionStages.WithSymmetricKey<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT>,
            UpdateDefinitionStages.WithAuthFileCertificate<ParentT>,
            UpdateDefinitionStages.WithAuthFileCertificatePassword<ParentT> {
    }

    /** Grouping of credential definition stages applicable as part of a application or service principal update. */
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
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithCertificateType<ParentT> {
            /**
             * Specifies the type of the certificate to be asymmetric X509.
             *
             * @return the next stage in credential definition
             */
            WithPublicKey<ParentT> withAsymmetricX509Certificate();

            /**
             * Specifies the type of the certificate to be symmetric.
             *
             * @return the next stage in credential definition
             */
            WithSymmetricKey<ParentT> withSymmetricEncryption();
        }

        /**
         * The credential definition stage allowing the public key to be set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithPublicKey<ParentT> {
            /**
             * Specifies the public key for an asymmetric X509 certificate.
             *
             * @param certificate the certificate content
             * @return the next stage in credential definition
             */
            WithAttach<ParentT> withPublicKey(byte[] certificate);
        }

        /**
         * The credential definition stage allowing the secret key to be set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSymmetricKey<ParentT> {
            /**
             * Specifies the secret key for a symmetric encryption.
             *
             * @param secret the secret key content
             * @return the next stage in credential definition
             */
            WithAttach<ParentT> withSecretKey(byte[] secret);
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

        /** A credential definition stage allowing exporting the auth file for the service principal. */
        interface WithAuthFile<ParentT> {
            /**
             * Export the information of this service principal into an auth file.
             *
             * @param outputStream the output stream to export the file
             * @return the next stage in credential definition
             */
            WithAuthFileCertificate<ParentT> withAuthFileToExport(OutputStream outputStream);
        }

        /** A credential definition stage allowing specifying the private key for exporting an auth file. */
        interface WithAuthFileCertificate<ParentT> {
            /**
             * Export the information of this service principal into an auth file.
             *
             * @param privateKeyPath the path to the private key file
             * @return the next stage in credential definition
             */
            WithAuthFileCertificatePassword<ParentT> withPrivateKeyFile(String privateKeyPath);
        }

        /**
         * A credential definition stage allowing specifying the password for the private key for exporting an auth
         * file.
         */
        interface WithAuthFileCertificatePassword<ParentT> {
            /**
             * Export the information of this service principal into an auth file.
             *
             * @param privateKeyPassword the password for the private key
             * @return the next stage in credential definition
             */
            WithAttach<ParentT> withPrivateKeyPassword(String privateKeyPassword);
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
            extends Attachable.InUpdate<ParentT>, WithStartDate<ParentT>, WithDuration<ParentT>, WithAuthFile<ParentT> {
        }
    }
}
