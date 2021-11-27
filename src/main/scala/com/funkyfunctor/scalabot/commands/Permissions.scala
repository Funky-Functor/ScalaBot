package com.funkyfunctor.scalabot.commands

object Permissions {
  case object BLOCKED extends Permissions(0) {
    override val resourceMask: Long = 0
  }
  case object OWNER extends Permissions(32) {
    override val resourceMask: Long =
      userMask + MODERATOR.userMask + VIP.userMask + SUBSCRIBER.userMask + TRUSTED.userMask + EVERYONE.userMask
  }
  case object MODERATOR extends Permissions(16) {
    override val resourceMask: Long =
      userMask + VIP.userMask + SUBSCRIBER.userMask + TRUSTED.userMask + EVERYONE.userMask
  }
  case object VIP extends Permissions(8) {
    override val resourceMask: Long =
      userMask + SUBSCRIBER.userMask + TRUSTED.userMask + EVERYONE.userMask
  }
  case object SUBSCRIBER extends Permissions(4) {
    override val resourceMask: Long =
      userMask + TRUSTED.userMask + EVERYONE.userMask
  }
  case object TRUSTED extends Permissions(2) {
    override val resourceMask: Long =
      userMask + EVERYONE.userMask
  }
  case object EVERYONE extends Permissions(1) {
    override val resourceMask: Long = Int.MaxValue
  }
  case class UserSpecific(whitelist: Set[String] = Set.empty) extends Permissions(0) {
    override val resourceMask: Long = 0
  }

  def canAccess(resourcePermission: Permissions, userPermission: Permissions, userName: => String): Boolean = {
    if (userPermission == BLOCKED)
      false
    else
      resourcePermission match {
        case BLOCKED => false
        case userSpecificPermission: UserSpecific =>
          (userPermission == OWNER) ||
            userSpecificPermission.whitelist.contains(userName)
        case _ =>
          // If permissions don't match
          // resource: 0001
          // user:     0010
          // => resource & user = 0001 & 0010 = 0000
          //
          // If permissions match
          // resource: 0011
          // user:     0010
          // => resource & user = 0011 & 0010 = 0010
          (resourcePermission.resourceMask & userPermission.userMask) != 0
      }
  }
}

sealed abstract class Permissions(val userMask: Long) {
  def resourceMask: Long
}
