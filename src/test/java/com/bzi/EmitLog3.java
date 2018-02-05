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
public class EmitLog3 {
    private static final String EXCHANGE_NAME = "logs";
    private static Channel channel = null;
    private static Connection connection = null;
    private static String username = null;

    public EmitLog3() {
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

    public static void main(String[] argv) throws Exception {
        EmitLog3 emitLog = new EmitLog3();
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入昵称：");
        username = scanner.nextLine();

        ReceiveHandler receiveHandler = new ReceiveHandler();
        Thread t = new Thread(receiveHandler);
        t.setDaemon(true);
        t.start();


        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

        while(true) {
            String message = scanner.nextLine();

            if (message.equalsIgnoreCase("exit")){
                t.interrupt();
                channel.close();
                connection.close();
                return;
            }

            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());
        }
    }

    private static class ReceiveHandler implements Runnable {

        public void run() {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost("localhost");
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();

                //channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
                //final String queueName = "jie";
                channel.queueDeclare(username, false, false, false, null);

                //final String queueName = channel.queueDeclare().getQueue();

                channel.queueBind(username, EXCHANGE_NAME, "");

                System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

                //QueueingConsumer consumer = new QueueingConsumer(channel);
                Consumer consumer = new DefaultConsumer(channel){
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        String msg = new String(body, "UTF-8");
                        System.out.println(" [x] Received :'" + msg + "'");
                    }
                };
                channel.basicConsume(username, true, consumer);

                /*while (true) {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                    String message = new String(delivery.getBody());

                    System.out.println(" [x] Received :" + queueName + "'" + message + "'");
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
