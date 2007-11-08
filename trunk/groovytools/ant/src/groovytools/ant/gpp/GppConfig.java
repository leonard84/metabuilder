package groovytools.ant.gpp;

import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;

import java.io.File;
import java.util.Map;

import groovy.text.TemplateEngine;

/**
 * <p>Use this <code>DataType</code> in your project to create a reusable
 * <code>VelocityEngine</code> and <code>VelocityContext</code>
 * configuration.</p>
 *
 * <p>The following example demonstrates how to use GppConfig in an init
 * task:</p>
 * <pre>
 * </pre>
 *
 * @see groovytools.ant.gpp.GppFilter
 * @see groovytools.ant.gpp.GppCopy
 * @see groovytools.ant.gpp.GppJavac
 *
 * @author <a href="mailto:didge@foundrylogic.com">didge</a>
 *
 * @version $Revision: 1.2.2.5 $
 */

public class GppConfig extends DataType {
    private GppContext gppContext;
    private GppEngine gppEngine;
    private File tempDir;
    private FileUtils fileUtils;
    private boolean useTempFile;
    private boolean keep;
    private static String DEFAULT_KEY = "GppConfig.DEFAULT_KEY";

    public GppConfig() {
        fileUtils = FileUtils.newFileUtils();
        tempDir = new File("gpp.out");
        useTempFile = false;
        keep = false;
    }

    public void addConfiguredContext(GppContext gppContext) {
        this.gppContext = gppContext;
    }

    public void addConfiguredEngine(GppEngine gppEngine) {
        this.gppEngine = gppEngine;
    }

    public GppStackMap getTemplateContext() {
        if(gppContext == null) {
            gppContext = new GppContext();
            gppContext.setProject(getProject());
        }
        return gppContext.getTemplateContext();
    }

    public TemplateEngine getTemplateEngine() {
        if(gppEngine == null) {
            gppEngine = new GppEngine();
            gppEngine.setProject(getProject());
        }
        return gppEngine.getTemplateEngine();
    }

    public File getTempDir() {
        return tempDir;
    }

    public void setTempDir(File tempDir) {
        this.tempDir = tempDir;
    }

    public boolean isUseTempFile() {
        return useTempFile;
    }

    public void setUseTempFile(boolean useTempFile) {
        this.useTempFile = useTempFile;
        if(useTempFile == true) {
            getProject().addBuildListener(new BuildListener() {
                public void buildStarted(BuildEvent event) {
                }

                public void buildFinished(BuildEvent event) {
                    if(keep == false && tempDir.exists()) {
                        FileSet oldPrepFiles = new FileSet();
                        oldPrepFiles.setDir(tempDir);
                        PatternSet.NameEntry name = oldPrepFiles.createInclude();
                        name.setName("**");
                        Delete deleteTask = new Delete();
                        deleteTask.setTaskName("gppconfig");
                        deleteTask.setProject(getProject());
                        deleteTask.setIncludeEmptyDirs(true);
                        deleteTask.setFailOnError(false);
                        deleteTask.setVerbose(false);
                        deleteTask.addFileset(oldPrepFiles);
                        deleteTask.execute();
                    }
                }

                public void targetStarted(BuildEvent event) {
                }

                public void targetFinished(BuildEvent event) {
                }

                public void taskStarted(BuildEvent event) {
                }

                public void taskFinished(BuildEvent event) {
                }

                public void messageLogged(BuildEvent event) {
                }
            });
        }
    }

    public boolean isKeep() {
        return keep;
    }

    public void setKeep(boolean keep) {
        this.keep = keep;
    }

    public FileUtils getFileUtils() {
        return fileUtils;
    }

    public void setRefid(Reference ref) {
        Object o = ref.getReferencedObject(getProject());
        if(o instanceof GppConfig) {
            GppConfig that = (GppConfig)o;
            this.gppContext = that.gppContext;
            this.gppEngine = that.gppEngine;
            this.tempDir = that.tempDir;
            this.fileUtils = that.fileUtils;
            this.useTempFile = that.useTempFile;
            this.keep = that.keep;
            super.setRefid(ref);
        }
        else {
            String msg = ref.getRefId() + " does not refer to a GppConfig";
            throw new BuildException(msg);
        }
    }

    public static GppConfig getDefaultConfig(Project project) {
        Object o = project.getReference(GppConfig.DEFAULT_KEY);
        if(o == null) {
            GppConfig defaultConfig = new GppConfig();
            defaultConfig.setProject(project);
            project.addReference(GppConfig.DEFAULT_KEY, defaultConfig);
            return defaultConfig;
        }
        else if(o instanceof GppConfig) {
            return (GppConfig)o;
        }
        else {
            throw new BuildException("referenced object is not a GppConfig");
        }

    }
}
