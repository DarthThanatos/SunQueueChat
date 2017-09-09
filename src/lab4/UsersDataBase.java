package lab4;

import java.util.HashMap;

public class UsersDataBase {
	
	private HashMap<String, User> users;
	
	public UsersDataBase(){
		users = new HashMap<String,User>();
	}
	
	public void update(String key, String receiverName, String receiverSurname, String line){
		User user = users.get(key);
		user.addToHistory(receiverName + receiverSurname, line);
	}
	
	public String getHistory(String key, String receiverName, String receiverSurname){
		User user = users.get(key);
		String history = user.getHistory(receiverName + receiverSurname);
		return history;
	}
	
	public boolean addUserToDB(String name, String surname){
		if (!userExists(name,surname)){
			System.out.println("adding " + name + surname);
			User user = new User(name, surname);
			users.put(name + surname, user);
		}
		return true;
	}
	
	public boolean userExists(String name, String surname){
		System.out.println("users: ");
		for (String key : users.keySet())
			System.out.println(key);
		if (users.containsKey(name+surname))
			return true;
		else return false;
	}
}
