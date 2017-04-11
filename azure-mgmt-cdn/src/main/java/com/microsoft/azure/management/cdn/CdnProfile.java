/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.cdn;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.cdn.implementation.CdnManager;
import com.microsoft.azure.management.cdn.implementation.ProfileInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;

import java.util.List;
import java.util.Map;

/**
 * An immutable client-side representation of an Azure CDN profile.
 */
@Fluent
public interface CdnProfile extends
        GroupableResource<CdnManager, ProfileInner>,
        Refreshable<CdnProfile>,
        Updatable<CdnProfile.Update> {

    /**
     * @return the SKU of the CDN profile
     */
    Sku sku();

    /**
     * @return CDN profile state
     */
    String resourceState();

    /**
     * @return endpoints in the CDN manager profile, indexed by name
     */
    Map<String, CdnEndpoint> endpoints();

    /**
     * Generates a dynamic SSO URI used to sign in to the CDN supplemental portal used for advanced management tasks.
     *
     * @return URI used to login to the third party web portal
     */
    @Method
    String generateSsoUri();

    /**
     * Asynchronously generates a dynamic SSO URI used to sign into the CDN supplemental portal used for advanced management tasks.
     *
     * @return Observable to URI used to login to third party web portal
     */
    @Method
    @Beta
    Observable<String> generateSsoUriAsync();

    /**
     * Asynchronously generates a dynamic SSO URI used to sign in to the CDN supplemental portal used for advanced management tasks.
     *
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    @Method
    @Beta
    ServiceFuture<String> generateSsoUriAsync(ServiceCallback<String> callback);

    /**
     * Starts a stopped CDN endpoint.
     *
     * @param endpointName a name of an endpoint under the profile
     */
    void startEndpoint(String endpointName);

    /**
     * Starts a stopped CDN endpoint asynchronously.
     *
     * @param endpointName a name of an endpoint under the profile
     * @return a representation of the deferred computation of this call
     */
    @Beta
    Completable startEndpointAsync(String endpointName);

    /**
     * Starts a stopped CDN endpoint asynchronously.
     *
     * @param endpointName a name of an endpoint under the profile
     * @param callback the callback to call on success or failure
     * @return a representation of the deferred computation of this call
     */
    @Beta
    ServiceFuture<Void> startEndpointAsync(String endpointName, ServiceCallback<Void> callback);

    /**
     * Stops a running CDN endpoint.
     *
     * @param endpointName a name of an endpoint under the profile
     */
    void stopEndpoint(String endpointName);

    /**
     * Stops a running CDN endpoint asynchronously.
     *
     * @param endpointName a name of an endpoint under the profile
     * @return a representation of the deferred computation of this call
     */
    @Beta
    Completable stopEndpointAsync(String endpointName);

    /**
     * Stops a running CDN endpoint asynchronously.
     *
     * @param endpointName a name of an endpoint under the profile
     * @param callback the callback to call on success or failure
     * @return a representation of the deferred computation of this call
     */
    @Beta
    ServiceFuture<Void> stopEndpointAsync(String endpointName, ServiceCallback<Void> callback);

    /**
     * Forcibly purges CDN endpoint content in the CDN profile.
     *
     * @param endpointName a name of the endpoint under the profile
     * @param contentPaths the paths to the content to be purged, which can be file paths or directory wild cards
     */
    @Beta // TODO: contentPaths should be Set<String>
    void purgeEndpointContent(String endpointName, List<String> contentPaths);

    /**
     * Forcibly purges CDN endpoint content in the CDN profile asynchronously.
     *
     * @param endpointName a name of the endpoint under the profile
     * @param contentPaths the paths to the content to be purged, which can be file paths or directory wild cards
     * @return a representation of the deferred computation of this call
     */
    @Beta // TODO: contentPaths should be Set<String>
    Completable purgeEndpointContentAsync(String endpointName, List<String> contentPaths);

    /**
     * Forcibly purges CDN endpoint content in the CDN profile asynchronously.
     *
     * @param endpointName a name of the endpoint under the profile
     * @param contentPaths the paths to the content to be purged, which can be file paths or directory wild cards
     * @param callback the callback to call on success or failure
     * @return a representation of the deferred computation of this call
     */
    @Beta // TODO: contentPaths should be Set<String>
    ServiceFuture<Void> purgeEndpointContentAsync(String endpointName, List<String> contentPaths, ServiceCallback<Void> callback);

    /**
     * Forcibly pre-loads CDN endpoint content in the CDN profile.
     * <p>
     * Note, this is Available for Verizon Profiles only.
     *
     * @param endpointName a name of the endpoint under the profile
     * @param contentPaths the paths to the content to be purged, which can be file paths or directory wild cards
     */
    @Beta // TODO: contentPaths should be Set<String>
    void loadEndpointContent(String endpointName, List<String> contentPaths);

    /**
     * Forcibly pre-loads CDN endpoint content in the CDN profile asynchronously.
     * <p>
     * Note, this is Available for Verizon Profiles only.
     *
     * @param endpointName a name of the endpoint under the profile
     * @param contentPaths the paths to the content to be purged, which can be file paths or directory wild cards
     * @return a representation of the deferred computation of this call
     */
    @Beta // TODO: contentPaths should be Set<String>
    Completable loadEndpointContentAsync(String endpointName, List<String> contentPaths);

    /**
     * Forcibly pre-loads CDN endpoint content in the CDN profile asynchronously.
     * <p>
     * Note, this is Available for Verizon Profiles only.
     *
     * @param endpointName a name of the endpoint under the profile
     * @param contentPaths the paths to the content to be purged, which can be file paths or directory wild cards
     * @param callback the callback to call on success or failure
     * @return a representation of the deferred computation of this call
     */
    @Beta // TODO: contentPaths should be Set<String>
    ServiceFuture<Void> loadEndpointContentAsync(String endpointName, List<String> contentPaths, ServiceCallback<Void> callback);

    /**
     * Validates a custom domain mapping to ensure it maps to the correct CNAME in DNS in current profile.
     *
     * @param endpointName a name of the endpoint under the profile
     * @param hostName the host name of the custom domain, which must be a domain name
     * @return CustomDomainValidationResult object if successful
     */
    CustomDomainValidationResult validateEndpointCustomDomain(String endpointName, String hostName);

    /**
     * Validates a custom domain mapping to ensure it maps to the correct CNAME in DNS in current profile asynchronously.
     *
     * @param endpointName a name of the endpoint under the profile
     * @param hostName the host name of the custom domain, which must be a domain name
     * @return the Observable to CustomDomainValidationResult object if successful
     */
    @Beta
    Observable<CustomDomainValidationResult> validateEndpointCustomDomainAsync(String endpointName, String hostName);


    /**
     * Validates a custom domain mapping to ensure it maps to the correct CNAME in DNS in current profile asynchronously.
     *
     * @param endpointName a name of the endpoint under the profile
     * @param hostName the host name of the custom domain, which must be a domain name
     * @param callback the callback to call on success or failure
     * @return a representation of the deferred computation of this call
     */
    @Beta
    ServiceFuture<CustomDomainValidationResult> validateEndpointCustomDomainAsync(String endpointName, String hostName, ServiceCallback<CustomDomainValidationResult> callback);

    /**
     * Checks the availability of an endpoint name without creating the CDN endpoint.
     *
     * @param name the endpoint resource name to validate
     * @return the result if successful.
     */
    CheckNameAvailabilityResult checkEndpointNameAvailability(String name);

    /**
     * Checks the availability of an endpoint name without creating the CDN endpoint asynchronously.
     *
     * @param name the endpoint resource name to validate.
     * @return the Observable of the result if successful
     */
    @Beta
    Observable<CheckNameAvailabilityResult> checkEndpointNameAvailabilityAsync(String name);

    /**
     * Checks the availability of an endpoint name without creating the CDN endpoint asynchronously.
     *
     * @param name the endpoint resource name to validate.
     * @param callback the callback to call on success or failure
     * @return a representation of the deferred computation of this call
     */
    @Beta
    ServiceFuture<CheckNameAvailabilityResult> checkEndpointNameAvailabilityAsync(String name, ServiceCallback<CheckNameAvailabilityResult> callback);

    /**
     * @return true if this CDN profile's SKU is of Premium Verizon, else false.
     */
    boolean isPremiumVerizon();

    /**
     * @return quotas and actual usages of endpoints under the current CDN profile
     */
    PagedList<ResourceUsage> listResourceUsage();

    /**
     * The entirety of a CDN profile definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithSku,
            DefinitionStages.WithStandardCreate,
            DefinitionStages.WithPremiumVerizonCreate,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of CDN profile definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a CDN profile definition.
         */
        interface Blank extends DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of a CDN profile definition allowing the resource group to be specified.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithSku> {
        }

        /**
         * A CDN profile definition allowing the SKU to be specified.
         */
        interface WithSku {
            /**
             * Selects the Standard Akamai SKU.
             *
             * @return the next stage of the definition.
             */
            WithStandardCreate withStandardAkamaiSku();

            /**
             * Selects the Standard Verizon SKU.
             *
             * @return the next stage of the definition.
             */
            WithStandardCreate withStandardVerizonSku();

            /**
             * Selects the Premium Verizon SKU.
             *
             * @return the next stage of the definition.
             */
            WithPremiumVerizonCreate withPremiumVerizonSku();
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created
         * but also allows for any other optional settings to be specified.
         */
        interface WithStandardCreate extends WithCreate {
            /**
             * Adds new endpoint to the CDN profile.
             *
             * @param endpointOriginHostname an endpoint origin hostname
             * @return the next stage of CDN profile definition.
             */
            WithStandardCreate withNewEndpoint(String endpointOriginHostname);

            /**
             * Starts the definition of a new endpoint to be attached to the CDN profile.
             *
             * @return the first stage of a new CDN endpoint definition
             */
            @Beta // TODO: Why is define() not requiring a name?
            CdnEndpoint.DefinitionStages.Blank.StandardEndpoint<WithStandardCreate> defineNewEndpoint();

            /**
             * Starts the definition of a new endpoint to be attached to the CDN profile.
             *
             * @param name a new endpoint name
             * @return the first stage of a new CDN endpoint definition
             */
            CdnEndpoint.DefinitionStages.Blank.StandardEndpoint<WithStandardCreate> defineNewEndpoint(String name);

            /**
             * Starts the definition of a new endpoint to be attached to the CDN profile.
             *
             * @param name the name for the endpoint
             * @param endpointOriginHostname an endpoint origin hostname
             * @return the first stage of a new CDN endpoint definition
             */
            @Beta // Why is define() taking more than just the name?
            CdnEndpoint.DefinitionStages.WithStandardAttach<WithStandardCreate> defineNewEndpoint(String name, String endpointOriginHostname);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created
         * but also allows for any other optional settings to be specified.
         */
        interface WithPremiumVerizonCreate extends WithCreate {
            /**
             * Adds a new endpoint to current CDN profile.
             *
             * @param endpointOriginHostname an endpoint origin hostname.
             * @return the next stage of the definition.
             */
            WithPremiumVerizonCreate withNewPremiumEndpoint(String endpointOriginHostname);

            /**
             * Starts the definition of a new endpoint to be attached to the CDN profile.
             *
             * @return the first stage of a new CDN endpoint definition
             */
            @Beta // Why is define() not requiring a name?
            CdnEndpoint.DefinitionStages.Blank.PremiumEndpoint<WithPremiumVerizonCreate> defineNewPremiumEndpoint();

            /**
             * Starts the definition of a new endpoint to be attached to the CDN profile.
             *
             * @param name a name for the endpoint
             * @return the first stage of a new CDN endpoint definition
             */
            CdnEndpoint.DefinitionStages.Blank.PremiumEndpoint<WithPremiumVerizonCreate> defineNewPremiumEndpoint(String name);

            /**
             * Starts the definition of a new endpoint to be attached to the CDN profile.
             *
             * @param name the name for the endpoint
             * @param endpointOriginHostname the endpoint origin hostname
             * @return the stage representing configuration for the endpoint
             */
            @Beta // Why is define() taking more than just the name?
            CdnEndpoint.DefinitionStages.WithPremiumAttach<WithPremiumVerizonCreate> defineNewPremiumEndpoint(String name, String endpointOriginHostname);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created
         * but also allows for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<CdnProfile>,
                Resource.DefinitionWithTags<WithCreate> {
        }
    }

    /**
     * Grouping of CDN manager update stages.
     */
    interface UpdateStages {
        /**
         * The stage of a CDN profile update allowing to modify the endpoints for the profile.
         */
        interface WithEndpoint {
            /**
             * Adds a new endpoint.
             *
             * @param endpointOriginHostname an endpoint origin hostname
             * @return the next stage of the update
             */
            Update withNewEndpoint(String endpointOriginHostname);

            /**
             * Starts the definition of a new endpoint to be attached to the CDN profile.
             *
             * @return the first stage of an endpoint definition
             */
            @Beta // TODO: why is define() taking no name?
            CdnEndpoint.UpdateDefinitionStages.Blank.StandardEndpoint<Update> defineNewEndpoint();

            /**
             * Starts the definition of a new endpoint to be attached to the CDN profile.
             *
             * @param name the name for the endpoint
             * @return the first stage of an endpoint definition
             */
            CdnEndpoint.UpdateDefinitionStages.Blank.StandardEndpoint<Update> defineNewEndpoint(String name);

            /**
             * Specifies definition of an endpoint to be attached to the CDN profile.
             *
             * @param name the name for the endpoint
             * @param endpointOriginHostname the endpoint origin hostname
             * @return the first stage of an endpoint definition
             */
            @Beta // TODO: Why define() is taking more than the name?
            CdnEndpoint.UpdateDefinitionStages.WithStandardAttach<Update> defineNewEndpoint(String name, String endpointOriginHostname);

            /**
             * Adds new endpoint to current Premium Verizon CDN profile.
             *
             * @param endpointOriginHostname the endpoint origin hostname
             * @return the next stage of the update
             */
            Update withNewPremiumEndpoint(String endpointOriginHostname);

            /**
             * Starts the definition of a new endpoint to be attached to this Premium Verizon CDN profile.
             *
             * @return the first stage of an endpoint definition
             */
            @Beta // TODO: why is this define* not requiring a name?
            CdnEndpoint.UpdateDefinitionStages.Blank.PremiumEndpoint<Update> defineNewPremiumEndpoint();

            /**
             * Starts the definition of a new endpoint to be attached to this Premium Verizon CDN profile.
             *
             * @param name a name for the new endpoint
             * @return the first stage of an endpoint definition
             */
            CdnEndpoint.UpdateDefinitionStages.Blank.PremiumEndpoint<Update> defineNewPremiumEndpoint(String name);

            /**
             * Starts the definition of a new endpoint to be attached to this Premium Verizon CDN profile.
             *
             * @param name a name for the endpoint
             * @param endpointOriginHostname the endpoint origin hostname.
             * @return the first stage of an endpoint definition
             */
            @Beta // TODO: why is this taking more than just the name?
            CdnEndpoint.UpdateDefinitionStages.WithPremiumAttach<Update> defineNewPremiumEndpoint(String name, String endpointOriginHostname);

            /**
             * Begins the description of an update of an existing endpoint in current profile.
             *
             * @param name the name of an existing endpoint
             * @return the first stage of the update of the endpoint
             */
            CdnEndpoint.UpdateStandardEndpoint updateEndpoint(String name);

            /**
             * Begins the description of an update of an existing endpoint in current Premium Verizon profile.
             *
             * @param name the name of the endpoint
             * @return the first stage of the update of the endpoint
             */
            CdnEndpoint.UpdatePremiumEndpoint updatePremiumEndpoint(String name);

            /**
             * Removes an endpoint from the profile.
             *
             * @param name the name of an existing endpoint
             * @return the next stage of the CDN profile update
             */
            Update withoutEndpoint(String name);
        }
    }

    /**
     * The template for an update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<CdnProfile>,
            UpdateStages.WithEndpoint,
            Resource.UpdateWithTags<Update> {
    }
}