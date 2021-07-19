package fp

trait Functor[F[_]] {

  def map[A, B](fa: F[A])(f: A => B): F[B]
}

trait Applicative[F[_]] extends Functor[F] { self =>

  def unit[A](a: => A): F[A]

  def map2[A, B, C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C]

  def apply[A, B](mf: F[A => B])(ma: F[A]): F[B] =
    map2(mf, ma)((fab, a) => fab(a))

  def map[A, B](fa: F[A])(f: A => B): F[B] =
    map2(fa, unit(()))((a, _) => f(a))

  def traverse[A, B](as: List[A])(f: A => F[B]): F[List[B]] =
    as.foldRight(unit(List.empty[B]))((a, fbs) => map2(f(a), fbs)(_ +: _))

  /**
    * Applicatives compose.
    * We form a new Applicative that is the product of this and the other.
    */
  def product[G[_]](G: Applicative[G]): Applicative[λ[X => (F[X], G[X])]] =
    new Applicative[λ[X => (F[X], G[X])]] {

      def unit[A](a: => A): (F[A], G[A]) =
        (self.unit(a), G.unit(a))

      def map2[A, B, C](fga: (F[A], G[A]), fgb: (F[B], G[B]))(f: (A, B) => C): (F[C], G[C]) = {
        val fc = self.map2(fga._1, fgb._1)(f)
        val gc = G.map2(fga._2, fgb._2)(f)
        (fc, gc)
      }
    }

  def compose[G[_]](G: Applicative[G]): Applicative[λ[A => F[G[A]]]] =
    new Applicative[λ[A => F[G[A]]]] {

      def unit[A](a: => A): F[G[A]] =
        self.unit(G.unit(a))

      def map2[A, B, C](fga: F[G[A]], fgb: F[G[B]])(f: (A, B) => C): F[G[C]] =
        self.map2(fga, fgb)((ga, gb) => G.map2(ga, gb)(f))
    }

  def sequenceMap[K, V](ofa: Map[K, F[V]]): F[Map[K, V]] =
    ofa.foldLeft(unit(Map.empty[K, V])) {
      case (fm, (k, fv)) => map2(fm, fv)((m, v) => m.updated(k, v))
    }
}

object Applicative {

  implicit val opt = new Applicative[Option] {

    def unit[A](a: => A): Option[A] =
      Option(a)

    def map2[A, B, C](fa: Option[A], fb: Option[B])(f: (A, B) => C): Option[C] =
      fa match {
        case None    => None
        case Some(a) => fb.map(f(a, _))
      }
  }

  implicit val list = new Applicative[List] {

    def unit[A](a: => A): List[A] =
      List(a)

    def map2[A, B, C](fa: List[A], fb: List[B])(f: (A, B) => C): List[C] =
      fa.foldLeft(List.empty[C])((ls, a) => ls ::: fb.map(f(a, _)))
  }

  implicit val tree = new Applicative[Tree] {
    def unit[A](a: => A): Tree[A] = Tree.unit(a)

    def map2[A, B, C](fa: Tree[A], fb: Tree[B])(f: (A, B) => C): Tree[C] = {
      val Tree(a, fas) = fa
      val Tree(b, fbs) = fb
      Tree(f(a, b), list.map2(fas, fbs)(map2(_, _)(f)))
    }
  }
}

// A tree with possibly many branches at each node
case class Tree[+A](head: A, tail: List[Tree[A]])

object Tree {
  def unit[A](a: => A): Tree[A] = Tree(a, List.empty)
}

trait Traverse[F[_]] extends Functor[F] with Foldable[F] { self =>

  def traverse[G[_]: Applicative, A, B](fa: F[A])(f: A => G[B]): G[F[B]] =
    sequence(map(fa)(f))
  def sequence[G[_]: Applicative, A](fga: F[G[A]]): G[F[A]] =
    traverse(fga)(identity)

  type Id[A] = A

  val idMonad = new Monad[Id] {
    def unit[A](a: => A) = a
    override def flatMap[A, B](a: A)(f: A => B): B = f(a)
  }

  def map[A, B](fa: F[A])(f: A => B): F[B] =
    traverse[Id, A, B](fa)(f)(idMonad)

  def compose[G[_]](implicit G: Traverse[G]): Traverse[λ[X => F[G[X]]]] =
    new Traverse[λ[X => F[G[X]]]] {
      override def traverse[M[_]: Applicative, A, B](fga: F[G[A]])(f: A => M[B]): M[F[G[B]]] =
        self.traverse(fga)(G.traverse(_)(f))
    }

  def fuse[M[_], N[_], A, B](fa: F[A])(f: A => M[B], g: A => N[B])(
    implicit M: Applicative[M],
    N: Applicative[N]
  ): (M[F[B]], N[F[B]]) =
    traverse[λ[X => (M[X], N[X])], A, B](fa)(a => (f(a), g(a)))(M product N)
}

object Traverse {

  def apply[F[_]: Traverse]: Traverse[F] = implicitly[Traverse[F]]

  implicit val list = new Traverse[List] {
    override def traverse[G[_], A, B](fa: List[A])(f: A => G[B])(
      implicit G: Applicative[G]
    ): G[List[B]] =
      fa.foldRight(G.unit(List.empty[B]))((a, glb) => G.map2(f(a), glb)(_ :: _))
  }

  implicit val opt = new Traverse[Option] {
    override def traverse[G[_], A, B](
      fa: Option[A]
    )(f: A => G[B])(implicit G: Applicative[G]): G[Option[B]] =
      fa match {
        case None    => G.unit(None)
        case Some(a) => G.map(f(a))(Option(_))
      }
  }

  implicit val tree = new Traverse[Tree] {

    override def traverse[G[_], A, B](fa: Tree[A])(f: A => G[B])(implicit G: Applicative[G]): G[Tree[B]] =
      G.map2(f(fa.head), list.traverse(fa.tail)((traverse(_)(f))))(Tree(_, _))
  }

}

trait Monad[F[_]] extends Applicative[F] {
  def flatMap[A, B](ma: F[A])(f: A => F[B]): F[B] =
    join(map(ma)(f))

  override def apply[A, B](mf: F[A => B])(ma: F[A]): F[B] =
    flatMap(mf)(f => map(ma)(f))

  override def map[A, B](m: F[A])(f: A => B): F[B] =
    flatMap(m)(a => unit(f(a)))

  override def map2[A, B, C](ma: F[A], mb: F[B])(f: (A, B) => C): F[C] =
    flatMap(ma)(a => map(mb)(b => f(a, b)))

  def compose[A, B, C](f: A => F[B], g: B => F[C]): A => F[C] =
    a => flatMap(f(a))(g)

  def join[A](mma: F[F[A]]): F[A] = flatMap(mma)(ma => ma)
}

trait Monoid[A] {
  def op(a1: A, a2: A): A
  def zero: A
}

object Monoid {
  // There is a choice of implementation here as well.
  // Do we implement it as `f compose g` or `f andThen g`? We have to pick one.
  def endoMonoid[A]: Monoid[A => A] = new Monoid[A => A] {
    def op(f: A => A, g: A => A) = f.compose(g)
    val zero = (a: A) => a
  }
  // We can get the dual of any monoid just by flipping the `op`.
  def dual[A](m: Monoid[A]): Monoid[A] = new Monoid[A] {
    def op(x: A, y: A): A = m.op(y, x)
    val zero = m.zero
  }
}

trait Foldable[F[_]] {
  import Monoid._

  def foldRight[A, B](as: F[A])(z: B)(f: (A, B) => B): B =
    foldMap(as)(f.curried)(endoMonoid[B])(z)

  def foldLeft[A, B](as: F[A])(z: B)(f: (B, A) => B): B =
    foldMap(as)(a => (b: B) => f(b, a))(dual(endoMonoid[B]))(z)

  def foldMap[A, B](as: F[A])(f: A => B)(mb: Monoid[B]): B =
    foldRight(as)(mb.zero)((a, b) => mb.op(f(a), b))

  def concatenate[A](as: F[A])(m: Monoid[A]): A =
    foldLeft(as)(m.zero)(m.op)

  def toList[A](as: F[A]): List[A] =
    foldRight(as)(List[A]())(_ :: _)
}
