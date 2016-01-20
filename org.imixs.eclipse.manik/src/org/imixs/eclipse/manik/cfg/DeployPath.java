package org.imixs.eclipse.manik.cfg;

public class DeployPath {

	private String path;
	private boolean war;
	
	public DeployPath() {
	}
	
	public DeployPath(String path, boolean war) {
		this.path = path;
		this.war = war;
	}

	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public boolean isWar() {
		return war;
	}
	public void setWar(boolean war) {
		this.war = war;
	}
}
