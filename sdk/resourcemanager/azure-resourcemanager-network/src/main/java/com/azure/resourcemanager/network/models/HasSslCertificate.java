// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import java.io.File;
import java.io.IOException;

/**
 * An interface representing a model's ability to reference an SSL certificate.
 *
 * @param <T> the SSL certificate type
 */
@Fluent
public interface HasSslCertificate<T> {
    /** @return the associated SSL certificate, if any */
    T sslCertificate();

    /** Grouping of definition stages involving specifying an SSL certificate. */
    interface DefinitionStages {
        /**
         * The stage of a resource definition allowing to specify the SSL certificate to associate with it.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithSslCertificate<ReturnT> {
            /**
             * Specifies an SSL certificate to associate with this resource.
             *
             * <p>If the certificate does not exist yet, it must be defined in the optional part of the parent resource
             * definition.
             *
             * @param name the name of an existing SSL certificate.
             * @return the next stage of the definition
             */
            ReturnT withSslCertificate(String name);

            /**
             * Sepecifies the content of the private key using key vault.
             *
             * @param keyVaultSecretId the secret id of key vault
             * @return the next stage of the definition
             */
            ReturnT withSslCertificateFromKeyVaultSecretId(String keyVaultSecretId);

            /**
             * Specifies the PFX file to import the SSL certificate from to associated with this resource.
             *
             * <p>The certificate will be named using an auto-generated name.
             *
             * @param pfxFile an existing PFX file
             * @return the next stage of the definition
             * @throws IOException when there are issues with the provided file
             */
            WithSslPassword<ReturnT> withSslCertificateFromPfxFile(File pfxFile) throws IOException;
        }

        /**
         * The stage of a resource definition allowing to specify the password for the private key of the imported SSL
         * certificate.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithSslPassword<ReturnT> {
            /**
             * Specifies the password for the specified PFX file containing the private key of the imported SSL
             * certificate.
             *
             * @param password the password of the imported PFX file
             * @return the next stage of the definition
             */
            ReturnT withSslCertificatePassword(String password);
        }
    }

    /** Grouping of update stages involving modifying SSL certificates. */
    interface UpdateStages {
        /**
         * The stage of a resource update allowing to specify the SSL certificate to associate with it.
         *
         * @param <ReturnT> the next stage of the update
         */
        interface WithSslCertificate<ReturnT> {
            /**
             * Specifies an SSL certificate to associate with this resource.
             *
             * <p>If the certificate does not exist yet, it must be defined in the parent resource update.
             *
             * @param name the name of an existing SSL certificate associated with this application gateway
             * @return the next stage of the update
             */
            ReturnT withSslCertificate(String name);

            /**
             * Sepecifies the content of the private key using key vault.
             *
             * @param keyVaultSecretId the secret id of key vault
             * @return the next stage of the update
             */
            ReturnT withSslCertificateFromKeyVaultSecretId(String keyVaultSecretId);

            /**
             * Specifies the PFX file to import the SSL certificate from to associate with this resource.
             *
             * <p>The certificate will be named using an auto-generated name.
             *
             * @param pfxFile an existing PFX file
             * @return the next stage of the definition
             * @throws IOException when there are issues with the provided file
             */
            WithSslPassword<ReturnT> withSslCertificateFromPfxFile(File pfxFile) throws IOException;
        }

        /**
         * The stage of a resource update allowing to specify the password for the private key of the imported SSL
         * certificate.
         *
         * @param <ReturnT> the next stage of the update
         */
        interface WithSslPassword<ReturnT> {
            /**
             * Specifies the password for the specified PFX file containing the private key of the imported SSL
             * certificate.
             *
             * @param password the password of the imported PFX file
             * @return the next stage of the definition
             */
            ReturnT withSslCertificatePassword(String password);
        }
    }

    /**
     * Grouping of definition stages applicable as part of a resource update, involving modifying the SSL certificates.
     */
    interface UpdateDefinitionStages {
        /**
         * The stage of a resource definition allowing to specify the SSL certificate to associate with it.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithSslCertificate<ReturnT> {
            /**
             * Specifies an SSL certificate to associate with this resource.
             *
             * <p>If the certificate does not exist yet, it must be defined in the optional part of the parent resource
             * definition.
             *
             * @param name the name of an existing SSL certificate.
             * @return the next stage of the definition
             */
            ReturnT withSslCertificate(String name);

            /**
             * Sepecifies the content of the private key using key vault.
             *
             * @param keyVaultSecretId the secret id of key vault
             * @return the next stage of the definition
             */
            ReturnT withSslCertificateFromKeyVaultSecretId(String keyVaultSecretId);

            /**
             * Specifies the PFX file to import the SSL certificate from to associated with this resource.
             *
             * <p>The certificate will be named using an auto-generated name.
             *
             * @param pfxFile an existing PFX file
             * @return the next stage of the definition
             * @throws IOException when there are issues with the provided file
             */
            WithSslPassword<ReturnT> withSslCertificateFromPfxFile(File pfxFile) throws IOException;
        }

        /**
         * The stage of a resource definition allowing to specify the password for the private key of the imported SSL
         * certificate.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithSslPassword<ReturnT> {
            /**
             * Specifies the password for the specified PFX file containing the private key of the imported SSL
             * certificate.
             *
             * @param password the password of the imported PFX file
             * @return the next stage of the definition
             */
            ReturnT withSslCertificatePassword(String password);
        }
    }
}
