package tetris.game;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BagRandomizerTest {

    @Test
    void everyBagContainsEveryPieceExactlyOnce() {
        BagRandomizer bag = new BagRandomizer(new Random(7));
        for (int i = 0; i < 50; i++) {
            List<Tetromino> pieces = bag.nextBag();
            assertEquals(7, pieces.size());
            assertEquals(Set.of(Tetromino.values()), new HashSet<>(pieces));
        }
    }

    @Test
    void equalSeedsProduceEqualBags() {
        BagRandomizer first = new BagRandomizer(new Random(1234));
        BagRandomizer second = new BagRandomizer(new Random(1234));
        for (int i = 0; i < 25; i++) {
            assertEquals(first.nextBag(), second.nextBag());
        }
    }

    @Test
    void eachPieceAppearsEquallyOftenAcrossBags() {
        BagRandomizer bag = new BagRandomizer(new Random(99));
        int[] counts = new int[Tetromino.values().length];
        int bags = 20;
        for (int i = 0; i < bags; i++) {
            for (Tetromino piece : bag.nextBag()) {
                counts[piece.ordinal()]++;
            }
        }
        for (int count : counts) {
            assertEquals(bags, count);
        }
        assertTrue(bags > 0);
    }
}
