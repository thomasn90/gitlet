package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * the Commit class.
 * @author Thomas Nguyen
 */
public class Commit implements Serializable {
    /**
     * it's the message.
     */
    private String message;
    /**
     * it's the timestamp.
     */
    private String timestamp;
    /**
     * it's the parent.
     */
    private String parent;
    /**
     * it's the commitHash.
     */
    private String commitHash;
    /**
     * it's the trackedBlobHashes.
     */
    private ArrayList<String> trackedBlobHashes = new ArrayList<>();
    /**
     * it's the timestampFormat.
     */
    private SimpleDateFormat timestampFormat =
            new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");

    /**
     * it's the listofFiles.
     */
    private HashMap<String, String> listofFiles;
    /**
     * it's getting the list of files.
     * @return returns the list of files
     */
    public HashMap<String, String> getListofFiles() {
        return listofFiles;
    }
    /**
     * commit class.
     * @param timestamp1 it's the timestamp
     * @param message1 it's the message
     */
    public Commit(String message1, String timestamp1) {
        this.timestamp = timestamp1;
        this.message = message1;
        this.commitHash = Utils.sha1(this.message, this.timestamp);
        byte[] commitByteArray = Utils.serialize(this);
        File commitFile = new File(System.getProperty("user.dir")
                + "/.gitlet/commits/"
                + Utils.sha1(this.message, this.timestamp));
        Utils.writeContents(commitFile, commitByteArray);
    }

    /**
     * commit class.
     * @param parentCommit commit's parent
     * @param message2 it's the message
     * @param stagingArea it's the staging area
     * @param blobHashMap it's the blob's hashmap
     * @param commitHashMap it's the commit's hashmap
     */
    public Commit(Commit parentCommit, String message2,
                  Repo stagingArea,
                  HashMap<String, Blob> blobHashMap,
                  HashMap<String, Commit> commitHashMap) {
        this.parent = parentCommit.commitHash;
        this.message = message2;
        this.timestamp = timestampFormat.format(new Date());
        try {
            if (stagingArea.getHashToAdd().size() == 0
                    && stagingArea.getHashesToRemove().size() == 0) {
                throw new NullPointerException();
            }
        } catch (NullPointerException e) {
            System.out.println("No changes added to the commit.");
        }
        ArrayList<String> stagingAreaFileNames = new ArrayList<>();
        for (String b : stagingArea.getHashToAdd().keySet()) {
            if (stagingArea.getHashToAdd().get(b) != null) {
                stagingAreaFileNames.add(b);
                trackedBlobHashes.add(stagingArea.getHashToAdd().get(b));
            }
        }
        File parentFile = new File(System.getProperty("user.dir")
                + "/.gitlet/commits/" + parent);
        Commit temp = Utils.readObject(parentFile, Commit.class);
        for (String b : temp.trackedBlobHashes) {
            File blobFile = new File(System.getProperty("user.dir")
                    + "/.gitlet/blobs/" + b);
            Blob curBlob = Utils.readObject(blobFile, Blob.class);
            if (!stagingAreaFileNames.contains(curBlob.getFileName())
                    && curBlob.isTracked()) {
                trackedBlobHashes.add(b);
            }
        }
        for (String fileName : stagingArea.getHashesToRemove().keySet()) {
            File workingDir = new File(System.getProperty("user.dir"));
            stagingArea.getRemovedBlobName().add(fileName);
        }
        stagingArea.getHashToAdd().clear();
        stagingArea.getHashesToRemove().clear();
        this.commitHash = Utils.sha1(this.message, this.timestamp, this.parent);

        writeToFile();
        stagingArea.writeToFile();
    }
    /**
     * gets the tracked blob hashes.
     * @return returns the tracked blob hashes
     */
    public ArrayList<String> getTrackedBlobHashes() {
        return this.trackedBlobHashes;
    }
    /**
     * gets the bob with name.
     * @param fileName name of the file
     * @param blobHashMap the hashmap of blobs
     * @return returns the current blob
     */
    public Blob getBlobWithName(String fileName,
                                HashMap<String,
                                        Blob> blobHashMap) {
        for (String blobHash : trackedBlobHashes) {
            Blob curBlob = blobHashMap.get(blobHash);
            if (fileName.equals(curBlob.getFileName())) {
                return curBlob;
            }
        }
        return null;
    }
    /**
     * gets the parent hash code.
     * @return returns the parent
     */
    public String getParentHashCode() {
        return this.parent;
    }
    /**
     * checks if contains file.
     * @param fileName name of file
     * @param blobHashMap blob's hashmap
     * @return a boolean
     */
    public boolean containsFile(String fileName,
                                HashMap<String, Blob> blobHashMap) {
        for (String blobHash : trackedBlobHashes) {
            File blobFile =
                    new File(System.getProperty("user.dir")
                            + "/.gitlet/blobs/" + blobHash);
            Blob temp = Utils.readObject(blobFile, Blob.class);
            if (temp.getFileName().equals(fileName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * gets the hash.
     * @return returns the hash
     */
    public String getHash() {
        return this.commitHash;
    }
    /**
     * gets the timestamp.
     * @return the timestamp
     */
    public String getTimestamp() {
        return "Date: " + this.timestamp;
    }
    /**
     * gets the logmessage.
     * @return the message
     */
    public String getLogMessage() {
        return this.message;
    }

    /**
     * Find function.
     * @param commitMessage logMessage of commit
     * @param hashMap it's a hashmap
     */
    public static void find(String commitMessage,
                            HashMap<String, Commit> hashMap) {
        ArrayList<String> foundHashes = new ArrayList<>();
        ArrayList<Commit> commitList = new ArrayList<>();
        File commitFolder = new File(".gitlet/commits");
        File[] commits = commitFolder.listFiles();
        boolean found = false;
        for (File file : commits) {
            Commit comm = Utils.readObject(file, Commit.class);
            if (comm.message.equals(commitMessage)) {
                System.out.println(comm.commitHash);
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }
    /**
     * Writes to file.
     */
    public void writeToFile() {
        byte[] commitByteArray = Utils.serialize(this);
        File commitFile = new File(System.getProperty("user.dir")
                + "/.gitlet/commits/"
                + Utils.sha1(this.message,
                this.timestamp, this.parent));
        Utils.writeContents(commitFile, commitByteArray);
    }
}
