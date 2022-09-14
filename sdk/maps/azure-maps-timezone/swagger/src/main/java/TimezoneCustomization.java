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

        // customize timezone id
        customizeTimezoneId(models);

        // customize reference time
        customizeReferenceTime(models);

        // customize time transition
        customizeTimeTransition(models);
    }

    // Customizes the timezone id class
    private void customizeTimezoneId(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TimezoneId");
        classCustomization.removeMethod("getRepresentativePoint");
        final String getRepresentativePointMethod = 
            "/**" +
            " * Returns a {@link GeoPosition} coordinate." +
            "*" +
            "* return the coordinate" +
            "*/" + 
            "public GeoPosition getRepresentativePoint() {" +
            "       return new GeoPosition(this.representativePoint.getLongitude(), this.representativePoint.getLatitude());" +
            "}";
        classCustomization.addMethod(getRepresentativePointMethod, Arrays.asList("com.azure.core.models.GeoPosition"));
    }

    // Customizes the reference time class
    private void customizeReferenceTime(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("ReferenceTime");
        classCustomization.removeMethod("getStandardOffset");
        classCustomization.removeMethod("getDaylightSavings");
        classCustomization.removeMethod("getWallTime");
        final String getStandardOffset = 
            "/**" +
            "Returns a {@link ZoneOffset} time offset." +
            "*" +
            "* return the standard offset" +
            "*/" + 
            "public ZoneOffset getStandardOffset() {" +
            "       if (standardOffset.charAt(0) != '+' && standardOffset.charAt(0) != '-') {" +
            "              standardOffset = \"+\" + standardOffset;" +
            "       }" +
            "       return ZoneOffset.of(standardOffset);" +
            "}";
        final String getDaylightSavings = 
            "/**" +
            "Returns a {@link ZoneOffset} daylight savings. Get the daylightSavings property: Time saving in minutes in effect at the `ReferenceUTCTimestamp" +
            "*" +
            "return the daylight savings value" +
            "*/" + 
            "public ZoneOffset getDaylightSavings() {" +
            "       if (daylightSavings.charAt(0) != '+' && daylightSavings.charAt(0) != '-') {" +
            "              daylightSavings = \"+\" + daylightSavings;" +
            "       }" +
            "       return ZoneOffset.of(daylightSavings);" +
            "}";
        final String getWallTime = 
            "/**" +
            "Returns a {@link OffsetDateTime} offset date time." +
            "*" +
            "* return the wall time" +
            "*/" + 
            "public OffsetDateTime getWallTime() {" +
            "       return OffsetDateTime.parse(wallTime);" +
            "}";
        classCustomization.addMethod(getStandardOffset, Arrays.asList("java.time.ZoneOffset;"));
        classCustomization.addMethod(getDaylightSavings, Arrays.asList("java.time.ZoneOffset;"));
        classCustomization.addMethod(getWallTime, Arrays.asList("java.time.OffsetDateTime;"));
    }

    // Customizes the time transition class
    private void customizeTimeTransition(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TimeTransition");
        classCustomization.removeMethod("getStandardOffset");
        classCustomization.removeMethod("getDaylightSavings");
        final String getStandardOffset = 
            "/**" +
            "Returns a {@link ZoneOffset} time offset." +
            "*" +
            "* return the standardOffset value" +
            "*/" + 
            "public ZoneOffset getStandardOffset() {" +
            "       if (standardOffset.charAt(0) != '+' && standardOffset.charAt(0) != '-') {" +
            "              standardOffset = \"+\" + standardOffset;" +
            "       }" +
            "       return ZoneOffset.of(standardOffset);" +
            "}";
        final String getDaylightSavings = 
            "/**" +
            "Returns a {@link ZoneOffset} daylight savings. Get the daylightSavings property: Time saving in minutes in effect at the `ReferenceUTCTimestamp" +
            "*" +
            "return the daylight savings value" +
            "*/" + 
            "public ZoneOffset getDaylightSavings() {" +
            "       if (daylightSavings.charAt(0) != '+' && daylightSavings.charAt(0) != '-') {" +
            "              daylightSavings = \"+\" + daylightSavings;" +
            "       }" +
            "       return ZoneOffset.of(daylightSavings);" +
            "}";
        classCustomization.addMethod(getStandardOffset, Arrays.asList("java.time.ZoneOffset;"));
        classCustomization.addMethod(getDaylightSavings, Arrays.asList("java.time.ZoneOffset;"));
    }
}
