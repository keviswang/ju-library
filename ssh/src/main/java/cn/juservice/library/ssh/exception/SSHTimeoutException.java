package cn.juservice.library.ssh.exception;

public class SSHTimeoutException extends Exception {
    private static final long serialVersionUID = 6921030052115946209L;

    public SSHTimeoutException(String cmd, String host, long timeout) {
        super("Command '" + cmd + "' on host '" + host + "' timed out after " + timeout + " seconds");
    }

}
