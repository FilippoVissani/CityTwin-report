package it.unibo.citytwin.core.actors

class AsyncTestingResourceSpec extends AnyWordSpec with BeforeAndAfterAll with Matchers:

  val testKit: ActorTestKit = ActorTestKit()

  override def afterAll(): Unit = testKit.shutdownTestKit()

  "Resource actor" should {

    "Ask resources to mainstay" in {
      val probe         = testKit.createTestProbe[MainstayActorCommand]()
      val resourceActor = testKit.spawn(ResourceActor())
      val resource      = Resource(name = Some("sensor1"))
      resourceActor ! SetMainstayActorsToResourceActor(Set(probe.ref))
      resourceActor ! ResourceChanged(resource)
      probe.expectMessage(UpdateResources(Set((resourceActor, resource))))
      testKit.stop(resourceActor)
    }

    "Send response when resource state is asked" in {
      val mainstayActor = testKit.spawn(MainstayActor("", ""))
      val resourceActor = testKit.spawn(ResourceActor())
      val probe         = testKit.createTestProbe[ResourcesFromMainstayResponse]()
      val resource      = Resource(name = Some("sensor1"), nodeState = Some(true))
      resourceActor ! SetMainstayActorsToResourceActor(Set(mainstayActor))
      resourceActor ! ResourceChanged(resource)
      resourceActor ! AskResourcesToMainstay(probe.ref, Set("sensor1"))
      probe.expectMessage(ResourcesFromMainstayResponse(Set(resource)))
      testKit.stop(resourceActor)
      testKit.stop(mainstayActor)
    }

    "Set mainstays correctly and notify new resource state to mainstay" in {
      val probe    = testKit.createTestProbe[MainstayActorCommand]()
      val resource = testKit.spawn(ResourceActor())
      resource ! SetMainstayActorsToResourceActor(Set(probe.ref))
      resource ! ResourceChanged(Resource())
      probe.expectMessage(UpdateResources(Set((resource, Resource()))))
      testKit.stop(resource)
    }
  }