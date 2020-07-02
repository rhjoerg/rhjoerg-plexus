package ch.rhjoerg.plexus.starter.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.google.inject.Injector;

@WithPlexus
public class WithPlexusTests
{
	@Inject
	private Injector injector;

	@Test
	public void test()
	{
		assertNotNull(injector);
	}
}
