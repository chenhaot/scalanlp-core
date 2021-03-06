package scalanlp.optimize

/*
 Copyright 2010 David Hall, Daniel Ramage

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

import org.scalatest._
import org.scalatest.junit._;
import org.scalatest.prop._;
import org.scalacheck._;
import org.junit.runner.RunWith

import scalala.library.Library.norm;
import scalala.tensor.mutable.Counter
import scalala.tensor.dense.{DenseVectorCol, DenseVector}
import scalala.tensor.Tensor
import scalala.operators.OpEq
import scalala.tensor.domain.IndexDomain
import scalanlp.util.logging.ConsoleLogging


@RunWith(classOf[JUnitRunner])
class OWLQNTest extends OptimizeTestBase {
  test("super simple") {
    val lbfgs = new OWLQN[Int,DenseVector[Double]](100,4) with ConsoleLogging

    def optimizeThis(init: DenseVector[Double]) = {
      val f = new DiffFunction[DenseVector[Double]] {
        def calculate(x: DenseVector[Double]) = {
          (norm((x -3) :^ 2,1),(x * 2) - 6);
        }
      }

      val result = lbfgs.minimize(f,init)
      result
    }

    val result = optimizeThis(DenseVector(-1.1053,0.,0.0));
    assert((result(0) - 2.5) < 1E-4, result)
  }


  test("optimize a simple multivariate gaussian") {
    val lbfgs = new OWLQN[Int,DenseVector[Double]](100,4,1.0)

    def optimizeThis(init: DenseVector[Double]) = {
      val f = new DiffFunction[DenseVector[Double]] {
        def calculate(x: DenseVector[Double]) = {
          (norm((x -3) :^ 2,1),(x * 2) - 6);
        }
      }

      val result = lbfgs.minimize(f,init)
      val closeish = norm(result - 2.5,2) < 1E-4
      if(closeish) {
        true
      } else {
        throw new Exception(result.toString + " is not close enough to 2.5");
      }
    }

    check(Prop.forAll(optimizeThis _));

  }

  test("optimize a simple multivariate gaussian with counters") {
    val lbfgsString = new OWLQN[String,Counter[String,Double]](100,4, 1.0);

    def optimizeThis(init: Counter[String,Double]) = {
      val f = new DiffFunction[Counter[String,Double]] {
        def calculate(x: Counter[String,Double]) = {
          (norm((x -3) :^ 2,1), (x * 2) - 6);
        }
      }

      val result = lbfgsString.minimize(f,init);
      val ok = norm(result - 2.5,2) < 1E-4
      if(ok) {
        true
      } else {
        throw new Exception(result.toString + " is not close enough to 2.5" + norm(result - 2.5, 2));
      }
    }

    check(Prop.forAll(optimizeThis _ ));

  }
}
