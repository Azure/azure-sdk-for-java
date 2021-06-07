// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.test;

import org.apache.maven.cli.MavenCli;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MavenBasedProject {

    private final String path;

    public MavenBasedProject(String path) {
        this.path = path;
    }

    public void packageUp() {
        executeMaven("-q", "package");
    }

    public void assembly() {
        executeMaven("-q", "assembly:single");
    }

    private void executeMaven(String... commands) {
        String existing = setMavenSystemProp(path);
        try {
            MavenCli cli = new MavenCli();
            int ret = cli.doMain(commands, path, System.out, System.err);

            if (ret != 0) {
                throw new RuntimeException();
            }

        } finally {
            setMavenSystemProp(existing);
        }
    }

    private String setMavenSystemProp(String value) {
        String result = System.getProperty("maven.multiModuleProjectDirectory");

        if (value != null) {
            System.setProperty("maven.multiModuleProjectDirectory", value);
        } else {
            System.clearProperty("maven.multiModuleProjectDirectory");
        }

        return result;
    }

    public String artifact() {
        try (FileInputStream input = new FileInputStream(new File(path, "pom.xml"))) {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            reader.read(input);

            File result = new File(new File(path, "target"), "app.jar");
            return result.getAbsolutePath();

        } catch (IOException | XmlPullParserException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String zipFile() {
        try (FileInputStream input = new FileInputStream(new File(path, "pom.xml"))) {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            reader.read(input);
            File result = new File(new File(path, "target"), "app.zip");
            return result.getAbsolutePath();

        } catch (IOException | XmlPullParserException ex) {
            throw new RuntimeException(ex);
        }
    }

}
