package cn.juservice.library.ssh;

import org.apache.sshd.common.session.Session;
import org.apache.sshd.scp.common.ScpTransferEventListener;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public class CustomScpTransferEventListener implements ScpTransferEventListener {

    @Override
    public void endFileEvent(Session session, ScpTransferEventListener.FileOperation op, Path file, long length,
                             Set<PosixFilePermission> perms, Throwable thrown) throws IOException {
        System.out.println(thrown);
        System.out.println("成功");
    }
}
