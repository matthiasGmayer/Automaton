package util;

public class OldAutomatonState {
	private class AutomatonLink{
		public int link;
		public String name;
		public AutomatonLink(int link, String name) {
			this.link = link;
			this.name = name;
		}
	}
	
	public final String name;
	public final AutomatonLink[] links;
	public OldAutomatonState(String name, String ...links){
		this.name = name;
		this.links = new AutomatonLink[links.length];
		for (int i = 0; i < links.length; i++) {
			this.links[i] = new AutomatonLink(-1, links[i]);
		}
	}
	
	public OldAutomatonState Setup(OldAutomatonState[] automatons) {
		for (int i = 0; i < automatons.length; i++) {
			for (int j = 0; j < links.length; j++) {
				if(links[j].name.equals(automatons[i].name)) {
					links[j].link = i;
				}
			}
		}
		return this;
	}
	
	public int GetStateId(String name) {
		for (int i = 0; i < links.length; i++) {
			System.out.println(links[i].name);
			if(links[i].name.equals(name)) return links[i].link;
		}
		return -1;
	}
	public int GetStateId(int id) {
		return links[id].link;
	}
	
	
	
}
