package groovytools.ant.gpp;

import groovy.text.SimpleTemplateEngine;
import groovy.text.TemplateEngine;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;

import java.util.ArrayList;
import java.util.List;

public class GppEngine extends DataType {
    private TemplateEngine templateEngine;
    private List configurerList;

    public GppEngine() {
        configurerList = new ArrayList();
    }

    public TemplateEngine getTemplateEngine() {
        if(templateEngine == null) {
            templateEngine = new SimpleTemplateEngine();

            try {
                defaultInit(templateEngine);
                Project project = getProject();
                for(int i = 0; i < configurerList.size(); i++) {
                    Configurer configurer = (Configurer)configurerList.get(i);
                    configurer.configure(templateEngine, project);
                }
            }
            catch(Exception e) {
                throw new BuildException("could not initialize the TemplateEngine", e);
            }
        }
        return templateEngine;
    }

    protected void defaultInit(TemplateEngine templateEngine) {
    }

    public void addConfiguredProperty(GppEngineProperty property) {
        configurerList.add(property);
    }

    /**
     * This interface supports the deferred configuration of properties
     * until after the Velocity Engine is initialized.
     */
    public interface Configurer {
        public void configure(TemplateEngine templateEngine, Project project);
    }
}
