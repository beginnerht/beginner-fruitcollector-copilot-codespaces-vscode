/**
 * Fruit.java
 *
 * Represents a single type of fruit in the Fruit Collector Game.
 *
 * Educational note:
 *   This is a classic Java "value object" (also called a data class or POJO –
 *   Plain Old Java Object).  It stores data about one fruit and provides
 *   getter methods so other classes can read that data safely.
 *
 * Every fruit has:
 *   - A name       (e.g., "Apple")
 *   - An emoji     (e.g., "🍎")  – used for terminal/web graphics
 *   - A rarity     (COMMON … LEGENDARY)
 *
 * The point value is derived directly from the rarity, so there is no need
 * to store it separately.
 */
public class Fruit {

    // ─── Fields (all final → the object is immutable once created) ────────────

    /** Plain English name of the fruit. */
    private final String name;

    /**
     * Unicode emoji that represents this fruit visually.
     * These render as colourful pictures in most modern terminals and browsers.
     */
    private final String emoji;

    /** Rarity tier that controls how often this fruit appears and its value. */
    private final FruitRarity rarity;

    // ─── Constructor ───────────────────────────────────────────────────────────

    /**
     * Creates a new Fruit instance.
     *
     * @param name   Display name (e.g. "Strawberry").
     * @param emoji  Unicode emoji (e.g. "🍓").
     * @param rarity Rarity tier from the {@link FruitRarity} enum.
     */
    public Fruit(String name, String emoji, FruitRarity rarity) {
        this.name   = name;
        this.emoji  = emoji;
        this.rarity = rarity;
    }

    // ─── Getters ───────────────────────────────────────────────────────────────

    /** @return The fruit's display name (e.g. "Apple"). */
    public String getName() { return name; }

    /** @return The fruit's emoji character(s) (e.g. "🍎"). */
    public String getEmoji() { return emoji; }

    /** @return The rarity tier of this fruit. */
    public FruitRarity getRarity() { return rarity; }

    /**
     * Convenience method: how many points does collecting this fruit earn?
     * Delegates straight to the rarity's point value.
     *
     * @return Point value (1 for Common up to 10 for Legendary).
     */
    public int getPointValue() { return rarity.getPointValue(); }

    // ─── Object overrides ──────────────────────────────────────────────────────

    /**
     * Returns a human-readable summary used in the market list.
     * Example: "🍎 Apple  ⭐ Common"
     */
    @Override
    public String toString() {
        // Left-pad the name so the rarity column lines up neatly
        return String.format("%s %-14s %s", emoji, name, rarity);
    }

    /**
     * Two fruits are equal when they have the same name.
     * Used when checking whether a collected fruit matches the current demand.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Fruit)) return false;
        Fruit other = (Fruit) obj;
        return name.equals(other.name);
    }

    /** Must override hashCode whenever equals is overridden. */
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
