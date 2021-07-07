package com.azure.identity.implementation.intellij;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.FileInputStream;
import java.io.InputStream;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*" })
public class IntelliJKDBXDatabaseParsingTest {

    @Test
    public void testKdbxDatabaseParsing() throws Exception {
        InputStream inputStreamwow = new FileInputStream(getPath("c.kdbx"));
        IntelliJKdbxDatabase kdbxDatabase = IntelliJKdbxDatabase.parse(inputStreamwow, "testpassword");
        String password = kdbxDatabase.getDatabaseEntryValue("ADAuthManager");
        Assert.assertEquals("DummyEntry", password);
    }

    private String getPath(String filename) {

        String path =  getClass().getClassLoader().getResource(filename).getPath();
        if (path.contains(":")) {
            path = path.substring(1);
        }
        return path;
    }
}
