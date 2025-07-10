import { CallToolResult } from "@modelcontextprotocol/sdk/types";
import { findAzureSdkRoot, spawnAsync } from "./utils/index.js";

export async function buildJavaSdk(
    rootDir: string,
    moduleDirectory: string,
    groupId: string,
    artifactId: string,
): Promise<CallToolResult> {
    try {
        // make sure it is the root directory of the Azure SDK for Java
        rootDir = await findAzureSdkRoot(rootDir);
        process.chdir(rootDir);

        const mvnCmd = process.platform === "win32" ? "mvn.cmd" : "mvn";

        // Run the Java SDK generation command
        const generateResult = await spawnAsync(
            mvnCmd,
            [
                "--no-transfer-progress",
                "clean",
                "package",
                "-f",
                moduleDirectory + "/pom.xml",
                "-Dmaven.javadoc.skip",
                "-Dcodesnippet.skip",
                "-Dgpg.skip",
                "-Drevapi.skip",
                "-pl",
                groupId + ":" + artifactId,
                "-am",
            ],
            {
                cwd: process.cwd(),
                shell: true, // Use shell to allow tsp-client command
                timeout: 600000, // 10 minute timeout
            },
        );

        let result = `Java SDK Build Results:\n\n`;

        if (generateResult.success) {
            result += `✅ SDK build completed successfully!\n\n`;
        } else {
            result += `❌ SDK build failed with exit code ${generateResult.exitCode}\n\n`;

            if (generateResult.stdout) {
                result += `Output:\n${generateResult.stdout}\n`;
            }

            if (generateResult.stderr) {
                result += `\nErrors:\n${generateResult.stderr}\n`;
            }
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
                    text: `Unexpected error during SDK build: ${error instanceof Error ? error.message : String(error)}`,
                },
            ],
        };
    }
}
