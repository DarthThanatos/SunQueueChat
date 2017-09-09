package lab4;

import javax.jms.*;
import javax.naming.*;

import java.util.Hashtable;

public class Session {

	public String getHistory(String queueName, String receiverName, String receiverSurname){
		String history = "";
		try{
			Hashtable  env = new Hashtable();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
			env.put(Context.PROVIDER_URL,"file:///C:/imq_admin_objects");
			Context  ctx = new InitialContext(env);
			String  CF_LOOKUP_NAME = "MyConnectionFactory";
			ConnectionFactory  myFactory =  (ConnectionFactory) ctx.lookup(CF_LOOKUP_NAME);
			Connection  myConnection = myFactory.createConnection();
			javax.jms.Session  mySession = myConnection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
			Destination  myDest = new com.sun.messaging.Queue("myDest");
			MessageProducer  myProducer = mySession.createProducer(myDest);
			myConnection.start();
			TextMessage  outMsg = mySession.createTextMessage("");
			Message inMsg;
			javax.jms.Session  backSession = myConnection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
			Destination  queueDest = new com.sun.messaging.Queue(queueName);
			MessageConsumer receiver = backSession.createConsumer(queueDest);
			System.out.println("Session getHistory " + queueName + " " + receiverName + " " + receiverSurname);
			outMsg.setText("getHistory " + queueName + " " + receiverName + " " + receiverSurname);
			myProducer.send(outMsg);
			inMsg = receiver.receive();
			TextMessage  txtMsg = (TextMessage) inMsg;
			history = txtMsg.getText();
			
			backSession.close();
			mySession.close();
			myConnection.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return history;
	}
	
	public void send(String name, String surname, String receiverName, String receiverSurname, String line){
		try{
			Hashtable  env = new Hashtable();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
			env.put(Context.PROVIDER_URL,"file:///C:/imq_admin_objects");
			Context  ctx = new InitialContext(env);
			String  CF_LOOKUP_NAME = "MyConnectionFactory";
			ConnectionFactory  myFactory =  (ConnectionFactory) ctx.lookup(CF_LOOKUP_NAME);
			Connection  myConnection = myFactory.createConnection();
			javax.jms.Session  mySession = myConnection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
			Destination  myDest = new com.sun.messaging.Queue("myDest");
			MessageProducer  myProducer = mySession.createProducer(myDest);
			myConnection.start();
			TextMessage  outMsg = mySession.createTextMessage("");
			outMsg.setText("send " + name + " " + surname + " " + receiverName + " " + receiverSurname + " " + line);
			myProducer.send(outMsg);
			
			mySession.close();
			myConnection.close();
		}catch(Exception e){
			e.printStackTrace();
		}		
	}
	
}
