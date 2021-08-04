// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.bomgenerator;

import java.util.regex.Matcher;
import static com.azure.tools.bomgenerator.Utils.ANALYZE_MODE;
import static com.azure.tools.bomgenerator.Utils.COMMANDLINE_OVERRIDDEN_INPUTDEPENDENCIES_FILE;
import static com.azure.tools.bomgenerator.Utils.COMMANDLINE_INPUTFILE;
import static com.azure.tools.bomgenerator.Utils.COMMANDLINE_MODE;
import static com.azure.tools.bomgenerator.Utils.COMMANDLINE_OUTPUTFILE;
import static com.azure.tools.bomgenerator.Utils.COMMANDLINE_POMFILE;
import static com.azure.tools.bomgenerator.Utils.GENERATE_MODE;
import static com.azure.tools.bomgenerator.Utils.validateNotNullOrEmpty;
import static com.azure.tools.bomgenerator.Utils.validateValues;

public class Main {

    public static void main(String[] args) {
        BomGenerator generator = new BomGenerator();
        parseCommandLine(args, generator);
        generator.run();
    }

    private static void parseCommandLine(String[] args, BomGenerator generator) {
        for (String arg : args) {
            Matcher matcher = Utils.COMMANDLINE_REGEX.matcher(arg);
            if (matcher.matches()) {
                if (matcher.groupCount() == 2) {
                    String argName = matcher.group(1);
                    String argValue = matcher.group(2);

                    switch (argName.toLowerCase()) {
                        case COMMANDLINE_INPUTFILE:
                            validateNotNullOrEmpty(argName, argValue);
                            generator.setInputFileName(argValue);
                            break;

                        case COMMANDLINE_OVERRIDDEN_INPUTDEPENDENCIES_FILE:
                            validateNotNullOrEmpty(argName, argValue);
                            generator.setOverriddenInputDependenciesFileName(argValue);
                            break;

                        case COMMANDLINE_OUTPUTFILE:
                            validateNotNullOrEmpty(argName, argValue);
                            generator.setOutputFileName(argValue);
                            break;

                        case COMMANDLINE_POMFILE:
                            validateNotNullOrEmpty(argName, argValue);
                            generator.setPomFileName(argValue);
                            break;

                        case COMMANDLINE_MODE:
                            validateNotNullOrEmpty(argName, argValue);
                            validateValues(argName, argValue, GENERATE_MODE, ANALYZE_MODE);
                            generator.setMode(argValue);
                            break;
                    }
                }
            }
        }

        validateOptions(generator);
    }

    private static void validateOptions(BomGenerator generator) {
        switch (generator.getMode()) {
            case ANALYZE_MODE:
                // In analyze mode, we should ensure that the pom file is set.
                validateNotNullOrEmpty(generator.getPomFileName(), "pomFile");
                break;

            case GENERATE_MODE:
                // In generate mode, we should have the inputFile, outputFile and the pomFile.
                validateNotNullOrEmpty(generator.getPomFileName(), "pomFile");
                validateNotNullOrEmpty(generator.getInputFileName(), "inputFileName");
                validateNotNullOrEmpty(generator.getOutputFileName(), "outputFileName");
                break;
        }
    }
}
