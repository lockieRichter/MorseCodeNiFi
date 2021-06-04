package app.netlify.lockierichter.processors.custom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processor.io.OutputStreamCallback;

@Tags({ "Morse", "Code", "Converter" })
@CapabilityDescription("Converts the text in a FlowFile to Morse Code and writes it back out.")
public class ConvertToMorseCode extends AbstractProcessor {

    public static final Relationship REL_SUCCESS = new Relationship.Builder().name("Success")
            .description("Successfully converted FlowFiles will be routed to this relationship.").build();

    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;

    // Characters that we can convert to morse code
    private static char[] letters = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' };

    // Morse code characters in alphabetical order
    private static String[] morseLetters = { ".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---",
            "-.-", ".-..", "--", "-.", "---", ".--.", "--.-", ".-.", "...", "-", "..-", "...-", ".--", "-..-", "-.--",
            "--..", "|" };

    @Override
    protected void init(final ProcessorInitializationContext context) {
        descriptors = new ArrayList<>();
        this.descriptors = Collections.unmodifiableList(descriptors);

        relationships = new HashSet<>();
        relationships.add(REL_SUCCESS);
        this.relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        final AtomicReference<String> flowFileText = new AtomicReference<>();

        // To read the contents of the FlowFile
        session.read(flowFile, new InputStreamCallback() {
            @Override
            public void process(InputStream in) throws IOException {
                String textValue = IOUtils.toString(in, Charset.defaultCharset());
                flowFileText.set(textValue);
            }
        });

        // To write the results back out to FlowFile
        flowFile = session.write(flowFile, new OutputStreamCallback() {

            @Override
            public void process(OutputStream out) throws IOException {
                String morseCodeText = convertTextToMorseCode(flowFileText.get());
                out.write(morseCodeText.getBytes());
            }
        });

        session.transfer(flowFile, REL_SUCCESS);

    }

    /**
     * Takes a blob of text and converts it to morse code. Words are separated by
     * seven dots.
     *
     * @param text the text blob to convert.
     * @return the converted text blob as morse code.
     */
    protected static String convertTextToMorseCode(String text) {
        // Remove all punctuation.
        String[] tokens = text.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase().split("\\s+");
        List<String> convertedTokens = new ArrayList<>();

        // Loop over each word and convert it to morse code.
        for (String token : tokens) {
            String convertedToken = convertWordToMorseCode(token);
            convertedTokens.add(convertedToken);
        }

        // Return the converted tokens as a single string, separated by seven dots.
        return String.join(" ....... ", convertedTokens);
    }

    /**
     * Converts a given word to morse code.
     *
     * @param word the word to convert.
     * @return the converted word in morse code.
     */
    protected static String convertWordToMorseCode(String word) {
        StringBuilder sb = new StringBuilder();
        // Loop over the characters in our word and get the index of the letter.
        for (char c : word.toLowerCase().toCharArray()) {
            int index = ArrayUtils.indexOf(letters, c);
            // An index of -1 means that the character wasn't found in our letters array.
            if (index != -1) {
                String morseChar = morseLetters[index];
                sb.append(morseChar);
                sb.append(" ");
            }
        }

        return sb.toString().trim();
    }
}
