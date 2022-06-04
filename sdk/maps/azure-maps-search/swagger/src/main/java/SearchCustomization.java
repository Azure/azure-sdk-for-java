// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import java.util.Arrays;

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.MethodCustomization;
import org.slf4j.Logger;

/**
 * Customization class for Queue Storage.
 */
public class SearchCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage("com.azure.maps.search.models");

        // customize Address
        customizeAddress(models);

        // customize AddressRanges
        customizeAddressRanges(models);

        // customize EntryPoint
        customizeEntryPoint(models);

        // customize SearchAddressResultItem
        customizeSearchAddressResultItem(models);

        // customize SearchSummary
        customizeSearchSummary(models);

        // customize ReverseSearchAddressResultItem
        customizeReverseSearchAddressResultItem(models);

        // customize ReverseSearchCrossStreetAddressResultItem
        customizeReverseSearchCrossStreetAddressResultItem(models);

        // customizePolygon
        customizePolygon(models);
    }

    // Customizes the Address class by changing the type of BoundingBox
    private void customizeAddress(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("Address");
        MethodCustomization methodCustomization = classCustomization.getMethod("getBoundingBox");
        methodCustomization.setReturnType("GeoBoundingBox",
            "com.azure.maps.search.implementation.helpers.Utility.toGeoBoundingBox(returnValue)");
        classCustomization.addImports("com.azure.core.models.GeoBoundingBox");
    }


    // Customizes the AddressRanges class
    private void customizeAddressRanges(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("AddressRanges");
        String[] methods = new String[] {"setRangeRight", "setRangeLeft", "setTo", "setFrom"};

        // getTo
        MethodCustomization toCustomization = classCustomization.getMethod("getTo");
        toCustomization.setReturnType("GeoPosition",
            "new GeoPosition(returnValue.getLon(), returnValue.getLat())");

        // getFrom
        MethodCustomization fromCustomization = classCustomization.getMethod("getFrom");
        fromCustomization.setReturnType("GeoPosition",
            "new GeoPosition(returnValue.getLon(), returnValue.getLat())");

        // remove setters
        for (String method : methods) {
            classCustomization.removeMethod(method);
        }
        classCustomization.addImports("com.azure.core.models.GeoPosition");
    }

    // Customizes the EntryPoint class
    private void customizeEntryPoint(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("EntryPoint");
        String[] methods = new String[] {"setPosition"};

        // getPosition
        MethodCustomization toCustomization = classCustomization.getMethod("getPosition");
        toCustomization.setReturnType("GeoPosition",
            "new GeoPosition(returnValue.getLon(), returnValue.getLat())");

        // remove setters
        for (String method : methods) {
            classCustomization.removeMethod(method);
        }
        classCustomization.addImports("com.azure.core.models.GeoPosition");
    }

    // Customizes the SearchAddressResultItem class
    private void customizeSearchAddressResultItem(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("SearchAddressResultItem");
        String[] methods = new String[] {"setPosition"};

        // getPosition
        MethodCustomization toCustomization = classCustomization.getMethod("getPosition");
        toCustomization.setReturnType("GeoPosition",
           "new GeoPosition(returnValue.getLon(), returnValue.getLat())");

        // getViewport
        MethodCustomization viewportCustomization = classCustomization.getMethod("getViewport");
        viewportCustomization.rename("getBoundingBox");
        viewportCustomization = classCustomization.getMethod("getBoundingBox");

        viewportCustomization.setReturnType("GeoBoundingBox",
            " new GeoBoundingBox(returnValue.getTopLeft().getLon(), " +
            " returnValue.getTopLeft().getLat(), " +
            " returnValue.getBottomRight().getLon(), " +
            " returnValue.getBottomRight().getLat())");

        // data sources
        MethodCustomization dsCustomization = classCustomization.getMethod("getDataSources");
        dsCustomization.rename("getDataSource");

        // remove setters
        for (String method : methods) {
            classCustomization.removeMethod(method);
        }
        classCustomization.addImports("com.azure.core.models.GeoPosition");
        classCustomization.addImports("com.azure.core.models.GeoBoundingBox");
    }

    // Customizes the SearchSummary class
    private void customizeSearchSummary(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("SearchSummary");

        // getPosition
        MethodCustomization toCustomization = classCustomization.getMethod("getGeoBias");
        toCustomization.setReturnType("GeoPosition",
            "returnValue != null ? new GeoPosition(returnValue.getLon(), returnValue.getLat()) : null");

        classCustomization.addImports("com.azure.core.models.GeoPosition");
    }

    // Customizes the ReverseSearchAddressResultItem class
    private void customizeReverseSearchAddressResultItem(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("ReverseSearchAddressResultItem");

        // getPosition
        MethodCustomization toCustomization = classCustomization.getMethod("getPosition");
        toCustomization.setReturnType("GeoPosition",
            "com.azure.maps.search.implementation.helpers.Utility.fromCommaSeparatedString(returnValue)");

        classCustomization.addImports("com.azure.core.models.GeoPosition");
    }

    // Customizes the ReverseSearchCrossStreetAddressResultItem class
    private void customizeReverseSearchCrossStreetAddressResultItem(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("ReverseSearchCrossStreetAddressResultItem");

        // getPosition
        MethodCustomization toCustomization = classCustomization.getMethod("getPosition");
        toCustomization.setReturnType("GeoPosition",
            "com.azure.maps.search.implementation.helpers.Utility.fromCommaSeparatedString(returnValue)");

        classCustomization.addImports("com.azure.core.models.GeoPosition");
    }

    // Customizes the Polygon class
    private void customizePolygon(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("Polygon");

        // getGeometryData
        MethodCustomization nameCustomization = classCustomization.getMethod("getGeometryData");
        MethodCustomization geometryCustomization = nameCustomization.rename("getGeometry");

        geometryCustomization.setReturnType("GeoObject",
            "com.azure.maps.search.implementation.helpers.Utility.toGeoObject(returnValue)");

        classCustomization.addImports("com.azure.core.models.GeoObject");
    }
}
