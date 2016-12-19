/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.cdn;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Method;
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
     * @return origin host header
     */
    String originHostHeader();

    /**
     * @return origin path
     */
    String originPath();

    /**
     * @return list of content types to be compressed
     */
    List<String> contentTypesToCompress();

    /**
     * @return true if content compression is enabled, otherwise false
     */
    boolean isCompressionEnabled();

    /**
     * @return true if Http traffic is allowed, otherwise false.
     */
    boolean isHttpAllowed();

    /**
     * @return true if Https traffic is allowed, otherwise false
     */
    boolean isHttpsAllowed();

    /**
     * @return query string caching behavior
     */
    QueryStringCachingBehavior queryStringCachingBehavior();

    /**
     * @return optimization type value
     */
    String optimizationType();

    /**
     * @return list of Geo filters
     */
    List<GeoFilter> geoFilters();

    /**
     * @return endpoint host name
     */
    String hostName();

    /**
     * @return endpoint state
     */
    EndpointResourceState resourceState();

    /**
     * @return endpoint provisioning state
     */
    String provisioningState();

    /**
     * @return origin host name
     */
    String originHostName();

    /**
     * @return http port value
     */
    int httpPort();

    /**
     * @return https port value
     */
    int httpsPort();

    /**
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
     * @param contentPaths the path to the content to be purged. Can describe a file path or a wild card directory.
     */
    void purgeContent(List<String> contentPaths);

    /**
     * Forcibly pre-loads current CDN endpoint content. Available for Verizon Profiles.
     *
     * @param contentPaths the path to the content to be loaded. Should describe a file path.
     */
    void loadContent(List<String> contentPaths);

    /**
     * Validates a custom domain mapping to ensure it maps to the correct CNAME in DNS for current endpoint.
     *
     * @param hostName the host name of the custom domain. Must be a domain name.
     * @return the CustomDomainValidationResult object if successful.
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
             * The stage of the CDN profile endpoint definition allowing to specify the origin.
             *
             * @param <ParentT> the return type of {@link AttachableStandard#attach()}
             */
            interface StandardEndpoint<ParentT> {
                /**
                 * Specifies the origin of the CDN endpoint.
                 *
                 * @param originName name of the origin.
                 * @param originHostName origin hostname.
                 * @return the next stage of the definition
                 */
                DefinitionStages.WithStandardAttach<ParentT> withOrigin(String originName, String originHostName);

                /**
                 * Specifies the origin of the CDN endpoint.
                 *
                 * @param originHostName origin hostname.
                 * @return the next stage of the definition
                 */
                DefinitionStages.WithStandardAttach<ParentT> withOrigin(String originHostName);
            }

            /**
             * The stage of the CDN profile endpoint definition allowing to specify the origin
             * for CDN Profile with Premium Verizon sku.
             *
             * @param <ParentT> the return type of {@link AttachablePremium#attach()}
             */
            interface PremiumEndpoint<ParentT> {
                /**
                 * Specifies the origin of the CDN endpoint.
                 *
                 * @param originName name of the origin.
                 * @param originHostName origin hostname.
                 * @return the next stage of the definition
                 */
                DefinitionStages.WithPremiumAttach<ParentT> withPremiumOrigin(String originName, String originHostName);

                /**
                 * Specifies the origin of the CDN endpoint.
                 *
                 * @param originHostName origin hostname.
                 * @return the next stage of the definition
                 */
                DefinitionStages.WithPremiumAttach<ParentT> withPremiumOrigin(String originHostName);
            }
        }

        /** The final stage of the CDN profile Standard Akamai or Standard Verizon endpoint definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the CDN profile endpoint
         * definition can be attached to the parent CDN profile definition using {@link CdnEndpoint.DefinitionStages.AttachableStandard#attach()}.
         * @param <ParentT> the return type of {@link CdnEndpoint.DefinitionStages.AttachableStandard#attach()}
         */
        interface WithStandardAttach<ParentT>
                extends AttachableStandard<ParentT> {
            /**
             * Specifies origin path.
             *
             * @param originPath origin path.
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withOriginPath(String originPath);

            /**
             * Specifies host header.
             *
             * @param hostHeader host header.
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withHostHeader(String hostHeader);

            /**
             * Specifies if http traffic is allowed.
             *
             * @param httpAllowed if set to true Http traffic will be allowed.
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withHttpAllowed(boolean httpAllowed);

            /**
             * Specifies if https traffic is allowed.
             *
             * @param httpsAllowed if set to true Https traffic will be allowed.
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withHttpsAllowed(boolean httpsAllowed);

            /**
             * Specifies http port for http traffic.
             *
             * @param httpPort http port number.
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withHttpPort(int httpPort);

            /**
             * Specifies https port for http traffic.
             *
             * @param httpsPort https port number.
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withHttpsPort(int httpsPort);

            /**
             * Specifies the content types to compress.
             *
             * @param contentTypesToCompress the list of content types to compress to set
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withContentTypesToCompress(List<String> contentTypesToCompress);

            /**
             * Specifies a single content type to compress.
             *
             * @param contentTypeToCompress a singe content type to compress to add to the list
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withContentTypeToCompress(String contentTypeToCompress);

            /**
             * Sets the compression state.
             *
             * @param compressionEnabled if set to true compression will be enabled. If set to false compression will be disabled
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withCompressionEnabled(boolean compressionEnabled);

            /**
             * Sets the query string caching behavior.
             *
             * @param cachingBehavior the query string caching behavior value to set
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withQueryStringCachingBehavior(QueryStringCachingBehavior cachingBehavior);

            /**
             * Sets the geo filters list.
             *
             * @param geoFilters the Geo filters list to set
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withGeoFilters(List<GeoFilter> geoFilters);

            /**
             * Adds a single entry to the Geo filters list.
             *
             * @param relativePath the relative path.
             * @param action the action value.
             * @param countryCode the ISO 2 letter country codes.
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withGeoFilter(String relativePath, GeoFilterActions action, CountryISOCode countryCode);

            /**
             * Sets the geo filters list for the specified countries list.
             *
             * @param relativePath the relative path.
             * @param action the action value.
             * @param countryCodes a list of the ISO 2 letter country codes.
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withGeoFilter(String relativePath, GeoFilterActions action, List<CountryISOCode> countryCodes);

            /**
             * Adds a new CDN custom domain within an endpoint.
             *
             * @param hostName custom domain host name.
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withCustomDomain(String hostName);
        }

        /** The final stage of the CDN profile Premium Verizon endpoint definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the CDN profile endpoint
         * definition can be attached to the parent CDN profile definition using {@link CdnEndpoint.DefinitionStages.AttachablePremium#attach()}.
         * @param <ParentT> the return type of {@link CdnEndpoint.DefinitionStages.AttachablePremium#attach()}
         */
        interface WithPremiumAttach<ParentT>
                extends AttachablePremium<ParentT> {
            /**
             * Specifies origin path.
             *
             * @param originPath origin path.
             * @return the next stage of the endpoint definition
             */
            WithPremiumAttach<ParentT> withOriginPath(String originPath);

            /**
             * Specifies host header.
             *
             * @param hostHeader host header.
             * @return the next stage of the endpoint definition
             */
            WithPremiumAttach<ParentT> withHostHeader(String hostHeader);

            /**
             * Specifies if http traffic is allowed.
             *
             * @param httpAllowed if set to true Http traffic will be allowed.
             * @return the next stage of the endpoint definition
             */
            WithPremiumAttach<ParentT> withHttpAllowed(boolean httpAllowed);

            /**
             * Specifies if https traffic is allowed.
             *
             * @param httpsAllowed if set to true Https traffic will be allowed.
             * @return the next stage of the endpoint definition
             */
            WithPremiumAttach<ParentT> withHttpsAllowed(boolean httpsAllowed);

            /**
             * Specifies http port for http traffic.
             *
             * @param httpPort http port number.
             * @return the next stage of the endpoint definition
             */
            WithPremiumAttach<ParentT> withHttpPort(int httpPort);

            /**
             * Specifies https port for http traffic.
             *
             * @param httpsPort https port number.
             * @return the next stage of the endpoint definition
             */
            WithPremiumAttach<ParentT> withHttpsPort(int httpsPort);

            /**
             * Adds a new CDN custom domain within an endpoint.
             *
             * @param hostName custom domain host name.
             * @return the next stage of the endpoint definition
             */
            WithPremiumAttach<ParentT> withCustomDomain(String hostName);
        }

        /**
         * The final stage of the Standard endpoint object definition, at which it can be attached to the parent, using {@link AttachableStandard#attach()}.
         *
         * @param <ParentT> the parent definition {@link AttachableStandard#attach()} returns to
         */
        interface AttachableStandard<ParentT> {
            @Method
            ParentT attach();
        }

        /**
         * The final stage of the Premium Verizon endpoint object definition, at which it can be attached to the parent, using {@link AttachableStandard#attach()}.
         *
         * @param <ParentT> the parent definition {@link AttachableStandard#attach()} returns to
         */
        interface AttachablePremium<ParentT> {
            @Method
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
             * The stage of the CDN profile endpoint definition allowing to specify the origin.
             *
             * @param <ParentT> the return type of {@link AttachableStandard#attach()}
             */
            interface StandardEndpoint<ParentT> {
                /**
                 * Specifies the origin of the CDN endpoint.
                 *
                 * @param originName name of the origin.
                 * @param originHostName origin host name.
                 * @return the next stage of the definition
                 */
                UpdateDefinitionStages.WithStandardAttach<ParentT> withOrigin(String originName, String originHostName);

                /**
                 * Specifies the origin of the CDN endpoint.
                 *
                 * @param originHostName origin host name.
                 * @return the next stage of the definition
                 */
                UpdateDefinitionStages.WithStandardAttach<ParentT> withOrigin(String originHostName);
            }

            /**
             * The stage of the CDN profile endpoint definition allowing to specify the origin
             * for CDN Profile with Premium Verizon sku.
             *
             * @param <ParentT> the return type of {@link AttachablePremium#attach()}
             */
            interface PremiumEndpoint<ParentT> {
                /**
                 * Specifies the origin of the CDN endpoint.
                 *
                 * @param originName name of the origin.
                 * @param originHostName origin host name.
                 * @return the next stage of the definition
                 */
                UpdateDefinitionStages.WithPremiumAttach<ParentT> withPremiumOrigin(String originName, String originHostName);

                /**
                 * Specifies the origin of the CDN endpoint.
                 *
                 * @param originHostName origin host name.
                 * @return the next stage of the definition
                 */
                UpdateDefinitionStages.WithPremiumAttach<ParentT> withPremiumOrigin(String originHostName);
            }
        }

        /** The final stage of the CDN profile Standard Akamai or Standard Verizon endpoint definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the CDN profile endpoint
         * definition can be attached to the parent CDN profile definition using {@link CdnEndpoint.DefinitionStages.AttachableStandard#attach()}.
         * @param <ParentT> the return type of {@link CdnEndpoint.DefinitionStages.AttachableStandard#attach()}
         */
        interface WithStandardAttach<ParentT>
                extends AttachableStandard<ParentT> {
            /**
             * Specifies origin path.
             *
             * @param originPath origin path.
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withOriginPath(String originPath);

            /**
             * Specifies host header.
             *
             * @param hostHeader host header.
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withHostHeader(String hostHeader);

            /**
             * Specifies if http traffic is allowed.
             *
             * @param httpAllowed if set to true Http traffic will be allowed.
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withHttpAllowed(boolean httpAllowed);

            /**
             * Specifies if https traffic is allowed.
             *
             * @param httpsAllowed if set to true Https traffic will be allowed.
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withHttpsAllowed(boolean httpsAllowed);

            /**
             * Specifies http port for http traffic.
             *
             * @param httpPort http port number.
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withHttpPort(int httpPort);

            /**
             * Specifies https port for http traffic.
             *
             * @param httpsPort https port number.
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withHttpsPort(int httpsPort);

            /**
             * Specifies the content types to compress.
             *
             * @param contentTypesToCompress the list of content types to compress to set
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withContentTypesToCompress(List<String> contentTypesToCompress);

            /**
             * Specifies a single content type to compress.
             *
             * @param contentTypeToCompress a singe content type to compress to add to the list
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withContentTypeToCompress(String contentTypeToCompress);

            /**
             * Sets the compression state.
             *
             * @param compressionEnabled if set to true compression will be enabled. If set to false compression will be disabled
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withCompressionEnabled(boolean compressionEnabled);

            /**
             * Sets the query string caching behavior.
             *
             * @param cachingBehavior the query string caching behavior value to set
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withQueryStringCachingBehavior(QueryStringCachingBehavior cachingBehavior);

            /**
             * Sets the geo filters list.
             *
             * @param geoFilters the Geo filters list to set
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withGeoFilters(List<GeoFilter> geoFilters);

            /**
             * Adds a single entry to the Geo filters list.
             *
             * @param relativePath the relative path.
             * @param action the action value.
             * @param countryCode the ISO 2 letter country codes.
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withGeoFilter(String relativePath, GeoFilterActions action, CountryISOCode countryCode);

            /**
             * Sets the geo filters list for the specified countries list.
             *
             * @param relativePath the relative path.
             * @param action the action value.
             * @param countryCodes a list of the ISO 2 letter country codes.
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withGeoFilter(String relativePath, GeoFilterActions action, List<CountryISOCode> countryCodes);

            /**
             * Adds a new CDN custom domain within an endpoint.
             *
             * @param hostName custom domain host name.
             * @return the next stage of the endpoint definition
             */
            WithStandardAttach<ParentT> withCustomDomain(String hostName);
        }

        /** The final stage of the CDN profile Premium Verizon endpoint definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the CDN profile endpoint
         * definition can be attached to the parent CDN profile definition using {@link CdnEndpoint.DefinitionStages.AttachablePremium#attach()}.
         * @param <ParentT> the return type of {@link CdnEndpoint.DefinitionStages.AttachablePremium#attach()}
         */
        interface WithPremiumAttach<ParentT>
                extends AttachablePremium<ParentT> {
            /**
             * Specifies origin path.
             *
             * @param originPath origin path.
             * @return the next stage of the endpoint definition
             */
            WithPremiumAttach<ParentT> withOriginPath(String originPath);

            /**
             * Specifies host header.
             *
             * @param hostHeader host header.
             * @return the next stage of the endpoint definition
             */
            WithPremiumAttach<ParentT> withHostHeader(String hostHeader);

            /**
             * Specifies if http traffic is allowed.
             *
             * @param httpAllowed if set to true Http traffic will be allowed.
             * @return the next stage of the endpoint definition
             */
            WithPremiumAttach<ParentT> withHttpAllowed(boolean httpAllowed);

            /**
             * Specifies if https traffic is allowed.
             *
             * @param httpsAllowed if set to true Https traffic will be allowed.
             * @return the next stage of the endpoint definition
             */
            WithPremiumAttach<ParentT> withHttpsAllowed(boolean httpsAllowed);

            /**
             * Specifies http port for http traffic.
             *
             * @param httpPort http port number.
             * @return the next stage of the endpoint definition
             */
            WithPremiumAttach<ParentT> withHttpPort(int httpPort);

            /**
             * Specifies https port for http traffic.
             *
             * @param httpsPort https port number.
             * @return the next stage of the endpoint definition
             */
            WithPremiumAttach<ParentT> withHttpsPort(int httpsPort);

            /**
             * Adds a new CDN custom domain within an endpoint.
             *
             * @param hostName custom domain host name.
             * @return the next stage of the endpoint definition
             */
            WithPremiumAttach<ParentT> withCustomDomain(String hostName);
        }

        /**
         * The final stage of the Standard endpoint object definition, at which it can be attached to the parent, using {@link AttachableStandard#attach()}.
         *
         * @param <ParentT> the parent definition {@link AttachableStandard#attach()} returns to
         */
        interface AttachableStandard<ParentT> {
            @Method
            ParentT attach();
        }

        /**
         * The final stage of the Premium Verizon endpoint object definition, at which it can be attached to the parent, using {@link AttachableStandard#attach()}.
         *
         * @param <ParentT> the parent definition {@link AttachableStandard#attach()} returns to
         */
        interface AttachablePremium<ParentT> {
            @Method
            ParentT attach();
        }
    }

    /**
     * The stage of an CDN profile endpoint update allowing to specify endpoint properties.
     */
    interface UpdateStandardEndpoint extends Update {
        /**
         * Specifies origin path.
         *
         * @param originPath origin path.
         * @return the next stage of the endpoint update
         */
        UpdateStandardEndpoint withOriginPath(String originPath);

        /**
         * Specifies host header.
         *
         * @param hostHeader host header.
         * @return the next stage of the endpoint update
         */
        UpdateStandardEndpoint withHostHeader(String hostHeader);
        /**
         * Specifies if http traffic is allowed.
         *
         * @param httpAllowed if set to true Http traffic will be allowed.
         * @return the next stage of the endpoint update
         */
        UpdateStandardEndpoint withHttpAllowed(boolean httpAllowed);

        /**
         * Specifies if https traffic is allowed.
         *
         * @param httpsAllowed if set to true Https traffic will be allowed.
         * @return the next stage of the endpoint update
         */
        UpdateStandardEndpoint withHttpsAllowed(boolean httpsAllowed);

        /**
         * Specifies http port for http traffic.
         *
         * @param httpPort http port number.
         * @return the next stage of the endpoint update
         */
        UpdateStandardEndpoint withHttpPort(int httpPort);

        /**
         * Specifies https port for http traffic.
         *
         * @param httpsPort https port number.
         * @return the next stage of the endpoint update
         */
        UpdateStandardEndpoint withHttpsPort(int httpsPort);

        /**
         * Specifies the content types to compress.
         *
         * @param contentTypesToCompress the list of content types to compress to set
         * @return the next stage of the endpoint definition
         */
        UpdateStandardEndpoint withContentTypesToCompress(List<String> contentTypesToCompress);

        /**
         * Clears entire list of content types to compress .
         *
         * @return the next stage of the endpoint update
         */
        UpdateStandardEndpoint withoutContentTypesToCompress();

        /**
         * Specifies a single content type to compress.
         *
         * @param contentTypeToCompress a singe content type to compress to add to the list
         * @return the next stage of the endpoint definition
         */
        UpdateStandardEndpoint withContentTypeToCompress(String contentTypeToCompress);

        /**
         * Removes the content type  to compress value from the list.
         *
         * @param contentTypeToCompress a singe content type to remove from the list
         * @return the next stage of the endpoint update
         */
        UpdateStandardEndpoint withoutContentTypeToCompress(String contentTypeToCompress);

        /**
         * Sets the compression state.
         *
         * @param compressionEnabled if set to true compression will be enabled. If set to false compression will be disabled
         * @return the next stage of the endpoint definition
         */
        UpdateStandardEndpoint withCompressionEnabled(boolean compressionEnabled);

        /**
         * Sets the query string caching behavior.
         *
         * @param cachingBehavior the query string caching behavior value to set
         * @return the next stage of the endpoint definition
         */
        UpdateStandardEndpoint withQueryStringCachingBehavior(QueryStringCachingBehavior cachingBehavior);

        /**
         * Sets the geo filters list.
         *
         * @param geoFilters the Geo filters list to set
         * @return the next stage of the endpoint definition
         */
        UpdateStandardEndpoint withGeoFilters(List<GeoFilter> geoFilters);

        /**
         * Clears entire geo filters list.
         *
         * @return the next stage of the endpoint update
         */
        UpdateStandardEndpoint withoutGeoFilters();

        /**
         * Adds a single entry to the Geo filters list.
         *
         * @param relativePath the relative path.
         * @param action the action value.
         * @param countryCode the ISO 2 letter country codes.
         * @return the next stage of the endpoint definition
         */
        UpdateStandardEndpoint withGeoFilter(String relativePath, GeoFilterActions action, CountryISOCode countryCode);

        /**
         * Sets the geo filters list for the specified countries list.
         *
         * @param relativePath the relative path.
         * @param action the action value.
         * @param countryCodes a list of the ISO 2 letter country codes.
         * @return the next stage of the endpoint definition
         */
        UpdateStandardEndpoint withGeoFilter(String relativePath, GeoFilterActions action, List<CountryISOCode> countryCodes);

        /**
         * Removes an entry from the geo filters list.
         *
         * @param relativePath the relative path value.
         * @return the next stage of the endpoint update
         */
        UpdateStandardEndpoint withoutGeoFilter(String relativePath);

        /**
         * Adds a new CDN custom domain within an endpoint.
         *
         * @param hostName custom domain host name.
         * @return the next stage of the endpoint update
         */
        UpdateStandardEndpoint withCustomDomain(String hostName);

        /**
         * Removes CDN custom domain within an endpoint.
         *
         * @param hostName custom domain host name.
         * @return the next stage of the endpoint update
         */
        UpdateStandardEndpoint withoutCustomDomain(String hostName);
    }

    /**
     * The stage of an CDN profile endpoint update allowing to specify endpoint properties.
     */
    interface UpdatePremiumEndpoint extends Update {
        /**
         * Specifies origin path.
         *
         * @param originPath origin path.
         * @return the next stage of the endpoint update
         */
        UpdatePremiumEndpoint withOriginPath(String originPath);

        /**
         * Specifies host header.
         *
         * @param hostHeader host header.
         * @return the next stage of the endpoint update
         */
        UpdatePremiumEndpoint withHostHeader(String hostHeader);

        /**
         * Specifies if http traffic is allowed.
         *
         * @param httpAllowed if set to true Http traffic will be allowed.
         * @return the next stage of the endpoint update
         */
        UpdatePremiumEndpoint withHttpAllowed(boolean httpAllowed);

        /**
         * Specifies if https traffic is allowed.
         *
         * @param httpsAllowed if set to true Https traffic will be allowed.
         * @return the next stage of the endpoint update
         */
        UpdatePremiumEndpoint withHttpsAllowed(boolean httpsAllowed);

        /**
         * Specifies http port for http traffic.
         *
         * @param httpPort http port number.
         * @return the next stage of the endpoint update
         */
        UpdatePremiumEndpoint withHttpPort(int httpPort);

        /**
         * Specifies https port for http traffic.
         *
         * @param httpsPort https port number.
         * @return the next stage of the endpoint update
         */
        UpdatePremiumEndpoint withHttpsPort(int httpsPort);

        /**
         * Adds a new CDN custom domain within an endpoint.
         *
         * @param hostName custom domain host name.
         * @return the next stage of the endpoint update
         */
        UpdatePremiumEndpoint withCustomDomain(String hostName);

        /**
         * Removes CDN custom domain within an endpoint.
         *
         * @param hostName custom domain host name.
         * @return the next stage of the endpoint update
         */
        UpdatePremiumEndpoint withoutCustomDomain(String hostName);
    }

    /**
     * The set of configurations that can be updated for all endpoint irrespective of their type.
     */
    interface Update extends
            Settable<CdnProfile.Update> {
    }
}