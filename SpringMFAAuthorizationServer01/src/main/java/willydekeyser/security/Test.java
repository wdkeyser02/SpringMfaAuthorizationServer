package willydekeyser.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class Test implements AuthenticationSuccessHandler {

	private final AuthenticationSuccessHandler authenticationSuccessHandler;
	
	public Test() {
		SimpleUrlAuthenticationSuccessHandler authenticationSuccessHandler =
	            new SimpleUrlAuthenticationSuccessHandler("/authenticator");
		authenticationSuccessHandler.setAlwaysUseDefaultTargetUrl(true);
		this.authenticationSuccessHandler = authenticationSuccessHandler;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		System.err.println("onAuthenticationSuccess");
		System.err.println(authentication);
		this.authenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);
		
	}

}
