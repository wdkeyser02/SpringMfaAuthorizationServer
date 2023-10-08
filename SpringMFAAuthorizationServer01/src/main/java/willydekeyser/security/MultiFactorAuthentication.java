package willydekeyser.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.authority.AuthorityUtils;

public class MultiFactorAuthentication extends AbstractAuthenticationToken {

	private static final long serialVersionUID = 1L;
	private final Authentication authentication;
    private final boolean authenticated;

    public MultiFactorAuthentication(Authentication authentication, String authority, boolean authenticated) {
        super(AuthorityUtils.createAuthorityList(authority));
        this.authentication = authentication;
        this.authenticated = authenticated;
    }

    @Override
    public Object getPrincipal() {
        return this.authentication.getPrincipal();
    }

    @Override
    public Object getCredentials() {
        return this.authentication.getCredentials();
    }

    @Override
    public void eraseCredentials() {
        if (this.authentication instanceof CredentialsContainer) {
            ((CredentialsContainer) this.authentication).eraseCredentials();
        }
    }

    @Override
    public boolean isAuthenticated() {
        return this.authenticated;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        throw new UnsupportedOperationException();
    }

    public Authentication getPrimaryAuthentication() {
        return this.authentication;
    }

}
