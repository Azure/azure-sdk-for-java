// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.model;

import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import com.azure.tools.apiview.processor.model.maven.Pom;
import com.azure.tools.apiview.processor.model.traits.Parent;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import static com.azure.tools.apiview.processor.analysers.models.Constants.APIVIEW_JSON_SCHEMA;
import static com.azure.tools.apiview.processor.analysers.models.Constants.JSON_NAME_DIAGNOSTICS;
import static com.azure.tools.apiview.processor.analysers.models.Constants.JSON_NAME_LANGUAGE;
import static com.azure.tools.apiview.processor.analysers.models.Constants.JSON_NAME_LANGUAGE_VARIANT;
import static com.azure.tools.apiview.processor.analysers.models.Constants.JSON_NAME_PACKAGE_NAME;
import static com.azure.tools.apiview.processor.analysers.models.Constants.JSON_NAME_PACKAGE_VERSION;
import static com.azure.tools.apiview.processor.analysers.models.Constants.JSON_NAME_PARSER_VERSION;
import static com.azure.tools.apiview.processor.analysers.models.Constants.JSON_NAME_REVIEW_LINES;

/**
 * API review lines, diagnostics, and package metadata.
 */
public class ApiListing implements Parent, JsonSerializable<ApiListing> {
    private static final String PARSER_VERSION = "29";

    private Language language;
    private LanguageVariant languageVariant;
    private String packageName;
    private String packageVersion;

    private final List<ReviewLine> reviewLines;
    private final List<Diagnostic> diagnostics;
    private final Map<String, String> knownTypes;

    private final Map<String, String> typeToPackageNameMap;
    private Pom mavenPom;
    private ApiViewProperties apiViewProperties;

    /**
     * Creates an API listing.
     */
    public ApiListing() {
        this.diagnostics = new ArrayList<>();
        this.knownTypes = new HashMap<>();
        this.typeToPackageNameMap = new HashMap<>();
        this.reviewLines = new ArrayList<>();
        this.apiViewProperties = new ApiViewProperties();
    }

    @Override
    public ReviewLine addChildLine(ReviewLine reviewLine) {
        this.reviewLines.add(reviewLine);
        return reviewLine;
    }

    @Override
    public ReviewLine addChildLine(final String lineId) {
        return addChildLine(new ReviewLine(this, lineId));
    }

    @Override
    public ReviewLine addChildLine() {
        return addChildLine(new ReviewLine(this));
    }

    @Override
    public List<ReviewLine> getChildren() {
        return reviewLines;
    }

    //    public List<TreeNode> getApiForest() {
    //        return Collections.unmodifiableList(apiForest);
    //    }

    /**
     * Adds diagnostic.
     *
     * @param diagnostic the diagnostic
     */
    public void addDiagnostic(Diagnostic diagnostic) {
        this.diagnostics.add(diagnostic);
    }

    /**
     * Returns the diagnostics.
     *
     * @return the diagnostics
     */
    public List<Diagnostic> getDiagnostics() {
        return Collections.unmodifiableList(diagnostics);
    }

    /**
     * Returns the language.
     *
     * @return the language
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * Sets the language.
     *
     * @param language the language
     */
    public void setLanguage(final Language language) {
        this.language = language;
    }

    /**
     * Returns the language variant.
     *
     * @return the language variant
     */
    public LanguageVariant getLanguageVariant() {
        return languageVariant;
    }

    /**
     * Sets the language variant.
     *
     * @param variant the variant
     */
    public void setLanguageVariant(final LanguageVariant variant) {
        this.languageVariant = variant;
    }

    /**
     * Returns the package name.
     *
     * @return the package name
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Sets the package name.
     *
     * @param packageName the package name
     */
    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    /**
     * Returns the package version.
     *
     * @return the package version
     */
    public String getPackageVersion() {
        return packageVersion;
    }

    /**
     * Sets the package version.
     *
     * @param packageVersion the package version
     */
    public void setPackageVersion(final String packageVersion) {
        this.packageVersion = packageVersion;
    }

    /**
     * Returns a map of type name to unique identifier, used for navigation.
     *
     * @return the known types
     */
    public Map<String, String> getKnownTypes() {
        return knownTypes;
    }

    /**
     * Adds package type mapping.
     *
     * @param packageName the package name
     * @param typeName the type name
     */
    public void addPackageTypeMapping(String packageName, String typeName) {
        //        packageNamesToTypesMap.computeIfAbsent(packageName, name -> new ArrayList<>()).add(typeName);
        typeToPackageNameMap.put(typeName, packageName);
    }

    /**
     * Returns the type to package name map.
     *
     * @return the type to package name map
     */
    public Map<String, String> getTypeToPackageNameMap() {
        return typeToPackageNameMap;
    }

    /**
     * Sets the Maven POM.
     *
     * @param mavenPom the Maven POM
     */
    public void setMavenPom(Pom mavenPom) {
        this.mavenPom = mavenPom;
    }

    /**
     * Returns the Maven POM.
     *
     * @return the Maven POM
     */
    public Pom getMavenPom() {
        return mavenPom;
    }

    /**
     * Sets the APIView properties.
     *
     * @param properties the properties
     */
    public void setApiViewProperties(ApiViewProperties properties) {
        this.apiViewProperties = properties;
    }

    /**
     * Returns the APIView properties.
     *
     * @return the APIView properties
     */
    public ApiViewProperties getApiViewProperties() {
        return apiViewProperties;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("$schema", APIVIEW_JSON_SCHEMA)
            // Version?
            .writeStringField(JSON_NAME_PARSER_VERSION, PARSER_VERSION)
            .writeStringField(JSON_NAME_LANGUAGE, language.toString())
            .writeStringField(JSON_NAME_LANGUAGE_VARIANT, languageVariant.toString())
            .writeStringField(JSON_NAME_PACKAGE_NAME, packageName)
            .writeStringField(JSON_NAME_PACKAGE_VERSION, packageVersion)
            // ServiceName?
            // PackageDisplayName?
            .writeArrayField(JSON_NAME_REVIEW_LINES, reviewLines, JsonWriter::writeJson)
            .writeArrayField(JSON_NAME_DIAGNOSTICS, diagnostics, JsonWriter::writeJson)
            .writeEndObject();
    }

    /**
     * Writes the {@link ApiListing} to the {@code outputFile} and returns the raw JSON.
     *
     * @param outputFile The file to write.
     * @param gzipOutput Whether the output needs to be GZIP'd.
     * @return The raw JSON bytes for other final checks.
     * @throws UncheckedIOException if the operation cannot be completed
     */
    public byte[] toFile(File outputFile, boolean gzipOutput) {
        try {
            // Write out to the filesystem, make the file if it doesn't exist
            if (!outputFile.exists()) {
                if (!outputFile.createNewFile()) {
                    System.out.printf("Failed to create output file %s%n", outputFile);
                }
            }

            byte[] rawJsonBytes = toJsonBytes();

            if (!gzipOutput) {
                Files.write(outputFile.toPath(), rawJsonBytes);
            } else {
                try (GZIPOutputStream output = new GZIPOutputStream(Files.newOutputStream(outputFile.toPath()))) {
                    output.write(rawJsonBytes);
                }
            }

            return rawJsonBytes;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
