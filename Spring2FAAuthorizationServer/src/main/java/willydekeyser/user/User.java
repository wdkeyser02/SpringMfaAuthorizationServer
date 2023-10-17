package willydekeyser.user;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;

public record User(
		String username, 
		String password,
		boolean isAccountNonExpired,
		boolean isAccountNonLocked,
		boolean isCredentialsNonExpired,
		boolean enabled,
		List<GrantedAuthority> authorities,
		String securityQuestion,
		String answer,
		String secret,
		boolean mfaEnabled,
		boolean mfaRegistered
		) {}
