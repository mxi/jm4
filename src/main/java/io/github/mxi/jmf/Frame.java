package io.github.mxi.jmf;

import io.github.mxi.jmf.util.Strings;
import io.github.mxi.jmf.util.Tokens;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Frame {

    public static final int NDIVERSIONS = 10;

    private int quoteBalance = 0;

    private int parenBalance = 0;

    private String symbol;

    private final List<String> params = new ArrayList<>();

    private final List<String> cparams = Collections.unmodifiableList(params);

    private final Set<String> terminal = new HashSet<>();

    /* diversions[0] = main;
     * diversions[1-9] = buffers;
     */
    private final Writer[] diversions = new Writer[NDIVERSIONS];

    private int diversion = 0;

    Frame(String... pTerminal) {
        this(null, pTerminal);
    }

    Frame(Writer pDestination, String... pTerminal) {
        /* diversion[0] = main; diversion[1-9] = lazy buffers */
        if (pDestination == null) {
            pDestination = new StringWriter();
        }
        diversions[0] = pDestination;

        /* terminal tokens which will end processing */
        terminal.addAll(
            Stream
                .of(pTerminal)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet())
        );
    }

    /* +--- state control ------------------------------------------+ */
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String pSymbol) {
        symbol = Tokens.isIdentifier(pSymbol) ? pSymbol : null;
    }

    public boolean hasSymbol() {
        return null != symbol;
    }

    public List<String> getParameters() {
        return cparams;
    }

    public void appendParameter(String pParameter) {
        params.add(Strings.isVapid(pParameter) ?
            Tokens.EMPTY : pParameter.trim());
    }

    public void clearParameters() {
        params.clear();
    }

    public boolean isTerminal(String token) {
        return Strings.isLegit(token) && terminal.contains(token);
    }

    /* +--- balance controls ---------------------------------------+ */
    public void decrQuoteBalance() {
        quoteBalance = Math.max(0, quoteBalance-1);
    }

    public void incrQuoteBalance() {
        ++quoteBalance;
    }

    public boolean isQuoteBalanced() {
        return 0 == quoteBalance;
    }

    public void decrParenthesisBalance() {
        parenBalance = Math.max(0, parenBalance-1);
    }

    public void incrParenthesisBalance() {
        ++parenBalance;
    }

    public boolean isParenthesisBalanced() {
        return 0 == parenBalance;
    }

    public boolean isBalanced() {
        return isQuoteBalanced() && isParenthesisBalanced();
    }

    /* +--- diversion stream controls ------------------------------+ */
    public void setDiversionIndex(int index) {
        diversion = index;
    }

    public int getDiversionIndex() {
        return diversion;
    }

    public Writer getDestinationDiversion() {
        return diversions[0];
    }

    public Writer getActiveDiversion() {
        return getDiversion(diversion);
    }

    public Writer getDiversion(int index) {
        if (index < 0 || diversions.length <= index) {
            return Writer.nullWriter();
        }
        return diversions[index];
    }

    /* +--- terminal operation controls ----------------------------+ */
    public void cleanup() throws IOException {
        Writer destination = getDestinationDiversion();
        for (int i = 1; i < diversions.length; ++i) {
            Writer div = diversions[i];
            if (div instanceof StringWriter) {
                StringWriter strdiv = (StringWriter) div;
                destination.write(
                    strdiv.getBuffer().toString());
            }
            else if (div != null) {
                div.close();
            }
        }
        destination.close();
    }
}