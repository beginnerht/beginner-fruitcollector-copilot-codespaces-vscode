# 🍓 Fruit Collector Game

A fruit-collecting game in **two implementations** — a **Java terminal version** and a **JavaScript web version**.

Collect the demanded fruits before time runs out!  Rare fruits are worth more points — but they appear far less often.

---

## 🎮 Game Rules

| Step | Action |
|------|--------|
| 1 | A **demand** is shown — e.g. "Collect 🍎 Apple × 2" |
| 2 | Six fruits appear in the **market** |
| 3 | Select / click the fruit that matches the demand |
| 4 | Correct → earn points.  Wrong → lose 1 point |
| 5 | Fulfil the demand → new demand + new market! |
| 6 | Game ends when the timer hits 0 |

### Rarity Table

| Rarity | Symbol | Points | Chance |
|--------|--------|--------|--------|
| Common | ⭐ | 1 | Very often |
| Uncommon | 🟢 | 3 | Moderate |
| Rare | 🔵 | 5 | Uncommon |
| Legendary | 💎 | 10 | Ultra rare! |

---

## ☕ Java Version (Terminal)

### Prerequisites
- Java 11 or newer (OpenJDK or any distribution)

### Directory structure

```
java/
  src/
    FruitRarity.java   ← enum: COMMON, UNCOMMON, RARE, LEGENDARY
    Fruit.java         ← fruit entity (name, emoji, rarity, points)
    FruitDemand.java   ← demand object (which fruit, how many)
    GameTimer.java     ← countdown timer (runs on its own thread)
    FruitGame.java     ← core game loop + terminal UI (ANSI + emoji)
    Main.java          ← entry point, duration selector (30 s – 5 min)
  compile.sh           ← builds all .java → out/*.class
  run.sh               ← compiles if needed, then launches the game
```

### How to run

```bash
cd java
chmod +x compile.sh run.sh   # first time only
./run.sh
```

Or manually:

```bash
cd java
javac -encoding UTF-8 -d out src/*.java
java -cp out -Dfile.encoding=UTF-8 Main
```

The game runs entirely in your terminal with coloured output and emoji graphics.

---

## 🌐 JavaScript Web Version

### Prerequisites
None — just a modern browser.

### How to run

1. Open `web/index.html` directly in your browser, **or**
2. Serve it locally for best results:

```bash
# Python 3
cd web && python3 -m http.server 8080
# then open http://localhost:8080

# Node.js (npx)
cd web && npx serve .
```

### Directory structure

```
web/
  index.html   ← page structure (HTML)
  style.css    ← appearance: dark theme, animations, rarity colours
  game.js      ← all game logic (market, timer, scoring, DOM rendering)
```

---

## 💡 Educational Notes

This repository is designed to be **readable and well-commented** for learners.

- **Java version** demonstrates: enums with fields, immutable value objects (POJOs), Runnable + Thread for background timers, AtomicInteger for thread-safe state, ArrayList + Collections.shuffle(), ANSI escape codes for terminal UI.
- **JavaScript version** demonstrates: const/let, object literals, Array methods (forEach, filter, map), closures, DOM manipulation, setInterval for timers, CSS animations triggered from JS.

Both versions share the same game design so you can compare how the same idea is expressed in two very different languages.
