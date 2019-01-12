package util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import math.Vector;

public class AutomatonState implements Serializable {
	private static final long serialVersionUID = -7403297805007381161L;
	public boolean isFinal;
	public HashMap<AutomatonState, List<Pair<Character,Character>>> transitions;
	public Vector position;
	public String name;
	public AutomatonState(boolean isFinal, HashMap<AutomatonState, List<Pair<Character, Character>>> transitions,
			Vector position, String name) {
		super();
		this.isFinal = isFinal;
		this.transitions = transitions;
		this.position = position;
		this.name = name;
	}

	

//	public List<Pair<Character, Character>> getTansition(AutomatonState s) {
//		List<Pair<Character, Character>> list = new LinkedList<>();
//		map.forEach((c, l) -> l.forEach((t) -> {
//			if (t.state == s)
//				list.add(new Pair<Character, Character>(c, t.output));
//		}));
//		return list;
//	}
	public AutomatonState clone() {
		return new AutomatonState(isFinal, transitions, position, name);
	}
}
