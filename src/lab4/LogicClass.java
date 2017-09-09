package lab4;

public class LogicClass {
	
	private boolean value;
	public String line;
	
	public LogicClass(boolean value){
		this.value = value;
	}
	
	public void set(boolean value){
		this.value = value;
	}
	
	public boolean get(){
		return value;
	}
}
