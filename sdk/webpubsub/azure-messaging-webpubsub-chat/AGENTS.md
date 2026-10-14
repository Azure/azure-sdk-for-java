# Web PubSub Chat development constraints

Before changing tests, recording tests, regenerating, or releasing this package,
read [KNOWN_ISSUES.md](KNOWN_ISSUES.md) for the pending upstream Java
generator paging fix. Do not add a paging customization; preserve strict
regression assertions and verify the upstream fix after regeneration.

Run live tests against a Chat-enabled resource in the TTL = 7 days subscription.
The server routing fix was verified in Central US EUAP on 2026-10-13 using the
complete sync/async live suite. All operations, including setup and cleanup,
use `2026-08-01-preview`; do not restore legacy-version clients or routing skips.
Upload sanitized recordings and verify clean-cache playback after test changes.

Before publishing an issue, PR, or team message for this package, show the user
the destination and exact draft and obtain explicit approval.
