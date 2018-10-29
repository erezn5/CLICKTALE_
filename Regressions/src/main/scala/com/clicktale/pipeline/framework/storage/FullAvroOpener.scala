package com.clicktale.pipeline.storagemaster
package logic.archives.binary

import java.io.{BufferedInputStream, ByteArrayInputStream}
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.Base64.Encoder

import com.clicktale.parsing.avro.{AvroRecordingSerializer, AvroVersionExtractor}
import com.clicktale.parsing.xml.{RecordingXmlReader, RecordingXmlWriter}
import com.clicktale.pipeline.storagemaster.logic.archives.binary.FullSessionBinarySerializer.StringBinaryDeserialized
import com.typesafe.scalalogging.StrictLogging
import org.apache.commons.io.IOUtils

class FullSessionBinarySerializer
  extends StrictLogging {
  private val base64Encoder: Encoder = Base64.getEncoder
  private val xmlPrinter = new scala.xml.PrettyPrinter(1000, 2)
  private val recordingSerializer = new AvroRecordingSerializer()

  def convertStringToBytes(eventsXml: String): Array[Byte] = {

    val eventsXmlStream = IOUtils.toInputStream(eventsXml, StandardCharsets.UTF_8)
    val bufferedInputStream = new BufferedInputStream(eventsXmlStream)
    val recording = RecordingXmlReader.parseRecording(bufferedInputStream)
    val serializedRecording = recordingSerializer.serialize(recording)
    serializedRecording
  }

  def convertBytesToString(byteArray: Array[Byte]): StringBinaryDeserialized = {
      val byteArrayStream = new ByteArrayInputStream(byteArray)
      val versionedRecording = AvroVersionExtractor.extractVersionRecordingPair(byteArrayStream)
      val deserializeRecording = recordingSerializer.deserialize(versionedRecording.recordingBytes)
      val encodedBinary = base64Encoder.encodeToString(byteArray)
      val generatedXml = RecordingXmlWriter.serializeRecoding(deserializeRecording)
      val formedXml = xmlPrinter.format(generatedXml)
      StringBinaryDeserialized(formedXml, encodedBinary, versionedRecording.avroVersion, byteArray)
    }

}

object FullSessionBinarySerializer {

  case class StringBinaryDeserialized(deserialized: String,
                                      encodedBinary: String,
                                      version: Int,
                                      source: Array[Byte]) {
    def length = deserialized.length
  }

}