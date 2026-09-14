package com.assistant.toby;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Pure command parsing, shared by typed and spoken input. No device side effects. */
public final class CommandParser {
    public enum Type { HELP, HELLO, CREATOR, SAVE_NOTE, READ_NOTES, ALARM, TIMER,
        START_STOPWATCH, STOP_STOPWATCH, TIME, DATE, ECHO, CANCEL, QUESTION }
    public static final class Command {
        public final Type type;
        public final String text;
        Command(Type type, String text) { this.type = type; this.text = text; }
    }
    private static final Pattern DURATION = Pattern.compile(
            "(\\d+)\\s*(hours?|hrs?|minutes?|mins?|seconds?|secs?)\\b");
    private CommandParser() {}

    public static Command parse(String input) {
        String text = input == null ? "" : input.trim().replaceFirst("(?i)^(?:hey\\s+)?toby\\b[,!:]?\\s*", "").trim();
        String lower = text.toLowerCase(Locale.US).replace('’', '\'');
        Type type;
        if (lower.matches("(?:cancel|stop|never mind|nevermind)")) type = Type.CANCEL;
        else if (lower.matches("(?:help|commands|show commands)")) type = Type.HELP;
        else if (lower.matches("(?:hello|hi|hey)[!.]?")) type = Type.HELLO;
        else if (lower.matches("who (?:created|made|built) you[?]?")) type = Type.CREATOR;
        else if (lower.matches("(?:read|show)(?: my| the)? notes?[.!]?")) type = Type.READ_NOTES;
        else if (lower.matches("(?:save|take|add)(?: a)? note\\b.*")) {
            return new Command(Type.SAVE_NOTE, text.replaceFirst("(?i)^(?:save|take|add)(?: a)? note\\b[: ,]*", ""));
        } else if (lower.matches("(?:stop|pause)(?: the| my| a)? stopwatch[.!]?")) type = Type.STOP_STOPWATCH;
        else if (lower.matches("(?:(?:start|open)(?: a| the| my)? )?stopwatch[.!]?")) type = Type.START_STOPWATCH;
        else if (lower.matches("(?:(?:set|start)(?: a| the| my)? )?(?:timer|countdown)\\b.*")) type = Type.TIMER;
        else if (lower.matches("(?:(?:set|create|open)(?: an| the| my)? )?alarm\\b.*")) type = Type.ALARM;
        else if (lower.matches("(?:what time is it|what's the time|what is the time|time)[?]?")) type = Type.TIME;
        else if (lower.matches("(?:what(?:'s| is) (?:the |today's )?date|date)[?]?")) type = Type.DATE;
        else if (lower.startsWith("echo ")) return new Command(Type.ECHO, text.substring(5).trim());
        else type = Type.QUESTION;
        return new Command(type, text);
    }

    /** Seconds, or -1 for missing/ambiguous/invalid duration. Never guesses a unit. */
    public static int timerSeconds(String input) {
        String value = input.toLowerCase(Locale.US).replace('-', ' ');
        // Reject negatives before normalizing hyphenated number words.
        if (input.matches(".*-\\s*\\d.*")) return -1;
        String[] small = {"zero","one","two","three","four","five","six","seven","eight","nine",
                "ten","eleven","twelve","thirteen","fourteen","fifteen","sixteen","seventeen","eighteen","nineteen"};
        String[] tens = {"twenty","thirty","forty","fifty","sixty","seventy","eighty","ninety"};
        for (int t = 0; t < tens.length; t++) {
            for (int s = 1; s < 10; s++) value = value.replaceAll("\\b" + tens[t] + "\\s+" + small[s] + "\\b", String.valueOf((t + 2) * 10 + s));
            value = value.replaceAll("\\b" + tens[t] + "\\b", String.valueOf((t + 2) * 10));
        }
        for (int n = 0; n < small.length; n++) value = value.replaceAll("\\b" + small[n] + "\\b", String.valueOf(n));
        value = value.replaceAll("\\b(?:a|an) (?=hour|minute|second)", "1 ")
                .replaceFirst("^(?:(?:set|start)(?: a| the| my)? )?(?:timer|countdown)\\b", "")
                .replaceFirst("^\\s*for\\b", "").trim();
        Matcher matcher = DURATION.matcher(value);
        long seconds = 0;
        int end = 0;
        boolean found = false;
        while (matcher.find()) {
            if (!value.substring(end, matcher.start()).matches("[ ,]*(?:and[ ,]*)?")) return -1;
            long amount;
            try { amount = Long.parseLong(matcher.group(1)); } catch (NumberFormatException e) { return -1; }
            if (amount > Integer.MAX_VALUE) return -1;
            String unit = matcher.group(2);
            seconds += amount * (unit.startsWith("h") ? 3600L : unit.startsWith("m") ? 60L : 1L);
            if (seconds > Integer.MAX_VALUE) return -1;
            found = true;
            end = matcher.end();
        }
        return found && value.substring(end).matches("[ .!]*") && seconds > 0 ? (int) seconds : -1;
    }

    /** Hour and minute only when unambiguous. Other wording is left to Clock's editor. */
    public static int[] alarmTime(String input) {
        String value = input.toLowerCase(Locale.US).replace("a.m.", "am").replace("p.m.", "pm");
        Matcher m = Pattern.compile("^(?:(?:set|create|open)(?: an| the| my)? )?alarm (?:for|at) (\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?[.!]?$", Pattern.CASE_INSENSITIVE).matcher(value);
        if (!m.matches()) return null;
        int hour = Integer.parseInt(m.group(1));
        int minute = m.group(2) == null ? 0 : Integer.parseInt(m.group(2));
        if (minute > 59) return null;
        String period = m.group(3);
        if (period != null) {
            if (hour < 1 || hour > 12) return null;
            hour = hour % 12 + (period.equals("pm") ? 12 : 0);
        } else if (hour > 23 || m.group(2) == null) return null;
        return new int[]{hour, minute};
    }
}
