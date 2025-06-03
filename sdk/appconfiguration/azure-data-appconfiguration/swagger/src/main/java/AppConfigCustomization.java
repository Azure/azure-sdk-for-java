// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

/**
 * This class contains the customization code to customize the AutoRest generated code for App Configuration.
 */
public class AppConfigCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage("com.azure.data.appconfiguration.models");

        customizeKeyValueFilter(models.getClass("ConfigurationSettingsFilter"));
        customizeKeyValueFields(models.getClass("SettingFields"));
        customizeSnapshot(models.getClass("ConfigurationSnapshot"));
    }

    private void customizeSnapshot(ClassCustomization customization) {
        customization.customizeAst(ast -> {
            ast.addImport("java.time.Duration");

            ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
                // Transfer Long to Duration internally
                clazz.getMethodsByName("getRetentionPeriod").forEach(method -> method.setType("Duration")
                    .setBody(StaticJavaParser.parseBlock("{ return this.retentionPeriod == null ? null : Duration.ofSeconds(this.retentionPeriod); }")));

                clazz.getMethodsByName("setRetentionPeriod").forEach(method ->
                    method.setParameter(0, new Parameter().setType("Duration").setName("retentionPeriod"))
                    .setBody(StaticJavaParser.parseBlock("{ this.retentionPeriod = retentionPeriod == null ? null : retentionPeriod.getSeconds(); return this; }")));
            });
        });
    }

    private void customizeKeyValueFilter(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
            // Edit javadoc of `setLabel` method
            clazz.getMethodsByName("setLabel").forEach(method -> method.getJavadoc()
                .ifPresent(javadoc -> method.setJavadocComment(replaceJavadocDescription(javadoc,
                    "Set the label property: Filters {@link ConfigurationSetting} by their label field."))));
            // Edit javadoc of `getKey` method
            clazz.getMethodsByName("getKey").forEach(method -> method.getJavadoc()
                .ifPresent(javadoc -> method.setJavadocComment(replaceJavadocDescription(javadoc,
                    "Get the key property: Filters {@link ConfigurationSetting} by their key field."))));
            // Edit javadoc of `getLabel` method
            clazz.getMethodsByName("getLabel").forEach(method -> method.getJavadoc()
                .ifPresent(javadoc -> method.setJavadocComment(replaceJavadocDescription(javadoc,
                    "Get the label property: Filters {@link ConfigurationSetting} by their label field."))));
        }));
    }

    private void customizeKeyValueFields(ClassCustomization customization) {
        customization.customizeAst(ast -> {
            // Add imports required by class changes.
            ast.addImport("java.util.Locale");

            ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {

                // Modify fromString() method
                clazz.getMethodsByName("fromString").forEach(method -> method.setJavadocComment(new Javadoc(
                    JavadocDescription.parseText("Creates or finds a {@link SettingFields} from its string representation."))
                        .addBlockTag("param", "name", "a name to look for.")
                        .addBlockTag("return", "the corresponding {@link SettingFields}")));

                // Add class-level javadoc
                clazz.setJavadocComment(new Javadoc(
                    JavadocDescription.parseText("Fields in {@link ConfigurationSetting} that can be returned from GET queries."))
                        .addBlockTag("see", "SettingSelector"));

                // Add toStringMapper static new method to SettingFields
                clazz.addMethod("toStringMapper", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
                    .setType("String")
                    .addParameter("SettingFields", "field")
                    .setBody(StaticJavaParser.parseBlock("{ return field.toString().toLowerCase(Locale.US); }"))
                    .addMarkerAnnotation(Deprecated.class)
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Converts the SettingFields to a string that is usable for HTTP requests and logging."))
                        .addBlockTag("param", "field", "SettingFields to map.")
                        .addBlockTag("return", "SettingFields as a lowercase string in the US locale.")
                        .addBlockTag("deprecated", "This method is no longer needed. SettingFields is using lower case enum value for the HTTP requests."));
            });
        });
    }

    private static Javadoc replaceJavadocDescription(Javadoc javadoc, String newDescription) {
        Javadoc newJavadoc = new Javadoc(JavadocDescription.parseText(newDescription));
        newJavadoc.getBlockTags().addAll(javadoc.getBlockTags());

        return newJavadoc;
    }
}
