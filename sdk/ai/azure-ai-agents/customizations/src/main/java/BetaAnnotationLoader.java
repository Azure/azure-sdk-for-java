import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;

import java.io.InputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * Loads the list of {@link BetaAnnotation} entries from {@code beta-annotations.json} on the
 * classpath using azure-core's azure-json.
 */
final class BetaAnnotationLoader {

    private static final String RESOURCE = "/beta-annotations.json";

    private BetaAnnotationLoader() {
    }

    static List<BetaAnnotation> load() {
        try (InputStream stream = BetaAnnotationLoader.class.getResourceAsStream(RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException("Could not find " + RESOURCE + " on the classpath.");
            }

            try (JsonReader reader = JsonProviders.createReader(stream)) {
                return reader.readArray(BetaAnnotation::fromJson);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + RESOURCE + ".", e);
        }
    }
}
