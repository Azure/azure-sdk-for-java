# Release History

## 1.0.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

- Upgraded Netty to `4.2.17.Final`, using I/O handler factories for event loops while preserving pooled buffer allocation
  unless `io.netty.allocator.type` is explicitly configured.
- Preserved HTTP/2 response decompression with Netty's separate compression module when using Java modules.
