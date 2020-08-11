// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
//
// package com.azure.management;
//
// import com.microsoft.azure.management.cdn.CdnEndpoint;
// import com.microsoft.azure.management.cdn.CdnProfile;
// import com.microsoft.azure.management.cdn.CdnProfiles;
// import com.microsoft.azure.management.cdn.EdgeNode;
// import com.microsoft.azure.management.cdn.GeoFilter;
// import com.microsoft.azure.management.cdn.GeoFilterActions;
// import com.microsoft.azure.management.cdn.QueryStringCachingBehavior;
// import com.microsoft.azure.management.cdn.ResourceUsage;
// import com.microsoft.azure.management.cdn.SkuName;
// import com.microsoft.azure.management.resources.fluentcore.arm.CountryIsoCode;
// import com.microsoft.azure.management.resources.fluentcore.arm.Region;
// import org.junit.Assert;
//
// import java.util.Map;
//
/// **
// * Test of CDN management.
// */
// public class TestCdn extends TestTemplate<CdnProfile, CdnProfiles> {
//    @Override
//    public CdnProfile createResource(CdnProfiles profiles) throws Exception {
//        final Region region = Region.US_EAST;
//        final String groupName = "rg" + this.testId;
//        final String cdnProfileName = "cdnProfile" + this.testId;
//        final String cdnEndpointName = "cdnEndpoint" + this.testId;
//        final String cdnOriginHostName = "mylinuxapp.azurewebsites.net";
//
//        CdnProfile cdnProfile = profiles.define(cdnProfileName)
//                .withRegion(region)
//                .withNewResourceGroup(groupName)
//                .withStandardAkamaiSku()
//                .defineNewEndpoint(cdnEndpointName)
//                    .withOrigin(cdnOriginHostName)
//                    .withGeoFilter("/path/videos", GeoFilterActions.BLOCK, CountryIsoCode.ARGENTINA)
//                    .withGeoFilter("/path/images", GeoFilterActions.BLOCK, CountryIsoCode.BELGIUM)
//                    .withContentTypeToCompress("text/plain")
//                    .withCompressionEnabled(true)
//                    .withQueryStringCachingBehavior(QueryStringCachingBehavior.BYPASS_CACHING)
//                    .withHttpsAllowed(true)
//                    .withHttpsPort(444)
//                    .withHttpAllowed(false)
//                    .withHttpPort(85)
//                    .attach()
//                .create();
//
//        Assert.assertTrue(cdnProfile.sku().name().equals(SkuName.STANDARD_AKAMAI));
//        Assert.assertNotNull(cdnProfile.endpoints());
//        Assert.assertEquals(1, cdnProfile.endpoints().size());
//        CdnEndpoint endpoint = cdnProfile.endpoints().get(cdnEndpointName);
//        Assert.assertNotNull(endpoint);
//        Assert.assertEquals(cdnOriginHostName, endpoint.originHostName());
//        Assert.assertEquals(444, endpoint.httpsPort());
//        Assert.assertEquals(85, endpoint.httpPort());
//        Assert.assertFalse(endpoint.isHttpAllowed());
//        Assert.assertTrue(endpoint.isHttpsAllowed());
//        Assert.assertTrue(endpoint.isCompressionEnabled());
//        Assert.assertEquals(QueryStringCachingBehavior.BYPASS_CACHING, endpoint.queryStringCachingBehavior());
//        Assert.assertNotNull(endpoint.geoFilters());
//        Assert.assertEquals(QueryStringCachingBehavior.BYPASS_CACHING, endpoint.queryStringCachingBehavior());
//
//        for (ResourceUsage usage : profiles.listResourceUsage()) {
//            Assert.assertNotNull(usage);
//            Assert.assertEquals("profile", usage.resourceType());
//        }
//
//        for (EdgeNode node : profiles.listEdgeNodes()) {
//            Assert.assertNotNull(node);
//        }
//
//        for (ResourceUsage usage : cdnProfile.listResourceUsage()) {
//            Assert.assertNotNull(usage);
//            Assert.assertEquals("endpoint", usage.resourceType());
//        }
//
//        for( CdnEndpoint ep : cdnProfile.endpoints().values()) {
//            for (ResourceUsage usage : ep.listResourceUsage()) {
//                Assert.assertNotNull(usage);
//                Assert.assertTrue("customdomain".equals(usage.resourceType())
//                                    || "geofilter".equals(usage.resourceType()));
//            }
//        }
//        return cdnProfile;
//    }
//
//    @Override
//    public CdnProfile updateResource(CdnProfile profile) throws Exception {
//        String firstEndpointName = profile.endpoints().keySet().iterator().next();
//
//        // Remove an endpoint, update two endpoints and add new one
//        //
//        profile.update()
//                .withTag("provider", "Akamai")
//                .withNewEndpoint("www.bing.com")
//                .defineNewEndpoint("somenewnamefortheendpoint")
//                    .withOrigin("www.contoso.com")
//                    .withGeoFilter("/path/music", GeoFilterActions.BLOCK,
// CountryIsoCode.UNITED_STATES_OUTLYING_ISLANDS)
//                    .attach()
//                .updateEndpoint(firstEndpointName)
//                    .withHttpAllowed(true)
//                    .withHttpPort(1111)
//                    .withoutGeoFilters()
//                    .parent()
//                .apply();
//
//        Assert.assertEquals(3, profile.endpoints().size());
//        CdnEndpoint updatedEndpoint = profile.endpoints().get(firstEndpointName);
//        Assert.assertTrue(updatedEndpoint.isHttpsAllowed());
//        Assert.assertEquals(1111, updatedEndpoint.httpPort());
//        Assert.assertEquals(0, updatedEndpoint.geoFilters().size());
//
//        return profile;
//    }
//
//    @Override
//    public void print(CdnProfile profile) {
//        StringBuilder info = new StringBuilder();
//        info.append("CDN Profile: ").append(profile.id())
//                .append("\n\tName: ").append(profile.name())
//                .append("\n\tResource group: ").append(profile.resourceGroupName())
//                .append("\n\tRegion: ").append(profile.regionName())
//                .append("\n\tSku: ").append(profile.sku().name())
//                .append("\n\tTags: ").append(profile.tags());
//
//        Map<String, CdnEndpoint> cdnEndpoints = profile.endpoints();
//        if (!cdnEndpoints.isEmpty()) {
//            info.append("\n\tCDN endpoints:");
//            int idx = 1;
//            for (CdnEndpoint endpoint : cdnEndpoints.values()) {
//                info.append("\n\t\tCDN endpoint: #").append(idx++)
//                        .append("\n\t\t\tId: ").append(endpoint.id())
//                        .append("\n\t\t\tName: ").append(endpoint.name())
//                        .append("\n\t\t\tState: ").append(endpoint.resourceState())
//                        .append("\n\t\t\tHost name: ").append(endpoint.hostName())
//                        .append("\n\t\t\tOrigin host name: ").append(endpoint.originHostName())
//                        .append("\n\t\t\tOrigin host header: ").append(endpoint.originHostHeader())
//                        .append("\n\t\t\tOrigin path: ").append(endpoint.originPath())
//                        .append("\n\t\t\tOptimization type: ").append(endpoint.optimizationType())
//                        .append("\n\t\t\tQuery string caching behavior:
// ").append(endpoint.queryStringCachingBehavior())
//                        .append("\n\t\t\tHttp allowed: ").append(endpoint.isHttpAllowed())
//                        .append("\t\tHttp port: ").append(endpoint.httpPort())
//                        .append("\n\t\t\tHttps allowed: ").append(endpoint.isHttpsAllowed())
//                        .append("\t\tHttps port: ").append(endpoint.httpsPort())
//                        .append("\n\t\t\tCompression enabled: ").append(endpoint.isCompressionEnabled());
//
//                info.append("\n\t\t\tContent types to compress: ");
//                for (String contentTypeToCompress : endpoint.contentTypesToCompress()) {
//                    info.append("\n\t\t\t\t").append(contentTypeToCompress);
//                }
//
//                info.append("\n\t\t\tGeo filters: ");
//                for (GeoFilter geoFilter : endpoint.geoFilters()) {
//                    info.append("\n\t\t\t\tAction: ").append(geoFilter.action());
//                    info.append("\n\t\t\t\tRelativePath: ").append(geoFilter.relativePath());
//                    info.append("\n\t\t\t\tCountry codes: ");
//                    for (String countryCode : geoFilter.countryCodes()) {
//                        info.append("\n\t\t\t\t\t").append(countryCode);
//                    }
//                }
//
//                info.append("\n\t\t\tCustom domains: ");
//                for (String customDomain : endpoint.customDomains()) {
//                    info.append("\n\t\t\t\t").append(customDomain);
//                }
//            }
//        }
//        System.out.println(info.toString());
//    }
// }
