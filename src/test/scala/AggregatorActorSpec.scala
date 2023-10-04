package assignment.akkactor

import akka.actor.testkit.typed.scaladsl.{ActorTestKit, ScalaTestWithActorTestKit}
import akka.actor.typed.ActorRef
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class AggregatorActorSpec
    extends ScalaTestWithActorTestKit
    with AnyWordSpecLike
    with Matchers
    with MockitoSugar
    with BeforeAndAfterEach {

  val testSystem = ActorTestKit()

  "An AggregationActor" should {
    "correctly aggregate and calculate averages" in {
      val ag = new AggregationActor
      val aggregationActor: ActorRef[Command] =
        testSystem.spawn(ag.behavior(), "aggregationActor")

      val updatedData1 = UpdatedData(Map("sensor1" -> Stats(0, 10.0, 0, 10, 1)))
      val updatedData2 = UpdatedData(Map("sensor2" -> Stats(0, 20.0, 0, 20, 1)))
      aggregationActor ! updatedData1
      aggregationActor ! updatedData2

      val averagesProbe = testSystem.createTestProbe[AveragesCalculated]()
      aggregationActor ! CalculateAverages(averagesProbe.ref)

      averagesProbe.expectMessage(AveragesCalculated())

      val stateProbe = testSystem.createTestProbe[State]()
      aggregationActor ! GetState(stateProbe.ref)
      val state = stateProbe.receiveMessage()

      state.data("sensor1") shouldEqual Stats(0, 10.0, 0, 10, 1)
      state.data("sensor2") shouldEqual Stats(0, 20.0, 0, 20, 1)
    }
  }

  override def afterAll(): Unit = {
    testSystem.shutdownTestKit()
    super.afterAll()
  }
}
