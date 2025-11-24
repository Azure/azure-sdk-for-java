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
        logger.info("Customizing Speech Transcription SDK");

        // Check if models package exists before attempting customization
        try {
            PackageCustomization models = customization.getPackage("com.azure.ai.speech.transcription.models");

            logger.info("Models package found - applying model customizations");

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
            logger.info(
                "Customizing TranscriptionOptions to add AudioFileDetails support and String audioUrl constructor");
            customizeTranscriptionOptions(models);
        } catch (IllegalArgumentException e) {
            logger.warn("Models package not found or empty - skipping model customizations: " + e.getMessage());
        }

        // Make transcribe(TranscriptionContent) package-private in clients
        logger.info("Customizing TranscriptionClient to make transcribe(TranscriptionContent) package-private");
        customizeTranscriptionClient(customization.getPackage("com.azure.ai.speech.transcription"));

        logger.info("Customizing TranscriptionAsyncClient to make transcribe(TranscriptionContent) package-private");
        customizeTranscriptionAsyncClient(customization.getPackage("com.azure.ai.speech.transcription"));
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
     * 2. Remove default no-arg constructor
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
                clazz.addFieldWithInitializer("AudioFileDetails", "audioFileDetails", null,
                    com.github.javaparser.ast.Modifier.Keyword.PRIVATE,
                    com.github.javaparser.ast.Modifier.Keyword.FINAL);

                // Remove default no-arg constructor
                clazz.getConstructors()
                    .stream()
                    .filter(c -> c.getParameters().isEmpty())
                    .findFirst()
                    .ifPresent(com.github.javaparser.ast.Node::remove);

                // Add constructor with String audioUrl parameter
                ConstructorDeclaration audioUrlConstructor = clazz.addConstructor(Modifier.Keyword.PUBLIC);
                audioUrlConstructor.addParameter("String", "audioUrl");
                audioUrlConstructor.setBody(parseBlock("{ this.audioUrl = audioUrl; this.audioFileDetails = null; }"));
                audioUrlConstructor.setJavadocComment(
                    new Javadoc(parseText("Creates an instance of TranscriptionOptions class with audio URL."))
                        .addBlockTag("param", "audioUrl the URL of the audio to be transcribed"));

                // Add constructor with AudioFileDetails parameter
                ConstructorDeclaration fileDetailsConstructor = clazz.addConstructor(Modifier.Keyword.PUBLIC);
                fileDetailsConstructor.addParameter("AudioFileDetails", "fileDetails");
                fileDetailsConstructor.setBody(parseBlock("{ this.audioFileDetails = fileDetails; }"));
                fileDetailsConstructor.setJavadocComment(
                    new Javadoc(parseText("Creates an instance of TranscriptionOptions class with audio file details."))
                        .addBlockTag("param", "fileDetails the audio file details"));

                // Make setAudioUrl() private
                clazz.getMethodsByName("setAudioUrl").forEach(method -> {
                    method.getModifiers().clear();
                    method.addModifier(Modifier.Keyword.PRIVATE);
                });
            });
        });
    }

    /**
     * Customize TranscriptionClient to add public transcribe(TranscriptionOptions) method
     * that hides TranscriptionContent construction and add transcribeWithResponse method.
     *
     * @param packageCustomization the package customization
     */
    private void customizeTranscriptionClient(PackageCustomization packageCustomization) {
        ClassCustomization classCustomization = packageCustomization.getClass("TranscriptionClient");
        classCustomization.customizeAst(ast -> {
            ast.getClassByName("TranscriptionClient").ifPresent(clazz -> {
                // Make the generated transcribe(TranscriptionContent) package-private (internal)
                // Only modify methods that have @Generated annotation to avoid affecting manual customizations
                clazz.getMethodsByName("transcribe").forEach(method -> {
                    if (method.getParameters().size() == 1
                        && "TranscriptionContent".equals(method.getParameter(0).getType().asString())
                        && method.getAnnotationByName("Generated").isPresent()) {
                        // Remove all modifiers (including final), making it package-private
                        method.getModifiers().clear();
                        // Remove @Generated annotation to prevent overwriting
                        method.getAnnotationByName("Generated").ifPresent(com.github.javaparser.ast.Node::remove);
                    }
                });

                // Add public transcribeWithResponse method that returns Response<TranscriptionResult>
                com.github.javaparser.ast.body.MethodDeclaration transcribeWithResponseMethod = clazz.addMethod("transcribeWithResponse", Modifier.Keyword.PUBLIC)
                    .addParameter("TranscriptionOptions", "options")
                    .setType("Response<TranscriptionResult>");
                com.github.javaparser.ast.expr.NormalAnnotationExpr serviceMethodAnnotation = new com.github.javaparser.ast.expr.NormalAnnotationExpr();
                serviceMethodAnnotation.setName("ServiceMethod");
                serviceMethodAnnotation.addPair("returns", "ReturnType.SINGLE");
                transcribeWithResponseMethod.addAnnotation(serviceMethodAnnotation);
                transcribeWithResponseMethod.setBody(parseBlock(
                        "{ TranscriptionContent requestContent = new TranscriptionContent(); "
                        + "requestContent.setOptions(options); "
                        + "RequestOptions requestOptions = new RequestOptions(); "
                        + "Response<BinaryData> response = transcribeWithResponse("
                        + "new MultipartFormDataHelper(requestOptions).serializeJsonField(\"definition\", requestContent.getOptions())"
                        + ".serializeFileField(\"audio\", requestContent.getAudio() == null ? null : requestContent.getAudio().getContent(), "
                        + "requestContent.getAudio() == null ? null : requestContent.getAudio().getContentType(), "
                        + "requestContent.getAudio() == null ? null : requestContent.getAudio().getFilename())"
                        + ".end().getRequestBody(), requestOptions); "
                        + "return new Response<TranscriptionResult>() { "
                        + "public int getStatusCode() { return response.getStatusCode(); } "
                        + "public com.azure.core.http.HttpHeaders getHeaders() { return response.getHeaders(); } "
                        + "public com.azure.core.http.HttpRequest getRequest() { return response.getRequest(); } "
                        + "public TranscriptionResult getValue() { return response.getValue().toObject(TranscriptionResult.class); } "
                        + "}; }"));
                transcribeWithResponseMethod.setJavadocComment(new Javadoc(parseText(
                        "Transcribes the provided audio stream with the specified options."))
                        .addBlockTag("param", "options the transcription options including audio file details or audio URL")
                        .addBlockTag("throws", "IllegalArgumentException thrown if parameters fail the validation.")
                        .addBlockTag("throws", "HttpResponseException thrown if the request is rejected by server.")
                        .addBlockTag("throws", "ClientAuthenticationException thrown if the request is rejected by server on status code 401.")
                        .addBlockTag("throws", "ResourceNotFoundException thrown if the request is rejected by server on status code 404.")
                        .addBlockTag("throws", "ResourceModifiedException thrown if the request is rejected by server on status code 409.")
                        .addBlockTag("throws", "RuntimeException all other wrapped checked exceptions if the request fails to be sent.")
                        .addBlockTag("return", "the response containing the result of the transcribe operation."));
            });
        });
    }

    /**
     * Customize TranscriptionAsyncClient to make transcribe(TranscriptionContent) package-private (internal)
     * and add transcribeWithResponse method.
     *
     * @param packageCustomization the package customization
     */
    private void customizeTranscriptionAsyncClient(PackageCustomization packageCustomization) {
        ClassCustomization classCustomization = packageCustomization.getClass("TranscriptionAsyncClient");
        classCustomization.customizeAst(ast -> {
            ast.getClassByName("TranscriptionAsyncClient").ifPresent(clazz -> {
                // Make the generated transcribe(TranscriptionContent) package-private (internal)
                // Only modify methods that have @Generated annotation to avoid affecting manual customizations
                clazz.getMethodsByName("transcribe").forEach(method -> {
                    if (method.getParameters().size() == 1
                        && "TranscriptionContent".equals(method.getParameter(0).getType().asString())
                        && method.getAnnotationByName("Generated").isPresent()) {
                        // Remove all modifiers (including final), making it package-private
                        method.getModifiers().clear();
                        // Remove @Generated annotation to prevent overwriting
                        method.getAnnotationByName("Generated").ifPresent(com.github.javaparser.ast.Node::remove);
                    }
                });

                // Add public transcribeWithResponse method that returns Mono<Response<TranscriptionResult>>
                com.github.javaparser.ast.body.MethodDeclaration transcribeWithResponseMethod = clazz.addMethod("transcribeWithResponse", Modifier.Keyword.PUBLIC)
                    .addParameter("TranscriptionOptions", "options")
                    .setType("Mono<Response<TranscriptionResult>>");
                com.github.javaparser.ast.expr.NormalAnnotationExpr serviceMethodAnnotation = new com.github.javaparser.ast.expr.NormalAnnotationExpr();
                serviceMethodAnnotation.setName("ServiceMethod");
                serviceMethodAnnotation.addPair("returns", "ReturnType.SINGLE");
                transcribeWithResponseMethod.addAnnotation(serviceMethodAnnotation);
                transcribeWithResponseMethod.setBody(parseBlock(
                        "{ TranscriptionContent requestContent = new TranscriptionContent(); "
                        + "requestContent.setOptions(options); "
                        + "RequestOptions requestOptions = new RequestOptions(); "
                        + "return transcribeWithResponse("
                        + "new MultipartFormDataHelper(requestOptions).serializeJsonField(\"definition\", requestContent.getOptions())"
                        + ".serializeFileField(\"audio\", requestContent.getAudio() == null ? null : requestContent.getAudio().getContent(), "
                        + "requestContent.getAudio() == null ? null : requestContent.getAudio().getContentType(), "
                        + "requestContent.getAudio() == null ? null : requestContent.getAudio().getFilename())"
                        + ".end().getRequestBody(), requestOptions)"
                        + ".map(response -> new Response<TranscriptionResult>() { "
                        + "public int getStatusCode() { return response.getStatusCode(); } "
                        + "public com.azure.core.http.HttpHeaders getHeaders() { return response.getHeaders(); } "
                        + "public com.azure.core.http.HttpRequest getRequest() { return response.getRequest(); } "
                        + "public TranscriptionResult getValue() { return response.getValue().toObject(TranscriptionResult.class); } "
                        + "}); }"));
                transcribeWithResponseMethod.setJavadocComment(new Javadoc(parseText(
                        "Transcribes the provided audio stream with the specified options."))
                        .addBlockTag("param", "options the transcription options including audio file details or audio URL")
                        .addBlockTag("throws", "IllegalArgumentException thrown if parameters fail the validation.")
                        .addBlockTag("throws", "HttpResponseException thrown if the request is rejected by server.")
                        .addBlockTag("throws", "ClientAuthenticationException thrown if the request is rejected by server on status code 401.")
                        .addBlockTag("throws", "ResourceNotFoundException thrown if the request is rejected by server on status code 404.")
                        .addBlockTag("throws", "ResourceModifiedException thrown if the request is rejected by server on status code 409.")
                        .addBlockTag("throws", "RuntimeException all other wrapped checked exceptions if the request fails to be sent.")
                        .addBlockTag("return", "the response containing the result of the transcribe operation on successful completion of Mono."));
            });
        });
    }
}
