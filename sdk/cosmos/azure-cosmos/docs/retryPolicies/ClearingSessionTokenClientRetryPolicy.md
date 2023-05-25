# ClearingSessionContainerClientRetryPolicy

# Background
- Nests an instance of `DocumentClientRetryPolicy` and runs through nested retries before executing its own.
- Used for handling `NOT_FOUND` / `READ_SESSION_NOT_AVAILABLE` (404/1002).
- Triggered at most once after which it simply lets through the `ShouldRetryResult` instance.
- Responsible for clearing / removing the session tokens associated with some collection / container.