package com.github.deityexe.command;

import org.javacord.api.event.message.MessageCreateEvent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

/**
 * Represents a command message.
 *
 * This class parses user commands that are received as a message splits them into the actual command and the different
 * attributes of the command.
 */
public class CommandMessage implements Executable {
    /**
     * Holds the command string.
     */
    protected final String command;

    /**
     * Holds the command arguments.
     */
    protected final String[] args;

    /**
     * Stors the message create event.
     */
    protected MessageCreateEvent messageCreateEvent;

    /**
     * Holds the command environment.
     */
    protected ICommandEnvironment commandEnvironment;

    /**
     * Constructor.
     *
     * @param command An other command gto create this command on.
     */
    public CommandMessage(final CommandMessage command) {
        this.command = command.command;
        this.args = command.args;
        this.messageCreateEvent = command.messageCreateEvent;
        this.commandEnvironment = command.commandEnvironment;
    }

    /**
     * Stores the associated MessageCreateEvent.
     *
     * @param messageCreateEvent The instance of the message create event.
     * @return Self.
     */
    public CommandMessage setMessageCreateEvent(MessageCreateEvent messageCreateEvent) {
        this.messageCreateEvent = messageCreateEvent;
        return this;
    }

    /**
     * Constructor.
     *
     * @param command The command.
     * @param args The command arguments.
     */
    public CommandMessage(String command, String[] args) {
        this.command = command;
        this.args = args;
    }

    /**
     * Parses an incoming command string.
     *
     * @param message The message string.
     * @return Instance of the new command object.
     */
    public static CommandMessage parse(String message) {
        List<String> tokenList = new ArrayList<>();
        Scanner scanner = new Scanner();
        scanner.scan(message, token -> {
            if (token.type == Scanner.Token.TYPE_WORD) {
                final String entry = new String(token.value.toByteArray());
                tokenList.add(entry);
            }
        });

        // final String[] tokens = message.split(" ");
        final String[] tokens = tokenList.toArray(new String[] {});
        final String command = tokens[0];
        final String args[] = Arrays.copyOfRange(tokens, 1, tokens.length);
        return new CommandMessage(command, args);
    }

    /**
     * Getter for the command name.
     *
     * @return Command name.
     */
    public String getCommand() {
        return command;
    }

    /**
     * Getter for the command arguments.
     *
     * @return Returns the command arguments.
     */
    public String[] getArgs() {
        return args;
    }

    /**
     * Getter for the given argument.
     *
     * @param index Index of the argument.
     * @return Value for the argument or an empty string, if undefined.
     */
    public String arg(final int index) {
        return this.arg(index, "");
    }

    /**
     * Getter for the given argument.
     *
     * @param index The index of the argument.
     * @param fallback Fallback value, if index is out of bounds.
     * @return The value.
     */
    public String arg(final int index, final String fallback) {
        if (index < 0 || index >= this.args.length) {
            return fallback;
        }
        return this.args[index];
    }

    @Override
    public void execute() {
        throw new UnsupportedOperationException("execution not possible on CommandMessage object");
    }

    /**
     * Sets the execution environment for the command.
     *
     * @param commandEnvironment New command environment.
     * @return Self.
     */
    public CommandMessage setEnvironment(ICommandEnvironment commandEnvironment) {
        this.commandEnvironment = commandEnvironment;
        return this;
    }

    /**
     * Getter for the instance of the command environment.
     *
     * @return The command environment.
     */
    public ICommandEnvironment getCommandEnvironment() {
        return this.commandEnvironment;
    }

    /**
     * Getter for the message create event.
     *
     * @return The message create event.
     */
    protected MessageCreateEvent getMessageCreateEvent() {
        return this.messageCreateEvent;
    }

    /**
     * Removes the original command that has called us.
     */
    protected void removeCommand() {
        final MessageCreateEvent event = this.getMessageCreateEvent();
        try {
            event.getMessage().delete().join();
        } catch (CompletionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Scanner to split the tokens into meaningful items. This is technical not a pure lexical scanner as it also
     * does contain the actual analysis for quoting to also make sense of the stuff it reads.
     */
    private static class Scanner {
        public static class Token {
            public static final int TYPE_WHITESPACE = 0;
            public static final int TYPE_WORD = 1;
            public static final int TYPE_END = 2;

            public int type = TYPE_WORD;
            public ByteArrayOutputStream value = new ByteArrayOutputStream();
        }

        private static final byte ESCAPE_CHARS[] = new byte[] { '\\' };
        private static final byte QUOTE_CHARS[] = new byte[] { '\"' };
        private static final byte WHITESPACE_CHARS[] = new byte[] { ' ', '\t', '\n', '\r' };

        boolean escapeNext = false;
        boolean quoted = false;
        String error = null;

        boolean scan(String input, Consumer<Token> tokenConsumer) {
            Token token = null;
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
                byte data[] = new byte[1];

                while ((inputStream.read(data, 0, 1)) == 1) {
                    if (escapeNext) {
                        escapeNext = false;
                        (token = require(token, Token.TYPE_WORD, tokenConsumer)).value.write(data);
                        continue;
                    }

                    if (isEscape(data[0])) {
                        escapeNext = true;
                        continue;
                    }

                    if (quoted) {
                        if (this.isQuote(data[0])) {
                            quoted = false;
                            continue;
                        }

                        (token = require(token, Token.TYPE_WORD, tokenConsumer)).value.write(data);
                        continue;
                    }

                    if (this.isQuote(data[0])) {
                        quoted = true;
                        continue;
                    }

                    if (isWhitespace(data[0])) {
                        (token = require(token, Token.TYPE_WHITESPACE, tokenConsumer)).value.write(data);
                        continue;
                    }

                    (token = require(token, Token.TYPE_WORD, tokenConsumer)).value.write(data);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                this.error = ex.getMessage();
                return false;
            }

            if (quoted) {
                this.error = "quoted text ends without closing quote";
                return false;
            }

            if (escapeNext) {
                this.error = "text ends with escape";
            }

            token = this.require(token, Token.TYPE_END, tokenConsumer);
            return true;
        }

        /**
         * Replaces the current token if the type differs.
         *
         * @param current The current token.
         * @param type The type of the current token.
         * @param tokenConsumer Class consuming the tokens.
         * @return New token, if type differs. Else current.
         */
        Token require(Token current, int type, Consumer<Token> tokenConsumer) {
            if (current == null || current.type != type) {
                return (Token) nextToken(current, type, tokenConsumer);
            }
            return current;
        }

        /**
         * Creates a new token and notifies the observer.
         *
         * @param current The current token to be replaced.
         * @param type The type of the following token.
         * @param tokenConsumer Class consuming closed tokens.
         * @return The new token created.
         */
        Token nextToken(Token current, int type, Consumer<Token> tokenConsumer) {
            if (current != null) {
                tokenConsumer.accept(current);
            }
            Token next = new Token();
            next.type = type;
            return next;
        }

        /**
         * Checks, if the char is a whitespace character.
         *
         * @param t Value of the char to be checked.
         * @return True, if char is a whitespace.
         */
        boolean isWhitespace(byte t) {
            return contains(WHITESPACE_CHARS, t);
        }

        /**
         * Checks, if the char is a quote character.
         *
         * @param t Value of the char to be checked.
         * @return True, if char is a quote.
         */
        boolean isQuote(byte t) {
            return contains(QUOTE_CHARS, t);
        }

        /**
         * Checks, if the char is a escape character.
         *
         * @param t Value of the char to be checked.
         * @return True, if char is a escape.
         */
        boolean isEscape(byte t) {
            return contains(ESCAPE_CHARS, t);
        }

        /**
         * Checks, if the given char is in a list of chars.
         *
         * @param t Value of the char to be checked.
         * @return True, if char is in the list.
         */
        boolean contains(byte[] array, byte t) {
            for (byte element: array) {
                if (element == t) {
                    return true;
                }
            }
            return false;
        }
    }
}
