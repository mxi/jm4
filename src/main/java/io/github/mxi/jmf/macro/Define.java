package io.github.mxi.jmf.macro;

import io.github.mxi.jmf.Frame;
import io.github.mxi.jmf.Parser;

import java.util.List;

public final class Define implements Macro {

    public static final String IDENTIFIER = "define";

    public static final Macro INSTANCE = new Define();

    private Define() {
        /* singleton */
    }

    @Override
    public void expand(Parser parser, Frame frame, List<String> params) {

    }
}
