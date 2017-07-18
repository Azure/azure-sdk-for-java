/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Beta.SinceVersion;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.graphrbac.implementation.ApplicationInner;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * An immutable client-side representation of an Azure AD application.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
@Beta(SinceVersion.V1_1_0)
public interface ActiveDirectoryApplication extends
        ActiveDirectoryObject,
        HasInner<ApplicationInner>,
        Updatable<ActiveDirectoryApplication.Update> {
    /**
     * @return the application ID
     */
    String applicationId();

    /**
     * @return the application permissions
     */
    List<String> applicationPermissions();

    /**
     * @return whether the application is be available to other tenants
     */
    boolean availableToOtherTenants();

    /**
     * @return a collection of URIs for the application
     */
    Set<String> identifierUris();

    /**
     * @return a collection of reply URLs for the application
     */
    Set<String> replyUrls();

    /**
     * @return the home page of the application
     */
    URL signOnUrl();

    /**
     * @return the set of password credentials
     */
    Set<PasswordCredential> passwordCredentials();

    /**
     * @return the set of certificate credentials
     */
    Set<CertificateCredential> certificateCredentials();

    /**************************************************************
     * Fluent interfaces to provision an application
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the application definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the application definition.
         */
        interface Blank extends WithSignOnUrl {
        }

        /**
         * The stage of application definition allowing specifying the sign on URL.
         */
        interface WithSignOnUrl {
            /**
             * Specifies the sign on URL.
             *
             * @param signOnUrl the URL where users can sign in and use this app
             * @return the next stage in application definition
             */
            WithCreate withSignOnUrl(String signOnUrl);
        }

        /**
         * The stage of application definition allowing specifying reply URLs.
         */
        interface WithReplyUrl {
            /**
             * Adds a reply URL to the application.
             *
             * @param replyUrl URIs to which Azure AD will redirect in response to an OAuth 2.0 request
             * @return the next stage in application definition
             */
            WithCreate withReplyUrl(String replyUrl);
        }

        /**
         * The stage of application definition allowing specifying identifier URLs.
         */
        interface WithIdentifierUrl {
            /**
             * Adds an identifier URL to the application.
             *
             * @param identifierUrl unique URI that Azure AD can use for this app
             * @return the next stage in application definition
             */
            WithCreate withIdentifierUrl(String identifierUrl);
        }

        /**
         * The stage of application definition allowing specifying identifier keys.
         */
        interface WithCredential {
            /**
             * Starts the definition of a certificate credential.
             * @return the first stage in certificate credential definition
             */
            CertificateCredential.DefinitionStages.Blank<WithCreate> defineCertificateCredential();

            /**
             * Starts the definition of a password credential.
             * @return the first stage in password credential definition
             */
            PasswordCredential.DefinitionStages.Blank<WithCreate> definePasswordCredential();
        }

        /**
         * The stage of application definition allowing specifying if the application can be used in multiple tenants.
         */
        interface WithMultiTenant {
            /**
             * Specifies if the application can be used in multiple tenants.
             * @param availableToOtherTenants true if this application is available in other tenants
             * @return the next stage in application definition
             */
            WithCreate withAvailableToOtherTenants(boolean availableToOtherTenants);
        }

        /**
         * An application definition with sufficient inputs to create a new
         * application in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                Creatable<ActiveDirectoryApplication>,
                WithIdentifierUrl,
                WithReplyUrl,
                WithCredential,
                WithMultiTenant {
        }
    }

    /**
     * Grouping of all the application update stages.
     */
    interface UpdateStages {
        /**
         * The stage of application update allowing specifying the sign on URL.
         */
        interface WithSignOnUrl {
            /**
             * Specifies the sign on URL.
             *
             * @param signOnUrl the URL where users can sign in and use this app
             * @return the next stage in application update
             */
            Update withSignOnUrl(String signOnUrl);
        }

        /**
         * The stage of application update allowing specifying reply URLs.
         */
        interface WithReplyUrl {
            /**
             * Adds a reply URL to the application.
             *
             * @param replyUrl URIs to which Azure AD will redirect in response to an OAuth 2.0 request
             * @return the next stage in application update
             */
            Update withReplyUrl(String replyUrl);

            /**
             * Removes a reply URL.
             *
             * @param replyUrl the reply URL to remove
             * @return the next stage in application update
             */
            Update withoutReplyUrl(String replyUrl);
        }

        /**
         * The stage of application update allowing specifying identifier URLs.
         */
        interface WithIdentifierUrl {
            /**
             * Adds an identifier URL to the application.
             *
             * @param identifierUrl unique URI that Azure AD can use for this app
             * @return the next stage in application update
             */
            Update withIdentifierUrl(String identifierUrl);

            /**
             * Removes an identifier URL from the application.
             *
             * @param identifierUrl identifier URI to remove
             * @return the next stage in application update
             */
            Update withoutIdentifierUrl(String identifierUrl);
        }

        /**
         * The stage of application update allowing specifying identifier keys.
         */
        interface WithCredential {
            /**
             * Starts the definition of a certificate credential.
             * @return the first stage in certificate credential definition
             */
            CertificateCredential.UpdateDefinitionStages.Blank<Update> defineCertificateCredential();

            /**
             * Starts the definition of a password credential.
             * @return the first stage in password credential definition
             */
            PasswordCredential.UpdateDefinitionStages.Blank<Update> definePasswordCredential();

            /**
             * Removes a credential.
             * @param credential the credential to remove
             * @return the next stage of the application update
             */
            Update withoutCredential(Credential credential);
        }

        /**
         * The stage of application update allowing specifying if the application can be used in multiple tenants.
         */
        interface WithMultiTenant {
            /**
             * Specifies if the application can be used in multiple tenants.
             * @param availableToOtherTenants true if this application is available in other tenants
             * @return the next stage in application update
             */
            Update withAvailableToOtherTenants(boolean availableToOtherTenants);
        }
    }

    /**
     * The template for an application update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<ActiveDirectoryApplication>,
            UpdateStages.WithSignOnUrl,
            UpdateStages.WithIdentifierUrl,
            UpdateStages.WithReplyUrl,
            UpdateStages.WithCredential,
            UpdateStages.WithMultiTenant {
    }
}
