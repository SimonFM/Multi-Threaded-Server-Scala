/**
 * Created by simon on 10/10/2015.
 *
 * I found this tutorial helpful in figuring out how to use
 * thread pools and concurrency in scala, I do take credit this implementation.
 * The library is built in to java as you can see, so all that work is
 * handled by that.
 *
 * https://twitter.github.io/scala_school/concurrency.html
 */
import java.io._
import java.net.{InetAddress, Socket, ServerSocket}
import java.util.concurrent.{Executors}
import scala.io._

object MultithreadedServer{
  var kill = false;
  var messageIP = ""

  class Server(portNumber: Int) extends Runnable{
    val NUMBER_OF_THREADS = 20
    val serverSocket = new ServerSocket(portNumber)
    println("Server running on port number: "+portNumber)
    val threadPool = Executors.newFixedThreadPool(NUMBER_OF_THREADS)

    def run(): Unit ={
      try{
        while (!serverSocket.isClosed) {
          if (kill == true){
            serverSocket.close()
            threadPool.shutdown()
            println("Server shut down")
          }
          else {
            messageIP = InetAddress.getLocalHost.getHostAddress
            threadPool.execute(new Worker(serverSocket.accept()))
            println("Made a new worker")
          }
        }
      }finally threadPool.shutdown()
    }
  }

  // A class that handles the work for the server.
  class Worker(socket: Socket) extends Runnable {

    val outputStream = socket.getOutputStream()
    val out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8")))
    lazy val in = new BufferedSource(socket.getInputStream()).getLines()

    var recv = ""
    def run() {
      if(in.hasNext){
        recv = in.next()
        println("Received: "+recv)
      }

      if(recv == "KILL_SERVICE") {
        kill = true
        println(Thread.currentThread.getName() + " is shutting down\n")
        out.println("KILL_SERVICE")
        out.flush()
        out.close()
        socket.close()
      }else if(recv.contains("HELO ")){
        val messageWithoutHELO = recv.drop(5)
        out.println(messageWithoutHELO+"IP:"+messageIP+"\n"+"Port:"+8000+"\n"+"StudentID:12307233\n")
        out.flush()
      }
      out.close()
    }
  }

  def main(args: Array[String]){
    if(args(0) == null) println("Please provide command line arguments")
    val Server = new Server(args(0).toInt)
    Server.run()
  }
}



