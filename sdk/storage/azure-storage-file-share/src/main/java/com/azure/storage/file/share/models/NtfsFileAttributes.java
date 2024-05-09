// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import java.util.EnumSet;

/**
 * Specifies options for NTFS File Attributes.
 */
public enum NtfsFileAttributes {

    /**
     * The file is read-only.
     */
    READ_ONLY,

    /**
     * The file is hidden, and thus is not included in ordinary directory listing.
     */
    HIDDEN,

    /**
     * The file is a system file. That is, the file is part of the operating system or is used exclusively by the
     * operating system.
     */
    SYSTEM,

    /**
     * The file is a standard file that has no special attributes.
     */
    NORMAL,

    /**
     * The file is a directory.
     */
    DIRECTORY,

    /**
     * The file is a candidate for backup or removal.
     */
    ARCHIVE,

    /**
     * The file is temporary. A temporary file contains data that is needed while an application is executing but is not
     * needed after the application is finished. File systems try to keep all the data in memory for quicker access
     * rather than flushing the data back to mass storage. A temporary file should be deleted by the application as soon
     * as it is no longer needed.
     */
    TEMPORARY,

    /**
     * The file is offline. The data of the file is not immediately available.
     */
    OFFLINE,

    /**
     * The file will not be indexed by the operating system's content indexing service.
     */
    NOT_CONTENT_INDEXED,

    /**
     * The file or directory is excluded from the data integrity scan. When this value is applied to a directory, by
     * default, all new files and subdirectories within that directory are excluded from data integrity.
     */
    NO_SCRUB_DATA;

    /**
     * Converts an enum set of {@code NtfsFileAttributes} to a string.
     *
     * @param ntfsAttributes Set of {@link NtfsFileAttributes} to convert to a string.
     * @return a string that represents the NTFS Attributes in the correct format delimited by {@code |} which is
     * described at {@link #toAttributes(String)}.
     */
    public static String toString(EnumSet<NtfsFileAttributes> ntfsAttributes) {
        if (ntfsAttributes == null) {
            return null;
        }

        if (ntfsAttributes.isEmpty()) {
            // If there are no attributes the following code does nothing.
            // TODO (alzimmer): Should this return null instead? Returning "" as it matches current behavior.
            return "";
        }

        final StringBuilder builder = new StringBuilder();

        toStringHelper(builder, ntfsAttributes, NtfsFileAttributes.READ_ONLY, "ReadOnly");
        toStringHelper(builder, ntfsAttributes, NtfsFileAttributes.HIDDEN, "Hidden");
        toStringHelper(builder, ntfsAttributes, NtfsFileAttributes.SYSTEM, "System");
        toStringHelper(builder, ntfsAttributes, NtfsFileAttributes.NORMAL, "None");
        toStringHelper(builder, ntfsAttributes, NtfsFileAttributes.DIRECTORY, "Directory");
        toStringHelper(builder, ntfsAttributes, NtfsFileAttributes.ARCHIVE, "Archive");
        toStringHelper(builder, ntfsAttributes, NtfsFileAttributes.TEMPORARY, "Temporary");
        toStringHelper(builder, ntfsAttributes, NtfsFileAttributes.OFFLINE, "Offline");
        toStringHelper(builder, ntfsAttributes, NtfsFileAttributes.NOT_CONTENT_INDEXED, "NotContentIndexed");
        toStringHelper(builder, ntfsAttributes, NtfsFileAttributes.NO_SCRUB_DATA, "NoScrubData");

        return builder.toString();
    }

    private static void toStringHelper(StringBuilder sb, EnumSet<NtfsFileAttributes> ntfsAttributes,
        NtfsFileAttributes attributes, String toAdd) {
        if (ntfsAttributes.contains(attributes)) {
            if (sb.length() > 0) {
                // Already appended an attribute, so append the delimiter before appending the next attribute.
                sb.append("|");
            }

            sb.append(toAdd);
        }
    }

    /**
     * Creates an enum set of {@code NtfsFileAttributes} from a valid String .
     *
     * @param ntfsAttributes A <code>String</code> that represents the ntfs attributes. The string must contain one or
     * more of the following values delimited by a |. Note they are case-sensitive.
     * <ul>
     * <li><code>ReadOnly</code></li>
     * <li><code>Hidden</code></li>
     * <li><code>System</code></li>
     * <li><code>None</code></li>
     * <li><code>Directory</code></li>
     * <li><code>Archive</code></li>
     * <li><code>Temporary</code></li>
     * <li><code>Offline</code></li>
     * <li><code>NotContentIndexed</code></li>
     * <li><code>NoScrubData</code></li>
     * </ul>
     * @return A set of {@link NtfsFileAttributes} that were contained in the passed string.
     * @throws IllegalArgumentException If {@code ntfsAttributes} contains an attribute that is unknown.
     */
    public static EnumSet<NtfsFileAttributes> toAttributes(String ntfsAttributes) {
        if (ntfsAttributes == null) {
            return null;
        }
        EnumSet<NtfsFileAttributes> attributes = EnumSet.noneOf(NtfsFileAttributes.class);
        String[] splitAttributes = ntfsAttributes.split("\\|");

        for (String sa : splitAttributes) {
            sa = sa.trim();
            if ("ReadOnly".equals(sa)) {
                attributes.add(NtfsFileAttributes.READ_ONLY);
            } else if ("Hidden".equals(sa)) {
                attributes.add(NtfsFileAttributes.HIDDEN);
            } else if ("System".equals(sa)) {
                attributes.add(NtfsFileAttributes.SYSTEM);
            } else if ("None".equals(sa)) {
                attributes.add(NtfsFileAttributes.NORMAL);
            } else if ("Directory".equals(sa)) {
                attributes.add(NtfsFileAttributes.DIRECTORY);
            } else if ("Archive".equals(sa)) {
                attributes.add(NtfsFileAttributes.ARCHIVE);
            } else if ("Temporary".equals(sa)) {
                attributes.add(NtfsFileAttributes.TEMPORARY);
            } else if ("Offline".equals(sa)) {
                attributes.add(NtfsFileAttributes.OFFLINE);
            } else if ("NotContentIndexed".equals(sa)) {
                attributes.add(NtfsFileAttributes.NOT_CONTENT_INDEXED);
            } else if ("NoScrubData".equals(sa)) {
                attributes.add(NtfsFileAttributes.NO_SCRUB_DATA);
            } else {
                throw new IllegalArgumentException("FileAttribute '" + sa + "' not recognized.");
            }
        }

        return attributes;
    }
}
