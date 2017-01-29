/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.samples;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import expect4j.Closure;
import expect4j.Expect4j;
import expect4j.ExpectState;
import expect4j.matches.Match;
import expect4j.matches.RegExpMatch;
import org.apache.oro.text.regex.MalformedPatternException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Utility class to run commands on Linux VM via SSH.
 */
public class SSHShell {
    private final Session session;
    private final ChannelShell channel;
    private final Expect4j expect;
    private final StringBuilder shellBuffer = new StringBuilder();
    private List<Match> linuxPromptMatches =  new ArrayList<>();

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
            throws JSchException, IOException {
        Closure expectClosure = getExpectClosure();
        for (String linuxPromptPattern : new String[]{"\\>", "#", "~#", "~\\$"}) {
            try {
                Match match = new RegExpMatch(linuxPromptPattern, expectClosure);
                linuxPromptMatches.add(match);
            } catch (MalformedPatternException malformedEx) {
                throw new RuntimeException(malformedEx);
            }
        }
        JSch jsch = new JSch();
        this.session = jsch.getSession(userName, host, port);
        session.setPassword(password);
        Hashtable<String, String> config = new Hashtable<>();
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
     * @throws JSchException
     * @throws IOException
     */
    public static SSHShell open(String host, int port, String userName, String password)
            throws JSchException, IOException {
        return new SSHShell(host, port, userName, password);
    }

    /**
     * Runs a given list of commands in the shell.
     *
     * @param commands the commands
     * @return the result
     * @throws Exception
     */
    public String runCommands(List<String> commands) throws Exception {
        String output = null;
        try {
            for (String command : commands) {
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
        return new Closure() {
            public void run(ExpectState expectState) throws Exception {
                String outputBuffer = expectState.getBuffer();
                System.out.println(outputBuffer);
                shellBuffer.append(outputBuffer);
                expectState.exp_continue();
            }
        };
    }
}
