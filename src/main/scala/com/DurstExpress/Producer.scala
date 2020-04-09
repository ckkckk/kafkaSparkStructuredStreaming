package com.stream.sparksreaming
import org.apache.kafka.clients.producer._
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.kafka.clients.producer.ProducerConfig

import scala.io.Source


object Producer extends App{
  import java.util.Properties
  Logger.getLogger("org").setLevel(Level.ERROR)

  val spark: SparkSession = SparkSession
    .builder()
    .master("local[*]")
    .appName("KafkaSparkStreaming")
    .getOrCreate()

  val  props = new Properties()
  props.put("bootstrap.servers", "localhost:9092")
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

  // Used for Strong Consistency Purposes for precise data. It causes latency and commented-out for this application
  //props.put("acks", "all")

  //Linger up to 1000ms(1 second) before sending batch if size not met. This will auto-manage latency problem
  props.put("linger.ms", 1000.asInstanceOf[java.lang.Integer])

  //Batch up to 192K buffer sizes. ~1000 users in a batch. Optimize throughput by considering latency.
  props.put("batch.size", 196608.asInstanceOf[java.lang.Integer])

  //Use Snappy compression for batch compression.
  props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy")

  val producer = new KafkaProducer[String, String](props)

  val TOPIC="Users4"

  val bufferedSource = Source.fromFile("/home/ckk/Desktop/mock_data.json")
  for (line <- bufferedSource.getLines) {
    // Put Records by hashing them on ID column will help to send same users to same partition and well-distribution
    val record = new ProducerRecord(TOPIC, "id", s"$line")
    producer.send(record)
  }

  producer.close()

  bufferedSource.close

}
