package run;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import graphics.ClickMenu;
import graphics.ClickMenuOption;
import graphics.Graphics;
import math.Circle;
import math.Line;
import math.Matrix;
import math.Rectangle;
import math.Vector;
import util.Automaton;
import util.AutomatonState;
import util.Pair;

public class Main {
	static JFrame frame;
	static JPanel displayPanel, configPanel;
	static JSplitPane splitPane;
	static Graphics g;
	static ClickMenu menu;

	static Automaton automaton = new Automaton();
	static AutomatonState selectedState = null;
	static Pair<AutomatonState, AutomatonState> selectedTransition = null;
	public static final int stateRadius = 50, autosaveMaxCount = 50;
	public static final float transitionOffset = 10;
	static boolean mousePressed, middleMousePressed, makingTransition, accepted;
	static String output;
	static Vector relativeMousePosition;
	static Vector mouse;
	static Vector scroll = new Vector(0, 0), dimension;
	static int autosavePosition = 0, autosaveCount;
	static float maxScrool = 250;

	static {
		Hashtable<ClickMenuOption, Runnable> mapEmpty = new Hashtable<>(), mapState = new Hashtable<>(),
				mapTransition = new Hashtable<>();
		Vector pos = new Vector(10, 10), dim = new Vector(10, 5);

		mapEmpty.put(new ClickMenuOption("Add State", new Rectangle(pos, dim)), () -> {
			automaton.addState(selectedState = new AutomatonState(false, new HashMap<>(), menu.position,
					"Z" + (automaton.getSize())));
			menu.setOptionList(1);
			selectedTransition = null;
			menu.animate(menu.position);
			autoSave();
		});
		mapState.put(new ClickMenuOption("Delete", new Rectangle(pos, dim)), () -> autoSave(() -> {
			automaton.deleteState(selectedState);
		}));
		mapState.put(new ClickMenuOption("Add Transition", new Rectangle(pos, dim)), () -> makingTransition = true);
		mapState.put(new ClickMenuOption("Change Name", new Rectangle(pos, dim)), () -> {
			String s = JOptionPane.showInputDialog("Name:");
			if (s != null)
				selectedState.name = s;
		});
		mapState.put(new ClickMenuOption("Switch Final", new Rectangle(pos, dim)), () -> {
			if (selectedState != null)
				selectedState.isFinal = !selectedState.isFinal;
			autoSave();
		});
		mapState.put(new ClickMenuOption("Make Start", new Rectangle(pos, dim)), () -> autoSave(() -> {
			automaton.start = selectedState;
		}));
		mapTransition.put(new ClickMenuOption("Delete", new Rectangle(pos, dim)), () -> autoSave(() -> {
			automaton.deleteTransition(selectedTransition.x, selectedTransition.y);
		}));
		mapTransition.put(new ClickMenuOption("Change Input", new Rectangle(pos, dim)), () -> {
			String s = JOptionPane.showInputDialog("Only Characters: Input/Output,...");
			if (s == null)
				return;
			automaton.clearTransition(selectedTransition.x, selectedTransition.y);
			if (s == "")
				return;
			Arrays.asList(s.split(",")).forEach(a -> {
				a = a.replaceAll(" ", "");
				String[] arr = a.split("/");
				automaton.addTransition(selectedTransition.x, selectedTransition.y,
						arr[0].length() > 0 ? arr[0].charAt(0) : null, arr.length > 1 ? arr[1].charAt(0) : null);
			});
			autoSave();
		});
		menu = new ClickMenu(new Vector(200, 200), mapEmpty, mapState, mapTransition);
	}

	private static final int WIDTH = 1080, HEIGHT = 720;

	public static void main(String[] args) {
		g = new Graphics();
		frame = new JFrame("Automaton");
		displayPanel = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(java.awt.Graphics g) {
//				g.clearRect(0, 0, WIDTH, HEIGHT);
				g.setColor(Color.WHITE);
				dimension = new Vector(this.getSize().width, this.getSize().height);
				g.fillRect(0, 0, dimension.x.intValue(), dimension.y.intValue());
				Graphics2D graphics2D = (Graphics2D) g;

				// Set anti-alias!
				graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				// Set anti-alias for text
				graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
						RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				Main.g.set(graphics2D);
				draw(Main.g);

			}
		};
		displayPanel.setBackground(Color.white);
		frame.add(displayPanel);

		configPanel = new JPanel();

		JTextField input = new JTextField();
		input.setPreferredSize(new Dimension(150, 30));
		JLabel answer = new JLabel(), answer2 = new JLabel();
		answer.setHorizontalAlignment(JLabel.CENTER);
		answer2.setHorizontalAlignment(JLabel.CENTER);
		answer.setPreferredSize(new Dimension(300, 30));
		answer2.setPreferredSize(new Dimension(300, 30));
		JButton b = new JButton("Execute");
		b.addActionListener((a) -> {
			selectedState = null;
			selectedTransition = null;
			menu.close();
			Pair<Boolean, String> p = automaton.execute(input.getText());
			answer.setText(p.x ? "Accepted" : "Not accepted");
			answer2.setText(p.y);
		});
		JTextField nameField = new JTextField("Untitled");
		nameField.setPreferredSize(new Dimension(100, 30));
		JButton save = new JButton("Save");
		JButton load = new JButton("Load");
		JButton browse = new JButton(">");
		save.addActionListener((a) -> automaton.save(nameField.getText()));
		load.addActionListener(as -> {
			load(nameField.getText());
			menu.close();
		});
		browse.addActionListener(a -> {
			JFileChooser f = new JFileChooser(new File("saves"));
			f.setFileFilter(new FileFilter() {

				@Override
				public String getDescription() {
					return null;
				}

				@Override
				public boolean accept(File f) {
					return !f.getName().contains("autosave");
				}
			});
			int r = f.showOpenDialog(null);
			if (f.getSelectedFile() == null)
				return;
			nameField.setText(f.getSelectedFile().getName());
			if (r == JFileChooser.APPROVE_OPTION) {
				load(nameField.getText());
			}
		});
		JButton back = new JButton("Back"), forward = new JButton("Forward"), clear = new JButton("Clear");
		back.addActionListener(a -> back());
		forward.addActionListener(a -> forward());
		clear.addActionListener(a -> automaton.clear());

		JButton minimize = new JButton("Minimize"), makeDeterministic = new JButton("Make Deterministic"),
				removeEpsilons = new JButton("Remove Epsilons");
		removeEpsilons.addActionListener(l -> autoSave(() -> automaton.removeEpsilons()));
		minimize.addActionListener(a -> autoSave(() -> automaton.minimize()));
		makeDeterministic.addActionListener(a -> autoSave(() -> automaton.makeDeterministic()));

		configPanel.add(input);
		configPanel.add(b);
		configPanel.add(answer);
		configPanel.add(answer2);
		configPanel.add(nameField);
		configPanel.add(browse);
		configPanel.add(save);
		configPanel.add(load);
		configPanel.add(back);
		configPanel.add(forward);
		configPanel.add(clear);
		configPanel.add(minimize);
		configPanel.add(makeDeterministic);
		configPanel.add(removeEpsilons);
		displayPanel.setPreferredSize(new Dimension(1000, 100));

		splitPane = new JSplitPane();
		splitPane.setLeftComponent(configPanel);
		splitPane.setRightComponent(displayPanel);
		splitPane.setDividerLocation(200);
		splitPane.setDividerSize(0);

		frame.add(splitPane);

		frame.setVisible(true);
		frame.setFocusable(true);
		frame.setDefaultCloseOperation(3);
		frame.setSize(WIDTH, HEIGHT);
		frame.setLocationRelativeTo(null);

		displayPanel.addMouseListener(new MouseListener() {

			boolean clicked;

			@Override
			public void mouseReleased(MouseEvent e) {
				clicked = false;
				if (e.getButton() == 1)
					mousePressed = false;
				if (e.getButton() == 2) {
					middleMousePressed = false;
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				mouse = translateMouse(e.getX(), e.getY());
				if (!clicked) {
					clicked(e.getButton());
					clicked = true;
					if (selectedState != null) {
						relativeMousePosition = selectedState.position.sub(mouse);
					}
				}
				if (e.getButton() == 1)
					mousePressed = true;

				if (e.getButton() == 2) {
					middleMousePressed = true;
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseClicked(MouseEvent e) {
//				Vector mouse = translateMouse(e.getX(), e.getY());
//				clicked(mouse, e.getButton());
			}
		});
		displayPanel.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				mouse = translateMouse(e.getX(), e.getY());
				menu.checkClose(mouse);
			}

			@Override
			public void mouseDragged(MouseEvent e) {

				if (middleMousePressed) {
					Vector m = translateMouse(e.getX(), e.getY());
					Vector dif = mouse.sub(m);
					scroll = scroll.sub(dif);
					scroll = new Vector(Math.max(-maxScrool, Math.min(scroll.x, maxScrool)),
							Math.max(-maxScrool, Math.min(scroll.y, maxScrool)));
				}
				mouse = translateMouse(e.getX(), e.getY());
				menu.checkClose(mouse);
				if (selectedState != null && mousePressed && !makingTransition) {
					Vector newPosition = mouse.add(relativeMousePosition);
					boolean b = true;
					for (AutomatonState s : automaton) {
						if (s == selectedState)
							continue;
						if (new Circle(s.position, stateRadius).distance(newPosition) < stateRadius) {
							b = false;
							break;
						}
					}
					if (b) {

						selectedState.position = newPosition;
						menu.position = newPosition;

					}

				}
			}
		});
		long lastTime = System.nanoTime();

		while (true) {
			long time = System.nanoTime();
			float deltaTime = (time - lastTime) / 1000000000f;
			displayPanel.repaint();
			Main.update(deltaTime);
			lastTime = time;

		}

	}

	private static Vector translateMouse(float x, float y) {
		return new Vector(x, y).sub(scroll);
	}

	private static void clicked(int button) {
		switch (button) {
		case 1:
			if (makingTransition) {
				for (AutomatonState s : automaton) {
					if (new Circle(s.position, stateRadius).distance(mouse) == 0) {
						if (automaton.addTransition(selectedState, s, null, null)) {
							autoSave();
							selectedTransition = new Pair<AutomatonState, AutomatonState>(selectedState, s);
							menu.setOptionList(2);
							Vector offset = Matrix.createRotation2D(3.141f / 2)
									.multiply(s.position.sub(selectedState.position).norm(transitionOffset));
							if (s == selectedState)
								menu.animate(s.position.sub(new Vector(stateRadius * 2.4f, 0)));
							else
								menu.animate(s.position.add(selectedState.position).scale(0.5f).add(offset));
							selectedState = null;
							makingTransition = false;
							break;
						}
					}
				}
			} else if (!menu.clicked(mouse)) {
				selectedState = null;
				selectedTransition = null;
				for (AutomatonState s : automaton) {
					if (new Circle(s.position, stateRadius).distance(mouse) == 0) {
						selectedState = s;
						selectedTransition = null;
						menu.setOptionList(1);
						menu.animate(s.position);
					}
				}
				if (selectedState == null) {
					automaton.forEach((s) -> s.transitions.forEach((s2, l) -> l.forEach((t) -> {
						if (s2 == s) {
							Vector pos = s.position.sub(new Vector(stateRadius * 1.2f, 0));
							if (new Circle(pos, stateRadius).distance(mouse) <= transitionOffset) {
								selectedTransition = new Pair<AutomatonState, AutomatonState>(s, s);
								menu.setOptionList(2);
								menu.animate(s.position.sub(new Vector(stateRadius * 2.4f, 0)));
							}
						} else {
							Vector offset = Matrix.createRotation2D(3.141f / 2)
									.multiply(s2.position.sub(s.position).norm(transitionOffset));
							if (Line.fromPoints(s2.position.add(offset), s.position.add(offset))
									.distance(mouse) <= transitionOffset) {
								selectedTransition = new Pair<AutomatonState, AutomatonState>(s, s2);
								menu.setOptionList(2);
								menu.animate(s2.position.add(s.position).scale(0.5f).add(offset));
							}
						}
					})));
				}
			}
			break;
		case 2:
			break;
		case 3:
			makingTransition = false;
			boolean b = true;
			for (AutomatonState s : automaton) {
				if (new Circle(s.position, stateRadius).distance(mouse) < stateRadius) {
					b = false;
					break;
				}
			}
			selectedTransition = null;
			if (b) {
				menu.animate(mouse);
				selectedState = null;
				menu.setOptionList(0);
			}
			break;
		}
	}

	private static void draw(Graphics g) {
		g.save();
		g.translate(scroll);
		menu.draw(g);
		for (AutomatonState s : automaton) {
			g.drawState(s, selectedState == s ? Color.cyan : Color.BLACK);
			if (automaton.start == s) {
				g.drawArrow(new Vector(-stateRadius * 2, 0).add(s.position),
						new Vector(-stateRadius, 0).add(s.position), Color.darkGray);
			}
			s.transitions.forEach((s2, l) -> {
				g.drawTransition(s, s2,
						l.stream().map(p -> p.x + (p.y == null ? "" : "/" + p.y)).reduce((a, b) -> a + ", " + b)
								.orElse(""),
						new Pair<AutomatonState, AutomatonState>(s, s2).equals(selectedTransition) ? Color.cyan
								: Color.black);
			});
		}
		if (makingTransition) {
			if (selectedState == null) {
				makingTransition = false;
				return;
			}
			Vector offset = Matrix.createRotation2D(3.141f / 2)
					.multiply(mouse.sub(selectedState.position).norm(transitionOffset));
			g.drawArrow(selectedState.position.add(mouse.sub(selectedState.position).norm(stateRadius)).add(offset),
					mouse.add(offset), Color.cyan);
		}
		g.reset();
	}

	private static void update(float deltaTime) {
		menu.update(deltaTime);
		splitPane.setDividerLocation(200);
	}

	public static void autoSave() {
		new Thread(() -> {
			File[] saves = new File("saves").listFiles((f, n) -> n.startsWith("autosave"));
			Arrays.sort(saves, (a, b) -> Integer.parseInt(a.getName().replace("autosave", "").replace(".dat", ""))
					- Integer.parseInt(b.getName().replace("autosave", "").replace(".dat", "")));
			int offset = 1 - autosavePosition;
			autosaveCount = Math.min(saves.length + 1, autosaveMaxCount);
			if (offset >= 0)
				for (int i = saves.length - 1; i >= 0; i--) {
					if (i > autosaveMaxCount - 1)
						saves[i].delete();
					else if (i + offset <= 0)
						saves[i].delete();
					else
						saves[i].renameTo(new File("saves", "autosave" + (i + offset) + ".dat"));
				}
			else {
				for (int i = 0; i < saves.length; i++) {
					if (i + offset <= 0)
						saves[i].delete();
					else
						saves[i].renameTo(new File("saves", "autosave" + (i + offset) + ".dat"));
				}
			}
			automaton.save("autosave0");
			autosavePosition = 0;

		}).start();
	}

	public static void autoSave(Runnable r) {
		r.run();
		autoSave();
	}

	public static void load(String name) {
		Automaton a = Automaton.load(name);
		if (a == null)
			return;
		automaton = a;
	}

	public static void back() {
		autosavePosition = Math.max(0, Math.min(autosaveCount - 2, autosavePosition));
		load("autosave" + (++autosavePosition));
	}

	public static void forward() {
		autosavePosition = Math.max(1, Math.min(autosaveCount, autosavePosition));
		load("autosave" + --autosavePosition);
	}
}
