/**
 * FruitDemand.java
 *
 * Represents one active demand in the Fruit Collector Game.
 *
 * Educational note:
 *   This is a simple "state-holding" object.  It stores WHAT fruit is needed
 *   and HOW MANY, then lets other code call collect() to record each successful
 *   collection.  This keeps all demand-tracking logic in one place instead of
 *   scattering it across the game class.
 *
 * Example demand:
 *   "Collect 🍎 Apple × 2  (0 / 2 collected)"
 */
public class FruitDemand {

    // ─── Fields ────────────────────────────────────────────────────────────────

    /** The fruit that must be collected to satisfy this demand. */
    private final Fruit fruit;

    /** Total number of this fruit the player must collect. */
    private final int quantity;

    /** Running count of how many have been collected so far. */
    private int collected;

    // ─── Constructor ───────────────────────────────────────────────────────────

    /**
     * Creates a new demand.
     *
     * @param fruit    The required fruit.
     * @param quantity How many the player must collect (≥ 1).
     */
    public FruitDemand(Fruit fruit, int quantity) {
        this.fruit     = fruit;
        this.quantity  = quantity;
        this.collected = 0;           // always starts at zero
    }

    // ─── Game logic ────────────────────────────────────────────────────────────

    /**
     * Records one successful collection of this demand's fruit.
     *
     * @return {@code true} if the collection counted (demand not yet fulfilled),
     *         {@code false} if the demand was already complete.
     */
    public boolean collect() {
        if (collected < quantity) {
            collected++;
            return true;
        }
        return false;   // demand was already done – shouldn't normally happen
    }

    /**
     * @return {@code true} once all required fruits have been collected.
     */
    public boolean isFulfilled() {
        return collected >= quantity;
    }

    // ─── Getters ───────────────────────────────────────────────────────────────

    /** @return The fruit associated with this demand. */
    public Fruit getFruit() { return fruit; }

    /** @return Total quantity required. */
    public int getQuantity() { return quantity; }

    /** @return How many have been collected so far. */
    public int getCollected() { return collected; }

    /**
     * Builds a progress bar like:  ██░░░  (2 / 5)
     * Makes it immediately clear how close the player is to completing demand.
     *
     * @return A formatted progress string.
     */
    public String getProgressBar() {
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < quantity; i++) {
            bar.append(i < collected ? "█" : "░");
        }
        return bar + "  (" + collected + "/" + quantity + ")";
    }

    // ─── Object overrides ──────────────────────────────────────────────────────

    /**
     * One-line summary shown in the demand panel.
     * Example: "Collect 🍊 Orange × 2  (0/2)"
     */
    @Override
    public String toString() {
        return String.format("Collect %s %s  ×%d  %s",
                fruit.getEmoji(), fruit.getName(), quantity, getProgressBar());
    }
}
