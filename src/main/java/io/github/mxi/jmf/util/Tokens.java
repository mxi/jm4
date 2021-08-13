package io.github.mxi.jmf.util;

public class Tokens {

    public static final String EMPTY = "";

    public static final String EOF = "";

    public static boolean isIdentifier(String token) {
        if (Strings.isVapid(token)) {
            return false;
        }
        boolean verdict = true;
        for (int i = 1; i < token.length(); ++i) {
            verdict = verdict && Character.isJavaIdentifierPart(
                token.charAt(i));
        }
        return verdict && Character.isJavaIdentifierStart(
            token.charAt(0));
    }
}
