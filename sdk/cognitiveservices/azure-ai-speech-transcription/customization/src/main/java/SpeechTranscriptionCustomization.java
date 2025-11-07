// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.javadoc.Javadoc;
import org.slf4j.Logger;

import java.time.Duration;

import static com.github.javaparser.StaticJavaParser.parseBlock;
import static com.github.javaparser.javadoc.description.JavadocDescription.parseText;

/**
 * Code customization after code generation for Speech Transcription SDK.
 */
public class SpeechTranscriptionCustomization extends Customization {

    /**
     * Creates an instance of SpeechTranscriptionCustomization.
     */
    public SpeechTranscriptionCustomization() {
    }

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing Speech Transcription SDK - changing Duration getters");
        PackageCustomization models = customization.getPackage("com.azure.ai.speech.transcription.models");

        // Customize TranscriptionResult.getDuration() to return Duration instead of int
        logger.info("Customizing TranscriptionResult.getDuration()");
        customizeDurationGetter(models, "TranscriptionResult");

        // Customize TranscribedPhrase.getDuration() to return Duration instead of int
        logger.info("Customizing TranscribedPhrase.getDuration()");
        customizeDurationGetter(models, "TranscribedPhrase");

        // Customize TranscribedWord.getDuration() to return Duration instead of int
        logger.info("Customizing TranscribedWord.getDuration()");
        customizeDurationGetter(models, "TranscribedWord");

        // Customize TranscriptionDiarizationOptions to properly serialize enabled field
        logger.info("Customizing TranscriptionDiarizationOptions.toJson()");
        customizeDiarizationOptionsToJson(models);
    }

    /**
     * Customize the getDuration() method to return Duration type instead of int.
     * The backing field remains as int (milliseconds), but the getter converts it to Duration.
     *
     * @param packageCustomization the package customization
     * @param className the name of the class to customize
     */
    private void customizeDurationGetter(PackageCustomization packageCustomization, String className) {
        packageCustomization.getClass(className).customizeAst(ast -> {
            ast.addImport("java.time.Duration");
            ast.getClassByName(className).ifPresent(clazz -> clazz.getMethodsByName("getDuration").forEach(method -> {
                method.setType("Duration")
                    .setBody(parseBlock("{ return Duration.ofMillis(this.duration); }"))
                    .setJavadocComment(
                        new Javadoc(parseText("Get the duration property: The duration in milliseconds."))
                            .addBlockTag("return", "the duration value as Duration."));
            }));
        });
    }

    /**
     * Customize the TranscriptionDiarizationOptions.toJson() method to properly serialize the enabled field.
     * When maxSpeakers is set, enabled should be automatically set to true and serialized.
     *
     * @param packageCustomization the package customization
     */
    private void customizeDiarizationOptionsToJson(PackageCustomization packageCustomization) {
        packageCustomization.getClass("TranscriptionDiarizationOptions").customizeAst(ast -> {
            ast.getClassByName("TranscriptionDiarizationOptions")
                .ifPresent(clazz -> clazz.getMethodsByName("toJson").forEach(method -> {
                    method.setBody(parseBlock(
                        "{ jsonWriter.writeStartObject(); if (this.maxSpeakers != null) { jsonWriter.writeBooleanField(\"enabled\", true); jsonWriter.writeNumberField(\"maxSpeakers\", this.maxSpeakers); } return jsonWriter.writeEndObject(); }"));
                }));
        });
    }
}
