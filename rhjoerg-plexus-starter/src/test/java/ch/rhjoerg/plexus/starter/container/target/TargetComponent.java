package ch.rhjoerg.plexus.starter.container.target;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component(role = TargetComponent.class, hint = "foo", instantiationStrategy = "singleton")
public class TargetComponent
{
	@Requirement(hint = "foo")
	public NamedTarget namedTarget;
}
