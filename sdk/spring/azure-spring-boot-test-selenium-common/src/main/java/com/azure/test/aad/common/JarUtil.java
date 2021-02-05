// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.common;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class JarUtil {
    public static final char JAR_SEPARATOR = '/';

    public static void copyFolderFromJar(String folderName, File destFolder, CopyOption option) throws IOException {
        if (!destFolder.exists())
            destFolder.mkdirs();

        byte[] buffer = new byte[1024];

        File fullPath = null;
        String path = JarUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath().trim();
        if (!path.startsWith("file")) {
            path = "file://" + path;
        }

        try {
            fullPath = new File(new URI(path));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        ZipInputStream zis = new ZipInputStream(new FileInputStream(fullPath));

        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (!entry.getName().startsWith(folderName + JAR_SEPARATOR))
                continue;

            String fileName = entry.getName();

            if (fileName.charAt(fileName.length() - 1) == JAR_SEPARATOR) {
                File file = new File(destFolder + File.separator + fileName);
                if (file.isFile()) {
                    file.delete();
                }
                file.mkdirs();
                continue;
            }

            File file = new File(destFolder + File.separator + fileName);
            if (option == CopyOption.COPY_IF_NOT_EXIST && file.exists())
                continue;

            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();

            if (!file.exists()){
                file.createNewFile();
                file.setExecutable(true);
            }
            FileOutputStream fos = new FileOutputStream(file);

            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
        }

        zis.closeEntry();
        zis.close();
    }

    public enum CopyOption {
        COPY_IF_NOT_EXIST, REPLACE_IF_EXIST;
    }

}