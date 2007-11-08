package groovytools.ant.gpp;

import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.commons.collections.ExtendedProperties;

import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class GppProperty {
    private String key;
    private String value;

    private Path file;
    private String encoding;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Path getFile() {
        return file;
    }

    public void setFile(Path file) {
        this.file = file;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    protected ExtendedProperties load(Project project) {
        ExtendedProperties result = new ExtendedProperties();
        String[] pathElements = file.list();
        for(int i = 0; i < pathElements.length; i++) {
            InputStream in = null;
            String pathElement = pathElements[i];
            // First try the filesystem, then the classpath
            try {
                File fullPath = project.resolveFile(pathElement);
                in = new FileInputStream(fullPath);
            }
            catch(Exception e) {
                ClassLoader classLoader = getClass().getClassLoader();
                in = classLoader.getResourceAsStream(pathElement);
            }
            if(in == null) {
                throw new BuildException("properties file '" + pathElement +
                        "' could not be found in either the file system or the classpath.");
            }
            try {
                if(encoding == null) {
                    result.load(in);
                }
                else {
                    result.load(in, encoding);
                }
            }
            catch(IOException e) {
                throw new BuildException("error reading properties file '" + pathElement + "'");
            }
        }
        return result;
    }
}
