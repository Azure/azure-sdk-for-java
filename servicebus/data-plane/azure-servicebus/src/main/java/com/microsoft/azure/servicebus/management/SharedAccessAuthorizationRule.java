package com.microsoft.azure.servicebus.management;

import com.microsoft.azure.servicebus.security.SecurityConstants;

import java.security.SecureRandom;
import java.util.*;

public class SharedAccessAuthorizationRule extends AuthorizationRule {
    static int SUPPORTED_SAS_KEY_LENGTH = 44;
    static String FIXED_CLAIM_TYPE = "SharedAccessKey";

    private String keyName;
    private String primaryKey;
    private String secondaryKey;
    private List<AccessRights> rights;

    SharedAccessAuthorizationRule() {
    }

    public SharedAccessAuthorizationRule(String keyName, List<AccessRights> rights) {
        this(keyName, SharedAccessAuthorizationRule.generateRandomKey(), SharedAccessAuthorizationRule.generateRandomKey(), rights);
    }

    public SharedAccessAuthorizationRule(String keyName, String primaryKey, List<AccessRights> rights) {
        this(keyName, primaryKey, SharedAccessAuthorizationRule.generateRandomKey(), rights);
    }

    public SharedAccessAuthorizationRule(String keyName, String primaryKey, String secondaryKey, List<AccessRights> rights) {
        this.setKeyName(keyName);
        this.setPrimaryKey(primaryKey);
        this.setSecondaryKey(secondaryKey);
        this.setRights(rights);
    }

    @Override
    public String getClaimType() {
        return SharedAccessAuthorizationRule.FIXED_CLAIM_TYPE;
    }

    @Override
    String getClaimValue() {
        return "None";
    }

    @Override
    public String getKeyName() {
        return keyName;
    }

    @Override
    public void setKeyName(String keyName) {
        if (keyName == null || keyName.isEmpty()) {
            throw new IllegalArgumentException("Argument cannot be null");
        }

        if (keyName.length() > SecurityConstants.MAX_KEY_NAME_LENGTH) {
            throw new IllegalArgumentException("sasKeyName cannot be greater than " + SecurityConstants.MAX_KEY_NAME_LENGTH + " characters.");
        }

        this.keyName = keyName;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        if (primaryKey == null || primaryKey.isEmpty()) {
            throw new IllegalArgumentException("Argument cannot be null");
        }

        if (primaryKey.length() > SharedAccessAuthorizationRule.SUPPORTED_SAS_KEY_LENGTH) {
            throw new IllegalArgumentException("sasKey cannot be greater than " + SharedAccessAuthorizationRule.SUPPORTED_SAS_KEY_LENGTH + " characters.");
        }

        this.primaryKey = primaryKey;
    }

    public String getSecondaryKey() {
        return secondaryKey;
    }

    public void setSecondaryKey(String secondaryKey) {
        if (secondaryKey == null || secondaryKey.isEmpty()) {
            throw new IllegalArgumentException("Argument cannot be null");
        }

        if (secondaryKey.length() > SharedAccessAuthorizationRule.SUPPORTED_SAS_KEY_LENGTH) {
            throw new IllegalArgumentException("sasKey cannot be greater than " + SharedAccessAuthorizationRule.SUPPORTED_SAS_KEY_LENGTH + " characters.");
        }

        this.secondaryKey = secondaryKey;
    }

    @Override
    public List<AccessRights> getRights() {
        return rights;
    }

    @Override
    public void setRights(List<AccessRights> rights) {
        if (rights == null || rights.size() <= 0 || rights.size() > ManagementClientConstants.SupportedClaimsCount) {
            throw new IllegalArgumentException("Rights cannot be null, empty or greater than " + ManagementClientConstants.SupportedClaimsCount);
        }

        HashSet<AccessRights> dedupedAccessRights = new HashSet<>(rights);
        if (rights.size() != dedupedAccessRights.size()) {
            throw new IllegalArgumentException("Access rights on an authorization rule must be unique");
        }

        if (dedupedAccessRights.contains(AccessRights.Manage) && dedupedAccessRights.size() != 3) {
            throw new IllegalArgumentException("Manage permission should also include Send and Listen.");
        }

        this.rights = rights;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof SharedAccessAuthorizationRule)) {
            return false;
        }

        SharedAccessAuthorizationRule other = (SharedAccessAuthorizationRule) o;
        if (this.keyName != null && !this.keyName.equalsIgnoreCase(other.keyName)
                || this.primaryKey != null && !this.primaryKey.equalsIgnoreCase(other.primaryKey)
                || this.secondaryKey != null && !this.secondaryKey.equalsIgnoreCase(other.secondaryKey)) {
            return false;
        }

        if ((this.rights != null && other.rights == null) ||
                this.rights == null && other.rights != null) {
            return false;
        }

        if (this.rights != null) {
            if (this.rights.size() != other.rights.size()) {
                return false;
            }

            HashSet<AccessRights> thisRights = new HashSet<>(this.rights);
            if (!thisRights.containsAll(other.rights)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 13;
        if (this.keyName != null) {
            hash = (hash * 7) + this.keyName.hashCode();
        }

        if (this.primaryKey != null) {
            hash = (hash * 7) + this.primaryKey.hashCode();
        }

        if (this.secondaryKey != null) {
            hash = (hash * 7) + this.secondaryKey.hashCode();
        }

        if (this.rights != null) {
            hash = (hash * 7) + this.rights.hashCode();
        }

        return hash;
    }

    private static String generateRandomKey() {
        SecureRandom random = new SecureRandom();
        byte[] key256 = new byte[32];
        random.nextBytes(key256);
        return Base64.getEncoder().encodeToString(key256);
    }
}
