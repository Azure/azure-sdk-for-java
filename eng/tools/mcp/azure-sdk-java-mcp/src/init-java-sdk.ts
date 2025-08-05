import { CallToolResult } from "@modelcontextprotocol/sdk/types";
import { findAzureSdkRoot, spawnAsync } from "./utils/index.js";

export async function initJavaSdk(localTspConfigPath?: string, remoteTspConfigUrl?: string): Promise<CallToolResult> {
    try {
        let rootDirectory: string = await findAzureSdkRoot(process.cwd());
        process.chdir(rootDirectory);

        // Run the Java SDK generation command
        let generateResult;
        if (localTspConfigPath) {
            // tsp-client init --debug --tsp-config C:\workspace\azure-rest-api-specs\specification\communication\Communication.Messages\tspconfig.yaml  --commit 123 --repo Azure/azure-rest-api-specs --local-spec-repo C:\workspace\azure-rest-api-specs\specification\communication\Communication.Messages\tspconfig.yaml --save-inputs
            generateResult = await spawnAsync(
                "tsp-client",
                [
                    "init",
                    "--debug",
                    "--tsp-config",
                    localTspConfigPath,
                    "--local-spec-repo",
                    localTspConfigPath,
                    "--save-inputs",
                ],
                {
                    cwd: process.cwd(),
                    shell: true, // Use shell to allow tsp-client command
                    timeout: 600000, // 10 minute timeout
                },
            );
        } else if (remoteTspConfigUrl) {
            generateResult = await spawnAsync(
                "tsp-client",
                ["init", "--debug", "--tsp-config", remoteTspConfigUrl, "--save-inputs"],
                {
                    cwd: process.cwd(),
                    shell: true, // Use shell to allow tsp-client command
                    timeout: 600000, // 10 minute timeout
                },
            );
        } else {
            return {
                content: [
                    {
                        type: "text",
                        text: "No tspconfig.yaml provided, please provide either local or remote tspconfig.yaml.",
                    },
                ],
            };
        }

        let result = `Java SDK Generation Results:\n\n`;

        if (generateResult.success) {
            result += `✅ SDK generation completed successfully!\n\n`;
        } else {
            result += `❌ SDK generation failed with exit code ${generateResult.exitCode}\n\n`;

            if (generateResult.stdout) {
                result += `Output:\n${generateResult.stdout}\n`;
            }

            if (generateResult.stderr) {
                result += `\nErrors:\n${generateResult.stderr}\n`;
            }

            result += `\nPlease check the above output for details on the failure. If it complains missing Java environment, please ask for preparing environment.\n`;
        }

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
                    text: `Unexpected error during SDK generation: ${error instanceof Error ? error.message : String(error)}`,
                },
            ],
        };
    }
}
