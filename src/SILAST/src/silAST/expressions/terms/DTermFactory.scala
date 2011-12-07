package silAST.expressions.terms

import silAST.programs.NodeFactory
import silAST.source.SourceLocation
import silAST.symbols.logical.quantification.BoundVariable
import collection.mutable.HashSet
import silAST.domains.DomainFunction
import silAST.types.{DataTypeFactory, DataType}
import silAST.expressions.util.{DTermSequenceC, DTermSequence, GTermSequence}


trait DTermFactory extends NodeFactory with GTermFactory with DataTypeFactory{

  /////////////////////////////////////////////////////////////////////////
  def makeBoundVariable(sl:SourceLocation,name : String,  dataType : DataType) : BoundVariable =
  {
    require(dataTypes.contains(dataType))
    val result : BoundVariable = new BoundVariable(sl, name, dataType)
    boundVariables += result
    result
  }

  /////////////////////////////////////////////////////////////////////////
  def makeBoundVariableTerm(sl : SourceLocation, v : BoundVariable) : BoundVariableTerm =
  {
    require(boundVariables.contains(v))
    addTerm(new BoundVariableTerm(sl,v))
  }

  /////////////////////////////////////////////////////////////////////////
  def makeDDomainFunctionApplicationTerm(sl : SourceLocation, f : DomainFunction, a : DTermSequence) : DDomainFunctionApplicationTerm =
  {
    require(termSequences.contains(a))
    require(domainFunctions(f.name) == f)

    a match{
      case a : GTermSequence => makeGDomainFunctionApplicationTerm(sl,f,a)
      case _ => addTerm(new DDomainFunctionApplicationTermC(sl,f,a))
    }
  }

  /////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////
  def makeDTermSequence(sl : SourceLocation, ts : Seq[DTerm]) : DTermSequence = {
    require( ts.toSet subsetOf terms)
    val result = new DTermSequenceC(sl,ts)
    termSequences += result
    result
  }
  /////////////////////////////////////////////////////////////////////////
  protected[silAST] val boundVariables = new HashSet[BoundVariable]
}