package ch.rhjoerg.plexus.starter.test.component;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class NamedParser
{
	public final static Pattern PATTERN = Pattern.compile("\\s+");

	public List<String> parse(String src)
	{
		return Stream.of(PATTERN.split(src)).filter(s -> !s.isEmpty()).collect(toList());
	}
}
