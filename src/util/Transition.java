package util;

public class Transition {
	public final AutomatonState state;
	public final char output;

	public Transition(AutomatonState state, char output) {
		super();
		this.state = state;
		this.output = output;
	}

}
