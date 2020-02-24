// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

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
