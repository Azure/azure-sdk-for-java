package com.microsoft.azure.management.cdn.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.cdn.CdnEndpoint;
import com.microsoft.azure.management.cdn.CdnProfile;
import com.microsoft.azure.management.cdn.GeoFilterActions;
import com.microsoft.azure.management.cdn.QueryStringCachingBehavior;
import com.microsoft.azure.management.resources.fluentcore.arm.CountryISOCode;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.samples.Utils;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.File;

/**
 * Created by hovsepm on 10/26/2016.
 */
public class ManageCdn {

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {

        final String cdnStandardProfileName = Utils.createRandomName("cdnStandardProfile");
        final String cdnPremiumProfileName = Utils.createRandomName("cdnPremiumProfile");
        final String epName1 = Utils.createRandomName("ep1");
        final String rgName = Utils.createRandomName("rgRCMC");

        try {

            final File credFile = new File("D:/my.azureauth");

            Azure azure = Azure
                    .configure()
                    .withLogLevel(HttpLoggingInterceptor.Level.BASIC)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            try {
                // ============================================================
                // Create a redis cache

                System.out.println("Creating a CDN Profile");

                CdnProfile standardProfile = azure.cdnProfiles().define(cdnStandardProfileName)
                        .withRegion(Region.US_CENTRAL)
                        .withNewResourceGroup(rgName)

                        .withStandardAkamaiSku()
                        .withNewEndpoint("https://someweirdnam.blob.core.windows.net")
                        .defineNewEndpoint("supermuperprofileAkamai")
                            .withOrigin("origin1","https://someweirdnam.blob.core.windows.net")
                            .attach()
                        .defineNewEndpoint()
                            .withOrigin("mylinuxapp.azurewebsites.net")
                            .withContentTypeToCompress("powershell/pain")
                            .withGeoFilter("/path/videos", GeoFilterActions.BLOCK, CountryISOCode.ARGENTINA)
                            .withGeoFilter("/path/images", GeoFilterActions.BLOCK, CountryISOCode.BELGIUM)
                            .withContentTypeToCompress("text/plain")
                            .withCompressionEnabled(true)
                            .withCachingBehavior(QueryStringCachingBehavior.BYPASS_CACHING)
                            .withHttpsAllowed(true)
                            .withHttpsPort(321)
                            .withHttpAllowed(true)
                            .withHttpPort(123)
                            .withCustomDomain("www.worldlinuxapps.com")
                            .withCustomDomain("www.domainone.com")
                            .withCustomDomain("www.domaintwo.au")
                            .withCustomDomain("www.domainthree.fr")
                            .attach()
                        .create();

                CdnProfile premiumProfile = azure.cdnProfiles().define(cdnPremiumProfileName)
                        .withRegion(Region.US_CENTRAL)
                        .withNewResourceGroup(rgName)
                        .withPremiumVerizonSku()
                        .withNewPremiumEndpoint("https://someweirdnam.blob.core.windows.net")
                        .defineNewPremiumEndpoint("supermuperep1")
                            .withPremiumOrigin("origin1", "https://xplattestvmss1sto0575014.blob.core.windows.net")
                            .attach()
                        .defineNewPremiumEndpoint()
                            .withPremiumOrigin("https://supername.cloudapp.net")
                            .withHttpAllowed(true)
                            .withHttpsAllowed(true)
                            .withHttpsPort(12)
                            .withHttpPort(123)
                            .withCustomDomain("www.europeaninternet.eu")
                            .withCustomDomain("www.asianinternet.ch")
                            .attach()
                        .create();

                for (CdnEndpoint endpoint : standardProfile.endpoints().values()) {
                    System.out.println("CDN Endpoint: " + endpoint.name());
                }

                if(standardProfile.isPremiumSku() == false){
                    standardProfile.update()
                            .withTag("provider", "Akamai")
                            .withNewEndpoint("https://www.vazgen.com")
                            .defineNewEndpoint()
                                .withOrigin("https://www.vazgen.com")
                                .withGeoFilter("/path/music", GeoFilterActions.BLOCK, CountryISOCode.ESTONIA)
                                .attach()
                            .updateEndpoint("supermuperprofileAkamai")
                                .withoutContentTypesToCompress()
                                .withoutGeoFilters()
                                .withHttpAllowed(true)
                                .withHttpPort(555)
                                .parent()
                    .apply();
                }

                premiumProfile.update()
                        .withTag("provider", "Verizon")
                        .withNewPremiumEndpoint("https://xplattestvmss1sto0575014.blob.core.windows.net")
                        .defineNewPremiumEndpoint("supermuperep3")
                            .withPremiumOrigin("https://xplattestvmss1sto0575014.blob.core.windows.net")
                            .withCustomDomain("www.northamerica.com")
                        .attach()
                        .updatePremiumEndpoint("supermuperep2")
                            .withHttpsAllowed(true)
                            .withHttpsPort(678)
                        .parent()
                        .withoutEndpoint("supermuperep1")
                .apply();

                String ssoUri = azure.cdnProfiles().generateSsoUri("Rg1", "ProfileName");
                String ssoUri1 = premiumProfile.generateSsoUri();

            } catch (Exception f) {
                System.out.println(f.getMessage());
                f.printStackTrace();
            } finally {
                if (azure.resourceGroups().getByName(rgName) != null) {
                    System.out.println("Deleting Resource Group: " + rgName);
                    azure.resourceGroups().deleteByName(rgName);
                    System.out.println("Deleted Resource Group: " + rgName);
                } else {
                    System.out.println("Did not create any resources in Azure. No clean up is necessary");
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageCdn() {
    }
}
