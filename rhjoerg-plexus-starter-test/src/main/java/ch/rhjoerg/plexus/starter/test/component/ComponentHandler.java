package ch.rhjoerg.plexus.starter.test.component;

import static ch.rhjoerg.commons.reflect.Methods.isExpectedMethod;

import java.lang.reflect.Method;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.eclipse.sisu.inject.DeferredClass;

import ch.rhjoerg.commons.annotation.AnnotationHandler;

public class ComponentHandler extends AnnotationHandler<Component>
{
	private final DeferredClass<?> role;

	public ComponentHandler(DeferredClass<?> role, Map<String, Object> values)
	{
		super(Component.class, values);

		this.role = role;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		if (isExpectedMethod(method, "role"))
		{
			return role.load();
		}

		return super.invoke(proxy, method, args);
	}
}
