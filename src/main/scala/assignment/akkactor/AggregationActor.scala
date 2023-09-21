package assignment.akkactor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}

import scala.collection.mutable
class AggregationActor extends LuxoftActor {
  override def behavior(): Behavior[Command] = {
    Behaviors.setup { context =>
      val finalData = mutable.Map.empty[String, Stats]

      Behaviors.receiveMessage {
        case UpdatedData(data) =>
          // Merge data from different files into finalData
          data.foreach {
            case (id, stats) =>
              val currentStats = finalData.getOrElse(id, Stats(0, 0.0, Int.MaxValue, Int.MinValue, 0))
              val newStats = Stats(
                currentStats.failedCount + stats.failedCount,
                currentStats.sum + stats.sum,
                Math.min(currentStats.min, stats.min),
                Math.max(currentStats.max, stats.max),
                currentStats.count + stats.count
              )
              finalData(id) = newStats
          }
          Behaviors.same

        case CalculateAverages(replyTo) =>
          printFinalData(finalData, context)
          replyTo ! AveragesCalculated()
          Behaviors.same

        case GetState(replyTo) =>
          replyTo ! State(finalData.toMap)
          Behaviors.same
      }
    }
  }

  def printFinalData(finalData: mutable.Map[String, Stats], context: ActorContext[Command]): Unit = {
    val count = finalData.map(_._2.count).sum
    val failedCount = finalData.map(_._2.failedCount).sum
    context.log.info(s"Num of processed measurements: ${count - failedCount}")
    context.log.info(s"Num of failed measurements: ${failedCount}\n")
    context.log.info("Sensors with highest avg humidity:\n")
    context.log.info("sensor-id,min,avg,max")
    finalData.toList
      .sortWith { (data1, data2) =>
        val stat1 = data1._2
        val stat2 = data2._2
        stat1.sum / stat1.count > stat2.sum / stat2.count
      }
      .foreach {
        case (id, stats) =>
          if (stats.count == stats.failedCount) {
            context.log.info(s"$id,NaN,NaN,NaN")
          } else {
            val average = stats.sum / stats.count
            context.log.info(s"$id,${stats.min},$average,${stats.max}")
          }
      }
  }
}

