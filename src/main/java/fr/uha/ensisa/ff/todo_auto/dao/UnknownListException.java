package fr.uha.ensisa.ff.todo_auto.dao;

public class UnknownListException extends DisplayableException {
	
	private static final long serialVersionUID = -7671982113087384392L;

	private final String username, listId;

	public UnknownListException(String username, String listId) {
		super("Unknown list " + listId + " for user " + username);
		this.username = username;
		this.listId = listId;
	}

	public String getUsername() {
		return username;
	}

	public String getListId() {
		return listId;
	}

}
