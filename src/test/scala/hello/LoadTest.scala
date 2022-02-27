package hello

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import org.springframework.boot.SpringApplication
import scala.concurrent.duration._

class LoadTest extends Simulation {

  before {
    val app = SpringApplication.run(classOf[Application])
    app.registerShutdownHook()
  }

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl("http://localhost:8080")

  object HelloWorldResource {
    val get: ChainBuilder = exec(http("HelloWorld")
      .get("/")
      .check(status.in(200 to 210)))
  }

  val myScenario: ScenarioBuilder = scenario("RampUpUsers")
    .exec(HelloWorldResource.get)
    .pause(100.millis);

  setUp(myScenario.inject(
    incrementUsersPerSec(20)
      .times(5)
      .eachLevelLasting(5 seconds)
      .separatedByRampsLasting(5 seconds)
      .startingFrom(20)
  )).protocols(httpProtocol)
    .assertions(global.successfulRequests.percent.is(100))
}
