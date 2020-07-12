package ch.rhjoerg.plexus.starter.dependency;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Requirement;

import com.google.inject.Key;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.InjectionPoint;

import ch.rhjoerg.commons.function.ThrowingConsumer;
import ch.rhjoerg.commons.io.Read;
import ch.rhjoerg.plexus.core.util.Keys;
import ch.rhjoerg.plexus.starter.dependency.ComponentParser.Descriptor;

public class Dependencies
{
	private final static List<Class<?>> OPTIONAL_DEPENDENCIES = List.of(Map.class, List.class, Set.class);

	private final PlexusContainer container;
	private final ClassRealm realm;
	private final NamedParser namedParser;
	private final ComponentParser componentParser;

	private final Map<Key<?>, Entry> entries = new HashMap<>();

	public Dependencies(PlexusContainer container)
	{
		this.container = container;
		this.realm = container.getLookupRealm();
		this.namedParser = new NamedParser(realm);
		this.componentParser = new ComponentParser(realm);
	}

	public void addNameds(URL url) throws Exception
	{
		namedParser.parse(Read.string(url, UTF_8)).forEach(type -> addNamed(type, url));
	}

	public void addComponents(URL url) throws Exception
	{
		componentParser.parse(Read.string(url, UTF_8)) //
				.forEach(ThrowingConsumer.wrap(desc -> addComponent(desc, url)));
	}

	private void addNamed(Class<?> type, URL source)
	{
		Entry entry = addEntry(Keys.key(type), source);

		InjectionPoint.forConstructorOf(type).getDependencies().forEach(d -> addDependency(entry, d));
	}

	private void addComponent(Descriptor desc, URL source) throws Exception
	{
		Entry entry = addEntry(Keys.key(desc.component(), realm), source);

		desc.forEachRequirement((name, req) -> addDependency(entry, req));
	}

	private void addDependency(Entry entry, Dependency<?> dependency)
	{
		if (isOptional(dependency))
		{
			return;
		}

		Entry dependencyEntry = addEntry(Keys.key(dependency), null);

		entry.dependencies.add(dependencyEntry);
	}

	private void addDependency(Entry entry, Requirement requirement)
	{
		if (requirement.optional())
		{
			return;
		}

		Entry dependencyEntry = addEntry(Keys.key(requirement), null);

		entry.dependencies.add(dependencyEntry);
	}

	private boolean isOptional(Dependency<?> dependency)
	{
		if (dependency.isNullable())
		{
			return true;
		}

		Class<?> rawType = dependency.getKey().getTypeLiteral().getRawType();

		return OPTIONAL_DEPENDENCIES.stream().anyMatch(type -> type.isAssignableFrom(rawType));
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
