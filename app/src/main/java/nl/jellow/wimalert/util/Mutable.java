package nl.jellow.wimalert.util;

/**
 * A class for storing a variable that can change, even if this instance is declared final. Can
 * be useful in anonymous inner classes that need to acces mutable variables from outside its own
 * scope.
 *
 * Created by Jelle on 1-6-2015.
 */
public final class Mutable<T>
{
	public T value;

	public Mutable(T value)
	{
		this.value = value;
	}
}
