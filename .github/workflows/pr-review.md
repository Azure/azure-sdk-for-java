---
name: "sdkReviewAgent"
description: "AI-powered multi-agent PR review for Azure SDK for Java"
on:
  slash_command: sdkReviewAgent
engine: copilot
safe-outputs:
  create-pull-request-review-comment: {}
permissions:
  contents: read
  pull-requests: read
  issues: read
imports:
  - xinlian12/sdk-auto-pr-review/.github/workflows/shared/pr-review-pipeline.md@main
---

{{#import xinlian12/sdk-auto-pr-review/.github/workflows/shared/pr-review-pipeline.md@main}}
