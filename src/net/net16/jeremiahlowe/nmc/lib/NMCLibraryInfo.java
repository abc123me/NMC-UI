package net.net16.jeremiahlowe.nmc.lib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.jar.*;

public class NMCLibraryInfo {
	public static final String PATH_REGEXP = "^((\\w+)[.](\\w+))+$";
	
	private String path;
	
	private final ArrayList<String> depends;
	private final ArrayList<String> conflicts;
	
	public NMCLibraryInfo(String path) {
		this.path = path;
		depends = new ArrayList<String>();
		conflicts = new ArrayList<String>();
	}
	public NMCLibraryInfo(String path, String[] dependsOn, String[] conflictsWith) {
		this(path);
		for(String s : dependsOn) depends.add(s);
		for(String s : conflictsWith) conflicts.add(s);
	}

	public static final String ATTR_LIB_PATH = "Library-Path";
	public static final String ATTR_LIB_DEPENDS = "Library-Depends";
	public static final String ATTR_LIB_CONFLICTS = "Library-Conflicts";
	
	public static NMCLibraryInfo fromManifest(Manifest mf) throws LibraryException{
		Attributes mfa = mf.getMainAttributes();
		NMCLibraryInfo out = new NMCLibraryInfo(null);
		for(Object k : mfa.keySet()) {
			if(k != null) {
				String sk = k.toString();
				String val = mfa.getValue(sk);
				if(sk.equals(ATTR_LIB_PATH)) 
					out.path = val;
				else if(sk.equals(ATTR_LIB_DEPENDS)) 
					out.depends.add(val);
				else if(sk.equals(ATTR_LIB_CONFLICTS)) 
					out.conflicts.add(val);
			}
		}
		if(out.path == null)
			throw new LibraryException("Library has no path (" + ATTR_LIB_PATH + ")!");
		return out;
	}
	
	public String getPath() {
		return path;
	}
	public ArrayList<String> getDependencies(){
		depends.trimToSize();
		return depends;
	}
	public ArrayList<String> getConflicts(){
		conflicts.trimToSize();
		return conflicts;
	}

	public static NMCLibraryInfo fromJAR(JarFile j) throws LibraryException, IOException {
		if(j == null)
			throw new NullPointerException();
		return fromManifest(j.getManifest());
	}
}
