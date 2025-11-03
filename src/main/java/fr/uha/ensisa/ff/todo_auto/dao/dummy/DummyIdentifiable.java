package fr.uha.ensisa.ff.todo_auto.dao.dummy;


public abstract class DummyIdentifiable {
	
	public final String id;
	public String name;
	
	public DummyIdentifiable(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
