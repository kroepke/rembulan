package net.sandius.rembulan.parser

import java.io.{ByteArrayInputStream, PrintWriter}

import net.sandius.rembulan.compiler.analysis.{BranchInlinerVisitor, TypeInfo, TyperVisitor}
import net.sandius.rembulan.compiler.ir.Branch
import net.sandius.rembulan.compiler.{Blocks, BlocksVisitor, CPUAccountingVisitor, IRTranslatorTransformer}
import net.sandius.rembulan.compiler.util.{BlocksSimplifier, IRPrinterVisitor, TempUseVerifierVisitor}
import net.sandius.rembulan.parser.analysis.NameResolutionTransformer
import net.sandius.rembulan.parser.ast.{Chunk, Expr}
import net.sandius.rembulan.test._
import org.junit.runner.RunWith
import org.scalatest.{FunSpec, MustMatchers}
import org.scalatest.junit.JUnitRunner

import scala.collection.JavaConverters._

@RunWith(classOf[JUnitRunner])
class IRTranslationTest extends FunSpec with MustMatchers {

  val bundles = Seq(
    BasicFragments,
    BasicLibFragments,
    CoroutineFragments,
    DebugLibFragments,
    IOLibFragments,
    MathFragments,
    MetatableFragments,
    OperatorFragments,
    StringFragments,
    TableLibFragments
  )

  def parseExpr(s: String): Expr = {
    val bais = new ByteArrayInputStream(s.getBytes)
    val parser = new Parser(bais)
    val result = parser.Expr()
    parser.Eof()
    result
  }

  def parseChunk(s: String): Chunk = {
    val bais = new ByteArrayInputStream(s.getBytes)
    val parser = new Parser(bais)
    parser.Chunk()
  }

  def resolveNames(c: Chunk): Chunk = {
    new NameResolutionTransformer().transform(c)
  }

  def verify(blocks: Blocks): Unit = {
    val visitor = new BlocksVisitor(new TempUseVerifierVisitor())
    visitor.visit(blocks)
  }

  def insertCpuAccounting(blocks: Blocks): Blocks = {
    val visitor = new CPUAccountingVisitor()
    visitor.visit(blocks)
    visitor.result()
  }

  def assignTypes(blocks: Blocks): TypeInfo = {
    val visitor = new TyperVisitor()
    visitor.visit(blocks)
    visitor.valTypes()
  }

  def inlineBranches(blocks: Blocks, types: TypeInfo): Blocks = {
    val visitor = new BranchInlinerVisitor(types)
    visitor.visit(blocks)
    visitor.result()
  }

  def printTypes(types: TypeInfo): Unit = {
    for (v <- types.vals().asScala) {
      println(v + " -> " + types.typeOf(v))
    }
  }

  def printBlocks(blocks: Blocks): Unit = {
    val pw = new PrintWriter(System.out)
    val printer = new IRPrinterVisitor(pw)
    printer.visit(blocks)
    pw.flush()

    println()
  }

  describe ("expression") {

    describe ("can be translated to IR:") {

      for ((s, o) <- Expressions.get if o) {

        val ss = "return " + s

        it (ss) {
          val ck = resolveNames(parseChunk(ss))

          val translator = new IRTranslatorTransformer()
          translator.transform(ck)
          val blocks = translator.blocks()
          printBlocks(blocks)

          verify(blocks)
        }

      }

    }

  }

  for (b <- bundles) {
    describe ("from " + b.name + " :") {
      for (f <- b.all) {
        describe (f.description) {
          it ("can be translated to IR") {
            val code = f.code

            println("--BEGIN--")
            println(code)
            println("---END---")

            val ck = resolveNames(parseChunk(code))

            val translator = new IRTranslatorTransformer()
            translator.transform(ck)
            val blocks = translator.blocks()

            val withCpu = insertCpuAccounting(blocks)

            val types = assignTypes(withCpu)
            val inlined = inlineBranches(withCpu, types)
            val filtered = BlocksSimplifier.filterUnreachableBlocks(inlined)
            val merged = BlocksSimplifier.mergeBlocks(filtered)

            printBlocks(merged)
            printTypes(types)

            verify(blocks)
          }
        }
      }
    }
  }


}
