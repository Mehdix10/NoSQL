package fr.uha.ensisa.ff.todo_auto.dao;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private static int taskCounter = 0;

	public MongoDAO(String uri) {
		client = MongoClients.create(uri);
		database = client.getDatabase("todo");

		// Enforcing connection is up and running
		Bson command = new BsonDocument("ping", new BsonInt64(1));
		Document commandResult = database.runCommand(command);
		System.out.println("Reached MongoDB : " + commandResult);
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
		Document userDoc = new Document().append("_id", user).append("pwd", password);
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
		final String id = user + '_' + Math.random();
		Document taskDoc = new Document().append("_id", id)
//				.append("_id",this.defaultTaskList)
//				.append("user", user)
				.append("task", taskName).append("done", false);

		try {
			this.database.getCollection("tasks").insertOne(taskDoc);
			return id;
		} catch (MongoWriteException error) {
			if (error.getCode() == 1100)
				throw new Error("Task " + taskName + " already exist for this user " + user);
		}
		return id;
	}

	@Override
	public List<Map<String, Object>> getDefaultTasks(String user) throws UnknownUserException {
		// TODO Auto-generated method stub
		FindIterable<Document> tasksDoc = this.database.getCollection("tasks")
				// If we use the regex expressions and we filter by the first chars in the _id
				// the mongo db will search
				// untill it found a different string than the regex, that is because the data
				// is ordred by _id
				.find(regex("_id", "^" + user)).projection(fields(include("_id", "task", "done")));
		List<Map<String, Object>> res = new ArrayList<>();
		for (var task : tasksDoc) {
			var tmp = new HashMap<String, Object>();
			tmp.put("id", task.getString("_id"));
			tmp.put("name", task.getString("task"));
			try {
				tmp.put("done", task.getBoolean("done"));
			} catch (Exception ex) {
				tmp.put("done", task.getString("done"));
			}

			res.add(tmp);
		}
		return res;
	}

	@Override
	public void setDefaultTaskDone(String user, String taskId, boolean done) throws UnknownUserException {
		var filter = Filters.eq("_id", taskId);
		var update = Updates.set("done", done);
		this.database.getCollection("tasks").updateOne(filter, update);
	}

	@Override
	public void renameDefaultTask(String user, String taskId, String newName) throws UnknownUserException {
 		var filter = Filters.eq("_id", taskId);
		var update = Updates.set("task", newName);
		this.database.getCollection("tasks").updateOne(filter, update);

	}

	@Override
	public void deleteDefaultTask(String user, String taskId) throws UnknownUserException {
		var filter = Filters.eq("_id", taskId);
		this.database.getCollection("tasks").deleteOne(filter);

	}

	@Override
	public String createList(String user, String name) throws UnknownUserException {
		final String listId = user+Math.random();
		Document listDoc = new Document().append("_id", listId).append("name", name);
		try {
			this.database.getCollection("list").insertOne(listDoc);
			return listId;
		} catch (MongoWriteException error) {
			throw new UnknownUserException(name);
		}
	}

	@Override
	public List<Map<String, ?>> getLists(String user) throws UnknownUserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteList(String user, String listId) throws UnknownUserException, UnknownListException {
		// TODO Auto-generated method stub

	}

	@Override
	public void renameList(String user, String listId, String newName)
			throws UnknownUserException, UnknownListException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Map<String, Object>> getTasksOfList(String user, String listId)
			throws UnknownUserException, UnknownListException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createListTask(String user, String listId, String taskName)
			throws UnknownUserException, UnknownListException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void renameListTask(String user, String listId, String taskId, String newTaskName)
			throws UnknownUserException, UnknownListException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setListTaskDone(String user, String listId, String taskId, boolean done)
			throws UnknownUserException, UnknownListException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteListTask(String user, String listId, String taskId)
			throws UnknownUserException, UnknownListException {
		// TODO Auto-generated method stub

	}

}