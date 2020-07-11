package ch.rhjoerg.plexus.starter.dependency;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.PlexusContainer;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.InjectionPoint;

import ch.rhjoerg.commons.Exceptions;
import ch.rhjoerg.commons.io.Read;

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
		Entry entry = addEntry(key(type), source);

		InjectionPoint.forConstructorOf(type).getDependencies().forEach(d -> addDependency(entry, d));
	}

	private Entry addEntry(Key<?> key, URL source)
	{
		Entry entry = entries.get(key);

		if (entry == null)
		{
			boolean exists = container.hasComponent(type(key), name(key));

			entry = new Entry(key, source, exists);
			entries.put(key, entry);
		}

		return entry;
	}

	private void addDependency(Entry entry, Dependency<?> dependency)
	{
		Entry dependencyEntry = addEntry(key(dependency), null);

		entry.dependencies.add(dependencyEntry);
	}

	private Key<?> key(Dependency<?> dependency)
	{
		return key(type(dependency), name(dependency.getKey()));
	}

	private Key<?> key(Class<?> type)
	{
		return key(type, name(type.getDeclaredAnnotations()));
	}

	private Key<?> key(Class<?> type, String name)
	{
		if (name == null || name.isEmpty())
		{
			name = "default";
		}

		return Key.get(type, Names.named(name));
	}

	private Class<?> type(Key<?> key)
	{
		TypeLiteral<?> typeLiteral = key.getTypeLiteral();

		if (!typeLiteral.getRawType().equals(typeLiteral.getType()))
		{
			throw Exceptions.notYetImplemented();
		}

		return typeLiteral.getRawType();
	}

	private Class<?> type(Dependency<?> dependency)
	{
		return type(dependency.getKey());
	}

	private String name(Annotation... annotations)
	{
		for (Annotation annotation : annotations)
		{
			if (annotation instanceof javax.inject.Named)
			{
				return javax.inject.Named.class.cast(annotation).value();
			}

			if (annotation instanceof com.google.inject.name.Named)
			{
				return com.google.inject.name.Named.class.cast(annotation).value();
			}
		}

		return "default";
	}

	private String name(Key<?> key)
	{
		Annotation annotation = key.getAnnotation();

		return annotation == null ? null : name(annotation);
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
