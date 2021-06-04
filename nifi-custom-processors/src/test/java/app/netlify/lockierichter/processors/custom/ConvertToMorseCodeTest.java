package app.netlify.lockierichter.processors.custom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ConvertToMorseCodeTest {

    private TestRunner testRunner;

    @BeforeEach
    void init() {
        testRunner = TestRunners.newTestRunner(ConvertToMorseCode.class);
    }

    private static Stream<Arguments> testProcessor() {
        return Stream.of(Arguments.of("BasicTextFlowFile", "BasicMorseCodeFlowFile"));
    }

    @ParameterizedTest
    @MethodSource
    void testProcessor(String inputFile, String expectedFile) throws IOException {
        testRunner.assertQueueEmpty();
        testRunner.run();
        testRunner.assertQueueEmpty();

        InputStream input = getClass().getClassLoader().getResourceAsStream(inputFile);
        testRunner.enqueue(input);
        testRunner.assertQueueNotEmpty();
        testRunner.run();
        testRunner.assertQueueEmpty();

        int expectedLength = 1;

        List<MockFlowFile> successFiles = testRunner.getFlowFilesForRelationship(ConvertToMorseCode.REL_SUCCESS);
        assertEquals(expectedLength, successFiles.size());

        MockFlowFile result = successFiles.get(0);
        InputStream expectedContentStream = getClass().getClassLoader().getResourceAsStream(expectedFile);
        String expectedContent = IOUtils.toString(expectedContentStream, StandardCharsets.UTF_8.name());
        result.assertContentEquals(expectedContent);

    }

    private static Stream<Arguments> testConvertWordToMorseCode() {
        return Stream.of(Arguments.of("Alice", ".- .-.. .. -.-. ."), Arguments.of("and", ".- -. -.."),
                Arguments.of("Bob", "-... --- -..."), Arguments.of("", ""), Arguments.of("       ", ""),
                Arguments.of("   -- { _}    ", ""));
    }

    @ParameterizedTest
    @MethodSource
    void testConvertWordToMorseCode(String input, String expected) {
        String actual = ConvertToMorseCode.convertWordToMorseCode(input);
        Assertions.assertEquals(expected, actual);
    }

    private static Stream<Arguments> testConvertTextToMorseCode() {
        return Stream.of(Arguments.of("Alice and Bob", ".- .-.. .. -.-. . ....... .- -. -.. ....... -... --- -..."),
                Arguments.of("", ""), Arguments.of("       ", ""), Arguments.of("   -- { _}    ", ""));
    }

    @ParameterizedTest
    @MethodSource
    void testConvertTextToMorseCode(String input, String expected) {
        String actual = ConvertToMorseCode.convertTextToMorseCode(input);
        Assertions.assertEquals(expected, actual);
    }

}
