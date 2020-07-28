// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.authorization.fluent.inner.ApplicationInner;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** An immutable client-side representation of an Azure AD application. */
@Fluent
public interface ActiveDirectoryApplication
    extends ActiveDirectoryObject, HasInner<ApplicationInner>, Updatable<ActiveDirectoryApplication.Update> {
    /** @return the application ID */
    String applicationId();

    /** @return the application permissions */
    List<String> applicationPermissions();

    /** @return whether the application is be available to other tenants */
    boolean availableToOtherTenants();

    /** @return a collection of URIs for the application */
    Set<String> identifierUris();

    /** @return a collection of reply URLs for the application */
    Set<String> replyUrls();

    /** @return the home page of the application */
    URL signOnUrl();

    /** @return the mapping of password credentials from their names */
    Map<String, PasswordCredential> passwordCredentials();

    /** @return the mapping of certificate credentials from their names */
    Map<String, CertificateCredential> certificateCredentials();

    /**************************************************************
     * Fluent interfaces to provision an application
     **************************************************************/

    /** Container interface for all the definitions that need to be implemented. */
    interface Definition extends DefinitionStages.Blank, DefinitionStages.WithCreate {
    }

    /** Grouping of all the application definition stages. */
    interface DefinitionStages {
        /** The first stage of the application definition. */
        interface Blank extends WithSignOnUrl {
        }

        /** The stage of application definition allowing specifying the sign on URL. */
        interface WithSignOnUrl {
            /**
             * Specifies the sign on URL.
             *
             * @param signOnUrl the URL where users can sign in and use this app
             * @return the next stage in application definition
             */
            WithCreate withSignOnUrl(String signOnUrl);
        }

        /** The stage of application definition allowing specifying reply URLs. */
        interface WithReplyUrl {
            /**
             * Adds a reply URL to the application.
             *
             * @param replyUrl URIs to which Azure AD will redirect in response to an OAuth 2.0 request
             * @return the next stage in application definition
             */
            WithCreate withReplyUrl(String replyUrl);
        }

        /** The stage of application definition allowing specifying identifier URLs. */
        interface WithIdentifierUrl {
            /**
             * Adds an identifier URL to the application.
             *
             * @param identifierUrl unique URI that Azure AD can use for this app
             * @return the next stage in application definition
             */
            WithCreate withIdentifierUrl(String identifierUrl);
        }

        /** The stage of application definition allowing specifying identifier keys. */
        interface WithCredential {
            /**
             * Starts the definition of a certificate credential.
             *
             * @param name the descriptive name of the certificate credential
             * @return the first stage in certificate credential definition
             */
            CertificateCredential.DefinitionStages.Blank<? extends WithCreate> defineCertificateCredential(String name);

            /**
             * Starts the definition of a password credential.
             *
             * @param name the descriptive name of the password credential
             * @return the first stage in password credential definition
             */
            PasswordCredential.DefinitionStages.Blank<? extends WithCreate> definePasswordCredential(String name);
        }

        /**
         * The stage of application definition allowing specifying if the application can be used in multiple tenants.
         */
        interface WithMultiTenant {
            /**
             * Specifies if the application can be used in multiple tenants.
             *
             * @param availableToOtherTenants true if this application is available in other tenants
             * @return the next stage in application definition
             */
            WithCreate withAvailableToOtherTenants(boolean availableToOtherTenants);
        }

        /**
         * An application definition with sufficient inputs to create a new application in the cloud, but exposing
         * additional optional inputs to specify.
         */
        interface WithCreate
            extends Creatable<ActiveDirectoryApplication>,
                WithIdentifierUrl,
                WithReplyUrl,
                WithCredential,
                WithMultiTenant {
        }
    }

    /** Grouping of all the application update stages. */
    interface UpdateStages {
        /** The stage of application update allowing specifying the sign on URL. */
        interface WithSignOnUrl {
            /**
             * Specifies the sign on URL.
             *
             * @param signOnUrl the URL where users can sign in and use this app
             * @return the next stage in application update
             */
            Update withSignOnUrl(String signOnUrl);
        }

        /** The stage of application update allowing specifying reply URLs. */
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

        /** The stage of application update allowing specifying identifier URLs. */
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

        /** The stage of application update allowing specifying identifier keys. */
        interface WithCredential {
            /**
             * Starts the definition of a certificate credential.
             *
             * @param name the descriptive name of the certificate credential
             * @return the first stage in certificate credential definition
             */
            CertificateCredential.UpdateDefinitionStages.Blank<? extends Update>
                defineCertificateCredential(String name);

            /**
             * Starts the definition of a password credential.
             *
             * @param name the descriptive name of the password credential
             * @return the first stage in password credential definition
             */
            PasswordCredential.UpdateDefinitionStages.Blank<? extends Update> definePasswordCredential(String name);

            /**
             * Removes a key.
             *
             * @param name the name of the key
             * @return the next stage of the application update
             */
            Update withoutCredential(String name);
        }

        /** The stage of application update allowing specifying if the application can be used in multiple tenants. */
        interface WithMultiTenant {
            /**
             * Specifies if the application can be used in multiple tenants.
             *
             * @param availableToOtherTenants true if this application is available in other tenants
             * @return the next stage in application update
             */
            Update withAvailableToOtherTenants(boolean availableToOtherTenants);
        }
    }

    /** The template for an application update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<ActiveDirectoryApplication>,
            UpdateStages.WithSignOnUrl,
            UpdateStages.WithIdentifierUrl,
            UpdateStages.WithReplyUrl,
            UpdateStages.WithCredential,
            UpdateStages.WithMultiTenant {
    }
}
