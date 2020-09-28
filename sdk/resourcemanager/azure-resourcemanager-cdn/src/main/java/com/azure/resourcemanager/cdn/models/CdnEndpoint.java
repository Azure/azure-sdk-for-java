// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.cdn.fluent.models.EndpointInner;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryIsoCode;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Set;

/**
 * An immutable client-side representation of an Azure CDN endpoint.
 */
@Fluent
public interface CdnEndpoint extends
    ExternalChildResource<CdnEndpoint, CdnProfile>,
        HasInnerModel<EndpointInner> {

    /**
     * @return origin host header
     */
    String originHostHeader();

    /**
     * @return origin path
     */
    String originPath();

    /**
     * @return content types to be compressed
     */
    Set<String> contentTypesToCompress();

    /**
     * @return true if content compression is enabled, otherwise false
     */
    boolean isCompressionEnabled();

    /**
     * @return true if HTTP traffic is allowed, otherwise false.
     */
    boolean isHttpAllowed();

    /**
     * @return true if HTTPS traffic is allowed, otherwise false
     */
    boolean isHttpsAllowed();

    /**
     * @return query string caching behavior
     */
    QueryStringCachingBehavior queryStringCachingBehavior();

    /**
     * @return optimization type
     */
    String optimizationType();

    /**
     * @return list of Geo filters
     */
    Collection<GeoFilter> geoFilters();

    /**
     * @return endpoint host name
     */
    String hostname();

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
     * @return HTTP port value
     */
    int httpPort();

    /**
     * @return HTTPS port value
     */
    int httpsPort();

    /**
     * @return custom domains associated with this endpoint
     */
    Set<String> customDomains();

    /**
     * Starts the CDN endpoint, if it is stopped.
     */
    void start();

    /**
     * Starts the CDN endpoint asynchronously, if it is stopped.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> startAsync();

    /**
     * Stops the CDN endpoint, if it is running.
     */
    void stop();

    /**
     * Stops the CDN endpoint asynchronously, if it is running.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> stopAsync();

    /**
     * Forcibly purges the content of the CDN endpoint.
     *
     * @param contentPaths the paths to the content to be purged, which can be file paths or directory wild cards.
     */
    void purgeContent(Set<String> contentPaths);

    /**
     * Forcibly purges the content of the CDN endpoint asynchronously.
     *
     * @param contentPaths the paths to the content to be purged, which can be file paths or directory wild cards.
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> purgeContentAsync(Set<String> contentPaths);

    /**
     * Forcibly preloads the content of the CDN endpoint.
     * <p>
     * Note: this is supported for Verizon profiles only.
     *
     * @param contentPaths the file paths to the content to be loaded
     */
    void loadContent(Set<String> contentPaths);

    /**
     * Forcibly preloads the content of the CDN endpoint asynchronously.
     * <p>
     * Note: this is supported for Verizon profiles only.
     *
     * @param contentPaths the file paths to the content to be loaded
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> loadContentAsync(Set<String> contentPaths);

    /**
     * Validates a custom domain mapping to ensure it maps to the correct CNAME in DNS for current endpoint.
     *
     * @param hostName the host name, which must be a domain name, of the custom domain
     * @return the result of the action, if successful.
     */
    CustomDomainValidationResult validateCustomDomain(String hostName);

    /**
     * Validates a custom domain mapping to ensure it maps to the correct CNAME
     * in DNS for current endpoint asynchronously.
     *
     * @param hostName the host name, which must be a domain name, of the custom domain
     * @return an observable of the result
     */
    Mono<CustomDomainValidationResult> validateCustomDomainAsync(String hostName);

    /**
     * Checks the quota and usage of geo filters and custom domains under the current endpoint.
     *
     * @return list of quotas and usages of geo filters and custom domains under the current endpoint
     */
    PagedIterable<ResourceUsage> listResourceUsage();

    /**
     * Grouping of CDN profile endpoint definition stages as a part of parent CDN profile definition.
     */
    interface DefinitionStages {
        /**
         * The first stage of a CDN profile endpoint definition.
         */
        interface Blank {
            /**
             * The stage of a CDN profile endpoint definition allowing to specify the origin.
             *
             * @param <ParentT> the stage of the parent CDN profile definition to
             *                 return to after attaching this definition
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
             * The stage of a CDN profile endpoint definition allowing to specify the origin
             * for the CDN profile with teh Premium Verizon SKU.
             *
             * @param <ParentT> the stage of the parent CDN profile definition to
             *                 return to after attaching this definition
             */
            interface PremiumEndpoint<ParentT> {
                /**
                 * Specifies the origin of the CDN endpoint.
                 *
                 * @param originName name of the origin
                 * @param originHostName origin hostname
                 * @return the next stage of the definition
                 */
                DefinitionStages.WithPremiumAttach<ParentT> withPremiumOrigin(String originName, String originHostName);

                /**
                 * Specifies the origin of the CDN endpoint.
                 *
                 * @param originHostName origin hostname
                 * @return the next stage of the definition
                 */
                DefinitionStages.WithPremiumAttach<ParentT> withPremiumOrigin(String originHostName);
            }
        }

        /** The final stage of the CDN profile Standard Akamai or Standard Verizon endpoint definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the CDN profile endpoint
         * definition can be attached to the parent CDN profile definition
         * @param <ParentT> the stage of the parent CDN profile definition to return to after attaching this definition
         */
        interface WithStandardAttach<ParentT> extends AttachableStandard<ParentT> {
            /**
             * Specifies the origin path.
             *
             * @param originPath an origin path
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withOriginPath(String originPath);

            /**
             * Specifies the host header.
             *
             * @param hostHeader a host header.
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withHostHeader(String hostHeader);

            /**
             * Specifies if HTTP traffic is allowed.
             *
             * @param httpAllowed if true then HTTP traffic will be allowed
             * @return the next stage of the definition
             */
            // TODO: add withHttp()/withoutHttp()
            WithStandardAttach<ParentT> withHttpAllowed(boolean httpAllowed);

            /**
             * Specifies if HTTPS traffic is allowed.
             *
             * @param httpsAllowed if set to true Https traffic will be allowed.
             * @return the next stage of the definition
             */
            // TODO: add withHttps()/withoutHttps()
            WithStandardAttach<ParentT> withHttpsAllowed(boolean httpsAllowed);

            /**
             * Specifies the port for HTTP traffic.
             *
             * @param httpPort a port number.
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withHttpPort(int httpPort);

            /**
             * Specifies the port for HTTPS traffic.
             *
             * @param httpsPort HTTPS port number.
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withHttpsPort(int httpsPort);

            /**
             * Specifies the content types to compress.
             *
             * @param contentTypesToCompress content types to compress to set
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withContentTypesToCompress(Set<String> contentTypesToCompress);

            /**
             * Specifies a single content type to compress.
             *
             * @param contentTypeToCompress a content type to compress to add to the list
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withContentTypeToCompress(String contentTypeToCompress);

            /**
             * Sets the compression state.
             *
             * @param compressionEnabled if true then compression will be enabled
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withCompressionEnabled(boolean compressionEnabled);

            /**
             * Selects the query string caching behavior.
             *
             * @param cachingBehavior the query string caching behavior value to set
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withQueryStringCachingBehavior(QueryStringCachingBehavior cachingBehavior);

            /**
             * Specifies the geo filters to use.
             *
             * @param geoFilters geo filters
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withGeoFilters(Collection<GeoFilter> geoFilters);

            /**
             * Adds a single entry to the geo filters list.
             *
             * @param relativePath a relative path
             * @param action a geo filter action
             * @param countryCode an ISO 2 letter country code
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withGeoFilter(
                String relativePath, GeoFilterActions action, CountryIsoCode countryCode);

            /**
             * Sets the geo filters list for the specified countries list.
             *
             * @param relativePath a relative path
             * @param action an action value
             * @param countryCodes a list of the ISO 2 letter country codes.
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withGeoFilter(
                String relativePath, GeoFilterActions action, Collection<CountryIsoCode> countryCodes);

            /**
             * Adds a new CDN custom domain within an endpoint.
             *
             * @param hostName a custom domain host name
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withCustomDomain(String hostName);
        }

        /** The final stage of the CDN profile Premium Verizon endpoint definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the CDN profile endpoint
         * definition can be attached to the parent CDN profile definition.
         * @param <ParentT> the stage of the parent CDN profile definition to return to after attaching this definition
         */
        interface WithPremiumAttach<ParentT>
                extends AttachablePremium<ParentT> {
            /**
             * Specifies the origin path.
             *
             * @param originPath an origin path.
             * @return the next stage of the definition
             */
            WithPremiumAttach<ParentT> withOriginPath(String originPath);

            /**
             * Specifies the host header.
             *
             * @param hostHeader a host header.
             * @return the next stage of the definition
             */
            WithPremiumAttach<ParentT> withHostHeader(String hostHeader);

            /**
             * Specifies if HTTP traffic is allowed.
             *
             * @param httpAllowed if true, then HTTP traffic will be allowed
             * @return the next stage of the definition
             */
            // TODO: add withHttp()/withoutHttp()
            WithPremiumAttach<ParentT> withHttpAllowed(boolean httpAllowed);

            /**
             * Specifies if HTTPS traffic is allowed.
             *
             * @param httpsAllowed if true then HTTPS traffic will be allowed
             * @return the next stage of the definition
             */
            WithPremiumAttach<ParentT> withHttpsAllowed(boolean httpsAllowed);

            /**
             * Specifies the port for HTTP traffic.
             *
             * @param httpPort a port number.
             * @return the next stage of the definition
             */
            WithPremiumAttach<ParentT> withHttpPort(int httpPort);

            /**
             * Specifies the port for HTTPS traffic.
             *
             * @param httpsPort a port number.
             * @return the next stage of the definition
             */
            WithPremiumAttach<ParentT> withHttpsPort(int httpsPort);

            /**
             * Adds a new CDN custom domain for the endpoint.
             *
             * @param hostName a custom domain host name
             * @return the next stage of the definition
             */
            WithPremiumAttach<ParentT> withCustomDomain(String hostName);
        }

        /**
         * The final stage of a Standard endpoint definition, at which it can be attached to the parent.
         *
         * @param <ParentT> the stage of the parent CDN profile definition to return to after attaching this definition
         */
        interface AttachableStandard<ParentT> {
            /**
             * Attaches the defined endpoint to the parent CDN profile.
             * @return the stage of the parent CDN profile definition to return to after attaching this definition
             */
            ParentT attach();
        }

        /**
         * The final stage of the Premium Verizon endpoint definition, at which it can be attached to the parent.
         *
         * @param <ParentT> the stage of the parent CDN profile definition to return to after attaching this definition
         */
        interface AttachablePremium<ParentT> {
            /**
             * Attaches the defined endpoint to the parent CDN profile.
             * @return the stage of the parent CDN profile definition to return to after attaching this definition
             */
            ParentT attach();
        }
    }

    /**
     * The entirety of a CDN profile endpoint definition as a part of a parent CDN profile update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a CDN profile endpoint definition.
         */
        interface Blank {
            /**
             * The stage of the CDN profile endpoint definition allowing to specify the origin.
             *
             * @param <ParentT> the stage of the parent CDN profile update to return to after attaching this definition
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
             * for CDN Profile with the Premium Verizon SKU.
             *
             * @param <ParentT> the stage of the parent CDN profile update to return to after attaching this definition
             */
            interface PremiumEndpoint<ParentT> {
                /**
                 * Specifies the origin of the CDN endpoint.
                 *
                 * @param originName name of the origin
                 * @param originHostName origin host name
                 * @return the next stage of the definition
                 */
                UpdateDefinitionStages.WithPremiumAttach<ParentT> withPremiumOrigin(
                    String originName, String originHostName);

                /**
                 * Specifies the origin of the CDN endpoint.
                 *
                 * @param originHostName origin host name
                 * @return the next stage of the definition
                 */
                UpdateDefinitionStages.WithPremiumAttach<ParentT> withPremiumOrigin(String originHostName);
            }
        }

        /**
         * The final stage of the CDN profile Standard Akamai or Standard Verizon endpoint definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the CDN profile endpoint
         * definition can be attached to the parent CDN profile definition
         * @param <ParentT> the stage of the parent CDN profile update to return to after attaching this definition
         */
        interface WithStandardAttach<ParentT>
                extends AttachableStandard<ParentT> {
            /**
             * Specifies the origin path.
             *
             * @param originPath an origin path
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withOriginPath(String originPath);

            /**
             * Specifies the host header.
             *
             * @param hostHeader a host header
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withHostHeader(String hostHeader);

            /**
             * Specifies if HTTP traffic is allowed.
             *
             * @param httpAllowed if true then HTTP traffic will be allowed
             * @return the next stage of the definition
             */
            // TODO: withHttp()/withoutHttp()
            WithStandardAttach<ParentT> withHttpAllowed(boolean httpAllowed);

            /**
             * Specifies if HTTPS traffic is allowed.
             *
             * @param httpsAllowed if true then HTTPS traffic will be allowed
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withHttpsAllowed(boolean httpsAllowed);

            /**
             * Specifies the port for HTTP traffic.
             *
             * @param httpPort a port number
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withHttpPort(int httpPort);

            /**
             * Specifies the port for HTTPS traffic.
             *
             * @param httpsPort a port number.
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withHttpsPort(int httpsPort);

            /**
             * Specifies the content types to compress.
             *
             * @param contentTypesToCompress content types to compress to set
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withContentTypesToCompress(Set<String> contentTypesToCompress);

            /**
             * Specifies a single content type to compress.
             *
             * @param contentTypeToCompress a single content type to compress to add to the list
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withContentTypeToCompress(String contentTypeToCompress);

            /**
             * Sets the compression state.
             *
             * @param compressionEnabled if true then compression will be enabled, else disabled
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withCompressionEnabled(boolean compressionEnabled);

            /**
             * Sets the query string caching behavior.
             *
             * @param cachingBehavior a query string caching behavior
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withQueryStringCachingBehavior(QueryStringCachingBehavior cachingBehavior);

            /**
             * Specifies the geo filters to use.
             *
             * @param geoFilters geo filters
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withGeoFilters(Collection<GeoFilter> geoFilters);

            /**
             * Adds a single entry to the geo filters list.
             *
             * @param relativePath a relative path
             * @param action an action
             * @param countryCode an ISO 2 letter country code
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withGeoFilter(
                String relativePath, GeoFilterActions action, CountryIsoCode countryCode);

            /**
             * Sets the geo filters list for the specified countries list.
             *
             * @param relativePath a relative path
             * @param action an action
             * @param countryCodes a list of ISO 2 letter country codes
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withGeoFilter(
                String relativePath, GeoFilterActions action, Collection<CountryIsoCode> countryCodes);

            /**
             * Adds a new CDN custom domain within an endpoint.
             *
             * @param hostName a custom domain host name
             * @return the next stage of the definition
             */
            WithStandardAttach<ParentT> withCustomDomain(String hostName);
        }

        /**
         * The final stage of a CDN profile Premium Verizon endpoint definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the CDN profile endpoint
         * definition can be attached to the parent CDN profile definition
         * @param <ParentT> the stage of the parent CDN profile update to return to after attaching this definition
         */
        interface WithPremiumAttach<ParentT>
                extends AttachablePremium<ParentT> {
            /**
             * Specifies the origin path.
             *
             * @param originPath an origin path
             * @return the next stage of the definition
             */
            WithPremiumAttach<ParentT> withOriginPath(String originPath);

            /**
             * Specifies the host header.
             *
             * @param hostHeader a host header.
             * @return the next stage of the definition
             */
            WithPremiumAttach<ParentT> withHostHeader(String hostHeader);

            /**
             * Specifies if HTTP traffic is allowed.
             *
             * @param httpAllowed if true then HTTP traffic will be allowed.
             * @return the next stage of the definition
             */
            // TODO: withHttp/withoutHttp
            WithPremiumAttach<ParentT> withHttpAllowed(boolean httpAllowed);

            /**
             * Specifies if HTTPS traffic is allowed.
             *
             * @param httpsAllowed if true then HTTPS traffic will be allowed.
             * @return the next stage of the definition
             */
            WithPremiumAttach<ParentT> withHttpsAllowed(boolean httpsAllowed);

            /**
             * Specifies the port for HTTP traffic.
             *
             * @param httpPort a port number.
             * @return the next stage of the definition
             */
            WithPremiumAttach<ParentT> withHttpPort(int httpPort);

            /**
             * Specifies the port for HTTPS traffic.
             *
             * @param httpsPort a port number.
             * @return the next stage of the definition
             */
            WithPremiumAttach<ParentT> withHttpsPort(int httpsPort);

            /**
             * Adds a new CDN custom domain within an endpoint.
             *
             * @param hostName custom domain host name.
             * @return the next stage of the definition
             */
            WithPremiumAttach<ParentT> withCustomDomain(String hostName);
        }

        /**
         * The final stage of the Standard endpoint object definition, at which it can be attached to the parent.
         *
         * @param <ParentT> the stage of the parent CDN profile update to return to after attaching this definition
         */
        interface AttachableStandard<ParentT> {
            /**
             * Attaches the endpoint definition to the parent CDN profile update.
             * @return the stage of the parent CDN profile update to return to after attaching this definition
             */
            ParentT attach();
        }

        /**
         * The final stage of the Premium Verizon endpoint object definition, at which it can be attached to the parent.
         *
         * @param <ParentT> the stage of the parent CDN profile update to return to after attaching this definition
         */
        interface AttachablePremium<ParentT> {
            /**
             * Attaches the endpoint definition to the parent CDN profile update.
             * @return the stage of the parent CDN profile update to return to after attaching this definition
             */
            ParentT attach();
        }
    }

    /**
     * The stage of an CDN profile endpoint update allowing to specify endpoint properties.
     */
    interface UpdateStandardEndpoint extends Update {
        /**
         * Specifies the origin path.
         *
         * @param originPath an origin path
         * @return the next stage of the endpoint update
         */
        UpdateStandardEndpoint withOriginPath(String originPath);

        /**
         * Specifies the host header.
         *
         * @param hostHeader a host header.
         * @return the next stage of the endpoint update
         */
        UpdateStandardEndpoint withHostHeader(String hostHeader);

        /**
         * Specifies if HTTP traffic is allowed.
         *
         * @param httpAllowed if true then HTTP traffic will be allowed
         * @return the next stage of the endpoint update
         */
        UpdateStandardEndpoint withHttpAllowed(boolean httpAllowed);

        /**
         * Specifies if HTTPS traffic is allowed.
         *
         * @param httpsAllowed if true then HTTPS traffic will be allowed.
         * @return the next stage of the endpoint update
         */
        UpdateStandardEndpoint withHttpsAllowed(boolean httpsAllowed);

        /**
         * Specifies the port for HTTP traffic.
         *
         * @param httpPort a port number.
         * @return the next stage of the endpoint update
         */
        UpdateStandardEndpoint withHttpPort(int httpPort);

        /**
         * Specifies the port for HTTP traffic.
         *
         * @param httpsPort a port number.
         * @return the next stage of the endpoint update
         */
        UpdateStandardEndpoint withHttpsPort(int httpsPort);

        /**
         * Specifies the content types to compress.
         *
         * @param contentTypesToCompress content types to compress to set
         * @return the next stage of the definition
         */
        UpdateStandardEndpoint withContentTypesToCompress(Set<String> contentTypesToCompress);

        /**
         * Clears entire list of content types to compress.
         *
         * @return the next stage of the endpoint update
         */
        UpdateStandardEndpoint withoutContentTypesToCompress();

        /**
         * Specifies a single content type to compress.
         *
         * @param contentTypeToCompress a single content type to compress to add to the list
         * @return the next stage of the definition
         */
        UpdateStandardEndpoint withContentTypeToCompress(String contentTypeToCompress);

        /**
         * Removes the content type to compress from the list.
         *
         * @param contentTypeToCompress a single content type to remove from the list
         * @return the next stage of the endpoint update
         */
        UpdateStandardEndpoint withoutContentTypeToCompress(String contentTypeToCompress);

        /**
         * Sets the compression state.
         *
         * @param compressionEnabled if true then compression will be enabled
         * @return the next stage of the definition
         */
        // TODO: withCompression/withoutCompression
        UpdateStandardEndpoint withCompressionEnabled(boolean compressionEnabled);

        /**
         * Sets the query string caching behavior.
         *
         * @param cachingBehavior the query string caching behavior value to set
         * @return the next stage of the definition
         */
        UpdateStandardEndpoint withQueryStringCachingBehavior(QueryStringCachingBehavior cachingBehavior);

        /**
         * Specifies the geo filters to use.
         *
         * @param geoFilters geo filters
         * @return the next stage of the definition
         */
        UpdateStandardEndpoint withGeoFilters(Collection<GeoFilter> geoFilters);

        /**
         * Clears entire geo filters list.
         *
         * @return the next stage of the endpoint update
         */
        UpdateStandardEndpoint withoutGeoFilters();

        /**
         * Adds a single entry to the Geo filters list.
         *
         * @param relativePath a relative path
         * @param action an action
         * @param countryCode an ISO 2 letter country code
         * @return the next stage of the definition
         */
        UpdateStandardEndpoint withGeoFilter(String relativePath, GeoFilterActions action, CountryIsoCode countryCode);

        /**
         * Sets the geo filters list for the specified countries list.
         *
         * @param relativePath a relative path
         * @param action an action
         * @param countryCodes a list of ISO 2 letter country codes
         * @return the next stage of the definition
         */
        UpdateStandardEndpoint withGeoFilter(
            String relativePath, GeoFilterActions action, Collection<CountryIsoCode> countryCodes);

        /**
         * Removes an entry from the geo filters list.
         *
         * @param relativePath a relative path
         * @return the next stage of the endpoint update
         */
        UpdateStandardEndpoint withoutGeoFilter(String relativePath);

        /**
         * Adds a new CDN custom domain within an endpoint.
         *
         * @param hostName custom domain host name
         * @return the next stage of the endpoint update
         */
        UpdateStandardEndpoint withCustomDomain(String hostName);

        /**
         * Removes CDN custom domain within an endpoint.
         *
         * @param hostName a custom domain host name
         * @return the next stage of the endpoint update
         */
        UpdateStandardEndpoint withoutCustomDomain(String hostName);
    }

    /**
     * The stage of an CDN profile endpoint update allowing to specify endpoint properties.
     */
    interface UpdatePremiumEndpoint extends Update {
        /**
         * Specifies the origin path.
         *
         * @param originPath an origin path
         * @return the next stage of the endpoint update
         */
        UpdatePremiumEndpoint withOriginPath(String originPath);

        /**
         * Specifies the host header.
         *
         * @param hostHeader a host header
         * @return the next stage of the endpoint update
         */
        UpdatePremiumEndpoint withHostHeader(String hostHeader);

        /**
         * Specifies if HTTP traffic is allowed.
         *
         * @param httpAllowed if true then HTTP traffic will be allowed.
         * @return the next stage of the endpoint update
         */
        // TODO: withHttp/withoutHttp
        UpdatePremiumEndpoint withHttpAllowed(boolean httpAllowed);

        /**
         * Specifies if HTTPS traffic is allowed.
         *
         * @param httpsAllowed if true then HTTPS traffic will be allowed.
         * @return the next stage of the endpoint update
         */
        UpdatePremiumEndpoint withHttpsAllowed(boolean httpsAllowed);

        /**
         * Specifies the port for HTTP traffic.
         *
         * @param httpPort a port number.
         * @return the next stage of the endpoint update
         */
        UpdatePremiumEndpoint withHttpPort(int httpPort);

        /**
         * Specifies the port for HTTPS traffic.
         *
         * @param httpsPort a port number.
         * @return the next stage of the endpoint update
         */
        UpdatePremiumEndpoint withHttpsPort(int httpsPort);

        /**
         * Adds a new CDN custom domain within an endpoint.
         *
         * @param hostName a custom domain host name.
         * @return the next stage of the endpoint update
         */
        UpdatePremiumEndpoint withCustomDomain(String hostName);

        /**
         * Removes CDN custom domain within an endpoint.
         *
         * @param hostName a custom domain host name.
         * @return the next stage of the endpoint update
         */
        UpdatePremiumEndpoint withoutCustomDomain(String hostName);
    }

    /**
     * The entirety of a CDN endpoint update as part of a CDN profile update.
     */
    interface Update extends
        Settable<CdnProfile.Update> {
    }
}
