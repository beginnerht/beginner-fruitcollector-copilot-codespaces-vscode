/**
 * FruitRarity.java
 *
 * Defines the rarity tiers for every fruit in the Fruit Collector Game.
 *
 * Educational note:
 *   Java enums can carry fields and methods just like regular classes.
 *   Each constant (COMMON, UNCOMMON, RARE, LEGENDARY) is an instance of
 *   this enum, and each instance stores its own pointValue, displayName,
 *   and visual symbol.
 *
 * Game design note:
 *   Rarer fruits are worth more points but appear less frequently in the
 *   market, creating an exciting risk/reward dynamic.
 */
public enum FruitRarity {

    // ─── Rarity tiers ──────────────────────────────────────────────────────────
    COMMON    (1,  "Common",    "⭐"),   // Everyday fruits, easy to find
    UNCOMMON  (3,  "Uncommon",  "🟢"),   // A bit harder to spot
    RARE      (5,  "Rare",      "🔵"),   // Lucky find!
    LEGENDARY (10, "Legendary", "💎");   // Ultra-rare — worth the hunt!

    // ─── Fields ────────────────────────────────────────────────────────────────

    /** Points awarded when the player collects a fruit of this rarity. */
    private final int pointValue;

    /** Human-readable name shown in the UI. */
    private final String displayName;

    /** Coloured symbol that appears next to the rarity label. */
    private final String symbol;

    // ─── Constructor ───────────────────────────────────────────────────────────

    /**
     * Enum constructors are always private.
     * Java calls this automatically for each constant listed above.
     *
     * @param pointValue  Points this rarity is worth when collected.
     * @param displayName Pretty name shown to the player.
     * @param symbol      Emoji/symbol that represents the rarity visually.
     */
    FruitRarity(int pointValue, String displayName, String symbol) {
        this.pointValue  = pointValue;
        this.displayName = displayName;
        this.symbol      = symbol;
    }

    // ─── Getters ───────────────────────────────────────────────────────────────

    /** @return Points awarded for collecting a fruit of this rarity. */
    public int getPointValue() { return pointValue; }

    /** @return Pretty display name (e.g. "Legendary"). */
    public String getDisplayName() { return displayName; }

    /** @return Visual symbol (e.g. "💎"). */
    public String getSymbol() { return symbol; }

    /**
     * Returns a short, nicely-formatted label used in the market list.
     * Example: "💎 Legendary"
     */
    @Override
    public String toString() {
        return symbol + " " + displayName;
    }
}
