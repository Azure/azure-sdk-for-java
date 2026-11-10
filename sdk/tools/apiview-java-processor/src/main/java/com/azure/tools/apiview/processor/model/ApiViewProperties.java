package com.azure.tools.apiview.processor.model;

import com.azure.json.*;
import com.azure.tools.apiview.processor.model.maven.Pom;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Sometimes libraries carry additional metadata with them that can make the output from APIView more useful. This
 * class is used to store that metadata, as it is deserialized from the /META-INF/[artifactid]_metadata.json file.
 */
public class ApiViewProperties implements JsonSerializable<ApiViewProperties> {
    private Flavor flavor;

    // This is a map of model names and methods to their TypeSpec definition IDs.
    private Map<String, String> crossLanguageDefinitions = new HashMap<>();

    ApiViewProperties() { }

    /**
     * Attempts to process the {@code [artifactid]_metadata.json} file in the jar file, if it exists.
     *
     * @param fs the {@link FileSystem} representing the jar file
     */
    public static Optional<ApiViewProperties> fromSourcesJarFile(FileSystem fs, Pom pom) {
        // the filename is <artifactid>_metadata.json
        final String artifactId = pom.getArtifactId();
        final String artifactName = (artifactId != null && !artifactId.isEmpty()) ? (artifactId + "_") : "";
        final String filePath = "/META-INF/" + artifactName + "metadata.json";
        final Path apiviewPropertiesPath = fs.getPath(filePath);

        if (!Files.exists(apiviewPropertiesPath)) {
            System.out.println("  No metadata.json file found in jar file - continuing...");
            return Optional.empty();
        }

        try {
            // we eagerly load the metadata.json file into an ApiViewProperties object, so that it can
            // be used throughout the analysis process, as required
            try (JsonReader reader = JsonProviders.createReader(Files.readAllBytes(apiviewPropertiesPath))) {
                final ApiViewProperties properties = ApiViewProperties.fromJson(reader);
                System.out.println("  Found metadata.json file in jar file");
                System.out.println("    - Found " + properties.getCrossLanguageDefinitions().size()
                        + " cross-language definition IDs");
                return Optional.of(properties);
            }
        } catch (IOException e) {
            System.out.println("  ERROR: Unable to parse metadata.json file in jar file - continuing...");
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Cross Languages Definition ID is used to map from a model name or a method name to a TypeSpec definition ID. This
     * is used to enable cross-language linking, to make review time quicker as reviewers can jump between languages to
     * see how the API is implemented in each language.
     */
    public Optional<String> getCrossLanguageDefinitionId(String fullyQualifiedName) {
        return Optional.ofNullable(crossLanguageDefinitions.get(fullyQualifiedName));
    }

    /**
     * Returns an unmodifiable map of all the cross-language definition IDs.
     */
    public Map<String, String> getCrossLanguageDefinitions() {
        return Collections.unmodifiableMap(crossLanguageDefinitions);
    }

    public Flavor getFlavor() {
        return flavor;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject()
            .writeMapField("crossLanguageDefinitions", crossLanguageDefinitions, JsonWriter::writeString);

        if (flavor != null) {
            jsonWriter.writeStringField("flavor", flavor.getSerializationValue());
        }

        return jsonWriter.writeEndObject();
    }

    /**
     * Constructs an instance of {@code ApiViewProperties} from the provided {@code JsonReader}.
     * The method reads the JSON structure and maps it to the fields of the {@code ApiViewProperties} object.
     * It skips over any unrecognized fields in the JSON.
     *
     * @param jsonReader the JSON reader used to parse the input JSON structure
     * @return an instance of {@code ApiViewProperties} constructed from the provided JSON data
     * @throws IOException if an I/O error occurs while reading the JSON structure
     */
    public static ApiViewProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ApiViewProperties properties = new ApiViewProperties();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("crossLanguageDefinitions".equals(fieldName)) {
                    properties.crossLanguageDefinitions = reader.readMap(JsonReader::getString);
                } else if ("flavor".equals(fieldName)) {
                    properties.flavor = Flavor.fromSerializationValue(reader.getString());
                } else {
                    // Skip unrecognized fields as metadata.json may contain additional data not relevant to APIView.
                    reader.skipChildren();
                }
            }

            return properties;
        });
    }
}
