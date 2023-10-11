package willydekeyser.security;

import java.io.IOException;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
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

public class TFAHandler implements AuthenticationSuccessHandler {

	private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
	private static final Authentication ANONYMOUS_AUTHENTICATION = new AnonymousAuthenticationToken(
			"anonymous", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS", "ROLE_2FA_REQUIRED"));
	
	private final AuthenticationSuccessHandler authenticationSuccessHandler;
	private final AuthenticationStore authenticationStore;
		
	public TFAHandler(AuthenticationStore authenticationStore) {
		SimpleUrlAuthenticationSuccessHandler authenticationSuccessHandler =
	            new SimpleUrlAuthenticationSuccessHandler("/authenticator");
		authenticationSuccessHandler.setAlwaysUseDefaultTargetUrl(true);
		this.authenticationSuccessHandler = authenticationSuccessHandler;
		this.authenticationStore = authenticationStore;
	}

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request, 
			HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		authenticationStore.saveAuthentication(authentication);
		setAnonymousAuthentication(request, response);
		this.authenticationSuccessHandler.onAuthenticationSuccess(request, response, ANONYMOUS_AUTHENTICATION);

	}

	private void setAnonymousAuthentication(
			HttpServletRequest request,
			HttpServletResponse response) {
		SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
		securityContext.setAuthentication(ANONYMOUS_AUTHENTICATION);
		SecurityContextHolder.setContext(securityContext);
		securityContextRepository.saveContext(securityContext, request, response);
	}
}
