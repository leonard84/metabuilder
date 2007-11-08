package groovytools.ant.gpp;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import java.lang.reflect.Method;
import java.util.Map;

public class GppTool implements GppContext.Configurer {
    private Class[] setTemplateContextParams = new Class[] { Map.class};
    private String key;
    private String className;
    private String refid;

    public String getRefid() {
        return refid;
    }

    public void setRefid(String refid) {
        if(className != null) {
            throw new BuildException("refid and className attributes are mutually exclusive");
        }
        this.refid = refid;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        if(refid != null) {
            throw new BuildException("refid and className attributes are mutually exclusive");
        }
        this.className = className;
    }

    protected Object getInstance(Map templateContext, Project project) {
        Object obj = null;
        if(refid != null) {
            obj = project.getReference(refid);
        }
        else {
            Class c = null;
            try {
                c = Class.forName(className);
                obj = c.newInstance();
            }
            catch(Exception e) {
                throw new BuildException("could not load class '" + className + "' for tool '" + getKey() + "'", e);
            }
        }
        try {
            Method m = obj.getClass().getMethod("setTemplateContext", setTemplateContextParams);
            m.invoke(obj, new Object[]{templateContext});
        }
        catch(NoSuchMethodException e) {
            // Ignore since this is an optional method
        }
        catch(Exception e) {
            throw new BuildException(e);
        }
        return obj;
    }

    public void configure(Map templateContext, Project project) {
        if(refid == null && className == null) {
            throw new BuildException("either refid or className must be set");
        }
        Object toolInstance = getInstance(templateContext, project);
        templateContext.put(getKey(), toolInstance);
    }
}
