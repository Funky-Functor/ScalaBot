package com.funkyfunctor.scalabot.twitch.eventHandlers

import com.funkyfunctor.scalabot.twitch.{EventHandlerRegisteringException, ScalaBotException}
import com.funkyfunctor.scalabot.{EventHandlerRegistrar, JavaEventHandler}
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.chat.events.AbstractChannelEvent
import com.github.twitch4j.chat.events.channel._
import com.github.twitch4j.chat.events.roomstate._
import zio.ZIO
import zio.logging.{Logging, log}

import java.util.function.Consumer
import scala.reflect.ClassTag

object EventHandler {
  def registerHandlers(
      twitchClient: TwitchClient,
      eventHandlers: Seq[EventHandler[_]]
  ): ZIO[Logging, ScalaBotException, Unit] = {
    for {
      eventManager <- ZIO { twitchClient.getEventManager }
      _ <- ZIO.foreach_(eventHandlers) { eventHandler =>
        log.debug(s"Class for $eventHandler is '${eventHandler.getClazz()}'") *>
          ZIO(EventHandlerRegistrar.registerHandlerToEventManager(eventManager, eventHandler))
      }
    } yield ()
  }.mapError(error => EventHandlerRegisteringException(error))
}

sealed abstract class EventHandler[E <: AbstractChannelEvent](consumerFunction: E => Unit)(implicit tag: ClassTag[E])
    extends JavaEventHandler[E] {
  override def getConsumer: Consumer[E] = (t: E) => consumerFunction(t)

  override def getClazz: Class[E] = tag.runtimeClass.asInstanceOf[Class[E]]
}

case class BitsBadgeEarnedEventHandler(f: BitsBadgeEarnedEvent => Unit) extends EventHandler[BitsBadgeEarnedEvent](f)
case class ChannelJoinEventHandler(f: ChannelJoinEvent => Unit)         extends EventHandler[ChannelJoinEvent](f)
case class ChannelLeaveEventHandler(f: ChannelLeaveEvent => Unit)       extends EventHandler[ChannelLeaveEvent](f)
case class ChannelMessageActionEventHandler(f: ChannelMessageActionEvent => Unit)
    extends EventHandler[ChannelMessageActionEvent](f)
case class ChannelMessageEventHandler(f: ChannelMessageEvent => Unit) extends EventHandler[ChannelMessageEvent](f)
case class ChannelModEventHandler(f: ChannelModEvent => Unit)         extends EventHandler[ChannelModEvent](f)
case class ChannelNoticeEventHandler(f: ChannelNoticeEvent => Unit)   extends EventHandler[ChannelNoticeEvent](f)
case class ChannelStateEventHandler(f: ChannelStateEvent => Unit)     extends EventHandler[ChannelStateEvent](f)
case class CheerEventHandler(f: CheerEvent => Unit)                   extends EventHandler[CheerEvent](f)
case class ClearChatEventHandler(f: ClearChatEvent => Unit)           extends EventHandler[ClearChatEvent](f)
case class DonationEventHandler(f: DonationEvent => Unit)             extends EventHandler[DonationEvent](f)
case class ExtendSubscriptionEventHandler(f: ExtendSubscriptionEvent => Unit)
    extends EventHandler[ExtendSubscriptionEvent](f)
case class FollowEventHandler(f: FollowEvent => Unit) extends EventHandler[FollowEvent](f)
case class GiftSubscriptionsEventHandler(f: GiftSubscriptionsEvent => Unit)
    extends EventHandler[GiftSubscriptionsEvent](f)
case class GiftSubUpgradeEventHandler(f: GiftSubUpgradeEvent => Unit)   extends EventHandler[GiftSubUpgradeEvent](f)
case class HostOnEventHandler(f: HostOnEvent => Unit)                   extends EventHandler[HostOnEvent](f)
case class ListModsEventHandler(f: ListModsEvent => Unit)               extends EventHandler[ListModsEvent](f)
case class ListVipsEventHandler(f: ListVipsEvent => Unit)               extends EventHandler[ListVipsEvent](f)
case class MessageDeleteErrorHandler(f: MessageDeleteError => Unit)     extends EventHandler[MessageDeleteError](f)
case class MessageDeleteSuccessHandler(f: MessageDeleteSuccess => Unit) extends EventHandler[MessageDeleteSuccess](f)
case class PayForwardEventHandler(f: PayForwardEvent => Unit)           extends EventHandler[PayForwardEvent](f)
case class PrimeGiftReceivedEventHandler(f: PrimeGiftReceivedEvent => Unit)
    extends EventHandler[PrimeGiftReceivedEvent](f)
case class PrimeSubUpgradeEventHandler(f: PrimeSubUpgradeEvent => Unit)   extends EventHandler[PrimeSubUpgradeEvent](f)
case class RaidCancellationEventHandler(f: RaidCancellationEvent => Unit) extends EventHandler[RaidCancellationEvent](f)
case class RaidEventHandler(f: RaidEvent => Unit)                         extends EventHandler[RaidEvent](f)
case class RewardGiftEventHandler(f: RewardGiftEvent => Unit)             extends EventHandler[RewardGiftEvent](f)
case class RitualEventHandler(f: RitualEvent => Unit)                     extends EventHandler[RitualEvent](f)
case class SubscriptionEventHandler(f: SubscriptionEvent => Unit)         extends EventHandler[SubscriptionEvent](f)
case class UserBanEventHandler(f: UserBanEvent => Unit)                   extends EventHandler[UserBanEvent](f)
case class UserStateEventHandler(f: UserStateEvent => Unit)               extends EventHandler[UserStateEvent](f)
case class UserTimeoutEventHandler(f: UserTimeoutEvent => Unit)           extends EventHandler[UserTimeoutEvent](f)

//ChannelStatesEvent
//case class BroadcasterLanguageEventHandler(f: BroadcasterLanguageEvent => Unit) extends EventHandler[BroadcasterLanguageEvent](f)
case class EmoteOnlyEventHandler(f: EmoteOnlyEvent => Unit)             extends EventHandler[EmoteOnlyEvent](f)
case class FollowersOnlyEventHandler(f: FollowersOnlyEvent => Unit)     extends EventHandler[FollowersOnlyEvent](f)
case class Robot9000EventHandler(f: Robot9000Event => Unit)             extends EventHandler[Robot9000Event](f)
case class SlowModeEventHandler(f: SlowModeEvent => Unit)               extends EventHandler[SlowModeEvent](f)
case class SubscribersOnlyEventHandler(f: SubscribersOnlyEvent => Unit) extends EventHandler[SubscribersOnlyEvent](f)
