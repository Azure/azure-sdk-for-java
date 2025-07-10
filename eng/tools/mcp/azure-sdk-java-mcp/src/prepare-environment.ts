import { CallToolResult } from "@modelcontextprotocol/sdk/types";

export async function prepareJavaSdkEnvironmentCookbook(
  cwd: string,
): Promise<CallToolResult> {
  const cookbook = `
# Java SDK Generation Environment Setup Guide

**Working Directory:** \`${cwd}\`

This guide provides step-by-step instructions to prepare your environment for Java SDK generation in 3 main areas: Node.js/npm, Java environment, and TypeSpec tools.

---

## 1. Node.js/npm Environment Setup

Check local Node.js and npm versions. If they are not installed, or if the versions are not the latest, install or update them.

## 2. Java Environment Setup

Install Java JDK and Maven if not exists, you can find the latest Microsoft OpenJDK from install the latest microsoft openjdk from https://learn.microsoft.com/en-us/java/openjdk/download. 
After installation, add the Java JDK and Maven to your system' variable, follow below steps:
  - Configure JAVA_HOME and Add to PATH
  - Configure MAVEN_HOME and Add to PATH

## 3. TypeSpec Environment Setup

Install and update TypeSpec tools to latest versions, tools include:
- @azure-tools/typespec-client-generator-cli
- @typespec/compiler

## 4. Verify Installation

output the versions of installed tools to ensure everything is set up correctly

**Note:** If you need to check if the required tools are installed, please check them together insteand of checking them one by one.
**Troubleshooting:** If any tool shows "Not found", revisit the relevant section above and follow the installation instructions.
  `;

  console.error(
    `Generated streamlined environment preparation cookbook for: ${cwd}`,
  );

  return {
    content: [
      {
        type: "text",
        text: cookbook,
      },
    ],
  };
}
