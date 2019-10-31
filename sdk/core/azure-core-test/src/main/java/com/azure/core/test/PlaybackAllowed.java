package com.azure.core.test;

import com.azure.core.test.annotation.IgnoreRecording;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import static org.junit.Assume.assumeTrue;

public class PlaybackAllowed extends TestWatcher {
    private volatile boolean isPlaybackAllowed;

    @Override
    protected void starting(Description description) {
        try {
            isPlaybackAllowed = description.getTestClass()
                .getMethod(description.getMethodName())
                .getAnnotation(IgnoreRecording.class) == null;
        } catch (NoSuchMethodException ex) {
            isPlaybackAllowed = true;
        }
    }

    public void assertPlaybackIsAllowed(TestMode testMode) {
        assumeTrue(isPlaybackAllowed && testMode == TestMode.PLAYBACK);
    }
}
