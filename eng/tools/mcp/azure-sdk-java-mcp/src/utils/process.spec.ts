import { describe, it, expect } from "vitest";
import { spawnAsync, ProcessResult } from "./process.js";

describe("process utilities", () => {
    describe("spawnAsync", () => {
        it("should be defined", () => {
            expect(spawnAsync).toBeDefined();
            expect(typeof spawnAsync).toBe("function");
        });

        it("should handle successful command execution", () => {
            return spawnAsync("echo", ["hello world"]).then((result: ProcessResult) => {
                expect(result.success).toBe(true);
                expect(result.stdout.trim()).toBe("hello world");
                expect(result.stderr).toBe("");
                expect(result.exitCode).toBe(0);
            });
        });

        it("should handle successful tsp-client execution", () => {
            return spawnAsync("tsp-client", ["--help"], { shell: true }).then((result: ProcessResult) => {
                expect(result.success).toBe(true);
                expect(result.stdout.trim()).toContain("tsp-client");
                expect(result.stderr).toBe("");
                expect(result.exitCode).toBe(0);
            });
        });
    });
});
