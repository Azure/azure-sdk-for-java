// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

const assert = require("node:assert/strict");
const { readFileSync } = require("node:fs");
const path = require("node:path");
const { test } = require("node:test");
const workflowPath = path.join(__dirname, "..", "workflows", "issue-investigation");
const source = readFileSync(`${workflowPath}.md`, "utf8").replace(/\r\n/g, "\n");
const lock = readFileSync(`${workflowPath}.lock.yml`, "utf8").replace(/\r\n/g, "\n");
const start = source.indexOf("\n  validate_input:\n");
assert.notEqual(start, -1, "Expected deterministic input validation");
const scriptStart = source.indexOf("\n          script: |\n", start);
assert.notEqual(scriptStart, -1);
const lines = source.slice(scriptStart + "\n          script: |\n".length).split("\n");
const script = [];
for (const line of lines) {
    if (line.startsWith("            ")) script.push(line.slice(12));
    else if (!line.trim()) script.push("");
    else break;
}
const AsyncFunction = Object.getPrototypeOf(async function () {}).constructor;
const validate = new AsyncFunction("core", "process", script.join("\n"));

async function run(input) {
    const outputs = {};
    const notices = [];
    const summaries = [];
    const summary = {
        addHeading(text) { summaries.push(text); return this; },
        addRaw(text) { summaries.push(text); return this; },
        async write() {}
    };
    await validate({
        setOutput: (key, value) => { outputs[key] = value; },
        notice: text => notices.push(text),
        summary
    }, { env: { ISSUE_NUMBER: input } });
    return { outputs, notices, summaries };
}

test("preflight accepts exact positive issue numbers within the JSON integer range", async () => {
    for (const input of ["1", "17", "50606", "2147483647", "9007199254740991"]) {
        const result = await run(input);
        assert.equal(result.outputs.valid, "true", input);
        assert.deepEqual(result.notices, []);
        assert.deepEqual(result.summaries, []);
    }
});

test("preflight rejects malformed input with an explicit local noop", async () => {
    for (const input of [undefined, null, 17, true, "", "0", "-1", "+1", "01", "1e3", "17junk",
        "1.0", "17\n", " 17", "17 ", "9007199254740992", "9".repeat(400), "\u0661",
        "$(echo test)", "${{ secrets.GITHUB_TOKEN }}"]) {
        const result = await run(input);
        assert.equal(result.outputs.valid, "false", String(input));
        assert.equal(result.notices.length, 1);
        assert.match(result.notices[0], /INVESTIGATION_PREFLIGHT: noop/);
        assert.ok(result.summaries.some(text => text.includes("**Outcome:** No action")));
        if (input !== "") assert.ok(result.notices.every(text => !text.includes(String(input))));
    }
});

test("generated activation waits for preflight without disabling native detection", () => {
    const activation = lock.split("\n  activation:\n")[1].split("\n  agent:\n")[0];
    assert.match(activation, /^    needs: validate_input$/m);
    assert.match(activation, /if:.*needs\.validate_input\.outputs\.valid == 'true'/);
    assert.match(lock, /\n  validate_input:\n/);
    const preflight = lock.split("\n  validate_input:\n")[1].split(/\n  [A-Za-z_][A-Za-z0-9_-]*:\n/)[0];
    assert.match(preflight, /permissions: \{\}/);
    assert.doesNotMatch(preflight, /\bneeds:/);
    assert.match(preflight, /valid: \$\{\{ steps\.validate\.outputs\.valid \}\}/);
    assert.doesNotMatch(preflight, /secrets\.|github\.rest|issues: write|contents: write/);
    assert.match(lock, /\n  detection:\n/);
    assert.match(lock, /Parse and conclude threat detection/);
    assert.match(lock, /GH_AW_DETECTION_CONTINUE_ON_ERROR: "true"/);
    assert.match(lock, /Log detection run/);
});

test("skipped activation cannot reach detection, safe outputs, or native reporting", () => {
    const needs = {
        validate_input: { result: "success", outputs: { valid: "false" } },
        activation: { result: "skipped", outputs: {} },
        agent: { result: "skipped", outputs: {} },
        detection: { result: "skipped", outputs: {} },
        safe_outputs: { result: "skipped", outputs: {} }
    };
    for (const name of ["activation", "detection", "safe_outputs", "conclusion"]) {
        const block = lock.split(`\n  ${name}:\n`)[1].split(/\n  [A-Za-z_][A-Za-z0-9_-]*:\n/)[0];
        const match = /^    if: ([^\n]*)(?:\n((?:      [^\n]*\n)*))?/m.exec(block);
        assert.ok(match, `${name} must retain its job condition`);
        const expression = match[1] === ">" ? match[2].trim() : match[1];
        const permits = Function("needs", "always", "cancelled", `return (${expression});`);
        assert.equal(permits(needs, () => true, () => false), false, `${name} must not run on invalid input`);
    }
    const agent = lock.split("\n  agent:\n")[1].split(/\n  [A-Za-z_][A-Za-z0-9_-]*:\n/)[0];
    assert.match(agent, /^    needs:\n      - activation\n      - validate_input$/m);
    const condition = /^    if: ([^\n]*)/m.exec(agent)[1];
    assert.doesNotMatch(condition, /always\(|failure\(|cancelled\(|success\(/,
        "The agent must preserve GitHub's implicit successful-dependency gate");
});

test("duplicate evidence cannot consist only of shared missing diagnostics", () => {
    const duplicate = source.split("### 2. Duplicate\n")[1].split("### 3. Insufficient Context\n")[0];
    assert.match(duplicate, /Two incomplete reports do not establish a duplicate/);
    assert.match(duplicate, /exact source\/documentation defect/);
    assert.match(duplicate, /A previous automated duplicate assertion is not independent confirmation/);
    assert.match(duplicate, /continue to the Insufficient Context rule/);
});
