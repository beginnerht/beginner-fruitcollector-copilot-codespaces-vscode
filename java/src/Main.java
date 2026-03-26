import java.util.Scanner;

/**
 * Main.java
 *
 * Entry point for the Fruit Collector Game (Java terminal version).
 *
 * This class is intentionally kept small – its only responsibility is to:
 *   1. Show a welcome banner.
 *   2. Ask the player how long they want to play (30 seconds – 5 minutes).
 *   3. Create a FruitGame instance with the chosen duration.
 *   4. Start the game.
 *
 * Educational note:
 *   Keeping main() thin is a best practice in Java.  The real logic lives in
 *   dedicated classes (FruitGame, GameTimer, etc.) where it can be tested and
 *   reused independently of the entry point.
 *
 * How to compile and run:
 *   cd java
 *   ./compile.sh      (or: javac -encoding UTF-8 -d out src/*.java)
 *   ./run.sh          (or: java -cp out Main)
 */
public class Main {

    // ─── ANSI colours (same constants as FruitGame for the welcome banner) ────
    private static final String RESET   = "\033[0m";
    private static final String BOLD    = "\033[1m";
    private static final String MAGENTA = "\033[35m";
    private static final String CYAN    = "\033[36m";
    private static final String YELLOW  = "\033[33m";

    /**
     * Program entry point.
     *
     * @param args Command-line arguments (not used – duration is selected
     *             interactively).
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // ── Welcome banner ───────────────────────────────────────────────────
        System.out.print("\033[H\033[2J");  // clear screen
        System.out.flush();

        System.out.println(BOLD + MAGENTA
                + "╔══════════════════════════════════════════════╗");
        System.out.println(  "║     🍓  WELCOME TO FRUIT COLLECTOR!  🍎      ║");
        System.out.println(  "║                                              ║");
        System.out.println(  "║  Collect the demanded fruits before          ║");
        System.out.println(  "║  time runs out.  Rare fruits = more points!  ║");
        System.out.println(  "╚══════════════════════════════════════════════╝"
                + RESET);
        System.out.println();

        // ── Duration selection ───────────────────────────────────────────────
        System.out.println(BOLD + CYAN + "  Choose game duration:" + RESET);
        System.out.println("    1)  30 seconds  (Quick round)");
        System.out.println("    2)  60 seconds  (Standard)");
        System.out.println("    3)  90 seconds  (Extended)");
        System.out.println("    4) 120 seconds  (2 minutes)");
        System.out.println("    5) 180 seconds  (3 minutes)");
        System.out.println("    6) 300 seconds  (5 minutes – Marathon!)");
        System.out.println();
        System.out.print(BOLD + "  Your choice (1–6): " + RESET);

        int durationSeconds = parseDurationChoice(scanner);

        System.out.println();
        System.out.println(BOLD + YELLOW
                + "  ⏱  Game duration set to "
                + formatDuration(durationSeconds) + "." + RESET);
        System.out.println();

        // ── Launch game ──────────────────────────────────────────────────────
        // Pass the same scanner so FruitGame doesn't create a second one
        // on System.in (two Scanners on the same stream lose buffered input).
        FruitGame game = new FruitGame(durationSeconds, scanner);
        game.start();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Reads the player's duration choice and maps it to seconds.
     * Keeps prompting until a valid option is entered.
     *
     * @param scanner Scanner connected to System.in.
     * @return Game duration in seconds.
     */
    private static int parseDurationChoice(Scanner scanner) {
        // Duration options mapped to seconds
        int[] durations = {30, 60, 90, 120, 180, 300};

        while (true) {
            String input = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= durations.length) {
                    return durations[choice - 1];
                }
            } catch (NumberFormatException e) {
                // Fall through to the error message below
            }
            System.out.print("\033[31mInvalid choice. Please enter 1–"
                    + durations.length + ": \033[0m");
        }
    }

    /**
     * Returns a human-friendly duration string.
     * Example: 90  →  "1 min 30 sec"
     *          60  →  "1 minute"
     *          30  →  "30 seconds"
     *
     * @param seconds Total seconds.
     * @return Formatted string.
     */
    private static String formatDuration(int seconds) {
        if (seconds < 60) {
            return seconds + " seconds";
        } else if (seconds % 60 == 0) {
            int mins = seconds / 60;
            return mins + (mins == 1 ? " minute" : " minutes");
        } else {
            return (seconds / 60) + " min " + (seconds % 60) + " sec";
        }
    }
}
