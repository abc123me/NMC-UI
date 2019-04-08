package net.net16.jeremiahlowe.nmc;

import net.net16.jeremiahlowe.nmc.lib.*;
import net.net16.jeremiahlowe.nmc.ui.*;
import net.net16.jeremiahlowe.shared.*;

import java.io.*;
import java.lang.reflect.Constructor;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import java.util.logging.Level;

import javax.swing.*;

public class NMCInstance {
	public final ModuleAccessPanel map;
	public final NMCMainUI ui;
	public final ArrayList<Duplet<NMCLibrary, NMCLibraryInfo>> libs;
	
	public static final Level[] LOG_LEVELS = new Level[] {
		Level.ALL, Level.OFF, Level.CONFIG, 
		Level.FINE, Level.FINER, Level.FINEST,
		Level.INFO, Level.WARNING, Level.SEVERE
	};
	
	public NMCInstance(ModuleAccessPanel map, NMCMainUI ui) {
		this.map = map;
		this.ui = ui;
		this.libs = new ArrayList<Duplet<NMCLibrary, NMCLibraryInfo>>();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override public void run() {
				unloadAllLibraries();
			}
		});
	}
	
	public Duplet<NMCLibrary, NMCLibraryInfo> loadLibraryFromJar(File file) throws LibraryException {
		Duplet<NMCLibrary, NMCLibraryInfo> w = getLibraryFromJar(file);
		if(w.a == null || w.b == null) 
			throw new LibraryException(new NullPointerException("getLibraryFromJAR returned null!"));
		loadLibrary(w);
		return w;
	}
	private Duplet<NMCLibrary, NMCLibraryInfo> getLibraryFromJar(File file) throws LibraryException {
		if(file == null)
			throw new NullPointerException("File is null!");
		NMCLibraryInfo libInfo = null;
		JarFile jar = null;
		URLClassLoader loader = null;
		NMCLibrary lib = null;
		try {
			try{
				jar = new JarFile(file);
				libInfo = NMCLibraryInfo.fromJAR(jar);
				if(libInfo == null)
					throw new NullPointerException();
			} catch(IOException ioe) {
				throw new LibraryException("Unable to load JAR file at \"" + file.getPath() + "\"!");
			} finally {
				jar.close();
			}
			try {
				URL url = file.toURI().toURL();
				loader = URLClassLoader.newInstance(new URL[] { url }, NMCInstance.class.getClassLoader());
				Class<?> cl = Class.forName(libInfo.getPath(), true, loader);
				Constructor<?> c = cl.getConstructor(NMCInstance.class);
				if(c == null)
					throw new LibraryException("Class does not have required super constructor \"public NMCLibrary(NMCInstance)\"");
				Object libo = c.newInstance(this);
				if(libo == null)
					throw new LibraryException("Failed instantiating class at " + libInfo.getPath() + "!");
				if(libo instanceof NMCLibrary)
					lib = (NMCLibrary) libo;
				else
					throw new LibraryException(libo.getClass().getName() + " is not an instance of " + NMCLibrary.class.getName());
			} catch (ClassNotFoundException e) {
				throw new LibraryException("Unable to find class specified at \"" + libInfo.getPath() + "\"");
			} catch (InstantiationException e) {
				throw new LibraryException("Unable to instantiate library: \"" + libInfo.getPath() + "\"");
			} catch (NoSuchMethodException e) {
				throw new LibraryException("No such method found: \"" + e + "\"");
			} catch (SecurityException e) {
				throw new LibraryException("Invalid permissions, cannot access JAR\n" + e);
			} catch (Exception e) {
				throw new LibraryException(e);
			} finally {
				if(loader != null)
					loader.close();
			}
		} catch(IOException ioe) {
			throw new LibraryException("IOException during handling of JAR file!");
		}
		return new Duplet<NMCLibrary, NMCLibraryInfo>(lib, libInfo);
	}
	public void loadLibrary(NMCLibrary lib, String path) throws LibraryException {
		NMCLibraryInfo info = new NMCLibraryInfo(path);
		loadLibrary(new Duplet<NMCLibrary, NMCLibraryInfo>(lib, info));
	}
	public void loadLibrary(Duplet<NMCLibrary, NMCLibraryInfo> lib) throws LibraryException {
		if(verifyLibrary(lib)) {
			try{
				String s = lib.a.onLibraryLoad();
				if(s != null) throw new Exception(s);
			}catch(Exception e) {
				throw new LibraryException(lib.a, e);
			}
			libs.add(lib);
			if(ui != null) ui.insertLibrary(lib.a);
			try{
				lib.a.onLibraryLoaded();
			}catch(Exception e) {
				throw new LibraryException(lib.a, e);
			}
		}
	}
	public void unloadLibrary(NMCLibrary lib) {
		ArrayList<Duplet<NMCLibrary, NMCLibraryInfo>> dps = new ArrayList<>();
		for(Duplet<NMCLibrary, NMCLibraryInfo> d : libs)
			if(d != null && d.a != null && d.a == lib)
				dps.add(d);
		for(Duplet<NMCLibrary, NMCLibraryInfo> d : dps)
			unloadLibrary(d);
		libs.trimToSize();
	}
	public void unloadLibrary(Duplet<NMCLibrary, NMCLibraryInfo> lib){
		try{
			lib.a.preLibraryUnload();
			lib.a.onLibraryUnload();
		}catch(Exception e) {
			LibraryException le = new LibraryException(lib.a, e);
			System.err.println(le.toString());
			le.printStackTrace();
		}
		libs.remove(lib);
		if(ui != null) ui.removeLibrary(lib.a);
	}
	public void unloadAllLibraries() {
		ArrayList<Duplet<NMCLibrary, NMCLibraryInfo>> dps = new ArrayList<>();
		for(Duplet<NMCLibrary, NMCLibraryInfo> d : libs)
			if(d != null && d.a != null)
				dps.add(d);
		for(Duplet<NMCLibrary, NMCLibraryInfo> d : dps)
			unloadLibrary(d);
		libs.trimToSize();
	}
	public void removeModule(Module m) {
		if(map != null)
			map.removeModule(m);
	}
	public void addModule(Module m) {
		if(map != null)
			map.addModule(m);
		else
			System.err.println("Cannot add module \"" + m.getModuleName() + "\" since there is no UI!");
	}
	private boolean verifyLibrary(Duplet<NMCLibrary, NMCLibraryInfo> l1) throws LibraryException {
		if(l1 == null || l1.a == null || l1.b == null)
			throw new LibraryException("Attempt to load a null library or a library with invalid parts");
		String libName = l1.b.getPath();
		for(Duplet<NMCLibrary, NMCLibraryInfo> l2 : libs) {
			for(String s : l1.b.getConflicts()) { 
				if(s == null) continue;
				if(l2.b.getPath().trim().equals(libName.trim()))
					throw new LibraryException(l1.a, "Library " + libName + " conflicts with " + s);
			}
		}
		for(String s : l1.b.getDependencies()) {
			if(s == null) continue;
			boolean has = false;
			for(Duplet<NMCLibrary, NMCLibraryInfo> l2 : libs) {
				if(l2 == null || l2.a == null) continue;
				if(s.equals(l1.b.getPath())){ has = true; break; }
			}
			if(!has) throw new LibraryException(l1.a, "Library is missing dependency: " + s);
		}
		return true;
	}
}
