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

public class APIListing implements Parent, JsonSerializable<APIListing> {
    private static final String parserVersion = "29";

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

    public APIListing() {
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

    public void addDiagnostic(Diagnostic diagnostic) {
        this.diagnostics.add(diagnostic);
    }

    public List<Diagnostic> getDiagnostics() {
        return Collections.unmodifiableList(diagnostics);
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(final Language language) {
        this.language = language;
    }

    public LanguageVariant getLanguageVariant() {
        return languageVariant;
    }

    public void setLanguageVariant(final LanguageVariant variant) {
        this.languageVariant = variant;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(final String packageVersion) {
        this.packageVersion = packageVersion;
    }

    /**
     * Returns a map of type name to unique identifier, used for navigation.
     */
    public Map<String, String> getKnownTypes() {
        return knownTypes;
    }

    public void addPackageTypeMapping(String packageName, String typeName) {
//        packageNamesToTypesMap.computeIfAbsent(packageName, name -> new ArrayList<>()).add(typeName);
        typeToPackageNameMap.put(typeName, packageName);
    }

    public Map<String, String> getTypeToPackageNameMap() {
        return typeToPackageNameMap;
    }

    public void setMavenPom(Pom mavenPom) {
        this.mavenPom = mavenPom;
    }

    public Pom getMavenPom() {
        return mavenPom;
    }

    public void setApiViewProperties(ApiViewProperties properties) {
        this.apiViewProperties = properties;
    }

    public ApiViewProperties getApiViewProperties() {
        return apiViewProperties;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("$schema", APIVIEW_JSON_SCHEMA)
            // Version?
            .writeStringField(JSON_NAME_PARSER_VERSION, parserVersion)
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
     * Writes the {@link APIListing} to the {@code outputFile} and returns the raw JSON.
     *
     * @param outputFile The file to write.
     * @param gzipOutput Whether the output needs to be GZIP'd.
     * @return The raw JSON bytes for other final checks.
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
                new GZIPOutputStream(Files.newOutputStream(outputFile.toPath()))
                    .write(rawJsonBytes);
            }

            return rawJsonBytes;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
