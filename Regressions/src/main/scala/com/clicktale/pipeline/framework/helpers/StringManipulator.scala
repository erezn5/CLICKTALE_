package com.clicktale.pipeline.framework.helpers

trait StringManipulator {

  /**
    * Receives a string and deletes all \n in it(new string returned).
    * @param inputString The input string
    * @return The formatted string.
    */
  def deleteLineEndings(inputString : String) : String = {
    inputString.filter(x => x != '\n' && x != '\r')
  }

  /**
    * Extracts the data string from a given query string(removing the first seven query params)
    * @param queryString The query string to extract from.
    * @return The data extracted from the query string as a string.
    */
  def extractDataString(queryString : String) : String = {
    val keyArray = queryString.split('&')
    keyArray.slice(7, keyArray.length - 1).mkString("&")
  }
}
