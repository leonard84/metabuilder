package groovytools.ant.gpp;

import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.types.FilterSetCollection;
import org.apache.tools.ant.Project;

import java.util.Hashtable;
import java.util.Vector;
import java.io.File;
import java.io.IOException;

public interface GppCopyImpl {
    public String[] getToFiles(String fromFile, Hashtable fileCopyMap);
    public void copyFile(FileUtils fileUtils, File srcFile, File dstFile, FilterSetCollection executionFilters, Vector filterChain, boolean overwrite, boolean preserveLastModified, String inputEncoding, String outputEncoding, Project project) throws IOException;
    public String getOutputEncoding(GppCopy copy);
    public  void handleEmptyDirectories(GppCopy copy, boolean includeEmpty, Hashtable dirCopyMap, File destDir);
}
