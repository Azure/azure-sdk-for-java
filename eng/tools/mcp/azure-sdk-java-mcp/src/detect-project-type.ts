import { CallToolResult } from "@modelcontextprotocol/sdk/types";
import * as fs from "fs";
import * as path from "path";

export async function detectProjectType(
  projectPath: string,
): Promise<CallToolResult> {
  try {
    if (!fs.existsSync(projectPath)) {
      return {
        content: [
          {
            type: "text",
            text: `❌ Error: Project path does not exist: ${projectPath}`,
          },
        ],
      };
    }

    const tspLocationPath = path.join(projectPath, "tsp-location.yaml");
    const pomXmlPath = path.join(projectPath, "pom.xml");
    const srcPath = path.join(projectPath, "src");
    const tempTypeSpecPath = path.join(projectPath, "TempTypeSpecFiles");

    let projectType = "unknown";
    let recommendations: string[] = [];

    if (fs.existsSync(tspLocationPath)) {
      projectType = "existing-typespec";
      recommendations.push("🔄 This is an EXISTING TypeSpec-based Java SDK project.");
      recommendations.push("✅ Use 'update_java_sdk' to update this project with latest TypeSpec changes.");
      recommendations.push("⚠️  Avoid using 'sync_java_sdk' as it's meant for new projects.");

      if (fs.existsSync(tempTypeSpecPath)) {
        recommendations.push("📁 Found 'TempTypeSpecFiles' directory - you can also use 'generate_java_sdk' directly.");
      }
    } else if (fs.existsSync(pomXmlPath) && fs.existsSync(srcPath)) {
      projectType = "existing-maven";
      recommendations.push("📦 This appears to be an existing Maven project without TypeSpec integration.");
      recommendations.push("🆕 Use 'sync_java_sdk' to initialize TypeSpec-based SDK generation.");
      recommendations.push("📋 Consider reviewing 'instruction_migrate_typespec' for migration guidance.");
    } else if (fs.existsSync(tempTypeSpecPath)) {
      projectType = "ready-for-generation";
      recommendations.push("🎯 This directory contains TypeSpec source files ready for generation.");
      recommendations.push("✅ Use 'generate_java_sdk' to generate the Java SDK from TypeSpec source.");
    } else {
      projectType = "new-project";
      recommendations.push("🆕 This appears to be a new/empty project directory.");
      recommendations.push("✅ Use 'sync_java_sdk' to initialize a new TypeSpec-based Java SDK project.");
      recommendations.push("🔧 Make sure to run 'prepare_java_sdk_environment' first if you haven't set up your development environment.");
    }

    let detectedFiles: string[] = [];
    if (fs.existsSync(tspLocationPath)) detectedFiles.push("✓ tsp-location.yaml");
    if (fs.existsSync(pomXmlPath)) detectedFiles.push("✓ pom.xml");
    if (fs.existsSync(srcPath)) detectedFiles.push("✓ src/ directory");
    if (fs.existsSync(tempTypeSpecPath)) detectedFiles.push("✓ TempTypeSpecFiles/ directory");

    const result = [
      `🔍 **Project Type Detection Results**`,
      ``,
      `**Path:** ${projectPath}`,
      `**Project Type:** ${projectType}`,
      ``,
      `**Detected Files:**`,
      detectedFiles.length > 0 ? detectedFiles.join("\n") : "❌ No key project files detected",
      ``,
      `**Recommendations:**`,
      recommendations.join("\n"),
      ``,
      `**Available Tools:**`,
      `• sync_java_sdk - Initialize new TypeSpec projects`,
      `• update_java_sdk - Update existing TypeSpec projects`,
      `• generate_java_sdk - Generate from existing TypeSpec source`,
      `• prepare_java_sdk_environment - Set up development environment`,
    ].join("\n");

    return {
      content: [
        {
          type: "text",
          text: result,
        },
      ],
    };
  } catch (error) {
    return {
      content: [
        {
          type: "text",
          text: `❌ Error detecting project type: ${error instanceof Error ? error.message : String(error)}`,
        },
      ],
    };
  }
}
