// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import org.slf4j.Logger;

public class TablesCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization modelsPackage = customization.getPackage("com.azure.data.tables.models");

        customizeTableSignedIdentifier(modelsPackage.getClass("TableSignedIdentifier"));
        customizeTableServiceGeoReplication(modelsPackage.getClass("TableServiceGeoReplication"));
        customizeTableServiceStatistics(modelsPackage.getClass("TableServiceStatistics"));
        customizeTableServiceMetrics(modelsPackage.getClass("TableServiceMetrics"));
        customizeTableServiceProperties(modelsPackage.getClass("TableServiceProperties"));

        PackageCustomization implementationModelsPackage
            = customization.getPackage("com.azure.data.tables.implementation.models");
        customizeTableEntityQueryResponse(implementationModelsPackage.getClass("TableEntityQueryResponse"));
    }

    private static void customizeTableSignedIdentifier(ClassCustomization customization) {
        customization.customizeAst(ast -> {
            ast.addImport("java.util.Objects");

            ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
                clazz.getDefaultConstructor().ifPresent(ctor -> {
                    ctor.setJavadocComment(ctor.getJavadoc().get()
                        .addBlockTag("param", "id", "A unique id for this {@link TableSignedIdentifier}.")
                        .addBlockTag("throws", "NullPointerException", "If {@code id} is null."));
                    ctor.addParameter("String", "id");
                    ctor.setBody(StaticJavaParser.parseBlock(
                        "{this.id = Objects.requireNonNull(id, \"'id' cannot be null\");}"));
                });
                // Remove "setId" as the ID is read-only (API-wise).
                clazz.getMethodsByName("setId").forEach(Node::remove);

                clazz.addConstructor(Modifier.Keyword.PRIVATE);
            });
        });
    }

    private static void customizeTableServiceGeoReplication(ClassCustomization customization) {
        customization.customizeAst(ast -> {
            ast.addImport("java.time.OffsetDateTime");
            ast.addImport("com.azure.core.annotation.Immutable");

            ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
                clazz.getAnnotationByName("Fluent").ifPresent(Node::remove);
                clazz.addMarkerAnnotation("Immutable");

                clazz.getDefaultConstructor().ifPresent(ctor -> {
                    ctor.setJavadocComment(ctor.getJavadoc().get()
                        .addBlockTag("param", "status", "The status of the secondary location.")
                        .addBlockTag("param", "lastSyncTime", "A GMT date/time value, to the second. All "
                            + "primary writes preceding this value are guaranteed\nto be available for read operations "
                            + "at the secondary. Primary writes after this point in time may or may not\nbe available "
                            + "for reads."));
                    ctor.addParameter("TableServiceGeoReplicationStatus", "status");
                    ctor.addParameter("OffsetDateTime", "lastSyncTime");
                    ctor.setBody(StaticJavaParser.parseBlock(
                        "{this.status = status;\nthis.lastSyncTime = (lastSyncTime == null) ? null : new DateTimeRfc1123(lastSyncTime);}"));
                });

                // Remove "setStatus" as the status is read-only (API-wise).
                clazz.getMethodsByName("setStatus").forEach(Node::remove);
                // Remove "setLastSyncTime" as the last sync time is read-only (API-wise).
                clazz.getMethodsByName("setLastSyncTime").forEach(Node::remove);

                clazz.addConstructor(Modifier.Keyword.PRIVATE);
            });
        });
    }

    private static void customizeTableServiceStatistics(ClassCustomization customization) {
        customization.customizeAst(ast -> {
            ast.addImport("com.azure.core.annotation.Immutable");

            ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
                clazz.getAnnotationByName("Fluent").ifPresent(Node::remove);
                clazz.addMarkerAnnotation("Immutable");

                clazz.getDefaultConstructor().ifPresent(ctor -> {
                    ctor.setJavadocComment(ctor.getJavadoc().get().addBlockTag("param", "geoReplication",
                        "Geo-Replication information for the Secondary Storage Service."));
                    ctor.addParameter("TableServiceGeoReplication", "geoReplication");
                    ctor.setBody(StaticJavaParser.parseBlock("{this.geoReplication = geoReplication;}"));
                });

                // Remove "setGeoReplication" as the geo replication is read-only (API-wise).
                clazz.getMethodsByName("setGeoReplication").forEach(Node::remove);

                clazz.addConstructor(Modifier.Keyword.PRIVATE);
            });
        });
    }

    private static void customizeTableServiceMetrics(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName()).ifPresent(clazz ->
            clazz.getMethodsByName("getRetentionPolicy")
                .forEach(method -> method.setName("getTableServiceRetentionPolicy"))));
    }

    private static void customizeTableServiceProperties(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
            clazz.getMethodsByName("getCors").forEach(method -> method.setName("getCorsRules")
                .setBody(StaticJavaParser.parseBlock("{return this.cors;}")));

            clazz.getMethodsByName("setCors").forEach(method -> method.setName("setCorsRules"));
        }));
    }

    private static void customizeTableEntityQueryResponse(ClassCustomization customization) {
        customization.customizeAst(ast -> {
            ast.addImport("com.azure.data.tables.implementation.TablesJacksonSerializer");
            ast.getClassByName(customization.getClassName()).ifPresent(clazz ->
                clazz.getMethodsByName("fromJson").forEach(method -> method.setBody(StaticJavaParser.parseBlock(
                    "{return TablesJacksonSerializer.deserializeTableEntityQueryResponse(jsonReader);}"))));
        });
    }
}
