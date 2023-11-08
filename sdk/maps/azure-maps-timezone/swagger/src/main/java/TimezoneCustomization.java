import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.ConstructorCustomization;
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
        customizeTimeZoneId(models);

        // customize timezone names
        customizeTimeZoneNames(models);

        // customize reference time
        customizeReferenceTime(models);

        // customize time transition
        customizeTimeTransition(models);

        // customize iana id
        customizeIanaId(models);

        // customize timeZone windows
        customizeTimeZoneWindows(models);

        // customize TimeZone Iana Version Result
        customizeTimeZoneIanaVersionResult(models);

        // customize TimeZone Result
        customizeTimeZoneResult(models);

        // customize TimeZone Options
        customizeTimeZoneOptions(models);
    }

    // Customizes the country record class
    private void customizeCountryRecord(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("CountryRecord");

        ConstructorCustomization constructorCustomization = classCustomization.getConstructor("public CountryRecord()");
        constructorCustomization.setModifier(2);
        constructorCustomization.getJavadoc().setDescription("Set default CountryRecord constructor to private");
    }

    // Customizes the timezone names class
    private void customizeTimeZoneNames(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TimeZoneNames");

        ConstructorCustomization constructorCustomization = classCustomization.getConstructor("public TimeZoneNames()");
        constructorCustomization.setModifier(2);
        constructorCustomization.getJavadoc().setDescription("Set default TimeZoneNames constructor to private");
    }

    // Customizes the timezone id class
    private void customizeTimeZoneId(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TimeZoneId");
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

        ConstructorCustomization constructorCustomization = classCustomization.getConstructor("public TimeZoneId()");
        constructorCustomization.setModifier(2);
        constructorCustomization.getJavadoc().setDescription("Set default TimeZoneId constructor to private");
    }

    // Customizes the reference time class
    private void customizeReferenceTime(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("ReferenceTime");
        classCustomization.removeMethod("getStandardOffset");
        classCustomization.removeMethod("getDaylightSavings");
        classCustomization.removeMethod("getWallTime");

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

        ConstructorCustomization constructorCustomization = classCustomization.getConstructor("public ReferenceTime()");
        constructorCustomization.setModifier(2);
        constructorCustomization.getJavadoc().setDescription("Set default ReferenceTime constructor to private");
    }

    // Customizes the time transition class
    private void customizeTimeTransition(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TimeTransition");
        classCustomization.removeMethod("getStandardOffset");
        classCustomization.removeMethod("getDaylightSavings");

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

            ConstructorCustomization constructorCustomization = classCustomization.getConstructor("public TimeTransition()");
            constructorCustomization.setModifier(2);
            constructorCustomization.getJavadoc().setDescription("Set default TimeTransition constructor to private");
    
    }

    // Customizes the iana id class
    private void customizeIanaId(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("IanaId");
        MethodCustomization methodCustomization = classCustomization.getMethod("getAliasOf");
        methodCustomization.rename("getAlias");
        MethodCustomization methodCustomization2 = classCustomization.getMethod("isHasZone1970Location");
        methodCustomization2.rename("getHasZone1970Location");
        ConstructorCustomization constructorCustomization = classCustomization.getConstructor("public IanaId()");
        constructorCustomization.setModifier(2);
        constructorCustomization.getJavadoc().setDescription("Set default IanaId constructor to private");
    }
    
    // Customizes the timezone windows class
    private void customizeTimeZoneWindows(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TimeZoneWindows");
        classCustomization.removeMethod("setIanaIds");
        ConstructorCustomization constructorCustomization = classCustomization.getConstructor("public TimeZoneWindows()");
        constructorCustomization.setModifier(2);
        constructorCustomization.getJavadoc().setDescription("Set default TimeZoneWindows constructor to private");
    }

    // Customizes the TimeZone Iana Version Result
    private void customizeTimeZoneIanaVersionResult(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TimeZoneIanaVersionResult");

        ConstructorCustomization constructorCustomization = classCustomization.getConstructor("public TimeZoneIanaVersionResult()");
        constructorCustomization.setModifier(2);
        constructorCustomization.getJavadoc().setDescription("Set default TimeZoneIanaVersionResult constructor to private");
    }

    // Customizes the timezone result class
    private void customizeTimeZoneResult(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TimeZoneResult");
        ConstructorCustomization constructorCustomization = classCustomization.getConstructor("public TimeZoneResult()");
        constructorCustomization.setModifier(2);
        constructorCustomization.getJavadoc().setDescription("Set default TimeZoneResult constructor to private");
    }

    // Customizes the timezone options class
    private void customizeTimeZoneOptions(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TimezoneOptions");
        classCustomization.rename("TimeZoneOptions");
    }
}
