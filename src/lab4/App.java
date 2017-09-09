package lab4;

import javax.jms.*;
import javax.naming.*;

import java.util.Hashtable;

public class App {
	
	private static Hashtable env;
	private static Context ctx;
	private static ConnectionFactory myFactory;
	private static Connection  myConnection;
	private static javax.jms.Session  mySession;
	private static Destination myDest;
	private static MessageProducer  myProducer;
	
	public static boolean getReceiver(String queueName, String name, String surname){
		boolean exists = false;
		try{ 
			String  CF_LOOKUP_NAME = "MyConnectionFactory";
			env = new Hashtable();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
			env.put(Context.PROVIDER_URL,"file:///C:/imq_admin_objects");
			ctx = new InitialContext(env);
			myFactory =  (ConnectionFactory) ctx.lookup(CF_LOOKUP_NAME);
			myConnection = myFactory.createConnection();
			mySession  = myConnection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
			myDest = new com.sun.messaging.Queue("myDest");
			myProducer = mySession.createProducer(myDest);
			myConnection.start();
			
			TextMessage  outMsg = mySession.createTextMessage("");
			javax.jms.Session  backSession = myConnection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
			Destination  queueDest = new com.sun.messaging.Queue(queueName);
			MessageConsumer receiver = backSession.createConsumer(queueDest);
			String msg =  "getReceiver " + queueName + " " + name + " " + surname;
			outMsg.setText(msg);
			myProducer.send(outMsg);
			Message inMsg = receiver.receive();
			TextMessage  txtMsg = (TextMessage) inMsg;
			switch(txtMsg.getText()){
				case "True": {
					System.out.println("client: true");
					exists = true;
					break;
				}
				default: {
					System.out.println("client: false");
					exists = false;
					break;
				}
			}    
			backSession.close();			
			mySession.close();
		}
		catch (Exception jmse){ 
			System.out.println("Exception occurred: " + jmse.toString() );
			jmse.printStackTrace();
		}
		return exists;
	}
	
	public static void logIn(String name, String surname){
		try{  
			String  CF_LOOKUP_NAME = "MyConnectionFactory";
			env = new Hashtable();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
			env.put(Context.PROVIDER_URL,"file:///C:/imq_admin_objects");
			ctx = new InitialContext(env);
			myFactory =  (ConnectionFactory) ctx.lookup(CF_LOOKUP_NAME);
			myConnection = myFactory.createConnection();
			mySession  = myConnection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
			myDest = new com.sun.messaging.Queue("myDest");
			myProducer = mySession.createProducer(myDest);
			myConnection.start();
			
			TextMessage  outMsg = mySession.createTextMessage("");
			String msg = "logIn " + name + " " + surname;
			outMsg.setText(msg);
			myProducer.send(outMsg);
			mySession.close();
		}
		catch (Exception jmse){ 
			System.out.println("Exception occurred: " + jmse.toString() );
			jmse.printStackTrace();
		}
	}
	
	public static void main(String argv[]){
		LogicClass shouldContinue = new LogicClass(true);
		LogicClass Exit = new LogicClass(false);
		IntroductionPanel intro = null;
		SessionPanel sessionPanel = null;
		while(!Exit.get()){
			shouldContinue.set(true);
			intro = new IntroductionPanel(shouldContinue,Exit);
			intro.introduce();
			while(shouldContinue.get());
			shouldContinue.set(true);
			sessionPanel = new SessionPanel(shouldContinue);
			System.out.println("After: " + shouldContinue.line);
			sessionPanel.visualize();
			while(shouldContinue.get());
		}
	}
}
