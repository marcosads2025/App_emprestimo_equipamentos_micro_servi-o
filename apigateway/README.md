# API Gateway

## Descrição
O **API Gateway** é o ponto de entrada único para todos os clientes do sistema de empréstimo de equipamentos. Ele atua como um proxy reverso, roteando as requisições para os microsserviços apropriados.

## Funcionalidades
- **Roteamento de Requisições**: Direciona as requisições para os microsserviços corretos (msusuario, msequipamento, msemprestimo)
- **Balanceamento de Carga**: Distribui o tráfego entre múltiplas instâncias dos serviços usando Spring Cloud LoadBalancer
- **Descoberta de Serviços**: Integra-se com o Eureka Server para descobrir dinamicamente as instâncias dos serviços
- **Gateway Reactivo**: Implementado com Spring WebFlux para alta performance e baixa latência

## Tecnologias
- **Spring Boot 3.5.6**
- **Spring Cloud Gateway**
- **Spring Cloud LoadBalancer**
- **Netflix Eureka Client**
- **Java 17**

## Como Funciona
1. O cliente faz uma requisição para o API Gateway
2. O Gateway consulta o Eureka Server para descobrir a localização dos serviços
3. A requisição é roteada para o serviço apropriado
4. O LoadBalancer distribui as requisições entre instâncias disponíveis

## Configuração
O gateway é configurado para se registrar no Eureka Server e rotear requisições baseadas em padrões de URL para cada microsserviço.

## Porta
O serviço roda na porta configurada em `application.yml` (geralmente 8080 ou outra porta especificada).
