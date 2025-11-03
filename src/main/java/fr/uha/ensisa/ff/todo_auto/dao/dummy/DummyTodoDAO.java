package fr.uha.ensisa.ff.todo_auto.dao.dummy;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import fr.uha.ensisa.ff.todo_auto.dao.TodoDAO;
import fr.uha.ensisa.ff.todo_auto.dao.UnknownListException;
import fr.uha.ensisa.ff.todo_auto.dao.UnknownUserException;
import fr.uha.ensisa.ff.todo_auto.dao.UserAlreadyExistsException;

public class DummyTodoDAO implements TodoDAO {

	private Map<String /* name */, DummyUser > users = new TreeMap<>();
		
	public DummyTodoDAO() {
		DummyUser u;
		
		u = new DummyUser("user@example.com", "{bcrypt}$2a$10$FY9OnO.Hv0Q8w7ia5OWi8eFF4KDhA9f.732GLCO3VxgBMCElmIbcm")
				.addTask("A first task")
				.addTask("A second task")
				.addTask("A lastTask")
				;
		u.addList("Perso")
			.addTask("A personal task")
			;
		u.addList("Empty");
		users.put(u.getName(), u);
		
	}
	
	protected DummyUser getUser(String name) throws UnknownUserException {
		DummyUser ret = users.get(name);
		if (ret == null) throw new UnknownUserException(name);
		return ret;
	}
	
	protected DummyList getList(String user, String listId) throws UnknownUserException, UnknownListException {
		return getList(getUser(user), listId);
	}
	
	protected DummyList getList(DummyUser user, String listId) throws UnknownUserException, UnknownListException {
		DummyList ret = user.lists.get(listId);
		if (ret == null) throw new UnknownListException(user.getName(), listId);
		return ret;
	}

	@Override
	public void registerUser(String userName, String password) throws UserAlreadyExistsException {
		if (users.containsKey(userName)) throw new UserAlreadyExistsException(userName);
		users.put(userName, new DummyUser(userName, password));
		System.err.println("Registered " + userName + " with password " + password);
	}

	@Override
	public String getUserPassword(String username) throws UnknownUserException {
		return getUser(username).getPassword();
	}

	@Override
	public List<Map<String, ?>> getLists(String username) throws UnknownUserException {
		List<Map<String, ?>> ret = new LinkedList<>();
		getUser(username).lists.entrySet().stream().forEach(e -> {
			DummyList dl = e.getValue();
			Map<String, Object> list = new TreeMap<>();
			list.put("id", e.getKey());
			list.put("name", dl.getName());
			ret.add(list);
		});
		return ret;
	}

	@Override
	public String createList(String user, String name) throws UnknownUserException {
		DummyUser u = getUser(user);
		String id = u.addList(name).getId();
		return id;
	}

	@Override
	public void deleteList(String user, String id) throws UnknownUserException, UnknownListException {
		DummyUser u = getUser(user);
		if (u.lists.remove(id) == null) throw new UnknownListException(user, id);
	}

	@Override
	public void renameList(String user, String id, String newName) throws UnknownUserException, UnknownListException {
		DummyUser u = getUser(user);
		getList(u, id).setName(newName);
	}
	
	protected List<Map<String, Object>> toTaskList(List<DummyTask> tasks) {
		return tasks.stream().map(t -> {
				Map<String, Object> ret = new LinkedHashMap<>();
				ret.put("id", t.getId());
				ret.put("name", t.getName());
				ret.put("done", t.isDone());
				return ret;
			}).collect(Collectors.toList());
	}

	@Override
	public List<Map<String, Object>> getDefaultTasks(String user) throws UnknownUserException {
		return toTaskList(getUser(user).defaultTasks);
	}

	@Override
	public List<Map<String, Object>> getTasksOfList(String user, String list) throws UnknownUserException, UnknownListException {
		return toTaskList(getList(user, list).tasks);
	}

	@Override
	public String createDefaultTask(String user, String name) throws UnknownUserException {
		DummyTask ret = new DummyTask(name);
		DummyUser u = getUser(user);
		u.defaultTasks.add(ret);
		return ret.getId();
	}

	@Override
	public String createListTask(String user, String list, String name) throws UnknownUserException, UnknownListException {
		DummyTask ret = new DummyTask(name);
		DummyUser u = getUser(user);
		getList(u, list).tasks.add(ret);
		return ret.getId();
	}

	@Override
	public void renameDefaultTask(String user, String id, String newName) throws UnknownUserException {
		DummyUser u = getUser(user);
		DummyTask t = u.getTask(id);
		t.setName(newName);
	}

	@Override
	public void renameListTask(String user, String list, String id, String newName)
			throws UnknownUserException, UnknownListException {
		DummyUser u = getUser(user);
		DummyTask t = getList(u, list).getTask(id);
		t.setName(newName);
	}

	@Override
	public void setDefaultTaskDone(String user, String id, boolean done) throws UnknownUserException {
		DummyUser u = getUser(user);
		DummyTask t = u.getTask(id);
		t.setDone(done);
	}

	@Override
	public void setListTaskDone(String user, String list, String id, boolean done)
			throws UnknownUserException, UnknownListException {
		DummyUser u = getUser(user);
		DummyTask t = getList(u, list).getTask(id);
		t.setDone(done);
	}

	@Override
	public void deleteDefaultTask(String user, String id) throws UnknownUserException {
		DummyUser u = getUser(user);
		DummyTask t = u.getTask(id);
		u.deleteTask(id);
	}

	@Override
	public void deleteListTask(String user, String list, String id) throws UnknownUserException, UnknownListException {
		DummyUser u = getUser(user);
		DummyList l = getList(u, list);
		DummyTask t = l.getTask(id);
		l.deleteTask(id);
	}

	@Override
	public void close() throws Exception {
		System.out.println("Close invoked on Dummy DAO");
	}

}
