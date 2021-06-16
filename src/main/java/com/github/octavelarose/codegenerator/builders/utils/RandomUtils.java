package com.github.octavelarose.codegenerator.builders.utils;

import java.util.Random;

public class RandomUtils {
    /**
     * Generates a random name. Used to name methods, among others. May need to be moved to a "Utils" class.
     *
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
        Random rn = new Random();
        return rn.nextInt(maxValue + 1);
    }
}
