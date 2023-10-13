package willydekeyser.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import willydekeyser.service.AuthenticationStore;

public class MultiFactorAuthenticationHandler implements AuthenticationSuccessHandler {

	private SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final String authority;
   
	public MultiFactorAuthenticationHandler(
			String successUrl, 
			String authority) {
		SimpleUrlAuthenticationSuccessHandler authenticationSuccessHandler =
	            new SimpleUrlAuthenticationSuccessHandler(successUrl);
	    authenticationSuccessHandler.setAlwaysUseDefaultTargetUrl(true);
	    this.authenticationSuccessHandler = authenticationSuccessHandler;
	    this.authority = authority;        
	}

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request, 
			HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		System.err.println("onAuthenticationSuccess " + authentication);
		if (authentication instanceof UsernamePasswordAuthenticationToken) {
			AuthenticationStore.authentication = (UsernamePasswordAuthenticationToken) authentication;
			saveAuthentication(request, response, new MultiFactorAuthentication(authentication, authority, true));
		}
		if (authentication instanceof MultiFactorAuthentication) {
			saveAuthentication(request, response, new MultiFactorAuthentication(authentication, authority, true));
		}
        this.authenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);
	}

	private void saveAuthentication(
			HttpServletRequest request, 
			HttpServletResponse response, 
			Authentication authentication) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        securityContextRepository.saveContext(securityContext, request, response);
    }
	
}
