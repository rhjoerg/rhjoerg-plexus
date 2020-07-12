package ch.rhjoerg.plexus.starter.dependency;

import static ch.rhjoerg.commons.annotation.Annotations.annotationProxy;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.eclipse.sisu.plexus.Roles;

public class ComponentParser
{
	private final ClassLoader classLoader;

	public ComponentParser(ClassLoader classLoader)
	{
		this.classLoader = classLoader;
	}

	public List<Descriptor> parse(String xml) throws Exception
	{
		List<Descriptor> result = new ArrayList<>();
		MXParser parser = new MXParser();

		parser.setInput(new StringReader(xml));
		parse(parser, result);

		return result;
	}

	private void parse(MXParser parser, List<Descriptor> result) throws Exception
	{
		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, null);

		if (parser.nextTag() == XmlPullParser.START_TAG)
		{
			parseComponents(parser, result);
		}
	}

	private void parseComponents(MXParser parser, List<Descriptor> result) throws Exception
	{
		parser.require(XmlPullParser.START_TAG, null, "components");

		while (parser.nextTag() == XmlPullParser.START_TAG)
		{
			parseComponent(parser, result);
		}
	}

	private void parseComponent(MXParser parser, List<Descriptor> result) throws Exception
	{
		parser.require(XmlPullParser.START_TAG, null, "component");

		Map<String, Object> values = new TreeMap<>();
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
				values.put("role", classLoader.loadClass(text(parser)));
			}
			else if ("role-hint".equals(name))
			{
				values.put("hint", text(parser));
			}
			else if ("instantiation-strategy".equals(name))
			{
				values.put("instantiationStrategy", text(parser));
			}
			else if ("description".equals(name))
			{
				values.put("description", text(parser));
			}
			else if ("implementation".equals(name))
			{
				values.put("type", text(parser));
			}
			else
			{
				parser.skipSubTree();
			}
		}

		Component component = annotationProxy(Component.class, values);
		Descriptor descriptor = new Descriptor(component, requirements, configurations);

		result.add(descriptor);
	}

	private void parseRequirement(MXParser parser, Map<String, Requirement> requirements) throws Exception
	{
		parser.require(XmlPullParser.START_TAG, null, "requirement");

		Map<String, Object> values = new TreeMap<>();
		List<String> hints = new ArrayList<String>();
		String fieldName = null;
		String roleName = null;

		while (parser.nextTag() == XmlPullParser.START_TAG)
		{
			String name = parser.getName();

			if ("role".equals(name))
			{
				roleName = text(parser);
				values.put("role", classLoader.loadClass(roleName));
			}
			else if ("role-hint".equals(name))
			{
				hints.add(text(parser));
			}
			else if ("role-hints".equals(name))
			{
				while (parser.nextTag() == XmlPullParser.START_TAG)
				{
					hints.add(text(parser));
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

		Requirement requirement = annotationProxy(Requirement.class, values);

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

	private String text(MXParser parser) throws Exception
	{
		return parser.nextText().trim();
	}

	public static class Descriptor
	{
		private final Component component;

		private final Map<String, Requirement> requirements = new TreeMap<>();
		private final Map<String, Configuration> configurations = new TreeMap<>();

		public Descriptor(Component component, Map<String, Requirement> requirements, Map<String, Configuration> configurations)
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
}
