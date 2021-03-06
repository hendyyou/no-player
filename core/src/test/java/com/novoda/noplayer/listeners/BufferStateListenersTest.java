package com.novoda.noplayer.listeners;

import com.novoda.noplayer.Player;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.Mockito.verify;

public class BufferStateListenersTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private Player.BufferStateListener aBufferStateListener;

    @Mock
    private Player.BufferStateListener anotherBufferStateListener;

    private BufferStateListeners bufferStateListeners;

    @Before
    public void setUp() {
        bufferStateListeners = new BufferStateListeners();
        bufferStateListeners.add(aBufferStateListener);
        bufferStateListeners.add(anotherBufferStateListener);
    }

    @Test
    public void givenBufferStateListeners_whenNotifyingOfBufferStarted_thenAllTheListenersAreNotifiedAppropriately() {

        bufferStateListeners.onBufferStarted();

        verify(aBufferStateListener).onBufferStarted();
        verify(anotherBufferStateListener).onBufferStarted();
    }

    @Test
    public void givenBufferStateListeners_whenNotifyingOfBufferCompleted_thenAllTheListenersAreNotifiedAppropriately() {

        bufferStateListeners.onBufferCompleted();

        verify(aBufferStateListener).onBufferCompleted();
        verify(anotherBufferStateListener).onBufferCompleted();
    }
}
