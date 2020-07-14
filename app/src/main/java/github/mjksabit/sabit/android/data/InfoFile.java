package github.mjksabit.sabit.android.data;

import java.io.File;

public class InfoFile {
    private ShareState state;
    private File file;
    private long fileSize;

    public static enum ShareState {
        SEND, RECEIVE
    }

    public InfoFile(ShareState state, File file) {
        this.state = state;
        this.file = file;
        this.fileSize = file.length();
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public ShareState getState() {
        return state;
    }

    public File getFile() {
        return file;
    }

    public long getFileSize() {
        return fileSize;
    }
}
