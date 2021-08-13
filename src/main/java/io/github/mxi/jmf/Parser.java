package io.github.mxi.jmf;

import io.github.mxi.jmf.macro.Define;
import io.github.mxi.jmf.macro.Macro;
import io.github.mxi.jmf.util.Strings;
import io.github.mxi.jmf.util.Tokens;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;

public class Parser {

    /* +------------------------------------------------------------+ */
    /* | Static Utilities                                           | */
    /* +------------------------------------------------------------+ */
    public static StringReader stringReader(String pSource) {
        return new StringReader(
            Strings.isVapid(pSource) ? Tokens.EOF : pSource);
    }

    public static String execute(String pSource) throws IOException {
        return execute(stringReader(pSource));
    }

    public static String execute(Reader pSource) throws IOException {
        StringWriter dest = new StringWriter();
        Parser parser = new Parser(pSource, dest);
        parser.execute();
        return dest.getBuffer().toString();
    }

    /* +------------------------------------------------------------+ */
    /* | Syntax Configuration                                       | */
    /* +------------------------------------------------------------+ */

    /* +--- quote open ---------------------------------------------+ */
    private String quoteOpen = "`";

    public String getQuoteOpen() {
        return quoteOpen;
    }

    public void setQuoteOpen(String pQuoteOpen) {
        if (!Strings.isLikeChar(pQuoteOpen)) {
            throw new IllegalArgumentException(
                "new open quote must be a single character long");
        }
        quoteOpen = pQuoteOpen;
    }

    /* +--- quote close --------------------------------------------+ */
    private String quoteClose = "'";

    public String getQuoteClose() {
        return quoteClose;
    }

    public void setQuoteClose(String pQuoteClose) {
        if (!Strings.isLikeChar(pQuoteClose)) {
            throw new IllegalArgumentException(
                "new close quote must be a single character long");
        }
        quoteClose = pQuoteClose;
    }

    /* +--- paren open ---------------------------------------------+ */
    private String parenOpen = "(";

    public String getParenthesisOpen() {
        return parenOpen;
    }

    public void setParenthesisOpen(String pParenthesisOpen) {
        if (!Strings.isLikeChar(pParenthesisOpen)) {
            throw new IllegalArgumentException(
                "new open parenthesis must be a single character long");
        }
        parenOpen = pParenthesisOpen;
    }

    /* +--- param sep ----------------------------------------------+ */
    private String paramDelim = ",";

    public String getParameterDelimiter() {
        return paramDelim;
    }

    public void setParamaeterDelimiter(String pParameterDelimiter) {
        if (!Strings.isLikeChar(pParameterDelimiter)) {
            throw new IllegalArgumentException(
                "new parameter delimiter must be a single character long");
        }
        paramDelim = pParameterDelimiter;
    }

    /* +---- paren close -------------------------------------------+ */
    private String parenClose = ")";

    public String getParenthesisClose() {
        return parenClose;
    }

    public void setParenthesisClose(String pParenthesisClose) {
        if (!Strings.isLikeChar(pParenthesisClose)) {
            throw new IllegalArgumentException(
                "new closing parenthesis must be a single character long");
        }
        parenClose = pParenthesisClose;
    }

    /* +---- quote set ---------------------------------------------+ */
    public void setQuotePair(String pQuoteOpen, String pQuoteClose) {
        setQuoteOpen(pQuoteOpen);
        setQuoteClose(pQuoteClose);
    }

    public void setParenthesisPair(
        String pParenthesisOpen, String pParenthesisClose)
    {
        setParenthesisOpen(pParenthesisOpen);
        setParenthesisClose(pParenthesisClose);
    }

    /* +------------------------------------------------------------+ */
    /* | Machine Implementation                                     | */
    /* +------------------------------------------------------------+ */
    private final Map<String, Macro> macros = new HashMap<>();

    private final Stack<PushbackReader> source = new Stack<>();

    private final Stack<String> stash = new Stack<>();

    private final Stack<Frame> stack = new Stack<>();


    public Parser() {
        this(Tokens.EOF);
    }

    public Parser(String pSource) {
        this(pSource, null);
    }

    public Parser(String pSource, Writer pDestination) {
        this(stringReader(pSource), pDestination);
    }

    public Parser(Reader pSource) {
        this(pSource, null);
    }

    public Parser(Reader pSource, Writer pDestination) {
        pushSource(pSource);
        pushFrame(pDestination, Tokens.EOF);
        loadBuiltinMacros();
    }

    private void loadBuiltinMacros() {
        /* define macro */
//        addMacro(Debug.IDENTIFIER, Debug.INSTANCE);
        addMacro(Define.IDENTIFIER, Define.INSTANCE);
    }

    /* +--- parser routines ----------------------------------------+ */
    public void execute() throws IOException {
        while (hasFrame()) {
            String token = fetchToken();
            Frame frame = peekFrame();

            /* check if terminal */
            if (frame.isBalanced() && frame.isTerminal(token)) {
                Frame old = popFrame();
                Frame now = peekFrame();
                if (now != null) {
                    /* NOTE: dest already close from popFrame() */
                    StringWriter strdest = (StringWriter) old
                        .getDestinationDiversion();
                    now.appendParameter(
                        strdest.getBuffer().toString());

                    if (token.equals(parenClose)) {
                        expand(now.getSymbol(), now.getParameters());
                        now.setSymbol(null);
                        now.clearParameters();
                    }
                    else if (token.equals(paramDelim)) {
                        pushFrame(Tokens.EOF, paramDelim, parenClose);
                    }
                }
                continue;
            }

            /* ensure EOF ends processing if not caught by prior block */
            if (token.equals(Tokens.EOF)) {
                break;
            }

            /* check if special token */
            if (token.equals(quoteOpen)) {
                if (!frame.isQuoteBalanced()) {
                    frame.getActiveDiversion().write(quoteOpen);
                }
                frame.incrQuoteBalance();
                continue;
            }

            if (token.equals(quoteClose)) {
                frame.decrQuoteBalance();
                if (!frame.isQuoteBalanced()) {
                    frame.getActiveDiversion().write(quoteClose);
                }
                continue;
            }

            if (token.equals(parenOpen)) {
                frame.incrParenthesisBalance();
                frame.getActiveDiversion().write(parenOpen);
                continue;
            }

            if (token.equals(parenClose)) {
                frame.decrParenthesisBalance();
                frame.getActiveDiversion().write(parenClose);
                continue;
            }

            /* check if macro */
            if (frame.isQuoteBalanced() && macros.containsKey(token)) {
                /* check for parameters */
                if (peekToken().equals(parenOpen)) {
                    eatToken();
                    frame.setSymbol(token);
                    pushFrame(Tokens.EOF, paramDelim, parenClose);
                }
                else {
                    expand(token, Collections.emptyList());
                }
                continue;
            }

            /* otherwise, write out token */
            frame.getActiveDiversion().write(token);
        }

        /* TODO: if frames left, notify with warning or error */
    }

    public void expand(String macro, List<String> parameters) {
        Frame frame = peekFrame();
        if (null == frame || !Tokens.isIdentifier(macro) || !macros.containsKey(macro)) {
            return;
        }
        final List<String> trueparams = new ArrayList<>();
        trueparams.add(macro);
        if (parameters != null) {
            trueparams.addAll(parameters);
        }

        try {
            macros.get(macro).expand(this, frame, trueparams);
        }
        catch (Exception e) {
            /* TODO: handle macro errors */
        }
    }

    public void pushFrame(String... pTerminal) {
        pushFrame(null, pTerminal);
    }

    public void pushFrame(Writer pDestination, String... pTerminal) {
        stack.push(new Frame(pDestination, pTerminal));
    }

    public Frame peekFrame() {
        if (stack.empty()) {
            return null;
        }
        return stack.peek();
    }

    public Frame popFrame() throws IOException {
        if (stack.empty()) {
            return null;
        }
        Frame top = stack.pop();
        top.cleanup();
        return top;
    }

    public boolean hasFrame() {
        return !stack.empty();
    }

    /* +--- macro management routines ------------------------------+ */
    public void addMacro(String identifier, Macro macro) {
        /* checks */
        if (!Tokens.isIdentifier(identifier)) {
            throw new IllegalArgumentException(
                "identifier string not a valid identifier (Parser.isIdentifier)");
        }
        if (macros.containsKey(identifier)) {
            throw new IllegalArgumentException(String.format(
                "macro with identifier '%s' already registered", identifier));
        }
        Objects.requireNonNull(macro,
            "non-null macro required for registration");

        /* put */
        macros.put(identifier, macro);
    }

    public void removeMacro(String identifier) {
        macros.remove(identifier);
    }

    /* +--- high-level IO & configuration --------------------------+ */
    public void pushSource(String pSource) {
        pushSource(new StringReader(pSource == null ? Tokens.EOF : pSource));
    }

    public void pushSource(Reader pSource) {
        PushbackReader x;
        if (pSource instanceof PushbackReader) {
            x = (PushbackReader) pSource;
        }
        else {
            x = new PushbackReader(
                Objects.requireNonNull(pSource, "non-null Reader required"));
        }
        source.push(x);
    }

    public String fetchToken() throws IOException  {
        /* use top of stash first */
        if (!stash.empty()) {
            return stash.pop();
        }

        /* remove any exhausted streams */
        PushbackReader in = null;
        int c = 0;
        while (!source.empty()) {
            in = source.peek();
            if ((c = in.read()) != -1) {
                in.unread(c);
                break;
            }
            else {
                in.close();
                in = null;
                source.pop();
            }
        }

        if (null == in) {
            return Tokens.EOF;
        }

        /* ok */
        if (Character.isWhitespace(c)) {
            return readTokenPredicated(in, Character::isWhitespace);
        }
        else if (Character.isJavaIdentifierPart(c)) {
            return readTokenPredicated(in, Character::isJavaIdentifierPart);
        }
        else {
            return Character.toString(in.read());
        }
    }

    public String peekToken() throws IOException {
        String fetched = fetchToken();
        stashToken(fetched);
        return fetched;
    }

    public void stashToken(String token) {
        if (Strings.isLegit(token)) {
            stash.push(token);
        }
    }

    public void eatToken() throws IOException {
        /* noreturn */ fetchToken();
    }

    /* +--- low-level IO -------------------------------------------+ */
    private static String readTokenPredicated(
        PushbackReader in, Predicate<Integer> predicate) throws IOException
    {
        final StringBuilder buffer = new StringBuilder();
        int c;
        while ((c = in.read()) != -1 && predicate.test(c)) {
            buffer.append(Character.toChars(c));
        }
        if (c != -1) {
            in.unread(c);
        }
        return buffer.toString();
    }
}
