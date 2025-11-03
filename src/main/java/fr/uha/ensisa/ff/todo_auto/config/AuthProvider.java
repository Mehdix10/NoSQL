package fr.uha.ensisa.ff.todo_auto.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import fr.uha.ensisa.ff.todo_auto.dao.TodoDAO;
import fr.uha.ensisa.ff.todo_auto.dao.UnknownUserException;

@Component
public class AuthProvider implements AuthenticationProvider {
	
	@Autowired private TodoDAO dao;
	
	@Autowired private PasswordEncoder passwordEncoder;

	public static GrantedAuthority USER_AUTHORITY = new SimpleGrantedAuthority("USER");
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String name = authentication.getName();
		Object credentials = authentication.getCredentials();
		if (credentials == null) return null;
		String password = credentials.toString();
		
		try {
			String actualPassword = dao.getUserPassword(name);
			if (actualPassword == null) {
				throw new InternalAuthenticationServiceException("Cannot use stored password (null)");
			}
			if (passwordEncoder.matches(password, actualPassword)) {
				return new UsernamePasswordAuthenticationToken(name, passwordEncoder.encode(password), Arrays.asList(USER_AUTHORITY));
			}
			throw new BadCredentialsException("Bad password");
		} catch (UnknownUserException e) {
			throw new BadCredentialsException(e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			throw new InternalAuthenticationServiceException("Cannot used stored password");
		} catch (InternalAuthenticationServiceException x) {
			throw x;
		} catch (Exception x) {
			throw new InternalAuthenticationServiceException(x.getMessage(), x);
		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
}
