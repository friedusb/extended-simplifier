package silAST.expressions

import scala.collection.Seq
import silAST.source.SourceLocation
import silAST.symbols.logical.BinaryBooleanOperator
import silAST.ASTNode
import silAST.expressions.terms.GTerm

class BinaryBooleanExpression[+T <: GTerm[T]](
		sl : SourceLocation,
		val operator : BinaryBooleanOperator,
		val expression1 : GExpression[T],
		val expression2 : GExpression[T]
    )
    extends GExpression[T](sl) 
{

	override def toString : String = expression1.toString + " " + operator.toString + " " + expression2.toString
	
	override def subNodes : Seq[ASTNode] = expression1 :: (operator :: (expression2 :: Nil))
	override def subExpressions: Seq[GExpression[T]] = expression1 :: expression2 :: Nil

}