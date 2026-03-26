import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GameTimer.java
 *
 * A countdown timer that runs in its own background thread.
 *
 * Educational notes:
 *
 *   1. Implements Runnable – the standard Java way to define code that runs
 *      on a separate thread.  You pass an instance of this class to a Thread,
 *      then call Thread.start().
 *
 *   2. AtomicInteger / AtomicBoolean – "atomic" means the read+modify+write
 *      operation happens without any other thread being able to interrupt it
 *      in the middle.  This prevents data races when the main thread reads
 *      secondsRemaining while this thread is decrementing it.
 *
 *   3. Thread.sleep(1_000) pauses this thread for one second without blocking
 *      any other thread, making it ideal for timers.
 *
 *   4. Thread.currentThread().interrupt() – best practice: when an
 *      InterruptedException is caught, re-set the interrupted flag so callers
 *      higher up the stack know the thread was interrupted.
 */
public class GameTimer implements Runnable {

    // ─── Fields ────────────────────────────────────────────────────────────────

    /** Initial duration in seconds (stored for reference / display). */
    private final int totalSeconds;

    /**
     * Current seconds remaining.
     * AtomicInteger makes decrement + read thread-safe without synchronized.
     */
    private final AtomicInteger secondsRemaining;

    /** {@code true} while the timer is actively counting down. */
    private final AtomicBoolean running;

    /** {@code true} once the countdown reaches zero (or is stopped). */
    private final AtomicBoolean finished;

    // ─── Constructor ───────────────────────────────────────────────────────────

    /**
     * Creates a new timer for the given duration.
     * The timer does NOT start until you call {@code new Thread(timer).start()}.
     *
     * @param totalSeconds Game duration in seconds (e.g. 60, 120, 300).
     */
    public GameTimer(int totalSeconds) {
        this.totalSeconds      = totalSeconds;
        this.secondsRemaining  = new AtomicInteger(totalSeconds);
        this.running           = new AtomicBoolean(false);
        this.finished          = new AtomicBoolean(false);
    }

    // ─── Runnable implementation ───────────────────────────────────────────────

    /**
     * Called by the Thread when it starts.
     * Counts down once per second until it hits zero or stop() is called.
     */
    @Override
    public void run() {
        running.set(true);

        // Keep ticking as long as time remains and we haven't been stopped
        while (secondsRemaining.get() > 0 && running.get()) {
            try {
                Thread.sleep(1_000);            // wait one second
                secondsRemaining.decrementAndGet(); // subtract one second
            } catch (InterruptedException e) {
                // Another thread interrupted us – honour it and exit cleanly
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Mark as finished so the game loop can detect "time's up"
        finished.set(true);
        running.set(false);
    }

    // ─── Control ───────────────────────────────────────────────────────────────

    /**
     * Signals the timer to stop before it reaches zero.
     * Useful when the player quits or the game ends early.
     */
    public void stop() {
        running.set(false);
    }

    // ─── Accessors ─────────────────────────────────────────────────────────────

    /** @return Total duration this timer was initialised with (seconds). */
    public int getTotalSeconds() { return totalSeconds; }

    /** @return Seconds left on the clock (0 once finished). */
    public int getSecondsRemaining() { return secondsRemaining.get(); }

    /** @return {@code true} if the countdown has reached zero. */
    public boolean isFinished() { return finished.get(); }

    /**
     * Formats the remaining time as  MM:SS  (e.g. "01:23").
     * Makes it easy to display a real-looking countdown in the UI.
     *
     * @return Formatted time string.
     */
    public String formatTime() {
        int secs = secondsRemaining.get();
        return String.format("%02d:%02d", secs / 60, secs % 60);
    }
}
