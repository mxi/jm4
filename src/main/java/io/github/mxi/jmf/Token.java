package io.github.mxi.jmf;

import java.util.Map;

public final class Token {

    public enum Type {
        /* A special token with empty content but NULL classification. */
        NULL,

        /* A special token with empty content but EOF classification. */
        EOF,

        /* Token consists of alphanumeric characters only. */
        WORD,

        /* Token consists of whitespace characters only. */
        SPACE,

        /* Token which does not fit any other classification; mostly punctuation. */
        OTHER
    }

    private static final Map<String, Token> COMMON = Map.of(

    );

    public static Token of(String content) {
        Token predefined = COMMON.get(content);
        if (predefined != null) {
            return predefined;
        }

        return new Token()
    }

    private final Type type;
    private final String content;
    private final int hash;

    private Token(Type
}
