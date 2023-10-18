package willydekeyser.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
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

import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import willydekeyser.security.MFAAuthentication;
import willydekeyser.security.MFAHandler;
import willydekeyser.user.CustomUserDetails;
import willydekeyser.user.User;

@Controller
public class LoginController {

	private final SecurityContextRepository securityContextRepository = 
			new HttpSessionSecurityContextRepository();
	private final AuthenticationFailureHandler authenticatorFailureHandler =
			new SimpleUrlAuthenticationFailureHandler("/authenticator?error");
	private final AuthenticationFailureHandler securityQuestionFailureHandler =
			new SimpleUrlAuthenticationFailureHandler("/security-question?error");
	private final AuthenticationSuccessHandler securityQuestionSuccessHandler =
			new MFAHandler("/security-question", "ROLE_SECURITY_QUESTION_REQUIRED");
	
	private final AuthenticationSuccessHandler authenticationSuccessHandler;
	
	public LoginController(AuthenticationSuccessHandler authenticationSuccessHandler) {
		this.authenticationSuccessHandler = authenticationSuccessHandler;
	}
	
	@GetMapping("/login")
	public String login() {
		return "login";
	}
	
	@GetMapping("/registration")
	public String registration(Model model) {
		String base32Secret = TimeBasedOneTimePasswordUtil.generateBase32Secret();
		String keyId = "Spring Boot Tutorial";
		String code = "";
		try {
			code = TimeBasedOneTimePasswordUtil.generateCurrentNumberString(base32Secret);
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
		System.err.println(code);
		model.addAttribute("qrImage", TimeBasedOneTimePasswordUtil.qrImageUrl(keyId, base32Secret));
		return "registration";
	}
	
	@GetMapping("/authenticator")
	public String authenticator(
			@CurrentSecurityContext SecurityContext context) throws GeneralSecurityException {		
		if (!getUser(context).mfaRegistered()) {
			return "redirect:registration";
		}
		return "authenticator";
	}

	@PostMapping("/authenticator")
	public void validateCode(
			@RequestParam("code") String code,
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentSecurityContext SecurityContext context) throws ServletException, IOException {
		if (code.equals(getUser(context).mfaSecret())) {
			if (getUser(context).securityQuestionEnabled()) {
				this.authenticationSuccessHandler.onAuthenticationSuccess(request, response, getAuthentication(request, response));
				return;
			}
			this.securityQuestionSuccessHandler.onAuthenticationSuccess(request, response, getAuthentication(request, response));
			return;
		}
		this.authenticatorFailureHandler.onAuthenticationFailure(request, response, new BadCredentialsException("bad credentials"));
	}
	
	@GetMapping("/security-question")
	public String securityQuestion(
			@CurrentSecurityContext SecurityContext context, 
			Model model) {
		model.addAttribute("question", getUser(context).securityQuestion());
		return "security-question";
	}
	
	@PostMapping("/security-question")
	public void validateSecurityQuestion(
			@RequestParam("answer") String answer,
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentSecurityContext SecurityContext context) throws ServletException, IOException {
		if (answer.equals(getUser(context).answer())) {
			this.authenticationSuccessHandler.onAuthenticationSuccess(request, response, getAuthentication(request, response));
			return;
		}
		this.securityQuestionFailureHandler.onAuthenticationFailure(request, response, new BadCredentialsException("bad credentials"));
	}
	
	private Authentication getAuthentication(
			HttpServletRequest request,
			HttpServletResponse response) {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		MFAAuthentication mfaAuthentication = (MFAAuthentication) securityContext.getAuthentication();		
		securityContext.setAuthentication(mfaAuthentication.getPrimaryAuthentication());
		SecurityContextHolder.setContext(securityContext);
		securityContextRepository.saveContext(securityContext, request, response);
		return mfaAuthentication.getPrimaryAuthentication();
	}
	
	private User getUser(SecurityContext context) {
		MFAAuthentication mfaAuthentication = (MFAAuthentication) context.getAuthentication();
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = 
				(UsernamePasswordAuthenticationToken) mfaAuthentication.getPrimaryAuthentication();
		CustomUserDetails userDetails = (CustomUserDetails) usernamePasswordAuthenticationToken.getPrincipal();
		return userDetails.getUser();
	}
}
