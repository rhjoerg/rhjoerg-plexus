package ch.rhjoerg.plexus.starter.test.component;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;

public class ComponentEntry
{
	private final Component component;

	private final Map<String, Requirement> requirements = new TreeMap<>();
	private final Map<String, Configuration> configurations = new TreeMap<>();

	public ComponentEntry(Component component, Map<String, Requirement> requirements, Map<String, Configuration> configurations)
	{
		this.component = component;
		this.requirements.putAll(requirements);
		this.configurations.putAll(configurations);
	}

	public Component component()
	{
		return component;
	}

	public void forEachRequirement(BiConsumer<String, Requirement> consumer)
	{
		requirements.forEach(consumer);
	}

	public void forEachConfiguration(BiConsumer<String, Configuration> consumer)
	{
		configurations.forEach(consumer);
	}
}
