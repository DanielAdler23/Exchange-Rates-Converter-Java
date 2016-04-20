package src

import java.io.IOException
import java.net.URL
import java.text.DateFormat
import java.util.Date
import java.io.InputStream
import javax.swing.JTable
import javax.swing.JTextField
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import org.apache.log4j.Logger
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.SAXException
import XMLParse._
import scala.collection.JavaConversions._

/*
 * XMLParse class.
 * Breaks down the xml to arrays and string...
 * 
 */

object XMLParse
{

  var rows: Array[Array[Any]] = _
}

class XMLParse 
{

  
  var columns: Array[String] = Array("NAME", "UNIT", "CODE", "COUNTRY", "RATE", "CHANGE")
 
  var rows: Array[Array[Any]] = _

  private var lastUpdate: String = _

  def getRows(): Array[Array[Any]] = rows
  
  def getColumns: Array[String] = columns

  def getLastupdate(): String = lastUpdate

  def update(xmlLogger: Logger)
  {
    var input: InputStream = null
    try {
      xmlLogger.info("Opening stream to bank of israel")           
      val url = new URL("http://boi.org.il/currency.xml")                              //opens a connection with the specified url.
      input = url.openStream()
      val factory = DocumentBuilderFactory.newInstance()
      val builder = factory.newDocumentBuilder()
      val doc = builder.parse(input)                                                   //builds a document and inserts the input from the url.
      lastUpdate = doc.getElementsByTagName("LAST_UPDATE").item(0).getTextContent
      val list = doc.getElementsByTagName("CURRENCY")
      val length = list.getLength
      rows = Array.ofDim[Any](length, 6)
      for (i <- 0 until length) {
        val currentNode = list.item(i)
        for (j <- 0 until 6 if currentNode.getNodeType == Node.ELEMENT_NODE) {        //separates the xml to linked lists by the name tags from the xml info tree.
          val tableElement = currentNode.asInstanceOf[Element]
          j match {
            case 0 => rows(i)(j) = tableElement.getElementsByTagName("NAME").item(0).getTextContent
            case 1 => rows(i)(j) = tableElement.getElementsByTagName("UNIT").item(0).getTextContent
            case 2 => rows(i)(j) = tableElement.getElementsByTagName("CURRENCYCODE").item(0)
              .getTextContent
            case 3 => rows(i)(j) = tableElement.getElementsByTagName("COUNTRY").item(0)
              .getTextContent
            case 4 => rows(i)(j) = tableElement.getElementsByTagName("RATE").item(0).getTextContent
            case 5 => rows(i)(j) = tableElement.getElementsByTagName("CHANGE").item(0)
              .getTextContent
          }
        }
      }
    } catch {
      case e: IOException => {
        e.printStackTrace()
        xmlLogger.info("Xml parse threw I/O exception")
      }
      case e: ParserConfigurationException => e.printStackTrace()
      case e: SAXException => e.printStackTrace()
    } finally {
      if (input != null) {
        try {
          xmlLogger.info("Closing stream with bank of israel")                            //finally closes the connection with the url.
          input.close()
        } catch {
          case e: IOException => {
            xmlLogger.info("Couldn't close stream with the bank")
            e.printStackTrace()
          }
        }
      }
    }
  }
}



/*
 * UpdateThread class.
 * Extends thread and is responsible for the constant checking and updating of the table.
 * Very similar to the XMLParse class.
 */
class UpdateThread extends Thread {

 
  var newRates: Array[String] = _

  var newChanges: Array[String] = _

  var newLastUpdate: String = _

  var rightNow: Date = _

  var timeNow: DateFormat = _

  var timeOutput: String = _

  def run(myTable: JTable, time: JTextField, log: Logger) {
    do {
      var input1: InputStream = null
      try {
        log.info("Next update in 10 minutes")
        Thread.sleep(600000)                                          // update table every 10 minutes (600000 milliseconds = 10 minutes)
        log.info("Table updated")
        val url1 = new URL("http://boi.org.il/currency.xml")
        input1 = url1.openStream()
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(input1)
        newLastUpdate = doc.getElementsByTagName("LAST_UPDATE").item(0).getTextContent
        newRates = Array.ofDim[String](14)
        newChanges = Array.ofDim[String](14)
        val newRatesList = doc.getElementsByTagName("RATE")
        val newChangesList = doc.getElementsByTagName("CHANGE")
        for (i <- 0 until 14) {
          val rateNode = newRatesList.item(i)
          val changeNode = newChangesList.item(i)
          val rateElement = rateNode.asInstanceOf[Element]
          val changeElement = changeNode.asInstanceOf[Element]
          newRates(i) = rateElement.getTextContent                              //saves the new rates and changes to an array.
          newChanges(i) = changeElement.getTextContent                          //
        }
        for (x <- 0 until 14) {                                                  //updates the rates and change columns.
          val newRateVal = newRates(x)
          val newChangeVal = newChanges(x)
          myTable.getModel.setValueAt(newRateVal, x, 4)
          myTable.getModel.setValueAt(newChangeVal, x, 5)
        }
        rightNow = new Date()
        timeNow = DateFormat.getTimeInstance(DateFormat.DEFAULT)
        timeOutput = timeNow.format(rightNow)                                    //updates the exact time of the update.
        time.setText(newLastUpdate + "  |  " + timeOutput)
      } catch {
        case e: Exception => {
          e.printStackTrace()
          log.info("Update thread threw an exception")
        }
      }
    } while (true);                                                            //while program is running...
  }
}




