package org.gmplib.test;

import java.io.IOException;

public interface UI {

    void display(String line);
    void displayProgress(int pct);
    long getSeed() throws IOException;
}
