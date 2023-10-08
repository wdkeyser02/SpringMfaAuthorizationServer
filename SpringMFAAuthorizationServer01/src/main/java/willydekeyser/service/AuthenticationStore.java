package willydekeyser.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationStore {

	public static UsernamePasswordAuthenticationToken authentication;
	
	public void save(UsernamePasswordAuthenticationToken authentication) {
		AuthenticationStore.authentication = authentication;
	}
	
	public UsernamePasswordAuthenticationToken get() {
		return authentication;
	}
}
