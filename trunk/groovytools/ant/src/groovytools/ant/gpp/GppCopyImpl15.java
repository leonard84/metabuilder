package groovytools.ant.gpp;

import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.types.FilterSetCollection;
import org.apache.tools.ant.Project;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.io.File;
import java.io.IOException;

/**
 * Implements GppCopy for Ant 1.5x API.
 *
 * @see groovytools.ant.gpp.GppCopy
 * @see groovytools.ant.gpp.GppCopyImpl
 * @see groovytools.ant.gpp.GppCopyImpl16
 *
 * @author <a href="mailto:didge@foundrylogic.com">didge</a>
 *
 * @version $Revision: 1.1.2.2 $
 */
public class GppCopyImpl15 implements GppCopyImpl {
    protected String[] _toFiles = new String[1];

    public String[] getToFiles(String fromFile, Hashtable fileCopyMap) {
        _toFiles[0] = (String)fileCopyMap.get(fromFile);
        return _toFiles;
    }

    public void copyFile(FileUtils fileUtils, File srcFile, File dstFile, FilterSetCollection executionFilters, Vector filterChain, boolean overwrite, boolean preserveLastModified, String inputEncoding, String outputEncoding, Project project) throws IOException {
        fileUtils.copyFile(srcFile, dstFile, executionFilters, filterChain, overwrite, preserveLastModified, inputEncoding, project);
    }

    public String getOutputEncoding(GppCopy copy) {
        return copy.getEncoding();
    }

    public void handleEmptyDirectories(GppCopy copy, boolean includeEmpty, Hashtable dirCopyMap, File destDir) {
        if (includeEmpty) {
            Enumeration e = dirCopyMap.elements();
            int count = 0;
            while (e.hasMoreElements()) {
                File d = new File((String) e.nextElement());
                if (!d.exists()) {
                    if (!d.mkdirs()) {
                        copy.log("Unable to create directory "
                            + d.getAbsolutePath(), Project.MSG_ERR);
                    } else {
                        count++;
                    }
                }
            }

            if (count > 0) {
                copy.log("Copied " + count +
                    " empty director" +
                    (count == 1 ? "y" : "ies") +
                    " to " + destDir.getAbsolutePath());
            }
        }
    }
}
