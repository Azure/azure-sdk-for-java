import { spawn, SpawnOptions } from "child_process";

export interface ProcessResult {
    stdout: string;
    stderr: string;
    exitCode: number;
    success: boolean;
}

export interface SpawnAsyncOptions extends SpawnOptions {
    /** Timeout in milliseconds. If not provided, no timeout is applied */
    timeout?: number;
}

/**
 * Spawns a process asynchronously and captures stdout/stderr
 * @param command The command to execute
 * @param args Arguments for the command
 * @param options Spawn options including optional timeout
 * @returns Promise that resolves with process result
 */
export async function spawnAsync(
    command: string,
    args: string[] = [],
    options: SpawnAsyncOptions = {},
): Promise<ProcessResult> {
    return new Promise((resolve) => {
        const { timeout, ...spawnOptions } = options;

        const child = spawn(command, args, {
            ...spawnOptions,
            stdio: ["pipe", "pipe", "pipe"],
        });

        let stdout = "";
        let stderr = "";
        let timeoutId: ReturnType<typeof globalThis.setTimeout> | null = null;
        let isTimedOut = false;

        // Set up timeout if specified
        if (timeout && timeout > 0) {
            timeoutId = globalThis.setTimeout(() => {
                isTimedOut = true;
                child.kill("SIGTERM");

                // If SIGTERM doesn't work, force kill after a short delay
                globalThis.setTimeout(() => {
                    if (!child.killed) {
                        child.kill("SIGKILL");
                    }
                }, 1000);
            }, timeout);
        }

        // Capture stdout
        if (child.stdout) {
            child.stdout.on("data", (data: Buffer) => {
                stdout += data.toString();
            });
        }

        // Capture stderr
        if (child.stderr) {
            child.stderr.on("data", (data: Buffer) => {
                stderr += data.toString();
            });
        }

        // Handle process completion
        child.on("close", (code: number | null) => {
            if (timeoutId) {
                globalThis.clearTimeout(timeoutId);
            }

            const exitCode = code ?? -1;
            const success = exitCode === 0 && !isTimedOut;

            const result: ProcessResult = {
                stdout: stdout.trim(),
                stderr: stderr.trim(),
                exitCode,
                success,
            };

            if (isTimedOut) {
                result.stderr = `Process timed out after ${timeout}ms\n${result.stderr}`;
            }

            resolve(result);
        });

        // Handle spawn errors
        child.on("error", (error: Error) => {
            if (timeoutId) {
                globalThis.clearTimeout(timeoutId);
            }

            const result: ProcessResult = {
                stdout: stdout.trim(),
                stderr: `Spawn error: ${error.message}\n${stderr.trim()}`,
                exitCode: -1,
                success: false,
            };

            resolve(result);
        });
    });
}

/**
 * Executes a shell command and returns the result
 * @param command Full command string (will be executed via shell)
 * @param options Spawn options including optional timeout
 * @returns Promise that resolves with process result
 */
export async function execAsync(command: string, options: SpawnAsyncOptions = {}): Promise<ProcessResult> {
    const isWindows = process.platform === "win32";
    const shell = isWindows ? "cmd.exe" : "/bin/sh";
    const shellFlag = isWindows ? "/c" : "-c";

    return spawnAsync(shell, [shellFlag, command], {
        ...options,
        shell: false, // We're manually handling shell execution
    });
}

/**
 * Utility function to run a command and throw if it fails
 * @param command The command to execute
 * @param args Arguments for the command
 * @param options Spawn options
 * @returns Promise that resolves with stdout on success, throws on failure
 */
export async function spawnAndThrow(
    command: string,
    args: string[] = [],
    options: SpawnAsyncOptions = {},
): Promise<string> {
    const result = await spawnAsync(command, args, options);

    if (!result.success) {
        const error = new Error(`Command failed: ${command} ${args.join(" ")}`) as Error & {
            stdout: string;
            stderr: string;
            exitCode: number;
        };
        error.stdout = result.stdout;
        error.stderr = result.stderr;
        error.exitCode = result.exitCode;
        throw error;
    }

    return result.stdout;
}

/**
 * Utility function to run a shell command and throw if it fails
 * @param command Full command string
 * @param options Spawn options
 * @returns Promise that resolves with stdout on success, throws on failure
 */
export async function execAndThrow(command: string, options: SpawnAsyncOptions = {}): Promise<string> {
    const result = await execAsync(command, options);

    if (!result.success) {
        const error = new Error(`Command failed: ${command}`) as Error & {
            stdout: string;
            stderr: string;
            exitCode: number;
        };
        error.stdout = result.stdout;
        error.stderr = result.stderr;
        error.exitCode = result.exitCode;
        throw error;
    }

    return result.stdout;
}
