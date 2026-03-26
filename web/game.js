/**
 * game.js  –  Fruit Collector Web Game
 *
 * JavaScript version of the Fruit Collector Game.
 * Runs entirely in the browser – no server required.
 *
 * Structure (mirrors the Java classes):
 *   FRUIT_CATALOGUE  – array of fruit objects (like Fruit.java + FruitRarity.java)
 *   GameState        – plain object holding the live game variables
 *   Functions:
 *     buildMarket()     – picks 6 random fruits for the market (weighted)
 *     generateDemand()  – selects which fruit to demand
 *     startGame()       – kicks off a new game session
 *     tick()            – called every second to update the timer
 *     handleCardClick() – processes a player's fruit selection
 *     renderMarket()    – draws the 6 fruit cards in the DOM
 *     renderDemand()    – updates the demand panel
 *     renderStatus()    – updates timer, score, demand-count displays
 *     showGameOver()    – displays the end-of-game screen
 *     showStartScreen() – displays the initial menu
 *
 * Educational notes are sprinkled throughout.
 */

'use strict'; // Enforce stricter JavaScript parsing – catches common mistakes

// ─── Fruit Catalogue ─────────────────────────────────────────────────────────
/**
 * All available fruits.
 *
 * Each fruit is a plain JavaScript object (similar to a Java POJO) with:
 *   name     – display name
 *   emoji    – Unicode emoji used as the visual
 *   rarity   – string key that maps to RARITIES below
 *
 * Educational note:
 *   JavaScript objects are created with curly-brace literals { key: value }.
 *   Array.freeze() makes the array immutable so no code can accidentally add
 *   or remove fruits at runtime.
 */
const FRUIT_CATALOGUE = Object.freeze([
    // ── Common (⭐ 1 point) ───────────────────────────────────────────────────
    { name: 'Apple',       emoji: '🍎', rarity: 'COMMON'    },
    { name: 'Orange',      emoji: '🍊', rarity: 'COMMON'    },
    { name: 'Lemon',       emoji: '🍋', rarity: 'COMMON'    },
    { name: 'Grapes',      emoji: '🍇', rarity: 'COMMON'    },
    { name: 'Strawberry',  emoji: '🍓', rarity: 'COMMON'    },
    { name: 'Banana',      emoji: '🍌', rarity: 'COMMON'    },

    // ── Uncommon (🟢 3 points) ────────────────────────────────────────────────
    { name: 'Mango',       emoji: '🥭', rarity: 'UNCOMMON'  },
    { name: 'Pineapple',   emoji: '🍍', rarity: 'UNCOMMON'  },
    { name: 'Peach',       emoji: '🍑', rarity: 'UNCOMMON'  },
    { name: 'Cherry',      emoji: '🍒', rarity: 'UNCOMMON'  },
    { name: 'Blueberry',   emoji: '🫐', rarity: 'UNCOMMON'  },
    { name: 'Watermelon',  emoji: '🍉', rarity: 'UNCOMMON'  },

    // ── Rare (🔵 5 points) ────────────────────────────────────────────────────
    { name: 'Kiwi',        emoji: '🥝', rarity: 'RARE'      },
    { name: 'Melon',       emoji: '🍈', rarity: 'RARE'      },
    { name: 'Coconut',     emoji: '🥥', rarity: 'RARE'      },
    { name: 'Pear',        emoji: '🍐', rarity: 'RARE'      },

    // ── Legendary (💎 10 points) – ultra rare! ───────────────────────────────
    { name: 'Dragon Fruit',  emoji: '🐲', rarity: 'LEGENDARY' },
    { name: 'Star Fruit',    emoji: '⭐', rarity: 'LEGENDARY' },
    { name: 'Golden Mango',  emoji: '🏆', rarity: 'LEGENDARY' },
    { name: 'Rainbow Berry', emoji: '🌈', rarity: 'LEGENDARY' },
]);

/**
 * Rarity meta-data: points, display name, colour class, and sampling weight.
 *
 * Educational note:
 *   Storing config in a lookup object (keyed by the same string used in the
 *   catalogue) is a common pattern.  It avoids long switch statements and
 *   makes it easy to add new rarities later.
 */
const RARITIES = Object.freeze({
    COMMON:    { points: 1,  label: '⭐ Common',    weight: 50, cssClass: 'COMMON'    },
    UNCOMMON:  { points: 3,  label: '🟢 Uncommon',  weight: 30, cssClass: 'UNCOMMON'  },
    RARE:      { points: 5,  label: '🔵 Rare',      weight: 15, cssClass: 'RARE'      },
    LEGENDARY: { points: 10, label: '💎 Legendary', weight:  5, cssClass: 'LEGENDARY' },
});

const MARKET_SIZE = 6; // number of fruit cards shown per round

// ─── Game State ───────────────────────────────────────────────────────────────
/**
 * A single object holds all live game variables.
 *
 * Educational note:
 *   Grouping mutable state into one object makes it obvious what can change
 *   during a game session.  Compare with Java's class fields.
 */
let state = {
    score:            0,
    demandsCompleted: 0,
    timeRemaining:    60,   // overwritten by duration selection
    totalTime:        60,
    timerInterval:    null, // reference to the setInterval callback
    market:           [],   // current 6 fruits
    demandFruit:      null, // the fruit that must be collected
    demandQty:        1,    // how many must be collected
    demandCollected:  0,    // how many collected so far
    active:           false,
};

// ─── DOM references ───────────────────────────────────────────────────────────
// Grab all elements we'll update frequently.  Storing them avoids repeated
// document.getElementById() calls which are slightly slower.
const dom = {
    startScreen:    document.getElementById('start-screen'),
    gameScreen:     document.getElementById('game-screen'),
    gameOverScreen: document.getElementById('game-over-screen'),
    durationBtns:   document.querySelectorAll('.duration-btn'),
    startBtn:       document.getElementById('start-btn'),
    playAgainBtn:   document.getElementById('play-again-btn'),
    timerDisplay:   document.getElementById('timer-display'),
    timerBar:       document.getElementById('timer-bar'),
    scoreDisplay:   document.getElementById('score-display'),
    demandsDisplay: document.getElementById('demands-display'),
    demandText:     document.getElementById('demand-text'),
    demandProgress: document.getElementById('demand-progress'),
    demandPanel:    document.getElementById('demand-panel'),
    marketGrid:     document.getElementById('market-grid'),
    feedbackBar:    document.getElementById('feedback-bar'),
    finalScore:     document.getElementById('final-score'),
    finalDemands:   document.getElementById('final-demands'),
    finalRating:    document.getElementById('final-rating'),
};

// Selected game duration (default 60 seconds)
let selectedDuration = 60;

// ─── Utility functions ────────────────────────────────────────────────────────

/**
 * Returns a random integer in the range [min, max] (inclusive).
 *
 * Educational note:
 *   Math.random() returns a float in [0, 1).
 *   Multiplying by (max - min + 1) and flooring gives an integer in the range.
 */
function randInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

/**
 * Shuffles an array in-place using the Fisher-Yates algorithm.
 *
 * Educational note:
 *   Fisher-Yates is the standard O(n) shuffle.  It works by iterating from
 *   the end of the array and swapping each element with a random earlier one.
 *   Simple slice() + sort(Math.random) is biased — don't use it for games!
 */
function shuffle(arr) {
    for (let i = arr.length - 1; i > 0; i--) {
        const j = randInt(0, i);
        [arr[i], arr[j]] = [arr[j], arr[i]]; // ES6 destructuring swap
    }
    return arr;
}

/**
 * Picks one random fruit from the catalogue using weighted random sampling.
 *
 * Each fruit's rarity determines its weight (common = 50, legendary = 5).
 * We build a virtual "bag" and pick randomly from it.
 *
 * @returns {object} A fruit from FRUIT_CATALOGUE.
 */
function pickWeightedFruit() {
    // Build the weighted pool
    const pool = [];
    for (const fruit of FRUIT_CATALOGUE) {
        const weight = RARITIES[fruit.rarity].weight;
        for (let i = 0; i < weight; i++) pool.push(fruit);
    }
    return pool[randInt(0, pool.length - 1)];
}

/**
 * Formats seconds as MM:SS  (e.g. 90 → "01:30").
 *
 * @param {number} seconds
 * @returns {string}
 */
function formatTime(seconds) {
    const m = String(Math.floor(seconds / 60)).padStart(2, '0');
    const s = String(seconds % 60).padStart(2, '0');
    return `${m}:${s}`;
}

// ─── Market & Demand generation ───────────────────────────────────────────────

/**
 * Creates a new demand and a fresh 6-card market.
 *
 * Demand quantity:
 *   Legendary / Rare  → always 1
 *   Uncommon / Common → randomly 1 or 2
 *
 * The market always contains exactly demandQty copies of the demanded fruit
 * plus (MARKET_SIZE - demandQty) other fruits chosen at random.
 */
function generateRound() {
    // 1. Choose the demanded fruit
    state.demandFruit     = pickWeightedFruit();
    const rarity          = state.demandFruit.rarity;
    state.demandQty       = (rarity === 'LEGENDARY' || rarity === 'RARE') ? 1 : randInt(1, 2);
    state.demandCollected = 0;

    // 2. Build the market
    const market = [];

    // Add required copies of demanded fruit
    for (let i = 0; i < state.demandQty; i++) {
        market.push(state.demandFruit);
    }

    // Fill remaining slots with other fruits
    const others = FRUIT_CATALOGUE.filter(f => f.name !== state.demandFruit.name);
    shuffle(others);
    for (let i = 0; i < MARKET_SIZE - state.demandQty; i++) {
        market.push(others[i % others.length]);
    }

    state.market = shuffle(market);
}

// ─── Rendering ────────────────────────────────────────────────────────────────

/**
 * Re-draws the 6 fruit cards in the market grid.
 *
 * Educational note:
 *   We clear innerHTML then rebuild it.  For a performance-critical game you
 *   would use a virtual DOM or diff algorithm, but for 6 cards this is fine.
 */
function renderMarket() {
    dom.marketGrid.innerHTML = '';

    state.market.forEach((fruit, index) => {
        const card = document.createElement('div');
        card.className  = 'fruit-card';
        card.dataset.index  = index;
        card.dataset.rarity = fruit.rarity;

        const rarityMeta = RARITIES[fruit.rarity];

        card.innerHTML = `
            <div class="fruit-emoji">${fruit.emoji}</div>
            <div class="fruit-name">${fruit.name}</div>
            <div class="fruit-rarity-badge ${rarityMeta.cssClass}">${rarityMeta.label}</div>
        `;

        // Each card's click is handled by handleCardClick()
        card.addEventListener('click', () => handleCardClick(card, fruit, index));
        dom.marketGrid.appendChild(card);
    });
}

/**
 * Updates the demand panel text and progress bar.
 */
function renderDemand() {
    const fruit      = state.demandFruit;
    const remaining  = state.demandQty - state.demandCollected;
    const progressPc = (state.demandCollected / state.demandQty) * 100;

    dom.demandText.textContent =
        `${fruit.emoji}  Collect ${fruit.name}  ×${state.demandQty}`;

    // Build progress dots: filled ● vs empty ○
    const dots = Array.from({ length: state.demandQty }, (_, i) =>
        i < state.demandCollected ? '●' : '○'
    ).join(' ');

    dom.demandProgress.textContent =
        `${dots}  (${state.demandCollected} / ${state.demandQty} collected)`;
}

/**
 * Refreshes the status bar: timer, score, demands-completed count.
 */
function renderStatus() {
    const pct = state.timeRemaining / state.totalTime;
    dom.timerDisplay.textContent  = `⏱ ${formatTime(state.timeRemaining)}`;
    dom.scoreDisplay.textContent  = `💎 ${state.score} pts`;
    dom.demandsDisplay.textContent = `✅ ${state.demandsCompleted} done`;

    // Update the timer progress bar width
    dom.timerBar.style.width = `${pct * 100}%`;

    // Turn bar red when less than 20% time remains
    if (pct < 0.2) {
        dom.timerBar.classList.add('urgent');
    } else {
        dom.timerBar.classList.remove('urgent');
    }
}

/**
 * Shows a feedback message below the market for a short time.
 *
 * @param {string} message   Text to display.
 * @param {'correct'|'wrong'} type  Controls the colour class.
 */
function showFeedback(message, type) {
    dom.feedbackBar.textContent = message;
    dom.feedbackBar.className   = type;

    // Auto-clear after 1.5 seconds so it doesn't linger
    setTimeout(() => {
        dom.feedbackBar.textContent = '';
        dom.feedbackBar.className   = '';
    }, 1500);
}

// ─── Game Logic ───────────────────────────────────────────────────────────────

/**
 * Called when the player clicks a fruit card.
 *
 * @param {HTMLElement} card  The clicked card element.
 * @param {object}      fruit The fruit object associated with that card.
 * @param {number}      index Position in the market array (0-based).
 */
function handleCardClick(card, fruit, index) {
    if (!state.active) return; // ignore clicks outside of active game

    if (fruit.name === state.demandFruit.name) {
        // ── Correct pick ─────────────────────────────────────────────────────
        const pts = RARITIES[fruit.rarity].points;
        state.score += pts;
        state.demandCollected++;

        card.classList.add('flash-correct');
        setTimeout(() => card.classList.remove('flash-correct'), 400);

        if (state.demandCollected >= state.demandQty) {
            // Demand complete!
            state.demandsCompleted++;
            showFeedback(`✅ Demand complete!  +${pts} pts 🎉`, 'correct');

            // Pulse the demand panel to signal the switch
            dom.demandPanel.classList.remove('pulse');
            void dom.demandPanel.offsetWidth; // force reflow to restart animation
            dom.demandPanel.classList.add('pulse');

            // Short delay before next round so the player sees the feedback
            setTimeout(() => {
                generateRound();
                renderMarket();
                renderDemand();
            }, 400);
        } else {
            showFeedback(`✅ +${pts} pts!  Keep going…`, 'correct');
            renderDemand(); // update progress dots immediately
        }
    } else {
        // ── Wrong pick ───────────────────────────────────────────────────────
        state.score = Math.max(0, state.score - 1);

        card.classList.add('flash-wrong');
        setTimeout(() => card.classList.remove('flash-wrong'), 400);

        showFeedback(
            `❌ That's ${fruit.name}!  -1 pt  (need ${state.demandFruit.emoji} ${state.demandFruit.name})`,
            'wrong'
        );
    }

    renderStatus(); // keep score display in sync
}

// ─── Timer ────────────────────────────────────────────────────────────────────

/**
 * Called every second by setInterval().
 * Decrements the timer and triggers game-over when it reaches zero.
 */
function tick() {
    state.timeRemaining--;
    renderStatus();

    if (state.timeRemaining <= 0) {
        endGame();
    }
}

// ─── Screen management ────────────────────────────────────────────────────────

/**
 * Starts a new game session.
 * Called when the player clicks "Start Game" on the start screen.
 */
function startGame() {
    // Initialise state
    state.score            = 0;
    state.demandsCompleted = 0;
    state.timeRemaining    = selectedDuration;
    state.totalTime        = selectedDuration;
    state.active           = true;

    // Generate the first round
    generateRound();

    // Show game screen, hide others
    dom.startScreen.classList.add('hidden');
    dom.gameOverScreen.classList.add('hidden');
    dom.gameScreen.classList.remove('hidden');

    // Initial render
    renderMarket();
    renderDemand();
    renderStatus();
    dom.feedbackBar.textContent = '';

    // Start the countdown timer (fires every 1000 ms = 1 second)
    state.timerInterval = setInterval(tick, 1000);
}

/**
 * Stops the timer and transitions to the game-over screen.
 * Called either when time runs out (tick()) or … in a future expansion.
 */
function endGame() {
    clearInterval(state.timerInterval);
    state.active = false;

    // Short delay so the player sees the final state
    setTimeout(showGameOver, 600);
}

/**
 * Renders and reveals the game-over screen with the final stats.
 */
function showGameOver() {
    dom.gameScreen.classList.add('hidden');
    dom.gameOverScreen.classList.remove('hidden');

    dom.finalScore.textContent   = `${state.score} pts`;
    dom.finalDemands.textContent = `Demands completed: ${state.demandsCompleted}`;

    // Assign a rating just like the Java version
    let rating;
    if (state.score >= 100) {
        rating = '🏆 LEGENDARY COLLECTOR!';
        dom.finalRating.style.color = '#f5c518';
    } else if (state.score >= 60) {
        rating = '🔵 Expert Harvester';
        dom.finalRating.style.color = '#3498db';
    } else if (state.score >= 30) {
        rating = '🟢 Skilled Picker';
        dom.finalRating.style.color = '#56cc9d';
    } else if (state.score >= 10) {
        rating = '⭐ Budding Collector';
        dom.finalRating.style.color = '#e0e0e0';
    } else {
        rating = '🌱 Keep Practising!';
        dom.finalRating.style.color = '#a0a0b0';
    }
    dom.finalRating.textContent = rating;
}

/**
 * Resets the UI back to the start screen.
 */
function showStartScreen() {
    dom.gameOverScreen.classList.add('hidden');
    dom.gameScreen.classList.add('hidden');
    dom.startScreen.classList.remove('hidden');
}

// ─── Event listeners ──────────────────────────────────────────────────────────

// Duration selector buttons
dom.durationBtns.forEach(btn => {
    btn.addEventListener('click', () => {
        // Deselect all, then mark this one as selected
        dom.durationBtns.forEach(b => b.classList.remove('selected'));
        btn.classList.add('selected');
        selectedDuration = parseInt(btn.dataset.seconds, 10);
    });
});

// Start / Play Again buttons
dom.startBtn.addEventListener('click', startGame);
dom.playAgainBtn.addEventListener('click', showStartScreen);

// ─── Bootstrap ────────────────────────────────────────────────────────────────
// Highlight the default duration button (60 s) when the page loads.
document.addEventListener('DOMContentLoaded', () => {
    const defaultBtn = document.querySelector('.duration-btn[data-seconds="60"]');
    if (defaultBtn) defaultBtn.classList.add('selected');
});
