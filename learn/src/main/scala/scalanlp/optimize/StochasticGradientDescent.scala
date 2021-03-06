package scalanlp.optimize;

import scalala._;
import generic.collection.{CanMapValues, CanCreateZerosLike}
import operators._
import bundles.MutableInnerProductSpace
import scalanlp.util._;
import logging.ConfiguredLogging
import scalanlp.stats.distributions._
import scalala.generic.math.{CanSqrt, CanNorm}
import scalala.library.Library.norm;
import scalala.tensor.mutable;

/*
 Copyright 2009 David Hall, Daniel Ramage
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at 
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License. 
*/

/** 
* Minimizes a function using stochastic gradient descent
*
* @author dlwh
*/
abstract class StochasticGradientDescent[T](val defaultStepSize: Double,
                                            val maxIter: Int)
                                            (implicit protected val vspace: MutableInnerProductSpace[Double,T],
                                             protected val canNorm: CanNorm[T])
  extends FirstOrderMinimizer[T,StochasticDiffFunction[T]](maxIter) with ConfiguredLogging {

  import vspace._;

  // Optional hooks with reasonable defaults

  /**
   * Projects the vector x onto whatever ball is needed. Can also incorporate regularization, or whatever.
   *
   * Default just takes a step
   */
  protected def takeStep(state: State, dir: T, stepSize: Double) = state.x + dir * stepSize
  protected def chooseDescentDirection(state: State) = state.grad * -1.0

  /**
   * Choose a step size scale for this iteration.
   *
   * Default is eta / math.pow(state.iter + 1,2.0 / 3.0)
   */
  def determineStepSize(state: State, f: StochasticDiffFunction[T], dir: T) = {
    defaultStepSize / math.pow(state.iter + 1, 2.0 / 3.0)
  }


}

object StochasticGradientDescent {
  def apply[T](initialStepSize: Double=4, maxIter: Int=100)(implicit vs: MutableInnerProductSpace[Double,T], canNorm: CanNorm[T]) :StochasticGradientDescent[T]  = {
      new SimpleSGD(initialStepSize,maxIter);
  }

  class SimpleSGD[T](eta: Double=4,
                      maxIter: Int=100)
                    (implicit vs: MutableInnerProductSpace[Double,T],
                     canNorm: CanNorm[T]) extends StochasticGradientDescent[T](eta,maxIter) {
      type History = Unit
      def initialHistory(f: StochasticDiffFunction[T],init: T)= ()
      def updateHistory(newX: T, newGrad: T, newValue: Double, oldState: State) = ()
  }

}
