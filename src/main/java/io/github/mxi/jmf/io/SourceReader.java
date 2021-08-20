package io.github.mxi.jmf.io;

import java.io.*;
import java.util.Objects;
import java.util.function.Function;

public class SourceReader extends Reader {

    public static SourceReader fromFile(String pPath) throws IOException {
        return new SourceReader(pPath, new FileReader(pPath));
    }

    public static SourceReader fromFile(File pFile) throws IOException {
        return new SourceReader(pFile.getPath(), new FileReader(pFile));
    }

    public static SourceReader fromString(String pSource) {
        String desc = pSource;
        if (pSource.length() > 9) { /* truncate desc */
            desc =
                pSource.substring(0, 3) +
                "..." +
                pSource.substring(pSource.length() - 3);
        }
        return new SourceReader(desc, new StringReader(pSource));
    }


    private static final int DEFAULT_PUSHBACK_SIZE = 256;

    private final String description;
    private Reader source;

    private final char[] pushback;
    private int pushbackIdx;

    private int line = 1;
    private int column = 1;

    public SourceReader(
        String pDesc,
        Reader pSource,
        int    pPushbackSize)
    {
        description = Objects.requireNonNullElse(
            pDesc, "<undescribed>");
        source = Objects.requireNonNull(pSource);

        pushback = new char[Math.max(pPushbackSize, 1)];
        pushbackIdx = pushback.length;
    }

    public SourceReader(String pDesc, Reader pSource) {
        this(pDesc, pSource, DEFAULT_PUSHBACK_SIZE);
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        ensureOpen();
        Objects.checkFromIndexSize(off, len, cbuf.length);

        /* save some information for records later */
        int read = 0;
        int endidx = off;
        int srcidx = off;

        /* read pushback first */
        if (pushbackIdx < pushback.length) {
            int pblen = pushback.length - pushbackIdx;
            int lim = Math.min(pblen, len);
            System.arraycopy(pushback, pushbackIdx, cbuf, off, lim);
            endidx += lim;
            srcidx += lim;
            read += lim;
        }

        /* read from source reader next */
        if (len > 0) {
            int lim = source.read(cbuf, off, len);
            if (lim != -1) {
                endidx += lim;
            }
            read += lim;
        }

        /* iterate over source-read characters and update */
        for (int i = srcidx; i < endidx; ++i) {
            char ch = cbuf[i];
            switch (ch) {
                case '\n': /* FALLTHROUGH */
                    ++line;
                case '\r':
                    column = 1;
                    break;
                default:
                    ++column;
            }
        }

        /* return from disaster */
        return read >= 0 ? endidx - off : -1;
    }

    public int unread(char[] cbuf, int off, int len) {
        Objects.checkFromIndexSize(off, len, cbuf.length);
        Objects.checkFromIndexSize(0, len, pushbackIdx);

        int idx = pushbackIdx - len;
        System.arraycopy(cbuf, off, pushback, idx, len);
        pushbackIdx = idx;

        return len;
    }

    public int unread(char[] cbuf) {
        return unread(cbuf, 0, cbuf.length);
    }

    public int unread(int ch) {
        if (ch != -1) {
            return unread(new char[] { (char) ch });
        }
        return -1;
    }

    public boolean eof() throws IOException {
        return pushbackIdx == pushback.length && unread(source.read()) < 0;
    }

    public boolean eofOrClosed() throws IOException {
        return source == null || eof();
    }

    @Override
    public void close() throws IOException {
        if (source != null) {
            source.close();
        }
        source = null;
        pushbackIdx = pushback.length;
    }

    private void ensureOpen() throws IOException {
        if (source == null && pushbackIdx == pushback.length) {
            throw new IOException("closed");
        }
    }

    public String getDescription() {
        return description;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public int getPushbackSize() {
        return pushback.length - pushbackIdx;
    }

    public String getLocationString() {
        return String.format("%s %d:%d-%d",
            description, line, column, getPushbackSize());
    }

    @Override
    public String toString() {
        return getLocationString();
    }
}
