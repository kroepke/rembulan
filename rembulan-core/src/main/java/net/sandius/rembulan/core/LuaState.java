package net.sandius.rembulan.core;

public abstract class LuaState {

	private static final ThreadLocal<LuaState> current = new ThreadLocal<LuaState>();

	public static LuaState getCurrentState() {
		return current.get();
	}

	public abstract Table nilMetatable();
	public abstract Table booleanMetatable();
	public abstract Table numberMetatable();
	public abstract Table stringMetatable();
	public abstract Table functionMetatable();
	public abstract Table threadMetatable();
	public abstract Table lightuserdataMetatable();

	public abstract boolean shouldPreemptNow();

	public abstract Coroutine getCurrentCoroutine();

}