package com.bzi;

import com.rabbitmq.client.*;
import com.rabbitmq.client.Consumer;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

/**
 * @author ChunjieTang
 * @create 2018-02-02 17:31
 */
public class EmitLog {
    private static final String EXCHANGE_NAME = "RabbitChatRoom";
    private static Channel channel = null;
    private static Connection connection = null;
    private static String username = null;

    public EmitLog() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        EmitLog emitLog = new EmitLog();

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入昵称：");
        username = scanner.nextLine();
        ReceiveHandler receiveHandler = new ReceiveHandler();
        Thread t = new Thread(receiveHandler);
        t.start();
        while (true) {
            String message = scanner.nextLine();
            message = username + "：" + message;
            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());
        }
    }

    private static class ReceiveHandler implements Runnable {

        public void run() {
            try {
                /*channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

                String queueName = channel.queueDeclare().getQueue();*/
                //final String queueName = "tjie";
                channel.queueDeclare(username, false, false, false, null);
                channel.queueBind(username, EXCHANGE_NAME, "");

                System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

                Consumer consumer = new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        String msg = new String(body, "UTF-8");
                        System.out.println(" [x] Received :'" + msg + "'");
                    }
                };
                channel.basicConsume(username, true, consumer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
