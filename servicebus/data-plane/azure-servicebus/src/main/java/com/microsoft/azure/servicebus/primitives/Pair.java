package com.microsoft.azure.servicebus.primitives;

// Utility class to encapsulate any pair
public class Pair<T,V>
{
	private T t;
	private V v;
	
	Pair(T t, V v)
	{
		this.t= t;
		this.v = v;
	}

	public T getFirstItem() {
		return t;
	}

	public V getSecondItem() {
		return v;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((t == null) ? 0 : t.hashCode());
		result = prime * result + ((v == null) ? 0 : v.hashCode());
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
		Pair other = (Pair) obj;
		if (t == null) {
			if (other.t != null)
				return false;
		} else if (!t.equals(other.t))
			return false;
		if (v == null) {
			if (other.v != null)
				return false;
		} else if (!v.equals(other.v))
			return false;
		return true;
	}	
}
