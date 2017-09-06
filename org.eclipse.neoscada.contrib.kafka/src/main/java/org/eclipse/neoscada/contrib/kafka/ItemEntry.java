package org.eclipse.neoscada.contrib.kafka;

import java.io.Serializable;

import org.eclipse.scada.da.client.DataItem;

public class ItemEntry implements Serializable, Comparable<ItemEntry> {

	private static final long serialVersionUID = 1L;

	private final String id;
	
	private String cachedName;
	
	private final DataItem dataItem;

	public ItemEntry(String id, DataItem dataItem) {
		this.id = id;
		this.dataItem = dataItem;
	}

	public String getId() {
		return id;
	}

	public DataItem getDataItem() {
		return dataItem;
	}
	
	public String getCachedName() {
		return cachedName;
	}
	
	public void setCachedName(String cachedName) {
		this.cachedName = cachedName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ItemEntry other = (ItemEntry) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ItemEntry [id=" + id + "]";
	}

	@Override
	public int compareTo(ItemEntry o) {
		return this.id.compareTo(o.id);
	}
}
