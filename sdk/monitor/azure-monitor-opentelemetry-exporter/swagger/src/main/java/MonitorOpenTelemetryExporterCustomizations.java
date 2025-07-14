// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashMap;

/**
 * Customization class for Monitor. These customizations will be applied on top of the generated code.
 */
public class MonitorOpenTelemetryExporterCustomizations extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        PackageCustomization implModels
            = libraryCustomization.getPackage("com.azure.monitor.opentelemetry.exporter.implementation.models");
        customizeTelemetryItem(implModels.getClass("TelemetryItem"));
    }

    private static void customizeTelemetryItem(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            ast.addImport("com.azure.monitor.opentelemetry.exporter.implementation.configuration.ConnectionString");
            ast.addImport("com.azure.monitor.opentelemetry.exporter.implementation.configuration.StatsbeatConnectionString");
            ast.addImport("io.opentelemetry.sdk.resources.Resource");
            ast.addImport(Collections.class);
            ast.addImport(HashMap.class);

            ast.getClassByName(classCustomization.getClassName()).ifPresent(clazz -> {
                clazz.addField("String", "connectionString", Modifier.Keyword.PRIVATE);
                clazz.addField("Resource", "resource", Modifier.Keyword.PRIVATE);

                clazz.addMethod("getConnectionString", Modifier.Keyword.PUBLIC)
                    .setType("String")
                    .setBody(StaticJavaParser.parseBlock("{ return connectionString; }"));

                clazz.addMethod("setConnectionString", Modifier.Keyword.PUBLIC)
                    .setType("TelemetryItem")
                    .addParameter("String", "connectionString")
                    .setBody(StaticJavaParser.parseBlock("{ this.connectionString = connectionString;"
                        + "this.instrumentationKey = ConnectionString.parse(connectionString).getInstrumentationKey();"
                        + "return this; }"));

                clazz.addMethod("setConnectionString", Modifier.Keyword.PUBLIC)
                    .setType("TelemetryItem")
                    .addParameter("ConnectionString", "connectionString")
                    .setBody(StaticJavaParser.parseBlock("{ this.connectionString = connectionString.getOriginalString();"
                        + "this.instrumentationKey = connectionString.getInstrumentationKey();"
                        + "return this; }"));

                clazz.addMethod("setConnectionString", Modifier.Keyword.PUBLIC)
                    .setType("TelemetryItem")
                    .addParameter("StatsbeatConnectionString", "connectionString")
                    .setBody(StaticJavaParser.parseBlock("{ instrumentationKey = connectionString.getInstrumentationKey();"
                        + "// TODO (heya) turn StatsbeatConnectionString into a real connection string?\n"
                        + "this.connectionString = \"InstrumentationKey=\" + instrumentationKey + \";IngestionEndpoint=\" + connectionString.getIngestionEndpoint();"
                        + "return this; }"));

                clazz.addMethod("getResource", Modifier.Keyword.PUBLIC)
                    .setType("Resource")
                    .setBody(StaticJavaParser.parseBlock("{ return resource; }"));

                clazz.addMethod("setResource", Modifier.Keyword.PUBLIC)
                    .setType("void")
                    .addParameter("Resource", "resource")
                    .setBody(StaticJavaParser.parseBlock("{ this.resource = resource; }"));

                clazz.addMethod("getResourceFromTags", Modifier.Keyword.PUBLIC)
                    .setType("Map<String, String>")
                    .setBody(StaticJavaParser.parseBlock(
                        "{ if (tags == null) { // Statsbeat doesn't have tags\n return Collections.emptyMap(); }"
                        + "Map<String, String> resourceFromTags = new HashMap<>();"
                        + "populateFromTag(ContextTagKeys.AI_CLOUD_ROLE.toString(), resourceFromTags);"
                        + "populateFromTag(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), resourceFromTags);"
                        + "populateFromTag(ContextTagKeys.AI_INTERNAL_SDK_VERSION.toString(), resourceFromTags);"
                        + "return resourceFromTags; }"));

                clazz.addMethod("populateFromTag", Modifier.Keyword.PRIVATE)
                    .setType("void")
                    .addParameter("String", "contextTagKey")
                    .addParameter("Map<String, String>", "resourceFromTags")
                    .setBody(StaticJavaParser.parseBlock("{ if (tags == null) { return; }"
                        + "String roleName = tags.get(contextTagKey);"
                        + "if (roleName != null) { resourceFromTags.put(contextTagKey, roleName); } }"));

                // Move serialization of "ver" back to the beginning.
                clazz.getMethodsByName("toJson").forEach(method -> method.getBody().ifPresent(body -> {
                    String toJsonStart = "jsonWriter.writeStartObject();";
                    String writeVer = "jsonWriter.writeNumberField(\"ver\", this.version);";
                    String toJsonBody = body.toString().replace(writeVer, "");
                    int index = toJsonBody.indexOf(toJsonStart) + toJsonStart.length();
                    toJsonBody = toJsonBody.substring(0, index) + writeVer + toJsonBody.substring(index);
                    method.setBody(StaticJavaParser.parseBlock(toJsonBody));
                }));
            });
        });
    }
}
