package io.github.mxi.jmf.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SourceReaderTest {

    @Test
    void stress1() throws IOException {
        final String content = "\n\n\n...\r";
        final SourceReader reader = SourceReader.fromString(content);
        final int length = reader.read(new char[4096]);

        assertEquals(content.length(), length);
        assertEquals(reader.getLine(), 4);
        assertEquals(reader.getColumn(), 1);
        assertTrue(reader.eof());
        assertTrue(reader.eofOrClosed());
    }

    @Test
    void stress2() throws IOException {
        final String content =
            "Tally ho gents,\n" +
            "'Tis but the morning dew\n" +
            "Which settles down";
        final SourceReader reader = SourceReader.fromString(content);
        final int length = reader.read(new char[4096]);

        assertEquals(reader.getDescription(), "Tal...own");
        assertEquals(content.length(), length);
        assertEquals(reader.getLine(), 3);
        assertEquals(reader.getColumn(), 19);
        assertTrue(reader.eof());
        assertTrue(reader.eofOrClosed());
    }
}
