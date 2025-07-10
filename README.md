# 💳 SistemaBancoDigitalCDB

## 🏦 Descrição

Projeto de sistema bancário desenvolvido com **Spring Boot** e **JPA**, simulando as funcionalidades principais de um banco digital, como:

- Cadastro e gestão de clientes  
- Criação de contas (corrente e poupança)  
- Geração e administração de cartões (crédito e débito)  
- Operações: depósitos, saques, transferências, Pix e pagamento de fatura  

O sistema implementa regras de negócio realistas com foco em **boas práticas**, **segurança**, **validações robustas** e **organização modular**.

---

## 🚀 Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.x**
- **Spring Data JPA**
- **Hibernate**
- **H2 Database**
- **Lombok**
- **Jakarta Validation API** 
- **BCryptPasswordEncoder** (criptografia de senhas dos cartões)
- **Maven**
- **Git/GitHub**
- **ViaCEP API** (busca automática de endereço via CEP)

---

## 🗂️ Estrutura de Pastas

```
br.com.cdb.bancodigitaljpa
├── config         # Configurações globais, como criptografia de senha
├── controller     # Endpoints REST (cliente, conta, cartão)
├── dto            # Objetos de entrada e saída da API
├── entity         # Entidades JPA mapeadas
├── enums          # Enumerações como TipoCliente
├── repository     # Interfaces para persistência
├── service        # Lógica de negócio
├── utils          # Utilitários (ex: validador de CPF)
└── BancodigitalJpaApplication.java
```

---

## 🔐 Segurança

- A senha dos cartões é **criptografada com BCrypt**.
- A criptografia está configurada na classe `SecurityConfig` dentro do pacote `config`.

---

## 📌 Regras de Negócio

### 👤 Cadastro de Clientes
- CPF único e válido
- Nome, data de nascimento (≥ 18 anos) e endereço completo
- Classificação: **Comum**, **Super** ou **Premium**

### 🏦 Contas Bancárias
- **Corrente**: cobra taxa de manutenção mensal
- **Poupança**: aplica rendimento mensal com base na categoria do cliente

### 💳 Cartões
- Crédito e débito vinculados à conta
- Operações: pagamento, troca de senha, ativação/desativação, limite ajustável

#### Cartão de Crédito
- Limite pré-definido por tipo de cliente:
  - Comum: R$ 1.000
  - Super: R$ 5.000
  - Premium: R$ 10.000
- Aplicação de taxa de 5% se o uso ultrapassar 80% do limite
- Bloqueio automático se atingir o limite total

#### Cartão de Débito
- Limite diário configurável
- Impede transações após exceder o limite diário

---

## 📥 Rotas da API

### 👥 Cliente
| Método | Rota               | Ação                     |
|--------|--------------------|--------------------------|
| POST   | `/cliente/add`     | Criar novo cliente       |
| GET    | `/cliente/{id}`    | Buscar cliente por ID    |
| GET    | `/cliente/listAll` | Listar todos os clientes |
| PUT    | `/cliente/{id}`    | Atualizar cliente        |
| DELETE | `/cliente/{id}`    | Remover cliente          |

### 🧾 Conta
| Método | Rota                          | Ação                           |
|--------|-------------------------------|--------------------------------|
| POST   | `/contas/{clienteId}`          | Criar conta (corrente/poupança)|
| GET    | `/contas/{id}`                 | Buscar conta por ID            |
| GET    | `/contas/listAll`              | Listar todas as contas         |
| POST   | `/contas/{id}/depositar`       | Realizar depósito              |
| POST   | `/contas/{id}/sacar`           | Realizar saque                 |
| POST   | `/contas/{id}/transferir`      | Transferência entre contas     |
| POST   | `/contas/{id}/pix`             | Pagamento via Pix              |
| PUT    | `/contas/{id}/manutencao`      | Aplicar taxa de manutenção     |
| PUT    | `/contas/{id}/rendimento`      | Aplicar rendimento da poupança |
| GET    | `/contas/{id}/saldo`           | Consultar saldo da conta       |

### 💳 Cartão
| Método | Rota                             | Ação                           |
|--------|----------------------------------|--------------------------------|
| POST   | `/cartao/{id}`                   | Criar cartão                   |
| GET    | `/cartao/{id}`                   | Buscar cartão por ID           |
| GET    | `/cartao/all`                    | Listar todos os cartões        |
| POST   | `/cartao/{id}/pagamento`         | Pagar com cartão               |
| POST   | `/cartao/{id}/fatura/pagamento`  | Pagar fatura do cartão         |
| GET    | `/cartao/{id}/fatura`            | Ver fatura do cartão           |
| PUT    | `/cartao/{id}/limite`            | Atualizar limite de crédito    |
| PUT    | `/cartao/{id}/limiteDiario`      | Atualizar limite diário (débito)|
| PUT    | `/cartao/{id}/status`            | Ativar/desativar cartão        |
| PUT    | `/cartao/{id}/senha`             | Atualizar senha                |

---

## ✅ Validações

### Cliente:
- **CPF:** único, formato válido e autenticado via algoritmo
- **Nome:** apenas letras e espaços (mín. 2, máx. 100 caracteres)
- **Data de Nascimento:** cliente deve ser maior de 18 anos
- **Endereço:** formatado corretamente com CEP validado via API externa

---

## 💸 Taxas e Rendimento

### Conta Corrente:
| Tipo de Cliente | Taxa de Manutenção |
|-----------------|--------------------|
| Comum           | R$ 12,00/mês       |
| Super           | R$ 8,00/mês        |
| Premium         | Isenta             |

### Conta Poupança:
| Tipo de Cliente | Rendimento Anual (%) |
|-----------------|----------------------|
| Comum           | 0.5%                 |
| Super           | 0.7%                 |
| Premium         | 0.9%                 |

---

## 🌐 Integrações Externas

- **ViaCEP API**  
  Utilizada para buscar automaticamente o endereço completo a partir do CEP informado no cadastro de cliente.

---

## ▶️ Como Executar o Projeto

1. Clone o repositório:
```bash
git clone https://github.com/taritalima/SistemaBancoDigitalCDB.git
cd SistemaBancoDigitalCDB
```

2. Compile e execute o projeto:
```bash
mvn clean install
mvn spring-boot:run
```

3. Acesse a aplicação via Postman ou Insomnia.

---

