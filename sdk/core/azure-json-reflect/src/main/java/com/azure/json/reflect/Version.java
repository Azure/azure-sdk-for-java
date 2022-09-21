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

    public Version(String ver) {
        try {
            String[] verInts = ver.split(".");
            _major = Integer.parseInt(verInts[0]);
            _minor = verInts.length > 1 ? Integer.parseInt(verInts[1]) : 0;
            _patch = verInts.length > 2 ? Integer.parseInt(verInts[2]) : 0;
            UNKNOWN = false;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();
        }
    }

    private Version(int major, int minor, int patch, boolean unknown) {
        _major = major;
        _minor = minor;
        _patch = patch;
        UNKNOWN = unknown;
    }

    @Override
    public String toString() {
        return _major + "." + _minor + "." + _patch;
    }

    @Override
    public int compareTo(Version other) {
        int diff = _major - other._major;

        if (diff == 0) {
            diff = _minor - other._minor;
        }

        if (diff == 0) {
            diff = _patch - other._patch;
        }

        return diff;
    }
}
