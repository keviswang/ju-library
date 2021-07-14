package cn.juservice.library.ssh;

import cn.juservice.library.ssh.exception.SSHTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import cn.juservice.library.ssh.exception.SSHAuthenticationException;
import org.apache.sshd.scp.client.ScpClient;
import org.apache.sshd.scp.client.ScpClientCreator;

@Slf4j
public class SSHClient {
    private static final long AUTH_TIMEOUT = 10000; // 10s
    private SshClient sshClient;
    private String username;
    private String host;
    private String password;
    private int port;

    public SSHClient(String username, String password, String host, int port) {
        sshClient = SshClient.setUpDefaultClient();
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    /**
     * 获取ssh会话
     *
     * @param duration 毫秒
     */
    public ClientSession connect(long duration) throws IOException, SSHTimeoutException {
        ConnectFuture connectFuture = sshClient.connect(username, host, port);
        connectFuture.await(duration);
        if (!connectFuture.isConnected()) {
            throw new SSHTimeoutException("connect", host, duration);
        }
        log.debug("SSHClient is connected: {}", connectFuture.isConnected());
        return connectFuture.getSession();
    }

    public void startClient() {
        sshClient.start();
        log.debug("SSHClient is started...");
    }

    public void stopClient() {
        sshClient.stop();
        log.debug("SSHClient is stopped...");
    }

    /**
     * @param cmd     需要执行的命令
     * @param timeout 超时时间（秒）
     */
    public SSHResult runCommand(String cmd, long timeout) throws IOException, SSHAuthenticationException,
            SSHTimeoutException {
        try {
            startClient();
            ClientSession session = connect(AUTH_TIMEOUT);
            session.addPasswordIdentity(password);
            AuthFuture authFuture = session.auth();
            authFuture.await(AUTH_TIMEOUT);
            if (!authFuture.isSuccess()) {
                throw new SSHAuthenticationException(host, username, password);
            }

            // Create the exec and channel its output/error streams
            ChannelExec channelExec = session.createExecChannel(cmd);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            channelExec.setOut(out);
            channelExec.setErr(err);

            // Execute and wait
            channelExec.open();
            Set<ClientChannelEvent> events =
                    channelExec.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), TimeUnit.SECONDS.toMillis(timeout));
            session.close(false);

            // Check if timed out
            if (events.contains(ClientChannelEvent.TIMEOUT)) {
                log.error("ssh执行超时");
                throw new SSHTimeoutException(cmd, host, timeout);
            }

            return new SSHResult(out.toString(), err.toString(), channelExec.getExitStatus());
        } finally {
            stopClient();
        }
    }


    public void uploadFile(String source, String dest) throws IOException, SSHTimeoutException,
            SSHAuthenticationException {
        try {
            startClient();
            ClientSession session = connect(AUTH_TIMEOUT);
            session.addPasswordIdentity(password);
            AuthFuture authFuture = session.auth();
            authFuture.await(AUTH_TIMEOUT);
            if (!authFuture.isSuccess()) {
                throw new SSHAuthenticationException(host, username, password);
            }
            ScpClientCreator creator = ScpClientCreator.instance();
            ScpClient scpClient = creator.createScpClient(session, new CustomScpTransferEventListener());
            scpClient.upload(Paths.get(source), dest,
                    ScpClient.Option.Recursive, ScpClient.Option.PreserveAttributes);
            session.close(false);
        } finally {
            stopClient();
        }
    }

    public static void main(String[] args) {
        SSHClient sshClient = new SSHClient("", "", "", 22);
        try {
//            SSHResult result = sshClient.runCommand("ls -l", 5);
//            System.out.println("res:");
//            System.out.println(result);
            sshClient.uploadFile("", "");
        } catch (Exception e) {
            System.out.println("err:");
            System.out.println(e);
        }
    }
}
