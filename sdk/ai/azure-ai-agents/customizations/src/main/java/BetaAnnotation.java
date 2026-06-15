import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;

import java.io.IOException;

/**
 * Base type for an entry in {@code beta-annotations.json}. The {@code type} field acts as a
 * discriminator that selects which concrete structure is deserialized: {@link BetaClass} for a whole
 * class, or {@link BetaMember} for a single field.
 * <p>
 * Deserialization follows the standard azure-json polymorphic pattern used by the generated models
 * (see {@code Tool#fromJson}): buffer the object, read the discriminator, then delegate to the
 * matching subtype's {@code fromJson}.
 */
abstract class BetaAnnotation implements JsonSerializable<BetaAnnotation> {

    private final String className;
    private final String description;

    BetaAnnotation(String className, String description) {
        this.className = className;
        this.description = description;
    }

    String getClassName() {
        return className;
    }

    String getDescription() {
        return description;
    }

    /**
     * Get the {@code type} discriminator value for this entry.
     *
     * @return the discriminator value.
     */
    abstract String getType();

    /**
     * Reads an instance of {@link BetaAnnotation} from the {@link JsonReader}, dispatching to the
     * concrete subtype based on the {@code type} discriminator.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of a {@link BetaAnnotation} subtype.
     * @throws IOException If an error occurs while reading.
     */
    static BetaAnnotation fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String discriminatorValue = null;
            try (JsonReader readerToUse = reader.bufferObject()) {
                // Prepare for reading
                readerToUse.nextToken();
                while (readerToUse.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = readerToUse.getFieldName();
                    readerToUse.nextToken();
                    if ("type".equals(fieldName)) {
                        discriminatorValue = readerToUse.getString();
                        break;
                    } else {
                        readerToUse.skipChildren();
                    }
                }
                // Use the discriminator value to determine which subtype should be deserialized.
                if (BetaClass.TYPE.equals(discriminatorValue)) {
                    return BetaClass.fromJson(readerToUse.reset());
                } else if (BetaMember.TYPE.equals(discriminatorValue)) {
                    return BetaMember.fromJson(readerToUse.reset());
                } else {
                    throw new IllegalStateException(
                        "Unknown or missing 'type' discriminator in beta-annotations.json: " + discriminatorValue);
                }
            }
        });
    }
}
