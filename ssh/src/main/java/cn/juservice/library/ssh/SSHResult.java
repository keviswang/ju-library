package cn.juservice.library.ssh;

import lombok.Data;

import java.io.Serializable;

/**
 * ssh客户端执行命令结果
 */
@Data
public final class SSHResult implements Serializable {
    private static final long serialVersionUID = -4856668210604945960L;
    private String stdOutput;
    private String errOutput;
    private int returnCode;

    SSHResult(String stdOutput, String errOutput, int returnCode) {
        this.stdOutput = stdOutput;
        this.errOutput = errOutput;
        this.returnCode = returnCode;
    }

}
