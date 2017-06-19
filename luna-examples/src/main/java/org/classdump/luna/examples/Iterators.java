package org.classdump.luna.examples;

import org.classdump.luna.StateContext;
import org.classdump.luna.Table;
import org.classdump.luna.Variable;
import org.classdump.luna.compiler.CompilerChunkLoader;
import org.classdump.luna.env.RuntimeEnvironments;
import org.classdump.luna.exec.CallException;
import org.classdump.luna.exec.CallPausedException;
import org.classdump.luna.exec.DirectCallExecutor;
import org.classdump.luna.impl.DefaultUserdata;
import org.classdump.luna.impl.ImmutableTable;
import org.classdump.luna.impl.NonsuspendableFunctionException;
import org.classdump.luna.impl.StateContexts;
import org.classdump.luna.lib.ArgumentIterator;
import org.classdump.luna.lib.BasicLib;
import org.classdump.luna.lib.StandardLibrary;
import org.classdump.luna.load.ChunkLoader;
import org.classdump.luna.load.LoaderException;
import org.classdump.luna.runtime.AbstractFunction1;
import org.classdump.luna.runtime.AbstractFunction2;
import org.classdump.luna.runtime.ExecutionContext;
import org.classdump.luna.runtime.LuaFunction;
import org.classdump.luna.runtime.ResolvedControlThrowable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class Iterators {

    public static void main(String[] args)
            throws InterruptedException, CallPausedException, CallException, LoaderException {
        String program = "local a = \"\"\n" +
                "for elem in pairs(listish) do a = a .. 1 .. \", \" end\n" +
                "return a";

        StateContext state = StateContexts.newDefaultInstance();
        Table env = StandardLibrary.in(RuntimeEnvironments.system()).installInto(state);
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        env.rawset("listish", new CollectionBridge(list));

        ChunkLoader loader = CompilerChunkLoader.of("call_from_lua");
        LuaFunction main = loader.loadTextChunk(new Variable(env), "", program);

        Object[] result = DirectCallExecutor.newExecutor().call(state, main);

        System.out.println("Result: " + result[0]);

    }

    private static class IteratorNext extends AbstractFunction2 {

        @Override
        public void invoke(ExecutionContext context, Object arg1, Object arg2) throws ResolvedControlThrowable {
            ListIterator iterator = (ListIterator) arg1;
            if (!iterator.hasNext()) {
                context.getReturnBuffer().setTo(null);
                return;
            }
            final int idx = iterator.nextIndex();
            final Object next = iterator.next();

            context.getReturnBuffer().setTo(idx, next);
        }

        @Override
        public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
            throw new NonsuspendableFunctionException();
        }
    }

    private static class CollectionBridge extends DefaultUserdata {
        private static final ImmutableTable META_TABLE = new ImmutableTable.Builder()
                .add(BasicLib.MT_PAIRS, new CollectionIteratorPairs())
                .build();

        /**
         * Constructs a new instance of this userdata with the specified initial {@code metatable}
         * and {@code userValue}.
         *
         * @param list initial user value, may be {@code null}
         */
        public CollectionBridge(List list) {
            super(META_TABLE, list);
        }

        private static class CollectionIteratorPairs extends AbstractFunction1 {
            @Override
            public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
                throw new NonsuspendableFunctionException();
            }

            @Override
            public void invoke(ExecutionContext context, Object arg1) throws ResolvedControlThrowable {
                CollectionBridge bridge = (CollectionBridge) arg1;
                final List list = (List) bridge.getUserValue();
                context.getReturnBuffer().setTo(new IteratorNext(), list.listIterator(), null);
            }
        }
    }
}
