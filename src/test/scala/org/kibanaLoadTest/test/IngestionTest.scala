package org.kibanaLoadTest.test

import java.io.File

import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.kibanaLoadTest.ESConfiguration
import org.kibanaLoadTest.helpers.Helper.{getLastReportPath, getTargetPath}
import org.kibanaLoadTest.helpers.{ESWrapper, Helper, LogParser}

class IngestionTest {

  val expCollectionSize = 305
  val expRequestString = "10 - login - 1601482441483 - 1601482441868 - 385 - OK"

  @Test
  def parseLogsTest(): Unit = {
    val logFilePath: String = new File(
      Helper.getTargetPath() + File.separator + "test-classes"
        + File.separator + "log" + File.separator
        + "simulation.log"
    ).getAbsolutePath
    val requests = LogParser.getRequests(logFilePath)
    assertEquals(
      expCollectionSize,
      requests.length,
      "Incorrect collection size"
    )
    assertEquals(
      expRequestString,
      requests(0).toString,
      "Incorrect content in first object"
    )
  }

  @Test
  def ingestReportTest(): Unit = {
    val host = System.getenv("HOST_FROM_VAULT")
    val username = System.getenv("USER_FROM_VAULT")
    val password = System.getenv("PASS_FROM_VAULT")
    val esConfig = new ESConfiguration(
      ConfigFactory.load
        .withValue("host", ConfigValueFactory.fromAnyRef(host))
        .withValue("username", ConfigValueFactory.fromAnyRef(username))
        .withValue("password", ConfigValueFactory.fromAnyRef(password))
    )

    val esClient = new ESWrapper(esConfig)
    val logFilePath = getLastReportPath() + File.separator + "simulation.log"
    val lastDeploymentFilePath =
      getTargetPath() + File.separator + "lastDeployment.txt"
    esClient.ingest(logFilePath, lastDeploymentFilePath)
  }

  @Test
  def saveDeploymentConfigTest(): Unit = {
    val meta = Map(
      "deploymentId" -> "asdkjqwr9cuw4j23k",
      "baseUrl" -> "http://localhost:5620",
      "version" -> "8.0.0"
    )
    Helper.writeMapToFile(
      meta,
      Helper.getTargetPath() + File.separator + "lastDeployment.txt"
    );
  }
}
