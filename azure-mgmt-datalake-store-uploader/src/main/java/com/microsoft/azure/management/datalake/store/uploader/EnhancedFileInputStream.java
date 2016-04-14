/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by begoldsm on 4/12/2016.
 */
public class EnhancedFileInputStream extends FileInputStream {

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        position = position;
    }

    private long position;

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    private long length;

    /**
     * Creates a <code>FileInputStream</code> by
     * opening a connection to an actual file,
     * the file named by the path name <code>name</code>
     * in the file system.  A new <code>FileDescriptor</code>
     * object is created to represent this file
     * connection.
     * <p>
     * First, if there is a security
     * manager, its <code>checkRead</code> method
     * is called with the <code>name</code> argument
     * as its argument.
     * <p>
     * If the named file does not exist, is a directory rather than a regular
     * file, or for some other reason cannot be opened for reading then a
     * <code>FileNotFoundException</code> is thrown.
     *
     * @param name the system-dependent file name.
     * @throws FileNotFoundException if the file does not exist,
     *                               is a directory rather than a regular file,
     *                               or for some other reason cannot be opened for
     *                               reading.
     * @throws SecurityException     if a security manager exists and its
     *                               <code>checkRead</code> method denies read access
     *                               to the file.
     * @see SecurityManager#checkRead(String)
     */
    public EnhancedFileInputStream(String name) throws FileNotFoundException {
        super(name);
        position = 0;
        length = new File(name).length();
    }

    /**
     * Creates a <code>FileInputStream</code> by
     * opening a connection to an actual file,
     * the file named by the <code>File</code>
     * object <code>file</code> in the file system.
     * A new <code>FileDescriptor</code> object
     * is created to represent this file connection.
     * <p>
     * First, if there is a security manager,
     * its <code>checkRead</code> method  is called
     * with the path represented by the <code>file</code>
     * argument as its argument.
     * <p>
     * If the named file does not exist, is a directory rather than a regular
     * file, or for some other reason cannot be opened for reading then a
     * <code>FileNotFoundException</code> is thrown.
     *
     * @param file the file to be opened for reading.
     * @throws FileNotFoundException if the file does not exist,
     *                               is a directory rather than a regular file,
     *                               or for some other reason cannot be opened for
     *                               reading.
     * @throws SecurityException     if a security manager exists and its
     *                               <code>checkRead</code> method denies read access to the file.
     * @see File#getPath()
     * @see SecurityManager#checkRead(String)
     */
    public EnhancedFileInputStream(File file) throws FileNotFoundException {
        super(file);
        position = 0;
        length = file.length();
    }

    /**
     * Creates a <code>FileInputStream</code> by using the file descriptor
     * <code>fdObj</code>, which represents an existing connection to an
     * actual file in the file system.
     * <p>
     * If there is a security manager, its <code>checkRead</code> method is
     * called with the file descriptor <code>fdObj</code> as its argument to
     * see if it's ok to read the file descriptor. If read access is denied
     * to the file descriptor a <code>SecurityException</code> is thrown.
     * <p>
     * If <code>fdObj</code> is null then a <code>NullPointerException</code>
     * is thrown.
     * <p>
     * This constructor does not throw an exception if <code>fdObj</code>
     * is {@link FileDescriptor#valid() invalid}.
     * However, if the methods are invoked on the resulting stream to attempt
     * I/O on the stream, an <code>IOException</code> is thrown.
     *
     * @param fdObj the file descriptor to be opened for reading.
     * @throws SecurityException if a security manager exists and its
     *                           <code>checkRead</code> method denies read access to the
     *                           file descriptor.
     * @see SecurityManager#checkRead(FileDescriptor)
     */
    public EnhancedFileInputStream(FileDescriptor fdObj) {
        super(fdObj);
        position = 0;
        length = -1;
    }

    @Override
    public int read() throws IOException {
        int toReturn = super.read();
        if (toReturn != -1) {
            position++;
        }

        return toReturn;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int toReturn = super.read(b, off, len);

        if (toReturn != -1) {
            position += toReturn;
        }

        return toReturn;
    }

    @Override
    public long skip(long n) throws IOException {
        long toReturn = super.skip(n);
        position += toReturn;
        return toReturn;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int toReturn = super.read(b);
        if (toReturn != -1) {
            position += toReturn;
        }

        return toReturn;
    }
}
