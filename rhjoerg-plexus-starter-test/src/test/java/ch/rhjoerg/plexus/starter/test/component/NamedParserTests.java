package ch.rhjoerg.plexus.starter.test.component;

import static ch.rhjoerg.commons.tool.ClassLoaders.contextClassLoader;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.Test;

import ch.rhjoerg.commons.io.Read;

public class NamedParserTests
{
	@Test
	public void test() throws Exception
	{
		URL url = contextClassLoader().getResource("components/maven-core.named.txt");
		String src = Read.string(url, UTF_8);
		NamedParser parser = new NamedParser();
		List<String> entries = parser.parse(src);

		assertEquals(13, entries.size());
	}
}
