// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */

package com.typespec.json.implementation.jackson.core;

/**
 * Object that encapsulates versioning information of a component.
 * Version information includes not just version number but also
 * optionally group and artifact ids of the component being versioned.
 *<p>
 * Note that optional group and artifact id properties are new with Jackson 2.0:
 * if provided, they should align with Maven artifact information.
 */
public class Version
    implements Comparable<Version>, java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    private final static Version UNKNOWN_VERSION = new Version(0, 0, 0, null, null, null);

    protected final int _majorVersion;

    protected final int _minorVersion;

    protected final int _patchLevel;

    protected final String _groupId;

    protected final String _artifactId;

    /**
     * Additional information for snapshot versions; null for non-snapshot
     * (release) versions.
     */
    protected final String _snapshotInfo;

    /**
     * @param major Major version number
     * @param minor Minor version number
     * @param patchLevel patch level of version
     * @param snapshotInfo Optional additional string qualifier
     *
     * @since 2.1
     * @deprecated Use variant that takes group and artifact ids
     */
    @Deprecated
    public Version(int major, int minor, int patchLevel, String snapshotInfo)
    {
        this(major, minor, patchLevel, snapshotInfo, null, null);
    }

    public Version(int major, int minor, int patchLevel, String snapshotInfo,
            String groupId, String artifactId)
    {
        _majorVersion = major;
        _minorVersion = minor;
        _patchLevel = patchLevel;
        _snapshotInfo = snapshotInfo;
        _groupId = (groupId == null) ? "" : groupId;
        _artifactId = (artifactId == null) ? "" : artifactId;
    }

    /**
     * Method returns canonical "not known" version, which is used as version
     * in cases where actual version information is not known (instead of null).
     *
     * @return Version instance to use as a placeholder when actual version is not known
     *   (or not relevant)
     */
    public static Version unknownVersion() { return UNKNOWN_VERSION; }

    /**
     * @return {@code True} if this instance is the one returned by
     *    call to {@link #unknownVersion()}
     *
     * @since 2.7 to replace misspelled {@link #isUknownVersion()}
     */
    public boolean isUnknownVersion() { return (this == UNKNOWN_VERSION); }

    public boolean isSnapshot() { return (_snapshotInfo != null && _snapshotInfo.length() > 0); }

    /**
     * @return {@code True} if this instance is the one returned by
     *    call to {@link #unknownVersion()}
     *
     * @deprecated Since 2.7 use correctly spelled method {@link #isUnknownVersion()}
     */
    @Deprecated
    public boolean isUknownVersion() { return isUnknownVersion(); }

    public int getMajorVersion() { return _majorVersion; }
    public int getMinorVersion() { return _minorVersion; }
    public int getPatchLevel() { return _patchLevel; }

    public String getGroupId() { return _groupId; }
    public String getArtifactId() { return _artifactId; }

    public String toFullString() {
        return _groupId + '/' + _artifactId + '/' + toString();
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(_majorVersion).append('.');
        sb.append(_minorVersion).append('.');
        sb.append(_patchLevel);
        if (isSnapshot()) {
            sb.append('-').append(_snapshotInfo);
        }
        return sb.toString();
    }

    @Override public int hashCode() {
        return _artifactId.hashCode() ^ _groupId.hashCode() + _majorVersion - _minorVersion + _patchLevel;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null) return false;
        if (o.getClass() != getClass()) return false;
        Version other = (Version) o;
        return (other._majorVersion == _majorVersion)
            && (other._minorVersion == _minorVersion)
            && (other._patchLevel == _patchLevel)
            && other._artifactId.equals(_artifactId)
            && other._groupId.equals(_groupId)
            ;
    }

    @Override
    public int compareTo(Version other)
    {
        if (other == this) return 0;
        
        int diff = _groupId.compareTo(other._groupId);
        if (diff == 0) {
            diff = _artifactId.compareTo(other._artifactId);
            if (diff == 0) {
                diff = _majorVersion - other._majorVersion;
                if (diff == 0) {
                    diff = _minorVersion - other._minorVersion;
                    if (diff == 0) {
                        diff = _patchLevel - other._patchLevel;
                    }
                }
            }
        }
        return diff;
    }
}
