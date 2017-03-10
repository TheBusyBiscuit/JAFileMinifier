package io.github.TheBusyBiscuit.JAFileMinifier;

public enum FileExtension {
	
	JAVASCRIPT("JS (JavaScript)", "https://javascript-minifier.com/raw", ".js"),
	CSS("CSS (Cascading Style Sheet)", "https://cssminifier.com/raw", ".css");
	
	public String type, url, file;
	
	private FileExtension(String file, String url, String type) {
		this.file = file;
		this.url = url;
		this.type = type;
	}

}
