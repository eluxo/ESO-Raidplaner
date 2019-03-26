package com.github.deityexe.command

class CommandMessageTest extends groovy.util.GroovyTestCase {
    void setUp() {
        super.setUp()
    }

    void tearDown() {
    }

    void testParseSimple() {
        CommandMessage message = CommandMessage.parse("!command arg0 arg1 arg2 arg3");
        assertEquals(message.getCommand(), "!command");
        assertEquals(message.getArgs().length, 4);
        for (int i = 0; i < 4; ++i) {
            assertEquals(message.arg(i), String.format("arg%d", i));
        }
    }

    void testParseMultispace() {
        CommandMessage message = CommandMessage.parse("!command   arg0 arg1  arg2    arg3");
        assertEquals(message.getCommand(), "!command");
        assertEquals(message.getArgs().length, 4);
        for (int i = 0; i < 4; ++i) {
            assertEquals(message.arg(i), String.format("arg%d", i));
        }
    }

    void testParseSimpleQuote() {
        CommandMessage message = CommandMessage.parse("!command \"arg0\" arg1 arg2 arg3");
        assertEquals(message.getCommand(), "!command");
        assertEquals(message.getArgs().length, 4);
        for (int i = 0; i < 4; ++i) {
            assertEquals(message.arg(i), String.format("arg%d", i));
        }
    }

    void testParseSpace() {
        CommandMessage message = CommandMessage.parse("!command \"arg0 arg1 arg2\" arg3");
        assertEquals(message.getCommand(), "!command");
        assertEquals(message.getArgs().length, 2);
        assertEquals(message.arg(0), "arg0 arg1 arg2");
        assertEquals(message.arg(1), "arg3");
    }

    void testParseEscape() {
        CommandMessage message = CommandMessage.parse("!command \"\\\"arg0 arg1 arg2\" arg3\\\"");
        assertEquals(message.getCommand(), "!command");
        assertEquals(message.getArgs().length, 2);
        assertEquals(message.arg(0), "\"arg0 arg1 arg2");
        assertEquals(message.arg(1), "arg3\"");

        message = CommandMessage.parse("!command \\\"\"arg0 arg1 arg2\" arg3\\\"");
        assertEquals(message.getCommand(), "!command");
        assertEquals(message.getArgs().length, 2);
        assertEquals(message.arg(0), "\"arg0 arg1 arg2");
        assertEquals(message.arg(1), "arg3\"");
    }

    void testParseUmlauts() {
        CommandMessage message = CommandMessage.parse("!command überhaupt öffentlich ärgerlich");
        assertEquals(message.getCommand(), "!command");
        assertEquals(message.getArgs().length, 3);
        assertEquals(message.arg(0), "überhaupt");
        assertEquals(message.arg(1), "öffentlich");
        assertEquals(message.arg(2), "ärgerlich");
    }

    void testParseUnicode() {
        CommandMessage message = CommandMessage.parse("!command \ua88a \u100f");
        assertEquals(message.getCommand(), "!command");
        assertEquals(message.getArgs().length, 2);
        assertEquals(message.arg(0), "\ua88a");
        assertEquals(message.arg(1), "\u100f");
    }

    void testArgOutOfBounds() {
        CommandMessage message = CommandMessage.parse("!command arg0 arg1 arg2 arg3");
        assertEquals(message.arg(-1), "");
        assertEquals(message.arg(4), "");
        assertEquals(message.arg(123), "");
    }
}
