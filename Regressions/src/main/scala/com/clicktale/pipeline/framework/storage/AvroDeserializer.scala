package com.clicktale.pipeline.framework.storage

import java.io._
import java.util.zip.ZipInputStream

import org.apache.commons.io.IOUtils

import scala.io.{BufferedSource, Source}
import scala.util.control.NonFatal

/**
  * Created by Asia.Salner on 13/07/2017.
  */
object AvroDeserializer {
  case class VersionedRecording(avroVersion: Short, recordingBytes: Array[Byte])

  object VersionedRecording {
    lazy val empty = VersionedRecording(-1, Array.empty)
  }

  object AvroVersionExtractor {
    def extractVersionRecordingPair(buffer: InputStream): VersionedRecording = {

      val dataInputStream = new DataInputStream(buffer)
      val avroVersion = dataInputStream.readShort()

      val recording = IOUtils.toByteArray(dataInputStream)
      VersionedRecording(avroVersion, recording)
    }
  }

  val asd: BufferedSource = Source.fromURL("")


  def unzipRecordings(input :BufferedSource): Map[Long, VersionedRecording] = {
    val bytes = IOUtils.toByteArray(input.reader())
    val zip = new ZipInputStream(new ByteArrayInputStream(bytes))
    Stream
      .continually(zip.getNextEntry)
      .takeWhile(zipEntry => zipEntry != null)
      .map { zipEntry =>
        var versionedRecording: VersionedRecording = null

        /**
          * Yes, this code is imperative, I know. It is executed on the hot path and we need to keep allocations
          * to a minimum. If we wrapped this with a `Try`, we'd be doing so one per recording, and for a report
          * running for 1,000,000 recordings this is just junk in memory that we don't need.
          */
        try {
          versionedRecording =
            AvroVersionExtractor.extractVersionRecordingPair(zip)
        } catch {
          case NonFatal(e) =>
            println(s"Failed to extract avro version. SessionId: ${zipEntry.getName}")
            versionedRecording = VersionedRecording.empty
        }
        (zipEntry.getName.toLong, versionedRecording)
      }
      .toMap
  }
}

