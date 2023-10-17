package willydekeyser.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.encrypt.BytesEncryptor;

import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;

public class AuthenticatorService {

	private final BytesEncryptor bytesEncryptor;

    public AuthenticatorService(BytesEncryptor bytesEncryptor) {
        this.bytesEncryptor = bytesEncryptor;
    }
    
    public boolean check(String key, String code) {
        try {
            String secret = new String(this.bytesEncryptor.decrypt(Hex.decode(key)), StandardCharsets.UTF_8);
            return TimeBasedOneTimePasswordUtil.validateCurrentNumber(secret, Integer.parseInt(code), 10000);
        }
        catch (IllegalArgumentException ex) {
            return false;
        }
        catch (GeneralSecurityException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    public String generateSecret() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[20];
        secureRandom.nextBytes(bytes);
        return new String(Hex.encode(this.bytesEncryptor.encrypt(bytes)));
    }
}
