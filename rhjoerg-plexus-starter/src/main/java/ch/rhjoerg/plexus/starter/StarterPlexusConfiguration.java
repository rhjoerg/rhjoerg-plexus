package ch.rhjoerg.plexus.starter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.inject.Module;

import ch.rhjoerg.plexus.starter.util.ConfigurationClassScanner;

public class StarterPlexusConfiguration
{
	public final static List<String> DEFAULT_CLASSLOADER_EXCLUSIONS = //
			List.of("META-INF/plexus/components.xml", "META-INF/sisu/javax.inject.Named");

	private final Set<String> classLoaderExclusions = new TreeSet<>();

	private final List<Module> customModules = new ArrayList<>();

	private final List<Class<?>> configurationClasses = new ArrayList<>();

	private ConfigurationClassScanner configurationClassScanner = new ConfigurationClassScanner();

	public StarterPlexusConfiguration(Class<?>... configurationClasses)
	{
		classLoaderExclusions.addAll(DEFAULT_CLASSLOADER_EXCLUSIONS);
		Collections.addAll(this.configurationClasses, configurationClasses);
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

	public StarterPlexusConfiguration addConfigurationClass(Class<?> configurationClass)
	{
		configurationClasses.add(configurationClass);

		return this;
	}

	public Class<?>[] configurationClasses()
	{
		return configurationClasses.toArray(Class<?>[]::new);
	}

	public ConfigurationClassScanner configurationClassScanner()
	{
		return configurationClassScanner;
	}
}
