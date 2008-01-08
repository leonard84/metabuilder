package groovytools.ant.gpp;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GppContext extends DataType {
    private GppStackMap templateContext;
    private List configurerList;
    private String innerRefId;
    private String eventHandler;

    public GppContext() {
        configurerList = new ArrayList();
    }

    public GppStackMap getTemplateContext() {
        if(templateContext == null) {
            templateContext = new GppStackMap();

            Project project = getProject();
            templateContext.put("ant", getProject().getProperties());
            templateContext.put("project", project);

            for(int i = 0; i < configurerList.size(); i++) {
                Configurer configurer = (Configurer)configurerList.get(i);
                configurer.configure(templateContext, project);
            }
        }
        return templateContext;
    }

    public String getInnerRefId() {
        return innerRefId;
    }

    public void setInnerRefId(String innerRefId) {
        this.innerRefId = innerRefId;
    }

    public String getEventHandler() {
        return eventHandler;
    }

    public void setEventHandler(String eventHandler) {
        this.eventHandler = eventHandler;
    }

    public void addConfiguredTool(GppTool tool) {
        configurerList.add(tool);
    }

    public void addConfiguredProperty(GppContextProperty property) {
        configurerList.add(property);
    }

    /**
     * This interface supports the deferred configuration of properties,
     * tools, and event handlers until after the Velocity Context is
     * initialized.
     */
    public interface Configurer {
        public void configure(Map templateContext, Project project);
    }
}
