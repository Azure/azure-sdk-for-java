import { describe, it, expect } from "vitest";
import { updateChangelogText } from "./update-changelog-md.js";

describe("updateChangelogText", () => {
    it("should be defined", () => {
        expect(updateChangelogText).toBeDefined();
        expect(typeof updateChangelogText).toBe("function");
    });

    it("should insert new changelog content after the first version subsection", () => {
        const oldChangelog = `# Changelog

## 1.2.0 (2025-01-01)

### Features Added
- Old feature 1
- Old feature 2

### Breaking Changes
- Old breaking change

## 1.1.0 (2024-12-01)

### Features Added
- Previous feature`;

        const newChangelog = `### Features Added
- New feature 1
- New feature 2

### Bug Fixes
- Fixed issue 1`;

        const result = updateChangelogText(oldChangelog, newChangelog);

        expect(result).toContain("New feature 1");
        expect(result).toContain("New feature 2");
        expect(result).toContain("Fixed issue 1");
        expect(result).toContain("Previous feature");
        expect(result).not.toContain("Old feature 1");
        expect(result).not.toContain("Old feature 2");
        expect(result).not.toContain("Old breaking change");
    });

    it("should preserve content before the first version", () => {
        const oldChangelog = `# Changelog

Some description text
More description

## 1.2.0 (2025-01-01)

### Features Added
- Old feature`;

        const newChangelog = `### Features Added
- New feature`;

        const result = updateChangelogText(oldChangelog, newChangelog);

        expect(result).toContain("# Changelog");
        expect(result).toContain("Some description text");
        expect(result).toContain("More description");
        expect(result).toContain("New feature");
        expect(result).not.toContain("Old feature");
    });

    it("should handle changelog with no existing subsections", () => {
        const oldChangelog = `# Changelog

## 1.2.0 (2025-01-01)

## 1.1.0 (2024-12-01)`;

        const newChangelog = `### Features Added
- New feature 1`;

        const result = updateChangelogText(oldChangelog, newChangelog);

        // Should not insert anything since there are no subsections in the first version
        expect(result).toBe(oldChangelog);
    });

    it("should handle changelog with only one version", () => {
        const oldChangelog = `# Changelog

## 1.0.0 (2025-01-01)

### Features Added
- Initial feature`;

        const newChangelog = `### Features Added
- New feature
- Another feature`;

        const result = updateChangelogText(oldChangelog, newChangelog);

        expect(result).toContain("- New feature");
        expect(result).toContain("- Another feature");
    });

    it("should handle multiline new changelog content", () => {
        const oldChangelog = `# Changelog

## 1.2.0 (2025-01-01)

### Features Added
- Old feature`;

        const newChangelog = `### Features Added
- Multi-line feature description
  that spans multiple lines
- Another feature

### Bug Fixes
- Fixed critical bug
- Fixed minor issue`;

        const result = updateChangelogText(oldChangelog, newChangelog);

        expect(result).toContain("Multi-line feature description");
        expect(result).toContain("  that spans multiple lines");
        expect(result).toContain("Fixed critical bug");
        expect(result).toContain("Fixed minor issue");
    });

    it("should not update if changelog is not well-formed", () => {
        const oldChangelog = `# Changelog

Description but not version`;

        const newChangelog = `### Features Added
- New feature
- Another feature`;

        const result = updateChangelogText(oldChangelog, newChangelog);

        expect(result).toContain("Description but not version");
        expect(result).not.toContain("- New feature");
    });
});
