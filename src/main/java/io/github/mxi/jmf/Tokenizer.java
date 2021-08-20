package io.github.mxi.jmf;

import java.io.*;
import java.util.Objects;
import java.util.Stack;

public class Tokenizer {

    private final Stack<Source> sources = new Stack<>();

    public Tokenizer() {
        /* */
    }


    public void sourceFile(String path) throws IOException {
        sourceFile(new File(path));
    }

    public void sourceFile(File file) throws IOException  {
        sourceStream(file.getPath(), new FileReader(file));
    }

    public void sourceString(String content) {
        int e = content.length();
        String description =
            content.substring(0, 3) + "..." + content.substring(e - 3);
        sourceStream(description, new StringReader(content));
    }

    public void sourceStream(String description, Reader stream) {
        sources.push(new Source(description, stream));
    }


    public String readNext() throws IOException {

    }


    static final class Source {

        public final String name;
        public final Reader stream;

        Source(String pName, Reader pStream) {
            name = Objects.requireNonNull(pName);
            stream = Objects.requireNonNull(pStream);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, stream);
        }

        @Override
        public String toString() {
            return String.format("Source[name %s]", name);
        }
    }
}
