package willydekeyser.controller;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import willydekeyser.service.AuthenticationStore;

@Controller
public class LoginController {

	private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
	private final AuthenticationFailureHandler authenticatorFailureHandler =
			new SimpleUrlAuthenticationFailureHandler("/authenticator?error");
	
	
	private final AuthenticationSuccessHandler authenticationSuccessHandler;
	private final AuthenticationStore authenticationStore;
	
	public LoginController(AuthenticationSuccessHandler authenticationSuccessHandler, AuthenticationStore authenticationStore) {
		this.authenticationSuccessHandler = authenticationSuccessHandler;
		this.authenticationStore = authenticationStore;
	}
	
	@GetMapping("/login")
	public String login() {
		return "login";
	}
		
	@GetMapping("/authenticator")
	public String authenticator(HttpServletRequest request,
			HttpServletResponse response) {
		return "authenticator";
	}

	@PostMapping("/authenticator")
	public void validateCode(
			@RequestParam("code") String code,
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication) throws ServletException, IOException {
		if (code.equals("123")) {
			this.authenticationSuccessHandler.onAuthenticationSuccess(request, response, getAuthentication(request, response));
			return;
		}
		authenticatorFailureHandler.onAuthenticationFailure(request, response, new BadCredentialsException("bad credentials"));

	}
		
	private Authentication getAuthentication(
			HttpServletRequest request,
			HttpServletResponse response) {
		Authentication authentication = authenticationStore.getAuthentication();
		SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
		securityContext.setAuthentication(authentication);
		SecurityContextHolder.setContext(securityContext);
		securityContextRepository.saveContext(securityContext, request, response);
		return authentication;
	}
}
