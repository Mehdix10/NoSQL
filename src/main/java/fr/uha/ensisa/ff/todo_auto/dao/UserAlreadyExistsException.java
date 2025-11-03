package fr.uha.ensisa.ff.todo_auto.dao;

public class UserAlreadyExistsException extends DisplayableException {
	
	private static final long serialVersionUID = -7671982113087384392L;

	private final String username;

	public UserAlreadyExistsException(String username) {
		super("User already exists: " + username);
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

}
