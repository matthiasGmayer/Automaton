package graphics;

import java.awt.Color;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import math.Matrix;
import math.Vector;
import run.Main;

public class ClickMenu {
	public Vector position;
	private boolean open, wait;
	private float animateTime = 0;
	private float maxAnimateTime = 0.15f;
	private float closureDistance = Main.stateRadius * 4.2f;
	private List<Hashtable<ClickMenuOption, Runnable>> optionLists;
	private Hashtable<ClickMenuOption, Runnable> optionList;

	@SafeVarargs
	public ClickMenu(Vector position, Hashtable<ClickMenuOption, Runnable>... optionLists) {
		super();
		this.position = position;
		this.optionLists = Arrays.asList(optionLists);
		setOptionList(0);
		setPositions(0, 100);
	}

	public void setOptionList(int i) {
		optionList = optionLists.get(i);
		animateTime = 0;
		closureDistance = i == 2 ? 100: Main.stateRadius * 4.2f;
	}

	public boolean clicked(Vector mouse) {
		for (Entry<ClickMenuOption, Runnable> e : optionList.entrySet()) {
			ClickMenuOption o = e.getKey();
			Runnable r = e.getValue();
			if (o.rectangle.distanceSquared(mouse) == 0 && o.rectangle.dimension.lengthSquared() > 50) {
				r.run();
				open = false;
				return true;

			}
		}
		return false;
	}

	private void setPositions(float angle, float distanceMultiplier) {
		int size = optionList.size();
		float interAngle = 3.14159f * 2 / size;
		int i = 0;
		for (ClickMenuOption o : optionList.keySet()) {

			o.rectangle.position = size == 1 ? position
					: position.add(Matrix.createRotation2D(interAngle * i++ + angle)
							.multiply(new Vector(0, distanceMultiplier)));
			o.rectangle.dimension = o.rectangle.dimension.norm(distanceMultiplier/closureDistance*200f);
		}
	}

	float angle = 0;
	float dis = 100;

	public void update(float deltaTime) {
		float factor = animateTime / maxAnimateTime;
		setPositions(factor * (float) Math.PI * 3 / 4f, Math.max(closureDistance / 2 * factor, 1));
		if (open) {
			animateTime = Math.min(animateTime + deltaTime, maxAnimateTime);
		} else {
			animateTime = Math.max(animateTime - deltaTime, 0);
		}
	}

	public void draw(Graphics g) {
		float factor = animateTime / maxAnimateTime;
		Color color = new Color(0, 0, 0, (float) Math.pow(factor, 0.5f));
		for (ClickMenuOption o : optionList.keySet()) {
			g.drawRectangle(o.rectangle, color);
			g.drawString(o.option, o.rectangle.position, color);
		}
	}

	public void animate(Vector position) {
		open = true;
		this.position = position;
		wait = true;
	}

	public void close() {
		open = false;
	}
	
	public void checkClose(Vector mouse) {
		if (mouse.sub(position).length() > (optionList.size() == 1 ? closureDistance / 2f : closureDistance)) {
			if (!wait)
				open = false;
		} else {
			wait = false;
		}
	}

}
