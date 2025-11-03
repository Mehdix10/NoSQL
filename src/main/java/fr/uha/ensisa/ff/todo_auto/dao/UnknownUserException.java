package fr.uha.ensisa.ff.todo_auto.dao;

public class UnknownUserException extends Exception {
	
	private static final long serialVersionUID = -7671982113087384392L;

	private final String username;

	public UnknownUserException(String username) {
		super("Unknown user: " + username);
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

}
