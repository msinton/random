package fp

trait Functor[F[_]] {

  def unit[A](a: => A): F[A]

  def map[A, B](fa: F[A])(f: A => B): F[B]
}

trait Applicative[F[_]] extends Functor[F] { self =>

  def unit[A](a: => A): F[A]

  def map2[A, B, C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C]

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

  val opt = new Applicative[Option] {

    def unit[A](a: => A): Option[A] =
      Option(a)

    def map2[A, B, C](fa: Option[A], fb: Option[B])(f: (A, B) => C): Option[C] =
      fa match {
        case None    => None
        case Some(a) => fb.map(f(a, _))
      }
  }

  val list = new Applicative[List] {

    def unit[A](a: => A): List[A] =
      List(a)

    def map2[A, B, C](fa: List[A], fb: List[B])(f: (A, B) => C): List[C] =
      (fa zip fb).map(f.tupled)
  }
}

// A tree with possibly many branches at each node
case class Tree[+A](head: A, tail: List[Tree[A]])

trait Traverse[F[_]] extends Functor[F] {

  def traverse[G[_]: Applicative, A, B](fa: F[A])(f: A => G[B]): G[F[B]] =
    sequence(map(fa)(f))
  def sequence[G[_]: Applicative, A](fga: F[G[A]]): G[F[A]] =
    traverse(fga)(identity)
}

object Traverse {

  val list = new Traverse[List] {
    def unit[A](a: => A): List[A] = List(a)

    def map[A, B](fa: List[A])(f: A => B): List[B] = fa.map(f)

    override def traverse[G[_], A, B](
      fa: List[A]
    )(f: A => G[B])(implicit G: Applicative[G]): G[List[B]] =
      fa.foldRight(G.unit(List.empty[B]))((a, glb) => G.map2(f(a), glb)(_ :: _))
  }

  val opt = new Traverse[Option] {
    def unit[A](a: => A): Option[A] = Option(a)

    def map[A, B](fa: Option[A])(f: A => B): Option[B] =
      fa.map(f)

    override def traverse[G[_], A, B](
      fa: Option[A]
    )(f: A => G[B])(implicit G: Applicative[G]): G[Option[B]] =
      fa match {                                                       
        case None    => G.unit(None)
        case Some(a) => G.map(f(a))(Option(_))
      }
  }

  val tree = new Traverse[Tree] {
    def unit[A](a: => A): Tree[A] = Tree(a, List())

    def map[A, B](fa: Tree[A])(f: A => B): Tree[B] =
      Tree(f(fa.head), fa.tail.map(map(_)(f)))

    override def traverse[G[_], A, B](
      fa: Tree[A]
    )(f: A => G[B])(implicit G: Applicative[G]): G[Tree[B]] =
      fa match {
        case Tree(a, xs) => G.map2(f(a), list.traverse(xs)(traverse(_)(f)))(Tree(_, _))
      }
  }

}
