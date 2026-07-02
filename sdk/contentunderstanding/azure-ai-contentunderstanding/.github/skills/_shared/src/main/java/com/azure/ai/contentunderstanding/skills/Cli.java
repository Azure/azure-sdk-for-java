// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/*
 * Subcommand dispatcher for the cu-skill tool. Mirrors Python's
 * extract_layout.py / create_and_test.py / create_and_test_router.py
 * entry points and the .NET Program.cs.
 */

package com.azure.ai.contentunderstanding.skills;

import java.io.IOException;
import java.util.Arrays;

public final class Cli {

    private Cli() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0 || isHelp(args[0])) {
            printUsage();
            System.exit(args.length == 0 ? 1 : 0);
            return;
        }
        String subcommand = args[0];
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        int exit;
        switch (subcommand) {
            case "extract-layout":
                exit = ExtractLayoutCommand.run(subArgs);
                break;
            case "create-and-test":
                exit = CreateAndTestCommand.run(subArgs);
                break;
            case "create-and-test-router":
                exit = CreateAndTestRouterCommand.run(subArgs);
                break;
            default:
                System.err.println("unknown subcommand: " + subcommand);
                printUsage();
                exit = 1;
        }
        System.exit(exit);
    }

    private static boolean isHelp(String arg) {
        return "-h".equals(arg) || "--help".equals(arg) || "help".equals(arg);
    }

    private static void printUsage() {
        System.out.println("cu-skill — Content Understanding analyzer-authoring tool.");
        System.out.println();
        System.out.println("Subcommands:");
        System.out.println("  extract-layout              extract document layout (stage 1)");
        System.out.println("  create-and-test             validate, create, batch-test a single-type analyzer");
        System.out.println("  create-and-test-router      classify-and-route variant (N inner + 1 outer)");
        System.out.println();
        System.out.println("Use '<subcommand> --help' for per-command flags.");
    }
}
