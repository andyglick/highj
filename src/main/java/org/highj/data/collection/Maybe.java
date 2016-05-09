package org.highj.data.collection;

import org.derive4j.hkt.__;
import org.highj.data.collection.maybe.*;
import org.highj.data.functions.F3;
import org.highj.data.functions.Functions;
import org.highj.typeclass0.compare.Eq;
import org.highj.typeclass0.group.Monoid;
import org.highj.typeclass1.comonad.Extend;
import org.highj.typeclass1.foldable.Traversable;
import org.highj.typeclass1.monad.MonadPlus;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Stack;
import java.util.function.*;

/**
 * A data type which may or may not hold a value.
 *
 * A.k.a. "Option", "Optional" or "Box".
 */
public abstract class Maybe<A> implements __<Maybe.µ, A>, Iterable<A> {
    private static final String SHOW_NOTHING = "Nothing";
    private static final String SHOW_JUST = "Just(%s)";

    public static final class µ {
    }

    private Maybe() {
    }

    @SuppressWarnings("rawtypes")
    private final static Maybe<?> NOTHING = new Maybe<Object>() {

        @Override
        public <B> B cata(B defaultValue, Function<Object, B> fn) {
            return defaultValue;
        }

        @Override
        public <B> B lazyCata(Supplier<B> defaultThunk, Function<Object, B> fn) {
            return defaultThunk.get();
        }
    };

    /**
     * Creates a {@link Maybe} holding no value (a.k.a. "Nothing").
     *
     * This method returns always the same NOTHING instance. As no actual A values are
     * involved, the cast is safe.
     *
     * @param <A> the element type
     * @return the empty {@link Maybe} instance
     */
    @SuppressWarnings("unchecked")
    public static <A> Maybe<A> newNothing() {
        return (Maybe) NOTHING;
    }

    /**
     * Creates a {@link Maybe} holding a value (a.k.a. "Just").
     *
     * The value needs to be non-null.
     *
     * @param value the value to be stored in the Maybe
     * @param <A>  the element type
     * @return a Maybe holding the value
     * @throws NullPointerException
     */
    public static <A> Maybe<A> newJust(final A value) throws NullPointerException {
        Objects.requireNonNull("newJust() can't take null argument");
        return new Maybe<A>() {
            public <B> B lazyCata(Supplier<B> defaultThunk, Function<A, B> fn) {
                return fn.apply(value);
            }

            @Override
            public <B> B cata(B defaultValue, Function<A, B> fn) {
                return fn.apply(value);
            }
        };
    }

    /**
     * Creates a {@link Maybe} holding a value (a.k.a. "Just") in a lazy fashion.
     *
     * @param thunk the {@link Supplier} generating the value to be stored in the Maybe
     * @param <A>  the element type
     * @return a {@link Maybe} holding the value
     */
    public static <A> Maybe<A> lazyJust(final Supplier<A> thunk) {
        return new Maybe<A>() {

            public <B> B lazyCata(Supplier<B> defaultThunk, Function<A, B> fn) {
                return fn.apply(thunk.get());
            }

            @Override
            public <B> B cata(B defaultValue, Function<A, B> fn) {
                return fn.apply(thunk.get());
            }
        };
    }

    /**
     * Creates a {@link Maybe} which contains a given value ("Just") when the condition is true,
     * or an empty on ("Nothing") otherwise.
     *
     * @param condition the condition
     * @param thunk the {@link Supplier} generating the value to be wrapped
     * @param <A>  the element type
     * @return a {@link Maybe}
     */
    public static <A> Maybe<A> justWhenTrue(boolean condition, Supplier<A> thunk) {
        return condition ? newJust(thunk.get()) : newNothing();
    }

    /**
     * The catamorphism of {@link Maybe}, which collapses it to a certain type.
     *
     * @param defaultValue the value to be returned when the {@link Maybe} is empty
     * @param fn  the function to be applied when the {@link Maybe} is not empty
     * @param <B> the result type
     * @return the result generated by the function or the default value
     */
    public abstract <B> B cata(B defaultValue, Function<A, B> fn);

    /**
     * A lazy version of {@link Maybe#cata}.
     *
     * @param defaultThunk the {@link Supplier} generating the default value when the {@link Maybe} is empty
     * @param fn the function to be applied when the {@link Maybe} is not empty
     * @param <B> the result type
     * @return the result generated by the function or the default value
     */
    public abstract <B> B lazyCata(Supplier<B> defaultThunk, Function<A, B> fn);

    /**
     * Changes the element type of a {@link Maybe} to a super type.
     *
     * This is safe, as {@link Maybe} is a read-only class.
     *
     * @param maybe a {@link Maybe} instance
     * @param <Super_A> the expected super type
     * @param <A> the current element type
     * @return a reference to the given {@link Maybe} with a more general type
     */
    @SuppressWarnings("unchecked")
    public static <Super_A, A extends Super_A> Maybe<Super_A> contravariant(Maybe<A> maybe) {
        return (Maybe) maybe;
    }

    /**
     * Checks whether the {@link Maybe} is empty ("Nothing") or not ("Just").
     *
     * @return true if the {@link Maybe} is empty, false otherwise
     */
    public boolean isNothing() {
        return this == NOTHING;
    }

    /**
     * Checks whether the {@link Maybe} is non-empty ("Just") or not ("Nothing").
     *
     * @return true if the {@link Maybe} is non-empty, false otherwise
     */
    public boolean isJust() {
        return this != NOTHING;
    }

    /**
     * Returns the value stored in the {@link Maybe}, or a default value, if it is empty.
     *
     * @param defaultValue the value used if the {@link Maybe} is empty
     * @return the wrapped value, or a default
     */
    public A getOrElse(A defaultValue) {
        return cata(defaultValue, x -> x);
    }

    /**
     * Returns the value stored in the {@link Maybe}, or a lazy default value, if it is empty.
     *
     * @param defaultThunk the {@link Supplier} to generate the value used if the {@link Maybe} is empty
     * @return the wrapped value, or a default
     */
    public A getOrElse(Supplier<A> defaultThunk) {
        return lazyCata(defaultThunk, x -> x);
    }

    /**
     * Returns the value stored in the {@link Maybe}, or throws an Exception if it is empty.
     *
     * Note that the specified exception class needs to have a default constructor.
     *
     * @param exClass the Class of the Exception to be thrown if the {@link Maybe} is empty
     * @return the wrapped value
     * @throws RuntimeException
     */
    public A getOrException(Class<? extends RuntimeException> exClass) {
        return getOrElse(Functions.<A>error(exClass));
    }

    /**
     * Returns the value stored in the {@link Maybe}, or throws an Exception if it is empty.
     *
     * Note that the specified exception class needs to have a constructor taking a String argument.
     *
     * @param exClass the Class of the Exception to be thrown if the {@link Maybe} is empty
     * @param message the error message
     * @return the wrapped value
     * @throws RuntimeException
     */
    public A getOrException(Class<? extends RuntimeException> exClass, String message) {
        return getOrElse(Functions.<A>error(exClass, message));
    }

    /**
     * Returns the value stored in the {@link Maybe}, or throws a {@link RuntimeException} if it is empty.
     *
     * @param message the message of the {@link RuntimeException}
     * @return the wrapped value
     * @throws RuntimeException
     */
    public A getOrException(String message) {
        return getOrElse(Functions.<A>error(message));
    }

    /**
     * Returns the value stored in the {@link Maybe}, or throws a {@link NoSuchElementException} if it is empty.
     *
     * @return the wrapped value
     * @throws NoSuchElementException
     */
    public A get() throws NoSuchElementException {
        return getOrException(NoSuchElementException.class);
    }

    /**
     * Replaces the the current {@link Maybe} when it is empty.
     *
     * @param that the {@link Maybe} to replace the current one.
     * @return the current {@link Maybe} if it is non-empty, else its replacement
     */
    public Maybe<A> orElse(Maybe<A> that) {
        return this.isJust() ? this : that;
    }

    @Override
    public void forEach(Consumer<? super A> consumer) {
        if (isJust()) consumer.accept(get());
    }

    /**
     * Checks the current value against a condition, and returns an empty {@link Maybe} when it fails.
     *
     * @param predicate the condition to be applied
     * @return the current {@link Maybe} if it passes, else an empty one
     */
    public Maybe<A> filter(Predicate<? super A> predicate) {
        return isJust() && predicate.test(get()) ? this : newNothing();
    }

    /**
     * Converts the {@link Maybe} to a {@link List}
     *
     * @return a list containg zero or one element.
     */
    public List<A> asList() {
        return cata(List.<A>nil(), List::of);
    }

    /**
     * Calculates a new {@link Maybe} from the actual value, or returns an empty one if there is none.
     *
     * Corresponds to {@link org.highj.typeclass1.monad.Monad#bind}.
     *
     * @param fn  the function to be applied
     * @param <B>  the element type of the result
     * @return the calculated {@link Maybe}, or an empty one
     */
    public <B> Maybe<B> bind(Function<A, Maybe<B>> fn) {
        return cata(Maybe.<B>newNothing(), fn);
    }

    /**
     * Calculates a new value from the actual value, or returns an empty {@link Maybe} if there is none.
     *
     * Corresponds to {@link org.highj.typeclass1.functor.Functor#map}.
     *
     * @param fn the function to be applied
     * @param <B> the element type of the result
     * @return the calculated {@link Maybe}, or an empty one
     */
    public <B> Maybe<B> map(Function<? super A, ? extends B> fn) {
        return bind(a -> Maybe.<B>newJust(fn.apply(a)));
    }

    /**
     * Lifts a unary function to the {@link Maybe} context.
     *
     * Corresponds to {@link org.highj.typeclass1.functor.Functor#lift}.
     *
     * @param fn  the function
     * @param <A> the argument type
     * @param <B>  the result type
     * @return  a {@link Function} operating on {@link Maybe} values.
     */
    public static <A,B> Function<Maybe<A>, Maybe<B>> lift(Function<A,B> fn) {
        return maybeA -> maybeA.map(fn);
    }

    /**
     * Lifts a binary function to the {@link Maybe} context.
     *
     * Corresponds to {@link org.highj.typeclass1.monad.Apply#lift2}.
     *
     * @param fn  the function
     * @param <A> the first argument type
     * @param <B> the second argument type
     * @param <C> the result type
     * @return a {@link BiFunction} operating on {@link Maybe} values.
     */
    public static <A,B,C> BiFunction<Maybe<A>, Maybe<B>, Maybe<C>> lift2(BiFunction<A,B,C> fn) {
        return (maybeA, maybeB) -> maybeA.bind(
                a -> maybeB.map(
                        b -> fn.apply(a,b)));
    }

    /**
     * Lifts a ternary function to the {@link Maybe} context.
     *
     * Corresponds to {@link org.highj.typeclass1.monad.Apply#lift3}.
     *
     * @param fn the function
     * @param <A> the first argument type
     * @param <B> the second argument type
     * @param <C> the third argument type
     * @param <D>  the result type
     * @return a {@link F3} operating on {@link Maybe} values.
     */
    public static <A,B,C,D> F3<Maybe<A>,Maybe<B>,Maybe<C>,Maybe<D>> lift3(F3<A,B,C,D> fn) {
        return (maybeA, maybeB, maybeC) -> maybeA.bind(
                a -> maybeB.bind(
                        b -> maybeC.map(
                                c -> fn.apply(a,b,c))));
    }

    /**
     * Converts the higher kinded representation of a {@link Maybe} back to the standard one.
     *
     * @param value the {@link Maybe} in higher kinded representation
     * @param <A> the element type
     * @return the {@link Maybe} converted back to standard form
     */
    @SuppressWarnings("unchecked")
    public static <A> Maybe<A> narrow(__<µ, A> value) {
        return (Maybe) value;
    }

    @Override
    public Iterator<A> iterator() {
        return asList().iterator();
    }

    @Override
    public String toString() {
        return cata(SHOW_NOTHING, a -> String.format(SHOW_JUST, a));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Maybe) {
            Maybe<?> that = (Maybe) obj;
            return cata(that.isNothing(), x -> that.<Boolean>cata(false, x::equals));
        } else return false;
    }

    /**
     * Generates an {@link Eq} instance for {@link Maybe}.
     *
     * @param eqA an {@link Eq} instance for the element type
     * @param <A> the element type
     * @return  the {@link Eq} instance
     */
    public static <A> Eq<Maybe<A>> eq(final Eq<? super A> eqA) {
        return (one, two) -> one.cata(two.isNothing(),
                x -> two.<Boolean>cata(false, y -> eqA.eq(x, y)));
    }

    @Override
    public int hashCode() {
        return cata(0, Object::hashCode);
    }

    /**
     * Collects all values from non-empty {@link Maybe} elements in a {@link List}.
     *
     * @param iterable a {@link Iterable} of {@link Maybe} elements
     * @param <A> the element type
     * @return a {@link List} of all values contained in non-empty {@link Maybe}s.
     */
    public static <A> List<A> justs(Iterable<Maybe<A>> iterable) {
        Stack<A> result = new Stack<>();
        for (Maybe<A> maybe : iterable) {
            maybe.forEach(result::push);
        }
        return List.buildFromStack(result);
    }

    /**
     * Returns the {@link MaybeMonad}.
     *
     * Note that it implements some subinterfaces of {@link org.highj.typeclass1.monad.Monad} as well
     */
    public static final MaybeMonad monad = new MaybeMonad(){};

    /**
     * Returns the {@link MaybeMonadPlus} which chooses the first non-empty {@link Maybe}.
     */
    public static final MaybeMonadPlus firstBiasedMonadPlus = () -> MonadPlus.Bias.FIRST;

    /**
     * Returns the {@link MaybeMonadPlus} which chooses the last non-empty {@link Maybe}.
     */
    public static final MaybeMonadPlus lastBiasedMonadPlus = () -> MonadPlus.Bias.LAST;


    /**
     * Returns the {@link Traversable} instance of {@link Maybe}.
     */
    public static final Traversable<µ> traversable = new MaybeTraversable(){};

    /**
     * Returns the {@link Extend} instance of {@link Maybe}.
     */
    public static final Extend<µ> extend = new MaybeExtend(){};

    /**
     * Returns the {@link Monoid} which chooses the first non-empty {@link Maybe}.
     * @param <A> the element type
     * @return the {@link Monoid} instance
     */
    public static <A> Monoid<Maybe<A>> firstMonoid() {
        return Monoid.create(Maybe.newNothing(), Maybe::orElse);
    }

    /**
     * Returns the {@link Monoid} which chooses the last non-empty {@link Maybe}.
     * @param <A> the element type
     * @return the {@link Monoid} instance
     */
    public static <A> Monoid<Maybe<A>> lastMonoid() {
        return Monoid.create(Maybe.newNothing(), (x, y) -> y.orElse(x));
    }

    /**
     * Returns the {@link Monoid} for {@link Maybe} given a {@link Monoid} for the element type.
     * @param <A> the element type
     * @return the {@link Monoid} instance
     */
    public static <A> Monoid<Maybe<A>> monoid(final BinaryOperator<A> semigroup) {
        BinaryOperator<Maybe<A>> op = (mx,my) ->
                mx.map(x -> my.map(y -> semigroup.apply(x,y)).getOrElse(x)).orElse(my);
        return Monoid.create(Maybe.newNothing(), op);
    }

}
