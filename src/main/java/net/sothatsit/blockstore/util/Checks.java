package net.sothatsit.blockstore.util;

/**
 * Static methods that check assertions.
 */
public class Checks {
    /**
     * Throw IllegalArgumentException if the argument is null.
     *
     * @param argument the object to check.
     * @param argName  the name of the argument in the exception message.
     */
    public static void ensureNonNull(Object argument, String argName) {
        ensureTrue(argument != null, argName + " cannot be null");
    }

    /**
     * Throw IllegalArgumentException if the array argument or any of its
     * elements is null.
     *
     * @param aray      the array to check.
     * @param arrayName the name of the array in the exception message.
     */
    public static <T> void ensureArrayNonNull(T[] array, String arrayName) {
        ensureNonNull(array, arrayName);

        for (T element : array) {
            ensureTrue(element != null, arrayName + " cannot contain null values");
        }
    }

    /**
     * Throw IllegalArgumentException if the expression is false.
     *
     * @param expression the expression to check,
     * @param message    the exception message.
     */
    public static void ensureTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

}
