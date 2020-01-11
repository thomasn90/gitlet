package gitlet;

import ucb.junit.textui;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/** The suite of all JUnit tests for the gitlet package.
 *  @author Thomas Nguyen
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        System.exit(textui.runClasses(UnitTest.class));
    }

    /** A dummy test to avoid complaint. */
    @Test
    public void placeholderTest() throws IOException, ClassNotFoundException {
        Main.main("init");
    }
    /** A dummy test to avoid complaint. */
    @Test
    public void placeholderTest2() throws IOException, ClassNotFoundException {
        Main.main("init");
        Main.main("log");
    }

    /** A dummy test to avoid complaint. */
    @Test
    public void placeholderTest3() throws IOException, ClassNotFoundException {
        File wug = new File(System.getProperty("user.dir") + "/wug.txt");
        Main.main("init");
        Main.main("status");
    }
    /** A dummy test to avoid complaint. */
    @Test
    public void placeholderTest4() throws IOException, ClassNotFoundException {
        File wug = new File(System.getProperty("user.dir") + "/wug.txt");
        Main.main("init");
    }
    /** A dummy test to avoid complaint. */
    @Test
    public void branchTest() throws IOException, ClassNotFoundException {
        File wug = new File(System.getProperty("user.dir") + "/wug.txt");
        Main.main("init");
        Main.main("branch", "branch1");
    }

    /** A dummy test to avoid complaint. */
    @Test
    public void checkout3Test() throws IOException, ClassNotFoundException {
        Main.main("init");
        Main.main("branch", "branch1");
        Main.main("log");
    }
    /** A dummy test to avoid complaint. */
    @Test
    public void resetTest() throws IOException, ClassNotFoundException {
        Main.main("init");
    }
    /** A dummy test to avoid complaint. */
    @Test
    public void mergeTest() throws IOException, ClassNotFoundException {
        Main.main("init");
    }
    /** A dummy test to avoid complaint. */
    @Test
    public void placeholderTest5() throws IOException, ClassNotFoundException {
        Main.main("init");
    }
    /** A dummy test to avoid complaint. */
    @Test
    public void placeholderTest8() throws IOException, ClassNotFoundException {
        Main.main("init");
    }
    /** A dummy test to avoid complaint. */
    @Test
    public void placeholderTest9() throws IOException, ClassNotFoundException {
        Main.main("init");
    }
    /** A dummy test to avoid complaint. */
    @Test
    public void placeholderTest10() throws IOException, ClassNotFoundException {
        Main.main("init");
    }
    /** A dummy test to avoid complaint. */
    @Test
    public void placeholderTest11() throws IOException, ClassNotFoundException {
        Main.main("init");
    }
    /** A dummy test to avoid complaint. */
    @Test
    public void placeholderTest12() throws IOException, ClassNotFoundException {
        Main.main("init");
    }
}


