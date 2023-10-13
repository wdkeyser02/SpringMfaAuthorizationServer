package willydekeyser.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

@Service
@SessionScope
public class AuthenticationStore {

	private Authentication authentication;
	
	public Authentication getAuthentication() {
		return authentication;
	}
	
	public void saveAuthentication(Authentication authentication) {
		this.authentication = authentication;
	}
}
