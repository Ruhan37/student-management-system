package com.project.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * ====================================================================
 * JWT TOKEN PROVIDER - Creates and validates JWT tokens
 * ====================================================================
 *
 * JWT (JSON Web Token) STRUCTURE:
 * ┌────────────────────────────────────────────────────────────────┐
 * │  HEADER.PAYLOAD.SIGNATURE                                      │
 * │                                                                │
 * │  Header: {"alg": "HS256", "typ": "JWT"}                       │
 * │  Payload: {"sub": "user@email.com", "exp": 1234567890, ...}   │
 * │  Signature: HMACSHA256(base64(header) + "." + base64(payload),│
 * │                        secret)                                 │
 * └────────────────────────────────────────────────────────────────┘
 *
 * WHY JWT?
 * 1. Stateless: Server doesn't need to store session data
 * 2. Scalable: Any server can validate the token
 * 3. Secure: Signed to prevent tampering
 * 4. Self-contained: Contains all user info needed
 *
 * JWT FLOW IN OUR APP:
 * 1. User logs in → JwtTokenProvider.generateToken() creates token
 * 2. Token sent to client
 * 3. Client sends token in header: "Authorization: Bearer <token>"
 * 4. JwtAuthenticationFilter extracts and validates token
 * 5. If valid, user is authenticated for this request
 */
@Component
public class JwtTokenProvider {

    /**
     * Secret key for signing tokens
     * CRITICAL: In production, use environment variable, not hardcoded!
     *
     * @Value reads from application.properties
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Token validity period in milliseconds
     * 10800000ms = 3 hours (as you requested)
     */
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Generate JWT token for authenticated user
     *
     * @param authentication Spring Security authentication object
     * @return JWT token string
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails);
    }

    /**
     * Generate JWT token from UserDetails
     *
     * TOKEN CLAIMS (data inside token):
     * - sub (subject): User's email (identifier)
     * - iat (issued at): When token was created
     * - exp (expiration): When token expires
     * - role: User's role for authorization
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Add role to claims (for frontend to know user's role)
        claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());

        return Jwts.builder()
                .claims(claims)                                    // Custom claims (role)
                .subject(userDetails.getUsername())                // Email as subject
                .issuedAt(new Date())                              // Current time
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))  // Expiry time
                .signWith(getSigningKey())                         // Sign with secret key
                .compact();                                        // Build the token string
    }

    /**
     * Extract username (email) from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract any claim from token using a function
     *
     * GENERIC METHOD EXPLANATION:
     * - <T> means this method can return any type
     * - Function<Claims, T> takes Claims and returns type T
     * - Allows extracting any claim flexibly
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims (payload) from token
     * This also validates the signature!
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())  // Verify signature with our secret
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Check if token is expired
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validate token
     *
     * VALIDATION STEPS:
     * 1. Extract username from token
     * 2. Compare with provided UserDetails
     * 3. Check if token is not expired
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Get signing key from secret
     *
     * HMAC-SHA256 requires at least 256-bit (32 byte) key
     * We decode our Base64 secret to get the key bytes
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Get token expiration time (for response)
     */
    public long getJwtExpiration() {
        return jwtExpiration;
    }
}
