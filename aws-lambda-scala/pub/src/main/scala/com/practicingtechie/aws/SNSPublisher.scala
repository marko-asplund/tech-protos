package com.practicingtechie.aws

object SNSPublisher {
  import com.amazonaws.auth.profile.ProfileCredentialsProvider
  import com.amazonaws.services.sns.AmazonSNSClientBuilder
  import com.amazonaws.regions.Regions
  import com.amazonaws.services.sns.model.PublishRequest
  import com.amazonaws.services.sns.model.MessageAttributeValue
  import collection.JavaConverters._
  import argonaut._, Argonaut._

  val topicArn = "arn:aws:sns:eu-central-1:429963740182:user-info-changes-topic"

  def main(args: Array[String]): Unit = {
    val regionId = Regions.fromName("eu-central-1")
    val credentials = new ProfileCredentialsProvider
    val snsClient = AmazonSNSClientBuilder.standard().withRegion(regionId).withCredentials(credentials).build()

    val ev = UserChangeEvent(UserInfo("user1", "erkki", "user", "erkki@acme.com"), UserEventTypes.Added)
    val attributes = Map("content-type" ->
      new MessageAttributeValue().withDataType("String").withStringValue("application/json")).
      asJava

    val publishRequest = new PublishRequest(topicArn, ev.asJson.spaces2).withMessageAttributes(attributes)

    val publishResult = snsClient.publish(publishRequest)
    //print MessageId of message published to SNS topic
    println("MessageId - " + publishResult.getMessageId())
  }

}
