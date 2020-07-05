package ch.rhjoerg.plexus.starter.util;

import static ch.rhjoerg.commons.annotation.Annotations.findAnnotations;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ch.rhjoerg.plexus.starter.annotation.PlexusConfigurations;
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
			boolean changed = false;

			for (PlexusPackages annotation : annotations)
			{
				if (packages.addAll(List.of(annotation.value())))
				{
					changed = true;
				}
			}

			if (!changed)
			{
				packages.add(configurationClass.getPackageName());
			}
		}

		return packages;
	}

	public Set<String> discoverPackages(Class<?>... configurationClasses)
	{
		return discoverPackages(List.of(configurationClasses));
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
