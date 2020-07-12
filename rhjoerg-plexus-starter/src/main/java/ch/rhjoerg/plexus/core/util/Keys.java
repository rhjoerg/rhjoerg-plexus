package ch.rhjoerg.plexus.core.util;

import java.lang.annotation.Annotation;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.spi.Dependency;

import ch.rhjoerg.commons.Exceptions;

public interface Keys
{
	public static Key<?> key(Class<?> type, String name)
	{
		if (name == null || name.isEmpty())
		{
			name = "default";
		}

		return Key.get(type, Names.named(name));
	}

	public static Key<?> key(Class<?> type)
	{
		return key(type, name(type));
	}

	public static Key<?> key(Dependency<?> dependency)
	{
		return key(type(dependency), name(dependency.getKey()));
	}

	public static Class<?> type(Key<?> key)
	{
		TypeLiteral<?> typeLiteral = key.getTypeLiteral();

		if (!typeLiteral.getRawType().equals(typeLiteral.getType()))
		{
			throw Exceptions.notYetImplemented();
		}

		return typeLiteral.getRawType();
	}

	public static Class<?> type(Dependency<?> dependency)
	{
		return type(dependency.getKey());
	}

	public static String name(Annotation... annotations)
	{
		for (Annotation annotation : annotations)
		{
			if (annotation instanceof javax.inject.Named)
			{
				return javax.inject.Named.class.cast(annotation).value();
			}

			if (annotation instanceof com.google.inject.name.Named)
			{
				return com.google.inject.name.Named.class.cast(annotation).value();
			}
		}

		return "default";
	}

	public static String name(Class<?> type)
	{
		return name(type.getDeclaredAnnotations());
	}

	public static String name(Key<?> key)
	{
		return name(key.getAnnotation());
	}
}
