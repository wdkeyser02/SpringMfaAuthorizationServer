package willydekeyser.controller;

import java.io.IOException;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import willydekeyser.security.MultiFactorAuthentication;
import willydekeyser.security.MultiFactorAuthenticationHandler;
import willydekeyser.service.AuthenticationStore;
import willydekeyser.service.AuthenticatorService;

@Controller
public class LoginController {

	private final AuthenticationSuccessHandler authenticationSuccessHandler;
	private final AuthenticatorService authenticatorService;
	private final AuthenticationStore authenticationStore;
	
	private final AuthenticationSuccessHandler securityQuestionSuccessHandler = new MultiFactorAuthenticationHandler("/security-question", "SECURITY_QUESTION_REQUIRED");
	private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
		
	private final AuthenticationFailureHandler authenticatorFailureHandler =
			new SimpleUrlAuthenticationFailureHandler("/authenticator?error");
	
	private final AuthenticationFailureHandler securityQuestionFailureHandler =
			new SimpleUrlAuthenticationFailureHandler("/security-question?error");
	
	private static final Authentication ANONYMOUS_AUTHENTICATION = new AnonymousAuthenticationToken(
			"anonymous", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS", "ROLE_TESTERS"));
	
	private static Authentication TEMP_AUTHENTICATION = new UsernamePasswordAuthenticationToken(
			"user", "user", AuthorityUtils.createAuthorityList("ROLE_USER"));
	
	public LoginController(AuthenticationSuccessHandler authenticationSuccessHandler, AuthenticatorService authenticatorService, AuthenticationStore authenticationStore) {
		this.authenticationSuccessHandler = authenticationSuccessHandler;
		this.authenticatorService = authenticatorService;
		this.authenticationStore = authenticationStore;
	}
	
	
	@GetMapping("/login")
	public String login() {
		return "login";
	}
		
	@GetMapping("/authenticator")
	public String authenticator(
			HttpServletRequest request,
			HttpServletResponse response) {
		SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
		securityContext.setAuthentication(ANONYMOUS_AUTHENTICATION);
		SecurityContextHolder.setContext(securityContext);
		securityContextRepository.saveContext(securityContext, request, response);
		return "authenticator";
	}

	@PostMapping("/authenticator")
	public void validateCode(
			@RequestParam("code") String code,
			HttpServletRequest request,
			HttpServletResponse response,
			MultiFactorAuthentication authentication) throws ServletException, IOException {
		if(this.authenticatorService.check("QDWSM3OYBPGTEVSPB5FKVDM3CSNCWHVK", code) || code.equals("test")) {
			SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
			SecurityContextHolder.setContext(securityContext);
			securityContextRepository.saveContext(securityContext, request, response);			
			this.securityQuestionSuccessHandler.onAuthenticationSuccess(request, response, TEMP_AUTHENTICATION);
			return;
		}
		authenticatorFailureHandler.onAuthenticationFailure(request, response, new BadCredentialsException("bad credentials"));
	}

	@GetMapping("/security-question")
	public String securityQuestion(Model model, 
			HttpServletRequest request,
			HttpServletResponse response) {
		SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
		securityContext.setAuthentication(ANONYMOUS_AUTHENTICATION);
		SecurityContextHolder.setContext(securityContext);
		securityContextRepository.saveContext(securityContext, request, response);
		return "security-question";
	}

	@PostMapping("/security-question")
	public void validateAnswer(
			@RequestParam("answer") String answer,
			HttpServletRequest request,
			HttpServletResponse response,
			MultiFactorAuthentication authentication) throws ServletException, IOException {
		if(answer.equals("Willy")) {
			SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
			securityContext.setAuthentication(authenticationStore.get());
			SecurityContextHolder.setContext(securityContext);
			securityContextRepository.saveContext(securityContext, request, response);		
			this.authenticationSuccessHandler.onAuthenticationSuccess(request, response, TEMP_AUTHENTICATION);
			return;
		}
		securityQuestionFailureHandler.onAuthenticationFailure(request, response, new BadCredentialsException("bad credentials"));
	}
	
}
