package com.github.deityexe;

/**
 * Exception for error events that shall be delivered to the actual user.
 *
 * {@link DeliverableError} exceptions are based on {@link RuntimeException} and are thrown at different locations were
 * error reporting to the user makes sense. If such an exception is thrown it will always be caught at the highest point
 * of execution on remote callbacks and the message will be delivered using the default way (normally a private message)
 * in a formatted way.
 */
public class DeliverableError extends RuntimeException {
    /**
     * Wrapped exception (if any).
     */
    private Exception exception = null;

    /**
     * If set false, the message should be delivered via channel (if possible).
     */
    private boolean privateError = true;

    /**
     * Constructor.
     *
     * @param message The error message to be delivered to the user.
     */
    public DeliverableError(String message) {
        super(message);
    }

    /**
     * Creates a new deliverable error and returns the object.
     *
     * @param message The message to be delivered to the user.
     * @return Instance of the new object.
     */
    public static DeliverableError create(String message) {
        return new DeliverableError(message);
    }

    /**
     * Sets the exception to be stored internalls.
     *
     * @param exeption The original exception.
     * @return Instance of itself.
     */
    public DeliverableError setException(Exception exeption) {
        this.exception = exeption;
        return this;
    }

    /**
     * Constructs the message to be sent to the user.
     *
     * @return Message to be sent to the user.
     */
    public String toUserMessage() {
        return this.getMessage();
    }

    /**
     * Switches between private and channel messages.
     *
     * @param privateError If true, the error is marked private and delivered via private message.
     * @return Self.
     */
    public DeliverableError setPrivate(boolean privateError) {
        this.privateError = privateError;
        return this;
    }
}
