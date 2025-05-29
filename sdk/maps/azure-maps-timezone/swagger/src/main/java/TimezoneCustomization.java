import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

public class TimezoneCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage("com.azure.maps.timezone.models");

        // customize timezone id
        customizeTimeZoneId(models);

        // customize reference time
        customizeReferenceTime(models);

        // customize time transition
        customizeTimeTransition(models);

        // customize iana id
        customizeIanaId(models);

        // customize timeZone windows
        customizeTimeZoneWindows(models);

        // Set the constructor for the following classes to private.
        setConstructorPrivate(models, "CountryRecord", "TimeZoneNames", "TimeZoneIanaVersionResult", "TimeZoneResult");
    }

    private static void setConstructorPrivate(PackageCustomization customization, String... classNames) {
        for (String name : classNames) {
            customization.getClass(name).customizeAst(ast -> ast.getClassByName(name).ifPresent(clazz ->
                clazz.getConstructors().get(0).setModifiers(Modifier.Keyword.PRIVATE)
                    .setJavadocComment("Set default " + name + " constructor to private.")));
        }
    }

    // Customizes the timezone id class
    private void customizeTimeZoneId(PackageCustomization models) {
        models.getClass("TimeZoneId").customizeAst(ast -> {
            ast.addImport("com.azure.core.models.GeoPosition");

            ast.getClassByName("TimeZoneId").ifPresent(clazz -> {
                clazz.getConstructors().get(0)
                    .setModifiers(Modifier.Keyword.PRIVATE)
                    .setJavadocComment("Set default TimeZoneId constructor to private");

                clazz.getMethodsByName("getRepresentativePoint").forEach(method -> method.setType("GeoPosition")
                    .setBody(StaticJavaParser.parseBlock("{ return new GeoPosition(this.representativePoint.getLongitude(), this.representativePoint.getLatitude()); }")));
            });
        });
    }

    // Customizes the reference time class
    private void customizeReferenceTime(PackageCustomization models) {
        models.getClass("ReferenceTime").customizeAst(ast -> {
            ast.addImport("java.time.ZoneOffset");
            ast.addImport("java.time.OffsetDateTime");

            ast.getClassByName("ReferenceTime").ifPresent(clazz -> {
                clazz.getConstructors().get(0)
                    .setModifiers(Modifier.Keyword.PRIVATE)
                    .setJavadocComment("Set default ReferenceTime constructor to private");

                clazz.getMethodsByName("getStandardOffset").forEach(method -> method.setType("ZoneOffset")
                    .setBody(StaticJavaParser.parseBlock("{ if (standardOffset.charAt(0) != '+' && standardOffset.charAt(0) != '-') { standardOffset = \"+\" + standardOffset; } return ZoneOffset.of(standardOffset); }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("Get the standard offset."))
                        .addBlockTag("return", "Returns a {@link ZoneOffset} time offset.")));

                clazz.getMethodsByName("getDaylightSavings").forEach(method -> method.setType("ZoneOffset")
                    .setBody(StaticJavaParser.parseBlock("{ if (daylightSavings.charAt(0) != '+' && daylightSavings.charAt(0) != '-') { daylightSavings = \"+\" + daylightSavings; } return ZoneOffset.of(daylightSavings); }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("Get the daylight savings."))
                        .addBlockTag("return", "Returns a {@link ZoneOffset} daylight savings. Get the daylightSavings property: Time saving in minutes in effect at the ReferenceUTCTimestamp.")));

                clazz.getMethodsByName("getWallTime").forEach(method -> method.setType("OffsetDateTime")
                    .setBody(StaticJavaParser.parseBlock("{ return OffsetDateTime.parse(wallTime); }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("Get the wall time."))
                        .addBlockTag("return", "Returns a {@link OffsetDateTime} offset date time.")));
            });
        });
    }

    // Customizes the time transition class
    private void customizeTimeTransition(PackageCustomization models) {
        models.getClass("TimeTransition").customizeAst(ast -> {
            ast.addImport("java.time.ZoneOffset");

            ast.getClassByName("TimeTransition").ifPresent(clazz -> {
                clazz.getConstructors().get(0)
                    .setModifiers(Modifier.Keyword.PRIVATE)
                    .setJavadocComment("Set default TimeTransition constructor to private");

                clazz.getMethodsByName("getStandardOffset").forEach(method -> method.setType("ZoneOffset")
                    .setBody(StaticJavaParser.parseBlock("{ if (standardOffset.charAt(0) != '+' && standardOffset.charAt(0) != '-') { standardOffset = \"+\" + standardOffset; } return ZoneOffset.of(standardOffset); }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("return the standardOffset value"))
                        .addBlockTag("return", "Returns a {@link ZoneOffset} time offset.")));

                clazz.getMethodsByName("getDaylightSavings").forEach(method -> method.setType("ZoneOffset")
                    .setBody(StaticJavaParser.parseBlock("{ if (daylightSavings.charAt(0) != '+' && daylightSavings.charAt(0) != '-') { daylightSavings = \"+\" + daylightSavings; } return ZoneOffset.of(daylightSavings); }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("return the daylight savings value"))
                        .addBlockTag("return", "Returns a {@link ZoneOffset} daylight savings. Get the daylightSavings property: Time saving in minutes in effect at the ReferenceUTCTimestamp.")));
            });
        });
    }

    // Customizes the iana id class
    private void customizeIanaId(PackageCustomization models) {
        models.getClass("IanaId").customizeAst(ast -> ast.getClassByName("IanaId").ifPresent(clazz -> {
            clazz.getConstructors().get(0)
                .setModifiers(Modifier.Keyword.PRIVATE)
                .setJavadocComment("Set default TimeZoneWindows constructor to private");

            clazz.getMethodsByName("getAliasOf").forEach(method -> method.setName("getAlias"));
            clazz.getMethodsByName("isHasZone1970Location").forEach(method -> method.setName("getHasZone1970Location"));
        }));
    }

    // Customizes the timezone windows class
    private void customizeTimeZoneWindows(PackageCustomization models) {
        models.getClass("TimeZoneWindows").customizeAst(ast -> ast.getClassByName("TimeZoneWindows").ifPresent(clazz -> {
            clazz.getConstructors().get(0)
                .setModifiers(Modifier.Keyword.PRIVATE)
                .setJavadocComment("Set default TimeZoneWindows constructor to private");

            clazz.getMethodsByName("setIanaIds").forEach(Node::remove);
        }));
    }
}
