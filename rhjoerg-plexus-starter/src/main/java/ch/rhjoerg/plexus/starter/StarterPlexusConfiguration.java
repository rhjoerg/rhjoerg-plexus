package ch.rhjoerg.plexus.starter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.inject.Module;

public class StarterPlexusConfiguration
{
	public final static List<String> DEFAULT_CLASSLOADER_EXCLUSIONS = //
			List.of("META-INF/plexus/components.xml", "META-INF/sisu/javax.inject.Named");

	private final Set<String> classLoaderExclusions = new TreeSet<>();

	private final List<Module> customModules = new ArrayList<>();

	public StarterPlexusConfiguration()
	{
		classLoaderExclusions.addAll(DEFAULT_CLASSLOADER_EXCLUSIONS);
	}

	public StarterPlexusConfiguration addClassLoaderExlusion(String classLoaderExclusion)
	{
		classLoaderExclusions.add(classLoaderExclusion);

		return this;
	}

	public String[] classLoaderExclusions()
	{
		return classLoaderExclusions.toArray(String[]::new);
	}

	public StarterPlexusConfiguration addCustomModule(Module customModule)
	{
		customModules.add(customModule);

		return this;
	}

	public Module[] customModules()
	{
		return customModules.toArray(Module[]::new);
	}
}
