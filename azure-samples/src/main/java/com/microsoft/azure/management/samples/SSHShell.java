/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.samples;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Session;
import expect4j.Closure;
import expect4j.Expect4j;
import expect4j.ExpectState;
import expect4j.matches.Match;
import expect4j.matches.RegExpMatch;
import org.apache.oro.text.regex.MalformedPatternException;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Utility class to run commands on Linux VM via SSH.
 */
public final class SSHShell {
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
     * Creates SSHShell.
     *
     * @param host the host name
     * @param port the ssh port
     * @param userName the ssh user name
     * @param sshPrivateKey the ssh password
     * @return the shell
     * @throws JSchException
     * @throws IOException
     */
    private SSHShell(String host, int port, String userName, byte[] sshPrivateKey)
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
        jsch.setKnownHosts(System.getProperty("user.home") + "/.ssh/known_hosts");
        jsch.addIdentity(host, sshPrivateKey, (byte[]) null, (byte[]) null);
        this.session = jsch.getSession(userName, host, port);
        this.session.setConfig("StrictHostKeyChecking", "no");
        this.session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
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
     */
    public static SSHShell open(String host, int port, String userName, String password)
            throws JSchException, IOException {
        return new SSHShell(host, port, userName, password);
    }

    /**
     * Opens a SSH shell.
     *
     * @param host the host name
     * @param port the ssh port
     * @param userName the ssh user name
     * @param sshPrivateKey the ssh private key
     * @return the shell
     * @throws JSchException exception thrown
     * @throws IOException IO exception thrown
     */
    public static SSHShell open(String host, int port, String userName, byte[] sshPrivateKey)
        throws JSchException, IOException {
        return new SSHShell(host, port, userName, sshPrivateKey);
    }

    /**
     * Runs a given list of commands in the shell.
     *
     * @param commands the commands
     * @return the result
     * @throws Exception exception thrown
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
     * Executes a command on the remote host.
     *
     * @param command the command to be executed
     * @param getExitStatus return the exit status captured in the stdout
     * @param withErr capture the stderr as part of the output
     * @return the content of the remote output from executing the command
     * @throws Exception exception thrown
     */
    public String executeCommand(String command, Boolean getExitStatus, Boolean withErr) throws Exception {
        String result = "";
        String resultErr = "";

        Channel channel = this.session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);
        InputStream commandOutput = channel.getInputStream();
        InputStream commandErr = ((ChannelExec) channel).getErrStream();
        channel.connect();
        byte[] tmp  = new byte[4096];
        while (true) {
            while (commandOutput.available() > 0) {
                int i = commandOutput.read(tmp, 0, 4096);
                if (i < 0) {
                    break;
                }
                result += new String(tmp, 0, i);
            }
            while (commandErr.available() > 0) {
                int i = commandErr.read(tmp, 0, 4096);
                if (i < 0) {
                    break;
                }
                resultErr += new String(tmp, 0, i);
            }
            if (channel.isClosed()) {
                if (commandOutput.available() > 0) {
                    continue;
                }
                if (getExitStatus) {
                    result += "exit-status: " + channel.getExitStatus();
                    if (withErr) {
                        result += "\n With error:\n" + resultErr;
                    }
                }
                break;
            }
            try {
                Thread.sleep(100);
            } catch (Exception ee) { }
        }
        channel.disconnect();

        return result;
    }

    /**
     * Downloads the content of a file from the remote host as a String.
     *
     * @param fileName the name of the file for which the content will be downloaded
     * @param fromPath the path of the file for which the content will be downloaded
     * @param isUserHomeBased true if the path of the file is relative to the user's home directory
     * @return the content of the file
     * @throws Exception exception thrown
     */
    public String download(String fileName, String fromPath, boolean isUserHomeBased) throws Exception {
        ChannelSftp channel = (ChannelSftp) this.session.openChannel("sftp");
        channel.connect();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BufferedOutputStream buff = new BufferedOutputStream(outputStream);
        String absolutePath = isUserHomeBased ? channel.getHome() + "/" + fromPath : fromPath;
        channel.cd(absolutePath);
        channel.get(fileName, buff);

        channel.disconnect();

        return outputStream.toString();
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
    public void upload(InputStream from, String fileName, String toPath, boolean isUserHomeBased, String filePerm) throws Exception {
        ChannelSftp channel = (ChannelSftp) this.session.openChannel("sftp");
        channel.connect();
        String absolutePath = isUserHomeBased ? channel.getHome() + "/" + toPath : toPath;

        String path = "";
        for (String dir : absolutePath.split("/")) {
            path = path + "/" + dir;
            try {
                channel.mkdir(path);
            } catch (Exception ee) {
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

    /**
     * Automatically generate SSH keys.
     * @param passPhrase the byte array content to be uploaded
     * @param comment the name of the file for which the content will be saved into
     * @return SSH public and private key
     * @throws Exception exception thrown
     */
    public static SshPublicPrivateKey generateSSHKeys(String passPhrase, String comment) throws Exception {
        JSch jsch = new JSch();
        KeyPair keyPair = KeyPair.genKeyPair(jsch, KeyPair.RSA);
        ByteArrayOutputStream privateKeyBuff = new ByteArrayOutputStream(2048);
        ByteArrayOutputStream publicKeyBuff = new ByteArrayOutputStream(2048);

        keyPair.writePublicKey(publicKeyBuff, (comment != null) ? comment : "SSHCerts");

        if (passPhrase == null  || passPhrase.isEmpty()) {
            keyPair.writePrivateKey(privateKeyBuff);
        } else {
            keyPair.writePrivateKey(privateKeyBuff, passPhrase.getBytes());
        }

        return new SshPublicPrivateKey(privateKeyBuff.toString(), publicKeyBuff.toString());
    }

    /**
     * Internal class to retain the generate SSH keys.
     */
    public static class SshPublicPrivateKey {
        private String sshPublicKey;
        private String sshPrivateKey;

        /**
         * Constructor.
         * @param sshPrivateKey SSH private key
         * @param sshPublicKey SSH public key
         */
        public SshPublicPrivateKey(String sshPrivateKey, String sshPublicKey) {
            this.sshPrivateKey = sshPrivateKey;
            this.sshPublicKey = sshPublicKey;
        }

        /**
         * Get SSH public key.
         * @return public key
         */
        public String getSshPublicKey() {
            return sshPublicKey;
        }

        /**
         * Get SSH private key.
         * @return private key
         */
        public String getSshPrivateKey() {
            return sshPrivateKey;
        }

        /**
         * Set SSH public key.
         * @param sshPublicKey public key
         */
        public void setSshPublicKey(String sshPublicKey) {
            this.sshPublicKey = sshPublicKey;
        }

        /**
         * Set SSH private key.
         * @param sshPrivateKey private key
         */
        public void setSshPrivateKey(String sshPrivateKey) {
            this.sshPrivateKey = sshPrivateKey;
        }
    }
}
