// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
/* Aalto XML processor
 *
 * Copyright (c) 2006- Tatu Saloranta, tatu.saloranta@iki.fi
 *
 * Licensed under the License specified in the file LICENSE which is
 * included with the source code.
 * You may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.azure.xml.implementation.aalto.util;

import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;

public final class URLUtil {
    private URLUtil() {
    }

    /**
     * Method that tries to figure out how to create valid URL from a system
     * id, without additional contextual information. We could perhaps use
     * java.net.URI class in future?
     */
    public static URL urlFromSystemId(String sysId) throws IOException {
        try {
            /* Ok, does it look like a full URL? For one, you need a colon.
             * Also, to reduce likelihood of collision with Windows paths,
             * let's only accept it if there are 3 preceding other chars...
             * Not sure if Mac might be a problem? (it uses ':' as file path
             * separator, alas, at least prior to MacOS X)
             */
            int ix = sysId.indexOf(':');
            /* Also, protocols are generally fairly short, usually 3 or 4
             * chars (http, ftp, urn); so let's put upper limit of 8 chars too
             */
            if (ix >= 3 && ix <= 8) {
                return new URL(sysId);
            }
            return fileToURL(new File(sysId));
        } catch (MalformedURLException e) {
            throwIoException(e, sysId);
            return null; // never gets here
        }
    }

    /**
     * Method that tries to get a stream (ideally, optimal one) to read from
     * the specified URL.
     * Currently it just means creating a simple file input stream if the
     * URL points to a (local) file, and otherwise relying on URL classes
     * input stream creation method.
     */
    public static InputStream inputStreamFromURL(URL url) throws IOException {
        if ("file".equals(url.getProtocol())) {
            /* On Windows, we can only create file readers for local
             * files... not sure about NFS, but let's be conservative:
             */
            String host = url.getHost();
            if (host == null || host.isEmpty()) {
                return new FileInputStream(url.getPath());
            }
        }
        return url.openStream();
    }

    /**
     * Method that tries to get a stream (ideally, optimal one) to write to
     * the resource specified by given URL.
     * Currently it just means creating a simple file output stream if the
     * URL points to a (local) file, and otherwise relying on URL classes
     * input stream creation method.
     */
    public static OutputStream outputStreamFromURL(URL url) throws IOException {
        if ("file".equals(url.getProtocol())) {
            // as with inputStreamFromURL, avoid probs with Windows network mounts:
            String host = url.getHost();
            if (host == null || host.isEmpty()) {
                return new FileOutputStream(url.getPath());
            }
        }
        return url.openConnection().getOutputStream();
    }

    /**
     * This method is added because the default conversion using
     * <code>file.toURL()</code> turns out to be rather slow, as
     * it tries to figure out if the file is actually a directory.
     * Now, for our use cases this is irrelevant, so we can optimize
     * out that expensive part.
     */
    public static URL fileToURL(File f) throws IOException {
        /* Based on earlier experiences, looked like using
         * File.toURL() is rather expensive (at least on jdk1.4/1.5),
         * so let's just use a faster replacement
         */
        String absPath = f.getAbsolutePath();
        // Need to convert colons/backslashes to regular slashes?
        {
            char sep = File.separatorChar;
            if (sep != '/') {
                absPath = absPath.replace(sep, '/');
            }
        }
        if (!absPath.isEmpty() && absPath.charAt(0) != '/') {
            absPath = "/" + absPath;
        }
        return new URL("file", "", absPath);
    }

    public static String fileToSystemId(File f) throws IOException {
        return fileToURL(f).toExternalForm();
    }

    public static String urlToSystemId(URL u) {
        return u.toExternalForm();
    }

    /*
    ///////////////////////////////////////////
    // Private helper methods
    ///////////////////////////////////////////
    */

    /**
     * Helper method that tries to fully convert strange URL-specific exception
     * to more general IO exception. Also, to try to use JDK 1.4 feature without
     * creating requirement, uses reflection to try to set the root cause, if
     * we are running on JDK1.4
     */
    private static void throwIoException(MalformedURLException mex, String sysId) throws IOException {
        throw new IOException("[resolving systemId '" + sysId + "']: " + mex.toString(), mex);
    }
}
