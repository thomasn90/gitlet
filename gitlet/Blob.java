package gitlet;

import java.io.File;
import java.io.Serializable;

/**
 * the Blob class.
 * @author Thomas Nguyen
 */
public class Blob implements Serializable {
    /**
     * its a textfile.
     */
    private File textFile;
    /**
     * its a blobhash.
     */
    private String blobHash;
    /**
     * its a filename.
     */
    private String fileName;
    /**
     * boolean to see if tracked.
     */
    private boolean tracked = false;
    /**
     * its the data.
     */
    private byte[] data;

    /**
     * the blob.
     * @param file it's the textfile
     */
    public Blob(File file) {
        this.textFile = file;
        this.fileName = file.getName();
        this.data = Utils.readContents(file);
        this.blobHash = Utils.sha1(this.data, this.fileName);
        writeToFile();
    }
    /**
     * gets the hash.
     * @return returns the sha
     */
    public String getHash() {
        return blobHash;
    }

    /**
     * gets the file name.
     * @return returns the filename
     */
    public String getFileName() {
        return fileName;
    }
    /**
     * gets the data.
     * @return returns the data
     */
    public byte[] getData() {
        return data;
    }
    /**
     * gets the text file.
     * @return returns the textfile
     */

    public File getTextFile() {
        return textFile;
    }
    /**
     * is it tracked.
     * @return if it's tracked
     */
    public boolean isTracked() {
        return tracked;
    }
    /**
     * tracks it.
     */
    public void track() {
        tracked = true;
        writeToFile();
    }
    /**
     * untracks it.
     */
    public void untrack() {
        tracked = false;
        writeToFile();
    }

    /**
     * Writes to file.
     */
    public void writeToFile() {
        byte[] blobByteArray = Utils.serialize(this);
        File blobFile =
                new File(System.getProperty("user.dir")
                        + "/.gitlet/blobs/" + blobHash);
        Utils.writeContents(blobFile, blobByteArray);
    }
}
