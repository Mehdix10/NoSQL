package fr.uha.ensisa.ff.todo_auto.dao;

import java.util.List;
import java.util.Map;

/**
 * Interface to access todo lists data
 * User are identified by their unique names and own a (supposedly hashed) password.
 * User have a certain number of tasks (in the so called default list)
 * User can also have additional lists of tasks, with a unique id (for the user) and a name.
 * Tasks have a unique id (for their owner, be it a user - in the case it belongs to the default list - or a list),
 * a name, and a boolean done marker. 
 */
public interface TodoDAO extends AutoCloseable {
	
	void registerUser(String user, String password) throws UserAlreadyExistsException;

	String getUserPassword(String user) throws UnknownUserException;

	/**
	 * @return the id of the created task (which is not done)
	 */
	String createDefaultTask(String user, String taskName) throws UnknownUserException;

	/**
	 * @return [{id: "id_t1", name: "name task 1", done: false}, ...]
	 */
	List<Map<String, Object>> getDefaultTasks(String user) throws UnknownUserException;

	void setDefaultTaskDone(String user, String taskId, boolean done) throws UnknownUserException;

	void renameDefaultTask(String user, String taskId, String newName) throws UnknownUserException;

	void deleteDefaultTask(String user, String taskId) throws UnknownUserException;

	/**
	 * @return the id of the created list
	 */
	String createList(String user, String name) throws UnknownUserException;
	
	/**
	 * @return [{id: "l1Id", name : "List 1 name"}, {id: "l2Id",  name: "List 2 name", sharedWith: ["user1@example.com", "user2@example.com"] }, ...]
	 */
	List<Map<String, ?>> getLists(String user) throws UnknownUserException;

	void deleteList(String user, String listId) throws UnknownUserException, UnknownListException;

	void renameList(String user, String listId, String newName) throws UnknownUserException, UnknownListException;

	/**
	 * @return [{id: "id_t1", name: "name task 1", done: false}, ...]
	 */
	List<Map<String, Object>> getTasksOfList(String user, String listId) throws UnknownUserException, UnknownListException;

	/**
	 * @return the id of the created task (which is not done)
	 */
	String createListTask(String user, String listId, String taskName) throws UnknownUserException, UnknownListException;

	void renameListTask(String user,  String listId, String taskId,String newTaskName) throws UnknownUserException, UnknownListException;

	void setListTaskDone(String user, String listId, String taskId, boolean done) throws UnknownUserException, UnknownListException;

	void deleteListTask(String user, String listId, String taskId) throws UnknownUserException, UnknownListException;
	
}
