package com.kolesova_violetta.ltc.ui.fragments.view;

public interface ExitFromFragment {
    int EXIT_MONITORING = 11;
    int EXIT_REGISTRATION = 10;
    int EXIT_CONFIG = 12;

    void exitFromFragment(int exitCode);
}