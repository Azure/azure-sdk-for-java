# Access Helpers


Access helpers are a pattern for exposing private and package-private members of classes **across package boundaries** without adding public APIs. This is useful when functionality must remain internal to a package but still needs to be accessible from other packages within the same library.

---

## Why Access Helpers?

The [Azure SDK design principles](https://azure.github.io/azure-sdk/general_introduction.html) require client library APIs to be consistent, approachable, and backward-compatible. This means public APIs should only expose what users need.

However, internal cross-package access is sometimes necessary. For example, `azure-core` needs to access `HttpHeaders`' internal backing map to strip headers from logs without exposing that map as a public API.

---

## Design

Access helpers live in `implementation.accesshelpers` packages so they can be found in one place.

### Naming Conventions

| Type | Name Pattern | Example |
|------|-------------|---------|
| Access helper class | `<ClassName>AccessHelper` | `HttpHeadersAccessHelper` |
| Accessor interface (inner type) | `<ClassName>Accessor` | `HttpHeadersAccessor` |

### Structure

**AccessHelper class** (`implementation/accesshelpers/HttpHeadersAccessHelper.java`):

```java
public final class HttpHeadersAccessHelper {
    // Non-final static: set once by the static initializer in HttpHeaders
    private static HttpHeadersAccessor accessor;

    // Must be public so HttpHeaders (another package) can implement it
    public interface HttpHeadersAccessor {
        Map<String, HttpHeader> getBackingMap(HttpHeaders httpHeaders);
        HttpHeaders internalCreate(Map<String, HttpHeader> internalMap);
    }

    public static void setAccessor(HttpHeadersAccessor accessor) {
        HttpHeadersAccessHelper.accessor = accessor;
    }

    public static Map<String, HttpHeader> getBackingMap(HttpHeaders httpHeaders) {
        return accessor.getBackingMap(httpHeaders);
    }

    public static HttpHeaders internalCreate(Map<String, HttpHeader> internalMap) {
        if (accessor == null) {
            // Force class loading if accessor is null (can happen for constructors)
            new HttpHeaders(); // public constructor dummy call
            // If no public constructor exists, use:
            // Class.forName(HttpHeaders.class.getName(), true,
            //     HttpHeadersAccessHelper.class.getClassLoader());
        }
        assert accessor != null; // required: SpotBugs will error otherwise
        return accessor.internalCreate(internalMap);
    }

    private HttpHeadersAccessHelper() { }
}
```

**Target class** (`HttpHeaders.java`) — add a static initializer:

```java
public class HttpHeaders {
    static {
        HttpHeadersAccessHelper.setAccessor(new HttpHeadersAccessHelper.HttpHeadersAccessor() {
            @Override
            public Map<String, HttpHeader> getBackingMap(HttpHeaders httpHeaders) {
                return httpHeaders.backingMap; // direct field access
            }

            @Override
            public HttpHeaders internalCreate(Map<String, HttpHeader> internalMap) {
                return new HttpHeaders(internalMap); // package-private constructor
            }
        });
    }
}
```

---

## Rules of Thumb

1. **One access helper per class** — keeps helpers simple and focused.
2. **Package**: Always place in `implementation.accesshelpers`.
3. **Thread safety**: The static initializer sets the accessor exactly once on class load; no synchronization needed.
4. **Constructor access**: If you need to invoke constructors before the class is loaded (circular), force class loading either via a dummy constructor call (preferred when a public constructor exists) or via `Class.forName(...)`.
5. **SpotBugs**: Always add `assert accessor != null;` before using `accessor` — SpotBugs cannot prove it is set.

---

## When NOT to Use Access Helpers

- When a public API can be added without violating design principles — prefer that.
- When the caller and callee are in the same package — use package-private visibility directly.
- In test code — use reflection or package-private visibility instead.

---

## See Also

- [Adding a Module](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/adding-a-module.md)
- [Code Quality](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/code-quality.md)
