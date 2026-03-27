# Http2ConnectionLifecycleTests — Network Delay Testing

## What This Tests

`Http2ConnectionLifecycleTests` validates that HTTP/2 parent TCP connections (NioSocketChannel) survive
stream-level ReadTimeoutExceptions triggered by real network delay. Uses Linux `tc netem` to inject
kernel-level packet delay inside a Docker container.

**Key invariant proven:** A real netty `ReadTimeoutException` on an `Http2StreamChannel` does NOT close
the parent `NioSocketChannel` — the connection pool reuses it for subsequent requests.

## Why Not SDK Fault Injection?

SDK `RESPONSE_DELAY` adds a `Mono.delay()` at the HTTP layer — bytes still flow normally on the wire.
Netty's `ReadTimeoutHandler` never fires because it monitors actual socket I/O, not application-layer delays.
Only `tc netem` creates real kernel-level packet delay that triggers the handler.

## Prerequisites

- Docker Desktop with Linux containers
- Docker memory: **8 GB+**
- A Cosmos DB account with thin client enabled
- Credentials in `sdk/cosmos/cosmos-v4.properties`:
  ```properties
  ACCOUNT_HOST=https://<account>.documents.azure.com:443/
  ACCOUNT_KEY=<primary-key>
  ```

## Build

```bash
cd sdk/cosmos

# Build SDK
mvn clean install -pl azure-cosmos,azure-cosmos-test,azure-cosmos-tests -am \
  -DskipTests -Dgpg.skip -Dcheckstyle.skip -Dspotbugs.skip \
  -Drevapi.skip -Dmaven.javadoc.skip -Denforcer.skip -Djacoco.skip

# Build Docker image
docker build -t cosmos-netem-test -f azure-cosmos-tests/Dockerfile.netem .

# Generate Linux classpath
mvn dependency:build-classpath -f azure-cosmos-tests/pom.xml -DincludeScope=test
# Convert Windows paths → Linux paths, save to azure-cosmos-tests/target/cp-linux.txt
```

## Run

```bash
cd sdk/cosmos

ACCOUNT_HOST=$(grep "^ACCOUNT_HOST" cosmos-v4.properties | cut -d: -f2- | tr -d ' ')
ACCOUNT_KEY=$(grep "^ACCOUNT_KEY" cosmos-v4.properties | cut -d: -f2- | tr -d ' ')

docker run --rm --cap-add=NET_ADMIN --memory 8g \
  -v "$(pwd):/workspace" \
  -v "$HOME/.m2:/root/.m2" \
  -e "ACCOUNT_HOST=$ACCOUNT_HOST" \
  -e "ACCOUNT_KEY=$ACCOUNT_KEY" \
  cosmos-netem-test bash -c '
    cd /workspace &&
    CP=$(cat azure-cosmos-tests/target/cp-linux.txt) &&
    java --add-opens java.base/java.lang=ALL-UNNAMED \
         --add-opens java.base/java.util=ALL-UNNAMED \
         --add-opens java.base/java.net=ALL-UNNAMED \
         --add-opens java.base/java.io=ALL-UNNAMED \
         --add-opens java.base/java.nio=ALL-UNNAMED \
         --add-opens java.base/java.util.concurrent=ALL-UNNAMED \
         --add-opens java.base/java.util.concurrent.atomic=ALL-UNNAMED \
         --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
         --add-opens java.base/sun.nio.cs=ALL-UNNAMED \
         --add-opens java.base/sun.security.action=ALL-UNNAMED \
         --add-opens java.base/sun.util.calendar=ALL-UNNAMED \
         -cp "$CP" \
         -DACCOUNT_HOST=$ACCOUNT_HOST \
         -DACCOUNT_KEY=$ACCOUNT_KEY \
         -DCOSMOS.THINCLIENT_ENABLED=true \
         -DCOSMOS.HTTP2_ENABLED=true \
         org.testng.TestNG /workspace/azure-cosmos-tests/src/test/resources/manual-http-network-fault-testng.xml \
         -verbose 2
  '
```

## tc netem Commands Used

### Add Global Delay

```bash
tc qdisc add dev eth0 root netem delay 8000ms
```

Delays ALL outbound packets by 8 seconds. This includes TCP SYN, data, ACKs.
The delay causes Netty's `ReadTimeoutHandler` to fire because the server's response
ACKs are delayed, stalling TCP flow from the application's perspective.

### Remove Delay

```bash
tc qdisc del dev eth0 root netem
```

Restores normal networking. Called in `@AfterMethod` and `@AfterClass` as safety net.

## Tests

| Test | What It Proves |
|------|---------------|
| `connectionReuseAfterRealNettyTimeout` | Parent NioSocketChannel survives ReadTimeoutException; recovery read uses same `parentChannelId` |
| `multiParentChannelConnectionReuse` | Under concurrent load (>30 streams), multiple parent channels are created and ALL survive timeout |
| `retryUsesConsistentParentChannelId` | Retry attempts (6s→6s→10s) use consistent parent channel(s); pool recovers post-delay |
| `connectionSurvivesE2ETimeoutWithRealDelay` | Parent survives when e2e timeout (7s) AND ReadTimeoutHandler both fire |
| `parentChannelSurvivesE2ECancelWithoutReadTimeout` | Parent survives when e2e cancel (3s) fires BEFORE ReadTimeoutHandler (6s) — stream RST only |

## Important Notes

- Tests run **sequentially** (`parallel="false" thread-count="1"`) — tc netem is interface-global
- `--cap-add=NET_ADMIN` is required for `tc` commands (Linux `CAP_NET_ADMIN` capability)
- Each test creates/closes its own client (`@BeforeMethod`/`@AfterMethod`) for connection pool isolation
- Delay cleanup runs in `finally` blocks AND `@AfterMethod` for reliability
