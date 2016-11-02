package com.microsoft.azure.management.cdn.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.cdn.CdnEndpoint;
import com.microsoft.azure.management.cdn.CdnProfile;
import com.microsoft.azure.management.cdn.GeoFilterActions;
import com.microsoft.azure.management.cdn.QueryStringCachingBehavior;
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
                        .defineNewEndpoint("supermuperprofileAkamai")
                            .withOrigin("origin1","https://someweirdnam.blob.core.windows.net")
                            .attach()
                        .defineNewEndpoint("supermuperprofileAkamaiLinux")
                            .withOrigin("origin2", "mylinuxapp.azurewebsites.net")
                            .withCustomDomain("zzz")
                            .withCustomDomain("www.worldlinuxapps.com")
                            .withOriginHttpAllowed(true)
                            .withOriginHttpsAllowed(true)
                            .withOriginHttpPort(123)
                            .withOriginHttpsPort(321)
                            .withCachingBehavior(QueryStringCachingBehavior.BYPASS_CACHING)
                            .withCompressionEnabled(true)
                            .withContentTypeToCompress("text/plain")
                            .withContentTypeToCompress("powershell/pain")
                            .withGeoFilter("/path/videos", GeoFilterActions.BLOCK, "AZ")
                            .withGeoFilter("/path/images", GeoFilterActions.BLOCK, "AZ")
                            .withCustomDomain("www.domainone.com")
                            .withCustomDomain("www.domaintwo.au")
                            .withCustomDomain("www.domainthree.fr")
                            .attach()
                        .create();

                CdnProfile premiumProfile = azure.cdnProfiles().define(cdnPremiumProfileName)
                        .withRegion(Region.US_CENTRAL)
                        .withNewResourceGroup(rgName)
                        .withPremiumVerizonSku()
                        .defineNewPremiumEndpoint("supermuperep1")
                            .withPremiumOrigin("origin1", "https://xplattestvmss1sto0575014.blob.core.windows.net")
                            .attach()
                        .defineNewPremiumEndpoint("supermuperep2")
                            .withPremiumOrigin("origin2","https://supername.cloudapp.net")
                            .withPremiumOriginHttpAllowed(true)
                            .withPremiumOriginHttpsAllowed(true)
                            .withPremiumOriginHttpsPort(12)
                            .withPremiumOriginHttpPort(123)
                            .withPremiumCustomDomain("www.europeaninternet.eu")
                            .withPremiumCustomDomain("www.asianinternet.ch")
                            .attach()
                        .create();

                for (CdnEndpoint endpoint : standardProfile.endpoints().values()) {
                    System.out.println("CDN Endpoint: " + endpoint.name());
                }

                standardProfile.update()
                        .withTag("provider", "Akamai")
                        .defineNewEndpoint("AkamaiSomewhereelse")
                            .withOrigin("origin3", "https://www.vazgen.com")
                            .withGeoFilter("/path/music", GeoFilterActions.BLOCK, "AZ")
                        .attach()
                        .updateEndpoint("supermuperprofileAkamai")
                            .withOriginHttpAllowed(true)
                            .withOriginHttpPort(555)
                            .withoutContentTypesToCompress()
                            .withoutGeoFilters()
                        .parent()
                .apply();

                premiumProfile.update()
                        .withTag("provider", "Verizon")
                        .defineNewPremiumEndpoint("supermuperep3")
                            .withPremiumOrigin("origin4", "https://xplattestvmss1sto0575014.blob.core.windows.net")
                            .withPremiumCustomDomain("www.northamerica.com")
                        .attach()
                        .updatePremiumEndpoint("supermuperep2")
                            .withPremiumOriginHttpsAllowed(true)
                            .withPremiumOriginHttpsPort(678)
                        .parent()
                        .withoutEndpoint("supermuperep1")
                .apply();

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
