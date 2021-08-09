package com.github.octavelarose.bootleg.builders.programs.utils;

import java.util.Random;

/**
 * Literally utils for randomness related operations, not a mess of utils for random classes, because we have (debatable) standards
 */
public class RandomUtils {
    /**
     * Generates a random name. Used to name methods, among others.
     * @param nbrCharacters Number of characters of the output string.
     * @return A string made up of nbrCharacters random characters.
     */
    static public String generateRandomName(int nbrCharacters) {
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(nbrCharacters);
        for (int i = 0; i < nbrCharacters; i++) {
            int randomLimitedInt = 'a' + (int) (random.nextFloat() * ('z' - 'a' + 1));
            buffer.append((char) randomLimitedInt);
        }

        return buffer.toString();
    }

    public static int generateRandomInt(int maxValue) {
        return new Random().nextInt(maxValue);
    }

    public static boolean generateRandomBool() {
        return new Random().nextBoolean();
    }

    public static float generateRandomFloat() {
        return new Random().nextFloat();
    }
}
