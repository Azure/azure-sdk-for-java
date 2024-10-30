## Realtime client library

Trying to figure out how we can best integrate the realtime spec into a Java SDK. This document will serve as a documentation issues with the emitter, websocket usage, etc. (should there be any)

### Emitter

- See `RealtimeServerEvent` the `protected String eventId` member variable. Code gen set it as `private`, and duplicated in every child class. An example of this can be seen in class `RealtimeServerEventSessionUpdated`.
- Deserialization methods `fromJson`, in a problem that could be related to the above, generate twice the same code. See the `TODO` in place as example in `RealtimeServerEventSessionUpdated`, where one instance of `String eventId = null;` is commented out. This also forces code gen to us the constructor with a wrong set of argument, where there is one more `eventId` than necessary.
- In the case of `RealtimeResponseMessageItem`, we see that `String object` is requested as constructor parameter. In reality it is hardcoded string that is part of the parent class (in this case `RealtimeResponseItem`). In the TSP definition, all subclasses have the same value. The parent class value is passed down into all of the subclasses. Interestingly, `String object` is not documented as a constructor parameter. See the `TODO` in `RealtimeResponseMessageItem`.

### Websocket implementation

- Created subclasses for `ClientEndpointConfiguration` for `Azure` and `nonAzure` cases
- Added SDK specific fields in there
- Removed `clientUrlProvider` in favour of letting the `ClientEndpointConfiguration` handle this. This prevents me from supporting correctly `TokenCredentials`. TODO: implement something that returns a `Mono<>` with the request ready for connection.
- `SendMessageFailedException` is the `AzureException` that wraps the reception of a `RealtimeServerEventError`
- Websocket frame fragmentation is not handled in WebPubSub and Realtime API does send fragmented message: https://www.rfc-editor.org/rfc/rfc6455.html#section-5.4

### Remaining larger TODOs

- [x] `TokenCredential` type support for Azure. This should be easily accomplished by having a mechanism similar to `clientAccessUrlProvider`. The challenge in our case is that we need to also inject specific header values.
  - Solved by adding `AuthenticationProvider`
- [ ] Convenience layer that allows for message grouping by their type. We could have convenience layer wrapper types for specific event types. Grouping `RealtimeServerResponse` subtypes like (`created` and `done` for example).
- [ ] `ConnectionId` seems to be a WebPubSub specific concept. Adapt the async client to account for this.