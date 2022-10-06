// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.ClassCustomization;
import org.slf4j.Logger;

public class GeoLocationCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage("com.azure.maps.geolocation.models");

        // customize Country Region
        customizeCountryRegion(models);

        // customize IpAddressToLocationResult
        customizeIpAddressToLocationResult(models);
    }

    // Customizes the CountryRegion class
    private void customizeCountryRegion(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("CountryRegion");
        classCustomization.addConstructor(
            "private CountryRegion() {\n" +
            "}")
            .getJavadoc()
            .setDescription("Set default constructor to private");
    }

    // Customizes the IpAddressToLocationResult class
    private void customizeIpAddressToLocationResult(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("IpAddressToLocationResult");
        classCustomization.addConstructor(
            "private IpAddressToLocationResult() {\n" +
            "}")
            .getJavadoc()
            .setDescription("Set default constructor to private");
    }
}
