import java.util.Arrays;

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.MethodCustomization;
import com.azure.autorest.customization.PackageCustomization;

import org.slf4j.Logger;

/**
 * Customization class for Queue Storage.
 */
public class ElevationCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage("com.azure.maps.elevation.models");
        PackageCustomization implementationModels = customization.getPackage("com.azure.maps.elevation.implementation.models");
        
        // customize elevation
        customizeElevation(implementationModels);

        // customize elevation result
        customizeElevationResult(models);

    }

    // Customizes the Elevation class
    private void customizeElevation(PackageCustomization implementationModels) {
        ClassCustomization classCustomization = implementationModels.getClass("Elevation");
        MethodCustomization methodCustomization = classCustomization.getMethod("getElevationInMeter");
        methodCustomization.rename("getElevationInMeters");
    }

    // Customizes the Elevation class
    private void customizeElevationResult(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("ElevationResult");
        classCustomization.addConstructor(
            "private ElevationResult() {\n" + 
            "}")
            .getJavadoc()
            .setDescription("Set default ElevationResult constructor to private");
        classCustomization.removeMethod("getElevations");
        classCustomization.addMethod(
            "public List<GeoPosition> getElevations() {\n" +
            "   List<GeoPosition> toreturn = new ArrayList<>();\n" +
            "   for (Elevation e : this.elevations) {\n" +
            "       toreturn.add(new GeoPosition(e.getCoordinate().getLatitude(), e.getCoordinate().getLongitude(), (double) e.getElevationInMeters()));\n" +
            "   }\n" +
            "   return toreturn;\n" +
            "}")
            .getJavadoc()
            .setDescription("Get the elevations property: The response for point/points elevation API. The result will be in same sequence of points listed in request.")
            .setReturn("the elevations value");
        classCustomization.addImports("com.azure.core.models.GeoPosition");
        classCustomization.addImports("java.util.ArrayList");
    }
}
