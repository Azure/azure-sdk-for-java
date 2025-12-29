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

            // Customize EnhancedModeOptions constructor to auto-set enabled to true
            logger.info("Customizing EnhancedModeOptions constructor to auto-set enabled to true");
            customizeEnhancedModeOptions(models);

            // Customize AudioFileDetails.getFilename() to auto-generate filename from contentType if not set
            logger.info("Customizing AudioFileDetails.getFilename() to auto-generate filename");
            customizeAudioFileDetailsGetFilename(models);

            // Add AudioFileDetails field and constructors to TranscriptionOptions, make setAudioUrl private, remove no-arg constructor
            logger
                .info("Customizing TranscriptionOptions to add AudioFileDetails support and remove no-arg constructor");
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
     * Customize EnhancedModeOptions to hide the enabled property from the public API.
     * The enabled property is automatically set to true in the constructor.
     * Both isEnabled() getter and setEnabled() setter are removed from the public API.
     *
     * @param packageCustomization the package customization
     */
    private void customizeEnhancedModeOptions(PackageCustomization packageCustomization) {
        packageCustomization.getClass("EnhancedModeOptions").customizeAst(ast -> {
            ast.getClassByName("EnhancedModeOptions").ifPresent(clazz -> {
                // Remove the @Generated no-arg constructor and replace with one that sets enabled = true
                clazz.getConstructors()
                    .stream()
                    .filter(c -> c.getParameters().isEmpty())
                    .findFirst()
                    .ifPresent(constructor -> {
                        // Remove @Generated annotation to prevent overwriting
                        constructor.getAnnotationByName("Generated").ifPresent(com.github.javaparser.ast.Node::remove);
                        // Set the constructor body to initialize enabled = true
                        constructor.setBody(parseBlock("{ this.enabled = true; }"));
                        // Add JavaDoc
                        constructor.setJavadocComment(
                            new Javadoc(parseText(
                                "Creates an instance of EnhancedModeOptions class with enhanced mode automatically enabled.")));
                    });
                
                // Remove isEnabled() getter to hide enabled from public API
                clazz.getMethodsByName("isEnabled").forEach(method -> method.remove());
                
                // Remove ALL setEnabled() methods to hide enabled from public API
                clazz.getMethodsByName("setEnabled").forEach(method -> method.remove());
            });
        });
    }

    /**
     * Customize AudioFileDetails.getFilename() to auto-generate a filename from contentType if not explicitly set.
     * This allows developers to omit setFilename() and have the SDK automatically provide a sensible default.
     *
     * @param packageCustomization the package customization
     */
    private void customizeAudioFileDetailsGetFilename(PackageCustomization packageCustomization) {
        packageCustomization.getClass("AudioFileDetails").customizeAst(ast -> {
            ast.getClassByName("AudioFileDetails").ifPresent(clazz -> {
                clazz.getMethodsByName("getFilename").forEach(method -> {
                    method.setBody(parseBlock(
                        "{ if (this.filename != null && !this.filename.isEmpty()) { return this.filename; } "
                        + "if (\"audio/wav\".equalsIgnoreCase(this.contentType)) { return \"audio.wav\"; } "
                        + "if (\"audio/mpeg\".equalsIgnoreCase(this.contentType) || \"audio/mp3\".equalsIgnoreCase(this.contentType)) { return \"audio.mp3\"; } "
                        + "if (\"audio/ogg\".equalsIgnoreCase(this.contentType)) { return \"audio.ogg\"; } "
                        + "if (\"audio/flac\".equalsIgnoreCase(this.contentType)) { return \"audio.flac\"; } "
                        + "if (\"audio/webm\".equalsIgnoreCase(this.contentType)) { return \"audio.webm\"; } "
                        + "if (\"audio/opus\".equalsIgnoreCase(this.contentType)) { return \"audio.opus\"; } "
                        + "return \"audio\"; }"));
                    method.setJavadocComment(
                        new Javadoc(parseText("Get the filename property: The filename of the file. "
                            + "If not explicitly set, a filename will be auto-generated from the contentType."))
                            .addBlockTag("return", "the filename value, or an auto-generated filename if not set."));
                });
            });
        });
    }

    /**
     * Customize TranscriptionOptions to:
     * 1. Add AudioFileDetails field (final)
     * 2. Remove default no-arg constructor
     * 3. Add constructor with String audioUrl parameter
     * 4. Add constructor with AudioFileDetails parameter
     * 5. Add getFileDetails() method to access AudioFileDetails
     * 6. Make setAudioUrl() private instead of public
     * 7. Fix fromJson to use one of the parameterized constructors
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

                // Fix fromJson method to use parameterized constructor instead of no-arg
                clazz.getMethodsByName("fromJson").forEach(method -> {
                    // Replace the entire method body to use the String constructor
                    method.setBody(parseBlock("{ return jsonReader.readObject(reader -> { "
                        + "TranscriptionOptions deserializedTranscriptionOptions = new TranscriptionOptions((String) null); "
                        + "while (reader.nextToken() != JsonToken.END_OBJECT) { "
                        + "String fieldName = reader.getFieldName(); " + "reader.nextToken(); "
                        + "if (\"audioUrl\".equals(fieldName)) { "
                        + "deserializedTranscriptionOptions.audioUrl = reader.getString(); "
                        + "} else if (\"locales\".equals(fieldName)) { "
                        + "List<String> locales = reader.readArray(reader1 -> reader1.getString()); "
                        + "deserializedTranscriptionOptions.locales = locales; "
                        + "} else if (\"localeModelMapping\".equals(fieldName)) { "
                        + "Map<String, String> localeModelMapping = reader.readMap(reader1 -> reader1.getString()); "
                        + "deserializedTranscriptionOptions.localeModelMapping = localeModelMapping; "
                        + "} else if (\"profanityFilterMode\".equals(fieldName)) { "
                        + "deserializedTranscriptionOptions.profanityFilterMode = ProfanityFilterMode.fromString(reader.getString()); "
                        + "} else if (\"diarization\".equals(fieldName)) { "
                        + "deserializedTranscriptionOptions.diarizationOptions = TranscriptionDiarizationOptions.fromJson(reader); "
                        + "} else if (\"channels\".equals(fieldName)) { "
                        + "List<Integer> activeChannels = reader.readArray(reader1 -> reader1.getInt()); "
                        + "deserializedTranscriptionOptions.activeChannels = activeChannels; "
                        + "} else if (\"enhancedMode\".equals(fieldName)) { "
                        + "deserializedTranscriptionOptions.enhancedModeOptions = EnhancedModeOptions.fromJson(reader); "
                        + "} else if (\"phraseList\".equals(fieldName)) { "
                        + "deserializedTranscriptionOptions.phraseListOptions = PhraseListOptions.fromJson(reader); "
                        + "} else { " + "reader.skipChildren(); " + "} " + "} "
                        + "return deserializedTranscriptionOptions; " + "}); }"));
                });

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

                // Add getFileDetails() method
                com.github.javaparser.ast.body.MethodDeclaration getFileDetailsMethod
                    = clazz.addMethod("getFileDetails", Modifier.Keyword.PUBLIC);
                getFileDetailsMethod.setType("AudioFileDetails");
                getFileDetailsMethod.setBody(parseBlock("{ return this.audioFileDetails; }"));
                getFileDetailsMethod.setJavadocComment(new Javadoc(
                    parseText("Get the audioFileDetails property: The audio file details for transcription."))
                        .addBlockTag("return", "the audioFileDetails value."));

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

                // Add public transcribe method that returns TranscriptionResult
                com.github.javaparser.ast.body.MethodDeclaration transcribeMethod
                    = clazz.addMethod("transcribe", Modifier.Keyword.PUBLIC)
                        .addParameter("TranscriptionOptions", "options")
                        .setType("TranscriptionResult");
                transcribeMethod.setJavadocComment("/**\n"
                    + " * Transcribes the provided audio stream with the specified options.\n" + " *\n"
                    + " * @param options the transcription options including audio file details or audio URL\n"
                    + " * @throws IllegalArgumentException thrown if parameters fail the validation.\n"
                    + " * @throws HttpResponseException thrown if the request is rejected by server.\n"
                    + " * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.\n"
                    + " * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.\n"
                    + " * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.\n"
                    + " * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.\n"
                    + " * @return the result of the transcribe operation.\n" + " */");
                com.github.javaparser.ast.expr.NormalAnnotationExpr transcribeServiceMethodAnnotation
                    = new com.github.javaparser.ast.expr.NormalAnnotationExpr();
                transcribeServiceMethodAnnotation.setName("ServiceMethod");
                transcribeServiceMethodAnnotation.addPair("returns", "ReturnType.SINGLE");
                transcribeMethod.addAnnotation(transcribeServiceMethodAnnotation);
                transcribeMethod
                    .setBody(parseBlock("{ TranscriptionContent requestContent = new TranscriptionContent(options); "
                        + "if (options.getFileDetails() != null) { requestContent.setAudio(options.getFileDetails()); } "
                        + "return transcribe(requestContent); }"));

                // Add public transcribeWithResponse method that returns Response<TranscriptionResult>
                com.github.javaparser.ast.body.MethodDeclaration transcribeWithResponseMethod
                    = clazz.addMethod("transcribeWithResponse", Modifier.Keyword.PUBLIC)
                        .addParameter("TranscriptionOptions", "options")
                        .setType("Response<TranscriptionResult>");
                transcribeWithResponseMethod.setJavadocComment("/**\n"
                    + " * Transcribes the provided audio stream with the specified options.\n" + " *\n"
                    + " * @param options the transcription options including audio file details or audio URL\n"
                    + " * @throws IllegalArgumentException thrown if parameters fail the validation.\n"
                    + " * @throws HttpResponseException thrown if the request is rejected by server.\n"
                    + " * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.\n"
                    + " * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.\n"
                    + " * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.\n"
                    + " * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.\n"
                    + " * @return the response containing the result of the transcribe operation.\n" + " */");
                com.github.javaparser.ast.expr.NormalAnnotationExpr serviceMethodAnnotation
                    = new com.github.javaparser.ast.expr.NormalAnnotationExpr();
                serviceMethodAnnotation.setName("ServiceMethod");
                serviceMethodAnnotation.addPair("returns", "ReturnType.SINGLE");
                transcribeWithResponseMethod.addAnnotation(serviceMethodAnnotation);
                transcribeWithResponseMethod
                    .setBody(parseBlock("{ TranscriptionContent requestContent = new TranscriptionContent(options); "
                        + "if (options.getFileDetails() != null) { requestContent.setAudio(options.getFileDetails()); } "
                        + "RequestOptions requestOptions = new RequestOptions(); "
                        + "Response<BinaryData> response = transcribeWithResponse("
                        + "new MultipartFormDataHelper(requestOptions).serializeJsonField(\"definition\", requestContent.getOptions())"
                        + ".serializeFileField(\"audio\", requestContent.getAudio() == null ? null : requestContent.getAudio().getContent(), "
                        + "requestContent.getAudio() == null ? null : requestContent.getAudio().getContentType(), "
                        + "requestContent.getAudio() == null ? null : requestContent.getAudio().getFilename())"
                        + ".end().getRequestBody(), requestOptions); "
                        + "return new SimpleResponse<>(response, response.getValue().toObject(TranscriptionResult.class)); }"));
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

                // Add public transcribe method that returns Mono<TranscriptionResult>
                com.github.javaparser.ast.body.MethodDeclaration transcribeMethod
                    = clazz.addMethod("transcribe", Modifier.Keyword.PUBLIC)
                        .addParameter("TranscriptionOptions", "options")
                        .setType("Mono<TranscriptionResult>");
                transcribeMethod.setJavadocComment("/**\n"
                    + " * Transcribes the provided audio stream with the specified options.\n" + " *\n"
                    + " * @param options the transcription options including audio file details or audio URL\n"
                    + " * @throws IllegalArgumentException thrown if parameters fail the validation.\n"
                    + " * @throws HttpResponseException thrown if the request is rejected by server.\n"
                    + " * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.\n"
                    + " * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.\n"
                    + " * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.\n"
                    + " * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.\n"
                    + " * @return the result of the transcribe operation on successful completion of {@link Mono}.\n"
                    + " */");
                com.github.javaparser.ast.expr.NormalAnnotationExpr transcribeServiceMethodAnnotation
                    = new com.github.javaparser.ast.expr.NormalAnnotationExpr();
                transcribeServiceMethodAnnotation.setName("ServiceMethod");
                transcribeServiceMethodAnnotation.addPair("returns", "ReturnType.SINGLE");
                transcribeMethod.addAnnotation(transcribeServiceMethodAnnotation);
                transcribeMethod
                    .setBody(parseBlock("{ TranscriptionContent requestContent = new TranscriptionContent(options); "
                        + "if (options.getFileDetails() != null) { requestContent.setAudio(options.getFileDetails()); } "
                        + "return transcribe(requestContent); }"));

                // Add public transcribeWithResponse method that returns Mono<Response<TranscriptionResult>>
                com.github.javaparser.ast.body.MethodDeclaration transcribeWithResponseMethod
                    = clazz.addMethod("transcribeWithResponse", Modifier.Keyword.PUBLIC)
                        .addParameter("TranscriptionOptions", "options")
                        .setType("Mono<Response<TranscriptionResult>>");
                transcribeWithResponseMethod.setJavadocComment("/**\n"
                    + " * Transcribes the provided audio stream with the specified options.\n" + " *\n"
                    + " * @param options the transcription options including audio file details or audio URL\n"
                    + " * @throws IllegalArgumentException thrown if parameters fail the validation.\n"
                    + " * @throws HttpResponseException thrown if the request is rejected by server.\n"
                    + " * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.\n"
                    + " * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.\n"
                    + " * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.\n"
                    + " * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.\n"
                    + " * @return the response containing the result of the transcribe operation on successful completion of {@link Mono}.\n"
                    + " */");
                com.github.javaparser.ast.expr.NormalAnnotationExpr serviceMethodAnnotation
                    = new com.github.javaparser.ast.expr.NormalAnnotationExpr();
                serviceMethodAnnotation.setName("ServiceMethod");
                serviceMethodAnnotation.addPair("returns", "ReturnType.SINGLE");
                transcribeWithResponseMethod.addAnnotation(serviceMethodAnnotation);
                transcribeWithResponseMethod
                    .setBody(parseBlock("{ TranscriptionContent requestContent = new TranscriptionContent(options); "
                        + "if (options.getFileDetails() != null) { requestContent.setAudio(options.getFileDetails()); } "
                        + "RequestOptions requestOptions = new RequestOptions(); " + "return transcribeWithResponse("
                        + "new MultipartFormDataHelper(requestOptions).serializeJsonField(\"definition\", requestContent.getOptions())"
                        + ".serializeFileField(\"audio\", requestContent.getAudio() == null ? null : requestContent.getAudio().getContent(), "
                        + "requestContent.getAudio() == null ? null : requestContent.getAudio().getContentType(), "
                        + "requestContent.getAudio() == null ? null : requestContent.getAudio().getFilename())"
                        + ".end().getRequestBody(), requestOptions)"
                        + ".map(response -> new SimpleResponse<>(response, response.getValue().toObject(TranscriptionResult.class))); }"));
            });
        });
    }
}
