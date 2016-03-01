/**
 * Object]
 */

package com.microsoft.azure.management.compute.models;


/**
 * Describes a diagnostics profile.
 */
public class DiagnosticsProfile {
    /**
     * Gets or sets the boot diagnostics.
     */
    private BootDiagnostics bootDiagnostics;

    /**
     * Get the bootDiagnostics value.
     *
     * @return the bootDiagnostics value
     */
    public BootDiagnostics getBootDiagnostics() {
        return this.bootDiagnostics;
    }

    /**
     * Set the bootDiagnostics value.
     *
     * @param bootDiagnostics the bootDiagnostics value to set
     */
    public void setBootDiagnostics(BootDiagnostics bootDiagnostics) {
        this.bootDiagnostics = bootDiagnostics;
    }

}
