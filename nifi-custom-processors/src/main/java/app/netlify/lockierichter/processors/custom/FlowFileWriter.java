package app.netlify.lockierichter.processors.custom;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.nifi.processor.io.OutputStreamCallback;

public class FlowFileWriter implements OutputStreamCallback {

    private final String flowFileText;

    public FlowFileWriter(String flowFileText) {
        this.flowFileText = flowFileText;
    }

    @Override
    public void process(OutputStream out) throws IOException {
        out.write(flowFileText.getBytes());
    }
}
