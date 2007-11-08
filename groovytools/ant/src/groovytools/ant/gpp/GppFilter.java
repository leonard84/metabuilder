package groovytools.ant.gpp;

import groovy.lang.Writable;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.filters.BaseParamFilterReader;
import org.apache.tools.ant.filters.ChainableReader;
import org.apache.tools.ant.types.Parameter;

import java.io.*;

/**
 * Implementation of <b><a href="http://ant.apache.org">Ant</a></b>'s
 * <code>{@link org.apache.tools.ant.filters.ChainableReader}</code> that
 * supports cpp-like preprocessing using
 * </b><a href="http://jakarta.apache.org/velocity">Velocity</a></b>.
 * <p>
 * <b><code>GppFilter</code></b> provides access to <b>Ant's</b> project
 * properties through a single property called <code>ant</code>.  For example,
 * <code>$ant.foobar</code> will return the value of the current <b>Ant</b>
 * project's property <code>foobar</code>.
 * <p>
 * The following target demonstrates the use of GppFilter:
 * <pre>
 *   &lt;target name="gppfilter" depends="defineTasks"&gt;
 *       &lt;copy todir="output" overwrite="true"&gt;
 *           &lt;fileset dir="src"&gt;
 *               &lt;include name="**&#047;*.html.gpp"/&gt;
 *           &lt;/fileset&gt;
 *           &lt;filterchain&gt;
 *               &lt;filterreader classname="groovytools.ant.gpp.GppFilter"&gt;
 *                   &lt;classpath refid="gppPath"/&gt;
 *               &lt;/filterreader&gt;
 *           &lt;/filterchain&gt;
 *           &lt;mapper type="glob" from="*.html.gpp" to="*.html"/&gt;
 *       &lt;/copy&gt;
 *   &lt;/target&gt;
 * </pre>
 *
 * Note that if you are defining a <code>copy</code> task that performs no other
 * than <code>GppFilter</code>, you can use {@link GppCopy} instead.

 * @see groovytools.ant.gpp.GppCopy
 * @see groovytools.ant.gpp.GppJavac
 *
 * @author <a href="mailto:didge@foundrylogic.com">didge</a>
 *
 * @version $Revision: 1.7.2.6 $
 */

public class GppFilter extends BaseParamFilterReader implements ChainableReader {
    private GppConfig gppConfig;
    private boolean preprocessed;

    public GppFilter() {
        super();
    }

    public GppFilter(final Reader in) {
        super(in);
    }

    protected GppFilter(final Reader in, GppConfig gppConfig) {
        this(in);
        this.gppConfig = gppConfig;
        setInitialized(true);
    }

    public boolean getPreprocessed() {
        return preprocessed;
    }

    public void setPreprocessed(boolean preprocessed) {
        this.preprocessed = preprocessed;
    }


    public Reader chain(Reader rdr) {
        return new GppFilter(rdr, gppConfig);
    }

    public int read() throws IOException {
        if(!getInitialized()) initialize();
        if(!getPreprocessed()) preprocess();

        return super.read();
    }

    public boolean ready() throws IOException {
        if(!getInitialized()) initialize();
        if(!getPreprocessed()) preprocess();

        return super.ready();
    }

    public int read(char cbuf[]) throws IOException {
        if(!getInitialized()) initialize();
        if(!getPreprocessed()) preprocess();

        return super.read(cbuf);
    }

    protected void initialize() {
        if(gppConfig == null) {
            Parameter[] parameters = getParameters();
            if(parameters != null) {
                for(int i = 0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    if(parameter.getType().equals("config") && parameter.getName().equals("refid")) {
                        Object obj = getProject().getReference(parameter.getValue());
                        if(obj != null && obj instanceof GppConfig) {
                            gppConfig = (GppConfig)obj;
                        }
                    }
                    else {
                        throw new BuildException("unknown parameter type '" + parameter.getType() + "' or name '" + parameter.getName() + "'");
                    }
                }
            }
            if(gppConfig == null) {
                gppConfig = GppConfig.getDefaultConfig(getProject());
            }
        }

        setInitialized(true);
    }

    protected void preprocess() throws IOException {
        try {
            TemplateEngine templateEngine = gppConfig.getTemplateEngine();
            File prepFile = null;
            Writer prepWriter = null;
            boolean useTempFile = gppConfig.isUseTempFile();
            if(useTempFile) {
                File tempDir = gppConfig.getTempDir();
                if(tempDir.exists() == false) {
                    tempDir.mkdirs();
                }
                prepFile = gppConfig.getFileUtils().createTempFile("out", "gpp", tempDir);

                prepWriter = new OutputStreamWriter(new FileOutputStream(prepFile));
            }
            else {
                prepWriter = new StringWriter();
            }

            GppStackMap templateContext = gppConfig.getTemplateContext();
            templateContext.push();  // prevents cross contamination between repeated calls to preprocess()
            try {
                Template template = templateEngine.createTemplate(in);
                Writable writable = template.make(templateContext);
                writable.writeTo(prepWriter);
            }
            finally {
                templateContext.pop();
                prepWriter.close();
            }

            if(useTempFile) {
                in = new InputStreamReader(new FileInputStream(prepFile));
            }
            else {
                String prepText = prepWriter.toString();
                in = new StringReader(prepText);
            }

            setPreprocessed(true);
        }
        catch(Exception exc) {
            GppCopyTool gpp = (GppCopyTool)this.gppConfig.getTemplateContext().get("gpp");
            String filename = "<stream>";
            if(gpp != null) {
                filename = gpp.getSourceName();
            }
            BuildException exc2 = new BuildException("error in '" + filename + "': " + exc.getMessage());
            exc2.setStackTrace(exc.getStackTrace());

            throw exc2;
        }
    }

    public void addConfiguredConfig(GppConfig gppConfig) {
        this.gppConfig = gppConfig;
    }

    public GppConfig getConfig() {
        return this.gppConfig;
    }
}
