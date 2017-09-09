package lab4;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class IntroductionPanel extends JFrame{
	
	LogicClass shouldContinue;
	LogicClass shouldExit;
	
	public IntroductionPanel(LogicClass shouldContinue, LogicClass shouldExit){
		this.shouldContinue = shouldContinue;
		this.shouldExit = shouldExit;
	}
	
	private JTextArea[] setInputs(){
		JTextArea inputs[] = new JTextArea[4];
		for (int i = 0;  i < inputs.length; i++){
			inputs[i] = new JTextArea();
		}
		return inputs;
	}
	
	private JLabel[] setDescriptionLabels(){
		JLabel descriptionLabels[] = new JLabel[6];
		String descriptions[] = {"sender","receiver","your name","your surname","receiver name","receiver surname"};
		for (int i = 0; i< descriptions.length; i++) {
			descriptionLabels[i] = new JLabel(descriptions[i]); 
		}
		return descriptionLabels;
	}
	
	private JPanel getBoxPanel(int start, 
							int end, 
							JLabel[] descriptionLabels,
							JTextArea[] inputs,
							JButton button,
							JLabel mainLabel){
		JPanel parent = new JPanel();
		JPanel labelPane = new JPanel();
		labelPane.add(mainLabel);
		parent.setLayout(new BoxLayout(parent,BoxLayout.PAGE_AXIS));
		parent.add(labelPane);
		for (int input = start, description = start + 2; input < end; input ++, description++){
			JPanel row = new JPanel();
			row.setLayout(new BoxLayout(row, BoxLayout.LINE_AXIS));
			row.add(descriptionLabels[description]);
			row.add(inputs[input]);
			parent.add(row);
		}
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(button);
		parent.add(buttonPanel);
		return parent;
	}
		
	public void introduce(){
		JFrame parentWindow = this; //to easily disable it later
		LogicClass registrationSuccessful = new LogicClass(false);
		LogicClass receiverGotSuccessgully = new LogicClass(false);
		Container contentPane = getContentPane();
		JTextArea inputs[] = setInputs();
		JLabel descriptionLabels[] = setDescriptionLabels();
		JPanel left = new JPanel(new GridLayout(1,2));
		JPanel right = new JPanel(new BorderLayout());
		JPanel parent = new JPanel(new GridLayout(1,2));
		JTextArea serverInfo = new JTextArea("Type your personal data to the fields to the right. If you do not exist in database, you will be added.Then type the user you want to speak with. He must exist!");
		serverInfo.setLineWrap(true);
		serverInfo.setEditable(false);
		JButton next = new JButton(new AbstractAction("Move to session"){
			public void actionPerformed(ActionEvent evt){
				shouldContinue.set(false);
				parentWindow.setVisible(false);
				parentWindow.disable();
			}
		});
		next.setEnabled(false);
		
		JButton getReceiver = new JButton(new AbstractAction("get receiver"){
			public void actionPerformed(ActionEvent evt){
				String name = inputs[2].getText();
				String surname = inputs[3].getText();
				if (name != null && surname != null){
					if(!name.equals("") && ! surname.equals("")){
						serverInfo.setText(registrationSuccessful.line);
						receiverGotSuccessgully.set(App.getReceiver(registrationSuccessful.line, name, surname));
						if(receiverGotSuccessgully.get()){
							shouldContinue.line += name + " " + surname; // remember queue name after you exit this panel
							System.out.println(shouldContinue.line);
							next.setEnabled(true);
							this.setEnabled(false);
						}
						else
							serverInfo.setText("Receiver does not exist in database!");
					}
					else
						serverInfo.setText("Fill gaps!");
				}
				else 
					serverInfo.setText("Error");
			}
		});		
		getReceiver.setEnabled(false);
		JButton logIn = new JButton(new AbstractAction("Log in"){
			public void actionPerformed(ActionEvent evt){
				String name = inputs[0].getText();
				String surname = inputs[1].getText();
				if (name != null && surname != null){
					if(!name.equals("") && ! surname.equals("")){
						App.logIn(name, surname);
						registrationSuccessful.line = name + surname;
						shouldContinue.line = name + " " + surname + " ";
						registrationSuccessful.set(true);
						getReceiver.setEnabled(true);
						this.setEnabled(false);
					}
					else
						serverInfo.setText("Fill gaps!");
				}
				else 
					serverInfo.setText("Error");
			}
		});	
		
		JButton Exit = new JButton(new AbstractAction("Exit"){
			public void actionPerformed(ActionEvent evt){
				shouldExit.set(true);
				System.exit(0);
			}
		});	
		JPanel senderPart = getBoxPanel(0, 2, descriptionLabels, inputs, logIn, descriptionLabels[0]);
		JPanel receiverPart = getBoxPanel(2, 4, descriptionLabels, inputs, getReceiver, descriptionLabels[1]);
		JPanel overLeft = new JPanel(new BorderLayout());
		overLeft.add(left,BorderLayout.NORTH);
		left.add(senderPart);
		left.add(receiverPart);
		right.add(serverInfo,BorderLayout.CENTER);
		right.add(next,BorderLayout.SOUTH);
		right.add(Exit,BorderLayout.NORTH);
		parent.add(overLeft);
		parent.add(right);
		contentPane.add(parent);
		pack();
    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	setVisible(true);
    	setSize(800, 500);
	}
}
