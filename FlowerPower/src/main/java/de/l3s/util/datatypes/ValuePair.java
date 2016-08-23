package de.l3s.util.datatypes;

public class ValuePair<K,V> {
	K key;
	V vaue;
	public ValuePair(K key, V vaue) {
		super();
		this.key = key;
		this.vaue = vaue;
	}
	public K getKey() {
		return key;
	}
	public V getValue() {
		return vaue;
	}
	
}
