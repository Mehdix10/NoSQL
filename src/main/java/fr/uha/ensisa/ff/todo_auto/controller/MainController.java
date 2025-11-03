package fr.uha.ensisa.ff.todo_auto.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import fr.uha.ensisa.ff.todo_auto.config.AuthProvider;
import fr.uha.ensisa.ff.todo_auto.dao.TodoDAO;
import fr.uha.ensisa.ff.todo_auto.dao.UserAlreadyExistsException;

@Controller
public class MainController {
	
	@Autowired private TodoDAO dao;
	
	@Autowired private PasswordEncoder passwordEncoder;

	@RequestMapping(value = "/login")
	public String login() {
		return "forward:/login_page.html";
	}

	@RequestMapping(value = "/")
	public String home() {
		return "forward:/index.html";
	}
	
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public String register(@RequestParam(required = true) String username, @RequestParam(required = true) String password, @RequestParam(required = true) String password2) {

		if (password == null || password2 == null || !password.equals(password2)) {
			return "redirect:/login?error=Passwords%20don%27t%20match";
		}
		
		password = passwordEncoder.encode(password);
		
		try {
			dao.registerUser(username, password);
			SecurityContextHolder.getContext().setAuthentication(
					new UsernamePasswordAuthenticationToken(username, password, Arrays.asList(AuthProvider.USER_AUTHORITY))
				);
			return "redirect:/";
		} catch (UserAlreadyExistsException x) {
			return "redirect:/login?error=User%20exists";
		} catch (Exception x) {
			x.printStackTrace();
			String error = x.getMessage();
			try {
				error = URLEncoder.encode(error, "UTF-8");
			} catch (UnsupportedEncodingException ee) {
				ee.printStackTrace();
			}
			return "redirect:/login?error=" + error;
		}
	}
}
