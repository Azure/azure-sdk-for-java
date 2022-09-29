import java.util.Arrays;

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.MethodCustomization;
import com.azure.autorest.customization.PackageCustomization;
import org.slf4j.Logger;

public class TimezoneCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage("com.azure.maps.timezone.models");

        // customize country record
        customizeCountryRecord(models);

        // customize timezone id
        customizeTimezoneId(models);

        // customize timezone names
        customizeTimezoneNames(models);

        // customize reference time
        customizeReferenceTime(models);

        // customize time transition
        customizeTimeTransition(models);

        // customize iana id
        customizeIanaId(models);

        // customize timezone windows
        customizeTimezoneWindows(models);

        // customize Timezone Iana Version Result
        customizeTimezoneIanaVersionResult(models);

        // customize Timezone Result
        customizeTimezoneResult(models);
    }

    // Customizes the country record class
    private void customizeCountryRecord(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("CountryRecord");

        classCustomization.addConstructor(
            "private CountryRecord() {\n" + 
            "}")
            .getJavadoc()
            .setDescription("Set default CountryRecord constructor to private");
    }

    // Customizes the timezone names class
    private void customizeTimezoneNames(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TimezoneNames");

        classCustomization.addConstructor(
            "private TimezoneNames() {\n" + 
            "}")
            .getJavadoc()
            .setDescription("Set default TimezoneNames constructor to private");
    }

    // Customizes the timezone id class
    private void customizeTimezoneId(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TimezoneId");
        classCustomization.removeMethod("getRepresentativePoint");
        // Get representative point
        classCustomization.addMethod(
            "public GeoPosition getRepresentativePoint() {\n" +
            "       return new GeoPosition(this.representativePoint.getLongitude(), this.representativePoint.getLatitude());\n" +
            "}")
            .getJavadoc()
            .setDescription("Returns the coordinate")
            .setReturn("Returns a {@link GeoPosition} coordinate.");
        classCustomization.addImports("com.azure.core.models.GeoPosition");

        classCustomization.addConstructor(
            "private TimezoneId() {\n" + 
            "}")
            .getJavadoc()
            .setDescription("Set default TimezoneId constructor to private");
    }

    // Customizes the reference time class
    private void customizeReferenceTime(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("ReferenceTime");
        classCustomization.removeMethod("getStandardOffset");
        classCustomization.removeMethod("getDaylightSavings");
        classCustomization.removeMethod("getWallTime");

        classCustomization.addConstructor(
            "private ReferenceTime() {\n" + 
            "}")
            .getJavadoc()
            .setDescription("ReferenceTime constructor");
        
        classCustomization.addConstructor(
            "private ReferenceTime(ZoneOffset daylightSavings, ZoneOffset standardOffset) {\n" + 
            "   this.daylightSavings = daylightSavings.toString();\n" + 
            "   this.standardOffset = standardOffset.toString();\n" +
            "}")
            .getJavadoc()
            .setDescription("ReferenceTime constructor")
            .setParam("daylightSavings", "daylightSavings Time saving in minutes in effect at the `ReferenceUTCTimestamp`.")
            .setParam("standardOffset", "UTC offset in effect at the `ReferenceUTCTimestamp`.");

        // Get standard offset
        classCustomization.addMethod(
            "public ZoneOffset getStandardOffset() {\n" +
            "       if (standardOffset.charAt(0) != '+' && standardOffset.charAt(0) != '-') {\n" +
            "              standardOffset = \"+\" + standardOffset;\n" +
            "       }\n" +
            "       return ZoneOffset.of(standardOffset);\n" +
            "}")
            .getJavadoc()
            .setDescription("Get the standard offset.")
            .setReturn("Returns a {@link ZoneOffset} time offset.");

        // Get daylight savings
        classCustomization.addMethod(
            "public ZoneOffset getDaylightSavings() {\n" +
            "       if (daylightSavings.charAt(0) != '+' && daylightSavings.charAt(0) != '-') {\n" +
            "              daylightSavings = \"+\" + daylightSavings;\n" +
            "       }\n" +
            "       return ZoneOffset.of(daylightSavings);\n" +
            "}")
            .getJavadoc()
            .setDescription("Returns the daylight savings value")
            .setReturn("Returns a {@link ZoneOffset} daylight savings. Get the daylightSavings property: Time saving in minutes in effect at the `ReferenceUTCTimestamp.");

        // Get wall time
        classCustomization.addMethod(
            "public OffsetDateTime getWallTime() {\n" +
            "       return OffsetDateTime.parse(wallTime);\n" +
            "}")
            .getJavadoc()
            .setDescription("Returns the wall time")
            .setReturn("Returns a {@link OffsetDateTime} offset date time.");

        classCustomization.addImports("java.time.ZoneOffset");
        classCustomization.addImports("java.time.OffsetDateTime");
    }

    // Customizes the time transition class
    private void customizeTimeTransition(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TimeTransition");
        classCustomization.removeMethod("getStandardOffset");
        classCustomization.removeMethod("getDaylightSavings");

        classCustomization.addConstructor(
            "private TimeTransition() {\n" + 
            "}")
            .getJavadoc()
            .setDescription("TimeTransition constructor");
        
        classCustomization.addConstructor(
            "private TimeTransition(ZoneOffset daylightSavings, ZoneOffset standardOffset) {\n" + 
            "   this.daylightSavings = daylightSavings.toString();\n" + 
            "   this.standardOffset = standardOffset.toString();\n" +
            "}")
            .getJavadoc()
            .setDescription("TimeTransition constructor")
            .setParam("daylightSavings", "daylightSavings Time saving in minutes in effect at the `ReferenceUTCTimestamp`.")
            .setParam("standardOffset", "UTC offset in effect at the `ReferenceUTCTimestamp`.");

        // Get standard offset
        classCustomization.addMethod(
            "public ZoneOffset getStandardOffset() {\n" +
            "       if (standardOffset.charAt(0) != '+' && standardOffset.charAt(0) != '-') {\n" +
            "              standardOffset = \"+\" + standardOffset;\n" +
            "       }\n" +
            "       return ZoneOffset.of(standardOffset);\n" +
            "}")
            .getJavadoc()
            .setDescription("return the standardOffset value")
            .setReturn("Returns a {@link ZoneOffset} time offset.");

        // Get daylight savings
        classCustomization.addMethod(
            "public ZoneOffset getDaylightSavings() {\n" +
            "       if (daylightSavings.charAt(0) != '+' && daylightSavings.charAt(0) != '-') {\n" +
            "              daylightSavings = \"+\" + daylightSavings;\n" +
            "       }\n" +
            "       return ZoneOffset.of(daylightSavings);\n" +
            "}")
            .getJavadoc()
            .setDescription("return the daylight savings value")
            .setReturn("Returns a {@link ZoneOffset} daylight savings. Get the daylightSavings property: Time saving in minutes in effect at the `ReferenceUTCTimestamp.");

            classCustomization.addImports("java.time.ZoneOffset");
    }

    // Customizes the iana id class
    private void customizeIanaId(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("IanaId");
        MethodCustomization methodCustomization = classCustomization.getMethod("getAliasOf");
        methodCustomization.rename("getAlias");
        MethodCustomization methodCustomization2 = classCustomization.getMethod("isHasZone1970Location");
        methodCustomization2.rename("getHasZone1970Location");
        classCustomization.addConstructor(
            "private IanaId() {\n" + 
            "}")
            .getJavadoc()
            .setDescription("Set default IanaId constructor to private");
    }
    
    // Customizes the timezone windows class
    private void customizeTimezoneWindows(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TimezoneWindows");
        classCustomization.removeMethod("setIanaIds");
        classCustomization.addConstructor(
            "private TimezoneWindows() {\n" + 
            "}")
            .getJavadoc()
            .setDescription("Set default TimezoneWindows constructor to private");
    }

    // Customizes the Timezone Iana Version Result
    private void customizeTimezoneIanaVersionResult(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TimezoneIanaVersionResult");

        classCustomization.addConstructor(
            "private TimezoneIanaVersionResult() {\n" + 
            "}")
            .getJavadoc()
            .setDescription("Set default TimezoneIanaVersionResult constructor to private");
    }

    // Customizes the timezone result class
    private void customizeTimezoneResult(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TimezoneResult");

        classCustomization.addConstructor(
            "private TimezoneResult() {\n" + 
            "}")
            .getJavadoc()
            .setDescription("Set default TimezoneResult constructor to private");
    }
}
