package ch.rhjoerg.plexus.starter.test.component;

import static ch.rhjoerg.commons.tool.ClassLoaders.contextClassLoader;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import ch.rhjoerg.plexus.starter.dependency.NamedParser;

public class NamedParserTests
{
	@Test
	public void test() throws Exception
	{
		String source = "java.lang.String";
		NamedParser parser = new NamedParser(contextClassLoader());
		List<Class<?>> entries = parser.parse(source);

		assertEquals(1, entries.size());
	}
}
