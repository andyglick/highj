package org.highj.data.instance;

import org.junit.Test;

import org.highj.data.List;
import org.highj.data.Stream;
import org.highj.data.num.Integers;
import org.highj.data.tuple.T2;
import org.highj.function.Strings;
import org.highj.typeclass1.monad.Monad;

import java.util.Iterator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.highj.Hkt.asStream;
import static org.highj.data.Stream.cycle;
import static org.highj.data.Stream.interleave;
import static org.highj.data.Stream.newLazyStream;
import static org.highj.data.Stream.newStream;
import static org.highj.data.Stream.range;
import static org.highj.data.Stream.repeat;
import static org.highj.data.Stream.unfold;
import static org.highj.data.Stream.unzip;
import static org.highj.data.Stream.zip;
import static org.highj.data.Stream.zipWith;

public class StreamTest
{
  @Test
  public void testHead() throws Exception
  {
    Stream<String> stream = cycle("foo");
    assertThat("foo").isEqualTo(stream.head());
    stream = newStream("foo", cycle("bar", "baz"));
    assertThat("foo").isEqualTo(stream.head());
    stream = newLazyStream("foo", () -> cycle("bar", "baz"));
    assertThat("foo").isEqualTo(stream.head());
    stream = unfold(s -> s + "!", "foo");
    assertThat("foo").isEqualTo(stream.head());
  }

  @Test
  public void testTail() throws Exception
  {
    Stream<String> stream = cycle("foo");
    assertThat("foo").isEqualTo(stream.tail().head());
    stream = newStream("foo", cycle("bar", "baz"));
    assertThat("bar").isEqualTo(stream.tail().head());
    stream = newLazyStream("foo", () -> cycle("bar", "baz"));
    assertThat("bar").isEqualTo(stream.tail().head());
    stream = unfold(s -> s + "!", "foo");
    assertThat("foo!").isEqualTo(stream.tail().head());
  }

  @Test
  public void testStreamRepeat() throws Exception
  {
    Stream<String> stream = cycle("foo");
    for (int i = 1; i < 10; i++)
    {
      assertThat("foo").isEqualTo(stream.head());
      stream = stream.tail();
    }
  }

  @Test
  public void testStreamHeadFn() throws Exception
  {
    Stream<String> stream = unfold(s -> s + "!", "foo");
    assertThat("foo").isEqualTo(stream.head());
    stream = stream.tail();
    assertThat("foo!").isEqualTo(stream.head());
    stream = stream.tail();
    assertThat("foo!!").isEqualTo(stream.head());
  }

  @Test
  public void testStreamHeadStream() throws Exception
  {
    Stream<String> stream = newStream("foo", cycle("bar"));
    assertThat("foo").isEqualTo(stream.head());
    stream = stream.tail();
    assertThat("bar").isEqualTo(stream.head());
    stream = stream.tail();
    assertThat("bar").isEqualTo(stream.head());
  }

  @Test
  public void testStreamHeadThunk() throws Exception
  {
    Stream<String> stream = newLazyStream("foo", () -> cycle("bar"));
    assertThat("foo").isEqualTo(stream.head());
    stream = stream.tail();
    assertThat("bar").isEqualTo(stream.head());
    stream = stream.tail();
    assertThat("bar").isEqualTo(stream.head());
  }

  @Test
  public void testStreamIterator() throws Exception
  {
    Iterator<Integer> myIterator = new Iterator<Integer>()
    {
      private int i = 0;

      @Override
      public boolean hasNext()
      {
        return true;
      }

      @Override
      public Integer next()
      {
        return i++;
      }
    };

    Stream<Integer> stream = newLazyStream(myIterator);
    assertThat("Stream(0,1,2,3,4,5,6,7,8,9...)").isEqualTo(stream.toString());
  }


  @Test
  public void testFilter() throws Exception
  {
    Stream<Integer> stream = range(1).filter(Integers.even::test);
    assertThat(Integer.valueOf(2)).isEqualTo(stream.head());
    stream = stream.tail();
    assertThat(Integer.valueOf(4)).isEqualTo(stream.head());
    stream = stream.tail();
    assertThat(Integer.valueOf(6)).isEqualTo(stream.head());
  }


  @Test
  public void testToString() throws Exception
  {
    Stream<Integer> stream = range(1).filter(Integers.even::test);
    assertThat("Stream(2,4,6,8,10,12,14,16,18,20...)").isEqualTo(stream.toString());
  }

  @Test
  public void testTake() throws Exception
  {
    Stream<Integer> stream = range(1).filter(Integers.odd::test);
    assertThat("List(1,3,5,7)").isEqualTo(stream.take(4).toString());
    assertThat("List()").isEqualTo(stream.take(0).toString());
    assertThat("List()").isEqualTo(stream.take(-4).toString());
  }

  @Test
  public void testTakeWhile() throws Exception
  {
    Stream<Integer> stream = range(10, -3);
    assertThat("List(10,7,4,1)").isEqualTo(stream.takeWhile(Integers.positive).toString());
    assertThat("List()").isEqualTo(stream.takeWhile(Integers.negative).toString());
  }

  @Test
  public void testDrop() throws Exception
  {
    Stream<Integer> stream = range(1);
    assertThat(Integer.valueOf(5)).isEqualTo(stream.drop(4).head());
    assertThat(Integer.valueOf(1)).isEqualTo(stream.drop(0).head());
    assertThat(Integer.valueOf(1)).isEqualTo(stream.drop(-4).head());
  }

  @Test
  public void testDropWhile() throws Exception
  {
    Stream<Integer> stream = range(10, -3);
    assertThat(Integer.valueOf(-2)).isEqualTo(stream.dropWhile(Integers.positive).head());
    assertThat(Integer.valueOf(10)).isEqualTo(stream.dropWhile(Integers.negative).head());
  }

  @Test
  public void testRangeFrom() throws Exception
  {
    Stream<Integer> stream = range(10);
    assertThat("Stream(10,11,12,13...)").isEqualTo(stream.toString(4));
  }

  @Test
  public void testRangeFromTo() throws Exception
  {
    Stream<Integer> stream = range(10, 3);
    assertThat("Stream(10,13,16,19...)").isEqualTo(stream.toString(4));
    stream = range(10, 0);
    assertThat("Stream(10,10,10,10...)").isEqualTo(stream.toString(4));
    stream = range(10, -3);
    assertThat("Stream(10,7,4,1...)").isEqualTo(stream.toString(4));
  }

  @Test
  public void testCycle() throws Exception
  {
    Stream<String> stream = cycle("foo", "bar", "baz");
    assertThat("Stream(foo,bar,baz,foo,bar,baz,foo,bar,baz,foo...)").isEqualTo(stream.toString());
  }

  @Test
  public void testMap() throws Exception
  {
    Stream<Integer> stream = cycle("one", "two", "three").map(String::length);
    assertThat("Stream(3,3,5,3,3,5,3,3,5,3...)").isEqualTo(stream.toString());
  }


  @Test
  public void testZip() throws Exception
  {
    Stream<T2<Integer, String>> stream = zip(range(1), cycle("foo", "bar", "baz"));
    assertThat("Stream((1,foo),(2,bar),(3,baz),(4,foo)...)").isEqualTo(stream.toString(4));
  }

  /*  ToDo: fix this there seems to be an issue aftefr the code was forked */
  @Test
  public void testZipWith() throws Exception
  {
    Stream<String> stream = zipWith(s -> n -> Strings.repeat.apply(s, n), cycle("foo", "bar", "baz"), range(2));
    assertThat("Stream(foofoo,barbarbar,bazbazbazbaz,foofoofoofoofoo...)").isEqualTo(stream.toString(4));
  }
  /* */

  @Test
  public void testUnzip() throws Exception
  {
    Stream<T2<Integer, String>> stream = zip(range(1), cycle("foo", "bar", "baz"));
    T2<Stream<Integer>, Stream<String>> t2 = unzip(stream);
    assertThat("Stream(1,2,3,4,5,6,7,8,9,10...)").isEqualTo(t2._1().toString());
    assertThat("Stream(foo,bar,baz,foo,bar,baz,foo,bar,baz,foo...)").isEqualTo(t2._2().toString());
  }

  @Test
  public void testIterator() throws Exception
  {
    int n = 3;
    for (int i : range(3))
    {
      assertThat(n).isEqualTo(i);

      n++;

      if (n > 10)
      {
        return;
      }
    }
  }

  @Test
  public void testMonad() throws Exception
  {
    Monad<Stream.µ> monad = Stream.monad;

    Stream<String> foobars = newStream("foo", repeat("bars"));
    Stream<Integer> foobarsLength = asStream(monad.map(String::length, foobars));
    assertThat("Stream(3,4,4,4,4,4,4,4,4,4...)").isEqualTo(foobarsLength.toString());

    Stream<String> foos = asStream(monad.pure("foo"));
    assertThat("Stream(foo,foo,foo,foo,foo,foo,foo,foo,foo,foo...)")
      .isEqualTo(foos.toString());

    Stream<Integer> absSqr = asStream(monad.ap(cycle(Integers.negate, Integers.sqr), range(1)));
    assertThat("Stream(-1,4,-3,16,-5,36,-7,64,-9,100...)")
      .isEqualTo(absSqr.toString());

    Stream<Integer> streamOfStream = asStream(monad.bind(range(1),
      integer -> range(1, integer)));

    assertThat("Stream(1,3,7,13,21,31,43,57,73,91...)").isEqualTo(streamOfStream.toString());
  }

  @Test
  public void testInits() throws Exception
  {
    Stream<List<Integer>> stream = range(1).inits();
    assertThat("Stream(List(),List(1),List(1,2),List(1,2,3)...)")
      .isEqualTo(stream.toString(4));
  }

  @Test
  public void testTails() throws Exception
  {
    Stream<Stream<Integer>> stream = range(1).tails();
    assertThat("Stream(List(1,2,3),List(2,3,4),List(3,4,5),List(4,5,6)...)")
      .isEqualTo(stream.map(x -> x.take(3)).toString(4));
  }

  @Test
  public void testIntersperse() throws Exception
  {
    Stream<Integer> stream = range(3).intersperse(0);
    assertThat("Stream(3,0,4,0,5,0,6,0,7,0...)")
      .isEqualTo(stream.toString());
  }

  @Test
  public void testInterleave() throws Exception
  {
    Stream<Integer> stream = interleave(range(3), range(1));
    assertThat("Stream(3,1,4,2,5,3,6,4,7,5...)")
      .isEqualTo(stream.toString());
  }
}
