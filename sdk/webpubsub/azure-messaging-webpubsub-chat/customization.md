# Web PubSub Chat SDK post-generation customization

Use this instruction after generating a Web PubSub Chat service SDK from the
TypeSpec project. Apply it in each language repository using that repository's
Azure SDK naming, API, testing, and pipeline conventions.

The generated REST operations and models remain generator-owned. Put the work
described here in customization, handwritten, or protocol-policy files. Do not
edit generated files unless the language generator explicitly requires a
checked-in generated output update. Regenerate before final validation and
verify that regeneration preserves the custom behavior.

The existing Web PubSub **service** SDK for the target language is the source of
truth for connection-string parsing, access-key authentication, reverse proxy
behavior, and client access token generation. Reuse or share its implementation
instead of creating a second incompatible implementation in the Chat SDK.

## 1. Establish the public client surface

Keep the generated Microsoft Entra ID credential support. Follow the same
language's existing Web PubSub service SDK convention for client creation,
including constructor or builder shape, overload ordering, credential names,
options placement, defaults, validation, and sync/async client organization.
Do not copy the .NET constructor shape into a language that uses a different
idiom. Add that language's conventional equivalents of these inputs:

- endpoint, hub, key credential
- endpoint, hub, key credential, client options
- connection string, hub
- connection string, hub, client options

Use the same public key credential type and connection-string entry point as the
same language's Web PubSub service SDK. Parse a connection string with the same
parser and rules as that SDK. At minimum, read `Endpoint` and `AccessKey`, honor
supported optional fields such as `Port`, and construct the key credential from
the parsed access key. Do not log or retain the original connection string after
parsing.

Validate endpoint, hub, credential, connection string, and options consistently
with the language's Azure SDK guidelines. Reject a null or empty hub. Options
omitted by the caller must receive the normal default instance.

The client must retain enough state to create an internal Web PubSub service
client with the same endpoint, hub, credential, and relevant options. This
internal client owns client access token generation described below.

## 2. Apply key credential authentication

Follow the target language's Web PubSub service SDK convention exactly. Prefer
sharing the service SDK policy or helper. If that is not possible because it is
internal, move the implementation to a shared source/module owned by the Web
PubSub service SDK, or port it without changing behavior.

For every Chat service REST request authenticated by key credential:

1. Create a short-lived JWT signed with the UTF-8 bytes of the current key.
2. Use the same signing algorithm and JWT builder as the Web PubSub service SDK.
3. Set `nbf`, `iat`, `exp`, and `aud` claims. The policy token lifetime is five
   minutes.
4. Set `Authorization: Bearer <jwt>`.
5. Read the key for every token creation, or invalidate cached key bytes when
   the credential is updated. Key rotation must work without recreating the
   client.

The `aud` claim is the complete original request URI, including scheme,
authority, path, and query string. It is not the reverse proxy URI.

Place the authentication policy at the same pipeline position as the Web PubSub
service SDK. Ensure any reverse proxy rewrite records the original audience
before authentication creates the JWT.

Microsoft Entra ID authentication continues to use the language's standard
bearer token policy and the scopes emitted for the Chat service.

## 3. Add reverse proxy support

Add the target language's conventional `reverseProxyEndpoint` client option.
The option must be set before client construction.

When configured, install one per-call policy that:

1. Captures the original request URI for authentication.
2. Replaces the request scheme and authority with the reverse proxy endpoint.
3. Preserves the original path and query without double escaping.
4. Sends the request to the rewritten URI.

Do not add duplicate policies when an option is assigned more than once. A
request through the proxy must still have an access-key JWT whose audience is
the original Web PubSub URI. Entra ID requests must use the proxy while retaining
their normal bearer token.

Copy the reverse proxy endpoint into the options used to construct the internal
Web PubSub service client. This propagation is required because Entra ID client
access token generation calls the service through that client. Do not silently
copy service name, API version, or other package-specific settings. Propagate
additional common options only when the target language can do so without
changing the inner client's service identity or duplicating policies.

## 4. Generate client access credentials by delegation

Expose the target language's idiomatic sync and async client access generation
API. Follow the Web PubSub service SDK's naming and result shape. For example,
some languages return a token response while the .NET Chat SDK exposes
`GetClientAccessUri` and returns a WebSocket URI containing `access_token`.

Provide an options type with:

- optional user ID
- token lifetime, defaulting to one hour

Delegate to the internal Web PubSub service client. Do not duplicate its token
generation or service call in the Chat SDK. Pass the caller's user ID and token
lifetime, no initial groups, the default Web PubSub client protocol, and these
two fixed Web PubSub data roles:

```text
webpubsub.getGroupState
webpubsub.setGroupState
```

These roles are service roles needed by a connected Chat client. They are not
Chat role names and are not caller-configurable.

With a connection string or key credential, the Web PubSub service SDK normally
signs the client token locally. With Microsoft Entra ID, it normally calls the
service's generate-client-token operation. Preserve those service SDK semantics,
including cancellation, errors, endpoint conversion from HTTP(S) to WS(S), and
reverse proxy routing for the service call.

## 5. Add built-in Chat constants

Expose constants using the target language's normal constant container or enum
pattern. Use these exact wire values.

Built-in roles:

| Name | Value |
| --- | --- |
| User normal | `user.normal` |
| Room member | `room.member` |
| Room operator | `room.operator` |

User permissions:

| Name | Value |
| --- | --- |
| Create room | `user.create_room` |
| Fetch all rooms | `user.fetch_all_rooms` |

Room permissions:

| Name | Value |
| --- | --- |
| Invite user | `room.invite` |
| Remove user | `room.remove_user` |
| Read history | `room.history` |
| Publish message | `room.publish_message` |

Do not expose planned permissions until the service supports them. In
particular, do not copy constants from an old API listing without checking the
handwritten source and current service behavior. Regenerate or update the
language's API surface artifact after adding these constants and verify that it
contains only supported values.

## 6. Unit test the customization

Add tests using the target language's Azure SDK test framework and mock
transport. Cover at least:

- null and empty validation for every new constructor or builder path
- connection-string parsing, including endpoint/port handling
- key credential requests contain a five-minute signed bearer JWT
- JWT `aud` equals the full original request URI
- updating the key credential changes subsequently generated JWTs
- reverse proxy requests use the proxy authority and preserve path and query
- reverse proxy plus key credential keeps the original URI as JWT audience
- reverse proxy plus Entra ID sends the normal bearer token to the proxy
- client access generation forwards user ID, expiration, no groups, and exactly
  the two required Web PubSub roles
- default client access lifetime is one hour
- sync and async client access APIs have equivalent behavior, where applicable
- key/connection-string access generation produces a WS(S) connection URL or
  the language-standard equivalent result
- Entra ID access generation delegates to the service operation and honors the
  reverse proxy endpoint
- every public role and permission constant has the exact expected wire value

Decode JWTs in tests and assert claims rather than checking only that a token is
present. Use fake credentials and endpoints in unit tests.

## 7. Add live and playback tests

Create a test project/suite that participates in the language repository's
record/playback framework. Provision an Azure Web PubSub resource with a system
identity and persistent storage suitable for Chat message history. Export the
endpoint and connection string under stable test environment variable names.
For consistency with the .NET reference, use:

```text
WPS_CHAT_ENDPOINT
WPS_CHAT_CONNECTION_STRING
```

Register the connection string as a secret and sanitize at least its
`AccessKey` value as Base64 secret data. Never commit a populated `.env` file,
credentials, deployed resource names tied to a developer, or raw access tokens.

Exercise both async and sync clients when the language supports both. Live tests
must cover:

- create, get, list, paginate, and delete roles
- create, get, and delete rooms
- get a room conversation and list messages
- empty message and member pages
- create, list, and delete room members
- create, get, and delete users
- generate a client access credential and verify its connection URL/result
- message send, list, update, and delete when the service and language client
  support creating a message

Use unique recorded IDs for mutable resources and clean up in `finally` or the
language equivalent. Cleanup must tolerate a resource that was not created or
was already deleted. Do not make a permanently skipped message test the only
coverage for message update/delete behavior; document the service limitation
and enable the scenario as soon as message creation is available.

If a WebSocket helper is needed, connect with subprotocol
`json.webpubsub.azure.v1` and send the room message as a `sendToGroup` request:

```json
{
  "type": "sendToGroup",
  "group": "<room-id>",
  "dataType": "json",
  "data": {
    "type": "text",
    "content": "<message-text>"
  }
}
```

Use a bounded timeout and wait for the service to persist the message before
asserting history. A test-only Chat client from another language may seed the
message when WebSocket handling is not practical, but pin its dependencies and
automate its invocation instead of requiring an undocumented manual step.

## 8. Record and upload test assets

Follow the target language repository's test-proxy setup. The required flow is:

1. Create the package's `assets.json` using that repository's language prefix
   and package path.
2. Deploy the test resources and set secret environment variables locally.
3. Run the complete live suite in record mode.
4. Inspect every recording for connection strings, access keys, bearer tokens,
   user data, and unstable values. Add sanitizers and record again if any secret
   remains.
5. Run the suite in playback mode without Azure credentials and verify that it
   passes.
6. Upload the recordings with the repository-supported test-proxy executable:

   ```text
   test-proxy push -a <package-path>/assets.json
   ```

7. Commit the updated `assets.json` tag. Do not commit the local `.assets`
   checkout or populated secret files.
8. Restore the new tag in a clean checkout and run playback once more.

Do not reuse the .NET assets tag in another language. Each language package owns
its own prefix, tag, recordings, and sanitizer configuration.

## 9. Write samples and README

Add executable, tested samples using the language repository's snippet system.
Include:

- connection string, key credential, and Microsoft Entra ID authentication
- client access credential generation
- creating a room and user, adding and listing a room member, and cleanup
- listing built-in roles and permissions
- creating, assigning, listing, and deleting a custom role
- reading paged message history
- updating and deleting a message when supported
- handling the language's standard Azure service exception

The package README must contain installation, prerequisites, authentication,
key concepts, short runnable examples, troubleshooting, and links to the package
samples. State clearly that the service client manages server-side Chat
resources while connected clients send real-time messages over WebSockets.

Before merging, validate every README link:

- Link only to package-manager pages that exist for the actual package name.
- Link only to identity packages and API references used by the sample code.
- Use relative sample links with exact path and filename casing.
- Use an official Azure Web PubSub documentation URL that resolves.
- Remove template links, placeholders, and references to samples or APIs that
  are not included in the package.
- Run the language repository's link checker when one is available.

Update the changelog/release notes with the added authentication methods,
resource operations, access generation, and built-in constants.

## 10. Configure build and live-test pipelines

Add the Chat package to the service area's package build/CI artifact list. Add a
live-test pipeline entry using the language repository's standard SDK test
template and configure:

- service directory `webpubsub`
- the Chat package/test project
- Public Azure cloud support unless another cloud is verified
- the package directory as a test-resource directory
- the Chat test resource template. The C# reference is located at
  `sdk/webpubsub/Azure.Messaging.WebPubSub.Chat/test-resources.bicep`; place the
  target language's copy at the equivalent package test-resource location
  expected by that repository's pipeline template
- secure injection of the endpoint and connection string
- test-proxy restore and playback in normal CI
- resource deployment, live execution, recording support, and cleanup in the
  live-test pipeline

Ensure service-level project discovery includes the Chat tests. If the service
repository supports conditional test exclusion, add a Chat-specific exclusion
property without excluding the tests by default.

## 11. C# reference map

Use these files in the .NET repository to compare behavior. Translate their
intent into the target language's conventions; do not translate C# mechanics
such as partial classes, linked compile items, or NUnit attributes literally.

| Concern | C# reference |
| --- | --- |
| Client constructors, inner service client, access URI delegation | `sdk/webpubsub/Azure.Messaging.WebPubSub.Chat/src/WebPubSubChatServiceClient.cs` |
| Reverse proxy option | `sdk/webpubsub/Azure.Messaging.WebPubSub.Chat/src/WebPubSubChatServiceClientOptions.cs` |
| Access options | `sdk/webpubsub/Azure.Messaging.WebPubSub.Chat/src/GetClientAccessTokenOptions.cs` |
| Built-in constants | `sdk/webpubsub/Azure.Messaging.WebPubSub.Chat/src/ChatRoles.cs`, `RoomPermissions.cs`, and `UserPermissions.cs` |
| Shared key authentication and proxy behavior | `sdk/webpubsub/Azure.Messaging.WebPubSub/src/Shared/` |
| Client project dependencies/shared source | `sdk/webpubsub/Azure.Messaging.WebPubSub.Chat/src/Azure.Messaging.WebPubSub.Chat.csproj` |
| Unit tests | `sdk/webpubsub/Azure.Messaging.WebPubSub.Chat/tests/WebPubSubChatServiceClientTests.cs` and `ChatRolesAndPermissionsTests.cs` |
| Live tests and test environment | `sdk/webpubsub/Azure.Messaging.WebPubSub.Chat/tests/WebPubSubChatServiceClientLiveTests.cs` and `WebPubSubChatTestEnvironment.cs` |
| WebSocket/test-client helpers | `sdk/webpubsub/Azure.Messaging.WebPubSub.Chat/tests/ChatWebSocketHelper.cs` and `tests/tools/` |
| Samples | `sdk/webpubsub/Azure.Messaging.WebPubSub.Chat/tests/Samples/` |
| Resource deployment and recordings | `sdk/webpubsub/Azure.Messaging.WebPubSub.Chat/test-resources.bicep` and `assets.json` |
| Package live-test pipeline | `sdk/webpubsub/Azure.Messaging.WebPubSub.Chat/tests.yml` |
| Service build and test discovery | `sdk/webpubsub/ci.yml`, `sdk/webpubsub/tests.yml`, and `sdk/webpubsub/service.projects` |
| Package documentation and release notes | `sdk/webpubsub/Azure.Messaging.WebPubSub.Chat/README.md` and `CHANGELOG.md` |

The C# working tree may contain work in progress. Treat handwritten source and
passing behavioral tests as authoritative. Do not copy stale API listings,
local `.env` content, ignored test behavior, or editor files.

## 12. Completion checks

The customization is complete only when all of the following pass:

- regenerate the SDK and confirm handwritten customizations remain intact
- format, lint, build, and run the package's unit tests
- run recorded tests in playback mode from a clean environment
- run the live suite against a deployed resource
- push recordings and verify the new assets tag restores
- regenerate and review the public API surface
- build every README and sample snippet
- validate README and sample links
- run the language repository's package checks and API compatibility checks
- run or queue both package CI and the Web PubSub Chat live-test pipeline

Review the final diff and confirm it contains no generated-file hand edits,
secrets, local `.env` values, `.assets` content, editor state, or unrelated
service changes.