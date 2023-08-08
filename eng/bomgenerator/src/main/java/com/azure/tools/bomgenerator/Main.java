// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.bomgenerator;

import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import static com.azure.tools.bomgenerator.Utils.ANALYZE_MODE;
import static com.azure.tools.bomgenerator.Utils.COMMANDLINE_INPUTDIRECTORY;
import static com.azure.tools.bomgenerator.Utils.COMMANDLINE_OUTPUTDIRECTORY;
import static com.azure.tools.bomgenerator.Utils.COMMANDLINE_MODE;
import static com.azure.tools.bomgenerator.Utils.COMMANDLINE_REGEX;
import static com.azure.tools.bomgenerator.Utils.GENERATE_MODE;
import static com.azure.tools.bomgenerator.Utils.validateNotNullOrEmpty;
import static com.azure.tools.bomgenerator.Utils.validateValues;

public class Main {

    public static void main(String[] args) {
        BomGenerator generator = null;
        try {
            generator = parseCommandLine(args);
            if(!generator.run()) {
                System.exit(1);
            }

            System.out.println("Completed successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("Error occurred.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static BomGenerator parseCommandLine(String[] args) throws FileNotFoundException {
        String inputDir = null, outputDir = null, mode = null;
        for (String arg : args) {
            Matcher matcher = COMMANDLINE_REGEX.matcher(arg);
            if (matcher.matches()) {
                if (matcher.groupCount() == 2) {
                    String argName = matcher.group(1);
                    String argValue = matcher.group(2);

                    switch (argName.toLowerCase()) {
                        case COMMANDLINE_INPUTDIRECTORY:
                            validateNotNullOrEmpty(argName, argValue);
                            inputDir = argValue;
                            break;

                        case COMMANDLINE_OUTPUTDIRECTORY:
                            validateNotNullOrEmpty(argName, argValue);
                            outputDir = argValue;
                            break;

                        case COMMANDLINE_MODE:
                            validateNotNullOrEmpty(argName, argValue);
                            validateValues(argName, argValue, GENERATE_MODE, ANALYZE_MODE);
                            mode = argValue;
                            break;
                    }
                }

            }
        }

        validateNotNullOrEmpty(inputDir, "inputDir");
        validateNotNullOrEmpty(outputDir, "outputDir");
        BomGenerator generator = new BomGenerator(inputDir, outputDir, mode);
        return generator;
    }
}
