import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * A {@link BetaAnnotation} that marks an entire generated class with {@code @Beta}.
 */
final class BetaClass extends BetaAnnotation {

    static final String TYPE = "class";

    BetaClass(String className, String description) {
        super(className, description);
    }

    @Override
    String getType() {
        return TYPE;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("type", TYPE);
        jsonWriter.writeStringField("class_name", getClassName());
        jsonWriter.writeStringField("annotation_description", getDescription());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of {@link BetaClass} from the {@link JsonReader}.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of {@link BetaClass}.
     * @throws IOException If an error occurs while reading.
     */
    static BetaClass fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String className = null;
            String description = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("class_name".equals(fieldName)) {
                    className = reader.getString();
                } else if ("annotation_description".equals(fieldName)) {
                    description = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return new BetaClass(className, description);
        });
    }
}
