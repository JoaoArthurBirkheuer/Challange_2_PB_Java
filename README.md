![Imagem do WhatsApp de 2025-04-15 à(s) 10 06 28_9b5fefef](https://github.com/user-attachments/assets/80db2d01-42b3-4b43-98aa-cc516c7d5baa)

# Sistema Bancário - Desafio Compass

## Visão Geral
Sistema bancário completo com:
- Gestão de contas (corrente, salário, poupança, investimento)
- Transações financeiras (depósito, saque, transferência)
- Controle de acesso para clientes e gerentes
- Sistema abrangente de auditoria e logs

## Funcionalidades

### Menu Principal
1. Login (auto-detecta cliente/gerente)
2. Criação de conta de Cliente
3. Audit Log System

### Menu do Gerente
1. Registrar Novo Gerente
2. Desbloquear Cliente
3. Revisar Solicitações de Estorno
4. Solicitações de Inativação de Conta
5. Reativação de Conta

### Menu do Cliente
- Lista contas ativas com:
  - Número da conta
  - Saldo atual
  - Status (incluindo solicitações de encerramento)
- Opção para criar nova conta

### Menu da Conta
1. Verificar Saldo
2. Realizar Depósito
3. Realizar Saque
4. Transferir Fundos
5. Solicitar Estorno de Transação
6. Solicitar Encerramento de Conta
7. Visualizar Extrato da Conta (Opcional: exportação de .csv)

### Menu de Logs
1. Visualizar Todos os Logs
2. Atividades de Login/Logout
3. Operações de Conta
4. Transações Financeiras
5. Logs de Cadastro de Clientes
6. Logs de Gerenciamento de Contas
7. Logs de Extratos
8. Filtro Personalizado
0. Voltar ao Menu Principal

## Tecnologias Utilizadas
- Java 22
- Hibernate 6.4.6.Final
- MySQL 8.0.33
- JUnit 4.13.2 (para testes)

## Configuração do Banco de Dados
O sistema está configurado para conectar a um banco MySQL local.

```xml
<persistence-unit name="PersistenceUnit">
  <properties>
    <property name="jakarta.persistence.jdbc.url" 
              value="jdbc:mysql://localhost:3306/bankchallengedb"/>
    <property name="hibernate.dialect" 
              value="org.hibernate.dialect.MySQL8Dialect"/>
    <property name="jakarta.persistence.schema-generation.database.action" 
              value="update"/>
  </properties>
</persistence-unit>
