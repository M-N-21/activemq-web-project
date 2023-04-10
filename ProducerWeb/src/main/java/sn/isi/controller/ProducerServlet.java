package sn.isi.controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

public class ProducerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String QUEUE_NAME = "test.queue";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String message = request.getParameter("message");
        if (message == null || message.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Message cannot be empty");
            return;
        }

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        Connection connection = null;
        try {
            connection = connectionFactory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(QUEUE_NAME);
            MessageProducer producer = session.createProducer(queue);

            TextMessage textMessage = session.createTextMessage(message);
            producer.send(textMessage);
            System.out.println("Sent message: " + textMessage.getText());

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

        response.sendRedirect("index.jsp");
    }
}
