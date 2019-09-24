package Lab3.Shapes.AlgebraicDataTypes

sealed trait Calculator
case class Success(result: Int) extends Calculator {
  override def equals(that: Any): Boolean = false
}
case class Failure(result: String) extends Calculator{
  override def equals(that: Any): Boolean = false
}


