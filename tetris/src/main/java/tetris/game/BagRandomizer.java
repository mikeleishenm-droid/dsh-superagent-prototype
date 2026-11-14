package tetris.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * The standard 7-bag randomizer: each bag contains every tetromino exactly
 * once, in a random order. This avoids long droughts of a single shape.
 */
public final class BagRandomizer {

    private final Random random;

    public BagRandomizer(Random random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    public List<Tetromino> nextBag() {
        List<Tetromino> bag = new ArrayList<>(List.of(Tetromino.values()));
        Collections.shuffle(bag, random);
        return List.copyOf(bag);
    }
}
