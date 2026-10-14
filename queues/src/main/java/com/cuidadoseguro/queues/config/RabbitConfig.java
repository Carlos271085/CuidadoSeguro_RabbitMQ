package com.cuidadoseguro.queues.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.client.RestClient;
@Configuration
public class RabbitConfig {

    public static final String LOGS_EXCHANGE = "logs_direct_exchange";
    public static final String ALL_LOGS_QUEUE = "all_logs_queue";
    public static final String ERRORS_ONLY_QUEUE = "errors_only_queue";

    public static final String FO_EXCHANGE = "fanout_exchange";
    public static final String COLA_CORREO = "cola_correo";
    public static final String COLA_PDF_FO = "cola_pdf_fo";

    public static final String DI_EXCHANGE = "direct_exchange";
    public static final String COLA_PDF_DI = "cola_pdf_di";
    public static final String PDF_DI = "pdf";

    public static final String ORDERS_EXCHANGE = "orders.exchange";
    public static final String ORDERS_QUEUE = "orders.queue";
    public static final String ORDERS_ESPERA_QUEUE = "orders.espera";
    public static final String ORDERS_DLX = "orders.dlx";
    public static final String ORDERS_DLQ = "orders.dlq";

    public static final String ORDER_CREATED = "order.created";
    public static final String ORDER_ESPERA = "order.espera";

    public static final String LOG_INFO = "INFO";
    public static final String LOG_WARNING = "WARNING";
    public static final String LOG_ERROR = "ERROR";

    public static final String COLA_CREADOS = "cola_creados";

    public static final String DLX_EXCHANGE = "cuidadoseguro.dlx";

    public static final String COLA_CORREO_DLQ = "cola_correo.dlq";
    public static final String COLA_PDF_DI_DLQ = "cola_pdf_di.dlq";
    public static final String COLA_PDF_FO_DLQ = "cola_pdf_fo.dlq";
    
    public static final String DLQ_CORREO_KEY = "dlq.correo";
    public static final String DLQ_PDF_DI_KEY = "dlq.pdf.direct";
    public static final String DLQ_PDF_FO_KEY = "dlq.pdf.fanout";

    public static final String EVENTOS_EXCHANGE = "eventos_exchange";
    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }


    @Bean
    MessageConverter jsonConverter() {
        DefaultClassMapper classMapper = new DefaultClassMapper();

        classMapper.setTrustedPackages(
            "com.cuidadoseguro.queues.dto",
            "com.cuidadoseguro.queues.message"
        );

        Jackson2JsonMessageConverter converter =
                new Jackson2JsonMessageConverter();

        converter.setClassMapper(classMapper);

        return converter;
    }


    @Bean
    SimpleRabbitListenerContainerFactory manualFactory(ConnectionFactory connectionFactory,
                                                       MessageConverter jsonConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(1);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    @Bean
    DirectExchange logsDirectExchange() {
        return new DirectExchange(LOGS_EXCHANGE);
    }

    @Bean
    Queue allLogsQueue() {
        return new Queue(ALL_LOGS_QUEUE, true);
    }

    @Bean
    Queue errorsOnlyQueue() {
        return new Queue(ERRORS_ONLY_QUEUE, true);
    }

    @Bean
    Binding allLogsInfo(Queue allLogsQueue, DirectExchange logsDirectExchange) {
        return BindingBuilder.bind(allLogsQueue).to(logsDirectExchange).with("INFO");
    }

    @Bean
    Binding allLogsWarning(Queue allLogsQueue, DirectExchange logsDirectExchange) {
        return BindingBuilder.bind(allLogsQueue).to(logsDirectExchange).with("WARNING");
    }

    @Bean
    Binding allLogsError(Queue allLogsQueue, DirectExchange logsDirectExchange) {
        return BindingBuilder.bind(allLogsQueue).to(logsDirectExchange).with("ERROR");
    }

    @Bean
    Binding errorsOnly(Queue errorsOnlyQueue, DirectExchange logsDirectExchange) {
        return BindingBuilder.bind(errorsOnlyQueue).to(logsDirectExchange).with("ERROR");
    }

    @Bean
    FanoutExchange fanout() {
        return new FanoutExchange(FO_EXCHANGE);
    }

    @Bean
    DirectExchange direct() {
        return new DirectExchange(DI_EXCHANGE);
    }

    @Bean
    Queue colaCreados() {
        Map<String, Object> args = new HashMap<>();

        args.put("x-dead-letter-exchange", DLX_EXCHANGE);

        return new Queue(
            COLA_CREADOS, true, false, false, args
        );
    }

    @Bean
    Queue colaCorreo() {
        Map<String, Object> args = new HashMap<>();

        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", DLQ_CORREO_KEY);
        

        return new Queue(
            COLA_CORREO, true, false, false, args
        );
    }

    @Bean
    Queue colaPdfFanout() {
        Map<String, Object> args = new HashMap<>();

        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", DLQ_PDF_FO_KEY);
        
        return new Queue(
            COLA_PDF_FO, true, false, false, args
        );
    }

    @Bean
    Queue colaPdfDirect() {
        Map<String, Object> args = new HashMap<>();

        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", DLQ_PDF_DI_KEY);

        return new Queue(
            COLA_PDF_DI, true, false, false, args
        );
    }

    @Bean
    Binding correoBinding(@Qualifier("colaCorreo") Queue colaCorreo, @Qualifier("fanout") FanoutExchange fanout) {
        return BindingBuilder.bind(colaCorreo).to(fanout);
    }

    @Bean
    Binding pdfFanoutBinding(
            @Qualifier("colaPdfFanout") Queue colaPdfFo,
            @Qualifier("fanout") FanoutExchange fanoutExchange) {

        return BindingBuilder
                .bind(colaPdfFo)
                .to(fanoutExchange);
    }

    @Bean
    Binding pdfDirectBinding(
            @Qualifier("colaPdfDirect") Queue colaPdfDi,
            @Qualifier("direct") DirectExchange direct) {

        return BindingBuilder
                .bind(colaPdfDi)
                .to(direct)
                .with(PDF_DI);
    }
////////////////////////////////////////////

    @Bean
    Binding creadosBinding(
            @Qualifier("colaCreados") Queue colaCreados,
            @Qualifier("eventosExchange") DirectExchange eventosExchange) {

        return BindingBuilder
                .bind(colaCreados)
                .to(eventosExchange)
                .with("creado");
    }


    @Bean
    DirectExchange ordersExchange() {
        return new DirectExchange(ORDERS_EXCHANGE);
    }

    @Bean
    FanoutExchange ordersDlx() {
        return new FanoutExchange(ORDERS_DLX);
    }

    @Bean
    Queue ordersQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", ORDERS_DLX);
        args.put("x-message-ttl", 30_000);
        return new Queue(ORDERS_QUEUE, true, false, false, args);
    }

    @Bean
    Queue ordersEspera() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", ORDERS_DLX);
        args.put("x-message-ttl", 8_000);
        return new Queue(ORDERS_ESPERA_QUEUE, true, false, false, args);
    }

    @Bean
    Queue ordersDlq() {
        return new Queue(ORDERS_DLQ, true);
    }

    @Bean
    Binding orderCreated(Queue ordersQueue, DirectExchange ordersExchange) {
        return BindingBuilder.bind(ordersQueue).to(ordersExchange).with(ORDER_CREATED);
    }

    @Bean
    Binding orderEspera(Queue ordersEspera, DirectExchange ordersExchange) {
        return BindingBuilder.bind(ordersEspera).to(ordersExchange).with(ORDER_ESPERA);
    }

    @Bean
    Binding dlqBinding(Queue ordersDlq, FanoutExchange ordersDlx) {
        return BindingBuilder.bind(ordersDlq).to(ordersDlx);
    }

        
    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    @Bean
    Queue correoDlq() {
        return new Queue(COLA_CORREO_DLQ, true);
    }

    @Bean
    Queue pdfDirectDlq() {
        return new Queue(COLA_PDF_DI_DLQ, true);
    }

    @Bean
    Queue pdfFanoutDlq() {
        return new Queue(COLA_PDF_FO_DLQ, true);
    }

    @Bean
    Binding correoDlqBinding(
            Queue correoDlq,
            DirectExchange deadLetterExchange) {

        return BindingBuilder.bind(correoDlq)
                .to(deadLetterExchange)
                .with(DLQ_CORREO_KEY);
    }

    @Bean
    Binding pdfDirectDlqBinding(
            Queue pdfDirectDlq,
            DirectExchange deadLetterExchange) {

        return BindingBuilder.bind(pdfDirectDlq)
                .to(deadLetterExchange)
                .with(DLQ_PDF_DI_KEY);
    }

    @Bean
    Binding pdfFanoutDlqBinding(
            Queue pdfFanoutDlq,
            DirectExchange deadLetterExchange) {

        return BindingBuilder.bind(pdfFanoutDlq)
                .to(deadLetterExchange)
                .with(DLQ_PDF_FO_KEY);
    }

    @Bean
    DirectExchange eventosExchange() {
        return new DirectExchange(EVENTOS_EXCHANGE);
    }


}
