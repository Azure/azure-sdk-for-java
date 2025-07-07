# MCP Server for Java SDK Tools

A Model Context Protocol (MCP) server that provides comprehensive tools for generating, building, and managing Java SDKs from TypeSpec definitions using the `tsp-client` tool.

## Features

This MCP server provides the following tools:

1. **init_java_sdk** - Initialize tsp-location.yaml configuration for Java SDK from a TypeSpec configuration URL
2. **sync_java_sdk** - Download and synchronize TypeSpec source files for Java SDK generation
3. **generate_java_sdk** - Generate or update Java SDK code from TypeSpec definitions
4. **clean_java_source** - Clean and remove generated Java source files from SDK directory
5. **build_java_sdk** - Compile and build the Java SDK with Maven for Azure services
6. **get_java_sdk_changelog** - Generate and retrieve changelog information for the Java SDK
7. **update_java_sdk_changelog** - Update the CHANGELOG.md file for a Java SDK module
8. **instruction_migrate_typespec** - Provide step-by-step instructions for migrating from Swagger to TypeSpec
9. **update_client_name** - Guide through updating client class and property names in TypeSpec and Java SDK
10. **prepare_java_sdk_environment** - Provide environment setup instructions and dependency requirements

## Prerequisites

Before using this MCP server, ensure you have:

1. **Project Structure** - The tools can be run from either:
   - A service module directory containing `tsp-location.yaml` (e.g., `/azure-sdk-for-java/sdk/batch/azure-compute-batch`)
   - The SDK root directory (e.g., `/azure-sdk-for-java`)
2. **Nodejs** installed.

## Installation

1. Install dependencies:
```bash
npm install
```

2. Build the project:
```bash
npm run build
```

## Configure MCP Server in VSCode


### Step 1: Install and Build the MCP Server

Add the following configuration to your `.vscode/mcp.json` file:

```
$ cd eng/tools/mcp/azure-sdk-java-mcp
$ npm install
$ npm run build
```

You can find the build files in `./eng/tools/mcp/azure-sdk-java-mcp/dist`. 

### Step 2: Restart VSCode

After adding the configuration, restart VSCode for the changes to take effect.

### Step 3: Start the MCP Server

Click the 'Start' button in mcp.json.
![alt text](assets/image.png)

You can verify that the MCP server is working by:
1. Opening GitHub Copilot Chat in VSCode
2. Asking Copilot to list available tools
3. You should see the Java SDK tools listed


## Sample Prompts

Here are some example prompts you can use with GitHub Copilot to interact with the Java SDK tools:

### Basic SDK Operations
- `generate java sdk for "azure-compute-batch" from local TypeSpec source: C:\workspace\azure-rest-api-specs\specification\batch\Azure.Batch\tspconfig.yaml`
- `update java sdk for "azure-compute-batch" from local TypeSpec source: C:\workspace\azure-rest-api-specs\specification\batch\Azure.Batch\tspconfig.yaml`

### Prepare Environments
- `prepare java sdk environment`

### Client Name Updates
- `update client name: EnableBatchJobOptions to BatchJobEnableOptions`
- `update client name: MediaMessageContent.mediaUri to MediaMessageContent.mediaUrl`

### Build and Changelog
- `build the java sdk for com.azure.batch`
- `get changelog for the java sdk`
- `update changelog for the java sdk`

### Generate SDK after Migrating from Swagger to TypeSpec 
- `generate sdk after migrating to typespec for "azure-mgmt-dnsresolver`


## Development

To run in development mode with automatic recompilation:
```bash
npm run dev
```

To run tests:
```bash
npm test              # Run tests in watch mode
npm run test:run      # Run tests once
```

## Tools Documentation

### 1. init_java_sdk
Initialize the tsp-location.yaml configuration file for Java SDK generation.

**Parameters:**
- `localTspConfigPath` (optional): The local path to the tspconfig.yaml file
- `tspConfigUrl` (optional): The URL to the tspconfig.yaml file (e.g., GitHub raw URL with commit ID)

**Note:** Provide either `localTspConfigPath` OR `tspConfigUrl`, not both.

**Example:**
```json
{
  "name": "init_java_sdk",
  "arguments": {
    "tspConfigUrl": "https://github.com/Azure/azure-rest-api-specs/blob/dee71463cbde1d416c47cf544e34f7966a94ddcb/specification/contosowidgetmanager/Contoso.WidgetManager/tspconfig.yaml"
  }
}
```

### 2. sync_java_sdk
Synchronize and download the TypeSpec source files for Java SDK from the configuration in tsp-location.yaml.

**Parameters:**
- `localTspConfigPath` (optional): The local path to the tspconfig.yaml file (e.g., `C:\workspace\azure-rest-api-specs\specification\communication\Communication.Messages\tspconfig.yaml`)
- `remoteTspConfigUrl` (optional): The remote URL to the tspconfig.yaml file (URL should contain commit ID instead of branch name)

**Note:** Provide either `localTspConfigPath` OR `remoteTspConfigUrl`, not both.

**Example:**
```json
{
  "name": "sync_java_sdk",
  "arguments": {
    "localTspConfigPath": "C:\\workspace\\azure-rest-api-specs\\specification\\communication\\Communication.Messages\\tspconfig.yaml"
  }
}
```

### 3. generate_java_sdk
Generate or update Java SDK from TypeSpec definitions. Requires existing tsp-location.yaml and TempTypeSpecFiles directory.

**Parameters:**
- `cwd` (required): The absolute path to the current working directory which contains the 'TempTypeSpecFiles' directory with TypeSpec source files (e.g., `C:\workspace\azure-sdk-for-java\sdk\communication\communication-messages`)

**Example:**
```json
{
  "name": "generate_java_sdk",
  "arguments": {
    "cwd": "C:\\workspace\\azure-sdk-for-java\\sdk\\communication\\communication-messages"
  }
}
```

### 4. clean_java_source
Clean Java source files in the specified SDK directory.

**Parameters:**
- `cwd` (required): The absolute path to the directory where tsp-location.yaml is located

**Example:**
```json
{
  "name": "clean_java_source",
  "arguments": {
    "cwd": "/path/to/java/sdk/module"
  }
}
```

### 5. build_java_sdk
Build the Java SDK for Azure services (groupId starting with `com.azure`).

**Parameters:**
- `moduleDirectory` (required): The absolute path to the service sub module directory containing tsp-location.yaml
- `rootDirectory` (required): The absolute path to the azure-sdk-for-java directory, where the moduleDirectory is a submodule of it
- `groupId` (required): The group ID for the Java SDK
- `artifactId` (required): The artifact ID for the Java SDK

**Example:**
```json
{
  "name": "build_java_sdk",
  "arguments": {
    "moduleDirectory": "/path/to/java/sdk/module",
    "rootDirectory": "/path/to/azure-sdk-for-java",
    "groupId": "com.azure.myservice",
    "artifactId": "azure-myservice"
  }
}
```

### 6. get_java_sdk_changelog
Get the changelog for the Java SDK for Azure services.

**Parameters:**
- `jarPath` (required): The absolute path to the JAR file (should be under the `target` directory)
- `groupId` (required): The group ID for the Java SDK
- `artifactId` (required): The artifact ID for the Java SDK

**Example:**
```json
{
  "name": "get_java_sdk_changelog",
  "arguments": {
    "jarPath": "/path/to/target/azure-myservice-1.0.0.jar",
    "groupId": "com.azure.myservice",
    "artifactId": "azure-myservice"
  }
}
```

### 7. update_java_sdk_changelog
Update the CHANGELOG.md file for a service sub module whose groupId starts with `com.azure`.

**Parameters:**
- `jarPath` (required): The absolute path to the JAR file of the Java SDK (should be under the `target` directory of the Java SDK module)
- `groupId` (required): The group ID for the Java SDK
- `artifactId` (required): The artifact ID for the Java SDK

**Example:**
```json
{
  "name": "update_java_sdk_changelog",
  "arguments": {
    "jarPath": "/path/to/target/azure-myservice-1.0.0.jar",
    "groupId": "com.azure.myservice",
    "artifactId": "azure-myservice"
  }
}
```

### 8. instruction_migrate_typespec
Get instructions for generating Java SDK after migrating from Swagger to TypeSpec.

**Parameters:**
- None

**Example:**
```json
{
  "name": "instruction_migrate_typespec",
  "arguments": {}
}
```

### 9. update_client_name
Update client names for both TypeSpec files and the generated Java SDK. Provides instructions for renaming.

**Parameters:**
- None (the tool returns instructions that guide you through the update process)

**Example:**
```json
{
  "name": "update_client_name",
  "arguments": {}
}
```

### 10. prepare_java_sdk_environment
Get step-by-step instructions to prepare the environment for Java SDK generation.

**Parameters:**
- `cwd` (required): The absolute path to the working directory where the environment should be prepared

**Example:**
```json
{
  "name": "prepare_java_sdk_environment",
  "arguments": {
    "cwd": "/path/to/workspace"
  }
}
```

## Usage

This server is designed to be used with MCP-compatible clients. The server communicates via stdio and provides tools for the complete Java SDK generation workflow.

### Working Directory Flexibility

The MCP tools support running from two different working directory locations:

1. **Service Module Directory**: Open the specific service directory (e.g., `azure-sdk-for-java/sdk/batch/azure-compute-batch`) that contains `tsp-location.yaml`
2. **SDK Root Directory**: Open the Azure SDK for Java root directory (e.g., `azure-sdk-for-java`) and the tools will automatically locate the appropriate service modules

This flexibility allows you to work at the level that best suits your workflow.

## Contributing

1. Make changes to the TypeScript source files in `src/`
2. Add tests for new functionality in `src/**/*.spec.ts`
3. Build the project with `npm run build`
4. Test your changes with `npm test` and `npm start`

## License

MIT
