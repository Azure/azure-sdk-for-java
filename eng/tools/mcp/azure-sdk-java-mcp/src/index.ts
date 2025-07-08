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
import { cleanJavaSource } from "./clean-java-source.js";
import { updateChangelogMd } from "./update-changelog-md.js";

// Create the MCP server
const server = new McpServer({
  name: "java-sdk-tools-server",
  version: "1.0.0",
});

// Setup logging function
const logToolCall = (toolName: string) => {
  const logMsg = `[${new Date().toISOString()}] [MCP] Tool called: ${toolName}\n`;
  process.stderr.write(logMsg);
};


// Register clean_java_source tool
server.registerTool(
  "clean_java_source",
  {
    description:
      "Clean the Java source code for a module, removing all generated source files and directories.",
    inputSchema: {
      cwd: z
        .string()
        .describe(
          "The absolute path to the directory where tsp-location.yaml is located",
        ),
    },
    annotations: {
      title: "Clean Java Source",
    },
  },
  async (args) => {
    logToolCall("clean_java_source");
    const result = await cleanJavaSource(args.cwd);
    return result;
  },
);

// Register build_java_sdk tool
server.registerTool(
  "build_java_sdk",
  {
    description:
      "Build the Java SDK for a service sub module whose groupId starts with `com.azure`. The tool takes the module directory, root directory, groupId and artifactId as input parameters.",
    inputSchema: {
      moduleDirectory: z
        .string()
        .describe(
          "The absolute path to the service sub module directory containing tsp-location.yaml",
        ),
      rootDirectory: z
        .string()
        .describe(
          "The absolute path to the azure-sdk-for-java directory, where the moduleDirectory is a submodule of it",
        ),
      groupId: z.string().describe("The group ID for the Java SDK"),
      artifactId: z.string().describe("The artifact ID for the Java SDK"),
    },
    annotations: {
      title: "Build Java SDK",
    },
  },
  async (args) => {
    logToolCall("build_java_sdk");
    const result = await buildJavaSdk(
      args.rootDirectory,
      args.moduleDirectory,
      args.groupId,
      args.artifactId,
    );
    return result;
  },
);

// Register get_java_sdk_changelog tool
server.registerTool(
  "get_java_sdk_changelog",
  {
    description:
      "Get the changelog for a service sub module whose groupId starts with `com.azure`. The tool takes the root directory, jarPath, groupId and artifactId as input parameters.",
    inputSchema: {
      jarPath: z
        .string()
        .describe(
          "The absolute path to the JAR file of the Java SDK. It should be under the `target` directory of the Java SDK module.",
        ),
      groupId: z.string().describe("The group ID for the Java SDK"),
      artifactId: z.string().describe("The artifact ID for the Java SDK"),
    },
    annotations: {
      title: "Get Java SDK Changelog",
    },
  },
  async (args) => {
    logToolCall("get_java_sdk_changelog");
    const result = await getJavaSdkChangelog(
      args.jarPath,
      args.groupId,
      args.artifactId,
    );
    return result;
  },
);

// Register update_java_sdk_changelog tool
server.registerTool(
  "update_java_sdk_changelog",
  {
    description:
      "Update the CHANGELOG.md file for a service sub module whose groupId starts with `com.azure`. The tool takes the absolute path to the JAR file, groupId and artifactId as input parameters.",
    inputSchema: {
      jarPath: z
        .string()
        .describe(
          "The absolute path to the JAR file of the Java SDK. It should be under the `target` directory of the Java SDK module.",
        ),
      groupId: z.string().describe("The group ID for the Java SDK"),
      artifactId: z.string().describe("The artifact ID for the Java SDK"),
    },
    annotations: {
      title: "Update Java SDK CHANGELOG.md file",
    },
  },
  async (args) => {
    logToolCall("update_java_sdk_changelog");
    const result = await updateChangelogMd(
      args.jarPath,
      args.groupId,
      args.artifactId,
    );
    return result;
  },
);

// Register instruction_migrate_typespec tool
server.registerTool(
  "instruction_migrate_typespec",
  {
    description:
      "The instructions for generating Java SDK after migrating from Swagger to TypeSpec",
    inputSchema: {},
    annotations: {
      title: "Migration Instructions",
    },
  },
  async () => {
    logToolCall("instruction_migrate_typespec");
    const result = await brownfieldMigration();
    return result;
  },
);

// Register sync_java_sdk tool
server.registerTool(
  "sync_java_sdk",
  {
    description:
      "Don't prepare environment before synchronize TypeSpec source. Synchronize/Download the TypeSpec source for a target service to generate Java SDK from. Always ask user to provide local tspconfig.yaml path or remote tspconfig.yaml url. The tool takes local tspconfig.yaml path or remote tspconfig.yaml url as input parameter.",
    inputSchema: {
      localTspConfigPath: z
        .string()
        .optional()
        .describe("The local absolute path to the tspconfig.yaml file. e.g. C:\\workspace\\azure-rest-api-specs\\specification\\communication\\Communication.Messages\\tspconfig.yaml"),
      remoteTspConfigUrl: z
        .string()
        .optional()
        .describe("The remote URL to the tspconfig.yaml file. The URL should contain commit id instead of branch name. e.g. https://github.com/Azure/azure-rest-api-specs/blob/dee71463cbde1d416c47cf544e34f7966a94ddcb/specification/contosowidgetmanager/Contoso.WidgetManager/tspconfig.yaml"),
    },
    annotations: {
      title: "Sync Java SDK",
    },
  },
  async (args) => {
    logToolCall("sync_java_sdk");
    const result = await initJavaSdk(
      args.localTspConfigPath,
      args.remoteTspConfigUrl,
    );
    return result;
  },
);

// Register generate_java_sdk tool
server.registerTool(
  "generate_java_sdk",
  {
    description:
      "Don't prepare environment before generating sdk. Generate SDK from TypeSpec source from 'TempTypeSpecFiles' for a target service module. If there is directory named 'TempTypeSpecFiles' in the current working directory, call this tool directly. If the directory is not present, ask user whether to generate from local TypeSpec source or remote TypeSpec source. If the user wants to generate from local TypeSpec source, ask for local path to tspconfig.yaml. If the user wants to generate from remote TypeSpec source, ask for remote tspconfig.yaml url. Then call the tool to sync sdk with proper input parameters before calling this tool to generate sdk. Ask to update changelog.md after generating SDK.",
    inputSchema: {
      cwd: z
        .string()
        .describe(
          "The absolute path to the current working directory which contains the 'TempTypeSpecFiles' directory with TypeSpec source files. e.g. C:\\workspace\\azure-sdk-for-java\\sdk\\communication\\communication-messages",
        ),
    },
    annotations: {
      title: "Generate Java SDK",
    },
  },
  async (args) => {
    logToolCall("generate_java_sdk");
    const result = await generateJavaSdk(
      args.cwd,
      true
    );
    return result;
  },
);

// Register update_client_name tool
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

// Register prepare_java_sdk_environment tool
server.registerTool(
  "prepare_java_sdk_environment",
  {
    description:
      "prepare the development environment for Java SDK generation, including 3 main areas: Node.js/npm, Java environment, and TypeSpec tools.",
    inputSchema: {
      cwd: z
        .string()
        .describe(
          "The absolute path to the working directory where the environment should be prepared",
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
  console.error("Java SDK Tools MCP server running on stdio");
}

main().catch(console.error);
