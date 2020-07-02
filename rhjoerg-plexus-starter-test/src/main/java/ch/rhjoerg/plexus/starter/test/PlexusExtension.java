package ch.rhjoerg.plexus.starter.test;

import static ch.rhjoerg.commons.annotation.Annotations.findAnnotationDetails;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import ch.rhjoerg.plexus.starter.PlexusConfigurations;

public class PlexusExtension implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback
{
	@Override
	public void beforeEach(ExtensionContext context) throws Exception
	{
		discoverConfigurationClass(context);
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception
	{
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception
	{
	}

	private Class<?> discoverConfigurationClass(ExtensionContext context)
	{
		Class<?> testClass = context.getRequiredTestClass();
		List<Map.Entry<Class<?>, List<PlexusConfigurations>>> candidates = findAnnotationDetails(PlexusConfigurations.class, testClass);

		if (candidates.isEmpty())
		{
			return testClass;
		}

		return candidates.get(0).getKey();
	}
}
