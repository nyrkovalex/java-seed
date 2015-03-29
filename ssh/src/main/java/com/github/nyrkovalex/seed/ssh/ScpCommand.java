package com.github.nyrkovalex.seed.ssh;

import com.github.nyrkovalex.seed.Seed;
import com.github.nyrkovalex.seed.sys.Sys;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScpCommand {

    private String username;
    private final List<ScpFile> files;
    private final String address;

    ScpCommand(String address) {
        this.files = new ArrayList<>();
        this.address = address;
    }

    public ScpCommand file(String from, String to) {
        this.files.add(new ScpFile(from, to));
        return this;
    }

    public ScpCommand asUser(String username) {
        this.username = username;
        return this;
    }

    public void run() throws SshException {
        Session session = null;
        ChannelExec channel = null;
        try {
            session = createSession();
            session.connect();
            channel = (ChannelExec) session.openChannel("exec");
            for (ScpFile f : files) {
                sendFile(channel, f);
            }
        } catch (JSchException | IOException ex) {
            throw new SshException(ex);
        } finally {
            disconnect(channel, session);
        }
    }

    private void sendFile(ChannelExec channel, ScpFile scpFile) throws SshException, IOException {
        channel.setCommand("scp -t " + scpFile.to());
        try (SshChannel sshChannel = new SshChannel(channel)) {
            File file = scpFile.toFile();
            sendModifiedTime(file, sshChannel);
            sendFileSize(file, sshChannel);
            sendFileContent(file, sshChannel);
        }
    }

    private void disconnect(ChannelExec channel, Session session) {
        if (Objects.nonNull(channel)) {
            channel.disconnect();
        }
        if (Objects.nonNull(session)) {
            session.disconnect();
        }
    }

    private Session createSession() throws JSchException {
        JSch jsch = new JSch();
        jsch.addIdentity(System.getProperty("user.home") + "/.ssh/id_rsa");
        Session session = jsch.getSession(username, address);
        session.setUserInfo(new ConsoleUserInfo(Sys.console()));
        return session;
    }

    private static void sendModifiedTime(File file, SshChannel channel)
            throws IOException, SshException {
        long lastModified = file.lastModified() / 1000;
        String command = String.format("T%d 0 %d 0\n", lastModified, lastModified);
        channel.send(command);
    }

    private static void sendFileSize(File file, SshChannel channel)
            throws IOException, SshException {
        String command = String.format("C0644 %d %s \n", file.length(), file.getName());
        channel.send(command);
    }

    private void sendFileContent(File file, SshChannel channel)
            throws IOException, SshException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buf = new byte[1024];
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0) {
                    break;
                }
                channel.write(buf);
            }
        }
        channel.send(0);
    }
}
