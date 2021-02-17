package no.ssb.rawdata.converter.service.dapla.oauth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

class JWTMother {

    private final RSAKey rsaPrivateJWK;
    private final RSAKey rsaPublicJWK;
    private final JWSSigner signer;

    public JWTMother() {
        try {
            // RSA signatures require a public and private RSA key pair, the public key
            // must be made known to the JWS recipient in order to verify the signatures
            rsaPrivateJWK = new RSAKeyGenerator(2048)
              .keyID("123")
              .generate();

            rsaPublicJWK = rsaPrivateJWK.toPublicJWK();

            // Create RSA-signer with the private key
            signer = new RSASSASigner(rsaPrivateJWK);
        } catch (JOSEException e) {
            throw new RuntimeException("Error initializing JWT test issuer", e);
        }
    }

    public String generateToken(Instant expirationTime) {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
          .subject("test")
          .issuer("https://keycloak.dev-bip-app.ssb.no/auth/realms/ssb")
          .expirationTime(Date.from(expirationTime))
          .jwtID(UUID.randomUUID().toString())
          .audience("dapla")
          .build();

        SignedJWT signedJWT = new SignedJWT(
          new JWSHeader.Builder(JWSAlgorithm.RS256)
            .type(JOSEObjectType.JWT)
            .keyID(rsaPrivateJWK.getKeyID())
            .build(), claimsSet
        );

        try {
            // Compute the RSA signature
            signedJWT.sign(signer);
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to sign JWT token", e);
        }

        return signedJWT.serialize();
    }

    public boolean verifyTokenSignature(String token) {
        SignedJWT signedJWT = null;
        try {
            signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new RSASSAVerifier(rsaPublicJWK);
            return signedJWT.verify(verifier);
        } catch (ParseException | JOSEException e) {
            return false;
        }
    }
}