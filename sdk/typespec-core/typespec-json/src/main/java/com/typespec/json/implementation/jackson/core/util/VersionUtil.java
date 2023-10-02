// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.util;

import java.io.*;
import java.util.Properties;
import java.util.regex.Pattern;

import com.typespec.json.implementation.jackson.core.Version;
import com.typespec.json.implementation.jackson.core.Versioned;

/**
 * Functionality for supporting exposing of component {@link Version}s.
 * Also contains other misc methods that have no other place to live in.
 *<p>
 * Note that this class can be used in two roles: first, as a static
 * utility class for loading purposes, and second, as a singleton
 * loader of per-module version information.
 *<p>
 * Note that method for accessing version information changed between versions
 * 2.1 and 2.2; earlier code used file named "VERSION.txt"; but this has serious
 * performance issues on some platforms (Android), so a replacement system
 * was implemented to use class generation and dynamic class loading.
 *<p>
 * Note that functionality for reading "VERSION.txt" was removed completely
 * from Jackson 2.6.
 */
public class VersionUtil
{
    private final static Pattern V_SEP = Pattern.compile("[-_./;:]");

    /*
    /**********************************************************************
    /* Instance life-cycle
    /**********************************************************************
     */

    protected VersionUtil() { }

    @Deprecated // since 2.9
    public Version version() { return Version.unknownVersion(); }

    /*
    /**********************************************************************
    /* Static load methods
    /**********************************************************************
     */
    
    /**
     * Loads version information by introspecting a class named
     * "PackageVersion" in the same package as the given class.
     *<p>
     * If the class could not be found or does not have a public
     * static Version field named "VERSION", returns "empty" {@link Version}
     * returned by {@link Version#unknownVersion()}.
     *
     * @param cls Class for which to look version information
     *
     * @return Version information discovered if any; 
     *  {@link Version#unknownVersion()} if none
     */
    public static Version versionFor(Class<?> cls)
    {
        Version v = null;
        try {
            String versionInfoClassName = cls.getPackage().getName() + ".PackageVersion";
            Class<?> vClass = Class.forName(versionInfoClassName, true, cls.getClassLoader());
            // However, if class exists, it better work correctly, no swallowing exceptions
            try {
                v = ((Versioned) vClass.getDeclaredConstructor().newInstance()).version();
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to get Versioned out of "+vClass);
            }
        } catch (Exception e) { // ok to be missing (not good but acceptable)
            ;
        }
        return (v == null) ? Version.unknownVersion() : v;
    }

    /**
     * Alias of {@link #versionFor(Class)}.
     *
     * @param cls Class for which to look version information
     *
     * @return Version information discovered if any; 
     *  {@link Version#unknownVersion()} if none
     *
     * @deprecated Since 2.12 simply use {@link #versionFor(Class)} instead
     */
    @Deprecated
    public static Version packageVersionFor(Class<?> cls) {
        return versionFor(cls);
    }

    /**
     * Will attempt to load the maven version for the given groupId and
     * artifactId.  Maven puts a pom.properties file in
     * META-INF/maven/groupId/artifactId, containing the groupId,
     * artifactId and version of the library.
     *
     * @param cl the ClassLoader to load the pom.properties file from
     * @param groupId the groupId of the library
     * @param artifactId the artifactId of the library
     * @return The version
     * 
     * @deprecated Since 2.6: functionality not used by any official Jackson component, should be
     *   moved out if anyone needs it
     */
    @SuppressWarnings("resource")
    @Deprecated // since 2.6
    public static Version mavenVersionFor(ClassLoader cl, String groupId, String artifactId)
    {
        InputStream pomProperties = cl.getResourceAsStream("META-INF/maven/"
                + groupId.replaceAll("\\.", "/")+ "/" + artifactId + "/pom.properties");
        if (pomProperties != null) {
            try {
                Properties props = new Properties();
                props.load(pomProperties);
                String versionStr = props.getProperty("version");
                String pomPropertiesArtifactId = props.getProperty("artifactId");
                String pomPropertiesGroupId = props.getProperty("groupId");
                return parseVersion(versionStr, pomPropertiesGroupId, pomPropertiesArtifactId);
            } catch (IOException e) {
                // Ignore
            } finally {
                _close(pomProperties);
            }
        }
        return Version.unknownVersion();
    }

    /**
     * Method used by <code>PackageVersion</code> classes to decode version injected by Maven build.
     *
     * @param s Version String to parse
     * @param groupId Maven group id to include with version
     * @param artifactId Maven artifact id to include with version
     *
     * @return Version instance constructed from parsed components, if successful;
     *    {@link Version#unknownVersion()} if parsing of components fail
     */
    public static Version parseVersion(String s, String groupId, String artifactId)
    {
        if (s != null && (s = s.trim()).length() > 0) {
            String[] parts = V_SEP.split(s);
            return new Version(parseVersionPart(parts[0]),
                    (parts.length > 1) ? parseVersionPart(parts[1]) : 0,
                    (parts.length > 2) ? parseVersionPart(parts[2]) : 0,
                    (parts.length > 3) ? parts[3] : null,
                    groupId, artifactId);
        }
        return Version.unknownVersion();
    }

    protected static int parseVersionPart(String s) {
        int number = 0;
        for (int i = 0, len = s.length(); i < len; ++i) {
            char c = s.charAt(i);
            if (c > '9' || c < '0') break;
            number = (number * 10) + (c - '0');
        }
        return number;
    }

    private final static void _close(Closeable c) {
        try {
            c.close();
        } catch (IOException e) { }
    }

    /*
    /**********************************************************************
    /* Orphan utility methods
    /**********************************************************************
     */

    public final static void throwInternal() {
        throw new RuntimeException("Internal error: this code path should never get executed");
    }
}
