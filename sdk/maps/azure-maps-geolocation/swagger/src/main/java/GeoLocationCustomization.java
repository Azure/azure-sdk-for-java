// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.ClassCustomization;
import com.github.javaparser.ast.Modifier;
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
        models.getClass("CountryRegion").customizeAst(ast -> ast.getClassByName("CountryRegion").ifPresent(clazz ->
            clazz.getConstructors().get(0).setModifiers(Modifier.Keyword.PRIVATE)
                .setJavadocComment("Set default constructor to private")));
    }

    // Customizes the IpAddressToLocationResult class
    private void customizeIpAddressToLocationResult(PackageCustomization models) {
        models.getClass("IpAddressToLocationResult").customizeAst(ast -> ast.getClassByName("IpAddressToLocationResult")
            .ifPresent(clazz -> clazz.getConstructors().get(0).setModifiers(Modifier.Keyword.PRIVATE)
                .setJavadocComment("Set default constructor to private")));
    }
}
