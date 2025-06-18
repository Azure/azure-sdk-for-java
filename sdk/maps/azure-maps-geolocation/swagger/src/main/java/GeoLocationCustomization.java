// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
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
        models.getClass("IpAddressToLocationResult").customizeAst(ast -> {
            ast.addImport("java.net.InetAddress");
            ast.addImport("java.net.UnknownHostException");
            ast.addImport("com.azure.core.util.logging.ClientLogger");

            ast.getClassByName("IpAddressToLocationResult").ifPresent(clazz -> {
                clazz.getConstructors().get(0).setModifiers(Modifier.Keyword.PRIVATE)
                    .setJavadocComment("Set default constructor to private");

                clazz.addFieldWithInitializer("ClientLogger", "LOGGER",
                    StaticJavaParser.parseExpression("new ClientLogger(IpAddressToLocationResult.class)"),
                    Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL);

                // Replace the existing getIpAddress method
                clazz.getMethodsByName("getIpAddress").forEach(method -> method.setType("InetAddress")
                    .setModifiers(Modifier.Keyword.PUBLIC)
                    .setBody(StaticJavaParser.parseBlock(
                        "{ try { return InetAddress.getByName(this.ipAddress); } catch (UnknownHostException e) {"
                            + "throw LOGGER.logExceptionAsError(new IllegalArgumentException(\"Invalid IP address: \" + this.ipAddress, e)); } }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("Get the IP address as an InetAddress."))
                        .addBlockTag("return", "The IP address as an InetAddress.")
                        .addBlockTag("throws", "IllegalArgumentException", "If the IP address isn't a valid InetAddress.")));
            });
        });
    }
}
