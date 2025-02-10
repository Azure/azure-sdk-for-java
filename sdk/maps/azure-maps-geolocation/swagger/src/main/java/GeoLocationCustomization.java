// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.github.javaparser.StaticJavaParser;
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

        customizeIpAddressToLocationResultMethod(models);
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

    private void customizeIpAddressToLocationResultMethod(PackageCustomization models) {
        // Get the class customization for IpAddressToLocationResult
        ClassCustomization ipAddressToLocationResult = models.getClass("IpAddressToLocationResult");

        // Add the necessary imports
        ipAddressToLocationResult.addImports(
            "java.net.InetAddress",
            "java.net.UnknownHostException",
            "com.azure.core.util.logging.ClientLogger"
        );

        // Use customizeAst to declare and initialize the logger field
        ipAddressToLocationResult.customizeAst(ast -> ast.getClassByName("IpAddressToLocationResult").ifPresent(clazz -> {
            clazz.addMember(
                StaticJavaParser.parseBodyDeclaration(
                    "private static final ClientLogger LOGGER = new ClientLogger(IpAddressToLocationResult.class);"
                )
            );
        }));

        // Remove the existing getIpAddress method
        ipAddressToLocationResult.removeMethod("getIpAddress");

        // Add the new getIpAddress method using ClientLogger
        ipAddressToLocationResult.addMethod(
            "/**\n" +
                " * Get the IP address as an InetAddress.\n" +
                " *\n" +
                " * @return The IP address as an InetAddress.\n" +
                " */\n" +
                "public InetAddress getIpAddress() {\n" +
                "    try {\n" +
                "        return InetAddress.getByName(this.ipAddress);\n" +
                "    } catch (UnknownHostException e) {\n" +
                "        throw LOGGER.logExceptionAsError(new IllegalArgumentException(\"Invalid IP address: \" + this.ipAddress, e));\n" +
                "    }\n" +
                "}\n"
        );
    }
}
