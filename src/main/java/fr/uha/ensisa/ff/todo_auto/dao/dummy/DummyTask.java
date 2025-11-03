package fr.uha.ensisa.ff.todo_auto.dao.dummy;

public class DummyTask extends DummyIdentifiable {
	
	private static long ID_GENERATOR;
	
	private boolean done;
	
	public DummyTask(String name) {
		this(Long.toString(++ID_GENERATOR), name);
	}
	
	public DummyTask(String id, String name) {
		super(id, name);
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}
	
}
