package utils;

import java.util.function.Supplier;

public class CachedValue<T> {
	private T value;
	private boolean needsUpdating;
	private Supplier<T> updateFunction;
	
	public CachedValue(T value, Supplier<T> updateFunction) {
		this.value = value;
		this.updateFunction = updateFunction;
		this.needsUpdating = (value == null);
	}
	
	public CachedValue(Supplier<T> updateFunction) {
		this(null, updateFunction);
	}
	
	public T getValue() {
		if(needsUpdating) {
			value = updateFunction.get();
			needsUpdating = true;
		}
		return value;
	}
	
	public void needsUpdating() {
		this.needsUpdating = true;
	}
}
