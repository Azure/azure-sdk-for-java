// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.test;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import expect4j.Closure;
import expect4j.Expect4j;
import expect4j.matches.Match;
import expect4j.matches.RegExpMatch;
import org.apache.oro.text.regex.MalformedPatternException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Utility class to run commands on Linux VM via SSH.
 */
public final class SSHShell implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SSHShell.class);
    private final Session session;
    private final ChannelShell channel;
    private final Expect4j expect;
    private final StringBuilder shellBuffer = new StringBuilder();
    private final List<Match> linuxPromptMatches =  new ArrayList<>();

    /**
     * Creates SSHShell.
     *
     * @param host the host name
     * @param port the ssh port
     * @param userName the ssh user name
     * @param password the ssh password
     * @return the shell
     * @throws JSchException
     * @throws IOException
     */
    private SSHShell(String host, int port, String userName, String password)
            throws JSchException, IOException, MalformedPatternException {
        final Closure expectClosure = getExpectClosure();
        for (final String linuxPromptPattern : new String[]{"\\>", "#", "~#", "~\\$"}) {
            final Match match = new RegExpMatch(linuxPromptPattern, expectClosure);
            linuxPromptMatches.add(match);
        }
        final JSch jsch = new JSch();
        this.session = jsch.getSession(userName, host, port);
        session.setPassword(password);
        final Hashtable<String, String> config = new Hashtable<>();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect(60000);
        this.channel = (ChannelShell) session.openChannel("shell");
        this.expect = new Expect4j(channel.getInputStream(), channel.getOutputStream());
        channel.connect();
    }

    /**
     * Opens a SSH shell.
     *
     * @param host the host name
     * @param port the ssh port
     * @param userName the ssh user name
     * @param password the ssh password
     * @return the shell
     * @throws JSchException exception thrown
     * @throws IOException IO exception thrown
     * @throws MalformedPatternException MalformedPatternException thrown
     */
    public static SSHShell open(String host, int port, String userName, String password)
            throws JSchException, IOException, MalformedPatternException {
        return new SSHShell(host, port, userName, password);
    }


    /**
     * Creates a new file on the remote host using the input content.
     *
     * @param from the byte array content to be uploaded
     * @param fileName the name of the file for which the content will be saved into
     * @param toPath the path of the file for which the content will be saved into
     * @param isUserHomeBased true if the path of the file is relative to the user's home directory
     * @param filePerm file permissions to be set
     * @throws Exception exception thrown
     */
    public void upload(InputStream from, String fileName, String toPath, boolean isUserHomeBased, String filePerm)
            throws Exception {
        final ChannelSftp channel = (ChannelSftp) this.session.openChannel("sftp");
        channel.connect();
        final String absolutePath = isUserHomeBased ? channel.getHome() + "/" + toPath : toPath;

        final StringBuilder path = new StringBuilder();
        for (final String dir : absolutePath.split("/")) {
            path.append("/" + dir);
            try {
                channel.mkdir(path.toString());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        channel.cd(absolutePath);
        channel.put(from, fileName);
        if (filePerm != null) {
            channel.chmod(Integer.parseInt(filePerm), absolutePath + "/" + fileName);
        }

        channel.disconnect();
    }

    /**
     * Runs a given list of commands in the shell.
     *
     * @param commands the commands
     * @return the result
     * @throws Exception exception thrown
     */
    public String runCommands(List<String> commands) throws Exception {
        final String output;
        try {
            for (final String command : commands) {
                expect.expect(this.linuxPromptMatches);
                expect.send(command);
                expect.send("\r");
                expect.expect(this.linuxPromptMatches);
            }
            output = shellBuffer.toString();
        } finally {
            shellBuffer.setLength(0);
        }
        return output;
    }


    /**
     * Closes shell.
     */
    public void close() {
        if (expect != null) {
            expect.close();
        }
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
    }

    private Closure getExpectClosure() {
        return expectState -> {
            final String outputBuffer = expectState.getBuffer();
            System.out.println(outputBuffer);
            shellBuffer.append(outputBuffer);
            expectState.exp_continue();
        };
    }

}
