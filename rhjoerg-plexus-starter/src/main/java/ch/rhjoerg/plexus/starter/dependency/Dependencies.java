package ch.rhjoerg.plexus.starter.dependency;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.PlexusContainer;

import com.google.inject.Key;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.InjectionPoint;

import ch.rhjoerg.commons.io.Read;
import ch.rhjoerg.plexus.core.util.Keys;

public class Dependencies
{
	private final PlexusContainer container;
	private final NamedParser namedParser;

	private final Map<Key<?>, Entry> entries = new HashMap<>();

	public Dependencies(PlexusContainer container)
	{
		this.container = container;
		this.namedParser = new NamedParser(container.getLookupRealm());
	}

	public void addNameds(URL url) throws Exception
	{
		namedParser.parse(Read.string(url, UTF_8)).forEach(type -> addNamed(type, url));
	}

	private void addNamed(Class<?> type, URL source)
	{
		Entry entry = addEntry(Keys.key(type), source);

		InjectionPoint.forConstructorOf(type).getDependencies().forEach(d -> addDependency(entry, d));
	}

	private void addDependency(Entry entry, Dependency<?> dependency)
	{
		Entry dependencyEntry = addEntry(Keys.key(dependency), null);

		entry.dependencies.add(dependencyEntry);
	}

	private Entry addEntry(Key<?> key, URL source)
	{
		Entry entry = entries.get(key);

		if (entry == null)
		{
			boolean exists = container.hasComponent(Keys.type(key), Keys.name(key));

			entry = new Entry(key, source, exists);
			entries.put(key, entry);
		}

		return entry;
	}

	public static class Entry
	{
		public final Key<?> key;
		public final URL source;
		public final boolean exists;

		public final List<Entry> dependencies = new ArrayList<>();

		public Entry(Key<?> key, URL source, boolean exists)
		{
			this.key = key;
			this.source = source;
			this.exists = exists;
		}
	}
}
