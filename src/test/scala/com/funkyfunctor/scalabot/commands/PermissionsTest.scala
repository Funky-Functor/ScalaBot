package com.funkyfunctor.scalabot.commands

import com.funkyfunctor.scalabot.commands.Permissions._
import zio.test._

object PermissionsTest extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] = suite("PermissionsTest")(
    canAccessTests
  )

  private val canAccessTests = suite("canAccess(Permissions, Permissions, => String)")(
    test1_01,
    test1_02,
    test1_03,
    test1_04,
    test1_05,
    test1_06,
    test1_07,
    test1_08,
    test1_09,
    test1_10
  )

  private lazy val test1_01 = test("a OWNER user should have access to everything (except BLOCKED resource)") {
    val userPermission = OWNER

    assertTrue(!canAccess(BLOCKED, userPermission, ???))
    assertTrue(canAccess(OWNER, userPermission, ???))
    assertTrue(canAccess(MODERATOR, userPermission, ???))
    assertTrue(canAccess(VIP, userPermission, ???))
    assertTrue(canAccess(SUBSCRIBER, userPermission, ???))
    assertTrue(canAccess(TRUSTED, userPermission, ???))
    assertTrue(canAccess(EVERYONE, userPermission, ???))
    assertTrue(canAccess(UserSpecific(), userPermission, ???))
  }

  private lazy val test1_02 = test("a MODERATOR user should have access to the proper resources") {
    val userPermission = MODERATOR

    assertTrue(!canAccess(BLOCKED, userPermission, ???))
    assertTrue(!canAccess(OWNER, userPermission, ???))
    assertTrue(canAccess(MODERATOR, userPermission, ???))
    assertTrue(canAccess(VIP, userPermission, ???))
    assertTrue(canAccess(SUBSCRIBER, userPermission, ???))
    assertTrue(canAccess(TRUSTED, userPermission, ???))
    assertTrue(canAccess(EVERYONE, userPermission, ???))
  }

  private lazy val test1_03 = test("a VIP user should have access to the proper resources") {
    val userPermission = VIP

    assertTrue(!canAccess(BLOCKED, userPermission, ???))
    assertTrue(!canAccess(OWNER, userPermission, ???))
    assertTrue(!canAccess(MODERATOR, userPermission, ???))
    assertTrue(canAccess(VIP, userPermission, ???))
    assertTrue(canAccess(SUBSCRIBER, userPermission, ???))
    assertTrue(canAccess(TRUSTED, userPermission, ???))
    assertTrue(canAccess(EVERYONE, userPermission, ???))
  }

  private lazy val test1_04 = test("a SUBSCRIBER user should have access to the proper resources") {
    val userPermission = SUBSCRIBER

    assertTrue(!canAccess(BLOCKED, userPermission, ???))
    assertTrue(!canAccess(OWNER, userPermission, ???))
    assertTrue(!canAccess(MODERATOR, userPermission, ???))
    assertTrue(!canAccess(VIP, userPermission, ???))
    assertTrue(canAccess(SUBSCRIBER, userPermission, ???))
    assertTrue(canAccess(TRUSTED, userPermission, ???))
    assertTrue(canAccess(EVERYONE, userPermission, ???))
  }

  private lazy val test1_05 = test("a TRUSTED user should have access to the proper resources") {
    val userPermission = TRUSTED

    assertTrue(!canAccess(BLOCKED, userPermission, ???))
    assertTrue(!canAccess(OWNER, userPermission, ???))
    assertTrue(!canAccess(MODERATOR, userPermission, ???))
    assertTrue(!canAccess(VIP, userPermission, ???))
    assertTrue(!canAccess(SUBSCRIBER, userPermission, ???))
    assertTrue(canAccess(TRUSTED, userPermission, ???))
    assertTrue(canAccess(EVERYONE, userPermission, ???))
  }

  private lazy val test1_06 = test("an EVERYONE user should have access to the proper resources") {
    val userPermission = EVERYONE

    assertTrue(!canAccess(BLOCKED, userPermission, ???))
    assertTrue(!canAccess(OWNER, userPermission, ???))
    assertTrue(!canAccess(MODERATOR, userPermission, ???))
    assertTrue(!canAccess(VIP, userPermission, ???))
    assertTrue(!canAccess(SUBSCRIBER, userPermission, ???))
    assertTrue(!canAccess(TRUSTED, userPermission, ???))
    assertTrue(canAccess(EVERYONE, userPermission, ???))
  }

  private lazy val test1_07 = test("a BLOCKED user should have access to no resources") {
    val userPermission = BLOCKED

    assertTrue(!canAccess(BLOCKED, userPermission, ???))
    assertTrue(!canAccess(OWNER, userPermission, ???))
    assertTrue(!canAccess(MODERATOR, userPermission, ???))
    assertTrue(!canAccess(VIP, userPermission, ???))
    assertTrue(!canAccess(SUBSCRIBER, userPermission, ???))
    assertTrue(!canAccess(TRUSTED, userPermission, ???))
    assertTrue(!canAccess(EVERYONE, userPermission, ???))
  }

  private lazy val test1_08 = testM("a USER_SPECIFIC resource should be accessible by the proper user") {
    check(Gen.anyString) { userName =>
      val resourcePermission = UserSpecific(Set(userName))

      assertTrue(canAccess(resourcePermission, EVERYONE, userName))
    }
  }

  private lazy val test1_09 = testM("a USER_SPECIFIC resource should be not accessible by an unauthorized user") {
    check(Gen.anyString, Gen.anyString) { (username1, username2) =>
      val resourcePermission = UserSpecific(Set(username1))

      if (username1 != username2)
        assertTrue(!canAccess(resourcePermission, EVERYONE, username2))
      else
        assertTrue(canAccess(resourcePermission, EVERYONE, username2))
    }
  }

  private lazy val test1_10 = testM("a USER_SPECIFIC resource should be not accessible by a BLOCKED user") {
    check(Gen.anyString) { userName =>
      val resourcePermission = UserSpecific(Set(userName))

      assertTrue(!canAccess(resourcePermission, BLOCKED, userName))
    }
  }
}
