// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.faultinjection;

import io.netty.channel.EventLoop;
import io.netty.resolver.AddressResolverGroup;
import io.netty.resolver.InetNameResolver;
import io.netty.resolver.InetSocketAddressResolver;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Test fixture: a DNS resolver that wraps JVM resolution but dynamically filters out
 * blocked IPs. Allows e2e tests to simulate DNS changes mid-workload without OS-level
 * hacks (no /etc/hosts, no iptables, no external DNS server).
 *
 * <p>Usage:
 * <pre>{@code
 *   FilterableDnsResolverGroup resolver = new FilterableDnsResolverGroup();
 *
 *   // Wire into reactor-netty HttpClient via .resolver(resolver)
 *   // ... run workload, connections land on IP1 ...
 *
 *   resolver.blockIp(ip1);    // dynamic — no restart, no OS change
 *   // ... wait for max lifetime → new connection → resolves to IP2 ...
 *
 *   resolver.unblockIp(ip1);  // dynamic — IP1 available again
 * }</pre>
 */
public class FilterableDnsResolverGroup extends AddressResolverGroup<InetSocketAddress> {

    private static final Logger logger = LoggerFactory.getLogger(FilterableDnsResolverGroup.class);

    private final Set<InetAddress> blockedIps = ConcurrentHashMap.newKeySet();

    /**
     * Block an IP — future DNS resolutions will exclude it.
     * Takes effect immediately for new connections. Existing connections are unaffected.
     */
    public void blockIp(InetAddress ip) {
        blockedIps.add(ip);
        logger.info("Blocked IP: {} (total blocked: {})", ip.getHostAddress(), blockedIps.size());
    }

    /**
     * Unblock an IP — future DNS resolutions may return it again.
     */
    public void unblockIp(InetAddress ip) {
        blockedIps.remove(ip);
        logger.info("Unblocked IP: {} (total blocked: {})", ip.getHostAddress(), blockedIps.size());
    }

    /**
     * Unblock all IPs.
     */
    public void unblockAll() {
        blockedIps.clear();
        logger.info("Unblocked all IPs");
    }

    /**
     * Returns the current set of blocked IPs (snapshot).
     */
    public Set<InetAddress> getBlockedIps() {
        return Set.copyOf(blockedIps);
    }

    @Override
    protected io.netty.resolver.AddressResolver<InetSocketAddress> newResolver(EventExecutor executor) {
        return new InetSocketAddressResolver(executor, new FilterableNameResolver(executor));
    }

    private class FilterableNameResolver extends InetNameResolver {

        FilterableNameResolver(EventExecutor executor) {
            super(executor);
        }

        @Override
        protected void doResolve(String inetHost, Promise<InetAddress> promise) {
            try {
                InetAddress[] allAddrs = InetAddress.getAllByName(inetHost);
                List<InetAddress> filtered = Arrays.stream(allAddrs)
                    .filter(addr -> !blockedIps.contains(addr))
                    .collect(Collectors.toList());

                if (filtered.isEmpty()) {
                    promise.setFailure(new UnknownHostException(
                        "All resolved IPs for " + inetHost + " are blocked: "
                            + Arrays.toString(allAddrs)));
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Resolved {} → {} (blocked {} of {})",
                            inetHost, filtered.get(0).getHostAddress(),
                            allAddrs.length - filtered.size(), allAddrs.length);
                    }
                    promise.setSuccess(filtered.get(0));
                }
            } catch (UnknownHostException e) {
                promise.setFailure(e);
            }
        }

        @Override
        protected void doResolveAll(String inetHost, Promise<List<InetAddress>> promise) {
            try {
                InetAddress[] allAddrs = InetAddress.getAllByName(inetHost);
                List<InetAddress> filtered = Arrays.stream(allAddrs)
                    .filter(addr -> !blockedIps.contains(addr))
                    .collect(Collectors.toList());

                if (filtered.isEmpty()) {
                    promise.setFailure(new UnknownHostException(
                        "All resolved IPs for " + inetHost + " are blocked: "
                            + Arrays.toString(allAddrs)));
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Resolved all {} → {} IPs (blocked {} of {})",
                            inetHost, filtered.size(),
                            allAddrs.length - filtered.size(), allAddrs.length);
                    }
                    promise.setSuccess(filtered);
                }
            } catch (UnknownHostException e) {
                promise.setFailure(e);
            }
        }
    }
}
