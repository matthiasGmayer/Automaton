package util;

public class OldAutomaton {
	private OldAutomatonState[] states;
	private String[] links;
	
	public OldAutomatonState currentState;
	
	public OldAutomaton(String[] links, OldAutomatonState...automatonStates) {
		states = automatonStates;
		for (int i = 0; i < states.length; i++) {
			states[i].Setup(states);
		}
		this.links = links;
		currentState = states[0];
	}
	
	public void exec(String...inputs) {
		
		
		for (int i = 0; i < inputs.length; i++) {
			for (int j = 0; j < links.length;j++) {
				if(inputs[i].equals(links[j]))
					currentState = states[currentState.GetStateId(j)];
			}
		}
	}
	
	
	
	public static void main(String[] args) {
		OldAutomaton a = new OldAutomaton(new String[]{"a", "b"},
									new OldAutomatonState("Z0", "Z0", "Z1"),
									new OldAutomatonState("Z1", "Z0", "Z2"),
									new OldAutomatonState("Z2", "Z0", "Z2"));
		a.exec("abbbbababaab".split(""));
		System.out.println(a.currentState.name);
	}
	
	
}
