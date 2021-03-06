package com.novoda.noplayer.mediaplayer;

import android.view.View;

import com.novoda.noplayer.Player;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.Mockito.*;

public class BuggyVideoDriverPreventerTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private View videoContainer;

    @Mock
    private Player player;

    @Mock
    private PlayerChecker playerChecker;

    @InjectMocks
    private BuggyVideoDriverPreventer buggyVideoDriverPreventer;

    @Test
    public void givenVideoDriverIsNotBuggy_whenPreventingVideoDriverBug_thenNothingHappens() {
        when(playerChecker.getPlayerType()).thenReturn(AndroidMediaPlayerType.NU);

        buggyVideoDriverPreventer.preventVideoDriverBug();

        verifyZeroInteractions(videoContainer);
    }

    @Test
    public void givenVideoDriverCanBeBuggy_whenPreventingVideoDriverBug_thenABuggyDriverLayoutListenerIsAddedToTheVideoContainer() {
        when(playerChecker.getPlayerType()).thenReturn(AndroidMediaPlayerType.AWESOME);

        buggyVideoDriverPreventer.preventVideoDriverBug();

        verify(videoContainer).addOnLayoutChangeListener(any(OnPotentialBuggyDriverLayoutListener.class));
    }
}
