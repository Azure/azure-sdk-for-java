import java.util.Arrays;

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.MethodCustomization;
import com.azure.autorest.customization.PackageCustomization;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
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
        models.getClass("ElevationResult").customizeAst(ast -> {
            ast.addImport("com.azure.core.models.GeoPosition");
            ast.addImport("java.util.ArrayList");

            ast.getClassByName("ElevationResult").ifPresent(clazz -> {
                clazz.setJavadocComment("The response from a successful Get Data for Bounding Box API.");
                clazz.getConstructors().get(0).setModifiers(Modifier.Keyword.PRIVATE)
                    .setJavadocComment("Set default ElevationResult constructor to private");
                clazz.getMethodsByName("getElevations").get(0).remove();

                clazz.addMethod("getElevations", Modifier.Keyword.PUBLIC)
                    .setType("List<GeoPosition>")
                    .setBody(StaticJavaParser.parseBlock(String.join("\n",
                        "{",
                        "   List<GeoPosition> toreturn = new ArrayList<>();",
                        "   for (Elevation e : this.elevations) {",
                        "       toreturn.add(new GeoPosition(e.getCoordinate().getLatitude(), e.getCoordinate().getLongitude(), (double) e.getElevationInMeters()));",
                        "   }",
                        "   return toreturn;",
                        "}")))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Get the elevations property: The response for point/points elevation API. The result will be in same sequence of points listed in request."))
                        .addBlockTag("return", "the elevations value"));
            });
        });
    }
}
