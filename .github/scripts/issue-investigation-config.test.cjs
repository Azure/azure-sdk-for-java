// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

const assert = require("node:assert/strict");
const { readFileSync } = require("node:fs");
const path = require("node:path");
const { test } = require("node:test");

const workflowPath = path.join(__dirname, "..", "workflows", "issue-investigation");
const source = readFileSync(`${workflowPath}.md`, "utf8").replace(/\r\n/g, "\n");
const lock = readFileSync(`${workflowPath}.lock.yml`, "utf8").replace(/\r\n/g, "\n");
const target = "${{ github.event.inputs.issue_number }}";

function outputConfig(variable) {
    // The compiler emits these environment values as JSON inside a quoted YAML scalar.
    const matches = [...lock.matchAll(new RegExp(`^ +${variable}: ("(?:[^"\\\\]|\\\\.)*")$`, "gm"))];
    assert.equal(matches.length, 1, `Expected one ${variable} value`);
    return JSON.parse(JSON.parse(matches[0][1]));
}

test("service-side guidance uses the closing tool for the entire comment", () => {
    const serviceSide = source.split("### 4. Working as Designed or Service-Side\n")[1]
        .split("### 5. Actionable SDK Issue\n")[0];
    assert.match(serviceSide, /Call `close_issue` once.*complete investigation comment in `body`/);
    assert.match(serviceSide, /Do not call `add_comment` for this outcome/);
    assert.doesNotMatch(serviceSide, /[Aa]dd one comment/);
    assert.match(source, /For \*\*Service-side or by-design\*\*, put the complete comment in `close_issue\.body` only/);
});

for (const variable of ["GH_AW_SAFE_OUTPUTS_CONFIG", "GH_AW_SAFE_OUTPUTS_HANDLER_CONFIG"]) {
    test(`${variable} preserves bounded, issue-targeted outputs and the fixed closure reason`, () => {
        const config = outputConfig(variable);
        assert.deepEqual(config.add_comment, { max: 1, target });
        assert.deepEqual(config.close_issue, {
            max: 1,
            state_reason: "not_planned",
            target
        });
    });
}
