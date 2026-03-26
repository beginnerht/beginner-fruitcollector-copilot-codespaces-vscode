import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * FruitGame.java
 *
 * The heart of the Fruit Collector Game.
 *
 * This class manages:
 *   - The full catalogue of fruits (all rarities).
 *   - The "market" – a randomly-selected set of 6 fruits shown each round.
 *   - The current demand (which fruit to collect, and how many).
 *   - Score tracking and demand completion counting.
 *   - The terminal-based UI rendered with ANSI colours and emoji.
 *   - The main game loop that ties everything together.
 *
 * Educational highlights:
 *   - ArrayList + Collections.shuffle() for random market generation.
 *   - ANSI escape codes for coloured terminal output.
 *   - Separation of concerns: display logic vs. game logic vs. input parsing.
 *   - Thread interaction: reading from a background GameTimer thread.
 */
public class FruitGame {

    // ─── ANSI colour codes (terminal colouring) ────────────────────────────────
    // "\033[" starts an ANSI escape sequence; "m" ends it.
    private static final String RESET   = "\033[0m";
    private static final String BOLD    = "\033[1m";
    private static final String RED     = "\033[31m";
    private static final String GREEN   = "\033[32m";
    private static final String YELLOW  = "\033[33m";
    private static final String BLUE    = "\033[34m";
    private static final String MAGENTA = "\033[35m";
    private static final String CYAN    = "\033[36m";
    private static final String WHITE   = "\033[37m";

    // ─── Game constants ────────────────────────────────────────────────────────

    /** Number of fruit options shown to the player each round. */
    private static final int MARKET_SIZE = 6;

    /**
     * Probability weights: common fruits appear 5× more often than legendary.
     * The list is iterated in reverse when sampling rare fruits.
     */
    private static final int[] RARITY_WEIGHTS = {50, 30, 15, 5}; // C U R L

    // ─── All available fruits ──────────────────────────────────────────────────

    /** The complete catalogue of fruits the game can ever show. */
    private final List<Fruit> allFruits;

    // ─── Per-game state ────────────────────────────────────────────────────────

    /** Countdown timer running on its own thread. */
    private final GameTimer timer;

    /** Scanner wrapping System.in for reading player input. */
    private final Scanner scanner;

    /** Accumulated score for the current game. */
    private int score;

    /** How many demands have been fully completed. */
    private int demandsCompleted;

    /** The demand the player is currently working towards. */
    private FruitDemand currentDemand;

    /**
     * The 6 fruits currently shown in the market.
     * Regenerated whenever a demand is completed.
     */
    private List<Fruit> market;

    /** Last feedback message shown below the market (correct / wrong hint). */
    private String lastFeedback;

    /**
     * Set to {@code true} when the player explicitly types 'q'.
     * Checked alongside the timer so the game loop exits immediately,
     * without waiting for the background timer thread to also finish.
     */
    private boolean playerQuit;

    /** Random number generator used throughout the game. */
    private final Random random;

    // ─── Constructor ───────────────────────────────────────────────────────────

    /**
     * Builds a new game session.
     *
     * @param gameDurationSeconds How long the game lasts (30 – 300 seconds).
     * @param scanner             The shared Scanner connected to System.in.
     *                            Passed in from Main to avoid creating a second
     *                            Scanner on the same stream (which causes input
     *                            to be lost due to independent buffering).
     */
    public FruitGame(int gameDurationSeconds, Scanner scanner) {
        this.allFruits        = buildFruitCatalogue();
        this.timer            = new GameTimer(gameDurationSeconds);
        this.scanner          = scanner;
        this.score            = 0;
        this.demandsCompleted = 0;
        this.lastFeedback     = "";
        this.playerQuit       = false;
        this.random           = new Random();
    }

    // ─── Fruit catalogue ───────────────────────────────────────────────────────

    /**
     * Defines every fruit available in the game.
     *
     * Design tip:
     *   In a larger project you might load these from a JSON or CSV file.
     *   Keeping them here makes the code self-contained and easy to read.
     *
     * @return Ordered list: common → uncommon → rare → legendary.
     */
    private List<Fruit> buildFruitCatalogue() {
        List<Fruit> fruits = new ArrayList<>();

        // ── Common fruits (⭐ 1 point each) ─────────────────────────────────
        fruits.add(new Fruit("Apple",       "🍎", FruitRarity.COMMON));
        fruits.add(new Fruit("Orange",      "🍊", FruitRarity.COMMON));
        fruits.add(new Fruit("Lemon",       "🍋", FruitRarity.COMMON));
        fruits.add(new Fruit("Grapes",      "🍇", FruitRarity.COMMON));
        fruits.add(new Fruit("Strawberry",  "🍓", FruitRarity.COMMON));
        fruits.add(new Fruit("Banana",      "🍌", FruitRarity.COMMON));

        // ── Uncommon fruits (🟢 3 points each) ──────────────────────────────
        fruits.add(new Fruit("Mango",       "🥭", FruitRarity.UNCOMMON));
        fruits.add(new Fruit("Pineapple",   "🍍", FruitRarity.UNCOMMON));
        fruits.add(new Fruit("Peach",       "🍑", FruitRarity.UNCOMMON));
        fruits.add(new Fruit("Cherry",      "🍒", FruitRarity.UNCOMMON));
        fruits.add(new Fruit("Blueberry",   "🫐", FruitRarity.UNCOMMON));
        fruits.add(new Fruit("Watermelon",  "🍉", FruitRarity.UNCOMMON));

        // ── Rare fruits (🔵 5 points each) ──────────────────────────────────
        fruits.add(new Fruit("Kiwi",        "🥝", FruitRarity.RARE));
        fruits.add(new Fruit("Melon",       "🍈", FruitRarity.RARE));
        fruits.add(new Fruit("Coconut",     "🥥", FruitRarity.RARE));
        fruits.add(new Fruit("Pear",        "🍐", FruitRarity.RARE));

        // ── Legendary fruits (💎 10 points each) ────────────────────────────
        // These are fictional / very exotic varieties — catch them if you can!
        fruits.add(new Fruit("Dragon Fruit",  "🐲", FruitRarity.LEGENDARY));
        fruits.add(new Fruit("Star Fruit",    "⭐", FruitRarity.LEGENDARY));
        fruits.add(new Fruit("Golden Mango",  "🏆", FruitRarity.LEGENDARY));
        fruits.add(new Fruit("Rainbow Berry", "🌈", FruitRarity.LEGENDARY));

        return fruits;
    }

    // ─── Public API ────────────────────────────────────────────────────────────

    /**
     * Starts the game:
     *   1. Shows the "how to play" screen.
     *   2. Launches the background timer thread.
     *   3. Runs the main game loop.
     *   4. Displays the final results.
     */
    public void start() {
        showHowToPlay();

        // Start the timer on a daemon thread.
        // A daemon thread is killed automatically when the JVM exits,
        // so we don't need to worry about it keeping the process alive.
        Thread timerThread = new Thread(timer);
        timerThread.setDaemon(true);
        timerThread.start();

        // Generate the first demand and market before entering the loop
        generateNewRound();

        // ── Main game loop ───────────────────────────────────────────────────
        // The loop exits when either the countdown timer finishes OR the player
        // explicitly quits with 'q'.  Checking playerQuit is necessary because
        // timer.stop() signals the timer thread to stop but isFinished() only
        // becomes true after that thread has fully exited its run() method.
        while (!timer.isFinished() && !playerQuit) {
            displayGame();                              // draw the UI
            String input = scanner.nextLine().trim();   // read player input
            if (timer.isFinished() || playerQuit) break; // time may have expired
            processInput(input);                        // handle selection
        }

        // Timer has run out (or player typed 'q')
        timer.stop();
        displayGameOver();
    }

    // ─── Round generation ──────────────────────────────────────────────────────

    /**
     * Picks a random demand and builds a fresh 6-fruit market that always
     * contains enough copies of the demanded fruit to let the player complete it.
     *
     * Rare fruits appear as demands less often, but reward more points.
     */
    private void generateNewRound() {
        // 1. Pick a demand fruit using weighted random selection
        Fruit demandedFruit = pickWeightedRandomFruit();

        // 2. Determine quantity based on rarity (rarer → fewer needed)
        int qty;
        switch (demandedFruit.getRarity()) {
            case LEGENDARY: qty = 1;                         break;
            case RARE:      qty = 1;                         break;
            case UNCOMMON:  qty = random.nextInt(2) + 1;     break;  // 1 or 2
            default:        qty = random.nextInt(2) + 1;     break;  // 1 or 2
        }

        currentDemand = new FruitDemand(demandedFruit, qty);

        // 3. Build the market
        buildMarket(demandedFruit, qty);
    }

    /**
     * Selects a fruit at random, giving common fruits a higher chance of
     * appearing than rare ones (so legendary demands feel special).
     *
     * @return A randomly chosen fruit from the catalogue.
     */
    private Fruit pickWeightedRandomFruit() {
        // Build a weighted pool by repeating each fruit proportionally
        List<Fruit> pool = new ArrayList<>();
        for (Fruit f : allFruits) {
            int weight = rarityWeight(f.getRarity());
            for (int i = 0; i < weight; i++) {
                pool.add(f);
            }
        }
        return pool.get(random.nextInt(pool.size()));
    }

    /**
     * Returns a sampling weight for a given rarity tier.
     *
     * @param rarity The rarity to look up.
     * @return Relative weight (higher → appears more often).
     */
    private int rarityWeight(FruitRarity rarity) {
        switch (rarity) {
            case COMMON:    return RARITY_WEIGHTS[0];
            case UNCOMMON:  return RARITY_WEIGHTS[1];
            case RARE:      return RARITY_WEIGHTS[2];
            case LEGENDARY: return RARITY_WEIGHTS[3];
            default:        return 1;
        }
    }

    /**
     * Fills the market with exactly {@code qty} copies of the demanded fruit
     * plus enough random other fruits to reach {@link #MARKET_SIZE}.
     *
     * @param demanded The fruit the player must find.
     * @param qty      How many copies of it to include.
     */
    private void buildMarket(Fruit demanded, int qty) {
        market = new ArrayList<>();

        // Add the required copies of the demanded fruit
        for (int i = 0; i < qty; i++) {
            market.add(demanded);
        }

        // Fill remaining slots with random different fruits
        List<Fruit> otherFruits = new ArrayList<>(allFruits);
        otherFruits.remove(demanded);                       // remove demanded fruit
        Collections.shuffle(otherFruits, random);

        int needed = MARKET_SIZE - qty;
        for (int i = 0; i < needed && i < otherFruits.size(); i++) {
            market.add(otherFruits.get(i));
        }

        // Shuffle so the demanded fruit isn't always first
        Collections.shuffle(market, random);
    }

    // ─── Input handling ────────────────────────────────────────────────────────

    /**
     * Handles one line of player input.
     *
     * Valid inputs:
     *   "1" – "6"  →  attempt to collect the fruit at that market position.
     *   "q" / "quit"  →  stop the timer and end the game immediately.
     *   Anything else  →  show a hint to use a valid number.
     *
     * @param input Raw string from the player.
     */
    private void processInput(String input) {
        if (input.equalsIgnoreCase("q") || input.equalsIgnoreCase("quit")) {
            // Signal both the timer thread and the local quit flag so the
            // game loop exits cleanly even before the timer thread finishes.
            timer.stop();
            playerQuit = true;
            return;
        }

        int choice;
        try {
            choice = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            lastFeedback = RED + "⚠  Please enter a number between 1 and "
                    + MARKET_SIZE + " (or 'q' to quit)." + RESET;
            return;
        }

        if (choice < 1 || choice > MARKET_SIZE) {
            lastFeedback = RED + "⚠  Out of range! Enter 1–" + MARKET_SIZE + "." + RESET;
            return;
        }

        // choice is 1-based; list is 0-based
        Fruit chosen = market.get(choice - 1);

        if (chosen.equals(currentDemand.getFruit())) {
            // ── Correct collection ───────────────────────────────────────────
            currentDemand.collect();
            int pts = chosen.getPointValue();
            score += pts;

            if (currentDemand.isFulfilled()) {
                // Demand completed – celebrate and move on
                demandsCompleted++;
                lastFeedback = GREEN + BOLD
                        + "✅  Demand complete!  +" + pts + " pts!  🎉"
                        + RESET;
                generateNewRound();     // fresh market + new demand
            } else {
                lastFeedback = GREEN + "✅  Good catch!  +" + pts + " pts!"
                        + "  Keep going…" + RESET;
            }
        } else {
            // ── Wrong fruit ──────────────────────────────────────────────────
            // Lose 1 point (minimum 0) as a gentle penalty
            score = Math.max(0, score - 1);
            lastFeedback = RED
                    + "❌  That's a " + chosen.getName() + "!  -1 pt.  "
                    + "Hint: look for " + currentDemand.getFruit().getEmoji()
                    + RESET;
        }
    }

    // ─── Terminal UI ───────────────────────────────────────────────────────────

    /**
     * Clears the terminal and redraws the entire game screen.
     *
     * Uses ANSI escape codes:
     *   "\033[H\033[2J"  →  move cursor to home position, then clear screen.
     *
     * This gives the effect of an updating UI without an external library.
     */
    private void displayGame() {
        // Clear screen
        System.out.print("\033[H\033[2J");
        System.out.flush();

        int timeLeft = timer.getSecondsRemaining();
        String timeColour = timeLeft <= 10 ? RED : (timeLeft <= 30 ? YELLOW : CYAN);

        // ── Header bar ───────────────────────────────────────────────────────
        System.out.println(BOLD + MAGENTA
                + "╔══════════════════════════════════════════════╗");
        System.out.println(  "║       🍓  FRUIT  COLLECTOR  GAME  🍎        ║");
        System.out.println(  "╚══════════════════════════════════════════════╝"
                + RESET);

        // ── Status bar (timer + score) ───────────────────────────────────────
        System.out.printf("%s⏱  Time: %s%s%s   │   💎 Score: %s%d%s   │   ✅ Done: %d%s%n",
                BOLD,
                timeColour, timer.formatTime(), RESET,
                YELLOW, score, RESET,
                demandsCompleted,
                RESET);
        System.out.println(BOLD + "──────────────────────────────────────────────" + RESET);

        // ── Demand panel ─────────────────────────────────────────────────────
        System.out.println(BOLD + BLUE + "📋  CURRENT DEMAND:" + RESET);
        System.out.println("    " + currentDemand);
        System.out.println();

        // ── Market panel ─────────────────────────────────────────────────────
        System.out.println(BOLD + CYAN + "🛒  MARKET (choose a fruit):" + RESET);
        for (int i = 0; i < market.size(); i++) {
            Fruit f = market.get(i);

            // Colour the number based on rarity to add visual flair
            String numColour = rarityColour(f.getRarity());
            System.out.printf("  %s%d)%s  %s%n",
                    numColour + BOLD, i + 1, RESET, f);
        }
        System.out.println();

        // ── Feedback from last move ───────────────────────────────────────────
        if (!lastFeedback.isEmpty()) {
            System.out.println("  " + lastFeedback);
            System.out.println();
        }

        // ── Prompt ───────────────────────────────────────────────────────────
        System.out.print(BOLD + "  Enter number (1–" + MARKET_SIZE
                + ") or 'q' to quit: " + RESET);
        System.out.flush();
    }

    /**
     * Shows the welcome / how-to-play screen before the game starts.
     * Waits for the player to press Enter so they have time to read it.
     */
    private void showHowToPlay() {
        System.out.print("\033[H\033[2J");
        System.out.flush();

        System.out.println(BOLD + MAGENTA
                + "╔══════════════════════════════════════════════╗");
        System.out.println(  "║       🍓  FRUIT  COLLECTOR  GAME  🍎        ║");
        System.out.println(  "╠══════════════════════════════════════════════╣");
        System.out.println(  "║  HOW TO PLAY                                 ║");
        System.out.println(  "║                                              ║");
        System.out.println(  "║  1. A DEMAND is shown (e.g. 'Collect 🍎 x2') ║");
        System.out.println(  "║  2. Six fruits appear in the MARKET.         ║");
        System.out.println(  "║  3. Type the NUMBER of the demanded fruit.   ║");
        System.out.println(  "║  4. Correct pick → earn points!              ║");
        System.out.println(  "║  5. Wrong pick   → lose 1 point.             ║");
        System.out.println(  "║  6. Collect all demanded fruits → new round! ║");
        System.out.println(  "║                                              ║");
        System.out.println(  "║  RARITY GUIDE                                ║");
        System.out.println(  "║  ⭐ Common    →  1 pt                        ║");
        System.out.println(  "║  🟢 Uncommon  →  3 pts                       ║");
        System.out.println(  "║  🔵 Rare      →  5 pts                       ║");
        System.out.println(  "║  💎 Legendary → 10 pts  (ultra rare!)        ║");
        System.out.println(  "║                                              ║");
        System.out.println(  "║  Type 'q' at any time to quit early.         ║");
        System.out.println(  "╚══════════════════════════════════════════════╝"
                + RESET);
        System.out.println();
        System.out.print(BOLD + "  Press ENTER to start… " + RESET);
        scanner.nextLine();     // wait for the player
    }

    /**
     * Renders the "game over" screen with the final score and a rating.
     */
    private void displayGameOver() {
        System.out.print("\033[H\033[2J");
        System.out.flush();

        System.out.println(BOLD + RED
                + "╔══════════════════════════════════════════════╗");
        System.out.println(  "║             ⏱  TIME'S UP!  ⏱               ║");
        System.out.println(  "╚══════════════════════════════════════════════╝"
                + RESET);
        System.out.println();
        System.out.println(BOLD + "  Final Score:        " + YELLOW + score   + RESET);
        System.out.println(BOLD + "  Demands Completed:  " + GREEN + demandsCompleted + RESET);
        System.out.println();

        // Give the player a rating based on their score
        System.out.print(BOLD + "  Rating: ");
        if (score >= 100) {
            System.out.println(YELLOW + "🏆 LEGENDARY COLLECTOR!" + RESET);
        } else if (score >= 60) {
            System.out.println(BLUE + "🔵 Expert Harvester"     + RESET);
        } else if (score >= 30) {
            System.out.println(GREEN + "🟢 Skilled Picker"       + RESET);
        } else if (score >= 10) {
            System.out.println(CYAN  + "⭐ Budding Collector"    + RESET);
        } else {
            System.out.println(WHITE + "🌱 Keep Practising!"     + RESET);
        }

        System.out.println();
        System.out.println(BOLD + MAGENTA + "  Thanks for playing Fruit Collector! 🍓" + RESET);
        System.out.println();
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Maps a rarity tier to an ANSI colour code for display.
     *
     * @param rarity The rarity to colour.
     * @return ANSI colour string.
     */
    private String rarityColour(FruitRarity rarity) {
        switch (rarity) {
            case LEGENDARY: return YELLOW;
            case RARE:      return BLUE;
            case UNCOMMON:  return GREEN;
            default:        return WHITE;
        }
    }
}
