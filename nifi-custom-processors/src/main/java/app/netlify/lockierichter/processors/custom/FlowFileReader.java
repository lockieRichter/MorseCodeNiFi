package app.netlify.lockierichter.processors.custom;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.nifi.processor.io.InputStreamCallback;

public class FlowFileReader implements InputStreamCallback {

    private String flowFileText;

    public FlowFileReader() {
        flowFileText = "";
    }

    @Override
    public void process(InputStream in) throws IOException {
        String textValue = IOUtils.toString(in, Charset.defaultCharset());
        flowFileText = textValue;
    }

    public String getFlowFileText() {
        return flowFileText;
    }
}
