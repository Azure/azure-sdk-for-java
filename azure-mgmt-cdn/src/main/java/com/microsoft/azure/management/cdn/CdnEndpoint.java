package com.microsoft.azure.management.cdn;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.cdn.implementation.EndpointInner;
import com.microsoft.azure.management.resources.fluentcore.arm.CountryISOCode;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ExternalChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.util.List;

/**
 * An immutable client-side representation of an Azure CDN endpoint.
 */
@Fluent
public interface CdnEndpoint extends
        ExternalChildResource<CdnEndpoint, CdnProfile>,
        Wrapper<EndpointInner> {

    /**
     * Get the originHostHeader value.
     *
     * @return the originHostHeader value
     */
    String originHostHeader();

    /**
     * Get the originPath value.
     *
     * @return the originPath value
     */
    String originPath();

    /**
     * Get the contentTypesToCompress value.
     *
     * @return the contentTypesToCompress value
     */
    List<String> contentTypesToCompress();

    /**
     * Get the isCompressionEnabled value.
     *
     * @return the isCompressionEnabled value
     */
    boolean isCompressionEnabled();

    /**
     * Get the isHttpAllowed value.
     *
     * @return the isHttpAllowed value
     */
    boolean isHttpAllowed();

    /**
     * Get the isHttpsAllowed value.
     *
     * @return the isHttpsAllowed value
     */
    boolean isHttpsAllowed();

    /**
     * Get the queryStringCachingBehavior value.
     *
     * @return the queryStringCachingBehavior value
     */
    QueryStringCachingBehavior queryStringCachingBehavior();

    /**
     * Get the optimizationType value.
     *
     * @return the optimizationType value
     */
    String optimizationType();

    /**
     * Get the geoFilters value.
     *
     * @return the geoFilters value
     */
    List<GeoFilter> geoFilters();

    /**
     * Get the hostName value.
     *
     * @return the hostName value
     */
    String hostName();

    /**
     * Get the resourceState value.
     *
     * @return the resourceState value
     */
    EndpointResourceState resourceState();

    /**
     * Get the provisioningState value.
     *
     * @return the provisioningState value
     */
    String provisioningState();

    /**
     * Get the hostName value.
     *
     * @return the hostName value
     */
    String originHostName();

    /**
     * Get the httpPort value.
     *
     * @return the httpPort value
     */
    int httpPort();

    /**
     * Get the httpsPort value.
     *
     * @return the httpsPort value
     */
    int httpsPort();

    /**
     * Get the custom domains.
     *
     * @return list of custom domains associated with current endpoint.
     */
    List<String> customDomains();

    /**
     * Starts current stopped CDN endpoint.
     */
    void start();

    /**
     * Stops current running CDN endpoint.
     */
    void stop();

    /**
     * Forcibly purges current CDN endpoint content.
     *
     * @param contentPaths The path to the content to be purged. Can describe a file path or a wild card directory.
     */
    void purgeContent(List<String> contentPaths);

    /**
     * Forcibly pre-loads current CDN endpoint content . Available for Verizon Profiles.
     *
     * @param contentPaths The path to the content to be loaded. Should describe a file path.
     */
    void loadContent(List<String> contentPaths);

    /**
     * Validates a custom domain mapping to ensure it maps to the correct CNAME in DNS for current endpoint.
     *
     * @param hostName The host name of the custom domain. Must be a domain name.
     * @return the ValidateCustomDomainOutputInner object if successful.
     */
    CustomDomainValidationResult validateCustomDomain(String hostName);

    /**
     * Grouping of CDN profile endpoint definition stages as a part of parent CDN profile definition.
     */
    interface DefinitionStages {
        /**
         * The first stage of a CDN profile endpoint definition.
         */
        interface Blank {
            /**
             * The stage of the CDN profile endpoint definition allowing to specify the Origin.
             *
             * @param <ParentT> the return type of {@link AttachableStandard#attach()}
             */
            interface StandardEndpoint<ParentT> {
                /**
                 * Specifies the Origin of the CDN Endpoint.
                 *
                 * @param originName name of the Origin.
                 * @param originHostName origin hostname.
                 * @return the next stage of the definition
                 */
                DefinitionStages.WithStandardAttach<ParentT> withOrigin(String originName, String originHostName);

                /**
                 * Specifies the Origin of the CDN Endpoint.
                 *
                 * @param originHostName origin hostname.
                 * @return the next stage of the definition
                 */
                DefinitionStages.WithStandardAttach<ParentT> withOrigin(String originHostName);
            }

            /**
             * The stage of the CDN profile endpoint definition allowing to specify the Origin
             * for CDN Profile with Premium Verizon sku.
             *
             * @param <ParentT> the return type of {@link AttachablePremium#attach()}
             */
            interface PremiumEndpoint<ParentT> {
                /**
                 * Specifies the Origin of the CDN Endpoint.
                 *
                 * @param originName name of the Origin.
                 * @param originHostName origin hostname.
                 * @return the next stage of the definition
                 */
                DefinitionStages.WithPremiumAttach<ParentT> withPremiumOrigin(String originName, String originHostName);

                /**
                 * Specifies the Origin of the CDN Endpoint.
                 *
                 * @param originHostName origin hostname.
                 * @return the next stage of the definition
                 */
                DefinitionStages.WithPremiumAttach<ParentT> withPremiumOrigin(String originHostName);
            }
        }

        /** The final stage of the CDN profile Standard Akamai or Standard Verizone endpoint definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the CDN profile endpoint
         * definition can be attached to the parent CDN profile definition using {@link CdnEndpoint.DefinitionStages.AttachableStandard#attach()}.
         * @param <ParentT> the return type of {@link CdnEndpoint.DefinitionStages.AttachableStandard#attach()}
         */
        interface WithStandardAttach<ParentT>
                extends AttachableStandard<ParentT> {
            WithStandardAttach<ParentT> withOriginPath(String originPath);
            WithStandardAttach<ParentT> withHostHeader(String hostHeader);
            WithStandardAttach<ParentT> withHttpAllowed(boolean httpAllowed);
            WithStandardAttach<ParentT> withHttpsAllowed(boolean httpsAllowed);
            WithStandardAttach<ParentT> withHttpPort(int httpPort);
            WithStandardAttach<ParentT> withHttpsPort(int httpsPort);
            WithStandardAttach<ParentT> withContentTypesToCompress(List<String> contentTypesToCompress);
            WithStandardAttach<ParentT> withContentTypeToCompress(String contentTypeToCompress);
            WithStandardAttach<ParentT> withCompressionEnabled(boolean compressionEnabled);
            WithStandardAttach<ParentT> withQueryStringCachingBehavior(QueryStringCachingBehavior cachingBehavior);
            WithStandardAttach<ParentT> withGeoFilters(List<GeoFilter> geoFilters);
            WithStandardAttach<ParentT> withGeoFilter(String relativePath, GeoFilterActions action, CountryISOCode countryCode);
            WithStandardAttach<ParentT> withGeoFilter(String relativePath, GeoFilterActions action, List<CountryISOCode> countryCodes);
            WithStandardAttach<ParentT> withCustomDomain(String hostName);
        }

        /** The final stage of the CDN profile Premium Verizone endpoint definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the CDN profile endpoint
         * definition can be attached to the parent CDN profile definition using {@link CdnEndpoint.DefinitionStages.AttachablePremium#attach()}.
         * @param <ParentT> the return type of {@link CdnEndpoint.DefinitionStages.AttachablePremium#attach()}
         */
        interface WithPremiumAttach<ParentT>
                extends AttachablePremium<ParentT> {
            WithPremiumAttach<ParentT> withOriginPath(String originPath);
            WithPremiumAttach<ParentT> withHostHeader(String hostHeader);
            WithPremiumAttach<ParentT> withHttpAllowed(boolean httpAllowed);
            WithPremiumAttach<ParentT> withHttpsAllowed(boolean httpsAllowed);
            WithPremiumAttach<ParentT> withHttpPort(int httpPort);
            WithPremiumAttach<ParentT> withHttpsPort(int httpsPort);
            WithPremiumAttach<ParentT> withCustomDomain(String hostName);
        }

        /**
         * The final stage of the Standard endpoint object definition, at which it can be attached to the parent, using {@link AttachableStandard#attach()}.
         *
         * @param <ParentT> the parent definition {@link AttachableStandard#attach()} returns to
         */
        interface AttachableStandard<ParentT> {
            ParentT attach();
        }

        /**
         * The final stage of the Premium Verizone endpoint object definition, at which it can be attached to the parent, using {@link AttachableStandard#attach()}.
         *
         * @param <ParentT> the parent definition {@link AttachableStandard#attach()} returns to
         */
        interface AttachablePremium<ParentT> {
            ParentT attach();
        }
    }

    /**
     * The entirety of a CDN profile endpoint definition as a part of parent update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a CDN profile endpoint definition.
         */
        interface Blank {
            /**
             * The stage of the CDN profile endpoint definition allowing to specify the Origin.
             *
             * @param <ParentT> the return type of {@link AttachableStandard#attach()}
             */
            interface StandardEndpoint<ParentT> {
                /**
                 * Specifies the Origin of the CDN Endpoint.
                 *
                 * @param originName name of the Origin.
                 * @param originHostName origin hostname.
                 * @return the next stage of the definition
                 */
                UpdateDefinitionStages.WithStandardAttach<ParentT> withOrigin(String originName, String originHostName);

                /**
                 * Specifies the Origin of the CDN Endpoint.
                 *
                 * @param originHostName origin hostname.
                 * @return the next stage of the definition
                 */
                UpdateDefinitionStages.WithStandardAttach<ParentT> withOrigin(String originHostName);
            }

            /**
             * The stage of the CDN profile endpoint definition allowing to specify the Origin
             * for CDN Profile with Premium Verizon sku.
             *
             * @param <ParentT> the return type of {@link AttachablePremium#attach()}
             */
            interface PremiumEndpoint<ParentT> {
                /**
                 * Specifies the Origin of the CDN Endpoint.
                 *
                 * @param originName name of the Origin.
                 * @param originHostName origin hostname.
                 * @return the next stage of the definition
                 */
                UpdateDefinitionStages.WithPremiumAttach<ParentT> withPremiumOrigin(String originName, String originHostName);

                /**
                 * Specifies the Origin of the CDN Endpoint.
                 *
                 * @param originHostName origin hostname.
                 * @return the next stage of the definition
                 */
                UpdateDefinitionStages.WithPremiumAttach<ParentT> withPremiumOrigin(String originHostName);
            }
        }

        /** The final stage of the CDN profile Standard Akamai or Standard Verizone endpoint definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the CDN profile endpoint
         * definition can be attached to the parent CDN profile definition using {@link CdnEndpoint.DefinitionStages.AttachableStandard#attach()}.
         * @param <ParentT> the return type of {@link CdnEndpoint.DefinitionStages.AttachableStandard#attach()}
         */
        interface WithStandardAttach<ParentT>
                extends AttachableStandard<ParentT> {
            WithStandardAttach<ParentT> withOriginPath(String originPath);
            WithStandardAttach<ParentT> withHostHeader(String hostHeader);
            WithStandardAttach<ParentT> withHttpAllowed(boolean httpAllowed);
            WithStandardAttach<ParentT> withHttpsAllowed(boolean httpsAllowed);
            WithStandardAttach<ParentT> withHttpPort(int httpPort);
            WithStandardAttach<ParentT> withHttpsPort(int httpsPort);
            WithStandardAttach<ParentT> withContentTypesToCompress(List<String> contentTypesToCompress);
            WithStandardAttach<ParentT> withContentTypeToCompress(String contentTypeToCompress);
            WithStandardAttach<ParentT> withCompressionEnabled(boolean compressionEnabled);
            WithStandardAttach<ParentT> withQueryStringCachingBehavior(QueryStringCachingBehavior cachingBehavior);
            WithStandardAttach<ParentT> withGeoFilters(List<GeoFilter> geoFilters);
            WithStandardAttach<ParentT> withGeoFilter(String relativePath, GeoFilterActions action, CountryISOCode countryCode);
            WithStandardAttach<ParentT> withGeoFilter(String relativePath, GeoFilterActions action, List<CountryISOCode> countryCodes);
            WithStandardAttach<ParentT> withCustomDomain(String hostName);
        }

        /** The final stage of the CDN profile Premium Verizone endpoint definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the CDN profile endpoint
         * definition can be attached to the parent CDN profile definition using {@link CdnEndpoint.DefinitionStages.AttachablePremium#attach()}.
         * @param <ParentT> the return type of {@link CdnEndpoint.DefinitionStages.AttachablePremium#attach()}
         */
        interface WithPremiumAttach<ParentT>
                extends AttachablePremium<ParentT> {
            WithPremiumAttach<ParentT> withOriginPath(String originPath);
            WithPremiumAttach<ParentT> withHostHeader(String hostHeader);
            WithPremiumAttach<ParentT> withHttpAllowed(boolean httpAllowed);
            WithPremiumAttach<ParentT> withHttpsAllowed(boolean httpsAllowed);
            WithPremiumAttach<ParentT> withHttpPort(int httpPort);
            WithPremiumAttach<ParentT> withHttpsPort(int httpsPort);
            WithPremiumAttach<ParentT> withCustomDomain(String hostName);
        }

        /**
         * The final stage of the Standard endpoint object definition, at which it can be attached to the parent, using {@link AttachableStandard#attach()}.
         *
         * @param <ParentT> the parent definition {@link AttachableStandard#attach()} returns to
         */
        interface AttachableStandard<ParentT> {
            ParentT attach();
        }

        /**
         * The final stage of the Premium Verizone endpoint object definition, at which it can be attached to the parent, using {@link AttachableStandard#attach()}.
         *
         * @param <ParentT> the parent definition {@link AttachableStandard#attach()} returns to
         */
        interface AttachablePremium<ParentT> {
            ParentT attach();
        }
    }

    /**
     * The stage of an CDN profile endpoint update allowing to specify endpoint properties.
     */
    interface UpdateStandardEndpoint extends Update {
        UpdateStandardEndpoint withOriginPath(String originPath);
        UpdateStandardEndpoint withHttpAllowed(boolean httpAllowed);
        UpdateStandardEndpoint withHttpsAllowed(boolean httpsAllowed);
        UpdateStandardEndpoint withHttpPort(int httpPort);
        UpdateStandardEndpoint withHttpsPort(int httpsPort);
        UpdateStandardEndpoint withHostHeader(String hostHeader);
        UpdateStandardEndpoint withContentTypesToCompress(List<String> contentTypesToCompress);
        UpdateStandardEndpoint withoutContentTypesToCompress();
        UpdateStandardEndpoint withContentTypeToCompress(String contentTypeToCompress);
        UpdateStandardEndpoint withoutContentTypeToCompress(String contentTypeToCompress);
        UpdateStandardEndpoint withCompressionEnabled(boolean compressionEnabled);
        UpdateStandardEndpoint withQueryStringCachingBehavior(QueryStringCachingBehavior cachingBehavior);
        UpdateStandardEndpoint withGeoFilters(List<GeoFilter> geoFilters);
        UpdateStandardEndpoint withoutGeoFilters();
        UpdateStandardEndpoint withGeoFilter(String relativePath, GeoFilterActions action, CountryISOCode countryCode);
        UpdateStandardEndpoint withGeoFilter(String relativePath, GeoFilterActions action, List<CountryISOCode> countryCodes);
        UpdateStandardEndpoint withoutGeoFilter(String relativePath);
        UpdateStandardEndpoint withCustomDomain(String hostName);
        UpdateStandardEndpoint withoutCustomDomain(String hostName);
    }

    /**
     * The stage of an CDN profile endpoint update allowing to specify endpoint properties.
     */
    interface UpdatePremiumEndpoint extends Update {
        UpdatePremiumEndpoint withOriginPath(String originPath);
        UpdatePremiumEndpoint withHostHeader(String hostHeader);
        UpdatePremiumEndpoint withHttpAllowed(boolean httpAllowed);
        UpdatePremiumEndpoint withHttpsAllowed(boolean httpsAllowed);
        UpdatePremiumEndpoint withHttpPort(int httpPort);
        UpdatePremiumEndpoint withHttpsPort(int httpsPort);
        UpdatePremiumEndpoint withCustomDomain(String hostName);
        UpdatePremiumEndpoint withoutCustomDomain(String hostName);
    }

    /**
     * The set of configurations that can be updated for all endpoint irrespective of their type.
     */
    interface Update extends
            Settable<CdnProfile.Update> {
    }
}