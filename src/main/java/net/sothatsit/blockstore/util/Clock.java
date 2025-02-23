package net.sothatsit.blockstore.util;

import java.text.DecimalFormat;

/**
 * Measures elapsed time as a sequence of measurement sections and reports
 * elapsed time for the current section, or for the sum of all sections if the
 * timer has stopped.
 */
public final class Clock {
    /**
     * Formats elapsed time with two digits after the decimal point.
     */
    private static final DecimalFormat millisecondsFormat = new DecimalFormat("#.##");

    /**
     * Nanoseconds timestamp when the timer started (object was created).
     */
    private final long overallStart;

    /**
     * Nanoseconds timestamp when {@link #nextSection()} was last called, or the
     * overallStart time if not yet called.
     */
    private long sectionStart;

    /**
     * Nanoseconds timestamp when {@link #stop()} was called, or -1 if still
     * measuring time.
     */
    private long end;

    /**
     * Constructor.
     *
     * Starts counting elapsed time.
     */
    private Clock() {
        this.overallStart = System.nanoTime();
        this.sectionStart = overallStart;
        this.end = -1;
    }

    /**
     * Retunr true if {@link #stop()} has been called.
     *
     * @return true if {@link #stop()} has been called.
     */
    public boolean hasEnded() {
        return end >= 0;
    }

    /**
     * Stop measuring time and return the elapsed time in milliseconds as a
     * string.
     *
     * @return the elapsed time in milliseconds as a string.
     */
    public String stop() {
        Checks.ensureTrue(!hasEnded(), "Timer has already been stopped.");

        this.end = System.nanoTime();

        return toString();
    }

    /**
     * Start the next timing section.
     *
     * This sets a new start time reference from which {@link #getDuration()}
     * will measure elapsed time.
     *
     * @return the elapsed time of the previous section as a string in
     *         milliseconds.
     */
    public String nextSection() {
        String timing = toString();

        this.sectionStart = System.nanoTime();

        return timing;
    }

    /**
     * Return the elapsed time in milliseconds of the current section, or the
     * total elapsed time of all sections if the timer has stopped.
     *
     * @return the elapsed time in milliseconds of the current section, or the
     *         total elapsed time of all sections if the timer has stopped.
     */
    public double getDuration() {
        return (hasEnded() ? end - overallStart : System.nanoTime() - sectionStart) / 1e6;
    }

    /**
     * Return the value of {@link #getDuration()}, formatted as a string.
     *
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "(" + millisecondsFormat.format(getDuration()) + " ms)";
    }

    /**
     * Return a new Clock, started now.
     *
     * @return a new Clock, started now.
     */
    public static Clock start() {
        return new Clock();
    }

}
