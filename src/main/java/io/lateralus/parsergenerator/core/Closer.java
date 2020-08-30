package io.lateralus.parsergenerator.core;

import java.util.Set;

/**
 * Interface that can calculate the closure over a set of items.
 */
public interface Closer {

	/**
	 * Calculates the closure over the given set of items.
	 * @param kernel The set to use as kernel for the closure
	 * @return The closure over the kernel
	 */
	Set<Item> closure(Set<Item> kernel);

}
