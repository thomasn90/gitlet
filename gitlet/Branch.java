package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * the Branch.
 * @author thomas nguyen
 */
public class Branch implements Serializable {
    /**
     * the branch's name.
     */
    private String branchName;
    /**
     * the endCommit.
     */
    private Commit endCommit;
    /**
     * the head.
     */
    private boolean isHead;
    /**
     * its the branch.
     * @param initialCommit the first commit
     */
    public Branch(Commit initialCommit) {
        this.endCommit = initialCommit;
        this.branchName = "master";
        this.isHead = true;
        writeToFile();
    }
    /**
     * the Branch.
     * @param bName the name of the branch
     * @param eCommit the last commit
     */
    public Branch(String bName, Commit eCommit) {
        this.branchName = bName;
        this.endCommit = eCommit;
        this.isHead = false;
        writeToFile();
    }
    /**
     * Removes the branch.
     * @param branchToDeleteName the branch to delete
     */
    public static void rmBranch(String branchToDeleteName) {
        File branchFile =
                new File(System.getProperty("user.dir")
                        + "/.gitlet/branches/"
                + branchToDeleteName);
        boolean fileExists = branchFile.delete();
        if (!fileExists) {
            System.out.println("A branch with that name does not exist.");
        }
    }
    /**
     * Gets the branch head.
     * @return returns the head
     */
    public boolean isBranchHead() {
        return isHead;
    }

    /**
     * Gets the Branch Name.
     * @return the branchname
     */
    public String getBranchName() {
        return branchName;
    }
    /**
     * Gets the endCommit.
     * @return returns the endCommit
     */
    public Commit getEndCommit() {
        return endCommit;
    }

    /**
     * get's the split point hash.
     * @param thisBranch it's the branch
     * @param otherBranch it's the other branch
     * @param commitHashMap it's the hashmap for commits
     * @return returns the split point hash
     */
    public static String getSplitPointHash(Branch thisBranch,
                                           Branch otherBranch,
                                           HashMap<String,
                                                   Commit> commitHashMap) {
        String splitPointHash;
        ArrayList<String> thisBranchCommitHashes = new ArrayList<>();
        Commit thisBranchCommit = thisBranch.getEndCommit();
        Commit otherBranchCommit = otherBranch.getEndCommit();
        while (thisBranchCommit.getParentHashCode() != null) {
            thisBranchCommitHashes.add(thisBranchCommit.getHash());
            thisBranchCommit =
                    commitHashMap.get(thisBranchCommit.getParentHashCode());
        }
        while (otherBranchCommit.getParentHashCode() != null) {
            if (thisBranchCommitHashes.contains(otherBranchCommit.getHash())) {
                splitPointHash = otherBranchCommit.getHash();
                return splitPointHash;
            }
            otherBranchCommit =
                    commitHashMap.get(otherBranchCommit.getParentHashCode());
        }
        splitPointHash = otherBranchCommit.getHash();
        return splitPointHash;
    }
    /**
     * Updates the end commit.
     * @param commit it's the commit
     */
    public void updateEndCommit(Commit commit) {
        if (isHead && endCommit != commit) {
            endCommit = commit;
        }
        writeToFile();
    }
    /**
     * switches the head.
     * @param branch1 it's the first branch
     * @param branch2 it's the second branch
     */
    public static void switchHead(Branch branch1, Branch branch2) {
        assert branch1.isHead || branch2.isHead;
        branch1.isHead = !branch1.isHead;
        branch2.isHead = !branch2.isHead;
        branch1.writeToFile();
        branch2.writeToFile();
    }
    /**
     * Writes to file.
     */
    public void writeToFile() {
        byte[] branchByteArray = Utils.serialize(this);
        File branchFile = new File(System.getProperty("user.dir")
                + "/.gitlet/branches/" + branchName);
        Utils.writeContents(branchFile, branchByteArray);
    }
}
