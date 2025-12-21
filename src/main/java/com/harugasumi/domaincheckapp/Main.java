package com.harugasumi.domaincheckapp;

import javax.swing.SwingUtilities;

import com.harugasumi.core.SentinelUI;


public class Main {
    public static void main(String[] args) {

         SwingUtilities.invokeLater(() -> {
            new SentinelUI().setVisible(true);
        });

    }

}