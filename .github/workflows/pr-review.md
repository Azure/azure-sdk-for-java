---
name: "sdkReviewAgent"
description: "AI-powered multi-agent PR review for Azure SDK for Java"
on:
  pull_request:
    types: [opened, synchronize, ready_for_review]
engine: copilot
safe-outputs:
  add-comment: {}
  create-pull-request-review-comment: {}
permissions:
  contents: read
  pull-requests: read
  issues: read
imports:
  - xinlian12/sdk-auto-pr-review/.github/workflows/shared/pr-review-pipeline.md@main
---

{{#import xinlian12/sdk-auto-pr-review/.github/workflows/shared/pr-review-pipeline.md@main}}
