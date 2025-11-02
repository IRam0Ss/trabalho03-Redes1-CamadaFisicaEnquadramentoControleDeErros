# 📡 E.D.E.N. v3 - Simulador de Rede com Controle de Erros e Fluxo

<p align="center">
  <img src="https://img.shields.io/badge/Linguagem-Java-blue?logo=java&logoColor=white" alt="Linguagem Principal: Java"/>
  <img src="https://img.shields.io/badge/Framework-JavaFX-orange?logo=oracle&logoColor=white" alt="Framework: JavaFX"/>
  <img src="https://img.shields.io/badge/Protocolo-Stop--and--Wait-blue" alt="Protocolo: Stop-and-Wait"/>
  <img src="https://img.shields.io/badge/Status-Concluído-brightgreen" alt="Status do Projeto: Concluído"/>
</p>

<p align="center">
  <em>Uma simulação de rede completa, implementando as camadas Física e de Enlace de Dados. Esta versão introduz algoritmos de <strong>Controle de Erros</strong> (Paridade, CRC, Hamming) e <strong>Controle de Fluxo</strong> (Stop-and-Wait com ACKs e Timeouts) em uma arquitetura de dois hosts.</em>
</p>

<p align="center">
  <img src="img/logoEDEN.png" width="300px" alt="Logo do Projeto E.D.E.N.">
</p>

---

## 📜 Visão Geral

O projeto **E.D.E.N. v3** é a evolução final do simulador de redes, construído sobre as fundações das camadas Física e de Enlace (Enquadramento). Esta versão implementa a funcionalidade crucial de **Controle de Erros**, permitindo que o receptor detecte e, em alguns casos, corrija falhas na transmissão.

Além disso, a arquitetura do projeto foi refatorada para simular uma rede real com dois `Host`s independentes. Para gerenciar a comunicação entre eles, foi implementado um protocolo de **Controle de Fluxo Stop-and-Wait**, que utiliza `ACKs` (confirmações) e `Timeouts` (temporizadores) para garantir uma entrega confiável dos quadros.

---

## ✨ Funcionalidades Principais

Este projeto simula o fluxo de dados completo de `Host A` para `Host B`, incluindo o retorno de `ACKs` de `Host B` para `Host A`.

### Novas Funcionalidades (Trabalho 03)

-   **🛡️ Controle de Erros (Detecção e Correção):** O transmissor adiciona bits de verificação ao quadro, e o receptor os utiliza para validar a integridade dos dados.
    -   **Bit de Paridade Par**
    -   **Bit de Paridade Ímpar**
    -   **CRC-32 (IEEE 802)**
    -   **Código de Hamming (com correção)**
-   **🌊 Controle de Fluxo (Stop-and-Wait):** Um protocolo de entrega confiável foi implementado:
    -   O `Host A` envia um quadro e inicia um `Timer`.
    -   O `Host B`, ao receber um quadro válido, envia um `ACK` de volta.
    -   Se o `ACK` for recebido antes do *timeout*, `Host A` envia o próximo quadro.
    -   Se o `ACK` não chegar (seja por perda do quadro ou do `ACK`), o *timeout* ocorre e o `Host A` **retransmite** o quadro anterior.
-   **💻 Arquitetura Host-to-Host:** O código foi refatorado para simular dois nós de rede (`HostA` e `HostB`), cada um contendo sua própria pilha de protocolos de transmissão e recepção.
-   **🐛 Simulação de Erros:** O usuário pode definir uma taxa de erro (de 0% a 100%) que o `MeioDeComunicacao` aplicará, corrompendo bits aleatoriamente para testar os algoritmos de controle.
-   **🎬 Animação Enfileirada:** A animação agora processa uma fila, exibindo os quadros (e retransmissões) em sequência, conforme são enviados.

### Funcionalidades Mantidas (Trabalhos Anteriores)

-   **Camada Física (Codificação de Linha):**
    -   Binário (NRZ)
    -   Manchester
    -   Manchester Diferencial
-   **Camada de Enlace (Enquadramento):**
    -   Contagem de Caracteres
    -   Inserção de Bytes (Byte Stuffing)
    -   Inserção de Bits (Bit Stuffing)
    -   Violação da Camada Física

---

## 🏗️ Arquitetura do Projeto

A simulação é gerenciada pelo `ControleRede`, que inicializa os dois `Host`s e o `MeioDeComunicacao`. Cada `Host` encapsula sua própria pilha de camadas.

### Fluxo de Dados (Host A -> Host B):

1.  **Aplicação (Host A):** O usuário digita a mensagem.
2.  **Camada de Aplicação (Host A):** Converte a `String` em bits (`int[]`).
3.  **Camada de Enlace (Host A):**
    -   Divide os bits em quadros (sub-quadros).
    -   Aplica o **Enquadramento** (ex: Inserção de Bits).
    -   Aplica o **Controle de Erro** (ex: CRC-32).
    -   Envia o quadro e inicia o `Timer` (Stop-and-Wait).
4.  **Camada Física (Host A):** Aplica a **Codificação de Linha** (ex: Manchester).
5.  **Meio de Comunicação:** Transmite o sinal de A para B, **aplicando erros** conforme a taxa definida.
6.  **Camada Física (Host B):** **Decodifica** o sinal.
7.  **Camada de Enlace (Host B):**
    -   Verifica o **Controle de Erro** (ex: recalcula o CRC).
    -   Se o quadro estiver corrompido, ele é **descartado** (e o `Host A` sofrerá *timeout*).
    -   Se estiver correto, aplica o **Desenquadramento** e envia um `ACK` de volta.
8.  **Camada de Aplicação (Host B):** Converte os bits em `String`.
9.  **Aplicação (Host B):** Exibe a `String` na tela.

### Fluxo de ACK (Host B -> Host A):

O fluxo de `ACK` é uma transmissão completa, mas com a mensagem "ACK". O `MeioDeComunicacao` roteia o `ACK` para o `Host A`, que o recebe em sua `CamadaEnlaceDadosTransmissora`, cancela o *timer* e envia o próximo quadro da fila.

<details>
<summary>Exemplo de Código - Lógica de Controle de Erro (Hamming TX)</summary>

```java
// Pacote: model/CamadaEnlaceDadosTransmissora.java
public int[] CamadaEnlaceDadosTransmissoraControleDeErroCodigoDeHamming(int quadro[]) {
    // ...
    // 1. Descobrir quantos bits de paridade 'r' são necessários
    int quantBitsParidade = 0;
    while ((1 << quantBitsParidade) < (totalBits + quantBitsParidade + 1)) {
        quantBitsParidade++;
    }
    // ...
    int totalBitsHammming = totalBits + quantBitsParidade;
    int[] quadroComHamming = new int[tamanhoQuadroFinal];

    // 2. Posicionar os bits de dados, pulando as posições que são potência de 2
    int indiceBit = 0;
    for (int posicao = 1; posicao <= totalBitsHammming; posicao++) {
        if (!((posicao & (posicao - 1)) == 0)) { // Se NAO for potencia de 2
            int bitDado = ManipulacaoBits.lerBits(quadro, indiceBit, 1);
            ManipulacaoBits.escreverBits(quadroComHamming, posicao - 1, bitDado, 1);
            indiceBit++;
        }
    }

    // 3. Calcular e inserir os bits de paridade nas posições corretas
    for (int i = 0; i < quantBitsParidade; i++) {
        int posBitParidade = 1 << i;
        int contadorUns = 0;
        
        // Verifica todos os bits que este bit de paridade cobre
        for (int bit = 1; bit <= totalBitsHammming; bit++) {
            if ((bit & posBitParidade) != 0) {
                if (bit != posBitParidade) { // Nao conta a si mesmo
                    if (ManipulacaoBits.lerBits(quadroComHamming, bit - 1, 1) == 1) {
                        contadorUns++;
                    }
                }
            }
        }

        // Garante a paridade PAR
        if ((contadorUns % 2) != 0) {
            ManipulacaoBits.escreverBits(quadroComHamming, posBitParidade - 1, 1, 1);
        }
    }
    return quadroComHamming;
}
```
</details>

---

## 🚀 Como Executar

Para executar este projeto, você precisará ter o **Java Development Kit (JDK)** com suporte a **JavaFX** (versão 11 ou superior) instalado.

1.  Clone o repositório.
2.  Navegue até o diretório do projeto.
3.  Compile e execute a classe principal `Principal.java`. Se estiver usando uma IDE como Eclipse ou IntelliJ, basta abri-lo como um projeto JavaFX e executar.

---

## 🧑‍💻 Autor

-   **Iury Ramos Sodré** - `https://github.com/IRam0Ss` `www.linkedin.com/in/iury-ramos-sodre-48a462309`
