import java.io._
import java.net.{Socket}
import scala.io._

object Client {
  // various member data to easily change
  val host = "localhost"
  val port = 8000

  def run(boolean: Boolean): Unit ={
    try{
      val message = readLine("Please enter message: ")
      val socket = new Socket(host, port)
      val outputStream = socket.getOutputStream()
      lazy val in = new BufferedSource(socket.getInputStream()).getLines()
      val out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8")))
      out.println(message)
      out.flush()// send the request to the server

      var recv = in.next()
      var notConverted = true
      //Loop forever, while the message hasn't been converted.

      while(notConverted){
        if(recv == "KILL_SERVICE"){
          notConverted = false
          socket.close()
        }
        else{
          println(recv)
          // A check to see if the input isn't Empty
          if(in.hasNext) recv = in.next()
          // Otherwise tell it to stop
          else notConverted = false
        }

      }
      socket.close()
      println("Socket Closed!")
    }catch{
      case notConnected : java.net.ConnectException =>
        println("Can't connect to: "+host+" on port: "+port)
    }

  }
  // Main method that runs the program
  def main(args: Array[String]) {
    var x = 0;
    for( x <- 1 to 2){
      //if (x == 2 ) Client.run(false)
      Client.run(true)
    }
  }
}