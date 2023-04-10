package sn.isi.controller;

import java.io.IOException;
import java.util.ArrayList;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
@WebServlet(urlPatterns = "/Consumer")
public class ConsumerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String QUEUE_NAME = "test.queue";
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        ArrayList<String> messages = (ArrayList<String>) session.getAttribute("messages");
        if (messages == null) {
            messages = new ArrayList<String>();
            session.setAttribute("messages", messages);
        }

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        Connection connection = null;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            Session jmsSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = jmsSession.createQueue(QUEUE_NAME);
            MessageConsumer consumer = jmsSession.createConsumer(queue);

            while (true) {
                Message message = consumer.receive(1000);
                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    String text = textMessage.getText();
                    messages.add(text);
                } else {
                    break;
                }
            }
        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }

        session.setAttribute("messages", messages);
        response.sendRedirect("index.jsp");
    }

}
