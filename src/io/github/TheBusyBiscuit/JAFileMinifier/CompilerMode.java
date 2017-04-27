package io.github.TheBusyBiscuit.JAFileMinifier;

public enum CompilerMode {
	
	MANUAL,
	AUTO;
	
	public static boolean isValid(String mode) {
	    for (CompilerMode c : CompilerMode.values()) {
	        if (c.name().equals(mode))
	            return true;
	    }
	    return false;
	}

}
