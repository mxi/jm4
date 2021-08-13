package io.github.mxi.jmf;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTest {

    @Test
    void token1() throws IOException {
        List<String> expected = List.of(
            "here", "!", " ", "is", " ", "a", ",", "source");
        List<String> observed = new ArrayList<>();
        Parser parser = new Parser("here! is a,source");
        String token;
        while ((token = parser.fetchToken()).length() > 0) {
            observed.add(token);
        }
        assertEquals(expected, observed);
    }

    @Test
    void token2() throws IOException {
        List<String> expected = List.of(
            "here", "!", " ", "is", " ", "a", ",", "source");
        List<String> observed = new ArrayList<>();
        Parser parser = new Parser();
        parser.pushSource("a,source");
        parser.pushSource("here! is ");
        String token;
        while ((token = parser.fetchToken()).length() > 0) {
            observed.add(token);
        }
        assertEquals(expected, observed);
    }

    @Test
    void stress1() throws IOException {
        final String expected = "";
        final String observed = Parser.execute("define( )");
        assertEquals(expected, observed);
    }

    @Test
    void stress2() throws IOException {
        final String expected = " ( )";
        final String observed = Parser.execute("define ( )");
        assertEquals(expected, observed);
    }

    @Test
    void stress3() throws IOException {
        final String expected = "define( )";
        final String observed = Parser.execute("`define( )'");
        assertEquals(expected, observed);
    }
}
