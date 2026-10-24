# Web PubSub Chat generator regression coverage

## Workaround: lifecycle visibility and requiredness in Java documentation

- **Tracking issue:** [Azure/typespec-azure#5721](https://github.com/Azure/typespec-azure/issues/5721).
- The Java customization corrects request schemas for all seven write operations
  on the sync, async, and implementation clients. Read-only properties are
  excluded from request bodies, PATCH properties are optional, and response
  properties retain their requiredness.
- Topic state is optional on initial creation and can be supplied for updates.
  PUT uses Create/Update visibility; this workaround does not exclude state
  from PUT serialization.
- `customization/src/test/java/WebPubSubChatDocumentationTests.java` checks the
  checked-in schemas, genuine required and optional fields, idempotence, and
  preservation of executable code. Run it with
  `mvn -f sdk/webpubsub/azure-messaging-webpubsub-chat/customization/pom.xml test`.

This is a documentation-only workaround. Shared models, constructors, partial
PATCH serialization, public APIs, and service validation are unchanged.

## Resolved: typed asynchronous page-size overrides

- **Tracking issue:** [microsoft/typespec#12149](https://github.com/microsoft/typespec/issues/12149).
- **Upstream fix:** [microsoft/typespec#12163](https://github.com/microsoft/typespec/pull/12163).
- **Verified emitter:** `@azure-tools/typespec-java` 0.48.1, adopted by the shared
  repository configuration through [Azure/azure-sdk-for-java#50789](https://github.com/Azure/azure-sdk-for-java/pull/50789).
- **Verification:** Regenerated from the pinned `tsp-location.yaml` on
  2026-10-14. All 59 tests passed with no skips using an independently restored
  recording cache, including all seven typed async page-size regression cases.

The previous generator discarded `pageSizeParam` when converting protocol
`PagedFlux<BinaryData>` responses to typed responses. The corrected generator
forwards page size on both first-page and continuation requests while preserving
default paging. No paging customization or manual generated-code fix is used.

Keep `WebPubSubChatPaginationTests` enabled after future regeneration. Its
23 cases cover all seven sync, typed async, and protocol async list operations,
plus the sync/async `listRoomsForUser` continuation overload. The strict cases
assert `maxpagesize=1` on both requests and check the continuation token.

The async live scenarios and samples now use typed paging directly. Their
request shapes remain compatible with the sanitized August API recordings;
do not weaken assertions or rewrite recordings to hide a paging regression.

## Resolved: server routing

All 14 sync/async live scenarios passed in Central US EUAP on 2026-10-13 with
`2026-08-01-preview`, including existing operations and setup/cleanup. The
legacy-version workaround, routing skips, and temporary skill were removed.
