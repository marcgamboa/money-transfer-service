# 💵 Money Transfer Service

A spring boot application that handles money transfers between accounts with currency conversion and concurrent transaction support.

## 📃 Features

- Transfer money between accounts
- Support for multiple currencies (USD, JPY, AUD, CNY)
- Currency conversion with fixed rates
- Transaction fee handling (1%)
- Concurrent transaction support
- H2 in-memory database

## 📝Test Scenarios
1. Transfer `50 USD` (Alice to Bob)
2. Transfer `50 AUD` (Bob to Alice) recurring for `20 times`
3. Concurrently:
   - Transfer `20 AUD` Bob to Alice
   - Transfer `40 USD` Alice to Bob
   - Transfer `40 CNY` Alice to Bob

### 💡 Assumptions
**FX Rates**

| Currency | Rates  |
|----------|--------|
| AUD/USD  | 0.50   |
| CNY/USD  | 0.0069 |
| JPY/USD  | 0.14   |


## 🔧 Prerequisites
### ✅ Java 17 Installation and Setup

To run this project, you need Java 17 installed and configured on your system.
Windows

1. Download Java 17 JDK  
Go to https://jdk.java.net/archive/ and download the 17.0.2 (build 17.0.2+8) JDK for Windows.

2. Install the JDK  
Run the installer and install it in a known directory (e.g., C:\Java\jdk-17).

3. Set JAVA_HOME and update PATH  
    - Open Start Menu → Environment Variables (search "Edit the system environment variables").
    - Click Environment Variables…
    - Under System variables, click New:
      - Name: `JAVA_HOME`
      - Value: `C:\Java\jdk-17` (or your install path)
    - Find the variable named Path, click Edit, then Add:
`%JAVA_HOME%\bin`

4. Verify installation  
Open Command Prompt and type:
`java -version`  
You should see something like:
`java version "17.0.10" 2024-01-16 LTS`

## 📦 How to Use

1. Download the project as ZIP and extract it
2. Double-click `run-tests.bat`
3. It will run the tests to execute and open the test report in your browser
4. On the test report, navigate to this location 
`all > com.marcgamboa.money_transfer.service > MoneyTransferServiceTest` then select `Standard output` tab to 
view the results