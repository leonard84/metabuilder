package groovytools.ant.gpp;

import java.io.File;

public class GppCopyTool {
    protected File _src;
    protected File _dst;
    protected String _srcEncoding;
    protected String _dstEncoding;

    public GppCopyTool(File src, File dst, String srcEncoding, String dstEncoding) {
        _src = src;
        _dst = dst;
        _srcEncoding = srcEncoding;
        _dstEncoding = dstEncoding;
    }

    public String getSourceName() {
        return _src.getAbsolutePath();
    }

    public String getSourceBaseName() {
        return _src.getName();
    }

    public String getSourceDirName() {
        return _src.getParent();
    }

    public long getSourceLength() {
        return _src.length();
    }

    public long getSourceLastModified() {
        return _src.lastModified();
    }

    public String getDestinationName() {
        return _dst.getAbsolutePath();
    }

    public String getDestinationBaseName() {
        return _dst.getName();
    }

    public String getDestinationDirName() {
        return _dst.getParent();
    }

    public long getDestinationLength() {
        return _dst.length();
    }

    public long getDestinationLastModified() {
        return _dst.lastModified();
    }

    public String getSourceEncoding() {
        return _srcEncoding;
    }

    public String getDestinationEncoding() {
        return _dstEncoding;
    }
}
