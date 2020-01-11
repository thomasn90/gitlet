package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * the Repo.
 * @author Thomas Nguyen
 */
public class Repo implements Serializable {
    /**
     * the hash to remove.
     */
    private HashMap<String, String> hashToAdd = new HashMap<>();
    /**
     * the has to remove.
     */
    private HashMap<String, String> hashToRemove = new HashMap<>();
    /**
     * the has to remove.
     */
    private ArrayList<String> removedBlobName = new ArrayList<>();
    /**
     * the staging area.
     */
    public Repo() {
        this.hashToAdd = new HashMap<>();
        this.hashToRemove = new HashMap<>();
        this.removedBlobName = new ArrayList<>();
    }
    /**
     * the hashes to add.
     * @return the hash to add
     */
    public HashMap<String, String> getHashToAdd() {
        return hashToAdd;
    }
    /**
     * the hashes to remove.
     * @return the hash to remove
     */
    public HashMap<String, String> getHashesToRemove() {
        return hashToRemove;
    }
    /**
     * the hashes to add.
     * @return the hash to add
     */
    public ArrayList<String> getRemovedBlobName() {
        return removedBlobName;
    }
    /**
     * adds.
     * @param file the file to add
     * @param head the branch head
     * @param blobHashMap the bob's hashmap
     */
    public void add(String file,
                    Branch head,
                    HashMap<String, Blob> blobHashMap) {
        File tempFile = new File(System.getProperty("user.dir"));
        HashMap<String, String> tempStagingArea = new HashMap<>(hashToRemove);
        boolean condition = false;
        for (String hash : tempStagingArea.keySet()) {
            if (hash.equals(file)) {
                hashToRemove.remove(hash);
                writeToFile();
                condition = true;
                return;
            }
        }
        for (String fileNameInDirectory
                : Objects.requireNonNull(Utils.plainFilenamesIn(tempFile))) {
            if (fileNameInDirectory.equals(file)) {
                Blob tempBlob = new Blob(new File(tempFile, file));
                if (head.getEndCommit().getTrackedBlobHashes().
                        contains(tempBlob.getHash())) {
                    return;
                }
                tempBlob.track();
                File added = new File(file);
                String sha = Utils.sha1(added.toString());
                byte[] stagingAreaByteArray = Utils.serialize(this);
                hashToAdd.put(file, tempBlob.getHash());
                writeToFile();
                condition = true;
            }
        }
        if (condition) {
            return;
        }
        System.out.println("File does not exist.");
    }
    /**
     * removes stuff.
     * @param file it's the file
     * @param head the branch head
     * @param blobHashMap the hashmap of the blob
     */
    public void rm(String file, Branch head,
                   HashMap<String, Blob> blobHashMap) {
        File workingDir = new File(System.getProperty("user.dir"));
        HashMap<String, String> tempStagingArea = new HashMap<>(hashToAdd);
        boolean condition = false;
        for (String hash : head.getEndCommit().getTrackedBlobHashes()) {
            File blobFile = new File(System.getProperty("user.dir")
                    + "/.gitlet/blobs/" + hash);
            Blob curBlob = Utils.readObject(blobFile, Blob.class);
            if (curBlob.getFileName().equals(file)
                    && !tempStagingArea.containsKey(file)) {
                hashToRemove.put(file, hash);
                Utils.restrictedDelete(new File(workingDir, file));
                curBlob.untrack();
                writeToFile();
                condition = true;
            }
        }
        for (String hash : tempStagingArea.keySet()) {
            if (hash.equals(file)) {
                hashToAdd.remove(hash);
                File blobFile = new File(System.getProperty("user.dir")
                        + "/.gitlet/blobs/" + tempStagingArea.get(file));
                Blob curBlob = Utils.readObject(blobFile, Blob.class);
                writeToFile();
                condition = true;
            }
        }
        if (condition) {
            return;
        }
        System.out.println("No reason to remove the file.");
    }

    /**
     * writes to file.
     */
    public void writeToFile() {
        byte[] stagingAreaByteArray = Utils.serialize(this);
        File stagingAreaFile = new File(System.getProperty("user.dir")
                + "/.gitlet/staging_area");
        Utils.writeContents(stagingAreaFile, stagingAreaByteArray);
    }
}
