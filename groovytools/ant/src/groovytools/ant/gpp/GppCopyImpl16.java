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
 * Implements GppCopy for Ant 1.6x API.
 *
 * @see groovytools.ant.gpp.GppCopy
 * @see groovytools.ant.gpp.GppCopyImpl
 * @see groovytools.ant.gpp.GppCopyImpl15
  *
 * @author <a href="mailto:didge@foundrylogic.com">didge</a>
 *
 * @version $Revision: 1.1.2.2 $
 */
public class GppCopyImpl16 implements GppCopyImpl {
    public String[] getToFiles(String fromFile, Hashtable fileCopyMap) {
        return (String[])fileCopyMap.get(fromFile);
    }

    public void copyFile(FileUtils fileUtils, File srcFile, File dstFile, FilterSetCollection executionFilters, Vector filterChain, boolean overwrite, boolean preserveLastModified, String inputEncoding, String outputEncoding, Project project) throws IOException {
        fileUtils.copyFile(srcFile, dstFile, executionFilters, filterChain, overwrite, preserveLastModified, inputEncoding, outputEncoding, project);
    }

    public String getOutputEncoding(GppCopy copy) {
        return copy.getOutputEncoding();
    }

    public void handleEmptyDirectories(GppCopy copy, boolean includeEmpty, Hashtable dirCopyMap, File destDir) {
        if (includeEmpty) {
            Enumeration e = dirCopyMap.elements();
            int createCount = 0;
            while (e.hasMoreElements()) {
                String[] dirs = (String[]) e.nextElement();
                for (int i = 0; i < dirs.length; i++) {
                    File d = new File(dirs[i]);
                    if (!d.exists()) {
                        if (!d.mkdirs()) {
                            copy.log("Unable to create directory "
                                + d.getAbsolutePath(), Project.MSG_ERR);
                        } else {
                            createCount++;
                        }
                    }
                }
            }
            if (createCount > 0) {
                copy.log("Copied " + dirCopyMap.size()
                    + " empty director"
                    + (dirCopyMap.size() == 1 ? "y" : "ies")
                    + " to " + createCount
                    + " empty director"
                    + (createCount == 1 ? "y" : "ies") + " under "
                    + destDir.getAbsolutePath());
            }
        }
    }
}
