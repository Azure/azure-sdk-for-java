import path from "path";
import { findAzureSdkRoot, findModuleDirectory } from "./utils/sdk-directory.js";
import { getJavaSdkChangelogJson } from "./java-sdk-changelog.js";
import { CallToolResult } from "@modelcontextprotocol/sdk/types";
import * as fs from "fs";

export async function updateChangelogMd(jarPath: string, groupId: string, artifactId: string): Promise<CallToolResult> {
    try {
        const repoRoot = await findAzureSdkRoot(path.dirname(jarPath));
        const moduleDirectoryPath = await findModuleDirectory(jarPath);
        const changelogMdPath = path.join(moduleDirectoryPath, "CHANGELOG.md");
        const changelogJson = await getJavaSdkChangelogJson(repoRoot, jarPath, groupId, artifactId);
        const changelogMd = changelogJson?.changelog;
        if (changelogMd) {
            const oldChangelog = await fs.promises.readFile(changelogMdPath, "utf8");
            const updatedChangelog = updateChangelogText(oldChangelog, changelogMd);
            await fs.promises.writeFile(changelogMdPath, updatedChangelog, "utf8");
        }
        return {
            content: [
                {
                    type: "text",
                    text: changelogMd
                        ? "✅ CHANGELOG.md updated successfully!"
                        : "CHANGELOG.md does not need to be updated, as no change was found.",
                },
            ],
        };
    } catch (error) {
        return {
            content: [
                {
                    type: "text",
                    text: `Unexpected error during SDK changelog: ${error instanceof Error ? error.message : String(error)}`,
                },
            ],
        };
    }
}

export function updateChangelogText(oldChangelog: string, newChangelog: string): string {
    const lines = oldChangelog.split("\n");
    const newChangelogLines = newChangelog.split("\n");
    const newLines: string[] = [];
    let foundFirstVersion = false;
    let foundFirstVersionSubSections = false;
    let foundSecondVersion = false;
    for (const line of lines) {
        if (!foundFirstVersion) {
            if (line.startsWith("## ")) {
                foundFirstVersion = true;
            }
            newLines.push(line);
        } else if (foundFirstVersion && !foundSecondVersion) {
            if (line.startsWith("## ")) {
                foundSecondVersion = true;
                newLines.push(line);
            } else if (!foundFirstVersionSubSections) {
                if (line.startsWith("### ")) {
                    foundFirstVersionSubSections = true;
                    newLines.push(...newChangelogLines);
                } else {
                    newLines.push(line);
                }
            }
        } else {
            newLines.push(line);
        }
    }
    return newLines.join("\n");
}
