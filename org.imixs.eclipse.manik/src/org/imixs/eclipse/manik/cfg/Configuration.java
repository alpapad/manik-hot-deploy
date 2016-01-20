package org.imixs.eclipse.manik.cfg;

import java.util.ArrayList;
import java.util.List;

public class Configuration {

	private String wildflyPath = "";
	
	private List<String> globs = new ArrayList<>();
	
	public String getWildflyPath() {
		return wildflyPath;
	}
	public void setWildflyPath(String wildflyPath) {
		this.wildflyPath = wildflyPath;
	}
	public List<String> getGlobs() {
		return globs;
	}
	public void setGlobs(List<String> globs) {
		this.globs = globs;
	}
	
}
