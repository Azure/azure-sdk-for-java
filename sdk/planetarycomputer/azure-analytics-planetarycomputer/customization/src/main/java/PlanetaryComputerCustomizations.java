// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains customizations for Azure Analytics Planetary Computer code generation.
 *
 * <p>Workaround for non-deterministic TypeReference ordering in generated Java clients.
 * The Java code generator produces TypeReference entries for the StacItemOrStacItemCollection
 * discriminated union subtypes (StacItem, StacItemCollection) in a non-deterministic order,
 * causing spurious diffs on each regeneration in DataClient.java and DataAsyncClient.java.
 *
 * <p>This customization enforces a consistent alphabetical ordering of TypeReference entries.
 *
 * <p><b>Temporary:</b> This can be removed once the generator fix for deterministic
 * TypeReference ordering is shipped.
 */
public class PlanetaryComputerCustomizations extends Customization {

    private static final String DATA_CLIENT_PATH =
        "src/main/java/com/azure/analytics/planetarycomputer/DataClient.java";
    private static final String DATA_ASYNC_CLIENT_PATH =
        "src/main/java/com/azure/analytics/planetarycomputer/DataAsyncClient.java";

    // Matches lines containing TypeReference<SomeType> patterns
    private static final Pattern TYPE_REFERENCE_PATTERN =
        Pattern.compile("TypeReference<(\\w+)>");

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        Editor editor = libraryCustomization.getRawEditor();

        sortTypeReferencesInFile(editor, DATA_CLIENT_PATH, logger);
        sortTypeReferencesInFile(editor, DATA_ASYNC_CLIENT_PATH, logger);
    }

    /**
     * Sorts consecutive lines containing TypeReference declarations/usages in the
     * given file to enforce a deterministic order.
     *
     * <p>The method identifies groups of consecutive lines that each contain a
     * TypeReference pattern, sorts each group alphabetically by the generic type
     * parameter name, and writes the sorted content back to the file.
     *
     * @param editor   The raw editor for file manipulation.
     * @param filePath The path to the file to process.
     * @param logger   Logger for diagnostic output.
     */
    private static void sortTypeReferencesInFile(Editor editor, String filePath, Logger logger) {
        String content = editor.getFileContent(filePath);
        if (content == null) {
            logger.warn("File not found, skipping TypeReference sorting: {}", filePath);
            return;
        }

        String[] lines = content.split("\n", -1);
        boolean modified = false;

        int i = 0;
        while (i < lines.length) {
            // Find the start of a group of consecutive lines with TypeReference
            if (containsTypeReference(lines[i])) {
                int groupStart = i;
                List<String> group = new ArrayList<>();
                group.add(lines[i]);
                i++;

                // Collect consecutive TypeReference lines
                while (i < lines.length && containsTypeReference(lines[i])) {
                    group.add(lines[i]);
                    i++;
                }

                // Only sort if the group has more than one line
                if (group.size() > 1) {
                    List<String> sorted = new ArrayList<>(group);
                    sorted.sort(Comparator.comparing(
                        PlanetaryComputerCustomizations::extractTypeReferenceName));

                    // Check if sorting changed anything
                    if (!sorted.equals(group)) {
                        for (int j = 0; j < sorted.size(); j++) {
                            lines[groupStart + j] = sorted.get(j);
                        }
                        modified = true;
                        logger.info("Sorted {} TypeReference entries at line {} in {}",
                            group.size(), groupStart + 1, filePath);
                    }
                }
            } else {
                i++;
            }
        }

        if (modified) {
            editor.replaceFile(filePath, String.join("\n", lines));
            logger.info("TypeReference ordering stabilized in {}", filePath);
        }
    }

    /**
     * Checks if a line contains a TypeReference pattern.
     */
    private static boolean containsTypeReference(String line) {
        return TYPE_REFERENCE_PATTERN.matcher(line).find();
    }

    /**
     * Extracts the generic type name from a TypeReference<TypeName> pattern.
     * Falls back to the full line for comparison if no match is found.
     */
    private static String extractTypeReferenceName(String line) {
        Matcher matcher = TYPE_REFERENCE_PATTERN.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return line;
    }
}