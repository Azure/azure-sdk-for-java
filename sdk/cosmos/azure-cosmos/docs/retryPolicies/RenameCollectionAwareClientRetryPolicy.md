# RenameCollectionAwareClientRetryPolicy

## Background
- Retries on errors with `NOT_FOUND`/`READ_SESSION_NOT_AVAILABLE`(404/1002) scenarios.
- Nests an instance of `DocumentClientRetryPolicy` and runs through nested retries before executing its own.
- Clears the session tokens associated with the collection, resolves the collection in the `collectionCache`.
- If `RenameCollectionAwareClientRetryPolicy` can't resolve the collection, just return ShouldRetryResult instance as is. 
- If the collection can be resolved, then retry immediately.
- `RenameCollectionAwareClientRetryPolicy` executes collection resolution / session token clearance from the `sessionContainer` at most once.
