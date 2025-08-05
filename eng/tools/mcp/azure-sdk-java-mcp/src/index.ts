#!/usr/bin/env node

import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { z } from "zod";
import { generateJavaSdk } from "./generate-java-sdk.js";
import { clientNameUpdateCookbook } from "./client-name-update.js";
import { brownfieldMigration } from "./brownfield-migrate.js";
import { initJavaSdk } from "./init-java-sdk.js";
import { prepareJavaSdkEnvironmentCookbook } from "./prepare-environment.js";
import { buildJavaSdk } from "./build-java-sdk.js";
import { getJavaSdkChangelog } from "./java-sdk-changelog.js";
import { updateChangelogMd } from "./update-changelog-md.js";

// Create the MCP server
const server = new McpServer({
    name: "azure-sdk-java-mcp",
    version: "1.0.0",
});

// Setup logging function
const logToolCall = (toolName: string) => {
    const logMsg = `[${new Date().toISOString()}] [MCP] Tool called: ${toolName}\n`;
    process.stderr.write(logMsg);
};

// // Tool: clean_java_source
// server.registerTool(
//     "clean_java_source",
//     {
//         description:
//             "Remove all generated Java source files and directories for a given module. This tool is typically used to clean up the output of previous SDK generations before a new build. It should only be applied to packages with the prefix `azure-resourcemanager-*`.",
//         inputSchema: {
//             cwd: z
//                 .string()
//                 .describe(
//                     "The absolute path to the module directory containing tsp-location.yaml. Example: C:\\workspace\\azure-sdk-for-java\\sdk\\devcenter\\azure-resourcemanager-devcenter",
//                 ),
//         },
//         annotations: {
//             title: "Clean Java Source",
//         },
//     },
//     async (args) => {
//         logToolCall("clean_java_source");
//         return await cleanJavaSource(args.cwd);
//     },
// );

// Tool: build_java_sdk
server.registerTool(
    "build_java_sdk",
    {
        description:
            "Build the Java SDK for a service submodule whose groupId starts with `com.azure`. This tool compiles and packages the Java SDK using Maven for the specified module.",
        inputSchema: {
            moduleDirectory: z
                .string()
                .describe(
                    "The absolute path to the service submodule directory containing tsp-location.yaml. Example: C:\\workspace\\azure-sdk-for-java\\sdk\\communication\\azure-communication-messages",
                ),
            rootDirectory: z
                .string()
                .describe(
                    "The absolute path to the azure-sdk-for-java repository root directory. The moduleDirectory must be a subdirectory of this path.",
                ),
            groupId: z.string().describe("The Maven groupId for the Java SDK module."),
            artifactId: z.string().describe("The Maven artifactId for the Java SDK module."),
        },
        annotations: {
            title: "Build Java SDK",
        },
    },
    async (args) => {
        logToolCall("build_java_sdk");
        return await buildJavaSdk(args.rootDirectory, args.moduleDirectory, args.groupId, args.artifactId);
    },
);

// Tool: get_java_sdk_changelog
server.registerTool(
    "get_java_sdk_changelog",
    {
        description:
            "Retrieve the changelog for a service submodule whose groupId starts with `com.azure`. Requires the absolute path to the JAR file, groupId, and artifactId as input parameters.",
        inputSchema: {
            jarPath: z
                .string()
                .describe(
                    "Absolute path to the JAR file of the Java SDK, typically located in the `target` directory of the module.",
                ),
            groupId: z.string().describe("The Maven groupId for the Java SDK module."),
            artifactId: z.string().describe("The Maven artifactId for the Java SDK module."),
        },
        annotations: {
            title: "Get Java SDK Changelog",
        },
    },
    async (args) => {
        logToolCall("get_java_sdk_changelog");
        const result = await getJavaSdkChangelog(args.jarPath, args.groupId, args.artifactId);
        return result;
    },
);

// Tool: update_java_sdk_changelog
server.registerTool(
    "update_java_sdk_changelog",
    {
        description:
            "Update the CHANGELOG.md file for a Java SDK service submodule whose groupId starts with `com.azure`. Requires the absolute path to the JAR file, groupId, and artifactId as input parameters.",
        inputSchema: {
            jarPath: z
                .string()
                .describe(
                    "Absolute path to the JAR file of the Java SDK, typically located in the `target` directory of the module.",
                ),
            groupId: z.string().describe("The Maven groupId for the Java SDK module."),
            artifactId: z.string().describe("The Maven artifactId for the Java SDK module."),
        },
        annotations: {
            title: "Update Java SDK CHANGELOG.md",
        },
    },
    async (args) => {
        logToolCall("update_java_sdk_changelog");
        return await updateChangelogMd(args.jarPath, args.groupId, args.artifactId);
    },
);

// Tool: instruction_migrate_typespec
server.registerTool(
    "instruction_migrate_typespec",
    {
        description:
            "Provides step-by-step instructions for generating a Java SDK after migrating from Swagger (OpenAPI) to TypeSpec. Use this tool to understand the migration workflow and required actions.",
        inputSchema: {},
        annotations: {
            title: "TypeSpec Migration Instructions",
        },
    },
    async () => {
        logToolCall("instruction_migrate_typespec");
        return await brownfieldMigration();
    },
);

// Tool: sync_typespec_source_files
server.registerTool(
    "sync_typespec_source_files",
    {
        description:
            "Synchronize or download the TypeSpec source for a target service to enable Java SDK generation. Accepts either a local absolute path to tspconfig.yaml or a remote URL (with commit id, not branch name).",
        inputSchema: {
            localTspConfigPath: z
                .string()
                .optional()
                .describe(
                    "The local absolute path to the tspconfig.yaml file. Example: C:\\workspace\\azure-rest-api-specs\\specification\\communication\\Communication.Messages\\tspconfig.yaml",
                ),
            remoteTspConfigUrl: z
                .string()
                .optional()
                .describe(
                    "The remote URL to the tspconfig.yaml file. The URL must contain a commit id, not a branch name. Example: https://github.com/Azure/azure-rest-api-specs/blob/dee71463cbde1d416c47cf544e34f7966a94ddcb/specification/contosowidgetmanager/Contoso.WidgetManager/tspconfig.yaml",
                ),
        },
        annotations: {
            title: "Sync TypeSpec Source Files",
        },
    },
    async (args) => {
        logToolCall("sync_typespec_source_files");
        const result = await initJavaSdk(args.localTspConfigPath, args.remoteTspConfigUrl);
        return result;
    },
);

// Tool: generate_java_sdk
server.registerTool(
    "generate_java_sdk",
    {
        description:
            "Generate the Java SDK from TypeSpec source files located in the 'TempTypeSpecFiles' directory within the specified working directory. If 'TempTypeSpecFiles' is not present, prompt the user to specify whether to generate from a local or remote TypeSpec source, and use the sync_typespec_source_files tool as needed before proceeding.",
        inputSchema: {
            cwd: z
                .string()
                .describe(
                    "The absolute path to the working directory containing the 'TempTypeSpecFiles' directory with TypeSpec source files. Example: C:\\workspace\\azure-sdk-for-java\\sdk\\communication\\azure-communication-messages",
                ),
        },
        annotations: {
            title: "Generate Java SDK",
        },
    },
    async (args) => {
        logToolCall("generate_java_sdk");
        const result = await generateJavaSdk(args.cwd, true);
        return result;
    },
);

// Tool: update_client_name
server.registerTool(
    "update_client_name",
    {
        description:
            "Update client name for both client.tsp and the generated java sdk. Follow the returned instruction to update old client name to new client name, be sure to ask for old client name and new client name. e.g. MediaMessageContent.mediaUri to MediaMessageContent.mediaUrl",
        inputSchema: {},
        annotations: {
            title: "Update Client Name",
        },
    },
    async () => {
        logToolCall("update_client_name");
        const result = await clientNameUpdateCookbook();
        return result;
    },
);

// Tool: prepare_java_sdk_environment
server.registerTool(
    "prepare_java_sdk_environment",
    {
        description:
            "Prepare the development environment required for Java SDK generation when there are errors about missing development environments. The tool returns a cookbook with step-by-step instructions to install necessary environments including Node.js/npm, Java/Maven, and TypeSpec tools.",
        inputSchema: {
            cwd: z
                .string()
                .describe(
                    "The absolute path to the working directory where the Java SDK environment should be prepared.",
                ),
        },
        annotations: {
            title: "Prepare Java SDK Environment",
        },
    },
    async (args) => {
        logToolCall("prepare_java_sdk_environment");
        const result = await prepareJavaSdkEnvironmentCookbook(args.cwd);
        return result;
    },
);

// Setup error handling
server.server.onerror = (error: Error) => {
    console.error("[MCP Error]", error);
};

process.on("SIGINT", async () => {
    await server.close();
    process.exit(0);
});

// Start the server
async function main() {
    const transport = new StdioServerTransport();
    await server.connect(transport);
}

main().catch(console.error);
