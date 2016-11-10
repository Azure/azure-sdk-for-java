package com.microsoft.azure.management.cdn;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.cdn.implementation.ProfileInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.util.List;
import java.util.Map;

/**
 * An immutable client-side representation of an Azure CDN profile.
 */
@Fluent
public interface CdnProfile extends
        GroupableResource,
        Refreshable<CdnProfile>,
        Wrapper<ProfileInner>,
        Updatable<CdnProfile.Update> {

    // Actions
    /**
     * @return the sku value.
     */
    Sku sku();

    /**
     * @return CDN profile state.
     */
    String resourceState();

    /**
     *
     * @return endpoints in the CDN manager profile, indexed by the name
     */
    Map<String, CdnEndpoint> endpoints();

    /**
     * Generates a dynamic SSO URI used to sign in to the CDN Supplemental Portal used for advanced management tasks.
     *
     * @return The URI used to login to third party web portal.
     */
    String generateSsoUri();

    CdnEndpoint endpointStart(String endpointName);

    CdnEndpoint endpointStop(String endpointName);

    void endpointPurgeContent(String endpointName, List<String> contentPaths);
    void endpointLoadContent(String endpointName, List<String> contentPaths);
    CustomDomainValidationResult endpointValidateCustomDomain(String endpointName, String hostName);

    CheckNameAvailabilityResult checkEndpointNameAvailability(String name);

    boolean isPremiumVerizon();
    /**************************************************************
     * Fluent interfaces to provision a CDN.
     **************************************************************/
    /**
     * The entirety of the CDN profile.
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
         * A Redis Cache definition allowing resource group to be set.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithSku> {
        }

        /**
         * A CDN profile definition allowing the sku to be set.
         */
        interface WithSku {
            /**
             * Specifies the sku of the CDN profile.
             *
             * @return the next stage of CDN profile definition.
             */
            WithStandardCreate withStandardAkamaiSku();

            WithStandardCreate withStandardVerizonSku();

            WithPremiumVerizonCreate withPremiumVerizonSku();
        }

        interface WithStandardCreate extends WithCreate {
            WithStandardCreate withNewEndpoint(String endpointHostname, String endpointOriginHostname);
            CdnEndpoint.DefinitionStages.Blank.StandardEndpoint<WithStandardCreate> defineNewEndpoint(String endpointHostname);
            CdnEndpoint.DefinitionStages.Blank.StandardEndpoint<WithStandardCreate> defineNewEndpoint(String name, String endpointHostname);
        }

        interface WithPremiumVerizonCreate extends WithCreate {
            WithPremiumVerizonCreate withNewPremiumEndpoint(String endpointHostname, String endpointOriginHostname);
            CdnEndpoint.DefinitionStages.Blank.PremiumEndpoint<WithPremiumVerizonCreate> defineNewPremiumEndpoint(String endpointHostname);
            CdnEndpoint.DefinitionStages.Blank.PremiumEndpoint<WithPremiumVerizonCreate> defineNewPremiumEndpoint(String name, String endpointHostname);
        }
        interface WithCreate extends
                Creatable<CdnProfile>,
                Resource.DefinitionWithTags<WithCreate> {
        }
    }

    interface UpdateStages {
        interface WithEndpoint {
            Update withNewEndpoint(String endpointHostname, String endpointOriginHostname);
            CdnEndpoint.UpdateDefinitionStages.Blank.StandardEndpoint<Update> defineNewEndpoint(String endpointHostname);
            CdnEndpoint.UpdateDefinitionStages.Blank.StandardEndpoint<Update> defineNewEndpoint(String name, String endpointHostname);

            Update withNewPremiumEndpoint(String endpointHostname, String endpointOriginHostname);
            CdnEndpoint.UpdateDefinitionStages.Blank.PremiumEndpoint<Update> defineNewPremiumEndpoint(String endpointHostname);
            CdnEndpoint.UpdateDefinitionStages.Blank.PremiumEndpoint<Update> defineNewPremiumEndpoint(String name, String endpointHostname);

            CdnEndpoint.UpdateStandardEndpoint updateEndpoint(String name);
            CdnEndpoint.UpdatePremiumEndpoint updatePremiumEndpoint(String name);

            Update withoutEndpoint(String name);
        }
    }

    /**
     * The template for an update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
            Appliable<CdnProfile>,
            UpdateStages.WithEndpoint,
            Resource.UpdateWithTags<Update> {
    }
}