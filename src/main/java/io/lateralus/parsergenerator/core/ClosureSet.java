package io.lateralus.parsergenerator.core;

import com.google.common.collect.Sets;

import java.util.AbstractCollection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

class ClosureSet extends AbstractCollection<Item> implements Set<Item> {

	private static class ItemHandle {
		private final Production production;
		private final int position;

		private ItemHandle(Item item) {
			this.production = item.getProduction();
			this.position = item.getPosition();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ItemHandle that = (ItemHandle) o;
			return position == that.position &&
					production.equals(that.production);
		}

		@Override
		public int hashCode() {
			return Objects.hash(production, position);
		}
	}

	private final Map<ItemHandle, Item> map = new HashMap<>();

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean contains(Object o) {
		if (!(o instanceof Item)) {
			return false;
		}
		Item item = (Item)o;
		ItemHandle itemHandle = new ItemHandle(item);
		return map.containsKey(itemHandle) && map.get(itemHandle).equals(item);
	}

	@Override
	public Iterator<Item> iterator() {
		Iterator<Map.Entry<ItemHandle, Item>> iterator = map.entrySet().iterator();
		return new Iterator<>() {
			Map.Entry<ItemHandle, Item> current = null;
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}
			@Override
			public Item next() {
				current = iterator.next();
				return current.getValue();
			}
			@Override
			public void remove() {
				map.remove(current.getKey());
			}
		};
	}

	@Override
	public boolean add(Item item) {
		ItemHandle itemHandle = new ItemHandle(item);
		Item oldItem = map.get(itemHandle);

		if (oldItem == null) {
			map.put(itemHandle, item);
			return true;
		} else if (oldItem.getLookahead().containsAll(item.getLookahead())) {
			return false;
		} else {
			Set<Terminal> lookahead = Sets.union(oldItem.getLookahead(), item.getLookahead());
			Item newItem = new Item(oldItem.getProduction(), lookahead, oldItem.getPosition());
			map.put(itemHandle, newItem);
			return true;
		}
	}

	@Override
	public boolean remove(Object o) {
		if (!contains(o)) {
			return false;
		}
		map.remove(new ItemHandle((Item)o));
		return true;
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClosureSet that = (ClosureSet) o;
		return that.map.equals(this.map);
	}

	@Override
	public int hashCode() {
		return map.hashCode();
	}
}
