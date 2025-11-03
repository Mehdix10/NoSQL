package fr.uha.ensisa.ff.todo_auto.dao.dummy;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DummyUser {
	public String name, password;
	
	public List<DummyTask> defaultTasks = new LinkedList<>();
	
	public Map<String /* name */, DummyList> lists = new LinkedHashMap<>();
	
	public DummyUser(String name, String password) {
		this.name = name;
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public DummyUser addTask(String task) {
		this.defaultTasks.add(new DummyTask(task));
		return this;
	}
	
	public DummyList addList(String list) {
		DummyList l = new DummyList(list);
		this.lists.put(l.getId(), l);
		return l;
	}
	
	public DummyTask getTask(String id) {
		return this.defaultTasks.stream().filter(t -> t.id.equals(id)).findFirst().get();
	}

	public void deleteTask(String id) {
		this.defaultTasks.removeIf(t -> t.id.equals(id));
	}
}
