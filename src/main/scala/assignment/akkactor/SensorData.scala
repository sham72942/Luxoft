package assignment.akkactor
case class SensorData(sensorId: String, humidity: Option[Int])
case class Stats(failedCount: Int, sum: Double, min: Int, max: Int, count: Int)

