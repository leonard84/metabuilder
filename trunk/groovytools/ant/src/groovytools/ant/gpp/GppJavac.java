package groovytools.ant.gpp;

import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.selectors.FileSelector;

import java.io.File;

/**
 * Extends <b><a href="http://jakarta.apache.org/ant">Ant</a></b>'s
 * <code>{@link org.apache.tools.ant.taskdefs.Javac}</code> task with integrated
 * cpp-like preprocessing using {@link GppFilter}.
 * <p>
 * This task is a 'drop in' replacement for <code>Javac</code> and may be used
 * anywhere <code>Javac</code> is used.  This implementation adds only a few
 * additional parameters used to specify the preprocessing behavior:
 * <ul>
 * <li>{@link #setPreprocess}
 * <li>{@link #setPrepdir}
 * </ul>
 * Example usage:
 * <pre>
 *   &lt;target name="gppjavac" &gt;
 *      &lt;gppjavac preprocess="true" keep="true" destdir="classes" srcdir="src"&gt;
 *          &lt;classpath refid="myClassPath" /&gt;
 *      &lt;/gppjavac&gt;
 *  &lt;/target&gt;
 * </pre>
 *
 * @see groovytools.ant.gpp.GppCopy
 * @see groovytools.ant.gpp.GppJavac
 *
 * @author <a href="mailto:didge@foundrylogic.com">didge</a>
 *
 * @version $Revision: 1.7.2.7 $
 */
public class GppJavac extends Javac {

    private boolean preprocess = true;
    private boolean keep = true;
    private File prepdir;
    private GppCopy gppCopy;

    /**
     * @return if false, no preprocessing will be performed; the
     * default is <code>true</code>
     */
    public boolean getPreprocess() {
        return preprocess;
    }

    /**
     * Sets the preprocess switch.
     * @param preprocess if false, no preprocessing will be performed; the
     * default is <code>true</code>
     */
    public void setPreprocess(boolean preprocess) {
        this.preprocess = preprocess;
    }

    /**
     * @return if true, any temporary preprocessing files will be kept; the
     * default is <code>false</code>
     */
    public boolean getKeep() {
        return keep;
    }

    /**
     * Sets the keep switch.
     * @param keep if true, any temporary preprocessing files will be kept; the
     * default is <code>false</code>
     */
    public void setKeep(boolean keep) {
        this.keep = keep;
    }


    public File getPrepdir() {
        return prepdir;
    }

    public void setPrepdir(File prepdir) {
        this.prepdir = prepdir;
    }

    public void execute() throws BuildException {
        if(!getPreprocess()) {
            super.execute();
            return;
        }

        // This section is straight from Javac.execute().
        Path src = getSrcdir();  // src is private in super...
        if(src == null) {
            throw new BuildException("srcdir attribute must be set!", getLocation());
        }
        String[] list = src.list();
        if(list.length == 0) {
            throw new BuildException("srcdir attribute must be set!", getLocation());
        }

        File destdir = getDestdir(); // destDir is private in super...
        if(destdir != null && !destdir.isDirectory()) {
            throw new BuildException("destination directory '" + destdir + "' does not exist or is not a directory", getLocation());
        }

        preprocess();
        recreateSrc();
        setSrcdir(new Path(getProject(), prepdir.getAbsolutePath()));
        super.execute();
        if(keep == false) {
            deletePrepdir();
        }
    }

    private void preprocess() {
        preparePrepdir();

        gppCopy.init();
        FileSelector[] fileSelectors = getSelectors(getProject());

        String[] srcPaths = getSrcdir().list();
        for(int i = 0; i < srcPaths.length; i++) {
            String srcPath = srcPaths[i];
            FileSet fileSet = new FileSet();
            fileSet.setDir(new File(srcPath));
            gppCopy.addFileset(fileSet);
            for(int j = 0; j < fileSelectors.length; j++) {
                FileSelector fileSelector = fileSelectors[j];
                fileSet.appendSelector(fileSelector);
            }
        }

        gppCopy.setTodir(prepdir);
        Mapper mapper = gppCopy.createMapper();
        mapper.setFrom("*.java");
        mapper.setTo("*.java");
        Mapper.MapperType mapperType = new Mapper.MapperType();
        mapperType.setValue("glob");
        mapper.setType(mapperType);

        // Preprocess!!
        gppCopy.execute();
    }

    protected void preparePrepdir() {
        if(prepdir == null) {
            String prepdirName = getProject().getBaseDir().getAbsolutePath() + File.separator + "gppjavac.out";
            prepdir = new File(prepdirName);
        }
        if(prepdir.exists() == false) {
            prepdir.mkdirs();
        }
        else {
            /*
                Would it make any sense to try to remove any old preprocessed files
                that no longer exist in the src fileset?  Thoughts:
                1. The same prepdir maybe used with different src filesets.
            */
        }
    }

    protected void deletePrepdir() {
        if(prepdir.exists() == true) {
            FileSet oldPrepFiles = new FileSet();
            oldPrepFiles.setDir(prepdir);
            PatternSet.NameEntry name = oldPrepFiles.createInclude();
            name.setName("**");
            Delete deleteTask = new Delete();
            deleteTask.setTaskName(getTaskName());
            deleteTask.setProject(getProject());
            deleteTask.setIncludeEmptyDirs(true);
            deleteTask.setFailOnError(false);
            deleteTask.setVerbose(false);
            deleteTask.addFileset(oldPrepFiles);
            deleteTask.execute();
        }
    }

    public void init() {
        if(gppCopy == null) {
            gppCopy = new GppCopy();
            gppCopy.setProject(getProject());
            gppCopy.setTaskName(getTaskName());
            gppCopy.setLocation(getLocation());
        }
    }

    public void addConfiguredConfig(GppConfig gppConfig) {
        gppCopy.addConfiguredConfig(gppConfig);
    }
}
