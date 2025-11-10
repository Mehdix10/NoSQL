package fr.uha.ensisa.ff.todo_auto.dao;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import fr.uha.ensisa.ff.todo_auto.dao.TodoDAO;
import fr.uha.ensisa.ff.todo_auto.dao.UnknownListException;
import fr.uha.ensisa.ff.todo_auto.dao.UnknownUserException;
import fr.uha.ensisa.ff.todo_auto.dao.UserAlreadyExistsException;

public class MongoDAO implements TodoDAO {

	private MongoClient client;
	private MongoDatabase database;
	private final String defaultTaskList = "default";

	private final static String dbName = "todo-one-enr";
	private final static String defaultListName = "default List";

	public class TaskList {
		private String id;
		private String name;
		private ArrayList<TreeMap<String, Object>> tasks;

		public TaskList(String id, String name) {
			this.setId(id);
			this.setName(name);
			setTasks(new ArrayList<TreeMap<String, Object>>());
		}

		public TaskList(String id, String name, ArrayList<TreeMap<String, Object>> tasks) {
			this.setId(id);
			this.setName(name);
			this.setTasks(tasks);
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public ArrayList<TreeMap<String, Object>> getTasks() {
			return tasks;
		}

		public void setTasks(ArrayList<TreeMap<String, Object>> tasks) {
			this.tasks = tasks;
		}
	}

	public MongoDAO(String uri) {
		client = MongoClients.create(uri);
		database = client.getDatabase(MongoDAO.dbName);

		// Enforcing connection is up and running
//		Bson command = new BsonDocument("ping", new BsonInt64(1));
//		Document commandResult = database.runCommand(command);
//		System.out.println("Reached MongoDB : " + commandResult);
	}

	@Override
	public void close() throws Exception {
		if (this.client != null) {
			this.client.close();
			this.client = null;
		}
	}

	@Override
	public void registerUser(String user, String password) throws UserAlreadyExistsException {
		Document userDoc = new Document().append("_id", user).append("pwd", password).append("lists",
				new ArrayList<Map<String, Object>>());
		try {
			this.database.getCollection("users").insertOne(userDoc);
		} catch (MongoWriteException error) {
			if (error.getCode() == 1100)
				throw new UserAlreadyExistsException(user);
		}
	}

	@Override
	public String getUserPassword(String user) throws UnknownUserException {
		Document userDoc = this.database.getCollection("users").find(eq("_id", user))
				.projection(fields(include("pwd"), excludeId())).first();
		if (userDoc == null)
			throw new UnknownUserException("User doesn't exist !!");
		return userDoc.getString("pwd");
	}

	@Override
	public String createDefaultTask(String user, String taskName) throws UnknownUserException {
		final String listId = Double.toString(Math.random());
		final String taskId = Double.toString(Math.random());

		var task = new Document().append("id", taskId).append("name", taskName).append("done", false);

		var tasksArray = new ArrayList<Document>();
		tasksArray.add(task);

		var taskList = new Document().append("id", listId).append("name", MongoDAO.defaultListName).append("tasks",
				tasksArray);

// Push the taskList Document into the "lists" array
		final var update = Updates.push("lists", taskList);
		this.database.getCollection("users").updateOne(eq("_id", user), update);
		return taskId;
	}

	@Override
	public List<Map<String, Object>> getDefaultTasks(String user) throws UnknownUserException {
		Document userDoc = this.database.getCollection("users").find(eq("_id", user))
				.projection(fields(include("tasks"), excludeId())).first();
		List<Map> tasks = userDoc.getList("tasks", Map.class);

		List<Map<String, Object>> res = new ArrayList<>();
		for (Map<String, Object> task : tasks) {
			var tmp = new TreeMap<String, Object>();
			tmp.put("id", task.get("id"));
			tmp.put("name", task.get("name"));
			tmp.put("done", task.get("done"));

			res.add(tmp);
		}
		return res;
	}

	@Override
	public void setDefaultTaskDone(String user, String taskId, boolean done) throws UnknownUserException {
		// TODO : ? We can filter by the user then by the task Id
		var filter = Filters.eq("task.id", taskId);
		var update = Updates.set("tasks.$.done", done);
		this.database.getCollection("users").updateOne(filter, update);
	}

	@Override
	public void renameDefaultTask(String user, String taskId, String newName) throws UnknownUserException {
		var filter = Filters.eq("tasks.id", taskId);
		var update = Updates.set("name", newName);
		this.database.getCollection("users").updateOne(filter, update);

	}

	@Override
	public void deleteDefaultTask(String user, String taskId) throws UnknownUserException {
		var filter = Filters.eq("tasks.id", taskId);
		this.database.getCollection("tasks").deleteOne(filter);

	}

	@Override
	public String createList(String user, String name) throws UnknownUserException {
		final String listId = name + '_' + Math.random();
		final var update = Updates.push("tasks", new TreeMap<String, Object>());
		this.database.getCollection("users").updateOne(eq("_id", user), update);
		return listId;
	}

	@Override
	public List<Map<String, ?>> getLists(String user) throws UnknownUserException {
		var filter = Filters.regex("_id", "^" + user);
		try {

			var lists = this.database.getCollection("list").find(filter).projection(fields(include("_id", "name")));

			List<Map<String, ?>> res = new ArrayList<>();
			for (final var list : lists) {
				var tmp = new TreeMap<>();
				tmp.put("id", list.get("_id").toString());
				tmp.put("name", list.get("name").toString());
				res.add((Map) tmp);
			}
			return res;
		} catch (Exception x) {
			System.err.println(x.getMessage());
			return null;
		}

	}

	@Override
	public void deleteList(String user, String listId) throws UnknownUserException, UnknownListException {
//		var filter = Filters.regex("_id", "^" + user);
		var filter = Filters.eq("_id", listId);
		try {

			this.database.getCollection("list").deleteOne(filter);
		} catch (Exception x) {
			System.err.println("[ERROR]" + x.getMessage());
		}

	}

	@Override
	public void renameList(String user, String listId, String newName)
			throws UnknownUserException, UnknownListException {
		var filter = Filters.eq("_id", listId);
		var update = Updates.set("name", newName);
		try {
			this.database.getCollection("list").updateOne(filter, update);
		} catch (Exception x) {
			System.err.println("[ERROR]" + x.getMessage());
		}
	}

	@Override
	public List<Map<String, Object>> getTasksOfList(String user, String listId)
			throws UnknownUserException, UnknownListException {

		var filter = Filters.regex("_id", "^" + listId);
		var req = this.database.getCollection("tasks").find(filter).projection(fields(include("_id", "name", "done")));
		List<Map<String, Object>> res = new ArrayList<>();
		try {
			for (final var task : req) {
				Map<String, Object> tmp = new TreeMap<>();
				tmp.put("id", task.get("_id"));
				tmp.put("name", task.get("name"));
				tmp.put("done", task.get("done"));
				res.add(tmp);
			}
			return res;
		} catch (Exception x) {
			System.err.println("[ERROR] " + x.getMessage());
			return null;
		}

	}

	@Override
	public String createListTask(String user, String listId, String taskName)
			throws UnknownUserException, UnknownListException {

		String taskId = listId + "_" + Math.random();
		Document newTask = new Document();
		newTask.append("_id", taskId).append("name", taskName).append("done", false);
		try {
			this.database.getCollection("tasks").insertOne(newTask);
			return taskId;
		} catch (Exception x) {
			System.err.println("[ERROR] " + x.getMessage());
			return null;
		}
	}

	@Override
	public void renameListTask(String user, String listId, String taskId, String newTaskName)
			throws UnknownUserException, UnknownListException {
		var filter = Filters.regex("_id", "^" + listId);
		var update = Updates.set("name", newTaskName);
		try {
			this.database.getCollection("tasks").updateOne(filter, update);
		} catch (Exception x) {
			System.err.println("[ERROR] " + x.getMessage());
		}

	}

	@Override
	public void setListTaskDone(String user, String listId, String taskId, boolean done)
			throws UnknownUserException, UnknownListException {
//		var filter = Filters.regex("_id", "^" + listId);
		var filter = Filters.eq("_id", taskId);
		var update = Updates.set("done", done);
		try {
			this.database.getCollection("tasks").updateOne(filter, update);
		} catch (Exception x) {
			System.err.println("[ERROR] " + x.getMessage());
		}
	}

	@Override
	public void deleteListTask(String user, String listId, String taskId)
			throws UnknownUserException, UnknownListException {
//		var filter = Filters.regex("_id", "^" + listId);
		var filter = Filters.eq("_id", taskId);
		try {
			this.database.getCollection("tasks").deleteOne(filter);
		} catch (Exception x) {
			System.err.println("[ERROR] " + x.getMessage());
		}

	}

}