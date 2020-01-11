package gitlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Command class.
 * @author Thomas Nguyen
 */
public class Command {
    /**
     * The staging area.
     */
    private Repo stagingArea;
    /**
     * The head.
     */
    private Branch head;
    /**
     * Hashmap of all commits.
     */
    private HashMap<String, Commit> commitHashMap;
    /**
     * hashmap of all the blobs.
     */
    private HashMap<String, Blob> blobHashMap;
    /**
     * array list for branches.
     */
    private ArrayList<Branch> branches;
    /**
     * First operand input.
     */
    private static String operand;
    /**
     * Extra argument 1.
     */
    private static String arg1;

    /**
     * Extra argument 2.
     */
    private static String arg2;

    /**
     * your command class. you need this.
     * @param args is your arguments
     * @param stg is your staging area
     * @param hd is the head
     * @param blob is the hashmap for all your blobs
     * @param commit is the hashmap for you all your commits
     * @param brch is the branch arraylist
     */
    public Command(String[] args, Repo stg, Branch hd,
                   HashMap<String, Blob> blob,
                   HashMap<String, Commit> commit, ArrayList<Branch> brch) {
        this.stagingArea = stg;
        this.head = hd;
        this.commitHashMap = commit;
        this.blobHashMap = blob;
        this.branches = brch;
        operand = args[0];
        setArgs(args);
        Repo repo = new Repo();
        try {
            switch (operand) {
            case "add":
                stagingArea.add(args[1], head, blobHashMap);
                break;
            case "commit":
                if (args.length == 1 || args[1].equals("")) {
                    System.out.println("Please enter a commit message.");
                } else {
                    head.updateEndCommit(new Commit(head.getEndCommit(),
                            args[1], stagingArea, blobHashMap, commitHashMap));
                }
                break;
            case "rm":
                stagingArea.rm(args[1], head, blobHashMap);
                break;
            case "log":
                logHelper(head.getEndCommit());
                break;
            case "global-log":
                globalLogHelper();
                break;
            case "find":
                Commit.find(args[1], commitHashMap);
                break;
            case "status":
                status();
                break;
            case "checkout":
                checkoutHelper(args);
                break;
            case "branch":
                branchHelper(args[1], head);
                break;
            case "rm-branch":
                rmBranchHelper(args);
                break;
            case "reset":
                resetHelper(args[1]);
                break;
            case "merge":
                mergeHelper(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Incorrect operands.");
        }
    }
    /**
     * helper for checkout 1!
     * @param fileName is the name of the file
     */
    public void checkout1(String fileName) {
        File workingDir = new File(System.getProperty("user.dir"));
        for (String hash : head.getEndCommit().getTrackedBlobHashes()) {
            if (hash != null) {
                File blobFile2 = new File(System.getProperty("user.dir")
                        + "/.gitlet/blobs/" + hash);
                Blob curBlob = Utils.readObject(blobFile2, Blob.class);
                if (curBlob.getFileName().equals(fileName)) {
                    File blobFile = new File(workingDir + "/" + fileName);
                    Utils.writeContents(blobFile, curBlob.getData());
                    return;
                }
            }
        }
        System.out.println("File does not exist in that commit.");
    }
    /**
     * helper for checkout 2!
     * @param commitHash is the sha of commit
     * @param fileName is the name of the file
     */
    public void checkout2(String commitHash, String fileName) {
        boolean present = false;
        boolean actuallyCommit = false;
        File commitDirectory = new File(System.getProperty("user.dir")
                + "/.gitlet/commits/");
        File[] listOfFiles = commitDirectory.listFiles();
        for (File file : listOfFiles) {
            if (file.getName().equals(commitHash)) {
                actuallyCommit = true;
            }
        }
        if (actuallyCommit) {
            File commitFile = new File(System.getProperty("user.dir")
                    + "/.gitlet/commits/" + commitHash);
            Commit temp = Utils.readObject(commitFile, Commit.class);
            for (String hash : temp.getTrackedBlobHashes()) {
                File blobFile2 =
                        new File(System.getProperty("user.dir")
                                + "/.gitlet/blobs/" + hash);
                Blob curBlob = Utils.readObject(blobFile2, Blob.class);
                if (curBlob.getFileName().equals(fileName)) {
                    File blobFile = new File(fileName);
                    Utils.writeContents(blobFile, curBlob.getData());
                    present = true;
                }
            }
            if (!present) {
                System.out.println("File does not exist in that commit.");
            }
        } else {
            System.out.println("No commit with that id exists.");
        }
    }
    /** Recursive Function used to list all.
     * commits along current branch for the "log" command
     * @param args the arguments
     */
    public void rmBranchHelper(String[] args) {
        if (head.getBranchName().equals(args[1])) {
            System.out.println("Cannot remove the current branch.");
        } else {
            Branch.rmBranch(args[1]);
        }
    }

    /** Recursive Function used to list all commits.
     * along current branch for the "log" command
     */
    public void globalLogHelper() {
        File commitFolder = new File(".gitlet/commits");
        File[] commits = commitFolder.listFiles();
        for (File file : commits) {
            Commit comm = Utils.readObject(file, Commit.class);
            System.out.println("===");
            System.out.println("commit " + comm.getHash());
            System.out.println(comm.getTimestamp());
            System.out.println(comm.getLogMessage());
            System.out.println();
        }
    }
    /** Recursive Function used to list all commits
     * along current branch for the "log" command.
     * @param args the arguments
     */
    public void checkoutHelper(String[] args) {
        if (args[1].equals("--")) {
            checkout1(args[2]);
        } else if (args.length == 4) {
            if (args[2].equals("--")) {
                String unshorten = shortUID(args[1]);
                checkout2(unshorten, args[3]);
            } else {
                System.out.println("Incorrect Operands.");
            }
        } else {
            checkout3(args[1]);
        }
    }
    /**
     * helper for checkout 3!
     * @param branchName name of branch
     */
    public void checkout3(String branchName) {
        File workingDir = new File(System.getProperty("user.dir"));
        Branch givenBranch = null;
        try {
            boolean branchExist = false;
            for (Branch curr : branches) {
                if (curr.getBranchName().equals(branchName)) {
                    branchExist = true;
                    givenBranch = curr;
                }
            }
            if (!branchExist) {
                throw new NullPointerException();
            }
            if (branchName.equals(head.getBranchName())) {
                throw new FileNotFoundException();
            }
            for (String fileInDirectory : Utils.plainFilenamesIn(workingDir)) {
                Blob curBlob = new Blob(new File(fileInDirectory));
                String curBlobHash = curBlob.getHash();
                if (!head.getEndCommit().
                            containsFile(fileInDirectory,
                                    blobHashMap)
                            && !stagingArea.getHashToAdd().
                            values().contains(curBlobHash)) {
                    throw new IllegalStateException();
                }

            }
        } catch (NullPointerException e) {
            System.out.println("No such branch exists.");
            return;
        } catch (FileNotFoundException e) {
            System.out.println("No need to checkout the current branch.");
            return;
        } catch (IllegalStateException e) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it or add it first.");
            return;
        }
        Commit endCommit = givenBranch.getEndCommit();
        for (String blobHash : endCommit.getTrackedBlobHashes()) {
            File blobFile = new File(System.getProperty("user.dir")
                    + "/.gitlet/blobs/" + blobHash);
            Blob curBlob = Utils.readObject(blobFile, Blob.class);
            curBlob.track();
            Utils.writeContents(curBlob.getTextFile(), curBlob.getData());
        }
        for (File curFile : workingDir.listFiles()) {
            if (!curFile.isDirectory()) {
                if (!endCommit.containsFile(curFile.getName(), blobHashMap)) {
                    Utils.restrictedDelete(curFile);
                }
            }
        }
        Branch.switchHead(head, givenBranch);
    }
    /**
     * this function helps with branch!
     * @param name is for the name
     * @param curr is for the current branch
     */
    public void branchHelper(String name, Branch curr) {
        try {
            if (name != null) {
                for (Branch branch : branches) {
                    if (branch.getBranchName().equals(name)) {
                        throw new IllegalArgumentException();
                    }
                }

            } else {
                throw new NullPointerException();
            }
            new Branch(name, curr.getEndCommit());
        } catch (NullPointerException e) {
            System.out.println("No branch name given.");
        } catch (IllegalArgumentException e) {
            System.out.println("A branch with that name already exists.");
        }
    }
    /**
     * this function helps with reset.
     * @param commitHash1 is the sha of your commit
     */
    public void resetHelper(String commitHash1) {
        File workingDir = new File(System.getProperty("user.dir"));
        String commitHash = shortUID(commitHash1);
        try {
            Boolean found = false;
            for (String hash : commitHashMap.keySet()) {
                String abbreviation = hash.substring(0, commitHash.length());
                if (abbreviation.equals(commitHash)) {
                    found = true;
                    commitHash = hash;
                }
            }
            if (commitHashMap.get(commitHash) != null) {
                found = true;
            }
            if (!found) {
                throw new IllegalStateException();
            }
            for (String fileInDirectory : Utils.plainFilenamesIn(workingDir)) {
                File tempe = new File(fileInDirectory);
                if (!tempe.isDirectory()) {
                    String curBlobHash = new Blob(tempe).getHash();
                    if (!head.getEndCommit().
                            getTrackedBlobHashes().contains(curBlobHash)
                            && !stagingArea.getHashToAdd().
                            values().contains(curBlobHash)) {
                        throw new FileNotFoundException();
                    }
                }
            }
        } catch (IllegalStateException e) {
            System.out.println("No commit with that id exists.");
            return;
        } catch (FileNotFoundException e) {
            System.out.println(ohNo());
            return;
        }
        File parentFile = new File(System.getProperty("user.dir")
                + "/.gitlet/commits/" + commitHash);
        Commit foundCommit = Utils.readObject(parentFile, Commit.class);
        ArrayList<File> newfiles = new ArrayList<>();
        for (String hash : foundCommit.getTrackedBlobHashes()) {
            File blobFile = new File(System.getProperty("user.dir")
                    + "/.gitlet/blobs/" + hash);
            Blob curBlob = Utils.readObject(blobFile, Blob.class);
            File blobFile2 = new File(workingDir
                    + "/" + curBlob.getFileName());
            Utils.writeContents(blobFile2, curBlob.getData());
            newfiles.add(blobFile2);
        }
        for (File file : workingDir.listFiles()) {
            if (!newfiles.contains(file)) {
                Utils.restrictedDelete(file);
            }
        }
        head.updateEndCommit(foundCommit);
        stagingArea.getHashToAdd().clear();
        stagingArea.getHashesToRemove().clear();
        stagingArea.writeToFile();
    }
    /**
     * this function helps with log.
     * @param curr is the current commit
     */
    public void logHelper(Commit curr) {
        System.out.println("===");
        System.out.println("commit " + curr.getHash());
        System.out.println(curr.getTimestamp());
        System.out.println(curr.getLogMessage());
        System.out.println();

        if (curr.getParentHashCode() == null) {
            return;
        } else {
            File parentFile = new File(System.getProperty("user.dir")
                    + "/.gitlet/commits/" + curr.getParentHashCode());
            Commit temp = Utils.readObject(parentFile, Commit.class);
            logHelper(temp);
        }
    }
    /**
     * the status function that ya gotta write.
     */
    public void status() {
        System.out.println("=== Branches ===");
        ArrayList<String> branch = new ArrayList<String>();
        for (Branch b : branches) {
            if (b.isBranchHead()) {
                branch.add("*" + b.getBranchName());
            } else {
                branch.add(b.getBranchName());
            }
        }
        Object[] b = branch.toArray();
        Arrays.sort(b);
        for (Object br : b) {
            System.out.println(br);
        }
        System.out.println();
        System.out.println("=== Staged Files ===");


        HashMap<String, String> addedStagingArea =
                new HashMap<>(stagingArea.getHashToAdd());
        Object[] keys = addedStagingArea.keySet().toArray();
        Arrays.sort(keys);
        for (Object s : keys) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");

        HashMap<String, String> removedStagingArea =
                new HashMap<>(stagingArea.getHashesToRemove());
        Object[] keys2 = removedStagingArea.keySet().toArray();
        Arrays.sort(keys2);
        for (Object s : keys2) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");

        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }
    /**
     * you need this for merge.
     * @param branchName is the name of the branch
     */
    public void mergeHelper(String branchName) {
        Branch givenBranch = null;
        File workingDir = new File(System.getProperty("user.dir"));
        for (File fileInDirectory : workingDir.listFiles()) {
            if (!fileInDirectory.isDirectory()) {
                String curBlobHash = new Blob(fileInDirectory).getHash();
                if (!head.getEndCommit().
                        containsFile(fileInDirectory.getName(), blobHashMap)
                        && !stagingArea.getHashToAdd().
                        values().contains(curBlobHash)) {
                    System.out.println(
                            "There is an untracked "
                                    + "file in the way; "
                                    + "delete it or add it first.");
                    return;
                }
            }
        }
        for (Branch curr : branches) {
            if (curr.getBranchName().equals(branchName)) {
                givenBranch = curr;
            }
        }
        if (givenBranch == null) {
            System.out.println("A branch with that name does not exist.");
            return;
        } else if (!stagingArea.getHashesToRemove().isEmpty()
                || !stagingArea.getHashToAdd().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        } else if (givenBranch.equals(head)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        String splitPointHash =
                Branch.getSplitPointHash(head,
                givenBranch, commitHashMap);
        Commit splitPoint = commitHashMap.get(splitPointHash);
        if (splitPointHash.equals(givenBranch.getEndCommit().getHash())) {
            System.out.println("Given branch is "
                    + "an ancestor of the current branch.");
            return;
        } else if (splitPointHash.equals(head.getEndCommit().getHash())) {
            head.updateEndCommit(givenBranch.getEndCommit());
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        Commit givenCommit = givenBranch.getEndCommit();
        Commit curCommit = head.getEndCommit();
        mergeHelper2(branchName, givenCommit,
                curCommit, splitPoint, givenBranch);
    }

    /**
     * you need this for merge.
     * @param branchName is the name of the branch
     * @param givenCommit is the given commit
     * @param curCommit is the current commit
     * @param splitPoint is the splitpoint
     * @param givenBranch is the given branch
     */
    public void mergeHelper2(String branchName, Commit givenCommit,
                             Commit curCommit,
                             Commit splitPoint, Branch givenBranch) {
        boolean mergeConflictOccurrence = false;
        for (String blobHash : curCommit.getTrackedBlobHashes()) {
            Blob curBlob = blobHashMap.get(blobHash);
            String fileName = curBlob.getFileName();
            byte[] curBlobData = curBlob.getData();
            if (splitPoint.containsFile(fileName, blobHashMap)
                    && givenCommit.containsFile(fileName,
                    blobHashMap)) {
                byte[] splitPointBlobData =
                        splitPoint.getBlobWithName(fileName,
                                blobHashMap).getData();
                Blob givenBlob = givenCommit.getBlobWithName(fileName,
                        blobHashMap);
                byte[] givenBlobData = givenBlob.getData();
                if (Arrays.equals(curBlobData,
                        splitPointBlobData)
                        && !Arrays.equals(splitPointBlobData,
                        givenBlobData)) {
                    checkout2(givenCommit.getHash(), fileName);
                    stagingArea.add(fileName, head, blobHashMap);
                } else if (!Arrays.equals(givenBlobData,
                        splitPointBlobData)
                        && !Arrays.equals(curBlobData, givenBlobData)
                        && !Arrays.equals(curBlobData,
                        splitPointBlobData)) {
                    mergeConflict(curBlob.getTextFile(),
                            curBlobData, givenBlobData);
                    mergeConflictOccurrence = true;
                }
            } else if (splitPoint.containsFile(fileName,
                    blobHashMap)
                    && !givenCommit.containsFile(fileName,
                    blobHashMap)) {
                if (Arrays.equals(curBlobData,
                        splitPoint.getBlobWithName(fileName,
                                blobHashMap).getData())) {
                    stagingArea.rm(fileName, head,
                            blobHashMap);
                } else {
                    mergeConflict(curBlob.getTextFile(),
                            curBlobData, new byte[0]);
                    mergeConflictOccurrence = true;
                }
            } else if (!splitPoint.containsFile(fileName,
                    blobHashMap)
                    && givenCommit.containsFile(fileName,
                    blobHashMap)
                    && !Arrays.equals(curBlobData, givenCommit
                    .getBlobWithName(fileName,
                            blobHashMap).getData())) {
                mergeConflict(curBlob.getTextFile(), curBlobData,
                        givenCommit.getBlobWithName(fileName,
                                blobHashMap).getData());
                mergeConflictOccurrence = true;
            }
        }
        mergeHelper3(branchName, givenCommit, curCommit,
                splitPoint, mergeConflictOccurrence, givenBranch);
    }
    /**
     * you need this for merge.
     * @param branchName is the name of the branch
     * @param givenBranch is the name of the given branch
     * @param givenCommit is the given commit
     * @param curCommit is the current commit
     * @param splitPoint is the commit
     * @param mergeConflictOccurrence is a boolean
     */
    public void mergeHelper3(String branchName,
                             Commit givenCommit,
                             Commit curCommit,
                             Commit splitPoint,
                             Boolean mergeConflictOccurrence,
                             Branch givenBranch) {
        for (String blobHash : givenCommit.getTrackedBlobHashes()) {
            Blob curBlob = blobHashMap.get(blobHash);
            String fileName = curBlob.getFileName();
            byte[] curBlobData = curBlob.getData();
            if (!splitPoint.containsFile(fileName, blobHashMap)
                    && !curCommit.containsFile(fileName, blobHashMap)) {
                checkout2(givenCommit.getHash(), fileName);
                stagingArea.add(fileName, head, blobHashMap);
            } else if (splitPoint.containsFile(fileName, blobHashMap)
                    && !curCommit.containsFile(fileName, blobHashMap)
                    && !Arrays.equals(curBlobData,
                    splitPoint.getBlobWithName(fileName,
                            blobHashMap).getData())) {
                mergeConflict(curBlob.getTextFile(), new byte[0], curBlobData);
                mergeConflictOccurrence = true;
            }
        }
        createMergeCommit(mergeConflictOccurrence, givenBranch);
    }
    /**
     * code to protect against merge conflicts!
     * @param file is for the file.
     * @param curBlobData is for the current Blob's data
     * @param givenBlobData is the given blob's data
     *
     */
    public void mergeConflict(File file,
                              byte[] curBlobData, byte[] givenBlobData) {
        byte[] header = "<<<<<<< HEAD\n".getBytes(StandardCharsets.UTF_8);
        byte[] separator = "=======\n".getBytes(StandardCharsets.UTF_8);
        byte[] footer = ">>>>>>>\n".getBytes(StandardCharsets.UTF_8);
        byte[] conflictFileData = new byte[curBlobData.length
                + header.length + separator.length
                + givenBlobData.length + footer.length];
        System.arraycopy(header, 0,
                conflictFileData, 0, header.length);
        System.arraycopy(curBlobData, 0,
                conflictFileData, header.length, curBlobData.length);
        System.arraycopy(separator, 0,
                conflictFileData, header.length
                        + curBlobData.length, separator.length);
        System.arraycopy(givenBlobData, 0,
                conflictFileData, header.length
                        + curBlobData.length
                        + separator.length, givenBlobData.length);
        System.arraycopy(footer, 0, conflictFileData,
                header.length + curBlobData.length
                        + separator.length
                        + givenBlobData.length, footer.length);
        Utils.writeContents(file, conflictFileData);
        System.out.println("Encountered a merge conflict.");
    }

    /**
     * holy this helps with merge commit so much.
     * @param mergeConflictOccurrence is an occurence
     * @param givenBranch is the branch you're given
     */
    public void createMergeCommit(boolean mergeConflictOccurrence,
                                  Branch givenBranch) {
        if (!mergeConflictOccurrence) {
            head.updateEndCommit(new Commit(head.getEndCommit(),
                    "Merged " + head.getBranchName()
                    + " with " + givenBranch.getBranchName() + ".",
                    stagingArea, blobHashMap, commitHashMap));
        }
    }

    /** Set the arguments from the args.
     * @param args is for the arguments put in
     */
    public static void setArgs(String...args) {
        if (args.length > 1) {
            arg1 = args[1];
        }
        if (args.length > 2) {
            arg2 = args[2];
        }
    }
    /** Set the arguments from the args.
     * @param s is for the arguments put in
     * @return returns a string
     */
    public static String shortUID(String s) {
        File commitDirectory = new File(System.getProperty("user.dir")
                + "/.gitlet/commits/");
        File[] listOfFiles = commitDirectory.listFiles();
        for (File file : listOfFiles) {
            if (file.getName().contains(s)) {
                return file.getName();
            }
        }
        System.out.println("No commit with that id exists.");
        System.exit(0);
        return null;
    }
    /** Set the arguments from the args.
     * @return returns a string
     */
    public static final String ohNo() {
        return "There is an untracked file in the way; "
                + "delete it or add it first.";
    }
}
