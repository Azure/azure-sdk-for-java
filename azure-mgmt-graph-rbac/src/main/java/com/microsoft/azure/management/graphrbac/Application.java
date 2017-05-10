/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.graphrbac.implementation.ApplicationInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasId;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import org.joda.time.LocalDate;

import java.util.List;

/**
 * An immutable client-side representation of an Azure AD application.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.Fluent.Graph.RBAC")
@Beta
public interface Application extends
        Indexable,
        HasInner<ApplicationInner>,
        HasId,
        HasName {
    /**
     * @return the object type
     */
    String objectType();

    /**
     * @return the application ID
     */
    String appId();

    /**
     * @return the application permissions
     */
    List<String> appPermissions();

    /**
     * @return whether the application is be available to other tenants
     */
    boolean availableToOtherTenants();

    /**
     * @return a collection of URIs for the application
     */
    List<String> identifierUris();

    /**
     * @return a collection of reply URLs for the application
     */
    List<String> replyUrls();

    /**
     * @return the home page of the application
     */
    String homepage();

    /**************************************************************
     * Fluent interfaces to provision an application
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithCreate,
            DefinitionStages.WithValidTime {
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
             * @param identifierUrl unique URIs that Azure AD can use for this app
             * @return the next stage in application definition
             */
            WithCreate withIdentifierUrl(String identifierUrl);
        }

        /**
         * The stage of application definition allowing specifying identifier keys.
         */
        interface WithKey {
            /**
             * Use a password as a key.
             * @param password the password value
             * @return the next stage in application definition
             */
            WithValidTime withPassword(String password);

            /**
             * Use a self signed certificate as a key.
             * @param pfxBlob the pfx certificate content
             * @return the next stage in application definition
             */
            WithValidTime withCertificate(byte[] pfxBlob);

            Credential.DefinitionStages.Blank<WithCreate> defineKey();
        }

        /**
         * The stage of application definition allowing specifying when the key will be valid.
         */
        interface WithValidTime extends WithCreate {
            /**
             * Specifies the start date after which password or key would be valid. Default value is current time.
             * @param startDate the start date for validity
             * @return the next stage in application definition
             */
            WithValidTime withStartDate(LocalDate startDate);
            WithValidTime withEndDate(LocalDate endDate);
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
            WithCreate withMultiTenant(boolean availableToOtherTenants);
        }

        /**
         * An application definition with sufficient inputs to create a new
         * application in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                Creatable<Application>,
                WithIdentifierUrl,
                WithReplyUrl,
                WithKey,
                WithMultiTenant {
        }
    }

    /**
     * Grouping of all the application update stages.
     */
    interface UpdateStages {
    }

    /**
     * The template for an application update operation, containing all the settings that can be modified.
     */
    interface Update {
    }
}
