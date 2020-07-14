package ch.rhjoerg.plexus.starter.util;

import static ch.rhjoerg.commons.annotation.Annotations.findAnnotations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.inject.Module;

import ch.rhjoerg.plexus.starter.annotation.PlexusConfigurations;
import ch.rhjoerg.plexus.starter.annotation.PlexusModules;
import ch.rhjoerg.plexus.starter.annotation.PlexusPackages;

public class ConfigurationClassScanner
{
	public Set<String> discoverPackages(Collection<Class<?>> configurationClasses)
	{
		Set<String> packages = new TreeSet<String>();

		configurationClasses = extendConfigurationClasses(configurationClasses);

		for (Class<?> configurationClass : configurationClasses)
		{
			List<PlexusPackages> annotations = findAnnotations(PlexusPackages.class, configurationClass);

			for (PlexusPackages annotation : annotations)
			{
				packages.addAll(List.of(annotation.value()));
			}
		}

		return packages;
	}

	public Set<String> discoverPackages(Class<?>... configurationClasses)
	{
		return discoverPackages(List.of(configurationClasses));
	}

	public List<Module> discoverModules(Collection<Class<?>> configurationClasses) throws Exception
	{
		Set<Class<? extends Module>> moduleClasses = new HashSet<>();

		configurationClasses = extendConfigurationClasses(configurationClasses);

		for (Class<?> configurationClass : configurationClasses)
		{
			List<PlexusModules> annotations = findAnnotations(PlexusModules.class, configurationClass);

			for (PlexusModules annotation : annotations)
			{
				moduleClasses.addAll(List.of(annotation.value()));
			}
		}

		List<Module> modules = new ArrayList<>();

		for (Class<? extends Module> moduleClass : moduleClasses)
		{
			modules.add(moduleClass.getConstructor().newInstance());
		}

		return modules;
	}

	public List<Module> discoverModules(Class<?>... configurationClasses) throws Exception
	{
		return discoverModules(List.of(configurationClasses));
	}

	public Set<Class<?>> extendConfigurationClasses(Collection<Class<?>> configurationClasses)
	{
		Set<Class<?>> result = new HashSet<>(configurationClasses);
		boolean changed = false;

		for (Class<?> configuration : configurationClasses)
		{
			List<PlexusConfigurations> annotations = findAnnotations(PlexusConfigurations.class, configuration);

			for (PlexusConfigurations annotation : annotations)
			{
				if (result.addAll(List.of(annotation.value())))
				{
					changed = true;
				}
			}
		}

		if (changed)
		{
			result = extendConfigurationClasses(result);
		}

		return result;
	}
}
