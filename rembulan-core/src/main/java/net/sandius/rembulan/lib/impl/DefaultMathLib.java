package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Conversions;
import net.sandius.rembulan.core.Dispatch;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.NonsuspendableFunctionException;
import net.sandius.rembulan.core.impl.FunctionAnyarg;
import net.sandius.rembulan.lib.LibUtils;
import net.sandius.rembulan.lib.MathLib;
import net.sandius.rembulan.util.Check;

import java.util.Random;

public class DefaultMathLib extends MathLib {

	protected final Random random;

	public DefaultMathLib(Random random) {
		this.random = Check.notNull(random);
	}

	public DefaultMathLib() {
		this(new Random());
	}

	@Override
	public Function _abs() {
		return Abs.INSTANCE;
	}

	@Override
	public Function _acos() {
		return ACos.INSTANCE;
	}

	@Override
	public Function _asin() {
		return ASin.INSTANCE;
	}

	@Override
	public Function _atan() {
		return ATan.INSTANCE;
	}

	@Override
	public Function _ceil() {
		return Ceil.INSTANCE;
	}

	@Override
	public Function _cos() {
		return Cos.INSTANCE;
	}

	@Override
	public Function _deg() {
		return Deg.INSTANCE;
	}

	@Override
	public Function _exp() {
		return Exp.INSTANCE;
	}

	@Override
	public Function _floor() {
		return Floor.INSTANCE;
	}

	@Override
	public Function _fmod() {
		return FMod.INSTANCE;
	}

	@Override
	public Double _huge() {
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public Function _log() {
		return Log.INSTANCE;
	}

	@Override
	public Function _max() {
		return MaxMin.MAX_INSTANCE;
	}

	@Override
	public Long _maxinteger() {
		return Long.MAX_VALUE;
	}

	@Override
	public Function _min() {
		return MaxMin.MIN_INSTANCE;
	}

	@Override
	public Long _mininteger() {
		return Long.MIN_VALUE;
	}

	@Override
	public Function _modf() {
		return ModF.INSTANCE;
	}

	@Override
	public Double _pi() {
		return Math.PI;
	}

	@Override
	public Function _rad() {
		return Rad.INSTANCE;
	}

	@Override
	public Function _random() {
		return new Rand(random);
	}

	@Override
	public Function _randomseed() {
		return new RandSeed(random);
	}

	@Override
	public Function _sin() {
		return Sin.INSTANCE;
	}

	@Override
	public Function _sqrt() {
		return Sqrt.INSTANCE;
	}

	@Override
	public Function _tan() {
		return Tan.INSTANCE;
	}

	@Override
	public Function _tointeger() {
		return ToInteger.INSTANCE;
	}

	@Override
	public Function _type() {
		return Type.INSTANCE;
	}

	@Override
	public Function _ult() {
		return ULt.INSTANCE;
	}

	public static abstract class MathFunction1 extends FunctionAnyarg {

		protected abstract String name();

		protected abstract Number op(double x);

		protected Number op(long x) {
			return op((double) x);
		}

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Number x = LibUtils.checkNumber(name(), args, 0);
			Number result = x instanceof Float || x instanceof Double ? op(x.doubleValue()) : op(x.longValue());
			context.getObjectSink().setTo(result);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Abs extends MathFunction1 {

		public static final Abs INSTANCE = new Abs();

		@Override
		protected String name() {
			return "abs";
		}

		@Override
		protected Number op(double x) {
			return Math.abs(x);
		}

		@Override
		protected Number op(long x) {
			return Math.abs(x);
		}

	}

	public static class ACos extends MathFunction1 {

		public static final ACos INSTANCE = new ACos();

		@Override
		protected String name() {
			return "acos";
		}

		@Override
		protected Number op(double x) {
			return Math.acos(x);
		}

	}

	public static class ASin extends MathFunction1 {

		public static final ASin INSTANCE = new ASin();

		@Override
		protected String name() {
			return "asin";
		}

		@Override
		protected Number op(double x) {
			return Math.asin(x);
		}

	}

	public static class ATan extends MathFunction1 {

		public static final ATan INSTANCE = new ATan();

		@Override
		protected String name() {
			return "atan";
		}

		@Override
		protected Number op(double x) {
			return Math.atan(x);
		}

	}

	public static class Ceil extends MathFunction1 {

		public static final Ceil INSTANCE = new Ceil();

		@Override
		protected String name() {
			return "ceil";
		}

		@Override
		protected Number op(double x) {
			double d = Math.ceil(x);
			long l = (long) d;
			return d == (double) l ? l : d;
		}

		@Override
		protected Number op(long x) {
			return x;
		}

	}

	public static class Cos extends MathFunction1 {

		public static final Cos INSTANCE = new Cos();

		@Override
		protected String name() {
			return "cos";
		}

		@Override
		protected Number op(double x) {
			return Math.cos(x);
		}

	}

	public static class Deg extends MathFunction1 {

		public static final Deg INSTANCE = new Deg();

		@Override
		protected String name() {
			return "deg";
		}

		@Override
		protected Number op(double x) {
			return Math.toDegrees(x);
		}

	}

	public static class Exp extends MathFunction1 {

		public static final Exp INSTANCE = new Exp();

		@Override
		protected String name() {
			return "exp";
		}

		@Override
		protected Number op(double x) {
			return Math.exp(x);
		}

	}

	public static class Floor extends MathFunction1 {

		public static final Floor INSTANCE = new Floor();

		@Override
		protected String name() {
			return "floor";
		}

		@Override
		protected Number op(double x) {
			double d = Math.floor(x);
			long l = (long) d;
			return d == (double) l ? l : d;
		}

		@Override
		protected Number op(long x) {
			return x;
		}

	}

	public static class FMod extends FunctionAnyarg {

		public static final FMod INSTANCE = new FMod();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Number x = LibUtils.checkNumber("fmod", args, 0);
			Number y = LibUtils.checkNumber("fmod", args, 1);

			final Number result;

			if (x instanceof Float || x instanceof Double
					|| y instanceof Float || y instanceof Double) {

				result = Math.IEEEremainder(x.doubleValue(), y.doubleValue());
			}
			else {
				long xi = x.longValue();
				long yi = y.longValue();

				if (yi != 0) {
					result = xi % yi;
				}
				else {
					throw new IllegalArgumentException("bad argument #2 to 'fmod' (zero)");
				}
			}

			context.getObjectSink().setTo(result);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Log extends FunctionAnyarg {

		public static final Log INSTANCE = new Log();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Number x = LibUtils.checkNumber("log", args, 0);
			double ln = Math.log(x.doubleValue());
			final double result;

			if (args.length > 1) {
				// explicit base
				double base = LibUtils.checkNumber("log", args, 1).doubleValue();
				result = ln / Math.log(base);
			}
			else {
				// no base specified
				result = ln;
			}

			context.getObjectSink().setTo(result);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class MaxMin extends FunctionAnyarg {

		private final boolean isMax;

		public static final MaxMin MAX_INSTANCE = new MaxMin(true);
		public static final MaxMin MIN_INSTANCE = new MaxMin(false);

		public MaxMin(boolean isMax) {
			this.isMax = isMax;
		}

		private static class State {

			public final Object[] args;
			public final int idx;
			public final Object best;

			public State(Object[] args, int idx, Object best) {
				this.args = args;
				this.idx = idx;
				this.best = best;
			}

		}

		private void run(ExecutionContext context, Object[] args, int idx, Object best) throws ControlThrowable {
			for ( ; idx < args.length; idx++) {
				Object o = args[idx];

				try {
					if (isMax) {
						Dispatch.lt(context, best, o);
					}
					else {
						Dispatch.lt(context, o, best);
					}
				}
				catch (ControlThrowable ct) {
					ct.push(this, new State(args, idx, best));
					throw ct;
				}

				if (Conversions.objectToBoolean(context.getObjectSink()._0())) {
					best = o;
				}
			}

			// we're done
			context.getObjectSink().setTo(best);
		}

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			if (args.length == 0) {
				String name = isMax ? "max" : "min";
				throw new IllegalArgumentException("bad argument #1 to '" + name + "' (value expected)");
			}

			run(context, args, 1, args[0]);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			State ss = (State) suspendedState;

			Object[] args = ss.args;
			int idx = ss.idx;
			Object best = ss.best;

			// best <> args[idx] comparison has just finished
			if (Conversions.objectToBoolean(context.getObjectSink()._0())) {
				best = args[idx];
			}

			run(context, args, idx + 1, best);
		}

	}

	public static class ModF extends FunctionAnyarg {

		public static final ModF INSTANCE = new ModF();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Number x = LibUtils.checkNumber("modf", args, 0);

			long intPart = x.longValue();
			double fltPart = x.doubleValue() - intPart;

			context.getObjectSink().setTo(intPart, fltPart);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Rad extends MathFunction1 {

		public static final Rad INSTANCE = new Rad();

		@Override
		protected String name() {
			return "rad";
		}

		@Override
		protected Number op(double x) {
			return Math.toRadians(x);
		}

	}

	public static class Rand extends FunctionAnyarg {

		protected final Random random;

		public Rand(Random random) {
			this.random = Check.notNull(random);
		}

		// return a long in the range [0, n)
		protected long nextLong(long n) {
			Check.nonNegative(n);
			if (n <= Integer.MAX_VALUE) {
				return random.nextInt((int) n);
			}
			else {
				long bits;
				long val;
				do {
					bits = random.nextLong() & Long.MAX_VALUE;
					val = bits % n;
				} while (bits - val + (n - 1) < 0);
				return val;
			}
		}

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			final Number result;

			switch (args.length) {

				// float in the range [0.0, 1.0)
				case 0:
					result = random.nextDouble();
					break;

				// integer in the range [1, m]
				case 1: {
					long m = LibUtils.checkInteger("random", args, 0);
					if (m < 1) {
						throw new IllegalArgumentException("bad argument #1 to 'random' (interval is empty)");
					}
					result = 1L + nextLong(m);
					break;
				}

				// integer in the range [m, n]
				case 2:
				default: {
					long m = LibUtils.checkInteger("random", args, 0);
					long n = LibUtils.checkInteger("random", args, 1);

					if (n < m) {
						throw new IllegalArgumentException("bad argument #1 to 'random' (interval is empty)");
					}

					long limit = n - m + 1;  // including the upper bound

					if (limit < 0) {
						throw new IllegalArgumentException("bad argument #1 to 'random' (interval too large)");
					}

					result = m + nextLong(limit);
					break;
				}
			}

			context.getObjectSink().setTo(result);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class RandSeed extends FunctionAnyarg {

		protected final Random random;

		public RandSeed(Random random) {
			this.random = Check.notNull(random);
		}

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Number arg = LibUtils.checkInteger("randomseed", args, 0);

			long seed = arg instanceof Double || arg instanceof Float
					? Double.doubleToLongBits(arg.doubleValue())
					: arg.longValue();

			random.setSeed(seed);

			context.getObjectSink().reset();
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Sin extends MathFunction1 {

		public static final Sin INSTANCE = new Sin();

		@Override
		protected String name() {
			return "sin";
		}

		@Override
		protected Number op(double x) {
			return Math.sin(x);
		}

	}

	public static class Sqrt extends MathFunction1 {

		public static final Sqrt INSTANCE = new Sqrt();

		@Override
		protected String name() {
			return "sqrt";
		}

		@Override
		protected Number op(double x) {
			return Math.sqrt(x);
		}

	}

	public static class Tan extends MathFunction1 {

		public static final Tan INSTANCE = new Tan();

		@Override
		protected String name() {
			return "tan";
		}

		@Override
		protected Number op(double x) {
			return Math.tan(x);
		}

	}

	public static class ToInteger extends FunctionAnyarg {

		public static final ToInteger INSTANCE = new ToInteger();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Object x = LibUtils.checkValue("tointeger", args, 0);
			context.getObjectSink().setTo(Conversions.objectAsLong(x));
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Type extends FunctionAnyarg {

		public static final Type INSTANCE = new Type();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Object x = LibUtils.checkValue("type", args, 0);

			String result = x instanceof Number
					? (x instanceof Float || x instanceof Double
							? "float"
							: "integer")
					: null;

			context.getObjectSink().setTo(result);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class ULt extends FunctionAnyarg {

		public static final ULt INSTANCE = new ULt();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			long x = LibUtils.checkInteger("ult", args, 0);
			long y = LibUtils.checkInteger("ult", args, 1);
			context.getObjectSink().setTo((x - y) < 0);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

}