# ListBlobs Arrow golden fixtures

These `*.arrow.base64` files are committed golden fixtures for the vendored ListBlobs Arrow
deserializer tests (`BlobListArrowGoldenDecodeTests`, `BlobListArrowStreamReaderRejectionTests`).
Each one is an Apache Arrow IPC stream that has been **Base64-encoded** before being written to disk.

## Why Base64 instead of raw binary?

Storing the payloads as Base64 text is deliberate and safer for golden fixtures:

- **No line-ending / encoding corruption.** Raw binary committed on Windows can be mangled by
  git autocrlf or editor "fixes." Base64 is plain ASCII and survives git/editor round-trips
  byte-for-byte — which is exactly what a byte-exact golden fixture needs.
- **Diff- and review-friendly.** git treats raw `.arrow` blobs as opaque binary; Base64 is text,
  so it shows up as a normal blob in PRs.
- **No special tooling to read/regenerate.** The generator emits Base64 directly and the tests
  decode it inline (`Base64.getDecoder().decode(...)`), so no binary read mode or hex tooling is
  required.

The tests decode the Base64 back to the original Arrow bytes before parsing, so the on-disk
encoding is transparent to the deserializer under test.

## Naming

`<name>.arrow.base64` — `.arrow` denotes the Apache Arrow IPC stream; `.base64` denotes the
Base64 text wrapper.
