import java.io.*;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.TreeMap;
import org.junit.Before;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.Iterator;

public class GitletTest extends Gitlet {
	private static final String GITLET_DIR = ".gitlet/";

    /* ALL CODE BELOW BEFORE THE LINE WAS COPIED FROM GITLETPUBLICTEST.JAVA
        THAT WAS PROVIDED AS A STARTER TEST
    */
	@Before
    public void setUp() {
        File f = new File(GITLET_DIR);
        if (f.exists()) {
            recursiveDelete(f);
        }
    }

    private static void recursiveDelete(File d) {
        if (d.isDirectory()) {
            for (File f : d.listFiles()) {
                recursiveDelete(f);
            }
        }
        d.delete();
    }

    private static void createFile(String fileName, String fileText) {
        File f = new File(fileName);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        writeFile(fileName, fileText);
    }

    private static void writeFile(String fileName, String fileText) {
        FileWriter fw = null;
        try {
            File f = new File(fileName);
            fw = new FileWriter(f, false);
            fw.write(fileText);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getText(String fileName) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(fileName));
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

//======================================================================================

    /* EACH TEST DOES EXACTLY WHAT THE METHOD NAME SAYS
    */
    @Test
    public void testMasterGitConstructor() {
    	Gitlet ng = new Gitlet();
    	createFile("wug1.txt", "This is a wug.");
    	createFile("wug2.txt", "This is a wug.");
    	createFile("wug3.txt", "This is not a wug.");
    	ng.gitInitialize();
    	ng.initialize();
        ng.setUpCurrentMaster();
    	ng.add("wug1.txt");
    	ng.add("wug2.txt");
    	ng.add("wug3.txt");
    	ng.commit("initial wugs");
        ng.setUpCurrentMaster();
    	createFile("wug4.txt", "This is definitely a wug.");
    	createFile("wug5.txt", "This migth be a wug.");
    	createFile("wug6.txt", "This is definitely not a wug.");
    	ng.add("wug4.txt");
    	ng.add("wug5.txt");
    	ng.add("wug6.txt");
    	ng.commit("changed wugs");
        ng.setUpCurrentMaster();
    	Entry<String> wugs = findCommitsMG("1", ng.masterFiles);
    	System.out.println("Test Git Constructor Wug1:");
    	while (wugs != null) {
    		System.out.println(wugs.value);
    		wugs = wugs.next00;
    	}
    	Entry<String> wugs1 = findCommitsMG("2", ng.masterFiles);
    	System.out.println("Test Git Constructor Wug2:");
    	while (wugs1 != null) {
    		System.out.println(wugs1.value);
    		wugs1 = wugs1.next00;
    	}
        MasterGit ogWugs = findTheGit("2", ng.masterFiles);
        Entry<String> wugs3 = ogWugs.commits;
        System.out.println("Test Git Constructor Wug3:");
        while (wugs3 != null) {
            System.out.println(wugs3.value);
            wugs3 = wugs3.next00;
        }
    }

	@Test
	public void testAddCommitCheckout1() {
		Gitlet ng = new Gitlet();
		createFile("wug.txt", "This is a wug.");
		createFile("wug2.txt", "This is not a wug.");
		ng.gitInitialize();
		ng.initialize();
		ng.add("wug.txt");
		ng.commit("asdfasf");
        ng.setUpCurrentMaster();
		writeFile("wug.txt", "This is not a wug.");
		ng.checkout1("wug.txt");
        assertEquals(getText("wug.txt"), "This is a wug.");
	}

	@Test
	public void testCheckout2() {
		Gitlet ng = new Gitlet();
		createFile("wug.txt", "This is a wug.");
		createFile("wug2.txt", "This is not a wug.");
		ng.gitInitialize();
		ng.initialize();
		ng.add("wug.txt");
		ng.commit("asdfasf");
        ng.setUpCurrentMaster();
		writeFile("wug.txt", "This is not a wug.");
		ng.add("wug.txt");
		ng.commit("asdfasfasdf");
        ng.setUpCurrentMaster();
		writeFile("wug.txt", "This might be a wug.");
		ng.checkout2("1", "wug.txt");
		assertEquals(getText("wug.txt"), "This is a wug.");
	}

	@Test
	public void testBranchCheckout3RMBranch() {
		Gitlet ng = new Gitlet();
		createFile("wug.txt", "This is a wug.");
		createFile("wug2.txt", "This is not a wug.");
		assertEquals(getText("wug.txt"), "This is a wug.");
		ng.gitInitialize();
		ng.initialize();
        ng.setUpCurrentMaster();
		ng.add("wug.txt");
		ng.commit("asdfasf");
        ng.setUpCurrentMaster();
		ng.branch("newBranch");
        ng.setUpCurrentMaster();
		ng.checkout3("newBranch");
        ng.setUpCurrentMaster();
		writeFile("wug.txt", "This is not a wug.");
		ng.add("wug.txt");
		ng.commit("asdfasfasdf");
        ng.setUpCurrentMaster();
		ng.checkout3("master");
        ng.setUpCurrentMaster();
		assertEquals(getText("wug.txt"), "This is a wug.");
        ng.rmbranch("newBranch");
        System.out.println("Should print no exist branch:");
        ng.checkout3("newBranch");
	}

    @Test
    public void testFindLogGlobalLogReset() {
        Gitlet ng = new Gitlet();
        createFile("wug.txt", "This is a wug.");
        createFile("wug2.txt", "This is not a wug.");
        assertEquals(getText("wug.txt"), "This is a wug.");
        ng.gitInitialize();
        ng.initialize();
        ng.setUpCurrentMaster();
        ng.add("wug.txt");
        ng.add("wug2.txt");
        ng.commit("asdfasf");
        ng.setUpCurrentMaster();
        ng.branch("newBranch");
        ng.setUpCurrentMaster();
        ng.checkout3("newBranch");
        ng.setUpCurrentMaster();
        writeFile("wug.txt", "This is not a wug.");
        ng.add("wug.txt");
        ng.commit("asdfasfasdf");
        ng.setUpCurrentMaster();
        writeFile("wug.txt", "This is not possibly a wug.");
        ng.add("wug.txt");
        ng.commit("asdfasfasdf");
        ng.setUpCurrentMaster();
        writeFile("wug.txt", "This could not possibly a wug.");
        ng.add("wug.txt");
        ng.commit("asdfasfasdf");
        System.out.println("Testing Log:");
        ng.log();
        System.out.println("Testing GlobalLog:");
        ng.globalLog();
        System.out.println("Testing Find:");
        ng.find("asdfasfasdf");
        ng.find("asdfasf");
        ng.checkout3("master");
        writeFile("wug.txt", "This might be a wug.");
        writeFile("wug2.txt", "This is probably not a wug.");
        ng.reset("1");
        assertEquals(getText("wug.txt"), "This is a wug.");
        assertEquals(getText("wug2.txt"), "This is not a wug.");
    }

    @Test
    public void testMerge() {
        Gitlet ng = new Gitlet();
        createFile("wug1.txt", "This is a wug.");
        createFile("wug2.txt", "This is a wug.");
        createFile("wug3.txt", "This is a wug.");
        ng.gitInitialize();
        ng.initialize();
        ng.setUpCurrentMaster();
        ng.add("wug1.txt");
        ng.add("wug2.txt");
        ng.add("wug3.txt");
        ng.commit("momma wug");
        ng.setUpCurrentMaster();
        ng.branch("dada branch");
        ng.setUpCurrentMaster();
        ng.checkout3("dada branch");
        ng.setUpCurrentMaster();
        writeFile("wug1.txt", "This is not a wug");
        writeFile("wug3.txt", "This is probably not a wug");
        ng.add("wug1.txt");
        ng.add("wug2.txt");
        ng.add("wug3.txt");
        ng.commit("modified wug 1 and 3");
        ng.setUpCurrentMaster();
        ng.checkout3("master");
        ng.setUpCurrentMaster();
        writeFile("wug2.txt", "This is definitely a wug");
        writeFile("wug3.txt", "This is definitely not a wug");
        ng.add("wug1.txt");
        ng.add("wug2.txt");
        ng.add("wug3.txt");
        ng.commit("modified wug 2 and 3");
        ng.setUpCurrentMaster();
        ng.merge("dada branch");
        assertEquals(getText("wug1.txt"), "This is not a wug");
        assertEquals(getText("wug2.txt"), "This is definitely a wug");
        assertEquals(getText("wug3.txt"), "This is definitely not a wug");
        assertEquals(getText("wug3.txt.conflicted"), "This is probably not a wug");
    }

    @Test
    public void testRebase() {
        Gitlet ng = new Gitlet();
        createFile("wug1.txt", "This is a wug.");
        createFile("wug2.txt", "This is a wug.");
        createFile("wug3.txt", "This is a wug.");
        ng.gitInitialize();
        ng.initialize();
        ng.setUpCurrentMaster();
        ng.add("wug1.txt");
        ng.add("wug2.txt");
        ng.add("wug3.txt");
        ng.commit("momma wug");
        ng.setUpCurrentMaster();
        ng.branch("dada branch");
        ng.setUpCurrentMaster();
        ng.checkout3("dada branch");
        ng.setUpCurrentMaster();
        writeFile("wug1.txt", "This is not a wug");
        writeFile("wug3.txt", "This is probably not a wug");
        ng.add("wug1.txt");
        ng.add("wug2.txt");
        ng.add("wug3.txt");
        ng.commit("modified wug 1 and 3");
        ng.setUpCurrentMaster();
        ng.checkout3("master");
        ng.setUpCurrentMaster();
        writeFile("wug3.txt", "This is definitely not a wug");
        ng.add("wug1.txt");
        ng.add("wug2.txt");
        ng.add("wug3.txt");
        ng.commit("modified wug 2 and 3");
        ng.setUpCurrentMaster();
        ng.rebase("dada branch");
        assertEquals(getText("wug1.txt"), "This is not a wug");
        assertEquals(getText("wug2.txt"), "This is a wug");
        assertEquals(getText("wug3.txt"), "This is definitely not a wug");
    }

	public static void main(String[] args) {
		jh61b.junit.textui.runClasses(GitletTest.class);
	}
}