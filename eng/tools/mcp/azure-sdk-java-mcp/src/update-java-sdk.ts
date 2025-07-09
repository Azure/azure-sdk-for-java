import { CallToolResult } from "@modelcontextprotocol/sdk/types";
import { spawnAsync } from "./utils/index.js";
import * as fs from "fs";
import * as path from "path";

export async function updateJavaSdk(
  packagePath: string,
  commitHash?: string,
  repo?: string,
  tspConfig?: string,
  localSpec?: string,
): Promise<CallToolResult> {
  try {
    // Validate package path exists and contains tsp-location.yaml
    if (!fs.existsSync(packagePath)) {
      return {
        content: [
          {
            type: "text",
            text: `Error: Package path does not exist: ${packagePath}`,
          },
        ],
      };
    }

    const tspLocationPath = path.join(packagePath, "tsp-location.yaml");
    if (!fs.existsSync(tspLocationPath)) {
      return {
        content: [
          {
            type: "text",
            text: `Error: tsp-location.yaml not found in ${packagePath}. This appears to be a new project - use sync_java_sdk instead.`,
          },
        ],
      };
    }

    // Build TypeSpec arguments for update command
    const args = ["update", "--debug"];

    // Add output directory
    args.push("--output-dir", packagePath);

    if (commitHash) {
      args.push("--commit", commitHash);
    }

    if (repo) {
      args.push("--repo", repo);
    }

    if (tspConfig) {
      args.push("--tsp-config", tspConfig);
    }

    if (localSpec) {
      args.push("--local-spec-repo", localSpec);
    }

    // Add save-inputs flag to preserve configuration
    args.push("--save-inputs");

    console.log(`Running tsp-client update with args: ${args.join(" ")}`);

    // Run the update command from the package directory
    const updateResult = await spawnAsync("tsp-client", args, {
      cwd: packagePath,
      shell: true,
      timeout: 600000, // 10 minute timeout
    });

    if (updateResult.success) {
      return {
        content: [
          {
            type: "text",
            text: `✅ Java SDK update completed successfully!\n\nOutput:\n${updateResult.stdout}\n\nThe TypeSpec source has been updated and Java SDK regenerated.`,
          },
        ],
      };
    } else {
      return {
        content: [
          {
            type: "text",
            text: `❌ Java SDK update failed!\n\nError:\n${updateResult.stderr}\n\nOutput:\n${updateResult.stdout}`,
          },
        ],
      };
    }
  } catch (error) {
    console.error("Error updating Java SDK:", error);
    return {
      content: [
        {
          type: "text",
          text: `❌ Error updating Java SDK: ${error instanceof Error ? error.message : String(error)}`,
        },
      ],
    };
  }
}
