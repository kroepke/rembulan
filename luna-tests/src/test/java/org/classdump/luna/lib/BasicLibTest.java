/*
 * Copyright 2017 Kay Roepke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.classdump.luna.lib;

import org.classdump.luna.Metatables;
import org.classdump.luna.StateContext;
import org.classdump.luna.Table;
import org.classdump.luna.Variable;
import org.classdump.luna.compiler.CompilerChunkLoader;
import org.classdump.luna.env.RuntimeEnvironments;
import org.classdump.luna.exec.DirectCallExecutor;
import org.classdump.luna.impl.DefaultUserdata;
import org.classdump.luna.impl.ImmutableTable;
import org.classdump.luna.impl.StateContexts;
import org.classdump.luna.load.ChunkLoader;
import org.classdump.luna.runtime.ExecutionContext;
import org.classdump.luna.runtime.LuaFunction;
import org.classdump.luna.runtime.ResolvedControlThrowable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicLibTest {

    private static class PairsList extends DefaultUserdata {
        private static final ImmutableTable META_TABLE = new ImmutableTable.Builder()
                .add(BasicLib.MT_PAIRS, new Pairs())
                .add(Metatables.MT_INDEX, new Index())
                .build();

        public PairsList() {
            super(META_TABLE, new ArrayList<String>() {{
                this.add("a");
                this.add("b");
                this.add("c");
                this.add("d");
            }});
        }

        private List getList() {
            return (List) getUserValue();
        }

        private static class Pairs extends AbstractLibFunction {
            @Override
            protected String name() {
                return "list_pairs";
            }

            @Override
            protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
                final List list = args.nextOfClass(PairsList.class).getList();
                context.getReturnBuffer().setTo(new Next(), list.listIterator(), null);
            }
        }

        private static class Index extends AbstractLibFunction {
            @Override
            protected String name() {
                return "list_index";
            }

            @Override
            protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
                final List list = args.nextOfClass(PairsList.class).getList();
                final int index = args.nextInt() - 1;
                if (index == list.size()) {
                    context.getReturnBuffer().setTo(null);
                    return;
                }
                context.getReturnBuffer().setTo(list.get(index));
            }
        }

        private static class Next extends AbstractLibFunction {
            @Override
            protected String name() {
                return "list_next";
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
    }

    @Test
    public void ipairsWithUserdata() throws Exception {
        String program = "local a = {}\n" +
                "for i, elem in ipairs(list) do a[elem] = i end\n" +
                "return a";

        StateContext state = StateContexts.newDefaultInstance();
        Table env = StandardLibrary.in(RuntimeEnvironments.system()).installInto(state);
        env.rawset("list", new PairsList());

        ChunkLoader loader = CompilerChunkLoader.of("call_from_lua");
        LuaFunction main = loader.loadTextChunk(new Variable(env), "", program);

        Object[] result = DirectCallExecutor.newExecutor().call(state, main);

        Table table = (Table) result[0];

        assertThat(table.rawget("a")).isEqualTo(1L);
        assertThat(table.rawget("b")).isEqualTo(2L);
        assertThat(table.rawget("c")).isEqualTo(3L);
        assertThat(table.rawget("d")).isEqualTo(4L);
    }

    @Test
    public void pairsWithUserdata() throws Exception {
        String program = "local a = {}\n" +
                "for elem in pairs(list) do print(elem); a[elem] = elem end\n" +
                "return a";

        StateContext state = StateContexts.newDefaultInstance();
        Table env = StandardLibrary.in(RuntimeEnvironments.system()).installInto(state);
        env.rawset("list", new PairsList());

        ChunkLoader loader = CompilerChunkLoader.of("call_from_lua");
        LuaFunction main = loader.loadTextChunk(new Variable(env), "", program);

        Object[] result = DirectCallExecutor.newExecutor().call(state, main);

        Table table = (Table) result[0];

        // this .toString() is a bit annoying but strings from Lua are ByteStrings..
        assertThat(table.rawget("a").toString()).isEqualTo("a");
        assertThat(table.rawget("b").toString()).isEqualTo("b");
        assertThat(table.rawget("c").toString()).isEqualTo("c");
        assertThat(table.rawget("d").toString()).isEqualTo("d");
    }

}