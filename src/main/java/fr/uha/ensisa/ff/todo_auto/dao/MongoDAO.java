package fr.uha.ensisa.ff.todo_auto.dao;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.internal.client.model.FindOptions;

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
				new ArrayList<Document>());
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
		final String listId = this.defaultTaskList;
		final String taskId = Double.toString(Math.random());

		Document taskDoc = new Document().append("id", taskId).append("name", taskName).append("done", false);

		// User filter
		var userFilter = Filters.eq("_id", user);
		// Array of lists filter
		var listFilter = Filters.eq("list.id", listId);

		// Update options to specify that we going to modify the array
		var options = new UpdateOptions().arrayFilters(Arrays.asList(listFilter));

		// The update action touched the array
		var taskPush = Updates.push("lists.$[list].tasks", taskDoc);
		UpdateResult result = null;
		result = this.database.getCollection("users").updateOne(userFilter, taskPush, options);
//		System.out.println("INFO "+ result.getModifiedCount());
		if (result.getModifiedCount() == 0) {
			Document listDoc = new Document().append("id", listId).append("name", listId).append("tasks",
					new ArrayList<Document>());
			var listPush = Updates.push("lists", listDoc);
			result = this.database.getCollection("users").updateOne(userFilter, listPush);
//			System.out.println("INFO "+ result.getModifiedCount());
			if (result.getModifiedCount() > 0)
				this.database.getCollection("users").updateOne(userFilter, taskPush, options);
			else
				System.err.println("[ERROR] Cannot push the list to the user " + user);
		}
		return taskId;
	}

	@Override
	public List<Map<String, Object>> getDefaultTasks(String user) throws UnknownUserException {
		var userFilter = Filters.eq("_id", user);
		var listFilter = Filters.eq("lists.id", this.defaultTaskList);

		var userDocList = this.database.getCollection("users").find(Filters.and(userFilter, listFilter))
				.projection(fields(include("lists.$"))).first();

		// En création le user ne contient pas de list
		if (userDocList == null)
			return null;

		return (List<Map<String, Object>>) userDocList.get("lists");

//		var res = new ArrayList<Map<String, Object>>();
//
//		for (final var task : taskList.getList("tasks", Document.class)) {
//			var map = new TreeMap<String, Object>();
//			map.put("id", task.getString("id"));
//			map.put("name", task.getString("name"));
//			map.put("done", task.getBoolean("done"));
//			res.add(map);
//		}
//		return res;
	}

	@Override
	public void setDefaultTaskDone(String user, String taskId, boolean done) throws UnknownUserException {
		var userFilter = Filters.eq("_id", user);

		var updateOptions = new UpdateOptions()
				.arrayFilters(List.of(Filters.eq("list.id", this.defaultTaskList), Filters.eq("task.id", taskId)));

		var update = Updates.set("lists.$[list].tasks.$[task].done", done);

		this.database.getCollection("users").updateOne(userFilter, update, updateOptions);
	}

	@Override
	public void renameDefaultTask(String user, String taskId, String newName) throws UnknownUserException {
		var userFilter = Filters.eq("_id", user);

		var updateOptions = new UpdateOptions()
				.arrayFilters(List.of(Filters.eq("list.id", this.defaultTaskList), Filters.eq("task.id", taskId)));

		var update = Updates.set("lists.$[list].tasks.$[task].name", newName);

		this.database.getCollection("users").updateOne(userFilter, update, updateOptions);

	}

	@Override
	public void deleteDefaultTask(String user, String taskId) throws UnknownUserException {
		var userFilter = Filters.eq("_id", user);

		var updateOptions = new UpdateOptions().arrayFilters(List.of(Filters.eq("list.id", this.defaultTaskList)));

		var update = Updates.pull("lists.$[list].tasks", Filters.eq("id", taskId));

		this.database.getCollection("users").updateOne(userFilter, update, updateOptions);

	}

	@Override
	public String createList(String user, String name) throws UnknownUserException {
		final String listId = this.defaultTaskList;

		// User filter
		var userFilter = Filters.eq("_id", user);

		Document listDoc = new Document().append("id", listId).append("name", name).append("tasks",
				new ArrayList<Document>());
		var listPush = Updates.push("lists", listDoc);
		var result = this.database.getCollection("users").updateOne(userFilter, listPush);
		if (result.getModifiedCount() > 0)
			return listId;
		else
			return null;
	}

	@Override
	public List<Map<String, ?>> getLists(String user) throws UnknownUserException {
		var userFilter = Filters.eq("_id", user);

		var userListDoc = this.database.getCollection("users").find(userFilter)
				.projection(include("lists.id", "lists.name")).first();
		return (List<Map<String, ?>>) userListDoc.get("lists");
	}

	@Override
	public void deleteList(String user, String listId) throws UnknownUserException, UnknownListException {
		var userFilter = Filters.eq("_id", user);

		var update = Updates.pull("lists.$", Filters.eq("id", listId));

		this.database.getCollection("users").updateOne(userFilter, update);

	}

	@Override
	public void renameList(String user, String listId, String newName)
			throws UnknownUserException, UnknownListException {
		var userFilter = Filters.eq("_id", user);
		var listFilter = Filters.eq("lists.id", listId);

		var update = Updates.set("lists.$", newName);

		this.database.getCollection("users").updateOne(Filters.and(userFilter, listFilter), update);

	}

	@Override
	public List<Map<String, Object>> getTasksOfList(String user, String listId)
			throws UnknownUserException, UnknownListException {
		var userFilter = Filters.eq("_id", user);
		var listFilter = Filters.eq("lists.id", listId);

		var userDocList = this.database.getCollection("users").find(Filters.and(userFilter, listFilter))
				.projection(fields(include("lists.$"))).first();

		// En création le user ne contient pas de list
		if (userDocList == null)
			return null;

		return (List<Map<String, Object>>) userDocList.get("lists");

	}

	@Override
	public String createListTask(String user, String listId, String taskName)
			throws UnknownUserException, UnknownListException {
		final String taskId = Double.toString(Math.random());

		Document taskDoc = new Document().append("id", taskId).append("name", taskName).append("done", false);

		// User filter
		var userFilter = Filters.eq("_id", user);
		// Array of lists filter
		var listFilter = Filters.eq("list.id", listId);

		// Update options to specify that we going to modify the array
		var options = new UpdateOptions().arrayFilters(Arrays.asList(listFilter));

		// The update action touched the array
		var taskPush = Updates.push("lists.$[list].tasks", taskDoc);
		UpdateResult result = null;
		result = this.database.getCollection("users").updateOne(userFilter, taskPush, options);
//		System.out.println("INFO "+ result.getModifiedCount());
		if (result.getModifiedCount() == 0) {
			Document listDoc = new Document().append("id", listId).append("name", listId).append("tasks",
					new ArrayList<Document>());
			var listPush = Updates.push("lists", listDoc);
			result = this.database.getCollection("users").updateOne(userFilter, listPush);
//			System.out.println("INFO "+ result.getModifiedCount());
			if (result.getModifiedCount() > 0)
				this.database.getCollection("users").updateOne(userFilter, taskPush, options);
			else
				System.err.println("[ERROR] Cannot push the list to the user " + user);
		}
		return taskId;

	}

	@Override
	public void renameListTask(String user, String listId, String taskId, String newTaskName)
			throws UnknownUserException, UnknownListException {
		var userFilter = Filters.eq("_id", user);

		var updateOptions = new UpdateOptions()
				.arrayFilters(List.of(Filters.eq("list.id", listId), Filters.eq("task.id", taskId)));

		var update = Updates.set("lists.$[list].tasks.$[task].name", newTaskName);

		this.database.getCollection("users").updateOne(userFilter, update, updateOptions);

	}

	@Override
	public void setListTaskDone(String user, String listId, String taskId, boolean done)
			throws UnknownUserException, UnknownListException {
		var userFilter = Filters.eq("_id", user);

		var updateOptions = new UpdateOptions()
				.arrayFilters(List.of(Filters.eq("list.id", listId), Filters.eq("task.id", taskId)));

		var update = Updates.set("lists.$[list].tasks.$[task].done", done);

		this.database.getCollection("users").updateOne(userFilter, update, updateOptions);
	}

	@Override
	public void deleteListTask(String user, String listId, String taskId)
			throws UnknownUserException, UnknownListException {
		var userFilter = Filters.eq("_id", user);

		var updateOptions = new UpdateOptions().arrayFilters(List.of(Filters.eq("list.id", listId)));

		var update = Updates.pull("lists.$[list].tasks", Filters.eq("id", taskId));

		this.database.getCollection("users").updateOne(userFilter, update, updateOptions);
	}

}