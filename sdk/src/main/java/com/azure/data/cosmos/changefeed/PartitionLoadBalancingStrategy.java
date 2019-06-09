/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos.changefeed;

import com.azure.data.cosmos.ChangeFeedProcessorOptions;

import java.util.List;

/**
 * A strategy defines which leases should be taken by the current host in a certain moment.
 * <p>
 * It can set new {@link Lease} properties() for all returned leases if needed, including currently owned leases.
 * Example
 * <pre>
 * {@code
 *  public class CustomStrategy : PartitionLoadBalancingStrategy
 *  {
 *      private STRING hostName;
 *      private STRING hostVersion;
 *      private Duration leaseExpirationInterval;
 *
 *      private final STRING VersionPropertyKey = "version";
 *
 *      public List<Lease> selectLeasesToTake(List<Lease> allLeases)
 *      {
 *          var takenLeases = this.findLeasesToTake(allLeases);
 *          foreach (var lease in takenLeases)
 *          {
 *              lease.Properties[VersionPropertyKey] = this.hostVersion;
 *          }
 *
 *          return takenLeases;
 *      }
 *
 *      private List<Lease> findLeasesToTake(List<Lease> allLeases)
 *      {
 *          List<Lease> takenLeases = new List<Lease>();
 *          foreach (var lease in allLeases)
 *          {
 *              if (string.IsNullOrWhiteSpace(lease.Owner) || this.IsExpired(lease))
 *              {
 *                  takenLeases.Add(lease);
 *              }
 *
 *              if (lease.Owner != this.hostName)
 *              {
 *                  var ownerVersion = lease.Properties[VersionPropertyKey];
 *                  if (ownerVersion < this.hostVersion)
 *                  {
 *                      takenLeases.Add(lease);
 *                  }
 *
 *                  // more logic for leases owned by other hosts
 *              }
 *          }
 *
 *          return takenLeases;
 *      }
 *
 *      private boolean isExpired(Lease lease)
 *      {
 *          return lease.Timestamp.ToUniversalTime() + this.leaseExpirationInterval < DateTime.UtcNow;
 *      }
 *  } * }
 * </pre>
 *
 */
public interface PartitionLoadBalancingStrategy {
    /**
     * Select leases that should be taken for processing.
     * This method will be called periodically with {@link ChangeFeedProcessorOptions} leaseAcquireInterval().

     * @param allLeases ALL leases.
     * @return Leases that should be taken for processing by this host.
     */
    List<Lease> selectLeasesToTake(List<Lease> allLeases);
}
