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

    /**
     * Generates a dynamic SSO URI used to sign in to the CDN Supplemental Portal used for advanced management tasks.
     *
     * @return The URI used to login to third party web portal.
     */
    String generateSsoUri(String resourceGroupName, String profileName);
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

        interface WithStandardCreate extends WithCreate{
            CdnEndpoint.DefinitionStages.Blank.StandardEndpoint<WithStandardCreate> defineNewEndpoint(String name);
        }

        interface WithPremiumVerizonCreate extends WithCreate {
            CdnEndpoint.DefinitionStages.Blank.PremiumEndpoint<WithPremiumVerizonCreate> defineNewPremiumEndpoint(String name);
        }
        interface WithCreate extends
                Creatable<CdnProfile>,
                Resource.DefinitionWithTags<WithCreate> {
        }
    }

    interface UpdateStages {
        interface WithEndpoint {
            CdnEndpoint.UpdateDefinitionStages.Blank.StandardEndpoint<Update> defineNewEndpoint(String name);
            CdnEndpoint.UpdateDefinitionStages.Blank.PremiumEndpoint<Update> defineNewPremiumEndpoint(String name);
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