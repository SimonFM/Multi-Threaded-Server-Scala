/**
 * Created by Simon on 10/10/2015.
 *
 * I found this tutorial helpful in figuring out how to use
 * thread pools and concurrency in scala, I do take credit this implementation.
 * The library is built in to java as you can see, so all that work is
 * handled by that.
 *
 * https://twitter.github.io/scala_school/concurrency.html
 */
import java.io._
import java.net._
import java.util
import java.util.concurrent.{Executors}
import scala.io._

object MultithreadedServer{
  /**
   * This is a simple server class to represent a multi threaded server.
   * It contains both a Server and a Worker class. The worker doing all the
   * work for the server.
   * @param portNumber - The port the server operates on.
   */
  class Server(portNumber: Int) extends Runnable {
    var NUMBER_OF_THREADS = 20 // Maximum number of threads.
    val serverSocket = new ServerSocket(portNumber) // setting up the server
    println("Server running on port number: " + portNumber) // display to console
    val threadPool = Executors.newFixedThreadPool(NUMBER_OF_THREADS) // create the thread pool
    /**
     * This is the run method of the server, it is needed as I have extended my server
     * to be Runnable, so I could have multiple servers should the need arise.
     * It creates a new socket for every new connection to the server.
     * It loops forever, as long as the server is not closed.
     */
    def run(): Unit = {
      try {
        while (!serverSocket.isClosed) {
          try{
            threadPool.execute(new Worker(serverSocket.accept())) // allocate a new Worker a Socket
            println("Made a new worker")
          }catch {
            case socketE: SocketException =>
              println("Sorry, the server isn't running");
          }
        }
      } finally {
        println("Thread Pool shutdown")
        threadPool.shutdown()
      }
    }


    /**
     * A class that handles the work for the server. It takes in a connection
     * from the server and does some work based off of input to the socket.
     */
    class Worker(socket: Socket) extends Runnable {

      // generic socket set up. ( used from the last lab)
      val outputStream = socket.getOutputStream()
      val out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8")))
      lazy val in = new BufferedSource(socket.getInputStream()).getLines()

      var recv = "" // variable to store messages.

      /**
       * This is where the work of the worker is done, it checks the
       * message for either KILL_SERVICE or "HELO " as does tasks depending
       * on the input
       * - Replying with the desired string if HELO
       * - Or it kills the server if KILL_SERVICE
       */
      def run() {
        try {
          // if there is another message, get it.
          if (in.hasNext) {
            recv = in.next()
            println("Received: " + recv)
          }
          val prefix = recv take 5
          if (recv == "KILL_SERVICE") {
            println(Thread.currentThread.getName() + " is shutting down\n")
            out.println("KILL_SERVICE")
            out.flush() // tell the client the server shut down
            shutdownServer() // call the shut down method
            socket.close() // close the socket (ie the thread).
          }
          else if (prefix == "HELO ") {
            val messageWithoutHELO = recv + "\n"
            print(messageWithoutHELO)
            val ip = socket.getLocalAddress().toString().drop(1) + "\n"
            val port = serverSocket.getLocalPort + "\n"
            out.println(messageWithoutHELO + "IP:" + ip + "Port:" + port  + "StudentID:ac7ce4082772456e04ad6d80cceff8ddc274a78fd3dc1f28fd05aafdc4665e1b")
            out.flush()
          }
          else {
            out.println("Malformed request")
            out.flush()
          }
          out.close()

        } catch {
          case s: SocketException => println("Server Not Running")
        }
      }
    }

    /**
     * This function kills the server.
     */
    def shutdownServer(): Unit = {
      try{
        if(serverSocket != null) {
          serverSocket.close()
          threadPool.shutdownNow()
          println("Server shut down")
        }
      }catch{
        case e: SocketException => println("Server shut down")

      }
    }
  }

  def main(args: Array[String]){
    try{
      new Server(args(0).toInt).run()
    }catch{
      case outOfBounds : java.lang.ArrayIndexOutOfBoundsException =>
        println("Please provide command line arguments")
    }
  }
}



