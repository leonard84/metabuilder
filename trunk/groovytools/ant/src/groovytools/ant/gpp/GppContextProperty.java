package groovytools.ant.gpp;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.commons.collections.ExtendedProperties;

import java.util.Iterator;
import java.util.Map;

public class GppContextProperty extends GppProperty implements GppContext.Configurer {
    public void configure(Map velocityContext, Project project) {
        if(getKey() != null) {
            velocityContext.put(getKey(), getValue());
        }
        else if(getFile() != null) {
            ExtendedProperties extendedProperties = load(project);
            Iterator entries = extendedProperties.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry)entries.next();
                velocityContext.put(entry.getKey(), entry.getValue());
            }
        }
        else {
            throw new BuildException("key or file must be set");
        }
    }
}
