package org.classdump.luna.examples;

import org.classdump.luna.Metatables;
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
import org.classdump.luna.lib.AbstractLibFunction;
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
import java.util.List;
import java.util.ListIterator;

public class IteratorsUserdata {

    public static void main(String[] args)
            throws InterruptedException, CallPausedException, CallException, LoaderException {
        String program = "local a = \"\"\n" +
                "for i, elem in ipairs(listish) do a = a .. i .. \" -> \".. elem .. \", \" end\n" +
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

    private static class IteratorNext extends AbstractLibFunction {

        @Override
        protected String name() {
            return "java_list_next";
        }

        @Override
        protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
            final ListIterator iterator = args.nextOfClass(ListIterator.class);
            if (!iterator.hasNext()) {
                context.getReturnBuffer().setTo(null);
                return;
            }
            final Object next = iterator.next();

            context.getReturnBuffer().setTo(next);
        }
    }

    private static class CollectionBridge extends DefaultUserdata {
        private static final ImmutableTable META_TABLE = new ImmutableTable.Builder()
                .add(BasicLib.MT_PAIRS, new CollectionIteratorPairs())
                .add(Metatables.MT_INDEX, new ListIndex())
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

        private List getList() {
            return (List) getUserValue();
        }

        private static class CollectionIteratorPairs extends AbstractFunction1 {
            @Override
            public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
                throw new NonsuspendableFunctionException();
            }

            @Override
            public void invoke(ExecutionContext context, Object arg1) throws ResolvedControlThrowable {
                final List list = ((CollectionBridge) arg1).getList();
                context.getReturnBuffer().setTo(new IteratorNext(), list.listIterator(), null);
            }
        }

        private static class ListIndex extends AbstractFunction2 {
            @Override
            public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
                throw new NonsuspendableFunctionException();
            }

            @Override
            public void invoke(ExecutionContext context, Object arg1, Object arg2) throws ResolvedControlThrowable {
                final List list = ((CollectionBridge) arg1).getList();
                final int index = ((Long) arg2).intValue() - 1;
                if (index == list.size()) {
                    context.getReturnBuffer().setTo(null);
                    return;
                }
                context.getReturnBuffer().setTo(list.get(index));
            }
        }
    }
}
