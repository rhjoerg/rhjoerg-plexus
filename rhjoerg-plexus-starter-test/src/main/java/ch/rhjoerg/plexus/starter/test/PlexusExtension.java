package ch.rhjoerg.plexus.starter.test;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class PlexusExtension implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback
{
	@Override
	public void afterAll(ExtensionContext context) throws Exception
	{
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception
	{
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception
	{
	}
}
