package com.phantomworlds.models.instances;

/**
 * Represents the current state of an instance
 */
public enum InstanceState {
    /**
     * Instance is in lobby phase, waiting for players
     */
    LOBBY,
    
    /**
     * Instance is actively running
     */
    RUNNING,
    
    /**
     * Instance is ending/cleanup phase
     */
    ENDING,
    
    /**
     * Instance is completed successfully
     */
    COMPLETED,
    
    /**
     * Instance failed or was cancelled
     */
    FAILED
}
