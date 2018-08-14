package com.csc.test_agent.exception;

public class TestAgentRuntimeException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID = -5901773945596136572L;

    /**
     * Constructor.
     *
     * @param message message
     */
    public TestAgentRuntimeException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param t root cause
     */
    public TestAgentRuntimeException(Throwable t) {
        super(t.getMessage(), t);
    }

    /**
     * Constructor.
     *
     * @param message message
     * @param t root cause
     */
    public TestAgentRuntimeException(String message, Throwable t) {
        super(message, t);
    }
}
