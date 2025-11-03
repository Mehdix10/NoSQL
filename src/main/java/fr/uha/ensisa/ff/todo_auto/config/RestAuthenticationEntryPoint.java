package fr.uha.ensisa.ff.todo_auto.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public final class RestAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint
  implements AuthenticationEntryPoint {
 
    public RestAuthenticationEntryPoint() {
		super("/login");
	}

	@Override
    public void commence(
        final HttpServletRequest request, 
        final HttpServletResponse response, 
        final AuthenticationException authException) throws IOException, ServletException {
    	
    	String path = request.getServletPath();
    	if (path.startsWith("/api/")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    	} else {
    		super.commence(request, response, authException);
    	}
         
    }
}