package org.highj.typeclass0.group;

import org.junit.Test;

import org.highj.data.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SemigroupTest
{
  @Test
  public void testFirst()
  {
    Integer result = Semigroup.<Integer>first().fold(1, List.of(2, 3, 4, 5));
    assertThat(Integer.valueOf(1)).isEqualTo(result);
  }

  @Test
  public void testLast()
  {
    Integer result = Semigroup.<Integer>last().fold(1, List.of(2, 3, 4, 5));
    assertThat(Integer.valueOf(5)).isEqualTo(result);
  }

  @Test
  public void testDual()
  {
    Integer result = Semigroup.dual(Semigroup.<Integer>first()).fold(1, List.of(2, 3, 4, 5));
    assertThat(Integer.valueOf(5)).isEqualTo(result);
  }

  @Test
  public void testMin()
  {
    Integer result = Semigroup.<Integer>min().fold(27, List.of(25, 11, 64, 57));
    assertThat(Integer.valueOf(11)).isEqualTo(result);
  }

  @Test
  public void testMax()
  {
    Integer result = Semigroup.<Integer>max().fold(27, List.of(25, 11, 64, 57));
    assertThat(Integer.valueOf(64)).isEqualTo(result);
  }
}
