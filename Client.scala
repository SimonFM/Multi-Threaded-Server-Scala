import java.io._
import java.net.{Socket}
import scala.io._

object Client {
  // various member data to easily change
  val host = "localhost"
  val port = 8000

  def run(): Unit ={
    val socket = new Socket(host, port)
    val outputStream = socket.getOutputStream()
    lazy val in = new BufferedSource(socket.getInputStream()).getLines()
    val out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8")))
    //out.println("KILL_SERVICE")
    out.println("HELO test")
    out.flush()// send the request to the server

    var recv = in.next()
    var notConverted = true
    //Loop forever, while the message hasn't been converted.
    println("Received: "+recv)

    while(notConverted){
      if(recv == "KILL_SERVICE"){
        socket.close()
        notConverted = true
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
  }
  // Main method that runs the program
  def main(args: Array[String]) {
    var x = 0;
    for( x <- 1 to 1000){
      Client.run()
    }
  }
}