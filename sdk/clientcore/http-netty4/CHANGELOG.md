# Release History

## 1.0.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

- Fixed response and read timeouts not being enforced when the write timeout is disabled.

### Other Changes

- Upgraded Netty to `4.2.19.Final`, using I/O handler factories for event loops while preserving pooled buffer allocation
  unless `io.netty.allocator.type` is explicitly configured.
- Preserved HTTP/2 response decompression with Netty's separate compression module when using Java modules.
