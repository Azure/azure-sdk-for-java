# Web PubSub Chat SDK known generator issues

## Typed asynchronous paging ignores the requested page size

- **Tracking issue:** [microsoft/typespec#12149](https://github.com/microsoft/typespec/issues/12149).
- **Status:** Awaiting an upstream Java generator fix; reproduced with
  `@azure-tools/typespec-java` 0.48.0 on 2026-10-09.
- **User decision:** Do not add a paging customization or manually repair
  generated SDK code. Wait for the generator team to fix the implementation.

The typed asynchronous paged convenience methods accept `pageSizeParam` but
discard it when delegating to `PagedFlux<BinaryData>`. For example,
`asyncClient.listRoomsForUser("alice").byPage(1)` does not send `maxpagesize=1`.
Default continuation-token paging still works; the page-size override is
ignored. Synchronous typed paging forwards the page size correctly.

This affects both existing operations (`listRoles`, `listRoomMembers`,
`listMessages`) and the new operations (`listRooms`, `listTopics`, `listUsers`,
`listRoomsForUser`). The defect predates this package's August API upgrade; the
first affected generator release has not been determined.

### Approved temporary test workaround

The user approved disabling only
`WebPubSubChatPaginationTests.asyncPageSizeReachesFirstAndNextRequests`; its
seven strict cases and assertions remain intact. New async live pagination tests
use the existing protocol overloads, such as
`asyncClient.listTopics(roomId, new RequestOptions()).byPage(1)`, and still assert
one item per page and follow continuation requests. Default typed async
collection/model coverage remains enabled. No production paging customization
or generated-code repair is applied.

The mock suite also verifies first and next request page sizes for all seven
protocol async list methods. Passing these tests does not mean the typed async
generator defect is fixed.

The implementation belongs to the shared
[Java generator in microsoft/typespec](https://github.com/microsoft/typespec/blob/main/packages/http-client-java/generator/http-client-generator-core/src/main/java/com/microsoft/typespec/http/client/generator/core/template/ConvenienceAsyncMethodTemplate.java).
The original [Azure/typespec-azure#5657](https://github.com/Azure/typespec-azure/issues/5657)
was filed in the wrong repository and is closed.

### Checking whether it has been fixed

1. Check the tracking issue and the version of `@azure-tools/typespec-java` in
   `eng/emitter-package.json`. An issue being closed does not prove that the
   installed emitter includes the fix.
2. After the repository adopts a generator release containing the fix,
   regenerate the package using its pinned `tsp-location.yaml`. Do not bypass
   the issue by adding a customization.
3. Remove the issue-linked `@Disabled` annotation from
   `asyncPageSizeReachesFirstAndNextRequests`, then run the strict,
   credential-free regression tests:

   ```powershell
   mvn -f sdk\webpubsub\azure-messaging-webpubsub-chat\pom.xml test "-Dtest=WebPubSubChatPaginationTests" "-Dgpg.skip=true" "-Drevapi.skip=true"
   ```

   `WebPubSubChatPaginationTests` covers all seven sync and async list operations.
   It asserts `maxpagesize=1` on **both first-page and continuation requests**,
   and checks that the corrected `listRoomsForUser` continuation overload sends
   a query parameter. The original 16-case reproduction produced seven async
   failures and nine passes. The protocol async cases were added for the
   approved test workaround. Do not weaken the assertions or accept skipped async
   page-size cases as evidence of a fix.
4. Verify the new sync/async live pagination scenarios, then verify sanitized
   recording playback using the persisted `assets.json` tag. Check actual page
   sizes and continuation requests, not only whether a non-empty page exists.
5. Only mark this issue resolved after the generated code and all strict
   regression cases pass. Switch async live page-size checks back to the typed
   overloads and remove the temporary test workaround at that point.

### Server routing fix is independent

The server routing issue was verified fixed in Central US EUAP on 2026-10-13:
all 14 sync/async live scenarios passed with `2026-08-01-preview`, including
existing operations and setup/cleanup. The routing workaround and its temporary
skill have been removed. This does not fix the typed async paging generator
defect; retain its strict regression cases and the protocol paging workaround.
