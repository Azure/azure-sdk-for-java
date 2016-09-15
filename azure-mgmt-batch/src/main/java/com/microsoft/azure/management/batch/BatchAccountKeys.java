package com.microsoft.azure.management.batch;

/**
 * Created by ans on 9/13/2016.
 */
public class BatchAccountKeys {
    /**
     * The primary key associated with the account.
     */
    private String primary;

    /**
     * The secondary key associated with the account.
     */
    private String secondary;

    /**
     * Constructor for the class.
     *
     * @param primaryKey primary key value for the batch account
     * @param secondaryKey secondary key value for the batch account
     */
    public BatchAccountKeys(String primaryKey, String secondaryKey) {
        primary = primaryKey;
        secondary = secondaryKey;
    }

    /**
     * Get the primary value.
     *
     * @return the primary value
     */
    public String primary() {
        return this.primary;
    }

    /**
     * Get the secondary value.
     *
     * @return the secondary value
     */
    public String secondary() {
        return this.secondary;
    }

}
