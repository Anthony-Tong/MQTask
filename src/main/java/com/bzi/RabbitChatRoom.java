package com.bzi;

import com.rabbitmq.client.*;
import com.rabbitmq.client.Consumer;

import java.io.IOException;
import java.util.Scanner;

/**
 * @author ChunjieTang
 * @create 2018-02-02 17:31
 */
public class RabbitChatRoom {
    private static final String EXCHANGE_NAME = "RabbitChatRoom";
    private Channel channel;
    private Connection connection;

    public RabbitChatRoom() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("guest");
        factory.setPassword("guest");
        //创建连接
        connection = factory.newConnection();
        channel = connection.createChannel();

    }

    public static void main(String[] args) {
        try {
            RabbitChatRoom chatRoom = new RabbitChatRoom();
            chatRoom.send();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("运行失败");
        }

    }

    /**
     * 发送方法
     */
    public void send() {
        System.out.println("欢迎来到RabbitMQ聊天室.^_^.");
        System.out.println("输入exit退出聊天室。");

        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入昵称：");
        String nickname = scanner.nextLine();
        System.out.println("hello " + nickname + ",欢迎加入聊天。");

        //开启接收线程
        ReceiveHandler receiveHandler = new ReceiveHandler();
        Thread t = new Thread(receiveHandler);
        //设置守护线程
        t.setDaemon(true);
        t.start();

        try {
            //声明交换器
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

            while (true) {
                //用户输入的内容
                String body = scanner.nextLine();
                String message = nickname + "--> " + body;
                //如果输入“exit”关闭程序
                if ("exit".equalsIgnoreCase(body)) {
                    t.interrupt();
                    channel.close();
                    connection.close();
                    return;
                }
                //发送消息
                channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("发送失败");
        }
    }

    /**
     * 接收端线程
     */
    private class ReceiveHandler implements Runnable {

        public void run() {
            try {
                //声明交换器
                channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

                String queueName = channel.queueDeclare().getQueue();
                //绑定交换器
                channel.queueBind(queueName, EXCHANGE_NAME, "");

                //获取消息
                Consumer consumer = new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        String msg = new String(body, "UTF-8");
                        System.out.println(msg);
                    }
                };
                //监听队列
                channel.basicConsume(queueName, true, consumer);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("接收消息失败");
            }
        }
    }
}
