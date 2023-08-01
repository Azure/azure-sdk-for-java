import org.slf4j.Logger;

import java.util.Arrays;

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.JavadocCustomization;
import com.azure.autorest.customization.LibraryCustomization;

public class RenderCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage("com.azure.maps.render.models");

        // customize maptileset
        customizeMapTileset(models);

        // customize tilesetid
        customizeTilesetId(models);

        // customize region copyrights country
        customizeRegionCopyrightsCountry(models);

        // customize map tile size
        customizeMapTileSize(models);
    }

    // Customizes the MapTileset class
    private void customizeMapTileset(PackageCustomization models) {
        final String getBoundsMethod =
            "public GeoBoundingBox getBounds() {" +
            "    return new GeoBoundingBox(this.bounds.get(0), this.bounds.get(1), this.bounds.get(2), this.bounds.get(3));" +
            "}";
        final String getCenterMethod =
            "public GeoPosition getCenter() {" +
            "    return new GeoPosition(this.center.get(0).doubleValue(), this.center.get(1).doubleValue(), this.center.get(2).doubleValue());" +
            "}";
        final String getTileJsonMethod =
            "public String getTileJson() {" +
            "    return this.tilejson;" +
            "}";
        final String setTileJsonMethod =
            "public MapTileset setTileJson(String tilejson) {" +
            "    this.tilejson = tilejson;" +
            "    return this;" +
            "}";
        ClassCustomization classCustomization = models.getClass("MapTileset");
        classCustomization.removeMethod("getBounds");
        classCustomization.removeMethod("setBounds");
        classCustomization.removeMethod("getCenter");
        classCustomization.removeMethod("setCenter");
        classCustomization.removeMethod("getTilejson");
        classCustomization.removeMethod("setTilejson");
        classCustomization.addMethod(getBoundsMethod, Arrays.asList("com.azure.core.models.GeoBoundingBox"));
        classCustomization.addMethod(getCenterMethod, Arrays.asList("com.azure.core.models.GeoPosition"));
        classCustomization.addMethod(getTileJsonMethod);
        classCustomization.addMethod(setTileJsonMethod);

        // javadoc customization to pass Checkstyle
        final String getCenterJavadocDescription = "Get the center property: The default location of the " +
            "tileset in the form [longitutde, latitude, zoom]. The zoom level must be between minzoom and " +
            "maxzoom. Implementation can use this value to set the default location.";
        JavadocCustomization centerDoc = classCustomization.getMethod("getCenter").getJavadoc();
        centerDoc.setDescription(getCenterJavadocDescription);
        centerDoc.setReturn("a {@code GeoPosition} representing the center.");

        final String getBoundsJavadocDescription = "Bounds must define an area covered by all zoom levels. " +
            "The bounds are represented in WGS:84 latitude and longitude values " +
            "in the order left, bottom, right, top. Values may be integers or floating point numbers.";
        JavadocCustomization boundsDoc = classCustomization.getMethod("getBounds").getJavadoc();
        boundsDoc.setDescription(getBoundsJavadocDescription);
        boundsDoc.setReturn("a {@code GeoBoundingBox} representing the bounding box.");

        final String getTileJsonMethodDescription = "Get the tilejson property: Describes the version of the TileJSON spec that is implemented by this JSON object.";
        JavadocCustomization getTileJsonJavadoc = classCustomization.getMethod("getTileJson").getJavadoc();
        getTileJsonJavadoc.setDescription(getTileJsonMethodDescription);
        getTileJsonJavadoc.setReturn("the tilejson value.");

        final String setTileJsonMethodDescription = "Set the tilejson property: Describes the version of the TileJSON spec that is implemented by this JSON object.";
        JavadocCustomization setTileJsonJavadoc = classCustomization.getMethod("setTileJson").getJavadoc();
        setTileJsonJavadoc.setDescription(setTileJsonMethodDescription);
        setTileJsonJavadoc.setParam("tilejson", "TileJson version");
        setTileJsonJavadoc.setReturn("the MapTileset object itself.");
    }

     // Customizes the TilesetId class
     private void customizeTilesetId(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TilesetID");
        classCustomization.rename("TilesetId");
     }

     // Customizes the RegionCopyrightsCountry class
     private void customizeRegionCopyrightsCountry(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("RegionCopyrightsCountry");
        final String getIso3Method =
            "public String getIso3() {" +
            "    return this.iSO3;" +
            "}";
        classCustomization.removeMethod("getISO3");
        classCustomization.addMethod(getIso3Method);

        // javadoc customization to pass Checkstyle
        final String getIso3JavadocDescription = "Get the iSO3 property: ISO3 property.";
        JavadocCustomization getIso3Javadoc = classCustomization.getMethod("getIso3").getJavadoc();
        getIso3Javadoc.setDescription(getIso3JavadocDescription);
        getIso3Javadoc.setReturn("the iSO3 value.");
     }

     // Customizes the MapTileSize class
     private void customizeMapTileSize(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("MapTileSize");
        classCustomization.getConstant("SIZE256").rename("SIZE_256");
        classCustomization.getConstant("SIZE512").rename("SIZE_512");
     }
}
