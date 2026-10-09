// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

/**
 * Code customization after code generation.
 */
public class MonitorSlisCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        Editor editor = customization.getRawEditor();
        customizeConditionOperator(editor);
        customizeCondition(editor);
    }

    private static void customizeConditionOperator(Editor editor) {
        String fileName = "src/main/java/com/azure/resourcemanager/monitor/slis/models/ConditionOperator.java";
        String fileContent = editor.getFileContent(fileName)
            .replace("Matches when `value` is one of the items in the `^^`-delimited list (for example, `value` = \"east^^west^^north\").",
                "Matches when {@code value} is one of the items in the {@code ^^}-delimited list (for example, {@code value} = \"east^^west^^north\").")
            .replace("Matches when `value` is none of the items in the `^^`-delimited list (for example, `value` =",
                "Matches when {@code value} is none of the items in the {@code ^^}-delimited list (for example, {@code value} =");

        editor.replaceFile(fileName, fileContent);
    }

    private static void customizeCondition(Editor editor) {
        String fileName = "src/main/java/com/azure/resourcemanager/monitor/slis/models/Condition.java";
        String fileContent = editor.getFileContent(fileName)
            .replace("`in`", "{@code in}")
            .replace("`notin`", "{@code notin}")
            .replace("`^^`", "{@code ^^}")
            .replace("Get the value property:", "Get the {@code value} property:")
            .replace("Set the value property:", "Set the {@code value} property:");

        editor.replaceFile(fileName, fileContent);
    }
}
