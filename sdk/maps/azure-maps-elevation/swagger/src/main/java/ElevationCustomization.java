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
        
        // customize elevation
        customizeElevation(models);

        // customize elevation result
        customizeElevationResult(models);

    }

    // Customizes the Elevation class
    private void customizeElevation(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("Elevation");
        classCustomization.removeMethod("getCoordinate");
        // get coordinate
        classCustomization.addMethod(
            "public GeoPosition getCoordinate() {\n" +
            "       return new GeoPosition(this.coordinate.getLongitude(), this.coordinate.getLatitude());\n" +
            "}")
            .getJavadoc()
            .setDescription("Return the coordinate.")
            .setReturn("Returns a {@link GeoPosition} coordinate.");
        classCustomization.addImports("com.azure.core.models.GeoPosition");
        classCustomization.removeMethod("setCoordinate");
        classCustomization.addConstructor(
            "private Elevation() {\n" + 
            "}")
            .getJavadoc()
            .setDescription("Set default Elevation constructor to private");
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
    }
}