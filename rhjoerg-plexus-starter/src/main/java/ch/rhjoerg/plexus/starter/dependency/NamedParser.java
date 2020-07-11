package ch.rhjoerg.plexus.starter.dependency;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import ch.rhjoerg.commons.function.ThrowingFunction;

public class NamedParser
{
	public final static Pattern PATTERN = Pattern.compile("\\s+");

	private final ClassLoader classLoader;

	public NamedParser(ClassLoader classLoader)
	{
		this.classLoader = classLoader;
	}

	public List<Class<?>> parse(String source) throws Exception
	{
		return Stream.of(PATTERN.split(source)).filter(s -> !s.isEmpty()) //
				.map(ThrowingFunction.wrap(s -> classLoader.loadClass(s))) //
				.collect(toList());
	}
}
