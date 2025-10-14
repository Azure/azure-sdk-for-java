// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.javadoc.Javadoc;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;

import static com.github.javaparser.StaticJavaParser.parseBlock;
import static com.github.javaparser.StaticJavaParser.parseExpression;
import static com.github.javaparser.StaticJavaParser.parseStatement;
import static com.github.javaparser.javadoc.description.JavadocDescription.parseText;

/**
 * This class contains the customization code to customize the AutoRest generated code for Event Grid.
 */
public class EventGridSystemEventsCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization systemEvent = customization.getPackage("com.azure.messaging.eventgrid.systemevents" +
            ".models");
        // Manual listing of classes in the package until a bug is fixed in TypeSpec Java.
        String packagePath = "src/main/java/com/azure/messaging/eventgrid/systemevents/models/";
        List<ClassCustomization> classCustomizations = customization.getRawEditor().getContents().keySet().stream()
            .filter(fileName -> fileName.startsWith(packagePath))
            .map(fileName -> fileName.substring(packagePath.length(), fileName.length() - 5))
            .filter(className -> !className.contains("/") && !"package-info".equals(className))
            .map(systemEvent::getClass)
            .collect(Collectors.toList());

        Map<String, String> nameMap = new TreeMap<>();
        Map<String, String> descriptionMap = new TreeMap<>();
        Map<String, String> constantNameMap = new TreeMap<>();
        List<String> imports = new ArrayList<>();

        logger.info("Total number of classes {}", classCustomizations.size());

        List<ClassCustomization> eventData = classCustomizations
            .stream()
            .filter(classCustomization -> classCustomization.getClassName().endsWith("EventData"))
            .collect(Collectors.toList());

        List<String> validEventDescription = eventData.stream()
            .map(classCustomization -> {
                String className = classCustomization.getClassName();
                AtomicReference<String> javadocRef = new AtomicReference<>();
                classCustomization.customizeAst(ast -> ast.getClassByName(className)
                    .flatMap(NodeWithJavadoc::getJavadocComment)
                    .ifPresent(javadoc -> javadocRef.set(javadoc.getContent())));

                String javadoc = javadocRef.get();
                int startIndex = javadoc.indexOf("Microsoft.");
                int endIndex = javadoc.lastIndexOf(" event.");
                boolean hasEventName = startIndex > 0 && endIndex > 0;
                if (!hasEventName) {
                    logger.info("Class {} {}", classCustomization.getClassName(), javadoc);
                    return null;
                }

                endIndex = javadoc.indexOf(" ", startIndex);
                String eventName = javadoc.substring(startIndex, endIndex);
                String constantName = getConstantName(className.replace("EventData", ""));

                constantNameMap.put(className, constantName);
                nameMap.put(className, eventName);
                descriptionMap.put(className, javadoc);
                imports.add(className);
                return eventName;
            })
            .collect(Collectors.toList());

        Collections.sort(imports);

        CompilationUnit compilationUnit = new CompilationUnit();

        compilationUnit.addOrphanComment(new LineComment(" Copyright (c) Microsoft Corporation. All rights reserved."));
        compilationUnit.addOrphanComment(new LineComment(" Licensed under the MIT License."));

        compilationUnit.setPackageDeclaration("com.azure.messaging.eventgrid.systemevents");

        compilationUnit.addImport("java.util.Collections");
        compilationUnit.addImport("java.util.HashMap");
        compilationUnit.addImport("java.util.Map");
        // these two imports are for deprecated events.
        compilationUnit.addImport("com.azure.messaging.eventgrid.systemevents.models" +
            ".AcsChatMemberAddedToThreadWithUserEventData");
        compilationUnit.addImport("com.azure.messaging.eventgrid.systemevents.models." +
            "AcsChatMemberRemovedFromThreadWithUserEventData");
        for (String className : imports) {
            compilationUnit.addImport("com.azure.messaging.eventgrid.systemevents.models." + className);
        }

        ClassOrInterfaceDeclaration clazz = compilationUnit.addClass("SystemEventNames", Modifier.Keyword.PUBLIC,
            Modifier.Keyword.FINAL);

        clazz.setJavadocComment("This class contains a number of constants that correspond to the value of "
            + "{@code eventType} of {@code EventGridEvent}s and {@code code} of {@code CloudEvent}s, when the event "
            + "originated from an Azure service. This list should be updated with all the service event strings. It "
            + "also contains a mapping from each service event string to the model class that the event string "
            + "corresponds to in the {@code data} field, which is used to automatically deserialize system events by "
            + "their known string.");

        for (String className : imports) {
            clazz.addFieldWithInitializer("String", constantNameMap.get(className),
                new StringLiteralExpr(nameMap.get(className)), Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC,
                Modifier.Keyword.FINAL)
                .setJavadocComment(descriptionMap.get(className));
        }

        clazz.addFieldWithInitializer("Map<String, Class<?>>", "SYSTEM_EVENT_MAPPINGS",
            parseExpression("new HashMap<String, Class<?>>()"), Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC,
            Modifier.Keyword.FINAL);

        BlockStmt staticInitializer = clazz.addStaticInitializer();

        for (String className : imports) {
            staticInitializer.addStatement(parseStatement("SYSTEM_EVENT_MAPPINGS.put(" + constantNameMap.get(className)
                + ", " + className + ".class);"));
        }

        clazz.addMethod("getSystemEventMappings", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
            .setType("Map<String, Class<?>>")
            .setBody(parseBlock("{ return Collections.unmodifiableMap(SYSTEM_EVENT_MAPPINGS); }"))
            .setJavadocComment(new Javadoc(parseText("Get a mapping of all the system event type strings to their "
                + "respective class. This is used by default in the {@code EventGridEvent} and {@code CloudEvent} classes."))
                .addBlockTag("return", "a mapping of all the system event strings to system event objects."));

        clazz.addConstructor(Modifier.Keyword.PRIVATE);

        logger.info("Total number of events {}", eventData.size());
        logger.info("Total number of events with proper description {}", validEventDescription.size());

        customization.getRawEditor()
            .addFile("src/main/java/com/azure/messaging/eventgrid/systemevents/SystemEventNames.java",
                compilationUnit.toString());

        customizeAcsRouterEvents(systemEvent);
        customizeAcsRecordingFileStatusUpdatedEventDataDuration(systemEvent);
        customizeStorageDirectoryDeletedEventData(systemEvent);
        customizeAcsMessageEventDataAndInheritingClasses(systemEvent);
        customizeIothubEventData(systemEvent);
        customizeEventGridMQTTClientEventData(systemEvent);
    }

    public void customizeEventGridMQTTClientEventData(PackageCustomization customization) {
        customization.getClass("EventGridMqttClientEventData").customizeAst(ast ->
            ast.getClassByName("EventGridMqttClientEventData")
                .ifPresent(clazz -> clazz.getMethodsByName("setClientName")
                    .forEach(method -> method.setModifiers(Modifier.Keyword.PRIVATE))));

        //// For inherited classes, remove the getClientName method entirely to avoid override conflicts
        //List<String> inheritedClassNames = Arrays.asList("EventGridMqttClientCreatedOrUpdatedEventData",
        //    "EventGridMqttClientDeletedEventData", "EventGridMqttClientSessionConnectedEventData",
        //    "EventGridMqttClientSessionDisconnectedEventData");
        //
        //for (String className : inheritedClassNames) {
        //    customization.getClass(className).customizeAst(ast -> {
        //        ast.getClassByName(className).ifPresent(clazz -> {
        //            // Remove the getClientName method from inherited classes
        //            clazz.getMethodsByName("getClientName")
        //                .forEach(method -> clazz.remove(method));
        //        });
        //    });
        //}
    }

    public void customizeIothubEventData(PackageCustomization customization) {
        customization.getClass("DeviceTwinMetadata").customizeAst(ast -> ast.getClassByName("DeviceTwinMetadata")
            .ifPresent(clazz -> clazz.getMethodsByName("getLastUpdated").forEach(m -> m.setType(OffsetDateTime.class)
                .setBody(parseBlock("{ return lastUpdated == null ? null : OffsetDateTime.parse(lastUpdated); }")))));

        customization.getClass("DeviceTwinInfo").customizeAst(ast -> ast.getClassByName("DeviceTwinInfo")
            .ifPresent(clazz -> clazz.getMethodsByName("getLastActivityTime").forEach(m -> m.setType(OffsetDateTime.class)
                .setBody(parseBlock("{ return lastActivityTime == null ? null : OffsetDateTime.parse" +
                    "(lastActivityTime); }")))));

        customization.getClass("DeviceTwinInfo").customizeAst(ast -> ast.getClassByName("DeviceTwinInfo")
            .ifPresent(clazz -> clazz.getMethodsByName("getStatusUpdateTime").forEach(m -> m.setType(OffsetDateTime.class)
                .setBody(parseBlock("{ return statusUpdateTime == null ? null : OffsetDateTime.parse(statusUpdateTime); }")))));
    }

    public void customizeAcsRouterEvents(PackageCustomization customization) {
        customization.getClass("AcsRouterWorkerSelector").customizeAst(ast -> ast.getClassByName("AcsRouterWorkerSelector")
            .ifPresent(clazz -> clazz.getMethodsByName("getTimeToLive").forEach(m -> m.setType(Duration.class)
                .setBody(parseBlock("{ return Duration.ofSeconds((long)timeToLive); }")))));

        customization.getClass("AcsRouterJobClassificationFailedEventData").customizeAst(ast -> {
            ast.addImport("com.azure.core.models.ResponseError");
            ast.addImport("java.util.stream.Collectors");

            ast.getClassByName("AcsRouterJobClassificationFailedEventData").ifPresent(clazz ->
                clazz.getMethodsByName("getErrors").forEach(m -> m.setType("List<ResponseError>")
                    .setBody(parseBlock("{ return this.errors.stream().map(e -> "
                        + "new ResponseError(e.getCode(), e.getMessage())).collect(Collectors.toList()); }"))));
        });
    }

    public void customizeAcsRecordingFileStatusUpdatedEventDataDuration(PackageCustomization customization) {
        customization.getClass("AcsRecordingFileStatusUpdatedEventData").customizeAst(ast -> {
            ast.addImport("java.time.Duration");
            ast.getClassByName("AcsRecordingFileStatusUpdatedEventData")
                .ifPresent(clazz -> clazz.getMethodsByName("getRecordingDuration").forEach(method -> method.setType("Duration")
                    .setBody(parseBlock("{ if (this.recordingDuration != null) { return Duration.ofMillis(this.recordingDuration); } return null; }"))
                    .setJavadocComment(new Javadoc(parseText("Get the recordingDuration property: The recording duration."))
                        .addBlockTag("return", "the recordingDuration value."))));
        });
    }

    public void customizeStorageDirectoryDeletedEventData(PackageCustomization customization) {
        customization.getClass("StorageDirectoryDeletedEventData").customizeAst(ast -> ast.getClassByName("StorageDirectoryDeletedEventData")
            .ifPresent(clazz -> clazz.getMethodsByName("getRecursive").forEach(m -> m.setName("isRecursive")
                .setType(Boolean.class)
                .setBody(parseBlock("{ return Boolean.getBoolean(this.recursive); }")))));
    }

    public void customizeAcsMessageEventDataAndInheritingClasses(PackageCustomization customization) {
        List<String> classNames = Arrays.asList("AcsMessageEventData", "AcsMessageDeliveryStatusUpdatedEventData",
            "AcsMessageReceivedEventData");
        for (String className : classNames) {
            customization.getClass(className).customizeAst(ast -> {
                ast.addImport("com.azure.core.models.ResponseError");
                ast.getClassByName(className).ifPresent(clazz -> clazz.getMethodsByName("getError").forEach(method ->
                    method.setType("ResponseError")
                        .setBody(parseBlock("{ return new ResponseError(this.error.getChannelCode(), this.error.getChannelMessage()); }"))
                        .setJavadocComment(new Javadoc(parseText("Get the error property: The channel error code and message."))
                            .addBlockTag("return", "the error value."))));
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
