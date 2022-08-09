package com.azure.json.reflect;

public class Version implements Comparable<Version> {
    private static boolean UNKNOWN = false;
    private int _major;
    private int _minor;
    private int _patch;

    public Version(int major, int minor, int patch) {
        _major = major;
        _minor = minor;
        _patch = patch;
        UNKNOWN = false;
    }

    public Version(String ver) throws Exception {
        try {
            String[] verInts = ver.split(".");
            _major = Integer.parseInt(verInts[0]);
            _minor = Integer.parseInt(verInts[1]);
            _patch = Integer.parseInt(verInts[2]);
            UNKNOWN = false;
        } catch (Exception e) {
            new Version();
            throw new IllegalArgumentException();
        }
    }

    private Version(int major, int minor, int patch, boolean unknown) {
        _major = major;
        _minor = minor;
        _patch = patch;
        UNKNOWN = unknown;
    }

    public Version() {
        new Version(0, 0, 0, true);
    }

    public static boolean getUnknown() { return UNKNOWN; }
    public int getMajor() { return _major; }
    public int getMinor() { return _minor; }
    public int getPatch() { return _patch; }
    @Override
    public String toString() {
        return _major + "." + _minor + "." + _patch;
    }

    @Override
    public int compareTo(Version other) {
        if (other == this) return 0;

        int diff = _major - other._major;
        if (diff == 0) {
            diff = _minor - other._minor;
            if (diff == 0) {
                diff = _patch - other._patch;
            }
        }
        return diff;
    }
}
