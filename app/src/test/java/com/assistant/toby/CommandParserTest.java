package com.assistant.toby;

import org.junit.Test;
import static org.junit.Assert.*;
import static com.assistant.toby.CommandParser.Type.*;

public class CommandParserTest {
    @Test public void recognizesWakePrefixAndCase() {
        assertEquals(HELP, CommandParser.parse("HEY TOBY, help").type);
        assertEquals(HELLO, CommandParser.parse("Toby hi").type);
        assertEquals(CREATOR, CommandParser.parse("Who created you?").type);
    }
    @Test public void preservesNoteAndEchoContent() {
        assertEquals("Buy Milk!", CommandParser.parse("Hey Toby, save a note: Buy Milk!").text);
        assertEquals(SAVE_NOTE, CommandParser.parse("save note").type);
        assertEquals("", CommandParser.parse("save note").text);
        assertEquals(READ_NOTES, CommandParser.parse("Read my notes").type);
        assertEquals("Hello World", CommandParser.parse("echo Hello World").text);
    }
    @Test public void avoidsAccidentalActionsInsideQuestions() {
        assertEquals(QUESTION, CommandParser.parse("How does an alarm work?").type);
        assertEquals(QUESTION, CommandParser.parse("What is a stopwatch?").type);
        assertEquals(QUESTION, CommandParser.parse("Who invented the timer?").type);
        assertEquals(QUESTION, CommandParser.parse("Can music help you sleep?").type);
    }
    @Test public void stopwatchStopIsNotStart() {
        assertEquals(STOP_STOPWATCH, CommandParser.parse("stop the stopwatch").type);
        assertEquals(START_STOPWATCH, CommandParser.parse("start stopwatch").type);
        assertEquals(CANCEL, CommandParser.parse("cancel").type);
    }
    @Test public void parsesCompoundAndSpokenDurations() {
        assertEquals(600, CommandParser.timerSeconds("set a timer for ten minutes"));
        assertEquals(5400, CommandParser.timerSeconds("timer for 1 hour 30 minutes"));
        assertEquals(75, CommandParser.timerSeconds("start countdown for one minute and fifteen seconds"));
        assertEquals(21, CommandParser.timerSeconds("timer for twenty-one seconds"));
        assertEquals(3600, CommandParser.timerSeconds("timer for an hour"));
        assertEquals(90, CommandParser.timerSeconds("timer 90 seconds"));
    }
    @Test public void rejectsMissingUnitsNegativeAmbiguousAndOverflow() {
        for (String value : new String[]{"timer", "timer 10", "timer -1 minutes", "timer 0 seconds",
                "timer 1.5 minutes", "timer one hundred minutes", "timer 10 minutes tomorrow",
                "timer 999999999999999999999999 hours", "timer 2147483647 hours"}) {
            assertEquals(value, -1, CommandParser.timerSeconds(value));
        }
    }
    @Test public void parsesExplicitAlarmTimeButDoesNotGuessDayOrAmbiguousHour() {
        assertArrayEquals(new int[]{7, 0}, CommandParser.alarmTime("set an alarm for 7 am"));
        assertArrayEquals(new int[]{0, 0}, CommandParser.alarmTime("alarm at 12 am"));
        assertArrayEquals(new int[]{12, 0}, CommandParser.alarmTime("alarm at 12 pm"));
        assertArrayEquals(new int[]{19, 30}, CommandParser.alarmTime("alarm at 19:30"));
        for (String value : new String[]{"alarm at 7", "alarm at 25:00", "alarm at 7:60", "alarm at 0 pm", "alarm at 7 am tomorrow"}) {
            assertNull(value, CommandParser.alarmTime(value));
        }
    }
}
