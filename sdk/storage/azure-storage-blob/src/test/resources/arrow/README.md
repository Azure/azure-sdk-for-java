# ListBlobs Arrow golden fixtures

These `*.arrow.base64` files are committed golden fixtures for the vendored ListBlobs Arrow
deserializer tests (`BlobListArrowGoldenDecodeTests`, `BlobListArrowStreamReaderRejectionTests`).
Each one is an Apache Arrow IPC stream that has been **Base64-encoded** before being written to disk.

The tests decode the Base64 back to the original Arrow bytes before parsing, so the on-disk
encoding is transparent to the deserializer under test.

## Naming

`<name>.arrow.base64` — `.arrow` denotes the Apache Arrow IPC stream; `.base64` denotes the
Base64 text wrapper.
