// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.ast.nodeTypes.NodeWithModifiers;
import com.github.javaparser.javadoc.Javadoc;
import org.slf4j.Logger;

import java.util.List;

import static com.github.javaparser.javadoc.description.JavadocDescription.parseText;

/**
 * This class contains the customization code to customize the AutoRest generated code for OpenAI.
 */
public class AppConfigCustomizations extends Customization {
    private static final String ROOT_FILE_PATH = "src/main/java/com/azure/v2/data/appconfiguration/";

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage("com.azure.v2.data.appconfiguration.models");
        PackageCustomization appConfigPackages = customization.getPackage("com.azure.v2.data.appconfiguration");
        hideClient(appConfigPackages);
        hideModels(models);
        renameServiceVersionClassName(customization.getRawEditor());

//        customizeKeyValueFilter(models.getClass("ConfigurationSettingsFilter"));
//        customizeKeyValueFields(models.getClass("SettingFields"));
//        customizeSnapshot(models.getClass("ConfigurationSnapshot"));
    }

    private void hideModels(PackageCustomization models) {
        // TODO: move both 'Key' and 'KeyValue' class to implementation package
        makeClassPackagePrivate(models, "Key");
        makeClassPackagePrivate(models, "KeyValue");
    }

    private void hideClient(PackageCustomization appConfigPackages) {
        // TODO: move both 'AzureAppConfigurationClient' and 'AzureAppConfigurationClientBuilder' class to
        //  implementation package
        makeClassPackagePrivate(appConfigPackages, "AzureAppConfigurationClient");
        makeClassPackagePrivate(appConfigPackages, "AzureAppConfigurationClientBuilder");
    }

    private static void makeClassPackagePrivate(PackageCustomization customization, String className) {
        customization.getClass(className).customizeAst(ast -> ast.getClassByName(className)
            .ifPresent(NodeWithModifiers::setModifiers));
    }

    private void renameServiceVersionClassName(Editor editor) {
        String serviceVersion = editor.getFileContent(ROOT_FILE_PATH + "AzureAppConfigurationServiceVersion.java")
            .replace("AzureAppConfigurationServiceVersion", "ConfigurationServiceVersion");

        editor.addFile(ROOT_FILE_PATH + "ConfigurationServiceVersion.java", serviceVersion);
        editor.removeFile(ROOT_FILE_PATH + "AzureAppConfigurationServiceVersion.java");

        for (String path : List.of("AzureAppConfigurationClientBuilder", "implementation/AzureAppConfigurationClientImpl")) {
            String fileName = ROOT_FILE_PATH + path + ".java";
            String fileContent = editor.getFileContent(fileName);
            fileContent = fileContent.replace("AzureAppConfigurationServiceVersion", "ConfigurationServiceVersion");
            editor.replaceFile(fileName, fileContent);
        }
    }

    // TODO: LRO is not support yet in codegen-v2, wait for codegen-v2 to support LRO
    // KeyValueFilter is used in LRO and no other place use it, so it is not generate setters.
    private void customizeKeyValueFilter(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
            // Edit javadoc of `setLabel` method
            clazz.getMethodsByName("setLabel").forEach(method -> replaceDescription(method,
                "Set the label property: Filters {@link ConfigurationSetting} by their label field."));
            // Edit javadoc of `getKey` method
            clazz.getMethodsByName("getKey").forEach(method -> replaceDescription(method,
                "Get the key property: Filters {@link ConfigurationSetting} by their key field."));
            // Edit javadoc of `getLabel` method
            clazz.getMethodsByName("getLabel").forEach(method -> replaceDescription(method,
                "Get the label property: Filters {@link ConfigurationSetting} by their label field."));
        }));
    }

    private static void replaceDescription(NodeWithJavadoc<?> node, String newDescription) {
        node.getJavadoc().ifPresent(javadoc -> {
            javadoc.getDescription().getElements().clear();
            javadoc.getDescription().getElements().addAll(parseText(newDescription).getElements());
            node.setJavadocComment(javadoc);
        });
    }

    private void customizeSnapshot(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.addImport("java.time.Duration")
            .getClassByName(customization.getClassName()).ifPresent(clazz -> {
                // Transfer Long to Duration internally
                clazz.getMethodsByName("getRetentionPeriod").forEach(method -> method.setType("Duration")
                    .setBody(StaticJavaParser.parseBlock("{ return this.retentionPeriod == null ? null : Duration.ofSeconds(this.retentionPeriod); }")));

                clazz.getMethodsByName("setRetentionPeriod").forEach(method -> method
                    .setParameter(0, new Parameter().setType("Duration").setName("retentionPeriod"))
                    .setBody(StaticJavaParser.parseBlock("{ this.retentionPeriod = retentionPeriod == null ? null : retentionPeriod.getSeconds(); return this; }")));
            }));
    }

    private void customizeKeyValueFields(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.addImport("java.util.Locale")
            .getClassByName(customization.getClassName()).ifPresent(clazz -> {
                // Modify fromString() method
                clazz.getMethodsByName("fromString").forEach(method -> method.setJavadocComment(new Javadoc(
                    parseText("Creates or finds a {@link SettingFields} from its string representation."))
                    .addBlockTag("param", "name", "a name to look for.")
                    .addBlockTag("return", "the corresponding {@link SettingFields}")));

                // Add class-level javadoc
                clazz.setJavadocComment(new Javadoc(
                    parseText("Fields in {@link ConfigurationSetting} that can be returned from GET queries."))
                    .addBlockTag("see", "SettingSelector"));

                // Add toStringMapper static new method to SettingFields
                clazz.addMethod("toStringMapper", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC).setType("String")
                    .addParameter("SettingFields", "field")
                    .setBody(StaticJavaParser.parseBlock("{ return field.toString().toLowerCase(Locale.US); }"))
                    .addMarkerAnnotation(Deprecated.class)
                    .setJavadocComment(new Javadoc(parseText(
                        "Converts the SettingFields to a string that is usable for HTTP requests and logging."))
                        .addBlockTag("param", "field", "SettingFields to map.")
                        .addBlockTag("return", "SettingFields as a lowercase string in the US locale.")
                        .addBlockTag("deprecated", "This method is no longer needed. SettingFields is using lower case enum value for the HTTP requests."));
            }));
    }
}
