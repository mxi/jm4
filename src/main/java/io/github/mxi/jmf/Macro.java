package io.github.mxi.jmf;

public interface Macro {
    String expand(Processor processor, String... args);
}
