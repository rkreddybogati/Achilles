package info.archinnov.achilles.wrapper.builder;

import info.archinnov.achilles.entity.context.AchillesPersistenceContext;
import info.archinnov.achilles.wrapper.ListIteratorWrapper;

import java.util.ListIterator;

/**
 * ListIteratorWrapperBuilder
 * 
 * @author DuyHai DOAN
 * 
 */
public class ListIteratorWrapperBuilder<ID, V> extends
		AbstractWrapperBuilder<ID, ListIteratorWrapperBuilder<ID, V>, Void, V>
{
	private ListIterator<V> target;

	public static <ID, V> ListIteratorWrapperBuilder<ID, V> builder(
			AchillesPersistenceContext<ID> context, ListIterator<V> target)
	{
		return new ListIteratorWrapperBuilder<ID, V>(context, target);
	}

	public ListIteratorWrapperBuilder(AchillesPersistenceContext<ID> context, ListIterator<V> target)
	{
		super.context = context;
		this.target = target;
	}

	public ListIteratorWrapper<ID, V> build()
	{
		ListIteratorWrapper<ID, V> listIteratorWrapper = new ListIteratorWrapper<ID, V>(this.target);
		super.build(listIteratorWrapper);
		return listIteratorWrapper;
	}

}
