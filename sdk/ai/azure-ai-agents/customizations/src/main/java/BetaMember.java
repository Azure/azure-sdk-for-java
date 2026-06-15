import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * A {@link BetaAnnotation} that marks a single field (and its getter/setter) on a generated class
 * with {@code @Beta}.
 */
final class BetaMember extends BetaAnnotation {

    static final String TYPE = "field";

    private final String member;

    BetaMember(String className, String description, String member) {
        super(className, description);
        this.member = member;
    }

    String getMember() {
        return member;
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
        jsonWriter.writeStringField("member_name", member);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of {@link BetaMember} from the {@link JsonReader}.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of {@link BetaMember}.
     * @throws IOException If an error occurs while reading.
     */
    static BetaMember fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String className = null;
            String description = null;
            String member = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("class_name".equals(fieldName)) {
                    className = reader.getString();
                } else if ("annotation_description".equals(fieldName)) {
                    description = reader.getString();
                } else if ("member_name".equals(fieldName)) {
                    member = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return new BetaMember(className, description, member);
        });
    }
}
