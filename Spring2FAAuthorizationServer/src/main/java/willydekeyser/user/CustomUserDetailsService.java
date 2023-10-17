package willydekeyser.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


public class CustomUserDetailsService implements UserDetailsService {

	private final JdbcTemplate jdbcTemplate;
	
	public CustomUserDetailsService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		List<String> authorities = new ArrayList<>();
		String sql = """
				SELECT user.username, user.password, user.enabled, authorities.authority, userinfo.isAccountNonExpired, 
				userinfo.isAccountNonLocked, userinfo.isCredentialsNonExpired, userinfo.securityQuestion, 
				userinfo.securityAnswer, userinfo.mfaSecret, userinfo.mfaEnabled, userinfo.mfaRegistered  
				FROM usersinfo userinfo, users user 
				LEFT JOIN authorities on user.username = authorities.username 
				WHERE user.username = userinfo.username AND user.username = ?;
				""";
		return jdbcTemplate.query(sql, rs -> {
			String username = "";
			String password = "";
			boolean enabled = false;
			boolean isAccountNonExpired = false;
			boolean isAccountNonLocked = false;
			boolean isCredentialsNonExpired = false;
			String securityQuestion = "";
			String securityAnswer = "";
			String mfaSecret = "";
			boolean mfaEnabled = false;
			boolean mfaRegistered = false;
			boolean first = true;
			while (rs.next()) {
				if (first) {
					first = false;
					username = rs.getString("username");
					password = rs.getString("password");
					enabled = rs.getBoolean("enabled");
					isAccountNonExpired = rs.getBoolean("isAccountNonExpired");
					isAccountNonLocked = rs.getBoolean("isAccountNonLocked");
					isCredentialsNonExpired = rs.getBoolean("isCredentialsNonExpired");
					securityQuestion = rs.getString("securityQuestion");
					securityAnswer = rs.getString("securityAnswer");
					mfaSecret = rs.getString("mfaSecret");
					mfaEnabled = rs.getBoolean("mfaEnabled");
					mfaRegistered = rs.getBoolean("mfaRegistered");
				}
				authorities.add(rs.getString("authority"));
			}
			return new CustomUserDetails(
					new User(username, password, enabled, isAccountNonExpired, isAccountNonLocked, isCredentialsNonExpired, AuthorityUtils.createAuthorityList(authorities), 
							securityQuestion, securityAnswer, mfaSecret, mfaEnabled, mfaRegistered));
		}, userName);

	}

}