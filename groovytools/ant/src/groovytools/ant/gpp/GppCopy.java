package groovytools.ant.gpp;

import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.FilterSetCollection;
import org.apache.tools.ant.types.FilterSet;

import java.util.Properties;
import java.util.Enumeration;
import java.util.Vector;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;

/**
 * Extends <b><a href="http://jakarta.apache.org/ant">Ant</a></b>'s
 * <code>{@link org.apache.tools.ant.taskdefs.Copy}</code> task with integrated
 * cpp-like preprocessing using {@link GppFilter}.
 * <p>
 * This task is a 'drop in' replacement for <code>Copy</code> and may be used
 * anywhere <code>Copy</code> is used.
 * <p>Example usage:
 * <pre>
 *    &lt;target name="gppcopy" &gt;
 *       &lt;gppcopy todir="output" overwrite="true"&gt;
 *           &lt;fileset dir="src" includes="**&#047;*.html.gpp"/&gt;
 *           &lt;mapper type="glob" from="*.html.gpp" to="*.html"/&gt;
 *       &lt;/gppcopy&gt;
 *   &lt;/target&gt;
 * </pre>
 *
 * @see groovytools.ant.gpp.GppFilter
 * @see groovytools.ant.gpp.GppJavac
 *
 * @author <a href="mailto:didge@foundrylogic.com">didge</a>
 *
 * @version $Revision: 1.2.2.8 $
 */
public class GppCopy extends Copy {
    protected static GppCopyImpl gppCopyImpl;

    protected GppFilter gppFilter;

    static {
        Properties props = new Properties();
        InputStream in =
                GppCopy.class.getResourceAsStream("/org/apache/tools/ant/version.txt");
        try {
            props.load(in);
            in.close();
        }
        catch(IOException e) {
            throw new BuildException("could not load ant version information", e);
        }

        String version = props.getProperty("VERSION");
        if(version.compareTo("1.6") > -1) {
            try {
                GppCopy.gppCopyImpl = (GppCopyImpl)Class.forName("groovytools.ant.gpp.GppCopyImpl16").newInstance();
            }
            catch(Exception e) {
                throw new BuildException("could not load class 'groovytools.ant.gpp.GppCopyImpl16'");
            }
        }
        else if(version.compareTo("1.5") > -1) {
            try {
                GppCopy.gppCopyImpl = (GppCopyImpl)Class.forName("groovytools.ant.gpp.GppCopyImpl15").newInstance();
            }
            catch(Exception e) {
                throw new BuildException("could not load class 'groovytools.ant.gpp.GppCopyImpl15'");
            }
        }
        else {
            throw new BuildException("gpp does not support ant version '" + version + "'");
        }
    }

    public GppCopy() {
        gppFilter = new GppFilter();
        FilterChain filterChain = createFilterChain();
        filterChain.getFilterReaders().add(gppFilter);
    }

    public void addConfiguredConfig(GppConfig gppConfig) {
        gppFilter.addConfiguredConfig(gppConfig);
    }

    public void execute() throws BuildException {
        gppFilter.setProject(getProject());
        gppFilter.initialize();
        super.execute();
    }

    protected void doFileOperations() {
        if(fileCopyMap.size() > 0) {
            log("Copying " + fileCopyMap.size()
                    + " file" + (fileCopyMap.size() == 1 ? "" : "s")
                    + " to " + destDir.getAbsolutePath());
            GppConfig gppConfig = gppFilter.getConfig();
            GppStackMap templateContext = gppConfig.getTemplateContext();

            Enumeration e = fileCopyMap.keys();

            Vector filterChains = getFilterChains();
            String inputEncoding = getEncoding();
            String outputEncoding = GppCopy.gppCopyImpl.getOutputEncoding(this);
            FileUtils fileUtils = getFileUtils();
            Project project = getProject();

            while (e.hasMoreElements()) {
                String fromFile = (String)e.nextElement();
                String[] toFiles = GppCopy.gppCopyImpl.getToFiles(fromFile, fileCopyMap);

                for(int i = 0; i < toFiles.length; i++) {
                    String toFile = toFiles[i];

                    if(fromFile.equals(toFile)) {
                        log("Skipping self-copy of " + fromFile, verbosity);
                        continue;
                    }

                    try {
                        log("Copying " + fromFile + " to " + toFile, verbosity);

                        FilterSetCollection executionFilters =
                                new FilterSetCollection();
                        if(filtering) {
                            executionFilters
                                    .addFilterSet(getProject().getGlobalFilterSet());
                        }
                        for(Enumeration filterEnum = getFilterSets().elements();
                            filterEnum.hasMoreElements();) {
                            executionFilters
                                    .addFilterSet((FilterSet)filterEnum.nextElement());
                        }

                        File srcFile = new File(fromFile);
                        File dstFile = new File(toFile);
                        templateContext.put("gpp", new GppCopyTool(srcFile, dstFile, inputEncoding, outputEncoding));

                        GppCopy.gppCopyImpl.copyFile(fileUtils, srcFile, dstFile, executionFilters, filterChains, forceOverwrite, preserveLastModified, inputEncoding, outputEncoding, project);


                    }
                    catch(Exception ioe) {
                        String msg = "Failed to copy " + fromFile + " to " + toFile
                                + " due to " + ioe.getMessage();
                        File targetFile = new File(toFile);
                        if(targetFile.exists() && !targetFile.delete()) {
                            msg += " and I couldn't delete the corrupt " + toFile;
                        }
                        throw new BuildException(msg, ioe, getLocation());
                    }
                }
            }
        }

    }
}
