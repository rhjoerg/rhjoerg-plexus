package ch.rhjoerg.plexus.starter.test.component;

import static ch.rhjoerg.commons.annotation.Annotations.annotationProxy;
import static ch.rhjoerg.commons.tool.ClassLoaders.contextClassLoader;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.eclipse.sisu.inject.DeferredClass;
import org.eclipse.sisu.plexus.Roles;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.space.URLClassSpace;

public class ComponentParser
{
	// See PlexusXmlScanner.

	private final ClassSpace space;

	public ComponentParser(ClassLoader classLoader)
	{
		space = new URLClassSpace(classLoader);
	}

	public ComponentParser()
	{
		this(contextClassLoader());
	}

	public List<ComponentEntry> parse(String xml) throws Exception
	{
		List<ComponentEntry> result = new ArrayList<>();
		MXParser parser = new MXParser();

		parser.setInput(new StringReader(xml));
		parse(parser, result);

		return result;
	}

	private void parse(MXParser parser, List<ComponentEntry> result) throws Exception
	{
		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, null);

		if (parser.nextTag() == XmlPullParser.START_TAG)
		{
			parseComponents(parser, result);
		}
	}

	private void parseComponents(MXParser parser, List<ComponentEntry> result) throws Exception
	{
		parser.require(XmlPullParser.START_TAG, null, "components");

		while (parser.nextTag() == XmlPullParser.START_TAG)
		{
			parseComponent(parser, result);
		}
	}

	private void parseComponent(MXParser parser, List<ComponentEntry> result) throws Exception
	{
		parser.require(XmlPullParser.START_TAG, null, "component");

		Map<String, Object> values = new TreeMap<>();
		DeferredClass<?> role = null;
		Map<String, Requirement> requirements = new TreeMap<>();
		Map<String, Configuration> configurations = new TreeMap<>();

		while (parser.nextTag() == XmlPullParser.START_TAG)
		{
			String name = parser.getName();

			if ("requirements".equals(name))
			{
				while (parser.nextTag() == XmlPullParser.START_TAG)
				{
					parseRequirement(parser, requirements);
				}
			}
			else if ("configuration".equals(name))
			{
				while (parser.nextTag() == XmlPullParser.START_TAG)
				{
					parseConfiguration(parser, configurations);
				}
			}
			else if ("role".equals(name))
			{
				role = space.deferLoadClass(parser.nextText().trim());
			}
			else if ("role-hint".equals(name))
			{
				values.put("hint", parser.nextText().trim());
			}
			else if ("instantiation-strategy".equals(name))
			{
				values.put("instantiationStrategy", parser.nextText().trim());
			}
			else if ("description".equals(name))
			{
				values.put("description", parser.nextText().trim());
			}
			else if ("implementation".equals(name))
			{
				values.put("type", parser.nextText().trim());
			}
			else
			{
				parser.skipSubTree();
			}
		}

		ComponentHandler handler = new ComponentHandler(role, values);
		Component component = annotationProxy(Component.class, handler);
		ComponentEntry entry = new ComponentEntry(component, requirements, configurations);

		result.add(entry);
	}

	private void parseRequirement(MXParser parser, Map<String, Requirement> requirements) throws Exception
	{
		parser.require(XmlPullParser.START_TAG, null, "requirement");

		Map<String, Object> values = new TreeMap<>();
		List<String> hints = new ArrayList<String>();
		String fieldName = null;
		String roleName = null;
		DeferredClass<?> role = null;

		while (parser.nextTag() == XmlPullParser.START_TAG)
		{
			String name = parser.getName();

			if ("role".equals(name))
			{
				roleName = parser.nextText().trim();
				role = space.deferLoadClass(roleName);
			}
			else if ("role-hint".equals(name))
			{
				hints.add(parser.nextText().trim());
			}
			else if ("role-hints".equals(name))
			{
				while (parser.nextTag() == XmlPullParser.START_TAG)
				{
					hints.add(parser.nextText().trim());
				}
			}
			else if ("field-name".equals(name))
			{
				fieldName = parser.nextText().trim();
			}
			else if ("optional".equals(name))
			{
				values.put("optional", Boolean.parseBoolean(parser.nextText().trim()));
			}
			else
			{
				parser.skipSubTree();
			}
		}

		if (fieldName == null)
		{
			fieldName = roleName;
		}

		if (hints.size() > 1)
		{
			values.put("hints", hints.toArray(String[]::new));
		}
		else if (hints.size() == 1)
		{
			values.put("hint", hints.get(0));
		}

		RequirementHandler handler = new RequirementHandler(role, values);
		Requirement requirement = annotationProxy(Requirement.class, handler);

		requirements.put(fieldName, requirement);
	}

	private void parseConfiguration(MXParser parser, Map<String, Configuration> configurations) throws Exception
	{
		String name = parser.getName();
		String fieldName = Roles.camelizeName(name);
		StringBuilder buf = new StringBuilder();

		String header = parser.getText().trim();
		int depth = parser.getDepth();

		while (parser.next() != XmlPullParser.END_TAG || parser.getDepth() > depth)
		{
			buf.append(parser.getText().trim());
		}

		if (buf.indexOf("<") == 0 || header.indexOf('=') > 0)
		{
			buf.insert(0, header);

			if (!header.endsWith("/>"))
			{
				buf.append("</" + name + '>');
			}
		}

		Map<String, Object> values = Map.of("name", fieldName, "value", buf.toString());
		Configuration configuration = annotationProxy(Configuration.class, values);

		configurations.put(fieldName, configuration);
	}
}
