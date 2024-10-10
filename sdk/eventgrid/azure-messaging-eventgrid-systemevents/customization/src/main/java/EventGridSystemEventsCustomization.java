// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

import java.util.List;

/**
 * This class contains the customization code to customize the AutoRest generated code for Event Grid.
 */
public class EventGridSystemEventsCustomization extends Customization {
    private final String newLine = System.lineSeparator();
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        customizeModuleInfo(customization, logger);
    }

    /**
     * Customize the module-info.java file. This is necessary due to having a models subpackage logically; we
     * end up with an export for a package with no types, so we remove the export.
     *
     * @param customization The LibraryCustomization object.
     * @param logger The logger object.
     */
    public void customizeModuleInfo(LibraryCustomization customization, Logger logger) {

        Editor editor = customization.getRawEditor();
        List<String> lines = editor.getFileLines("src/main/java/module-info.java");
        StringBuilder sb = new StringBuilder();
        lines.forEach(line -> {
            if (!line.trim().equals("exports com.azure.messaging.eventgrid;")) {
                sb.append(line).append(newLine);
            }
        });
        editor.replaceFile("src/main/java/module-info.java", sb.toString());
    }

}
