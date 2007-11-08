/*
 * Copyright (c) 2005 FoundryLogic, LLC. All Rights Reserved.
 */

package groovytools.ant.cygpath;

import org.apache.tools.ant.*;
import org.apache.tools.ant.util.*;
import org.apache.tools.ant.taskdefs.*;

import java.util.*;
import java.io.*;

public class Cygpath extends Task {
    protected String _properties;

    public void execute() throws BuildException {
        Project project = getProject();
        boolean isCygwin = project.getProperty("cygwin.user.home") != null;

        if(!isCygwin || _properties == null || _properties.length() == 0) return;
        StringTokenizer propertyToker = new StringTokenizer(_properties, ", \n\t", false);
        StringBuffer paths = new StringBuffer();
        boolean first = true;
        while(propertyToker.hasMoreTokens()) {
            if(first) {
                first = false;
            }
            else {
                paths.append(":*");
            }
            String property = propertyToker.nextToken();
            String path = project.getProperty(property);
            paths.append(property);
            paths.append("*:");
            paths.append(path);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Execute exe = new Execute(new PumpStreamHandler(out));
        exe.setAntRun(project);
        exe.setWorkingDirectory(project.getBaseDir());
        exe.setVMLauncher(true);
        exe.setCommandline(new String[]{"cygpath", "-wp", paths.toString()});

        int err = 0;
        try {
            err = exe.execute();
        }
        catch(IOException e) {
            throw new BuildException("cygpath.exe failed", e, getLocation());
        }

        String result = out.toString();

        if(err != 0) {
            throw new BuildException("cygpath.exe failed: " + result, getLocation());
        }

        result = result.substring(0, result.length() - 1);  // remove trailing \n
        Vector resultVector = StringUtils.split(result, '*');

        for(Iterator results = resultVector.iterator(); results.hasNext();) {
            String property = (String)results.next();
            String path = (String)results.next();
            if(results.hasNext()) {
                // remove leading and trailing ';'
                path = path.substring(1, path.length() - 1);
            }
            else {
                path = path.substring(1);  // remove leading ';'
            }
            project.setUserProperty(property, path);
        }
    }

    public void setProperties(String properties) {
        _properties = properties;
    }
}
