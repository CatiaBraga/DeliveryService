# DeliveryService

O projecto escolhido para implementar foi Delivery Service. A implementação está contida em 4 projectos/repositórios:
- https://github.com/CatiaBraga/DeliveryService
- https://github.com/CatiaBraga/DeliveryServiceWS
- https://github.com/CatiaBraga/DeliveryServiceClient
- https://github.com/CatiaBraga/DataGenerator

Este repositório contém os DDL das tabelas necessárias, no ficheiro tables_DDL.sql. O RDBMS escolhido foi MySQL.

Todos os projectos foram implementados em Java, utilizando NetBeans 8.1 como IDE, Java 8 e Apache Tomcat 8.0.50.

O repositório DataGenerator contém os procedimentos de injecção de dados em base de dados, na classe Injector.java.

O repositório DeliveryService abrange toda a camada da lógica de negócio.

O repositório DeliveryServiceWS inclui a implementação de um webservice REST, e finalmente o repositório DeliveryServiceClient representa
um cliente desse webservice, com exemplos de pedidos.
