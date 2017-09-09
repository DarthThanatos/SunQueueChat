package lab4;

import java.util.Arrays;
import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;


public class Server {
	
	private static Hashtable env;
	private static Context ctx;
	private static ConnectionFactory myFactory;
	private static Connection  myConnection;
	private static javax.jms.Session  mySession;
	private static Destination myDest;
	private static MessageConsumer  myConsumer;
	private static UsersDataBase dataBase;
	
	public static void init(){
		dataBase = new UsersDataBase();
		String  CF_LOOKUP_NAME = "MyConnectionFactory";
		env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
		env.put(Context.PROVIDER_URL,"file:///C:/imq_admin_objects");
		try {
			ctx = new InitialContext(env);
			myFactory =  (ConnectionFactory) ctx.lookup(CF_LOOKUP_NAME);
			myConnection = myFactory.createConnection();
			mySession  = myConnection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
			myDest = new com.sun.messaging.Queue("myDest");
			myConsumer = mySession.createConsumer(myDest);
			myConnection.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private static void register(String name, String surname){
		dataBase.addUserToDB(name, surname);
	}
	
	private static void sendHistory(String[] what_to_do){
		// message in the form "getHistory " + queueName + " " + receiverName + " " + receiverSurname
		String history = dataBase.getHistory(what_to_do[1], what_to_do[2], what_to_do[3]);
		javax.jms.Session backSession;
		try {
			backSession = myConnection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
			Destination  queueDest = new com.sun.messaging.Queue(what_to_do[1]);
			MessageProducer producer = backSession.createProducer(queueDest);
			TextMessage outMsg = mySession.createTextMessage(history);
			producer.send(outMsg);
			backSession.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	static class Notifier implements Runnable{
		
		String queueName;
		String line;
		
		public Notifier(String queueName, String line){
			this.queueName = queueName;
			this.line = line;
		}
		
		public void run(){
			javax.jms.Session backSession;
			try {
				backSession = myConnection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
				Destination  queueDest = new com.sun.messaging.Queue(queueName); 
				// ^ new convention: observers and notifiers have different connection than main processes   
				MessageProducer producer = backSession.createProducer(queueDest);
				TextMessage outMsg = mySession.createTextMessage(line);
            	System.out.println("server thread Trying to send on " + queueName + " msg: " + line);
				producer.send(outMsg);
				backSession.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static String getObserverQueueName(String [] what_to_do){
		String queueName = "";
		String index = what_to_do [3] + what_to_do[4];
		String [] parts = new String[4];
		for (int i = 0; i < 4; i++){
			parts[i] = what_to_do[i+1];
		}
		Arrays.sort(parts);
		for (String part: parts) queueName += part;
		return queueName + index;
	}
	
	private static void send(String[] what_to_do){
		// message in the form "send " + name + " " + surname + " " + receiverName + " " + receiverSurname + " " + line
		String msg = "";
		for (int i = 5; i<what_to_do.length; i++) 
			msg += what_to_do[i] + " ";
		dataBase.update(what_to_do[1] + what_to_do[2],what_to_do[3], what_to_do[4], msg);
		dataBase.update(what_to_do[3] + what_to_do[4],what_to_do[1], what_to_do[2],msg);
		Notifier notifier = new Notifier(getObserverQueueName(what_to_do),msg);
		Thread thread = new Thread(notifier);
		thread.start();
	}
	
	private static void verifyIfExists(String what_to_do[]){
		// message in the form "getReceiver " + queueName + " " + name + " " + surname
		javax.jms.Session backSession;
		try {
			backSession = myConnection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
			Destination  queueDest = new com.sun.messaging.Queue(what_to_do[1]);
			MessageProducer producer = backSession.createProducer(queueDest);
			if (dataBase.userExists(what_to_do[2], what_to_do[3])){
				TextMessage  outMsg = mySession.createTextMessage("True");
				producer.send(outMsg);
			}
			else{
				TextMessage  outMsg = mySession.createTextMessage("False");
				producer.send(outMsg);
			}
			backSession.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String argv[]){
		init();
		while(true){
			try {
				Message inMsg = myConsumer.receive();
				TextMessage  txtMsg = (TextMessage) inMsg;
				System.out.println("Got: " + txtMsg.getText());
				String [] what_to_do = txtMsg.getText().split(" ");
				
				switch(what_to_do[0]){
					case "logIn": {
						register(what_to_do[1], what_to_do[2]);
						break;
					}
					case "getReceiver":{
						verifyIfExists(what_to_do);
						break;
					}
					case "getHistory": {
						sendHistory(what_to_do);
						break;
					}
					case "send":{
						send(what_to_do);
						break;
					}
					default: 
						break;
				}  
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
}
