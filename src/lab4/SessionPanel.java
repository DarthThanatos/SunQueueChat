package lab4;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.jms.*;
import javax.naming.*;

import java.util.Arrays;
import java.util.Hashtable;

public class SessionPanel extends JFrame {
	
	LogicClass shouldContinue;
	String chatBufor = "";
	String name = "", surname = "", receiverName = "", receiverSurname = "";
	JTextArea screen = new JTextArea();
	
	public SessionPanel(LogicClass shouldContinue){
		this.shouldContinue = shouldContinue;
		System.out.println(shouldContinue.line);
		// ^ this object also contains client name and surname, and the receiver's data
	}
	
	class Observer implements Runnable{
		
		String queueName;
		
		public Observer(String queueName){
			this.queueName = queueName;
		}
		
		public void run(){
			try{ 
				Hashtable  env = new Hashtable();
				env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
				env.put(Context.PROVIDER_URL,"file:///C:/imq_admin_objects");
				Context  ctx = new InitialContext(env);
				String  CF_LOOKUP_NAME = "MyConnectionFactory";
				ConnectionFactory  myFactory =  (ConnectionFactory) ctx.lookup(CF_LOOKUP_NAME);
				Connection  myConnection = myFactory.createConnection();
				myConnection.start();
				javax.jms.Session  backSession = myConnection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
				Destination  queueDest = new com.sun.messaging.Queue(queueName);
				MessageConsumer receiver = backSession.createConsumer(queueDest);
	            while(true){
	            	System.out.println("client thread Trying to receive a msg on " + queueName);
					Message  inMsg = receiver.receive();
					TextMessage  txtMsg = (TextMessage) inMsg;
	            	System.out.println("client thread got " + txtMsg.getText());
					modifyBufor(txtMsg.getText());
				}      
			}
			catch (Exception jmse){ 
					System.out.println("Exception occurred: " + jmse.toString() );
					jmse.printStackTrace();
			 }
		}
	}
	
	
	public void modifyBufor(String line){
		final Lock lock = new ReentrantLock();
		final Condition notBlocked  = lock.newCondition(); 
		lock.lock();
		try {
			chatBufor += line;
			screen.setText(chatBufor);
	        notBlocked.signal();
	     } finally {
	       lock.unlock();
	     }
	}
	
	private static String getObserverQueueName(String [] parts){
		String queueName = "";
		String index = parts[0] + parts[1];
		Arrays.sort(parts);
		for (String part: parts) queueName += part;
		return queueName + index;
	}
	
	public void visualize(){
		JFrame parentWindow = this;
		Session session = new Session();
		String[] parts = shouldContinue.line.split(" ");
		name = shouldContinue.line.split(" ")[0];
		surname = shouldContinue.line.split(" ")[1];
		receiverName = shouldContinue.line.split(" ")[2];
		receiverSurname = shouldContinue.line.split(" ")[3];
		Observer observer = new Observer(getObserverQueueName(parts));
		Thread thread = new Thread(observer);
		thread.start();
		chatBufor = session.getHistory(name+surname, receiverName,receiverSurname);
		if (chatBufor != null){
			System.out.println("History: " + chatBufor);
			screen.setText(chatBufor);
		}
		else 
			chatBufor = "";
		JScrollPane scroll = new JScrollPane(screen);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		screen.setEditable(false);
		screen.setLineWrap(true);
		JTextArea chat = new JTextArea();
		JButton logout = new JButton(new AbstractAction("Log Out"){
			public void actionPerformed(ActionEvent evt){
				shouldContinue.set(false);
				parentWindow.setVisible(false);
				parentWindow.disable();
			}
		});	
		JButton send = new JButton(new AbstractAction("Send"){
			public void actionPerformed(ActionEvent evt){
				String line = chat.getText();
				chat.setText("");
				if(line != null && !line.equals("")){
					String sentMsg = name + " " + surname + ": " + line + "\n";
					System.out.println("Sent: " + sentMsg);
					session.send(name, surname, receiverName, receiverSurname, sentMsg);
					modifyBufor(name + " " + surname + ": " + line + "\n");
				}
			}
		});	
		JPanel bottomInterface = new JPanel();
		bottomInterface.setLayout(new BoxLayout(bottomInterface, BoxLayout.PAGE_AXIS));
		bottomInterface.add(chat);
		JPanel sendPanel = new JPanel();
		JLabel nameLabel = new JLabel(name);
		sendPanel.add(nameLabel);
		sendPanel.add(send);
		bottomInterface.add(sendPanel);
		add(bottomInterface,BorderLayout.SOUTH);
		add(scroll,BorderLayout.CENTER);
		add(logout,BorderLayout.NORTH);
		pack();
		setVisible(true);
    	setSize(800, 500);
	}
}
