package io.github.mxi.jmf;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public final class Processor {

    public static void exec(Reader source, Writer destination) throws IOException {

    }

    public static final class State {

        final StringBuilder render = new StringBuilder();

        char goal = '\0';

        int parenthesisBalance = 0;

        int quoteBalance = 0;
    }
}