package org.highj.data;

import org.junit.Test;

import org.derive4j.hkt.__;
import org.highj.data.structural.Dual;
import org.highj.function.F1;
import org.highj.typeclass2.arrow.Category;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.highj.Hkt.asDual;
import static org.highj.Hkt.asF1;

/* import static junit.framework.Assert.assertEquals; */

public class DualTest
{

  @Test
  public void categoryIdentityTest() throws Exception
  {
    Category<__<Dual.µ, F1.µ>> category = Dual.category(F1.arrow);
    Dual<F1.µ, Integer, Integer> idDual = asDual(category.<Integer>identity());
    F1<Integer, Integer> id = asF1(idDual.get());

    assertThat((Integer.valueOf(42))).isEqualTo(id.apply(42));
  }

  @Test
  public void categoryDotTest() throws Exception
  {
    Category<__<Dual.µ, F1.µ>> category = Dual.category(F1.arrow);

    Dual<F1.µ, Integer, Integer> squareDual = new Dual<>((F1<Integer, Integer>) x -> x * x);
    Dual<F1.µ, Integer, Integer> negateDual = new Dual<>((F1<Integer, Integer>) x -> -x);
    Dual<F1.µ, Integer, Integer> squareNegateDual = asDual(category.dot(squareDual, negateDual));

    F1<Integer, Integer> squareNegate = asF1(squareNegateDual.get());

    // executes square first, and then negate
    // in contrast to category.apply(sqr, negate), which negates first, and squares then
    assertThat(Integer.valueOf(-16)).isEqualTo( squareNegate.apply(4));
  }
}
