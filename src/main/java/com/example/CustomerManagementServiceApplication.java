package com.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.stream.Stream;



@EnableBinding(Sink.class)
@EnableEurekaClient
@SpringBootApplication
public class CustomerManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerManagementServiceApplication.class, args);
    }
}

@RepositoryRestResource
interface CustomerRepository extends JpaRepository<Customer, Long> {

}

@MessageEndpoint
class CustomerProcessor {

    @ServiceActivator(inputChannel = "input")
    public void acceptNewCustomers(String cn) {
        this.customerRepository.save(new Customer(cn));
    }

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerProcessor(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
}

@Component
class DummyCLR implements CommandLineRunner {

    private final CustomerRepository customerRepository;

    @Autowired
    public DummyCLR(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public void run(String... strings) throws Exception {
        Stream.of("Jon", "Tufail", "Darren", "Kevin", "Shannon", "Clyde")
                .forEach(n -> customerRepository.save(new Customer(n)));

        customerRepository.findAll().forEach(System.out::println);

    }
}

@RestController
@RefreshScope
class MessageRestController {


    @Value("${message}")
    private String msg;

    @RequestMapping("/message")
    String read() {
        return this.msg;
    }
}

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
class Customer {
    @Id
    @GeneratedValue
    private Long id;
    private String customerName;

    public Customer() {
    }

    public Customer(String customerName) {
        this.customerName = customerName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", customerName='" + customerName + '\'' +
                '}';
    }
}

