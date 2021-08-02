// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.bomgenerator;

import java.util.regex.Matcher;

import static com.azure.tools.bomgenerator.Utils.BASE_AZURE_GROUPID;
import static com.azure.tools.bomgenerator.Utils.COMMANDLINE_EXTERNALDEPENDENCIES;
import static com.azure.tools.bomgenerator.Utils.COMMANDLINE_GROUPID;
import static com.azure.tools.bomgenerator.Utils.COMMANDLINE_INPUTFILE;
import static com.azure.tools.bomgenerator.Utils.COMMANDLINE_OUTPUTFILE;
import static com.azure.tools.bomgenerator.Utils.COMMANDLINE_POMFILE;

public class Main {

    public static void main(String[] args) {
        BomGenerator generator = parseCommandLine(args);
        generator.generate();
    }

    private static BomGenerator parseCommandLine(String[] args) {
        String inputFile = null, outputFile = null, pomFile = null;
        for (String arg : args) {
            Matcher matcher = Utils.COMMANDLINE_REGEX.matcher(arg);
            if (matcher.matches()) {
                if (matcher.groupCount() == 2) {
                    String argName = matcher.group(1);
                    String argValue = matcher.group(2);

                    switch (argName.toLowerCase()) {
                        case COMMANDLINE_INPUTFILE:
                            inputFile = argValue;
                            break;

                        case COMMANDLINE_OUTPUTFILE:
                            outputFile = argValue;
                            break;

                        case COMMANDLINE_POMFILE:
                            pomFile = argValue;
                            break;
                    }
                }
            }
        }

        // validate that each of these are present.
        validateInputs(inputFile, outputFile, pomFile);
        return new BomGenerator(inputFile, outputFile, pomFile);
    }

    private static void validateInputs(String inputFile, String outputFile, String pomFile) {
        validateInput(inputFile, COMMANDLINE_INPUTFILE);
        validateInput(outputFile, COMMANDLINE_OUTPUTFILE);
        validateInput(pomFile, COMMANDLINE_POMFILE);
    }

    private static void validateInput(String argName, String argValue) {
        if(argValue == null || argValue.isEmpty()) {
            throw new NullPointerException(String.format("%s can't be null", argName));
        }
    }
}
