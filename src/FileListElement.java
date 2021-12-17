import java.io.File;

public class FileListElement {
    File file;
    boolean error = false;

    public FileListElement(File file, boolean error) {
        this.file = file;
        this.error = error;
    }

    @Override
    public String toString() {
        return file.getAbsolutePath();
    }
}
