package assignment.akkactor

import java.io.{BufferedReader, FileReader}
import scala.collection.mutable
import scala.util.Try

object SensorManager {
  def getSensorDataMap(filePath: String): Map[String, Stats] = {
    println(s"start getSensorDataMap for file ${filePath}")

    val sensorDataMap = mutable.Map.empty[String, Stats]

    val fileReader = new FileReader(filePath)
    val bufferedReader = new BufferedReader(fileReader)

    // Read and discard the header row
    val headerRow = bufferedReader.readLine()

    var line: String = bufferedReader.readLine()

    while (line != null) {
      val parts = line.split(",")
      val sensorData = SensorData(parts(0), Try(parts(1).toInt).toOption)

      val currentStats =
        sensorDataMap.getOrElse(sensorData.sensorId, Stats(0, 0.0, Int.MaxValue, Int.MinValue, 0))

      val newStats = Stats(
        if (sensorData.humidity.isEmpty) currentStats.failedCount + 1 else currentStats.failedCount,
        currentStats.sum + sensorData.humidity.getOrElse(0),
        Math.min(currentStats.min, sensorData.humidity.getOrElse(Int.MaxValue)),
        Math.max(currentStats.max, sensorData.humidity.getOrElse(Int.MinValue)),
        currentStats.count + 1
      )
      sensorDataMap(sensorData.sensorId) = newStats

      // Read the next line
      line = bufferedReader.readLine()
    }

    // Close the file reader
    bufferedReader.close()

    collection.immutable.Map(sensorDataMap.toSeq: _*)
  }
}
