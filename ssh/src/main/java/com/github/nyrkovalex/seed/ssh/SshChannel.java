package com.github.nyrkovalex.seed.ssh; 

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class SshChannel implements AutoCloseable {

        private final InputStream in;
        private final OutputStream out;

        SshChannel(Channel channel) throws SshException {
            try {
                in = channel.getInputStream();
                out = channel.getOutputStream();
                channel.connect();
            } catch (IOException | JSchException ex) {
                throw new SshException(ex);
            }
        }

        void send(byte[] command) throws IOException, SshException {
            sendBytes(command);
        }
        
        void send(int oneByte) throws IOException, SshException {
            sendBytes(new byte[] { (byte) oneByte });
        }

        @Override
        public void close() throws IOException {
            out.close();
            in.close();
        }

        private int nextByte() throws SshException {
            try {
                return in.read();
            } catch (IOException ex) {
                throw new SshException(ex);
            }
        }

        void sendBytes(byte[] command) throws IOException, SshException {
            write(command);
            out.flush();
            checkReply();
        }

        private void checkReply() throws SshException {
            final int SUCCESS = 0;
            final int ERROR = 1;
            final int FATAL_ERROR = 2;
            final int WTF = -1;

            int firstByte = nextByte();
            switch (firstByte) {
                case SUCCESS: // fallthrough
                case WTF:
                    return;
                case ERROR: // fallthrough
                case FATAL_ERROR:
                    StringBuilder sb = new StringBuilder();
                    int nextByte;
                    do {
                        nextByte = nextByte();
                        sb.append((char) nextByte);
                    } while (nextByte != '\n');
                    throw new SshException(sb.toString());
            }
            throw new SshException("Unknown return code");
        }

        void send(String command) throws IOException, SshException {
            send(command.getBytes());
        }

        void write(byte[] buf) throws IOException {
            out.write(buf);
        }
    }