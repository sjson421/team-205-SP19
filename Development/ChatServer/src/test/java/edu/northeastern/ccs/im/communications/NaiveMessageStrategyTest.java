package edu.northeastern.ccs.im.communications;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Test Naive Message Strategy
 */
@SuppressWarnings("squid:S00100") // testing naming convention is different
public class NaiveMessageStrategyTest {
    private IMessageStrategy strategy;

    /**
     * set up strategy each time.
     */
    @Before
    public void setUp() {
        strategy = new NaiveMessageStrategy();
    }

    /**
     * should return original text
     */
    @Test
    public void test_getOutputText_returnOriginalText() {
        // Arrange
        String raw = "foo";

        // Act
        String result = strategy.getOutputText(raw);

        // Assert
        assertEquals(raw, result);
    }

    /**
     * always return false
     */
    @Test
    public void test_isExpired_returnFalse() {
        // Arrange
        // Act
        boolean result = strategy.isExpired(null);

        // Assert
        assertFalse(result);
    }
}
