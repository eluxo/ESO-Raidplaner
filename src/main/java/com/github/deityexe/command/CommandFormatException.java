package com.github.deityexe.command;

import com.github.deityexe.DeliverableError;

/**
 * Raised by command classes if the format does not match the requirements.
 */
public class CommandFormatException extends DeliverableError {
    /**
     * Constructor.
     *
     * @param message The error message to be delivered to the user.
     */
    public CommandFormatException(String message) {
        super(message);
    }
}
