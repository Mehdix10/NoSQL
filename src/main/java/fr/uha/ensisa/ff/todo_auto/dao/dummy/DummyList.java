package fr.uha.ensisa.ff.todo_auto.dao.dummy;

import java.util.LinkedList;
import java.util.List;

public class DummyList extends DummyIdentifiable {
	
	private static long ID_GENERATOR;
	
	protected List<DummyTask> tasks = new LinkedList<>();
	
	public DummyList(String name) {
		this(Long.toString(++ID_GENERATOR), name);
	}
	
	public DummyList(String id, String name) {
		super(id, name);
	}
	
	public DummyList addTask(String task) {
		this.tasks.add(new DummyTask(name));
		return this;
	}

	public DummyTask getTask(String id) {
		return this.tasks.stream().filter(t -> t.id.equals(id)).findFirst().get();
	}

	public void deleteTask(String id) {
		this.tasks.removeIf(t -> t.id.equals(id));
	}

}
