package com.microsoft.azure.management.cdn;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.cdn.implementation.EndpointInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ExternalChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.util.List;

/**
 * An immutable client-side representation of an Azure CDN profile.
 */
@Fluent
public interface CdnEndpoint extends
        ExternalChildResource<CdnEndpoint, CdnProfile>,
        Wrapper<EndpointInner> {

    // Actions
    // TODO: DODO

    /**************************************************************
     * Fluent interfaces to provision a CDN.
     **************************************************************/
    /**
     * The entirety of the CDN profile.
     */
    interface Definition<ParentT> extends
            DefinitionStages.Blank.StandardEndpoint<ParentT>,
            DefinitionStages.Blank.PremiumEndpoint<ParentT>,
            DefinitionStages.WithStandardOrigin<ParentT>,
            DefinitionStages.WithPremiumOrigin<ParentT>,
            DefinitionStages.WithStandardAttach<ParentT>,
            DefinitionStages.WithPremiumAttach<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of CDN profile definition stages.
     */
    interface DefinitionStages {

        interface Blank {

            interface StandardEndpoint<ParentT> extends WithStandardOrigin<ParentT>{
            }

            interface PremiumEndpoint<ParentT> extends WithPremiumOrigin<ParentT>{
            }
        }

        /**
         * A CDN profile definition allowing the sku to be set.
         */
        interface WithStandardOrigin<ParentT> {
            /**
             * Specifies the sku of the CDN profile.
             *
             * @return the next stage of CDN profile definition.
             */
            WithStandardAttach<ParentT> withOrigin(String originName, String hostname);
        }

        /**
         * A CDN profile definition allowing the sku to be set.
         */
        interface WithPremiumOrigin<ParentT> {
            /**
             * Specifies the sku of the CDN profile.
             *
             * @return the next stage of CDN profile definition.
             */
            WithPremiumAttach<ParentT> withPremiumOrigin(String originName, String hostname);
        }

        interface WithAttach<ParentT> extends Attachable.InDefinition<ParentT> {
        }

        interface WithStandardAttach<ParentT> extends WithAttach<ParentT> {
            WithStandardAttach<ParentT> withOriginHttpAllowed(boolean httpAllowed);
            WithStandardAttach<ParentT> withOriginHttpsAllowed(boolean httpsAllowed);
            WithStandardAttach<ParentT> withOriginHttpPort(int httpPort);
            WithStandardAttach<ParentT> withOriginHttpsPort(int httpsPort);
            WithStandardAttach<ParentT> withOriginHostHeader(String hostHeader);
            WithStandardAttach<ParentT> withOriginPath(String originPath);
            WithStandardAttach<ParentT> withContentTypesToCompress(List<String> contentTypesToCompress);
            WithStandardAttach<ParentT> withContentTypeToCompress(String contentTypeToCompress);
            WithStandardAttach<ParentT> withCompressionEnabled(boolean compressionEnabled);
            WithStandardAttach<ParentT> withCachingBehavior(QueryStringCachingBehavior cachingBehavior);
            WithStandardAttach<ParentT> withGeoFilters(List<GeoFilter> geoFilters);
            WithStandardAttach<ParentT> withGeoFilter(String relativePath, GeoFilterActions action, String countryCodes);
            WithStandardAttach<ParentT> withCustomDomain(String hostName);
        }

        interface WithPremiumAttach<ParentT> extends WithAttach<ParentT> {
            WithPremiumAttach<ParentT> withPremiumOriginHttpAllowed(boolean httpAllowed);
            WithPremiumAttach<ParentT> withPremiumOriginHttpsAllowed(boolean httpsAllowed);
            WithPremiumAttach<ParentT> withPremiumOriginHttpPort(int httpPort);
            WithPremiumAttach<ParentT> withPremiumOriginHttpsPort(int httpsPort);
            WithPremiumAttach<ParentT> withPremiumOriginHostHeader(String hostHeader);
            WithPremiumAttach<ParentT> withPremiumOriginPath(String originPath);
            WithPremiumAttach<ParentT> withPremiumCustomDomain(String hostName);
        }
    }

    interface UpdateDefinition<ParentT> extends
            UpdateDefinitionStages.Blank,
            UpdateDefinitionStages.WithStandardOrigin<ParentT>,
            UpdateDefinitionStages.WithPremiumOrigin<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT> {
    }

    interface UpdateDefinitionStages {

        interface Blank {

            interface StandardEndpoint<ParentT> extends WithStandardOrigin<ParentT> {
            }

            interface PremiumEndpoint<ParentT> extends UpdateDefinitionStages.WithPremiumOrigin<ParentT> {
            }
        }
        interface WithStandardOrigin<ParentT> {
            /**
             * Specifies the sku of the CDN profile.
             *
             * @return the next stage of CDN profile definition.
             */
            WithStandardAttach<ParentT> withOrigin(String originName, String hostname);
        }

        /**
         * A CDN profile definition allowing the sku to be set.
         */
        interface WithPremiumOrigin<ParentT> {
            /**
             * Specifies the sku of the CDN profile.
             *
             * @return the next stage of CDN profile definition.
             */
            WithPremiumAttach<ParentT> withPremiumOrigin(String originName, String hostname);
        }

        interface WithStandardAttach<ParentT> extends WithAttach<ParentT> {
            WithStandardAttach<ParentT> withOriginHttpAllowed(boolean httpAllowed);
            WithStandardAttach<ParentT> withOriginHttpsAllowed(boolean httpsAllowed);
            WithStandardAttach<ParentT> withOriginHttpPort(int httpPort);
            WithStandardAttach<ParentT> withOriginHttpsPort(int httpsPort);
            WithStandardAttach<ParentT> withOriginHostHeader(String hostHeader);
            WithStandardAttach<ParentT> withOriginPath(String originPath);
            WithStandardAttach<ParentT> withContentTypesToCompress(List<String> contentTypesToCompress);
            WithStandardAttach<ParentT> withContentTypeToCompress(String contentTypeToCompress);
            WithStandardAttach<ParentT> withCompressionEnabled(boolean compressionEnabled);
            WithStandardAttach<ParentT> withCachingBehavior(QueryStringCachingBehavior cachingBehavior);
            WithStandardAttach<ParentT> withGeoFilters(List<GeoFilter> geoFilters);
            WithStandardAttach<ParentT> withGeoFilter(String relativePath, GeoFilterActions action, String countryCodes);
            WithStandardAttach<ParentT> withCustomDomain(String hostName);
        }

        interface WithPremiumAttach<ParentT>  extends WithAttach<ParentT> {
            WithPremiumAttach<ParentT> withPremiumOriginHttpAllowed(boolean httpAllowed);
            WithPremiumAttach<ParentT> withPremiumOriginHttpsAllowed(boolean httpsAllowed);
            WithPremiumAttach<ParentT> withPremiumOriginHttpPort(int httpPort);
            WithPremiumAttach<ParentT> withPremiumOriginHttpsPort(int httpsPort);
            WithPremiumAttach<ParentT> withPremiumOriginHostHeader(String hostHeader);
            WithPremiumAttach<ParentT> withPremiumOriginPath(String originPath);
            WithPremiumAttach<ParentT> withPremiumCustomDomain(String hostName);
        }

        interface WithAttach<ParentT> extends
                Attachable.InUpdate<ParentT> {
        }
    }

    interface UpdateStandardEndpoint extends Update {
        UpdateStandardEndpoint withOriginHttpAllowed(boolean httpAllowed);
        UpdateStandardEndpoint withOriginHttpsAllowed(boolean httpsAllowed);
        UpdateStandardEndpoint withOriginHttpPort(int httpPort);
        UpdateStandardEndpoint withOriginHttpsPort(int httpsPort);
        UpdateStandardEndpoint withOriginHostHeader(String hostHeader);
        UpdateStandardEndpoint withOriginPath(String originPath);
        UpdateStandardEndpoint withContentTypesToCompress(List<String> contentTypesToCompress);
        UpdateStandardEndpoint withoutContentTypesToCompress();
        UpdateStandardEndpoint withContentTypeToCompress(String contentTypeToCompress);
        UpdateStandardEndpoint withoutContentTypeToCompress(String contentTypeToCompress);
        UpdateStandardEndpoint withCompressionEnabled(boolean compressionEnabled);
        UpdateStandardEndpoint withCachingBehavior(QueryStringCachingBehavior cachingBehavior);
        UpdateStandardEndpoint withGeoFilters(List<GeoFilter> geoFilters);
        UpdateStandardEndpoint withoutGeoFilters();
        UpdateStandardEndpoint withGeoFilter(String relativePath, GeoFilterActions action, String countryCodes);
        UpdateStandardEndpoint withoutGeoFilter(String relativePath);
        UpdateStandardEndpoint withCustomDomain(String hostName);
        UpdateStandardEndpoint withoutCustomDomain(String hostName);
    }

    interface UpdatePremiumEndpoint extends Update {
        UpdatePremiumEndpoint withPremiumOriginHttpAllowed(boolean httpAllowed);
        UpdatePremiumEndpoint withPremiumOriginHttpsAllowed(boolean httpsAllowed);
        UpdatePremiumEndpoint withPremiumOriginHttpPort(int httpPort);
        UpdatePremiumEndpoint withPremiumOriginHttpsPort(int httpsPort);
        UpdatePremiumEndpoint withPremiumOriginHostHeader(String hostHeader);
        UpdatePremiumEndpoint withPremiumOriginPath(String originPath);
        UpdatePremiumEndpoint withPremiumCustomDomain(String hostName);
    }

    interface Update extends
            Settable<CdnProfile.Update> {
    }

    interface UpdateStages {

    }
}