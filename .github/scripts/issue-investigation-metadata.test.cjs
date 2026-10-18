// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

const assert = require("node:assert/strict");
const { readFileSync } = require("node:fs");
const path = require("node:path");
const { test } = require("node:test");

// Execute the literal MCP script, not a second implementation of the lookup.
const workflow = readFileSync(path.join(__dirname, "..", "workflows", "issue-investigation.md"), "utf8")
    .replace(/\r\n/g, "\n");
const toolStart = workflow.indexOf("\n  java_release_metadata:\n");
assert.notEqual(toolStart, -1);
const scriptStart = workflow.indexOf("\n    script: |\n", toolStart);
assert.notEqual(scriptStart, -1);
const lines = workflow.slice(scriptStart + "\n    script: |\n".length).split("\n");
const script = [];
for (const line of lines) {
    if (line.startsWith("      ")) {
        script.push(line.slice(6));
    } else if (!line.trim()) {
        script.push("");
    } else {
        break;
    }
}
const AsyncFunction = Object.getPrototypeOf(async function () {}).constructor;
const execute = new AsyncFunction("inputs", "fetch", "AbortSignal",
    "const { groupId, artifactId } = inputs;\n" + script.join("\n"));
const source = "https://raw.githubusercontent.com/Azure/azure-sdk/main/_data/releases/latest/java-packages.csv";
const coordinate = { groupId: "com.azure", artifactId: "azure-security-keyvault-secrets" };
const header = '"Package","GroupId","VersionGA","VersionPreview","Notes"';
const record = '"azure-security-keyvault-secrets","com.azure","4.11.2","","example"';
const csv = `${header}\n${record}\n`;
const maxBytes = 2 * 1024 * 1024;

function run(content = csv, input = coordinate) {
    return execute(input, async (url, options) => {
        assert.equal(url, source);
        assert.equal(options.method, "GET");
        assert.equal(options.redirect, "error");
        assert.equal(options.credentials, "omit");
        assert.equal(options.body, undefined);
        assert.equal(options.headers, undefined);
        assert.ok(options.signal instanceof AbortSignal);
        return new Response(content);
    }, AbortSignal);
}

test("returns compact published evidence, not the entire catalog", async () => {
    const result = await run();
    assert.deepEqual(result, {
        status: "found",
        ...coordinate,
        stableVersion: "4.11.2",
        previewVersion: null,
        source
    });
    assert.ok(JSON.stringify(result, null, 2).length < 500);
});

test("supports BOM, CRLF, escaped quotes, commas, multiline fields and no final newline", async () => {
    const content = `\uFEFF${header}\r\n` +
        '"other","com.azure","1.0.0","","quoted ""text"", with\r\na newline"\r\n' + record;
    assert.equal((await run(content)).stableVersion, "4.11.2");
});

test("uses header names rather than fixed column positions", async () => {
    const content = "VersionPreview,Notes,GroupId,Package,VersionGA\n" +
        ",example,com.azure,azure-security-keyvault-secrets,4.11.2";
    assert.equal((await run(content)).stableVersion, "4.11.2");
});

test("keeps a preview-only package distinct from GA", async () => {
    const content = header + '\n"azure-ai-agents-persistent","com.azure","","1.0.0-beta.2",""';
    const result = await run(content, { groupId: "com.azure", artifactId: "azure-ai-agents-persistent" });
    assert.equal(result.stableVersion, null);
    assert.equal(result.previewVersion, "1.0.0-beta.2");
});

test("represents absent publication versions explicitly", async () => {
    const result = await run(csv.replace('"4.11.2",""', '"NA",""'));
    assert.equal(result.status, "found");
    assert.equal(result.stableVersion, null);
    assert.equal(result.previewVersion, null);
});

test("matches both coordinate components exactly", async () => {
    const content = csv + record.replace('"com.azure"', '"com.azure.resourcemanager"')
        .replace('"4.11.2"', '"2.55.0"');
    assert.equal((await run(content)).stableVersion, "4.11.2");
    assert.equal((await run(content, { ...coordinate, groupId: "com.azure.resourcemanager" })).stableVersion, "2.55.0");
    assert.equal((await run(content, { ...coordinate, groupId: "COM.AZURE" })).status, "not_found");
});

test("does not limit group IDs to a namespace or reject legacy punctuation", async () => {
    for (const groupId of [
        "com.azure.spring", "com.azure.cosmos.spark", "com.azure.v2", "io.clientcore",
        "com.microsoft.azure", "com.microsoft.azure.profile_2019_03_01_hybrid",
        "com.microsoft.azure.batchai-2018-05-01", "com.azurenight.maven"
    ]) {
        const result = await run(csv.replace('"com.azure"', `"${groupId}"`), { ...coordinate, groupId });
        assert.equal(result.groupId, groupId);
        assert.equal(result.stableVersion, "4.11.2");
    }
});

test("unknown or URL-shaped input cannot change the fixed GET destination", async () => {
    for (const groupId of ["unknown", "https://example.invalid/private", "../../private", "__proto__"]) {
        assert.deepEqual(await run(csv, { ...coordinate, groupId }), { status: "not_found", source });
    }
});

test("trims surrounding input whitespace without modifying Maven case", async () => {
    assert.equal((await run(csv, {
        groupId: " com.azure ",
        artifactId: " azure-security-keyvault-secrets "
    })).stableVersion, "4.11.2");
});

test("rejects invalid coordinate arguments before making a request", async () => {
    for (const input of [{}, { ...coordinate, groupId: "" }, { ...coordinate, artifactId: " " },
        { ...coordinate, groupId: 42 }]) {
        await assert.rejects(execute(input, () => assert.fail("Unexpected fetch"), AbortSignal), /nonempty strings/);
    }
});

test("accepts identical duplicate version evidence but rejects conflicts", async () => {
    assert.equal((await run(csv + record)).stableVersion, "4.11.2");
    await assert.rejects(run(csv + record.replace("4.11.2", "4.11.3")), /Conflicting published versions/);
});

test("rejects malformed quoting, missing or duplicate headers, and incomplete matching rows", async () => {
    for (const content of [
        `${header}\n"unterminated`,
        `${header}\n${record}junk`,
        "Package,GroupId,VersionGA\nx,com.azure,1",
        "Package,GroupId,VersionGA,VersionGA,VersionPreview\nx,com.azure,1,2,",
        `${header}\n"azure-security-keyvault-secrets","com.azure"`
    ]) {
        await assert.rejects(run(content), /CSV|quoted|columns|incomplete/);
    }
});

test("surfaces HTTP failures rather than reporting a missing package", async () => {
    for (const status of [404, 429, 502]) {
        await assert.rejects(execute(coordinate, async () => new Response("failure", { status }), AbortSignal),
            new RegExp(`HTTP ${status}`));
    }
});

test("uses an enforced fetch deadline and propagates transport or timeout failures", async () => {
    const failure = new DOMException("Deadline exceeded", "TimeoutError");
    const signalProvider = {
        timeout(milliseconds) {
            assert.equal(milliseconds, 15000);
            return AbortSignal.abort(failure);
        }
    };
    await assert.rejects(execute(coordinate, async (url, options) => {
        throw options.signal.reason;
    }, signalProvider), error => error === failure);
    await assert.rejects(execute(coordinate, async () => {
        throw new Error("Connection reset");
    }, AbortSignal), /Connection reset/);
});

test("rejects oversized content lengths and streamed bodies", async () => {
    await assert.rejects(execute(coordinate, async () =>
        new Response(csv, { headers: { "content-length": String(maxBytes + 1) } }), AbortSignal), /2 MiB/);
    await assert.rejects(run(new Uint8Array(maxBytes + 1)), /2 MiB/);
});

test("accepts a response exactly at the byte limit", async () => {
    assert.equal((await run(csv + " ".repeat(maxBytes - Buffer.byteLength(csv)))).stableVersion, "4.11.2");
});

test("handles split UTF-8 chunks and rejects invalid UTF-8", async () => {
    const bytes = new TextEncoder().encode(csv.replace("example", "caf\u00e9"));
    const body = new ReadableStream({
        start(controller) {
            for (const byte of bytes) controller.enqueue(Uint8Array.of(byte));
            controller.close();
        }
    });
    assert.equal((await run(body)).stableVersion, "4.11.2");
    await assert.rejects(run(Uint8Array.of(0xff, 0xfe)), /encoded data|encoding/i);
});

test("surfaces empty responses instead of fabricating publication evidence", async () => {
    await assert.rejects(run(null), /no body/);
    await assert.rejects(run(""), /columns/);
});
