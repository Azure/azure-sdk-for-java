// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

const assert = require("node:assert/strict");
const { readFileSync } = require("node:fs");
const { execFileSync } = require("node:child_process");
const path = require("node:path");
const { test } = require("node:test");
const repo = path.resolve(__dirname, "..", "..");
const baseline = process.env.INVESTIGATION_BASE_SHA;
assert.match(baseline || "", /^[0-9a-f]{40}$/, "Set INVESTIGATION_BASE_SHA to the PR base commit");
const read = file => readFileSync(path.join(repo, file), "utf8").replace(/\r\n/g, "\n");

for (const file of [
    ".github/workflows/issue-triage.md",
    ".github/workflows/issue-triage.lock.yml",
    ".github/workflows/actionlint.yml"
]) {
    test(`existing workflow remains byte-identical apart from checkout line endings: ${file}`, () => {
        const original = execFileSync("git", ["show", `${baseline}:${file}`], { cwd: repo, encoding: "utf8" })
            .replace(/\r\n/g, "\n");
        assert.equal(read(file), original);
    });
}

test("handoff executes separately after triage and never checks out the triggering branch", () => {
    const workflow = read(".github/workflows/issue-investigation-handoff.yml");
    assert.match(workflow, /workflow_run:\n\s+workflows: \["Agentic Triage"\]\n\s+types: \[completed\]/);
    assert.match(workflow, /ref: \$\{\{ github\.sha \}\}/);
    assert.match(workflow, /persist-credentials: false/);
    assert.doesNotMatch(workflow, /pull_request_target:|secrets\.|workflow_run\.head_sha \}\}/);
    assert.doesNotMatch(workflow, /issues: write|contents: write/);
    assert.doesNotMatch(read(".github/workflows/issue-triage.lock.yml"), /issue-investigation|dispatch_workflow/);
});

const triage = read(".github/workflows/issue-triage.md");
const ownerScript = /            script: \|\n([\s\S]*?)\n\n(?:tools|jobs):/.exec(triage)[1]
    .split("\n").map(line => line.startsWith("              ") ? line.slice(14) : line).join("\n");
const AsyncFunction = Object.getPrototypeOf(async function () {}).constructor;
const notifyOwners = new AsyncFunction("require", "context", "github", "core", "process", ownerScript);

for (const recoveryFails of [false, true]) {
    test(`owner notification failure preserves legacy recovery (recoveryFails=${recoveryFails})`, async () => {
        const calls = [];
        let failed = false;
        const fs = {
            existsSync: () => true,
            readFileSync: () => JSON.stringify({ items: [{ type: "mention_owners", owners: "owner1", message: "Routing" }] })
        };
        await notifyOwners(name => {
            assert.equal(name, "fs");
            return fs;
        }, { repo: { owner: "example", repo: "java-sdk" }, issue: { number: 17 } },
        { rest: { issues: {
            createComment: async args => {
                calls.push({ operation: "comment", body: args.body });
                if (args.body.includes("//cc:")) throw new Error("Routing unavailable");
            },
            addLabels: async args => {
                calls.push({ operation: "recovery", labels: args.labels });
                if (recoveryFails) throw new Error("Recovery unavailable");
            }
        } } },
        { error() {}, info() {}, setFailed() { failed = true; } },
        { env: { GH_AW_AGENT_OUTPUT: "memory" } });
        assert.equal(failed, true);
        assert.ok(calls.some(call => call.operation === "recovery" && call.labels.includes("needs-team-triage")));
        const generated = read(".github/workflows/issue-triage.lock.yml").split("\n  safe_outputs:\n")[1];
        assert.ok(generated);
        assert.doesNotMatch(generated, /needs\.mention_owners|^\s+- mention_owners$/m);
        assert.match(generated, /needs\.detection\.result == 'success'/);
    });
}
