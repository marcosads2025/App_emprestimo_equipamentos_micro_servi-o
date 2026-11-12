# Discovery Server (Eureka Server)

## Descrição
O **Discovery Server** é o componente central de descoberta de serviços baseado em Netflix Eureka. Ele permite que os microsserviços se registrem e descubram uns aos outros sem configuração estática.

## Funcionalidades
- **Registro de Serviços**: Microsserviços se registram automaticamente ao iniciar
- **Descoberta de Serviços**: Serviços podem consultar o Eureka para encontrar outras instâncias
- **Health Checks**: Monitora a saúde das instâncias registradas
- **Balanceamento de Carga**: Fornece informações para o LoadBalancer distribuir requisições

## Tecnologias
- **Spring Boot 3.5.6**
- **Spring Cloud Netflix Eureka Server**
- **Java 17**

## Como Funciona
1. Cada microsserviço (apigateway, msemprestimo, msequipamento, msusuario) se configura como cliente Eureka
2. Ao iniciar, cada serviço se registra no Discovery Server
3. O Discovery Server mantém um registro atualizado de todas as instâncias disponíveis
4. Serviços podem consultar o Eureka para descobrir onde outros serviços estão rodando
5. O Eureka realiza health checks periódicos para remover instâncias não saudáveis

## Arquitetura
O Discovery Server é o primeiro serviço a ser iniciado na arquitetura de microsserviços, pois todos os outros serviços dependem dele para se registrar e descobrir outros serviços.

## Configuração
Configurado com `@EnableEurekaServer` para ativar o servidor de descoberta. Os serviços clientes usam `@EnableDiscoveryClient` para se registrar.

## Porta
O serviço roda na porta configurada em `application.yml` (geralmente 8761, porta padrão do Eureka).
