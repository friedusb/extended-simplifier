// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2011-2022 ETH Zurich.

package viper.silver.plugin.standard.adt

import viper.silver.ast.pretty.FastPrettyPrinter.{ContOps, braces, brackets, char, defaultIndent, line, nest, nil, parens, show, showVars, space, ssep, text}
import viper.silver.ast.pretty.PrettyPrintPrimitives
import viper.silver.ast._

/**
  * This class represents a user-defined ADT.
  *
  * @param name name of the ADT
  * @param constructors sequence of corresponding constructors
  * @param typVars sequence of type variables (generics)
  */
case class Adt(name: String, constructors: Seq[AdtConstructor], typVars: Seq[TypeVar] = Nil)
                 (val pos: Position = NoPosition, val info: Info = NoInfo, val errT: ErrorTrafo = NoTrafos) extends ExtensionMember {
  val scopedDecls: Seq[Declaration] = Seq()

  override def extensionSubnodes: Seq[Node] = constructors ++ typVars

  override def prettyPrint: PrettyPrintPrimitives#Cont = {
    text("adt") <+> name <>
      (if (typVars.isEmpty) nil else text("[") <> ssep(typVars map show, char (',') <> space) <> "]") <+>
      braces(nest(defaultIndent,
        line <> line <>
          ssep(constructors map show, line <> line)
      ) <> line)
  }
}

/**
  * This class represents an ADT constructor. See also the companion object below, which allows passing a
  * Adt - this should be used in general for creation (so that typ is guaranteed to
  * be set correctly)
  *
  * @param name name of the ADT constructor
  * @param formalArgs sequence of arguments of the constructor
  * @param typ the return type of the constructor
  * @param adtName the name corresponding of the corresponding ADT
  */
case class AdtConstructor(name: String, formalArgs: Seq[AnyLocalVarDecl])
                     (val pos: Position, val info: Info, typ: AdtType, val adtName : String, val errT: ErrorTrafo)
  extends ExtensionMember {

  override def getMetadata:Seq[Any] = {
    Seq(pos, info, errT)
  }
  val scopedDecls: Seq[Declaration] = formalArgs.filter(p => p.isInstanceOf[LocalVarDecl]).asInstanceOf[Seq[LocalVarDecl]]

  override def extensionSubnodes: Seq[Node] = formalArgs

  override def prettyPrint: PrettyPrintPrimitives#Cont = text(name) <> parens(showVars(formalArgs))

  override def withChildren(children: Seq[Any], pos: Option[(Position, Position)], forceRewrite: Boolean): this.type = {
    if (!forceRewrite && this.children == children && pos.isEmpty)
      this
    else {
      assert(children.length == 2, s"AdtConstructor : expected length 2 but got ${children.length}")
      val first = children.head.asInstanceOf[String]
      val second = children.tail.head.asInstanceOf[Seq[AnyLocalVarDecl]]
      AdtConstructor(first, second)(this.pos, this.info, this.typ, this.adtName, this.errT).asInstanceOf[this.type]
    }

  }
}

object AdtConstructor {
  def apply(adt: Adt, name: String, formalArgs: Seq[AnyLocalVarDecl])(pos: Position = NoPosition, info: Info = NoInfo, errT: ErrorTrafo = NoTrafos): AdtConstructor = {
    AdtConstructor(name, formalArgs)(pos, info, AdtType(adt, Map.empty), adt.name, errT)
  }
}

/**
  * This class represents an user-defined ADT type. See also the companion object below, which allows passing a
  * Adt - this should be used in general for creation (so that typeParameters is guaranteed to
  * be set correctly)
  *
  * @param adtName The name of the underlying adt.
  * @param partialTypVarsMap Maps type parameters to (possibly concrete) types. May not map all
  *                          type parameters, may even be empty.
  * @param typeParameters The type variables of the ADT type.
  */
case class AdtType(adtName: String, partialTypVarsMap: Map[TypeVar, Type])
                  (val typeParameters: Seq[TypeVar]) extends ExtensionType {

  /**
    * Map each type parameter `A` to `partialTypVarsMap(A)`, if defined, otherwise to `A` itself.
    * `typVarsMap` thus contains a mapping for each type parameter.
    */
  val typVarsMap: Map[TypeVar, Type] =
    typeParameters.map(tp => tp -> partialTypVarsMap.getOrElse(tp, tp)).to(implicitly)

  /**
    * Takes a mapping of type variables to types and substitutes all
    * occurrences of those type variables with the corresponding type.
    */
  override def substitute(typVarsMap: Map[TypeVar, Type]): Type = ???

  /** Is this a concrete type (i.e. no uninstantiated type variables)? */
  override def isConcrete: Boolean = typVarsMap.values.forall(_.isConcrete)

  override def prettyPrint: PrettyPrintPrimitives#Cont = {
    val typArgs = typeParameters map (t => show(typVarsMap.getOrElse(t, t)))
    text(adtName) <> (if (typArgs.isEmpty) nil else brackets(ssep(typArgs, char (',') <> space)))
  }

  override def withChildren(children: Seq[Any], pos: Option[(Position, Position)], forceRewrite: Boolean): this.type = {
    if (!forceRewrite && this.children == children && pos.isEmpty)
      this
    else {
      assert(children.length == 2, s"AdtType : expected length 2 but got ${children.length}")
      val first = children.head.asInstanceOf[String]
      val second = children.tail.head.asInstanceOf[Map[TypeVar, Type]]
      AdtType(first, second)(this.typeParameters).asInstanceOf[this.type]
    }
  }

}

object AdtType{
  def apply(adt: Adt, typVarsMap: Map[TypeVar, Type]): AdtType =
    AdtType(adt.name, typVarsMap)(adt.typVars)
}


