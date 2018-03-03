package com.practicingtechie

package object aws {
  import argonaut._, Argonaut._

  object UserEventTypes extends Enumeration {
    type UserEventType = Value
    val Added, Modified, Disabled, Removed = Value
  }

  case class UserInfo(userId: String, firstName: String, lastName: String, emailAddress: String)
  case class UserChangeEvent(user: UserInfo, event: UserEventTypes.UserEventType)

  implicit def UserInfoCodecJson = casecodec4(UserInfo.apply, UserInfo.unapply)("userId", "firstName", "lastName", "emailAddress")
  implicit def UserEventTypeCodecJson: CodecJson[UserEventTypes.UserEventType] = CodecJson(
    ev => ev.toString.asJson,
    c => c.focus.as[String].map(UserEventTypes.withName)
  )
  implicit def UserChangeEventCodecJson = casecodec2(UserChangeEvent.apply, UserChangeEvent.unapply)("user", "event")
}