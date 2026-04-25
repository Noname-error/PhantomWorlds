package com.phantomworlds.models.parties;

/**
 * Represents the current state of a party
 */
public enum PartyState {
    /**
     * Party is being formed, members can join/leave
     */
    FORMING,
    
    /**
     * Party is in lobby, waiting for instance to start
     */
    LOBBY,
    
    /**
     * Party is in an active instance
     */
    IN_INSTANCE,
    
    /**
     * Party is transitioning between instances
     */
    TRANSITIONING,
    
    /**
     * Party is disbanded
     */
    DISBANDED
}
