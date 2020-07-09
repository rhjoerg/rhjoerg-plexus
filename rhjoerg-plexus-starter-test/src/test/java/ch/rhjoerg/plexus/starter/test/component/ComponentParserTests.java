package ch.rhjoerg.plexus.starter.test.component;

import static ch.rhjoerg.commons.tool.ClassLoaders.contextClassLoader;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.Test;

import ch.rhjoerg.commons.io.Read;

public class ComponentParserTests
{
	@Test
	public void test() throws Exception
	{
		URL url = contextClassLoader().getResource("components/maven-core.components.xml");
		String xml = Read.string(url, UTF_8);
		ComponentParser parser = new ComponentParser();
		List<ComponentEntry> entries = parser.parse(xml);

		assertEquals(80, entries.size());
	}
}
