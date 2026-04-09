// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static org.testng.AssertJUnit.fail;

/**
 * Shared utility for Linux network fault injection via {@code tc netem} and {@code iptables}.
 * <p>
 * Encapsulates privileged command execution, sudo detection, network interface discovery,
 * and cleanup. Used by {@code Http2ConnectionLifecycleTests} and
 * {@code Http2ConnectTimeoutBifurcationTests}.
 * <p>
 * Requires Linux with {@code --cap-add=NET_ADMIN} (Docker) or passwordless sudo (CI VM).
 */
public final class NetworkFaultInjector {

    private static final Logger logger = LoggerFactory.getLogger(NetworkFaultInjector.class);

    private final String networkInterface;
    private final String sudoPrefix;

    public NetworkFaultInjector() {
        this.sudoPrefix = "root".equals(System.getProperty("user.name")) ? "" : "sudo ";
        this.networkInterface = detectNetworkInterface();
        logger.info("NetworkFaultInjector: interface={}, sudo={}", this.networkInterface, !this.sudoPrefix.isEmpty());
    }

    public String getNetworkInterface() {
        return this.networkInterface;
    }

    public String getSudoPrefix() {
        return this.sudoPrefix;
    }

    /**
     * Verifies that {@code tc} is available on the detected interface.
     * Fails the test immediately if not.
     */
    public void verifyTcAvailable() {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c",
                sudoPrefix + "tc qdisc show dev " + networkInterface});
            int exit = p.waitFor();
            if (exit != 0) {
                try (BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                    String errMsg = err.readLine();
                    fail("tc not available on " + networkInterface + " (exit=" + exit + "): " + errMsg);
                }
            }
        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            fail("tc check failed: " + e.getMessage());
        }
    }

    /**
     * Adds a tc netem delay to all outbound traffic on the network interface.
     * Includes a brief settling period to ensure the qdisc is fully active
     * before callers send traffic through it.
     *
     * @param delayMs delay in milliseconds
     */
    public void addNetworkDelay(int delayMs) {
        String cmd = String.format("%stc qdisc add dev %s root netem delay %dms",
            sudoPrefix, networkInterface, delayMs);
        execOrFail(cmd, "tc add");
        // Settling delay: tc netem applies asynchronously to the kernel qdisc.
        // Without this, the first packets may enter the queue before netem is active,
        // causing the request to complete within the timeout window (flaky test).
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        logger.info(">>> Network delay active: {}ms on {}", delayMs, networkInterface);
    }

    /**
     * Removes any tc netem qdisc from the network interface. Best-effort — does not fail on error.
     */
    public void removeNetworkDelay() {
        String cmd = String.format("%stc qdisc del dev %s root", sudoPrefix, networkInterface);
        execBestEffort(cmd, "tc del");
    }

    /**
     * Adds an iptables rule to DROP all outgoing TCP packets to the specified port.
     *
     * @param port destination port to block
     */
    public void addPacketDrop(int port) {
        String cmd = String.format("%siptables -A OUTPUT -p tcp --dport %d -j DROP", sudoPrefix, port);
        execOrFail(cmd, "iptables add");
        logger.info(">>> Packet drop active on port {}", port);
    }

    /**
     * Removes the iptables DROP rule for the specified port. Best-effort.
     *
     * @param port destination port to unblock
     */
    public void removePacketDrop(int port) {
        String cmd = String.format("%siptables -D OUTPUT -p tcp --dport %d -j DROP", sudoPrefix, port);
        execBestEffort(cmd, "iptables del");
    }

    /**
     * Removes all network faults — both tc netem and iptables rules. Best-effort.
     * Call in {@code @AfterMethod} and {@code @AfterClass} for clean-slate cleanup.
     */
    public void removeAll() {
        removeNetworkDelay();
        removePacketDrop(10250);
    }

    private void execOrFail(String cmd, String label) {
        logger.info(">>> {}: {}", label, cmd);
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
            int exit = p.waitFor();
            if (exit != 0) {
                try (BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                    String errMsg = err.readLine();
                    fail(label + " failed (exit=" + exit + "): " + errMsg);
                }
            }
        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            logger.error("{} failed", label, e);
            fail("Could not execute " + label + ": " + e.getMessage());
        }
    }

    private void execBestEffort(String cmd, String label) {
        logger.info(">>> {}: {}", label, cmd);
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
            int exit = p.waitFor();
            if (exit == 0) {
                logger.info(">>> {} succeeded", label);
            } else {
                logger.warn("{} returned exit={} (may already be removed)", label, exit);
            }
        } catch (Exception e) {
            logger.warn("{} failed: {}", label, e.getMessage());
        }
    }

    private static String detectNetworkInterface() {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c",
                "ip route show default | awk '{print $5}' | head -1"});
            p.waitFor();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String iface = reader.readLine();
                if (iface != null && !iface.isEmpty() && !iface.contains("@")) {
                    return iface.trim();
                }
            }
        } catch (Exception e) {
            // fall through
        }
        return "eth0";
    }
}
