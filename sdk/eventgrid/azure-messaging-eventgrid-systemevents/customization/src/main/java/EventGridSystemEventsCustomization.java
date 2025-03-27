// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.github.javaparser.javadoc.description.JavadocSnippet;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.javaparser.StaticJavaParser.parseBlock;

/**
 * This class contains the customization code to customize the AutoRest generated code for Event Grid.
 */
public class EventGridSystemEventsCustomization extends Customization {
    private static final String newLine = System.lineSeparator();

    private static final String SYSTEM_EVENT_CLASS_HEADER = "// Copyright (c) Microsoft Corporation. All rights reserved." + newLine +
        "// Licensed under the MIT License." + newLine + newLine +
        "package com.azure.messaging.eventgrid;" + newLine + newLine;

    private static final String CLASS_DEF = "/**" + newLine +
        " * This class contains a number of constants that correspond to the value of {@code eventType} of {@code EventGridEvent}s" + newLine +
        " * and {@code code} of {@code CloudEvent}s, when the event originated from an Azure service. This list should be" + newLine +
        " * updated with all the service event strings. It also contains a mapping from each service event string to the" + newLine +
        " * model class that the event string corresponds to in the {@code data} field, which is used to automatically deserialize" + newLine +
        " * system events by their known string." + newLine +
        " */" + newLine +
        "public final class SystemEventNames {" + newLine;

    private static final String PRIVATE_CTOR = "/**" + newLine +
        "     * Get a mapping of all the system event type strings to their respective class. This is used by default in" + newLine +
        "     * the {@code EventGridEvent} and {@code CloudEvent} classes." + newLine +
        "     * @return a mapping of all the system event strings to system event objects." + newLine +
        "     */" + newLine +
        "    public static Map<String, Class<?>> getSystemEventMappings() {" + newLine +
        "        return Collections.unmodifiableMap(SYSTEM_EVENT_MAPPINGS);" + newLine +
        "    }" + newLine +
        newLine +
        "    private SystemEventNames() { " + newLine +
        "    }";

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        customizeModuleInfo(customization, logger);
        customizeAcsRouterEvents(customization, logger);
        customizeAcsRecordingFileStatusUpdatedEventDataDuration(customization, logger);
        customizeStorageDirectoryDeletedEventData(customization, logger);
        customizeAcsMessageEventDataAndInheritingClasses(customization, logger);
        generateSystemEventNames(customization, logger);
    }


    public void generateSystemEventNames(LibraryCustomization customization, Logger logger) {

        PackageCustomization packageCustomization = customization.getPackage("com.azure.messaging.eventgrid.systemevents");

        List<ClassCustomization> classCustomizations = customization.getPackage("com.azure.messaging.eventgrid.systemevents")
            .listClasses();

        Map<String, String> nameMap = new TreeMap<>();
        Map<String, String> classMap = new TreeMap<>();
        Map<String, String> descriptionMap = new TreeMap<>();
        Map<String, String> constantNameMap = new TreeMap<>();
        List<String> imports = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        logger.info("Total number of classes " + classCustomizations.size());

        List<ClassCustomization> eventData = classCustomizations
            .stream()
            .filter(classCustomization -> classCustomization.getClassName().endsWith("EventData"))
            .collect(Collectors.toList());

        List<String> validEventDescription = eventData.stream()
            .filter(classCustomization -> {
                int startIndex =
                    classCustomization.getJavadoc().getDescription().indexOf("Microsoft.");
                int endIndex = classCustomization.getJavadoc().getDescription().lastIndexOf(" event.");
                boolean hasEventName = startIndex > 0 && endIndex > 0;
                if (!hasEventName) {
                    logger.info("Class " + classCustomization.getClassName() + " " + classCustomization.getJavadoc().getDescription());
                }
                return hasEventName;
            })
            .map(classCustomization -> {
                int startIndex =
                    classCustomization.getJavadoc().getDescription().indexOf("Microsoft.");
                int endIndex = classCustomization.getJavadoc().getDescription().indexOf(" ", startIndex);
                String eventName = classCustomization.getJavadoc().getDescription().substring(startIndex, endIndex);
                String className = classCustomization.getClassName();
                String constantName = getConstantName(className.replace("EventData", ""));

                constantNameMap.put(className, constantName);
                nameMap.put(className, eventName);
                classMap.put(className, className + ".class");
                descriptionMap.put(className, classCustomization.getJavadoc().getDescription());
                imports.add(className);
                return eventName;
            })
            .collect(Collectors.toList());

        Collections.sort(imports);
        sb.append(SYSTEM_EVENT_CLASS_HEADER);

        Consumer<String> appender = s -> {
            sb.append(s);
            sb.append(newLine);
        };

        appender.accept("import java.util.Collections;");
        appender.accept("import java.util.HashMap;");
        appender.accept("import java.util.Map;");
        // these two imports are for deprecated events.
        appender.accept("import com.azure.messaging.eventgrid.systemevents.AcsChatMemberAddedToThreadWithUserEventData;");
        appender.accept("import com.azure.messaging.eventgrid.systemevents.AcsChatMemberRemovedFromThreadWithUserEventData;");
        for (String className : imports) {
            appender.accept("import com.azure.messaging.eventgrid.systemevents." + className + ";");
        }
        appender.accept(CLASS_DEF);

        for (String className : imports) {
            appender.accept("/**");
            appender.accept("* " + descriptionMap.get(className));
            appender.accept("*/");
            appender.accept("public static final String " + constantNameMap.get(className) + " = \"" + nameMap.get(className) + "\";");
            appender.accept("");
        }

        appender.accept("private static final Map<String, Class<?>> SYSTEM_EVENT_MAPPINGS = new HashMap<String, Class<?>>() {{");

        for (String className : imports) {
            appender.accept("put(" + constantNameMap.get(className) + ", " + classMap.get(className) + ");");
        }
        appender.accept("}};");

        appender.accept(PRIVATE_CTOR);

        appender.accept("}");
        logger.info("Total number of events " + eventData.size());
        logger.info("Total number of events with proper description " + validEventDescription.size());

        customization.getRawEditor()
            .addFile("src/main/java/com/azure/messaging/eventgrid/SystemEventNames.java", sb.toString());

        Editor editor = customization.getRawEditor();
        editor.addFile("src/main/java/com/azure/messaging/eventgrid/SystemEventNames.java", SYSTEM_EVENT_CLASS_HEADER + CLASS_DEF + PRIVATE_CTOR);
    }


    /**
     * Customize the module-info.java file. This is necessary due to having a models subpackage logically; we
     * end up with an export for a package with no types, so we remove the export.
     *
     * @param customization The LibraryCustomization object.
     * @param logger        The logger object.
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

    public void customizeAcsMessageEventDataAndInheritingClasses(LibraryCustomization customization, Logger logger) {
        PackageCustomization packageModels = customization.getPackage("com.azure.messaging.eventgrid.systemevents");
        List<String> classNames = Arrays.asList(
            "AcsMessageEventData",
            "AcsMessageDeliveryStatusUpdatedEventData",
            "AcsMessageReceivedEventData"
        );
        for (String className : classNames) {
            ClassCustomization classCustomization = packageModels.getClass(className);
            classCustomization.addImports("com.azure.core.models.ResponseError");
            classCustomization.customizeAst(comp -> {
                ClassOrInterfaceDeclaration clazz = comp.getClassByName(className).get();
                // Fix up the getError method to always return a ResponseError.
                clazz.getMethodsByName("getError").forEach(m -> {
                    m.setType("ResponseError")
                        .setBody(parseBlock("{ return new ResponseError(this.error.getChannelCode(), this.error.getChannelMessage()); }"))
                        .setJavadocComment(new Javadoc(new JavadocDescription(List.of(new JavadocSnippet("Get the error property: The channel error code and message."))))
                            .addBlockTag("return", "the error value."));
                });
            });
        }
    }

    public static String getConstantName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return name;
        }

        // trim leading and trailing '_'
        if ((name.startsWith("_") || name.endsWith("_")) && !name.chars().allMatch(c -> c == '_')) {
            StringBuilder sb = new StringBuilder(name);
            while (sb.length() > 0 && sb.charAt(0) == '_') {
                sb.deleteCharAt(0);
            }
            while (sb.length() > 0 && sb.charAt(sb.length() - 1) == '_') {
                sb.setLength(sb.length() - 1);
            }
            name = sb.toString();
        }

        String result = name;
        result = result.replaceAll("_{2,}", "_");  // merge multiple underlines
        Function<Character, Boolean> isUpper = c -> c >= 'A' && c <= 'Z';
        Function<Character, Boolean> isLower = c -> c >= 'a' && c <= 'z';
        for (int i = 1; i < result.length() - 1; i++) {
            if (isUpper.apply(result.charAt(i))) {
                if (result.charAt(i - 1) != '_' && isLower.apply(result.charAt(i - 1))) {
                    result = result.substring(0, i) + "_" + result.substring(i);
                }
            }
        }
        return result.toUpperCase();
    }

}
