// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.github.javaparser.javadoc.description.JavadocSnippet;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.List;

import static com.github.javaparser.StaticJavaParser.parseBlock;

/**
 * This class contains the customization code to customize the AutoRest generated code for Event Grid.
 */
public class EventGridSystemEventsCustomization extends Customization {
    private final String newLine = System.lineSeparator();
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        customizeModuleInfo(customization, logger);
        customizeMediaLiveEventChannelArchiveHeartbeatEventData(customization, logger);
        customizeAcsRouterEvents(customization, logger);
        customizeAcsRecordingFileStatusUpdatedEventDataDuration(customization, logger);
        customizeStorageDirectoryDeletedEventData(customization, logger);
    }

    /**
     * Customize the module-info.java file. This is necessary due to having a models subpackage logically; we
     * end up with an export for a package with no types, so we remove the export.
     *
     * @param customization The LibraryCustomization object.
     * @param logger The logger object.
     */
    public void customizeModuleInfo(LibraryCustomization customization, Logger logger) {

        Editor editor = customization.getRawEditor();
        List<String> lines = editor.getFileLines("src/main/java/module-info.java");
        StringBuilder sb = new StringBuilder();
        lines.forEach(line -> {
            if (!line.trim().equals("exports com.azure.messaging.eventgrid;")) {
                sb.append(line).append(newLine);
            }
        });
        editor.replaceFile("src/main/java/module-info.java", sb.toString());
    }

    /**
     * Customize the MediaLiveEventChannelArchiveHeartbeatEventData.getChannelLatency method to return a Duration object.
     * @param customization The LibraryCustomization object.
     * @param logger The logger object.
     */
    public void customizeMediaLiveEventChannelArchiveHeartbeatEventData(LibraryCustomization customization, Logger logger) {
        PackageCustomization packageModels = customization.getPackage("com.azure.messaging.eventgrid.systemevents");
        ClassCustomization classCustomization = packageModels.getClass("MediaLiveEventChannelArchiveHeartbeatEventData");
        classCustomization.addStaticBlock("static final ClientLogger LOGGER = new ClientLogger(MediaLiveEventChannelArchiveHeartbeatEventData.class);");
        logger.info("Fixing getChannelLatency");
        classCustomization.customizeAst(ast -> {
            ast.addImport("java.time.Duration");
            ast.addImport("com.azure.core.util.logging.ClientLogger");
            ClassOrInterfaceDeclaration clazz = ast.getClassByName("MediaLiveEventChannelArchiveHeartbeatEventData").get();
            clazz.getMethodsByName("getChannelLatency").forEach(method -> {
                method.setType("Duration");
                method.setBody(parseBlock("{ if (\"n/a\".equals(this.channelLatency)) { return null; } Long channelLatencyLong; try { channelLatencyLong = Long.parseLong(this.channelLatency); } catch (NumberFormatException ex) { LOGGER.logExceptionAsError(ex); return null; } return Duration.ofMillis(channelLatencyLong); }"));
                method.setJavadocComment(new Javadoc(new JavadocDescription(List.of(new JavadocSnippet("Gets the duration of channel latency."))))
                    .addBlockTag("return", "the duration of channel latency."));
            });

        });
    }

    public void customizeAcsRouterEvents(LibraryCustomization customization, Logger logger) {
        PackageCustomization packageModels = customization.getPackage("com.azure.messaging.eventgrid.systemevents");
        ClassCustomization classCustomization = packageModels.getClass("AcsRouterWorkerSelector");

        classCustomization.customizeAst(comp -> {
            ClassOrInterfaceDeclaration clazz = comp.getClassByName("AcsRouterWorkerSelector").get();
            clazz.getMethodsByName("getTimeToLive").forEach(m -> {
                m.setType(Duration.class);
                m.setBody(parseBlock("{ return Duration.ofSeconds((long)timeToLive); }"));
            });
        });

        classCustomization = packageModels.getClass("AcsRouterJobClassificationFailedEventData");
        classCustomization.addImports("com.azure.core.models.ResponseError");
        classCustomization.addImports("java.util.stream.Collectors");
        classCustomization.customizeAst(comp -> {
            ClassOrInterfaceDeclaration clazz = comp.getClassByName("AcsRouterJobClassificationFailedEventData").get();
            clazz.getMethodsByName("getErrors").forEach(m -> {
                m.setType("List<ResponseError>");
                m.setBody(parseBlock("{ return this.errors.stream().map(e -> new ResponseError(e.getCode(), e.getMessage())).collect(Collectors.toList()); }"));
            });
        });
    }

    public void customizeAcsRecordingFileStatusUpdatedEventDataDuration(LibraryCustomization customization, Logger logger) {
        PackageCustomization packageModels = customization.getPackage("com.azure.messaging.eventgrid.systemevents");
        ClassCustomization classCustomization = packageModels.getClass("AcsRecordingFileStatusUpdatedEventData");

        classCustomization.customizeAst(ast -> {
            ast.addImport("java.time.Duration");
            ClassOrInterfaceDeclaration clazz = ast.getClassByName("AcsRecordingFileStatusUpdatedEventData").get();
            clazz.getMethodsByName("getRecordingDuration").forEach(method -> {
                method.setType("Duration");
                method.setBody(parseBlock("{ if (this.recordingDuration != null) { return Duration.ofMillis(this.recordingDuration); } return null; }"));
                method.setJavadocComment(new Javadoc(new JavadocDescription(List.of(new JavadocSnippet("Get the recordingDuration property: The recording duration."))))
                    .addBlockTag("return", "the recordingDuration value."));
            });

        });
    }

    public void customizeStorageDirectoryDeletedEventData(LibraryCustomization customization, Logger logger) {
        PackageCustomization packageModels = customization.getPackage("com.azure.messaging.eventgrid.systemevents");
        ClassCustomization classCustomization = packageModels.getClass("StorageDirectoryDeletedEventData");
        classCustomization.getMethod("getRecursive").rename("isRecursive").setReturnType("Boolean", "Boolean.getBoolean(%s)");
    }

}
