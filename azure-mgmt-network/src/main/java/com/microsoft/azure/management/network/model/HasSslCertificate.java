/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.model;

import java.io.File;

import com.microsoft.azure.management.apigeneration.Fluent;

/**
 * An interface representing a model's ability to reference an SSL certificate.
 * @param <T> the SSL certificate type
 */
@Fluent
public interface HasSslCertificate<T>  {
    /**
     * @return the associated SSL certificate, if any
     */
    T sslCertificate();

    /**
     * Grouping of definition stages involving specifying an SSL certificate.
     */
    interface DefinitionStages {
        /**
         * The stage of a resource definition allowing to specify the SSL certificate to associate with it.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithSslCertificate<ReturnT> {
            /**
             * Specifies an SSL certificate to associate with this listener, if its protocol is HTTPS.
             * @param name the name of an existing SSL certificate associated with this application gateway
             * @return the next stage of the definition
             */
            ReturnT withSslCertificate(String name);

            /**
             * Specifies the PFX file to import the SSL certificate from to associate with this resource.
             * <p>
             * The certificate will be named using an auto-generated name.
             * @param pfxFile an existing PFX file
             * @return the next stage of the definition
             */
            WithSslPassword<ReturnT> withSslCertificateFromPfxFile(File pfxFile);

            /**
             * Specifies the PFX file to import the SSL certificate from to associate with this resource.
             * @param pfxFile an existing PFX file
             * @param name a new name for the certificate that will be used to reference this certificate
             * @return the next stage of the definition
             */
            WithSslPassword<ReturnT> withSslCertificateFromPfxFile(File pfxFile, String name);
        }

        /**
         * The stage of a resource definition allowing to specify the password for the private key of the imported SSL certificate.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithSslPassword<ReturnT> {
            /**
             * Specifies the password for the specified PFX file containing the private key of the imported SSL certificate.
             * @param password the password of the imported PFX file
             * @return the next stage of the definition
             */
            ReturnT withSslCertificatePassword(String password);
        }
    }

    /**
     * Grouping of update stages involving modifying SSL certificates.
     */
    interface UpdateStages {
        /**
         * The stage of a resource definition allowing to specify the SSL certificate to associate with it.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithSslCertificate<ReturnT> {
            /**
             * Specifies an SSL certificate to associate with this listener, if its protocol is HTTPS.
             * @param name the name of an existing SSL certificate associated with this application gateway
             * @return the next stage of the definition
             */
            ReturnT withSslCertificate(String name);

            /**
             * Specifies the PFX file to import the SSL certificate from to associate with this resource.
             * <p>
             * The certificate will be named using an auto-generated name.
             * @param pfxFile an existing PFX file
             * @return the next stage of the definition
             */
            WithSslPassword<ReturnT> withSslCertificateFromPfxFile(File pfxFile);

            /**
             * Specifies the PFX file to import the SSL certificate from to associate with this resource.
             * @param pfxFile an existing PFX file
             * @param name a new name for the certificate that will be used to reference this certificate
             * @return the next stage of the definition
             */
            WithSslPassword<ReturnT> withSslCertificateFromPfxFile(File pfxFile, String name);
        }

        /**
         * The stage of a resource definition allowing to specify the password for the private key of the imported SSL certificate.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithSslPassword<ReturnT> {
            /**
             * Specifies the password for the specified PFX file containing the private key of the imported SSL certificate.
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
         * @param <ReturnT> the next stage of the definition
         */
        interface WithSslCertificate<ReturnT> {
            /**
             * Specifies an SSL certificate to associate with this listener, if its protocol is HTTPS.
             * @param name the name of an existing SSL certificate associated with this application gateway
             * @return the next stage of the definition
             */
            ReturnT withSslCertificate(String name);

            /**
             * Specifies the PFX file to import the SSL certificate from to associate with this resource.
             * <p>
             * The certificate will be named using an auto-generated name.
             * @param pfxFile an existing PFX file
             * @return the next stage of the definition
             */
            WithSslPassword<ReturnT> withSslCertificateFromPfxFile(File pfxFile);

            /**
             * Specifies the PFX file to import the SSL certificate from to associate with this resource.
             * @param pfxFile an existing PFX file
             * @param name a new name for the certificate that will be used to reference this certificate
             * @return the next stage of the definition
             */
            WithSslPassword<ReturnT> withSslCertificateFromPfxFile(File pfxFile, String name);
        }

        /**
         * The stage of a resource definition allowing to specify the password for the private key of the imported SSL certificate.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithSslPassword<ReturnT> {
            /**
             * Specifies the password for the specified PFX file containing the private key of the imported SSL certificate.
             * @param password the password of the imported PFX file
             * @return the next stage of the definition
             */
            ReturnT withSslCertificatePassword(String password);
        }
    }
}
