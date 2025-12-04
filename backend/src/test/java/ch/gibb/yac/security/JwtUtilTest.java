package ch.gibb.yac.security;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests for JwtUtil to verify token generation and validation functionality.
 */
public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String TEST_SECRET =
            "MyTestSecretKeyThatIsLongEnoughForHS256SigningAlgorithm";
    private static final int TEST_EXPIRATION_MS = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Inject test values using reflection
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", TEST_EXPIRATION_MS);
        // Call init to initialize the key
        jwtUtil.init();
    }

    @Test
    void shouldGenerateValidToken() {
        String username = "testuser";

        String token = jwtUtil.generateToken(username);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        // JWT tokens contain 3 parts separated by dots
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void shouldGenerateDifferentTokensForDifferentUsers() {
        String token1 = jwtUtil.generateToken("user1");
        String token2 = jwtUtil.generateToken("user2");

        assertNotEquals(token1, token2);
        assertEquals("user1", jwtUtil.getUsernameFromToken(token1));
        assertEquals("user2", jwtUtil.getUsernameFromToken(token2));
    }

    @Test
    void shouldGenerateTokenWithCorrectExpiration() {
        String username = "testuser";
        long beforeGeneration = System.currentTimeMillis();

        String token = jwtUtil.generateToken(username);

        long afterGeneration = System.currentTimeMillis();

        // Extract expiration from token
        long expirationTime =
                Jwts.parser()
                        .verifyWith(
                                Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8)))
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
                        .getExpiration()
                        .getTime();

        // Expiration should be approximately TEST_EXPIRATION_MS from now
        long expectedMinExpiration =
                afterGeneration + TEST_EXPIRATION_MS - 1000; // Allow 1 second tolerance
        long expectedMaxExpiration = afterGeneration + TEST_EXPIRATION_MS + 1000;
        assertTrue(
                expirationTime >= expectedMinExpiration && expirationTime <= expectedMaxExpiration,
                "Expiration time should be approximately "
                        + TEST_EXPIRATION_MS
                        + "ms from generation");
    }

    @Test
    void shouldExtractCorrectUsernameFromToken() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        String extractedUsername = jwtUtil.getUsernameFromToken(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void shouldExtractUsernameWithSpecialCharacters() {
        String username = "test.user@example.com";
        String token = jwtUtil.generateToken(username);

        String extractedUsername = jwtUtil.getUsernameFromToken(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void shouldThrowExceptionWhenExtractingUsernameFromInvalidToken() {
        String invalidToken = "invalid.token.here";

        assertThrows(Exception.class, () -> jwtUtil.getUsernameFromToken(invalidToken));
    }

    @Test
    void shouldThrowExceptionWhenExtractingUsernameFromMalformedToken() {
        String malformedToken = "this-is-not-a-jwt";

        assertThrows(Exception.class, () -> jwtUtil.getUsernameFromToken(malformedToken));
    }

    @Test
    void shouldValidateCorrectToken() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        boolean isValid = jwtUtil.validateJwtToken(token);

        assertTrue(isValid);
    }

    @Test
    void shouldRejectInvalidToken() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtUtil.validateJwtToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    void shouldRejectMalformedToken() {
        String malformedToken = "this-is-not-a-jwt";

        boolean isValid = jwtUtil.validateJwtToken(malformedToken);

        assertFalse(isValid);
    }

    @Test
    void shouldRejectEmptyToken() {
        String emptyToken = "";

        boolean isValid = jwtUtil.validateJwtToken(emptyToken);

        assertFalse(isValid);
    }

    @Test
    void shouldRejectExpiredToken() throws InterruptedException {
        // Create a new JwtUtil with very short expiration
        JwtUtil shortLivedJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortLivedJwtUtil, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(shortLivedJwtUtil, "jwtExpirationMs", 1); // 1ms expiration
        shortLivedJwtUtil.init();

        String token = shortLivedJwtUtil.generateToken("testuser");
        // Wait for token to expire
        Thread.sleep(100);

        boolean isValid = shortLivedJwtUtil.validateJwtToken(token);

        assertFalse(isValid);
    }

    @Test
    void shouldValidateCorrectTokens() {
        String token1 = jwtUtil.generateToken("user1");
        String token2 = jwtUtil.generateToken("user2");
        String token3 = jwtUtil.generateToken("user3");

        assertTrue(jwtUtil.validateJwtToken(token1));
        assertTrue(jwtUtil.validateJwtToken(token2));
        assertTrue(jwtUtil.validateJwtToken(token3));
    }
}
