package util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import math.Vector;
import run.Main;

public class Automaton implements Iterable<AutomatonState>, Serializable {
	private static final long serialVersionUID = -5796614558047576283L;
	private ArrayList<AutomatonState> states = new ArrayList<AutomatonState>();
	public AutomatonState start;

	public String transcribe(String s) {
		return execute(s).y;
	}

	public boolean accepts(String s) {
		return execute(s).x;
	}

	public Pair<Boolean, String> execute(String s) {
		String output = "";
		AutomatonState currentState = start;
		if (currentState != null)
			for (char c : s.toCharArray()) {
				List<Pair<AutomatonState, Character>> l = getTransitionStates(currentState, c);
				if (l.isEmpty()) {
					currentState = null;
					break;
				}
				Pair<AutomatonState, Character> p = l.get((int) (Math.random() * 0.9999f * l.size()));
				currentState = p.x;
				if (p.y != null)
					output += p.y;
//			Transition t = currentState.map.get(c).get((int)(Math.random()*0.9999f*currentState.map.size()));
//			output += t.output;
//			currentState = t.state;

			}
		return new Pair<Boolean, String>(currentState == null ? false : currentState.isFinal, output);
	}

	public void addState(AutomatonState s) {
		if (states.isEmpty())
			start = s;
		states.add(s);
	}

	public void deleteState(AutomatonState s) {
		states.remove(s);
		forEach((st) -> st.transitions.remove(s));
//		forEach((st)->st.map.values().forEach((l)->l.removeIf((t)->t.state == s)));
	}

	public boolean addTransition(AutomatonState s, AutomatonState s2, Character in, Character out) {
		s.transitions.putIfAbsent(s2, new LinkedList<>());
		List<Pair<Character, Character>> l = s.transitions.get(s2);
		Pair<Character, Character> p = new Pair<Character, Character>(in, out);
		if (l.contains(p))
			return false;
		l.add(p);
		return true;
//		if (!states.contains(s))
//			addState(s);
//		s.map.putIfAbsent(c, new ArrayList<Transition>(1));
//		List<Transition> l = s.map.get(c);
//		for (Transition transition : l) {
//			if(transition.state == t.state)
//				return false;
//		}
//		s.map.get(c).add(t);
//		return true;
	}

	public List<Pair<AutomatonState, Character>> getTransitionStates(AutomatonState s, Character c) {
		List<Pair<AutomatonState, Character>> list = new LinkedList<>();
		s.transitions.forEach((st, l) -> l.forEach(p -> {
			if (p.x.equals(c)) {
				list.add(new Pair<AutomatonState, Character>(st, p.y));
			}
		}));
		return list;
	}

	public void deleteTransition(AutomatonState s, AutomatonState s2) {
		s.transitions.remove(s2);
	}

	public void deleteTransition(AutomatonState s, AutomatonState s2, Character c) {
		s.transitions.forEach((s3, l) -> {
			if (s3 == s2) {
				l.removeIf(p -> p.x == c);
				return;
			}
		});
		s.transitions.entrySet().removeIf(e -> e.getValue().isEmpty());
	}

	public void clearTransition(AutomatonState s, AutomatonState s2) {
		s.transitions.get(s2).clear();
	}

	public List<Pair<Character, Character>> getTransition(AutomatonState s, AutomatonState s2) {
		return s.transitions.get(s2);
	}

	@Override
	public Iterator<AutomatonState> iterator() {
		return states.iterator();
	}

	public int getSize() {
		return states.size();
	}

	public void clear() {
		states.clear();
	}

	public void removeEpsilons() {
		boolean b = true;
		while (b) {
			b = false;
			for (AutomatonState s : states) {
				List<Pair<AutomatonState, Pair<Character, Character>>> toAdd = new LinkedList<>();
				s.transitions.forEach((s2, l) -> s2.transitions.forEach((s3, l2) -> l2.forEach(p -> {
					if (p.x == null) {
						l.forEach(p2 -> toAdd.add(new Pair<AutomatonState, Pair<Character, Character>>(s3, p2)));
					}
				})));
				for (Pair<AutomatonState, Pair<Character, Character>> p : toAdd) {
					if (addTransition(s, p.x, p.y.x, p.y.y))
						b = true;
				}
			}
		}
		forEach(s -> forEach(s2 -> deleteTransition(s, s2, null)));
	}

	public void makeDeterministic() {
		removeEpsilons();

		HashMap<HashSet<AutomatonState>, AutomatonState> stateSet = new HashMap<>();
		HashSet<AutomatonState> set = new HashSet<>();
		List<HashSet<AutomatonState>> unfinishedStates = new LinkedList<>(), finishedStates = new LinkedList<>();
		Set<Character> inputSet = getInputSet();
		set.add(start);
		unfinishedStates.add(set);

		stateSet.put(set, start = new AutomatonState(start.isFinal, new HashMap<>(), new Vector(0, 0), start.name));

		while (!unfinishedStates.isEmpty()) {
			HashSet<AutomatonState> current = unfinishedStates.remove(0);
			AutomatonState currentState = stateSet.get(current);
			for (Character input : inputSet) {
				HashSet<AutomatonState> next = new HashSet<>();
				Character output = null;
				for (AutomatonState s : current) {
					Set<AutomatonState> set2 = getStates(s, input);
					AutomatonState state = set2.stream().findAny().orElse(null);
					if (state != null) {
						List<Pair<Character, Character>> l = state.transitions.get(s);
						if (l != null && !l.isEmpty()) {
							output = l.get(0).y;
						}
					}
					next.addAll(set2);
				}
				stateSet.putIfAbsent(next, new AutomatonState(false, new HashMap<>(), new Vector(0, 0),
						next.stream().map(a -> a.name).reduce((a, b) -> a + "," + b).orElse("Error")));
				AutomatonState nextState = stateSet.get(next);
				nextState.isFinal = nextState.isFinal || next.stream().anyMatch(s -> s.isFinal);
				addTransition(currentState, nextState, input, output);
				if (!finishedStates.contains(next)) {
					unfinishedStates.add(next);
					finishedStates.add(next);
				}
			}
		}

		clear();
		states.addAll(stateSet.values());
		setPositions();

	}

	public void minimize() {
		makeDeterministic();
		// the set contains all possible pairs, that are not proven to be unequal
		HashSet<Set<AutomatonState>> set = new HashSet<>();
		forEach(s -> forEach(s2 -> {
			if (s != s2 && s.isFinal == s2.isFinal)
				set.add(new HashSet<>(Arrays.asList(s, s2)));
		}));

		Set<Character> InputSet = getInputSet();
		int initialSize;
		do {
			initialSize = set.size();
			loop: for (Iterator<Set<AutomatonState>> it = set.iterator(); it.hasNext();) {
				Set<AutomatonState> set2 = (Set<AutomatonState>) it.next();
				for (Character c : InputSet) {
					Set<AutomatonState> set3 = set2.stream().map(s -> getState(s, c)).collect(Collectors.toSet());
					if (set3.size() != 1 && !set.contains(set3)) {
						it.remove();
						continue loop;
					}
				}
			}
		} while (initialSize != set.size());

		for (Set<AutomatonState> s : set) {
			List<AutomatonState> l = s.stream().collect(Collectors.toList());
			mergeStates(l.get(0), l.get(1));
		}
		setPositions();
	}

	private void mergeStates(AutomatonState s, AutomatonState s2) {

		if (start == s) {
			AutomatonState temp = s;
			s = s2;
			s2 = temp;
		}

		for (AutomatonState st : states) {
			List<Pair<Character, Character>> l = st.transitions.remove(s);
			if (l != null) {
				st.transitions.putIfAbsent(s2, new LinkedList<>());
				st.transitions.get(s2).addAll(l);
			}
		}

		deleteState(s);
	}

	private void setPositions() {
		int posI = 0;
		for (AutomatonState s : states) {

			s.position = new Vector((posI++ % 4 + 1), (int) (posI++ / 4 + 1)).scale(Main.stateRadius * 2.5f);
		}
	}

	public Set<Character> getInputSet() {
		HashSet<Character> set = new HashSet<>();
		forEach(s -> s.transitions
				.forEach((s2, l) -> set.addAll(l.stream().map(p -> p.x).collect(Collectors.toSet()))));
		return set;
	}

	public Set<AutomatonState> getStates(AutomatonState s, Character input) {
		HashSet<AutomatonState> set = new HashSet<>();
		s.transitions.forEach((st, l) -> {
			if (l.stream().anyMatch(p -> p.x.equals(input))) {
				set.add(st);
			}
		});
		return set;
	}

	public AutomatonState getState(AutomatonState s, Character input) {
		return getStates(s, input).stream().findAny().orElse(null);
	}

	public void save(String name) {
		try {
			FileOutputStream fos;
			ObjectOutputStream oos;
			fos = new FileOutputStream(directory(name));
			oos = new ObjectOutputStream(fos);
			oos.writeObject(this);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Automaton load(String name) {
		FileInputStream fis;
		try {
			fis = new FileInputStream(directory(name));
			ObjectInputStream ois = new ObjectInputStream(fis);
			Automaton a = (Automaton) ois.readObject();
			ois.close();
			return a;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String directory(String name) {
		return "saves/" + name + (name.contains(".") ? "" : ".dat");
	}
}
