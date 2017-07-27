/*
 * Copyright 2016 Miroslav Janíček
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

package org.classdump.luna.test.fragments

import org.classdump.luna.test.FragmentExecTestSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BasicFragmentsRunSpec extends FragmentExecTestSuite {

  override def bundles = Seq(BasicFragments)

  override def expectations = Seq(BasicFragments)

  override def contexts = Seq(Empty, Basic)

  override def steps = Seq(1, Int.MaxValue)

  override def compilerConfigs = CompilerConfigs.All

}
