package com.mentormatch.utils;

public final class TestPause {
    private TestPause() {
    }

    public static void forMilliseconds(long milliseconds) {
        if (milliseconds <= 0) {
            return;
        }

        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Test pause was interrupted", exception);
        }
    }
}
