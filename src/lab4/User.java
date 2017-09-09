package lab4;

import java.util.HashMap;

public class User {
	private String name;
	private String surname;
	private HashMap<String,String> history = new HashMap<String,String>(); 
	
	public User(String name, String surname){
		this.name = name;
		this.surname = surname;
	}
	
	public String getName(){
		return name ;
	}
	
	public void addToHistory(String key, String line){
		String passedHistory = getHistory(key);
		history.put(key, passedHistory + line);
		System.out.println("Put " + line + " to DB");
	}
	
	public String getHistory(String key){
		if (!history.containsKey(key)){
			history.put(key, "");
		}
		return history.get(key);
	}
	
	public String getSurname(){
		return surname;
	}
	
}
