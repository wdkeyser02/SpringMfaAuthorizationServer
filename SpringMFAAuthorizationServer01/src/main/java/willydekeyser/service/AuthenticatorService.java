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
}
