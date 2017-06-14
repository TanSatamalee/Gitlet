import java.text.SimpleDateFormat;
import java.io.File;
import java.io.Console;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.Serializable;
import java.io.FileNotFoundException;
import java.util.TreeMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Date;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Gitlet implements Serializable {

    MasterGit masterFiles;
    MasterGit currentMaster;
    StageMaster stagedFiles;
    StageMasterTM stagedFilesTM;
    MasterBranch branches;

    /*Executes a UI like behavior for Gitlet
    */
    public static void main(String[] args) {
        Gitlet og = new Gitlet();
        if (args.length > 0) {
            og.gitInitialize();
            og.setUpCurrentMaster();
            switch (args[0]) {
                case "init":
                    og.initialize();
                    og.gitFinalize();
                    return;
                case "add":
                    if (args.length > 1) {
                        og.add(args[1]);
                    } else {
                        System.out.println("Please enter file name");
                    }
                    og.gitFinalize();
                    return;
                case "commit":
                    if (args.length > 1) {
                        og.commit(args[1]);
                    } else {
                        System.out.println("Please enter a commit message");
                    }
                    og.gitFinalize();
                    return;
                case "rm":
                    og.remove(args[1]);
                    return;
                case "checkout":
                    og.checkout(args);
                    og.gitFinalize();
                    return;
                case "log":
                    og.log();
                    return;
                case "global-log":
                    og.globalLog();
                    return;
                case "find":
                    og.find(args[1]);
                    return;
                case "status":
                    og.status();
                    return;
                case "branch":
                    og.branch(args[1]);
                    og.gitFinalize();
                    return;
                case "rm-branch":
                    og.rmbranch(args[1]);
                    og.gitFinalize();
                    return;
                case "reset":
                    og.reset(args[1]);
                    og.gitFinalize();
                    return;
                case "merge":
                    og.merge(args[1]);
                    og.gitFinalize();
                    return;
                case "rebase":
                    og.rebase(args[1]);
                    og.gitFinalize();
                    return;
                case "i-rebase":
                    og.interactiveRebase(args[1]);
                    og.gitFinalize();
                    return;
                default:
                    return;
            }
        }
    }

    /*Constructor for Gitlet class that holds instance variables to use in main class
        masterFile = main Gitlet object stores commit names, id, commit messages
        currentMaster = the location of the current MasterGit being used
        stagedFiles = contains all staged files and name (includes add and remove)
        stagedFiles(TM) = treemap for easier access to file name and add or remove
        branches = contains treemap of all branches with OG being the current branch on and master
            the original branch when created
    */
    public Gitlet() {
        masterFiles = null;
        currentMaster = null;
        stagedFiles = null;
        stagedFilesTM = new StageMasterTM();
        branches = new MasterBranch();
    }

    /*Begins Gitlet by using Serialization to initialize the main already commit files, 
          the currently staged files, and the previously staged files
    */
    public void gitInitialize() {
        try {
            File master = new File(".gitlet/master.ser");
            File stage = new File(".gitlet/stagedFiles.ser");
            File branch = new File(".gitlet/branches.ser");
            File smtm = new File(".gitlet/smtm.ser");
            if (master.exists()) {
                FileInputStream masterIn = new FileInputStream(".gitlet/master.ser");
                ObjectInputStream mIn = new ObjectInputStream(masterIn);
                masterFiles = (MasterGit) mIn.readObject();
                mIn.close();
                masterIn.close();
            }
            if (stage.exists()) {
                FileInputStream stagedIn = new FileInputStream(".gitlet/stagedFiles.ser");
                ObjectInputStream sIn = new ObjectInputStream(stagedIn);
                stagedFiles = (StageMaster) sIn.readObject();
            }
            if (branch.exists()) {
                FileInputStream branchin = new FileInputStream(
                                    ".gitlet/branches.ser");
                ObjectInputStream brIn = new ObjectInputStream(branchin);
                branches = (MasterBranch) brIn.readObject();
            }
            if (smtm.exists()) {
                FileInputStream smtmIn = new FileInputStream(
                                    ".gitlet/xmtm.ser");
                ObjectInputStream smIn = new ObjectInputStream(smtmIn);
                stagedFilesTM = (StageMasterTM) smIn.readObject();
            }
        } catch (IOException i) {
            return;
        } catch (ClassNotFoundException c) {
            return;
        }
    }

    public void setUpCurrentMaster() {
        if ((branches == null) || (!branches.contains("OG"))) {
            currentMaster = null;
        } else {
            String ogId = branches.value("OG");
            currentMaster = findTheGit(ogId, masterFiles);
        }
    }

    public void gitFinalize() {
        try {
            FileOutputStream masterIn = new FileOutputStream(".gitlet/master.ser");
            ObjectOutputStream mIn = new ObjectOutputStream(masterIn);
            mIn.writeObject(masterFiles);
            mIn.close();
            masterIn.close();

            FileOutputStream stagedIn = new FileOutputStream(".gitlet/stagedFiles.ser");
            ObjectOutputStream sIn = new ObjectOutputStream(stagedIn);
            sIn.writeObject(stagedFiles);
            sIn.close();
            stagedIn.close();

            FileOutputStream branchin = new FileOutputStream(".gitlet/branches.ser");
            ObjectOutputStream brIn = new ObjectOutputStream(branchin);
            brIn.writeObject(branches);
            brIn.close();
            branchin.close();

            FileOutputStream smtmIn = new FileOutputStream(".gitlet/xmtm.ser");
            ObjectOutputStream smIn = new ObjectOutputStream(smtmIn);
            smIn.writeObject(stagedFilesTM);
            smIn.close();
            smtmIn.close();
        } catch (IOException e) {
            return;
        }
    }

    public void testWriteObject() throws FileNotFoundException, IOException {
        FileOutputStream masterIn = new FileOutputStream(".gitlet/master.ser");
        ObjectOutputStream mIn = new ObjectOutputStream(masterIn);
        mIn.writeObject(masterFiles);
        mIn.writeObject(currentMaster);
        mIn.close();
        masterIn.close();

    }

    /*Basic constructor for the main already commit files that will be serialized
    */
    public class MasterGit implements Serializable {
        MasterGit previous;
        String message;
        Entry<String> commits;
        String id;
        String time;
        Entry<MasterGit> next;

        public MasterGit(MasterGit previous0, String message0, Entry<String> commits0,
                            String id0, String time0, Entry<MasterGit> next0) {
            previous = previous0;
            message = message0;
            commits = commits0;
            id = id0;
            time = time0;
            next = next0;
        }

        public void globalLogMG() {
            String[] ds = this.id.split("\\.");
            if (!ds[ds.length - 1].equals("0")  || this.id.equals("0")) {
                System.out.println("====");
                System.out.println("Commit " + this.id);
                System.out.println(this.time);
                System.out.println(this.message);
                System.out.println();
            }
            Entry<MasterGit> temp = this.next;
            while (temp != null) {
                temp.value.globalLogMG();
                temp = temp.next00;
            }
        }

        public void findMG(String message0) {
            if (this.message != null) {
                if (this.message.equals(message0)) {
                    System.out.println(id);
                }
            }
            Entry<MasterGit> temp = this.next;
            while (temp != null) {
                if (temp.value != null) {
                    temp.value.findMG(message0);
                }
                temp = temp.next00;
            }
        }
    }

    protected Entry<String> findCommitsMG(String id0, MasterGit xxxx) {
        if (xxxx != null) {
            if (xxxx.id.equals(id0)) {
                return xxxx.commits;
            }
            Entry<MasterGit> temp = xxxx.next;
            while ((temp != null) && (temp.value != null)) {
                Entry<String> temp2 = findCommitsMG(id0, temp.value);
                if (temp2 != null) {
                    return temp2;
                }
                temp = temp.next00;
            }
        }
        return null;
    }

    protected MasterGit findTheGit(String id0, MasterGit xxx) {
        if (xxx != null) {
            if (xxx.id != null) {
                if (xxx.id.equals(id0)) {
                    return xxx;
                }
            }
            Entry<MasterGit> temp2 = xxx.next;
            while ((temp2 != null) && (temp2.value != null)) {
                MasterGit temp3 = findTheGit(id0, temp2.value);
                if (temp3 != null) {
                    return temp3;
                }
                temp2 = temp2.next00;
            }
        }
        return null;
    }

    protected class Entry<X> implements Serializable {
        X value;
        Entry next00;
            
        public Entry() {
            value = null;
            next00 = null;
        }

        public Entry(X value0, Entry<X> next000) {
            value = value0;
            next00 = next000;
        }

        public Entry<X> add(X value0) {
            return new Entry(value0, this);
        }

        public boolean hasValue(X value00) {
            Entry x = this;
            while (!value00.equals(x.value)) {
                x = x.next00;
            }
            if (x == null) {
                return false;
            }
            return true;
        }
    }

    /*Basic constructor for the staged files and the staged files of the last commit
    */
    public class StageMaster implements Serializable {
        boolean isAdd;
        String name;
        File file;
        StageMaster next;

        public StageMaster(boolean isAdd0, File file0, StageMaster next0,
                            String name0) {
            isAdd = isAdd0;
            name = name0;
            file = file0;
            next = next0;
        }

        public boolean isEquals(String name00, File compare, boolean isAdd00) {
            StageMaster x = this;
            while (x != null) {
                if (!x.name.equals(name00)) {
                    x = x.next;
                } else {
                    break;
                }
            }
            if (x == null) {
                return false;
            }
            return x.file.equals(compare);
        }

        public void remove(String fileName, boolean isAdd000) {
            StageMaster x = this;
            if ((x != null) && (x.name != null) && (x.file != null) && (x.next != null)) {
                if (x.name.equals(fileName)) {
                    if (x.isAdd && !isAdd000) {
                        this.isAdd = x.next.isAdd;
                        this.name = x.next.name;
                        this.file = x.next.file;
                        this.next = x.next.next;
                        return;
                    } else if (!x.next.isAdd && isAdd000) {
                        this.isAdd = x.next.isAdd;
                        this.name = x.next.name;
                        this.file = x.next.file;
                        this.next = x.next.next;
                        return;
                    } else {
                        return;
                    }
                }
                while ((x.next != null) && (x.next.file != null) && (x.next.next != null)) {
                    if (x.next.name.equals(fileName)) {
                        if (x.next.isAdd && !isAdd000) {
                            x = x.next.next;
                            return;
                        } else if (!x.next.isAdd && isAdd000) {
                            x = x.next.next;
                            return;
                        } else {
                            return;
                        }
                    }
                    x = x.next;
                }
            }
        }
    }

    public class StageMasterTM extends TreeMap<String, Boolean> implements Serializable {

        public StageMasterTM() {
            super();
        }

        public void add(String name, Boolean isAdd) {
            super.put(name, isAdd);
        }

        public boolean contains(String name) {
            return super.containsKey(name);
        }

        public Boolean value(String name) {
            return super.get(name);
        }

        public Set<String> getSet() {
            return super.keySet();
        }

        public void remove(String name) {
            super.remove(name);
        }

        public void removeTM(String fileName, boolean isAdd) {
            if (contains(fileName)) {
                boolean truth = value(fileName).booleanValue();
                if (truth && !isAdd) {
                    remove(fileName);
                    return;
                } else if (!truth && isAdd) {
                    remove(fileName);
                    return;
                } else {
                    return;
                }
            }
        }
    }

    /* Constructor for serialization of location of each branch pointer by referencing
        branch name with id number.
    */
    public class MasterBranch extends TreeMap<String, String> implements Serializable {

        public MasterBranch() {
            super();
        }

        public void add(String name, String id) {
            super.put(name, id);
        }

        public boolean contains(String name) {
            return super.containsKey(name);
        }

        public String value(String name) {
            return super.get(name);
        }

        public Set<String> keys() {
            return super.keySet();
        }

        public void removeBranch(String name) {
            String removed = super.remove(name);
        }
    }

    protected void fileTransfer(String destination, String extract) {
        try {
            FileWriter fileWriter = new FileWriter(destination);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            FileReader fileReader = new FileReader(extract);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                bufferedWriter.write(line);
            }
            bufferedReader.close();
            fileReader.close();
            bufferedWriter.close();
            fileWriter.close();
        } catch (FileNotFoundException e) {
            System.out.println("File does not exist");
        } catch (IOException ioe) {
            System.out.println("File does not exist");
        }
    }

    /* Main method for Initialization
            Creates .gitlet directory if not yet created and adds initial commit
    */
    public void initialize() {
        File theDir = new File(".gitlet");
        if (!theDir.exists()) {
            try {
                theDir.mkdir();
            } catch (SecurityException se) {
                System.out.println("Cannot create .gitlet directory");
            }
        } else {
            String a = "A gitlet version control system already exists in the current directory";
            System.out.println(a);
        }
        add(null);
        commit("initial commit");
    }

    /* Main method for Adding files to Stage
            Checks to see if files exists and if it changed from previous commits
            Adds File to stagedFiles if true for both statements
        Idea: Adds file to the stagedFiles and removes any marked remove
            creates an instance of the file in the .gitlet but not officially a commit
    */
    public void add(String fileName) {
        String actualName = null;
        if (fileName != null) {
            String[] fileSplit = fileName.split("/");
            if (fileSplit.length > 1) {
                actualName = fileSplit[fileSplit.length - 1];
            } else {
                actualName = fileName;
            }
        }
        if (fileName == null) {
            stagedFiles = new StageMaster(true, null, stagedFiles, "initial");
            stagedFilesTM.add("initial", true);
            branches.add("master", "0");
            branches.add("OG", "0");
            return;
        } else {
            File fileAdd = new File(fileName);
            if (fileAdd.exists()) {
                if ((currentMaster != null) && (currentMaster.previous != null)
                                && (currentMaster.previous.commits != null)) {
                    if (!this.isFileCopy(fileName, fileAdd)) {
                        fileTransfer(".gitlet/" + actualName, fileName);
                        File adder = new File(".gitlet/" + actualName);
                        stagedFiles = new StageMaster(true, adder, stagedFiles, fileName);
                        stagedFilesTM.add(fileName, true);
                        stagedFiles.remove(fileName, true);
                        stagedFilesTM.removeTM(fileName, true);
                    } else {
                        System.out.println("File has not been modified since the last commit");
                    }
                } else {
                    fileTransfer(".gitlet/" + actualName, fileName);
                    File adder = new File(".gitlet/" + actualName);
                    stagedFiles = new StageMaster(true, adder, stagedFiles, fileName);
                    stagedFilesTM.add(fileName, true);
                    stagedFiles.remove(fileName, true);
                    stagedFilesTM.removeTM(fileName, true);
                }
            } else {
                System.out.println("File does not exist");
            }
        }
    }

    protected boolean isFileCopy(String fileName, File fileAdd) {
        Entry<String> keys = currentMaster.previous.commits;
        while ((keys != null) && (keys.value != null)) {
            if (keys.value.equals(fileName)) {
                File fileAdder = new File(keys.value);
                if (fileAdd.equals(fileAdder)) {
                    return true;
                }
            }
            keys = keys.next00;
        }
        return false;
    }

    /*Commit main method
        Idea: checks to see if message is valid and then logs all files in stagedFiles
            adds one more node to masterFiles and moves currentMaster to the new node
            sets stagedFiles to null
            creates files in .gitlet folder with id + name as notation for file name
    */
    public void commit(String message) {
        if (message.equals("initial commit")) {
            stagedFiles = null;
            masterFiles = new MasterGit(null, message, null, "0", writeTime(), null);
            currentMaster = masterFiles;
            return;
        } else if (stagedFiles == null) {
            System.out.println("No changes added to the commmit.");
            return;
        } else if ((message == null) || message.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }
        String oldID = currentMaster.id;
        String currentID = newID(currentMaster.id);
        MasterGit temp = new MasterGit(currentMaster, message, null, currentID, writeTime(), null);
        if (currentMaster.next != null) {
            currentMaster.next.add(temp);
        } else {
            currentMaster.next = new Entry(temp, null);
        }
        currentMaster = currentMaster.next.value;
        if (currentMaster.commits == null) {
            currentMaster.commits = new Entry<String>();
        }
        Entry<String> keys = currentMaster.previous.commits;
        while ((keys != null) && (keys.value != null)) {
            String nex = keys.value;
            String[] ds = nex.split("\\."); String actualNex = null;
            if (ds.length > 1) {
                actualNex = ds[ds.length - 2] + ds[ds.length - 1];
            } else {
                actualNex = nex;
            }
            if (!stagedFilesTM.contains(actualNex)) {
                if ((nex != null) && (!nex.equals("initial"))) {
                    currentMaster.commits = currentMaster.commits.add(nex);
                }
            }
            keys = keys.next00;
        }
        StageMaster temp2 = stagedFiles;
        while (temp2 != null) {
            String[] dataSpliter = temp2.name.split("/"); String actualName = null;
            if (dataSpliter.length > 1) {
                actualName = dataSpliter[dataSpliter.length - 1];
            } else {
                actualName = temp2.name;
            }
            if (temp2.isAdd) {
                currentMaster.commits = currentMaster.commits.add(currentID + "." + actualName);
                fileTransfer(".gitlet/" + currentID + "." + actualName, temp2.name);
            }
            temp2 = temp2.next;
        }
        Set<String> keySet = branches.keys();
        Iterator<String> keyIter = keySet.iterator();
        while (keyIter.hasNext()) {
            String xx = keyIter.next();
            if ((branches.value(xx).equals(branches.value("OG"))) && (!xx.equals("OG"))) {
                branches.add(xx, currentID);
            }
        }
        branches.add("OG", currentID);
        stagedFiles = null;
        stagedFilesTM = new StageMasterTM();
    }

    /*Creates new ID String from old ID String by adding 1 to the old ID in the last digit
    */
    protected String newID(String ogID) {
        String[] dataSplit = ogID.split("\\.");
        int foo = Integer.parseInt(dataSplit[dataSplit.length - 1]);
        dataSplit[dataSplit.length - 1] = Integer.toString(foo + 1);
        String answer = dataSplit[0];
        for (int i = 1; i < dataSplit.length; i += 1) {
            answer += "." + dataSplit[i];
        }
        return answer;
    }

    /*Returns of String formated with current MM-dd-yyyy h:mm:ss
    */
    protected String writeTime() {
        Date date = new Date();
        SimpleDateFormat fd = new SimpleDateFormat("MM-dd-yyyy h:mm:ss");
        return fd.format(date);
    }

    /*Main method of remove that removes the file added in stagedFiles and mark for removal
        the file marked for remove does not continue on in the commit main method to new node
    */
    public void remove(String fileName) {
        if (stagedFilesTM.contains(fileName) && stagedFilesTM.value(fileName)) {
            stagedFilesTM.removeTM(fileName, false);
            stagedFiles.remove(fileName, false);
            stagedFiles = new StageMaster(false, null, stagedFiles, fileName);
            stagedFilesTM.add(fileName, false);        
        } else {
            System.out.println("No reason to remove the file");
            return;
        }
    }

    /*Main method of log by printing out each MasterGit in format from currentMaster to front
        by using the previous link to the previous node in MasterGit
    */
    public void log() {
        MasterGit temp = currentMaster;
        while (temp != null) {
            String[] ds = temp.id.split("\\.");
            if (!ds[ds.length - 1].equals("0") || temp.id.equals("0")) {
                System.out.println("====");
                System.out.println("Commit " + temp.id);
                System.out.println(temp.time);
                System.out.println(temp.message);
                System.out.println();
            }
            temp = temp.previous;
        }
    }

    //Runs global-log argument
    public void globalLog() {
        MasterGit temp = masterFiles;
        temp.globalLogMG();
    }

    //Runs find argument
    public void find(String message) {
        MasterGit temp = masterFiles;
        temp.findMG(message);
    }

    //Runs status argument
    public void status() {
        //Prints out branches
        System.out.println("=== Branches ===");
        MasterBranch temp = branches;
        Set<String> setK = temp.keys();
        Iterator<String> iterK = setK.iterator();
        while (iterK.hasNext()) {
            String branchx = iterK.next();
            if (!branchx.equals("OG")) {
                System.out.println(branchx);
            }
        }
        System.out.println();

        //Prints out stagedFiles added
        System.out.println("=== Staged Files ===");
        StageMaster temp2 = stagedFiles;
        while (temp2 != null) {
            if (temp2.isAdd) {
                System.out.println(temp2.name);
            }
            temp2 = temp2.next;
        }
        System.out.println();

        //Prints out Removal Files
        System.out.println("=== Files Marked for Removal ===");
        StageMaster temp3 = stagedFiles;
        while (temp3 != null) {
            if (!temp3.isAdd) {
                System.out.println(temp3.name);
            }
            temp3 = temp3.next;
        }
    }

    /*Main method for CheckingOut
            Analyzes arguments to see which type of checkout and runs that type
    */
    public void checkout(String[] args) {
        if (args.length == 3) {
            this.checkout2(args[1], args[2]);
            return;
        } else {
            if (branches.contains(args[1])) {
                this.checkout3(args[1]);
                return;
            }
            this.checkout1(args[1]);
        }
    }

    /*Checkout for only file name scenerio:
        Understandment: Restores the original file in the commit before to current directory
    */
    public void checkout1(String fileName) {
        Entry<String> commit = currentMaster.commits;
        String x = "File does not exist in the most recent commit, or no such branch exists";
        while (commit != null) {
            if (commit.value != null) {
                String[] z = commit.value.split("\\.");
                String[] k = fileName.split("/"); String zz = null;
                if (k.length > 1) {
                    zz = k[k.length - 1];
                } else {
                    zz = fileName;
                }
                String[] y = zz.split("\\.");
                if (z[z.length - 2].equals(y[0])) {
                    fileTransfer(fileName, ".gitlet/" + commit.value);
                    return;
                }
            }
            commit = commit.next00;
        }
        System.out.println(x);
    }

    /*Checkout for commit id and file name scenerio:
        Understandment: Restores the file at the commit to the directory
    */
    public void checkout2(String id, String fileName) {
        String actualName;
        String[] fileSplit = fileName.split("/");
        if (fileSplit.length > 1) {
            actualName = fileSplit[fileSplit.length - 1];
        } else {
            actualName = fileName;
        }
        File checkingOut = new File(".gitlet/" + id + "." + actualName);
        if (checkingOut.exists()) {
            fileTransfer(fileName, ".gitlet/" + id + "." + actualName);
        } else {
            if (findCommitsMG(id, masterFiles) == null) {
                System.out.println("No commit with that id exists");
            } else {
                System.out.println("File does not exist in that commit");
            }
        }
    }

    /*Checkout for branch name:
        Understandment: Restores all files with the file name at the branch head
    */
    public void checkout3(String branchName) {
        if (branches.contains(branchName)) {
            String y = branches.value(branchName);
            if (!y.equals(branches.value("OG"))) {
                branches.add("OG", y);
                Entry<String> changeCommits = findCommitsMG(y, masterFiles);
                while ((changeCommits != null) && (changeCommits.value != null)) {
                    String z = changeCommits.value;
                    String[] dataSplit = z.split("\\.");
                    String fileName = dataSplit[dataSplit.length - 2];
                    String fileSuffix = dataSplit[dataSplit.length - 1];
                    File currentDirectory = new File(fileName + "." + fileSuffix);
                    if (currentDirectory.exists()) {
                        fileTransfer(fileName + "." + fileSuffix, ".gitlet/" + z);
                    }
                    changeCommits = changeCommits.next00;
                }
            } else {
                System.out.println("No need to checkout the current branch");
            }
        } else {
            String x = "File does not exist in the most recent commit, or no such branch exists";
            System.out.println(x);
        }
    }

    /*Creates new Branch in masterFiles by creating essentially a new node that has
        null for its components and ID ending with a 0 denoting beginning of a branch
        Ultimately creates a branch by adding to the branches object with the current
        ID the node is pointing at (the beginning of th branch)
    */
    public void branch(String branchName) {
        Set<String> keySet = branches.keys();
        Iterator<String> keyIter = keySet.iterator();
        String currentID = null;
        while (keyIter.hasNext()) {
            String search = keyIter.next();
            if (search.equals(branchName)) {
                System.out.println("A branch with that name already exists");
                return;
            } else if (search.equals("OG")) {
                currentID = branches.value(search);
            }
        }
        if (currentID != null) {
            branches.add(branchName, currentID + ".0");
            currentID = currentID + ".0";
            MasterGit temp = new MasterGit(currentMaster, null, null, currentID, null, null);
            if (currentMaster.next != null) {
                currentMaster.next.add(temp);
            } else {
                currentMaster.next = new Entry(temp, null);
            }
        }
    }

    /*Removes the pointer to the branch (note that the branch is still accessible but
        not through direct reference of a pointer
        This function only takes the pointer away in the branches object
    */
    public void rmbranch(String branchName) {
        if (branches.contains(branchName)) {
            if (!branches.value(branchName).equals(branches.value("OG"))) {
                branches.remove(branchName);
            } else {
                System.out.println("Cannot remove the current branch");
            }
        } else {
            System.out.println("A branch with that name does not exist");
        }
    }

    /* Implements reset by looking for the MasterGit in the whole masterFiles object
        with the correct id and changing all avaliable files into that version
        by going through the commit Entry<String> object of that MasterGit
    */
    public void reset(String id) {
        Entry<String> commits = findCommitsMG(id, masterFiles);
        if (commits == null) {
            System.out.println("No commit with that id exists");
        } else {
            Entry<String> x = commits;
            while ((x != null) && (x.value != null)) {
                String[] y = x.value.split("\\.");
                fileTransfer(y[y.length - 2] + "." + y[y.length - 1], ".gitlet/" + x.value);
                x = x.next00;
            }
        }
    }

    /* Implements merge by looking for the common ancestory between two MasterGit
        by using the function locateLink below and getting the ID of that ancestor
        then it compares each file in the .gitlet folder between the current, ancestor,
        and given branch with the same file name in the other ones
    */
    public void merge(String branchName) {
        System.out.println("PRINTING FOR MERGE:");
        if (branches.contains(branchName)) {
            if (!branches.value(branchName).equals(branches.value("OG"))) {
                String ancestorID = locateLink(branchName);
                String currentID = branches.value("OG");
                String mergeID = branches.value(branchName);
                Entry<String> ancestorFiles = findCommitsMG(ancestorID, masterFiles);
                while ((ancestorFiles != null) && (ancestorFiles.value != null)) {
                    String actualName = actualName(ancestorFiles.value);
                    String ancestorName = ".gitlet/" + ancestorFiles.value;
                    String currentName = ".gitlet/" + currentID + "." + actualName;
                    String mergeName = ".gitlet/" + mergeID + "." + actualName;
                    if (isEquals(currentName, ancestorName) && !isEquals(mergeName, ancestorName)) {
                        fileTransfer(actualName, mergeName);
                    } else if (!isEquals(currentName, ancestorName)) {
                        if (!isEquals(mergeName, ancestorName)) {
                            fileTransfer(actualName + ".conflicted", mergeName);
                        }
                    }
                    ancestorFiles = ancestorFiles.next00;
                }
            } else {
                System.out.println("Cannot merge a branch with itself");
            }
        } else {
            System.out.println("A branch with that name does not exist");
        }
    }

    /* Returns whether two files contain the same text as each other
        This code is influenced by the getText method of GitletPublicTest that was given
    */
    protected boolean isEquals(String fileName1, String fileName2) {
        String file1; String file2;
        try {
            byte[] encoded1 = Files.readAllBytes(Paths.get(fileName1));
            byte[] encoded2 = Files.readAllBytes(Paths.get(fileName2));
            file1 = new String(encoded1, StandardCharsets.UTF_8);
            file2 = new String(encoded2, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return false;
        }
        return file1.equals(file2);
    }

    /* Disects the full name of a file into the actual name
        Example: 1.1.1.12.4.2.wug.txt ==> wug.txt *which is what it should be named
            in the current directory
    */
    protected String actualName(String fullName) {
        String[] x = fullName.split("/");
        String[] y;
        if (x != null) {
            y = x[x.length - 1].split("\\.");
        } else {
            y = fullName.split("\\.");
        }
        String answer = y[y.length - 2] + "." + y[y.length - 1];
        return answer;
    }

    /*Finds ID of the most common ancestor in MasterGit
        Idea: Searches for the lowest common numbers in a row from front to back of both IDs
            If no common numbers then adds the lowest most front number between the two
    */
    protected String locateLink(String branch1) {
        if (!branches.contains(branch1)) {
            System.out.println("A branch with that name does not exist");
            return null;
        }
        String id1 = branches.value(branch1);
        String id2 = branches.value("OG");
        String[] idSplit1 = id1.split("\\.");
        String[] idSplit2 = id2.split("\\.");
        int dom; int other;
        int lengthDifference = idSplit2.length - idSplit1.length;
        if (lengthDifference > 0) {
            dom = idSplit2.length;
            other = idSplit1.length;
        } else {
            dom = idSplit1.length;
            other = idSplit2.length;
        }
        String combine = ""; int i;
        for (i = 0; i < other; i += 1) {
            if (idSplit2[i].equals(idSplit1[i])) {
                combine += idSplit2[i];
            } else {
                i += 1;
                break;
            }
        }
        if (Integer.parseInt(idSplit2[i - 1]) < Integer.parseInt(idSplit1[i - 1])) {
            combine += idSplit2[i - 1];
        } else {
            combine += idSplit1[i - 1];
        }
        return combine;
    }

    /* Implements rebase by looking at each MasterGit node in a branch and creating
        the same MasterGit attached to the current branch
        (This one and interactive rebase are very buggy)
    */
    public void rebase(String branchName) {
        if (branches.contains(branchName)) {
            String idBranch = branches.value(branchName);
            String idMaster = branches.value("OG");
            if (!idBranch.equals(idMaster)) {
                String ancestorID = locateLink(branchName);
                MasterGit ancestorGit = findTheGit(ancestorID, masterFiles);
                ancestorGit = nextCommit(hopOnBranch(ancestorGit));
                String newID = newID(currentMaster.id);
                while (ancestorGit.next != null) {
                    newID = newID(currentMaster.id);
                    MasterGit temp = new MasterGit(currentMaster, ancestorGit.message, 
                                ancestorGit.commits, newID, writeTime(), null);
                    currentMaster.next.add(temp);
                    currentMaster = currentMaster.next.value;
                    ancestorGit = nextCommit(ancestorGit);
                }
                branches.add(branchName, newID);
            } else {
                System.out.println("Cannot rebase a branch onto itself");
            }
        } else {
            System.out.println("A branch with that name does not exist");
        }
    }

    /* Returns the logic next MasterGit (ID wise)
        Example: 1.1.1.4.2 ==> 1.1.1.4.3
    */
    protected MasterGit nextCommit(MasterGit x) {
        Entry<MasterGit> temp = x.next;
        String[] dataSplit2 = x.id.split("\\.");
        boolean moveOn = false;
        while (!moveOn) {
            String[] dataSplit = temp.value.id.split("\\.");
            String goal = Integer.toString(Integer.parseInt(dataSplit2[dataSplit2.length - 1]) + 1);
            if (dataSplit[dataSplit.length - 1].equals(goal)) {
                moveOn = true;
            } else {
                temp = temp.next00;
            }
        }
        return temp.value;
    }

    /* Returns the MasterGit if a branch needed to be hopped on when searching
        since it is not numerically in order when jumping onto a branch
        Example: 1.1.1.4.2 ==> 1.1.1.4.2.0
    */
    protected MasterGit hopOnBranch(MasterGit x) {
        Entry<MasterGit> temp = x.next;
        boolean moveOn = false;
        while (!moveOn) {
            String[] dataSplit = temp.value.id.split("\\.");
            if (dataSplit[dataSplit.length - 1].equals("0")) {
                moveOn = true;
            } else {
                temp = temp.next00;
            }
        }
        return temp.value;
    }

    /* Same implementation as rebase except added interaction and prompting with user
    */
    public void interactiveRebase(String branchName) {
        if (branches.contains(branchName)) {
            String idBranch = branches.value(branchName);
            String idMaster = branches.value("OG");
            if (!idBranch.equals(idMaster)) {
                String ancestorID = locateLink(branchName);
                MasterGit ancestorGit = findTheGit(ancestorID, masterFiles);
                ancestorGit = nextCommit(hopOnBranch(ancestorGit));
                String newID = newID(currentMaster.id);
                while (ancestorGit.next != null) {
                    System.out.println("Currently replaying:");
                    System.out.println("Commit " + currentMaster.id);
                    System.out.println(currentMaster.time);
                    System.out.println(currentMaster.message);
                    System.out.println();
                    String x = "Would you like to (c)ontinue, ";
                    String z = "(s)kip this commit, or change this commit's (m)essage?";
                    System.out.println(x + z);
                    Console console = System.console();
                    String input = console.readLine();
                    switch (input) {
                        case "c":
                            newID = newID(currentMaster.id);
                            MasterGit temp = new MasterGit(currentMaster, ancestorGit.message, 
                                        ancestorGit.commits, newID, writeTime(), null);
                            currentMaster.next.add(temp);
                            currentMaster = currentMaster.next.value;
                            ancestorGit = nextCommit(ancestorGit);
                            continue;
                        case "s":
                            ancestorGit = nextCommit(ancestorGit);
                            continue;
                        case "m":
                            String y = "Please enter a new message for this commit.";
                            System.out.println(y);
                            Console console2 = System.console();
                            String input2 = console.readLine();
                            newID = newID(currentMaster.id);
                            MasterGit temp2 = new MasterGit(currentMaster, input2, 
                                        ancestorGit.commits, newID, writeTime(), null);
                            currentMaster.next.add(temp2);
                            currentMaster = currentMaster.next.value;
                            ancestorGit = nextCommit(ancestorGit);
                            continue;
                        default:
                            continue;
                    }
                }
                branches.add(branchName, newID);
            } else {
                System.out.println("Cannot rebase a branch onto itself");
            }
        } else {
            System.out.println("A branch with that name does not exist");
        }
    }
}
