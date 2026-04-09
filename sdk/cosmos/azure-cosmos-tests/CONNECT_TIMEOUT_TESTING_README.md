# Http2ConnectTimeoutBifurcationTests — Connect Timeout Testing

## What This Tests

`Http2ConnectTimeoutBifurcationTests` validates that the TCP connect timeout (`CONNECT_TIMEOUT_MILLIS`) is
correctly bifurcated between Gateway V1 metadata (45s) and Gateway V2 thin client data plane (5s default).
Uses Linux `iptables` to DROP SYN packets and `tc netem` with `iptables mangle` for per-port delay.

**Key invariant proven:** Thin client data plane requests fail fast (5s connect timeout) while
metadata requests on port 443 remain unaffected (45s timeout).

## Why Not SDK Fault Injection?

SDK `CONNECTION_DELAY` delays at the HTTP/Mono layer — the TCP handshake already completed.
`CONNECT_TIMEOUT_MILLIS` fires at the TCP SYN→SYN-ACK layer, which requires blocking at the kernel.
Only `iptables DROP SYN` prevents the TCP handshake, triggering the real netty `ConnectTimeoutException`.

## Prerequisites

- Docker Desktop with Linux containers
- Docker memory: **8 GB+**
- A Cosmos DB account with thin client enabled
- System properties: `COSMOS.THINCLIENT_ENABLED=true`, `COSMOS.THINCLIENT_CONNECTION_TIMEOUT_IN_MS=5000` (default; override to 1000 for fast-fail local tests)
- Credentials in `sdk/cosmos/cosmos-v4.properties`

## Build & Run

Same Docker setup as `NETWORK_DELAY_TESTING_README.md`, with additional system properties:

```bash
# See NETWORK_DELAY_TESTING_README.md for full Docker run setup
# (credential env vars, volume mounts, image build).
# Additional system properties for connect timeout tests:
docker run --rm --cap-add=NET_ADMIN --memory 8g \
  ... \
  cosmos-netem-test bash -c '
    cd /workspace && \
    java -DCOSMOS.THINCLIENT_ENABLED=true \
      -DCOSMOS.THINCLIENT_CONNECTION_TIMEOUT_IN_MS=1000 \
      -DCOSMOS.HTTP2_ENABLED=true \
      org.testng.TestNG /workspace/azure-cosmos-tests/src/test/resources/manual-http-network-fault-testng.xml \
      -verbose 2
  '
```

## Network Commands Used

### iptables DROP SYN — Block New TCP Connections

```bash
# Block TCP handshake to thin client port
iptables -A OUTPUT -p tcp --dport 10250 --tcp-flags SYN,ACK,FIN,RST SYN -j DROP

# Remove rule
iptables -D OUTPUT -p tcp --dport 10250 --tcp-flags SYN,ACK,FIN,RST SYN -j DROP
```

Drops only the initial SYN packet. Server never sees the connection. Client's TCP stack
retransmits with exponential backoff. Netty's `CONNECT_TIMEOUT_MILLIS` fires after 1s →
`ConnectTimeoutException`. Existing connections are unaffected.

### Per-Port Delay — tc prio + iptables mangle

```bash
# 1. Create 3-band priority qdisc
tc qdisc add dev eth0 root handle 1: prio bands 3

# 2. Attach delays to bands
tc qdisc add dev eth0 parent 1:1 handle 10: netem delay 5000ms   # port 443 SYN
tc qdisc add dev eth0 parent 1:2 handle 20: netem delay 5000ms   # port 10250 SYN
tc qdisc add dev eth0 parent 1:3 handle 30: pfifo_fast            # everything else

# 3. Mark SYN-ONLY packets by port
iptables -t mangle -A OUTPUT -p tcp --dport 443 --tcp-flags SYN,ACK,FIN,RST SYN -j MARK --set-mark 1
iptables -t mangle -A OUTPUT -p tcp --dport 10250 --tcp-flags SYN,ACK,FIN,RST SYN -j MARK --set-mark 2

# 4. Route marks to bands
tc filter add dev eth0 parent 1:0 protocol ip prio 1 handle 1 fw flowid 1:1
tc filter add dev eth0 parent 1:0 protocol ip prio 2 handle 2 fw flowid 1:2

# Cleanup
tc qdisc del dev eth0 root
iptables -t mangle -F OUTPUT
```

Port 443 gets 5s SYN delay (< 45s connect timeout → succeeds;
5s > 1s thin client timeout → proves metadata uses 45s, not 1s).
Port 10250 gets 5s SYN delay (> 1s thin client connect timeout → fails).
**Same delay, different outcomes** — the only variable is the CONNECT_TIMEOUT_MILLIS value.

**Why SYN-only delay?** tc netem delays every packet it matches. Delaying ALL packets causes
TLS handshake timeout (sslHandshakeTimeout=10s), HTTP response timeout, and premature
connection close — all unrelated to CONNECT_TIMEOUT_MILLIS. SYN-only delay isolates the
TCP connect phase, which is exactly what CONNECT_TIMEOUT_MILLIS controls.

**Critical tc detail:** The `prio` qdisc's default priomap sends unmarked traffic to
band 1 (the first delay band). A catch-all filter (`u32 match u32 0 0 flowid 1:3`)
is required to route non-SYN traffic to band 3 (no delay).

## Tests

| Test | Technique | What It Proves |
|------|-----------|---------------|
| `connectTimeout_GwV2_DataPlane_1sFiresOnDroppedSyn` | iptables DROP SYN on 10250 | Data plane fails in ~1s, not 45s |
| `connectTimeout_GwV1_Metadata_UnaffectedByGwV2Drop` | iptables DROP SYN on 10250 only | Metadata on 443 unaffected |
| `connectTimeout_GwV2_PreciseTiming` | iptables DROP SYN, 3s e2e | ≥2 connect attempts in 3s budget (proving 1s each) |
| `connectTimeout_Bifurcation_DelayBased_...` | tc prio + SYN-only mangle | Same 5s SYN delay on both ports: 443 succeeds (5s < 45s), 10250 fails (5s > 1s) |

## Important Notes

- Tests run **sequentially** — tc/iptables are interface-global
- `--cap-add=NET_ADMIN` required for both `tc` and `iptables`
- `@AfterClass` removes all iptables rules (`alwaysRun=true`)
- System property `COSMOS.THINCLIENT_CONNECTION_TIMEOUT_IN_MS=1000` sets the 1s bifurcated timeout
