// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.ClassCustomization;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

import java.time.Duration;

import static com.github.javaparser.StaticJavaParser.parseBlock;
import static com.github.javaparser.StaticJavaParser.parseStatement;
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

        // Add AudioFileDetails field and constructors to TranscriptionOptions, make setAudioUrl private
        logger.info("Customizing TranscriptionOptions to add AudioFileDetails support and String audioUrl constructor");
        customizeTranscriptionOptions(models);
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

    /**
     * Customize TranscriptionOptions to:
     * 1. Add AudioFileDetails field (final)
     * 2. Add initialization to default constructor
     * 3. Add constructor with String audioUrl parameter
     * 4. Add constructor with AudioFileDetails parameter
     * 5. Make setAudioUrl() private instead of public
     *
     * @param packageCustomization the package customization
     */
    private void customizeTranscriptionOptions(PackageCustomization packageCustomization) {
        packageCustomization.getClass("TranscriptionOptions").customizeAst(ast -> {
            ast.getClassByName("TranscriptionOptions").ifPresent(clazz -> {
                // Add the AudioFileDetails field as final
                clazz.addFieldWithInitializer(
                    "AudioFileDetails",
                    "audioFileDetails",
                    null,
                    com.github.javaparser.ast.Modifier.Keyword.PRIVATE,
                    com.github.javaparser.ast.Modifier.Keyword.FINAL
                );

                // Update default constructor to initialize audioFileDetails
                clazz.getConstructors().stream()
                    .filter(c -> c.getParameters().isEmpty())
                    .findFirst()
                    .ifPresent(defaultConstructor -> {
                        BlockStmt body = defaultConstructor.getBody();
                        body.addStatement(0, parseStatement("this.audioFileDetails = null;"));
                    });

                // Add constructor with String audioUrl parameter
                ConstructorDeclaration audioUrlConstructor = clazz.addConstructor(Modifier.Keyword.PUBLIC);
                audioUrlConstructor.addParameter("String", "audioUrl");
                audioUrlConstructor.setBody(parseBlock("{ this.audioUrl = audioUrl; this.audioFileDetails = null; }"));
                audioUrlConstructor.setJavadocComment(new Javadoc(parseText(
                    "Creates an instance of TranscriptionOptions class with audio URL."))
                    .addBlockTag("param", "audioUrl the URL of the audio to be transcribed"));

                // Add constructor with AudioFileDetails parameter
                ConstructorDeclaration fileDetailsConstructor = clazz.addConstructor(Modifier.Keyword.PUBLIC);
                fileDetailsConstructor.addParameter("AudioFileDetails", "fileDetails");
                fileDetailsConstructor.setBody(parseBlock("{ this.audioFileDetails = fileDetails; }"));
                fileDetailsConstructor.setJavadocComment(new Javadoc(parseText(
                    "Creates an instance of TranscriptionOptions class with audio file details."))
                    .addBlockTag("param", "fileDetails the audio file details"));

                // Make setAudioUrl() private
                clazz.getMethodsByName("setAudioUrl").forEach(method -> {
                    method.getModifiers().clear();
                    method.addModifier(Modifier.Keyword.PRIVATE);
                });
            });
        });
    }    /**
     * Find the position to insert constructors (after existing constructors).
     *
     * @param clazz the class declaration
     * @return the position to insert constructors
     */
    private int getConstructorPosition(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration clazz) {
        int constructorIndex = 0;
        NodeList<BodyDeclaration<?>> members = clazz.getMembers();
        for (int i = 0; i < members.size(); i++) {
            BodyDeclaration<?> member = members.get(i);
            if (member.isFieldDeclaration()) {
                constructorIndex = i + 1;
            } else if (member.isConstructorDeclaration()) {
                constructorIndex = i + 1;
            } else if (member.isMethodDeclaration()) {
                return constructorIndex;
            }
        }
        return constructorIndex;
    }
}
