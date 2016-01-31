package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.Slots;
import net.sandius.rembulan.compiler.gen.Slots.SlotType;
import net.sandius.rembulan.lbc.OpCode;
import net.sandius.rembulan.lbc.Prototype;

import java.util.Objects;

public class LuaInstruction {

	public static SlotType constantType(Object k) {
		if (k == null) return SlotType.NIL;
		else if (k instanceof Boolean) return SlotType.BOOLEAN;
		else if (k instanceof Double || k instanceof Float) return SlotType.NUMBER_FLOAT;
		else if (k instanceof Number) return SlotType.NUMBER_INTEGER;
		else if (k instanceof String) return SlotType.STRING;
		else {
			throw new IllegalStateException("Unknown constant: " + k);
		}
	}

	public static class Move extends Linear {
		public final int dest;
		public final int src;

		public Move(int dest, int src) {
			this.dest = dest;
			this.src = src;
		}

		@Override
		public String toString() {
			return "MOVE(" + dest + "," + src + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			return in.updateType(dest, in.getType(src));
		}

	}

	public static class LoadK extends Linear {

		public final Prototype prototype;

		public final int dest;
		public final int kIndex;

		public LoadK(Prototype prototype, int dest, int kIndex) {
			this.prototype = Objects.requireNonNull(prototype);
			this.dest = dest;
			this.kIndex = kIndex;
		}

		@Override
		public String toString() {
			return "LOADK(" + dest + "," + kIndex + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			return in.updateType(dest, constantType(prototype.getConstants().get(kIndex)));
		}

	}

	public static class LoadNil extends Linear {
		public final int dest;
		public final int lastOffset;

		public LoadNil(int dest, int lastOffset) {
			this.dest = dest;
			this.lastOffset = lastOffset;
		}

		@Override
		public String toString() {
			return "LOADNIL(" + dest + "," + lastOffset + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			Slots s = in;
			for (int i = dest; i <= dest + lastOffset; i++) {
				s = s.updateType(i, SlotType.NIL);
			}
			return s;
		}

	}

	public static class LoadBool extends Linear {
		public final int dest;
		public final int arg;

		public LoadBool(int dest, int arg) {
			this.dest = dest;
			this.arg = arg;
		}

		@Override
		public String toString() {
			return "LOADBOOL(" + dest + "," + arg + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			return in.updateType(dest, SlotType.BOOLEAN);
		}
	}

	public enum BinOpType {
		ADD, SUB, MUL, MOD, POW, DIV, IDIV, BAND, BOR, BXOR, SHL, SHR;

		public static BinOpType fromOpcode(int opcode) {
			switch (opcode) {
				case OpCode.ADD:  return BinOpType.ADD;
				case OpCode.SUB:  return BinOpType.SUB;
				case OpCode.MUL:  return BinOpType.MUL;
				case OpCode.MOD:  return BinOpType.MOD;
				case OpCode.POW:  return BinOpType.POW;
				case OpCode.DIV:  return BinOpType.DIV;
				case OpCode.IDIV: return BinOpType.IDIV;
				case OpCode.BAND: return BinOpType.BAND;
				case OpCode.BOR:  return BinOpType.BOR;
				case OpCode.BXOR: return BinOpType.BXOR;
				case OpCode.SHL:  return BinOpType.SHL;
				case OpCode.SHR:  return BinOpType.SHR;
				default: return null;
			}
		}
	}

	public enum UnOpType {
		UNM, BNOT, NOT, LEN;

		public static UnOpType fromOpcode(int opcode) {
			switch (opcode) {
				case OpCode.UNM:  return UnOpType.UNM;
				case OpCode.BNOT: return UnOpType.BNOT;
				case OpCode.NOT:  return UnOpType.NOT;
				case OpCode.LEN:  return UnOpType.LEN;
				default: return null;
			}
		}
	}

	public static class BinOp extends Linear {

		public final Prototype prototype;

		public final BinOpType op;
		public final int dest;
		public final int b;
		public final int c;

		public BinOp(Prototype prototype, BinOpType op, int dest, int b, int c) {
			this.prototype = Objects.requireNonNull(prototype);
			this.op = Objects.requireNonNull(op);
			this.dest = dest;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return op.toString() + opType(inSlots()).toSuffix() + "(" + dest + "," + b + "," + c + ")";
		}

		private NumOpType opType(Slots in) {
			SlotType lType = b < 0 ? constantType(prototype.getConstants().get(-b - 1)) : in.getType(b);
			SlotType rType = c < 0 ? constantType(prototype.getConstants().get(-c - 1)) : in.getType(c);

			if (lType.isNumber() && rType.isNumber()) {
				boolean lInteger = lType == SlotType.NUMBER_INTEGER;
				boolean rInteger = rType == SlotType.NUMBER_INTEGER;

				boolean lFloat = lType == SlotType.NUMBER_FLOAT;
				boolean rFloat = rType == SlotType.NUMBER_FLOAT;

				switch (op) {
					case ADD:
					case SUB:
					case MUL:
					case MOD:
					case IDIV:
						if (lInteger && rInteger) return NumOpType.Integer;
						else if (lFloat || rFloat) return NumOpType.Float;
						else return NumOpType.Number;

					case DIV:
					case POW:
						return NumOpType.Float;

					case BAND:
					case BOR:
					case BXOR:
					case SHL:
					case SHR:
						return NumOpType.Integer;
				}
			}

			// we might be calling a metamethod
			return NumOpType.Any;
		}

		@Override
		protected Slots effect(Slots in) {
			SlotType lType = b < 0 ? constantType(prototype.getConstants().get(-b - 1)) : in.getType(b);
			SlotType rType = c < 0 ? constantType(prototype.getConstants().get(-c - 1)) : in.getType(c);

			SlotType resultType = SlotType.ANY;  // assume we'll be calling a metamethod

			if (lType.isNumber() && rType.isNumber()) {
				boolean lInteger = lType == SlotType.NUMBER_INTEGER;
				boolean rInteger = rType == SlotType.NUMBER_INTEGER;

				boolean lFloat = lType == SlotType.NUMBER_FLOAT;
				boolean rFloat = rType == SlotType.NUMBER_FLOAT;

				// it's a number in any case
				resultType = SlotType.NUMBER;

				switch (op) {
					case ADD:
					case SUB:
					case MUL:
					case MOD:
					case IDIV:
						if (lInteger && rInteger) resultType = SlotType.NUMBER_INTEGER;
						else if (lFloat || rFloat) resultType = SlotType.NUMBER_FLOAT;
						break;

					case DIV:
					case POW:
						resultType = SlotType.NUMBER_FLOAT;
						break;

					case BAND:
					case BOR:
					case BXOR:
					case SHL:
					case SHR:
						resultType = SlotType.NUMBER_INTEGER;
						break;
				}
			}

			return in.updateType(dest, resultType);
		}

	}

	public static class UnOp extends Linear {

		public final Prototype prototype;

		public final UnOpType op;
		public final int dest;
		public final int b;

		public UnOp(Prototype prototype, UnOpType op, int dest, int b) {
			this.prototype = Objects.requireNonNull(prototype);
			this.op = Objects.requireNonNull(op);
			this.dest = dest;
			this.b = b;
		}

		@Override
		public String toString() {
			return op.toString() + "(" + dest + "," + b + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			SlotType argType = b < 0 ? constantType(prototype.getConstants().get(-b - 1)) : in.getType(b);

			SlotType resultType = SlotType.ANY;  // assume we'll be calling a metamethod

			switch (op) {
				case UNM:
					if (argType.isNumber()) resultType = argType;
					break;

				case BNOT:
					if (argType == SlotType.NUMBER_INTEGER) resultType = argType;
					break;

				case NOT:
					resultType = SlotType.BOOLEAN;
					break;

				case LEN:
					if (argType == SlotType.STRING) resultType = SlotType.NUMBER_INTEGER;
					break;
			}

			return in.updateType(dest, resultType);
		}
	}

	public static class Concat extends Linear {

		public final int a;
		public final int b;
		public final int c;

		public Concat(int a, int b, int c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return "CONCAT(" + a + "," + b + "," + c + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			throw new UnsupportedOperationException();  // TODO
		}

	}

	public static class Call extends Linear {

		public final int a;
		public final int b;
		public final int c;

		public Call(int a, int b, int c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return "CALL(" + a + "," + b + "," + c + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			Slots s = in;

			// Since we don't know what the called function does, we must
			// assume that it may change any open upvalue.
			for (int i = 0; i < in.size(); i++) {
				if (s.getState(i).isCaptured()) {
					s = s.updateType(i, SlotType.ANY);
				}
			}

			if (c > 0) {
				// (c - 1) is the exact number of result values
				for (int i = a; i < a + c - 1; i++) {
					s = s.updateType(i, SlotType.ANY);
				}
			}
			else {
				// variable number of results
				for (int i = a; i < in.size(); i++) {
					s = s.updateType(i, SlotType.ANY);
				}
			}

			return s;
		}

	}

	public enum NumOpType {
		Integer,
		Float,
		Number,
		Any;

		public String toSuffix() {
			switch (this) {
				case Integer: return "_i";
				case Float:   return "_f";
				case Number:  return "_N";
				case Any:
				default:      return "";
			}
		}
	}

	public static class ForPrep extends Linear {
		public final int a;
		public final int b;

		private enum LoopType {
		}

		public ForPrep(int a, int b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return "FORPREP" + loopType(inSlots()).toSuffix() + "(" + a + "," + b + ")";
		}

		private NumOpType loopType(Slots in) {
			SlotType a0 = in.getType(a + 0);
			SlotType a1 = in.getType(a + 1);
			SlotType a2 = in.getType(a + 2);

			if (a0 == SlotType.NUMBER_INTEGER
					&& a1 == SlotType.NUMBER_INTEGER
					&& a2 == SlotType.NUMBER_INTEGER) {

				return NumOpType.Integer;
			}
			else if (a0.isNumber()
					&& a1.isNumber()
					&& a2.isNumber()) {

				return NumOpType.Float;
			}
			else {
				// unable to determine at compile-time, we can nevertheless expect
				// this to be a numeric loop -- will throw an error otherwise
				return NumOpType.Number;
			}
		}

		@Override
		protected Slots effect(Slots in) {
			NumOpType tpe = loopType(in);
			switch (tpe) {
				case Integer: return in.updateType(a + 3, SlotType.NUMBER_INTEGER);
				case Float:   return in.updateType(a + 3, SlotType.NUMBER_FLOAT);
				default:      return in.updateType(a + 3, SlotType.NUMBER);
			}
		}

	}


	public static class GetUpVal extends Linear {
		public final int a;
		public final int b;

		public GetUpVal(int a, int b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return "GETUPVAL(" + a + "," + b + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			return in.updateType(a, SlotType.ANY);
		}

	}

	public static class GetTabUp extends Linear {
		public final int a;
		public final int b;

		public GetTabUp(int a, int b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return "GETTABUP(" + a + "," + b + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			return in.updateType(a, SlotType.ANY);
		}

	}

	public static class GetTable extends Linear {

		public final int a;
		public final int b;
		public final int c;

		public GetTable(int a, int b, int c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return "GETTABLE(" + a + "," + b + "," + c + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			return in.updateType(a, SlotType.ANY);
		}

	}

	public static class SetTabUp extends Linear {

		public final int a;
		public final int b;
		public final int c;

		public SetTabUp(int a, int b, int c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return "SETTABUP(" + a + "," + b + "," + c + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			// TODO: this might have a possible effect?
			return in;
		}

	}

	public static class SetUpVal extends Linear {
		public final int a;
		public final int b;

		public SetUpVal(int a, int b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return "SETUPVAL(" + a + "," + b + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			// TODO: this might have a possible effect?
			return in;
		}

	}

	public static class SetTable extends Linear {
		public final int a;
		public final int b;
		public final int c;

		public SetTable(int a, int b, int c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return "SETTABLE(" + a + "," + b + "," + c + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			return in;
		}

	}

	public static class NewTable extends Linear {
		public final int a;
		public final int b;
		public final int c;

		public NewTable(int a, int b, int c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return "NEWTABLE(" + a + "," + b + "," + c + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			return in.updateType(a, SlotType.TABLE);
		}

	}

	public static class Eq extends Branch {
		public final boolean pos;
		public final int b;
		public final int c;

		public Eq(Target trueBranch, Target falseBranch, boolean pos, int b, int c) {
			super(trueBranch, falseBranch);
			this.pos = pos;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return (pos ? "EQ" : "NOT-EQ") +  "(" + b + "," + c + ")";
		}

	}

	public static class Lt extends Branch {
		public final boolean pos;
		public final int b;
		public final int c;

		public Lt(Target trueBranch, Target falseBranch, boolean pos, int b, int c) {
			super(trueBranch, falseBranch);
			this.pos = pos;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return (pos ? "LT" : "NOT-LT") + "(" + b + "," + c + ")";
		}

	}

	public static class Le extends Branch {
		public final boolean pos;
		public final int b;
		public final int c;

		public Le(Target trueBranch, Target falseBranch, boolean pos, int b, int c) {
			super(trueBranch, falseBranch);
			this.pos = pos;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return (pos ? "LE" : "NOT-LE") + "(" + b + "," + c + ")";
		}

	}

	public static class ForLoop extends Branch {
		public final int a;
		public final int sbx;

		public ForLoop(Target trueBranch, Target falseBranch, int a, int sbx) {
			super(trueBranch, falseBranch);
			this.a = a;
			this.sbx = sbx;
		}

		@Override
		public String toString() {
			return "FORLOOP(" + a + "," + sbx + ")";
		}

		// TODO: can also be specialised

	}

	public static class Closure extends Linear implements LocalVariableEffect {

		public final Prototype prototype;

		public final int dest;
		public final int index;

		public Closure(Prototype prototype, int dest, int index) {
			this.prototype = Objects.requireNonNull(prototype);
			this.dest = dest;
			this.index = index;
		}

		@Override
		public String toString() {
			return "CLOSURE(" + dest + "," + index + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			Slots s = in.updateType(dest, SlotType.FUNCTION);

			Prototype p = prototype.getNestedPrototypes().get(index);
			for (Prototype.UpvalueDesc uvd : p.getUpValueDescriptions()) {
				if (uvd.inStack) {
					s = s.capture(uvd.index);
				}
			}

			return s;
		}

	}

	public static class Return extends Exit {
		public final int a;
		public final int b;

		public Return(int a, int b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return "RETURN(" + a + "," + b + ")";
		}


	}

	public static class TailCall extends Exit {
		public final int a;
		public final int b;
		public final int c;

		public TailCall(int a, int b, int c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return "TAILCALL(" + a + "," + b + "," + c + ")";
		}

	}

}