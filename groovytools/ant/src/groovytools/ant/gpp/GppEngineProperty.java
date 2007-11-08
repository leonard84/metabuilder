package groovytools.ant.gpp;

import groovy.text.TemplateEngine;
import org.apache.tools.ant.Project;

public class GppEngineProperty extends GppProperty implements GppEngine.Configurer {
    private boolean add;

    public boolean isAdd() {
        return add;
    }

    public void setAdd(boolean add) {
        this.add = add;
    }

    public void configure(TemplateEngine templateEngine, Project project) {
    }
}
