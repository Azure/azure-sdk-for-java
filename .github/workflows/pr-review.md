---
name: "sdkReviewAgent"
description: "AI-powered multi-agent deep PR review for Azure SDK for Java"
on:
  pull_request:
    types: [opened, synchronize, ready_for_review]
engine: copilot
network:
  allowed:
    - defaults
    - github
safe-outputs:
  create-pull-request-review-comment: {}
permissions:
  contents: read
  pull-requests: read
  issues: read
imports:
  - xinlian12/sdk-copilot-toolkit/.github/workflows/shared/pr-review-pipeline.md@reviewAgentEnhancements
---

{{#import xinlian12/sdk-copilot-toolkit/.github/workflows/shared/pr-review-pipeline.md@reviewAgentEnhancements}}
