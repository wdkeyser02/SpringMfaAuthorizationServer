package willydekeyser.user;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;

public record User(
		String username, 
		String password,
		boolean enabled,
		boolean isAccountNonExpired,
		boolean isAccountNonLocked,
		boolean isCredentialsNonExpired,
		List<GrantedAuthority> authorities,
		String securityQuestion,
		String answer,
		String mfasecret,
		String mfaKeyId,
		boolean mfaEnabled,
		boolean mfaRegistered,
		boolean securityQuestionEnabled
		) {}
