package gitlet;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;

/** Driver class for Gitlet, the tiny version-control system.
 *  @author Thomas Nguyen
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args)
            throws IOException, ClassNotFoundException {
        File workingDir = new File(System.getProperty("user.dir"));
        File gitletDir = new File(workingDir, ".gitlet");
        File commitDir = new File(gitletDir, "commits");
        File blobDir = new File(gitletDir, "blobs");
        File branchDir = new File(gitletDir, "branches");
        File stagingAreaFile = new File(gitletDir, "staging_area");
        if (args.length == 0) {
            System.out.println("Please enter a command");
            System.exit(0);
        }
        if (args[0].equals("init")) {
            try {
                boolean dirAlreadyExists = !gitletDir.mkdir();
                if (dirAlreadyExists) {
                    throw new InstantiationException();
                }
            } catch (InstantiationException e) {
                System.out.println(
                        "A gitlet version-control system "
                                + "already exists in the current directory.");
            }
            commitDir.mkdir(); blobDir.mkdir(); branchDir.mkdir();
            Utils.writeContents(stagingAreaFile, Utils.serialize(new Repo()));
            Commit head = new Commit("initial commit",
                            "Thu Jan 1 00:00:00 1970 -0800");
            new Branch(head);
        } else {
            HashMap<String, Commit> commitHashMap = new HashMap<>();
            HashMap<String, Blob> blobHashMap = new HashMap<>();
            fillHashMap(commitDir, commitHashMap);
            fillHashMap(blobDir, blobHashMap);
            Branch head = null;
            ArrayList<Branch> branches = new ArrayList<>();
            for (File branch : branchDir.listFiles()) {
                try {
                    ObjectInputStream inp = new ObjectInputStream(new
                            FileInputStream(branch));
                    Branch curBranch = (Branch) inp.readObject();
                    branches.add(curBranch);
                    if (curBranch.isBranchHead()) {
                        head = curBranch;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Error with setting head branch");
                }
            }
            Repo stagingArea = null;
            try {
                ObjectInputStream input
                        = new ObjectInputStream(
                                new FileInputStream(stagingAreaFile));
                stagingArea = (Repo) input.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error with getting StagingArea");
            }
            new Command(args, stagingArea, head,
                    blobHashMap, commitHashMap, branches);
        }
    }

    /**
     * fills the hashmap.
     * @param gitletDir the gitlet directory
     * @param hashes the hashes
     */
    public static void fillHashMap(File gitletDir, HashMap hashes) {
        for (File inFile : gitletDir.listFiles()) {
            if (inFile.isDirectory()) {
                fillHashMap(inFile, hashes);
            } else {
                hashes.put(inFile.getName(), Utils.readContents(inFile));
            }
        }
    }
}




