package ch.rhjoerg.plexus.starter.container;

import static ch.rhjoerg.commons.annotation.Annotations.findAnnotations;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ch.rhjoerg.plexus.starter.PlexusConfigurations;
import ch.rhjoerg.plexus.starter.PlexusPackages;

public interface ConfigurationUtils
{
	public static Set<Class<?>> discoverPlexusConfigurations(Collection<Class<?>> configurations)
	{
		Set<Class<?>> result = new HashSet<>();
		boolean changed = false;

		result.addAll(configurations);

		for (Class<?> configuration : configurations)
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
			result = discoverPlexusConfigurations(result);
		}

		return result;
	}

	public static Set<String> discoverPlexusPackages(Collection<Class<?>> configurations)
	{
		Set<String> packages = new TreeSet<String>();

		configurations = discoverPlexusConfigurations(configurations);

		for (Class<?> configuration : configurations)
		{
			List<PlexusPackages> annotations = findAnnotations(PlexusPackages.class, configuration);
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
				packages.add(configuration.getPackageName());
			}
		}

		return packages;
	}
}
