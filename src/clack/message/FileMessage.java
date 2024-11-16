package clack.message;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Message class that carries the contents of a file, and a suggested
 * name to use when saving it on another user's device.
 * <p>
 * This class does not override equals() and hashCode(). Given the
 * precision of the timestamp it is unlikely that any two objects of
 * any Message subclass will ever test equal, or have equal hashCodes.
 */
public class FileMessage extends Message {
    private final String fileContents;
    private final String fileName;

    /**
     * Constructs a new FileMessage object by reading the contents of
     * the file at a given path, and using a supplied file name as the
     * suggested saveAs name.
     *
     * @param username       name of the user creating this Message.
     * @param fileReadPath   where to find the file to read.
     * @param fileSaveAsName name to suggest when saving the file.
     *                       If this String contains any file path
     *                       components, they are removed.
     * @throws IOException if fileReadPath is not a readable file.
     */
    public FileMessage(String username, String fileReadPath,
                       String fileSaveAsName) throws IOException {
        super(username, MsgTypeEnum.FILE);
        // readString() "... ensures that the file is closed when all
        // content have been read or an I/O error, or other runtime
        // exception, is thrown." -- File.readString() javadoc.
        fileContents = Files.readString(Path.of(fileReadPath));
        fileName = String.valueOf(
                Path.of(fileSaveAsName).getFileName());
    }

    /**
     * Convenience constructor. Acts exactly as
     * FileMessage(username, fileReadPath, fileReadPath)
     *
     * @param username     name of the user creating this message.
     * @param fileReadPath where to find the file to read.
     * @throws IOException if fileReadPath is not a readable file.
     */
    public FileMessage(String username, String fileReadPath)
            throws IOException {
        this(username, fileReadPath, fileReadPath);
    }

    /**
     * Returns the file contents.
     *
     * @return the file contents.
     */
    public String getFileContents() {
        return fileContents;
    }

    /**
     * Returns the suggested name for saving the file.
     *
     * @return the suggested name for saving the file.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns a string representation of this Message.
     *
     * @return a string representation of this Message.
     */
    @Override
    public String toString() {
        return "FileMessage{" + super.toString()
                + ", fileName='" + fileName + "'"
                + ", fileContents='" + fileContents + "'}";
    }

}
