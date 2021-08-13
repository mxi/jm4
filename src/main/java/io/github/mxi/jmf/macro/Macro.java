package io.github.mxi.jmf.macro;

import io.github.mxi.jmf.Frame;
import io.github.mxi.jmf.Parser;

import java.util.List;

public interface Macro {
    void expand(Parser parser, Frame frame, List<String> params);
}
