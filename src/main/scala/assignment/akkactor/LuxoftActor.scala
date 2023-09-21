package assignment.akkactor

import akka.actor.typed.Behavior

trait LuxoftActor {
  def behavior(): Behavior[Command]
}