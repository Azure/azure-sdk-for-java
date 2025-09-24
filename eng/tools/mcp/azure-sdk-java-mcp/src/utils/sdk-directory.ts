import path from "path";
import * as fs from "fs";

export async function findAzureSdkRoot(dir: string): Promise<string> {
    // Check if the input directory itself is the Azure SDK root
    if (await isAzureSdkRootDir(dir)) {
        return dir;
    }

    // find from the input directory's parent dir, until we find the azure-sdk-for-java root path
    let currentDir = path.dirname(dir);

    while (currentDir !== path.dirname(currentDir)) {
        if (await isAzureSdkRootDir(currentDir)) {
            return currentDir;
        }
        const parentDir = path.dirname(currentDir);
        if (parentDir === currentDir) {
            break; // Reached the root directory
        }
        currentDir = parentDir;
    }
    throw new Error(`Azure SDK root not found from module directory: ${dir}`);
}

async function isAzureSdkRootDir(dir: string): Promise<boolean> {
    // pom.xml, sdk and eng directories are expected at the root of the Azure SDK for Java
    return (
        (await checkFileExistence(path.join(dir, "pom.xml"))) &&
        (await checkFileExistence(path.join(dir, "sdk"))) &&
        (await checkFileExistence(path.join(dir, "eng")))
    );
}

export async function findModuleDirectory(dir: string): Promise<string> {
    let currentDir = path.dirname(dir);
    while (currentDir !== path.dirname(currentDir)) {
        // pom.xml, src directory are expected at the directory of the Azure SDK for Java module
        if (
            (await checkFileExistence(path.join(currentDir, "pom.xml"))) &&
            (await checkFileExistence(path.join(currentDir, "src")))
        ) {
            return currentDir;
        }
        const parentDir = path.dirname(currentDir);
        if (parentDir === currentDir) {
            break; // Reached the root directory
        }
        currentDir = parentDir;
    }
    throw new Error(`Azure SDK module not found from source: ${dir}`);
}

export async function checkFileExistence(filePath: string): Promise<boolean> {
    try {
        await fs.promises.stat(filePath);
        return true;
    } catch (error) {
        if (error) {
            return false;
        } else {
            return false;
        }
    }
}
