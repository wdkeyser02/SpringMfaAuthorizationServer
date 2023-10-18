package willydekeyser.service;

import java.security.GeneralSecurityException;

import org.springframework.stereotype.Service;

import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;

@Service
public class AuthenticatorService {

    public boolean check(String key, String code) {
        try {
            return TimeBasedOneTimePasswordUtil.validateCurrentNumber(key, Integer.parseInt(code), 10000);
        }
        catch (IllegalArgumentException ex) {
            return false;
        }
        catch (GeneralSecurityException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    public String generateSecret() {
        return TimeBasedOneTimePasswordUtil.generateBase32Secret();
    }
    
    public String generateQrImageUrl(String keyId, String base32Secret) {
        return TimeBasedOneTimePasswordUtil.qrImageUrl(keyId, base32Secret);
    }
    
    public String getCode(String base32Secret) throws GeneralSecurityException {
    	return TimeBasedOneTimePasswordUtil.generateCurrentNumberString(base32Secret);
    }
}
