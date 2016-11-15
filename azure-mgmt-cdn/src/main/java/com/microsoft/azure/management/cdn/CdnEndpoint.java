package com.microsoft.azure.management.cdn;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.cdn.implementation.EndpointInner;
import com.microsoft.azure.management.resources.fluentcore.arm.CountryISOCode;
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

    List<String> customDomains();

    // Actions
    void start();
    void stop();
    void purgeContent(List<String> contentPaths);
    void loadContent(List<String> contentPaths);
    CustomDomainValidationResult validateCustomDomain(String hostName);

    interface DefinitionStages {
        interface Blank {
            interface StandardEndpoint<ParentT> {
                DefinitionStages.WithStandardAttach<ParentT> withOrigin(String originName, String originHostName);
                DefinitionStages.WithStandardAttach<ParentT> withOrigin(String originHostName);
            }

            interface PremiumEndpoint<ParentT> {
                DefinitionStages.WithPremiumAttach<ParentT> withPremiumOrigin(String originName, String originHostName);
                DefinitionStages.WithPremiumAttach<ParentT> withPremiumOrigin(String originHostName);
            }
        }

        interface WithStandardAttach<ParentT>
                extends AttachableStandard<ParentT>
        {
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

        interface WithPremiumAttach<ParentT>
                extends AttachablePremium<ParentT>
        {
            WithPremiumAttach<ParentT> withOriginPath(String originPath);
            WithPremiumAttach<ParentT> withHostHeader(String hostHeader);
            WithPremiumAttach<ParentT> withHttpAllowed(boolean httpAllowed);
            WithPremiumAttach<ParentT> withHttpsAllowed(boolean httpsAllowed);
            WithPremiumAttach<ParentT> withHttpPort(int httpPort);
            WithPremiumAttach<ParentT> withHttpsPort(int httpsPort);
            WithPremiumAttach<ParentT> withCustomDomain(String hostName);
        }

        interface AttachableStandard<ParentT> {
            ParentT attach();
        }

        interface AttachablePremium<ParentT> {
            ParentT attach();
        }
    }

    interface UpdateDefinitionStages {

        interface Blank {
            interface StandardEndpoint<ParentT> {
                UpdateDefinitionStages.WithStandardAttach<ParentT> withOrigin(String originName, String originHostName);
                UpdateDefinitionStages.WithStandardAttach<ParentT> withOrigin(String originHostName);
            }

            interface PremiumEndpoint<ParentT> {
                UpdateDefinitionStages.WithPremiumAttach<ParentT> withPremiumOrigin(String originName, String originHostName);
                UpdateDefinitionStages.WithPremiumAttach<ParentT> withPremiumOrigin(String originHostName);
            }
        }

        interface WithStandardAttach<ParentT>
                extends AttachableStandard<ParentT>
        {
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

        interface WithPremiumAttach<ParentT>
                extends AttachablePremium<ParentT>
        {
            WithPremiumAttach<ParentT> withOriginPath(String originPath);
            WithPremiumAttach<ParentT> withHostHeader(String hostHeader);
            WithPremiumAttach<ParentT> withHttpAllowed(boolean httpAllowed);
            WithPremiumAttach<ParentT> withHttpsAllowed(boolean httpsAllowed);
            WithPremiumAttach<ParentT> withHttpPort(int httpPort);
            WithPremiumAttach<ParentT> withHttpsPort(int httpsPort);
            WithPremiumAttach<ParentT> withCustomDomain(String hostName);
        }

        interface AttachableStandard<ParentT> {
            ParentT attach();
        }

        interface AttachablePremium<ParentT> {
            ParentT attach();
        }
    }

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

    interface Update extends
            Settable<CdnProfile.Update> {
    }
}