package cn.juservice.library.ssh.exception;

public class SSHAuthenticationException extends Exception {
    private static final long serialVersionUID = 6921030052115946209L;

    public SSHAuthenticationException(String host, String username, String password) {
        super("host '" + host + "'" + " username '" + username + "' password '" + password + "' Authentication " +
                "Failed");
    }

}
