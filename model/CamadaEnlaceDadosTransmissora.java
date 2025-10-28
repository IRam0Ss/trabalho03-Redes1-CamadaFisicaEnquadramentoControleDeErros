package model;

import controller.ControlerTelaPrincipal;
import util.ManipulacaoBits;

/**
 * classe responsavel por separar o quadro em subquadros aplicando os algoritmos
 * responsaveis pelo enquadramento a escolha do usuario
 */
public class CamadaEnlaceDadosTransmissora {

  private CamadaFisicaTransmissora camadaFisicaTransmissora;
  private ControlerTelaPrincipal controlerTelaPrincipal;

  /**
   * construtor da classe
   * 
   * @param camadaFisicaTransmissora camada imediatamente abaixo
   * @param controlerTelaPrincipal   contorle da interface
   */
  public CamadaEnlaceDadosTransmissora(CamadaFisicaTransmissora camadaFisicaTransmissora,
      ControlerTelaPrincipal controlerTelaPrincipal) {
    this.camadaFisicaTransmissora = camadaFisicaTransmissora;
    this.controlerTelaPrincipal = controlerTelaPrincipal;
  } // fim contrutor

  public void transmitirQuadro(int[] quadro) {

    // debug
    System.out.println("Camada de Enlace TX: Recebi " + quadro.length + " inteiros para transmitir.");

    // trata cada int ou seja cada 32bits de carga util como sendo um subquadro
    for (int i = 0; i < quadro.length; i++) {

      // verifica se o 'int' contem dados validos antes de processar
      int totalBitsNoInt = ManipulacaoBits.descobrirTotalDeBitsReais(new int[] { quadro[i] });
      if (totalBitsNoInt == 0) {
        continue; // pular 'ints' de padding (vazios)
      } // fim if

      int[] subQuadro = new int[] { quadro[i] }; // o subquadro eh de 1 int ou seja, ate 32 bits de carga util

      // debug
      System.out.println("Enlace TX: Processando sub-quadro " + i);

      // aplica enquadramento no subquadro.
      int[] quadroEnquadrado = CamadaEnlaceDadosTransmissoraEnquadramento(subQuadro);

      // aplica controle de erro
      int[] quadroComControleDeErro = CamadaEnlaceDadosTransmissoraControleDeErro(quadroEnquadrado);

      // 3. APLICA CONTROLE DE FLUXO (Stub de Teste)
      // Este metodo, por agora, apenas envia para a proxima camada.
      // Ele NAO espera pelo ACK, permitindo testar o fluxo.
      CamadaEnlaceDadosTransmissoraControleDeFluxo(quadroComControleDeErro);

    } // fim for

    System.out.println("Camada de Enlace TX: Todos os sub-quadros foram disparados.");

  }// fim e transmitirQuadro

  /**
   * metodo que escolhe o tipo de enquadramento a ser aplicado na mensagem
   * 
   * @param quadro mensagem na forma binaria, o array de int, ja com os bits
   *               armazenados recebido da camada anterior
   * @return o quadro ja enquadrado
   */
  public int[] CamadaEnlaceDadosTransmissoraEnquadramento(int quadro[]) {

    int tipoDeEnquadramento = this.controlerTelaPrincipal.opcaoEnquadramentoSelecionada();
    int quadroEnquadrado[] = null;
    switch (tipoDeEnquadramento) {
      case 0: // contagem de caracteres
        quadroEnquadrado = CamadaEnlaceDadosTransmissoraEnquadramentoContagemDeCaracteres(quadro);
        break;
      case 1: // insercao de bytes
        quadroEnquadrado = CamadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBytes(quadro);
        break;
      case 2: // insercao de bits
        quadroEnquadrado = CamadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBits(quadro);
        break;
      case 3: // violacao da camada fisica
        quadroEnquadrado = CamadaEnlaceDadosTransmissoraEnquadramentoViolacaoDaCamadaFisica(quadro);
        break;
    }// fim do switch/case

    // fazer aqui a mensagem unica ja enquadrada em um unico vetor(o
    // quadroEnquadrado), se tornar um vetor de vetores, onde cada vetor corresponde
    // a um quadro enquadrado
    // int[][] quadrosEnquadrados = separarEmQuadros(quadroEnquadrado);

    return quadroEnquadrado; // retorna o quadro ja enquadrado

  }// fim do metodo CamadaEnlaceDadosTransmissoraEnquadramentos

  /**
   * metodo que escolhe o tipo de controle de erro a ser aplicado na mensagem
   * 
   * @param quadro mensagem ja com o enquadramento
   * @return mensagem com o controle de erro aplicado
   */
  public int[] CamadaEnlaceDadosTransmissoraControleDeErro(int quadro[]) {

    int tipoDeControleDeErro = this.controlerTelaPrincipal.opcaoControleErroSelecionada();
    int quadroComControleDeErro[] = null;
    switch (tipoDeControleDeErro) {
      case 0: // paridade par
        quadroComControleDeErro = CamadaEnlaceDadosTransmissoraControleDeErroBitParidadePar(quadro);
        break;
      case 1: // paridade impar
        quadroComControleDeErro = CamadaEnlaceDadosTransmissoraControleDeErroBitParidadeImpar(quadro);
        break;
      case 2: // CRC
        quadroComControleDeErro = CamadaEnlaceDadosTransmissoraControleDeErroCRC(quadro);
        break;
      case 3: // Hamming
        quadroComControleDeErro = CamadaEnlaceDadosTransmissoraControleDeErroCodigoDeHamming(quadro);
        break;
    }// fim do switch/case

    return quadroComControleDeErro; // retorna o quadro ja com controle de erro aplicado

  }// fim do metodo CamadaEnlaceDadosTransmissoraControleDeErro

  public void CamadaEnlaceDadosTransmissoraControleDeFluxo(int quadro[]) {
    System.out.println("Enlace TX (Controle de Fluxo): Enviando quadro para a Camada Fisica.");
    this.camadaFisicaTransmissora.transmitirQuadro(quadro);
  }// fim do metodo CamadaEnlaceDadosTransmissoraControleDeFluxo

  /**
   * metodo que realiza o enquadramento por contagem de caracteres
   * 
   * @param quadro quadro original a ser enquadrado
   * @return o quadro ja enquadrado
   */
  public int[] CamadaEnlaceDadosTransmissoraEnquadramentoContagemDeCaracteres(int quadro[]) {
    // descobre o tamanho real dos dados, ignorando o lixo.
    int totalDeBitsReais = ManipulacaoBits.descobrirTotalDeBitsReais(quadro);
    if (totalDeBitsReais == 0)
      return new int[0];

    final int TAMANHO_MAX_CARGA_UTIL_EM_BITS = 32; // A carga util sera de ATE 4 bytes
    final int TAMANHO_CABECALHO_EM_BITS = 8;

    // Para calcular o tamanho final, precisamos simular a criacao dos quadros
    int tamanhoTotalEstimadoEmBits = 0;
    for (int i = 0; i < totalDeBitsReais; i += TAMANHO_MAX_CARGA_UTIL_EM_BITS) {
      int bitsNesteFrame = Math.min(TAMANHO_MAX_CARGA_UTIL_EM_BITS, totalDeBitsReais - i);
      tamanhoTotalEstimadoEmBits += TAMANHO_CABECALHO_EM_BITS + bitsNesteFrame;
    }

    int[] quadroEnquadrado = new int[(tamanhoTotalEstimadoEmBits + 31) / 32];
    int bitEscritaGlobal = 0;
    int bitLeituraGlobal = 0;

    while (bitLeituraGlobal < totalDeBitsReais) {
      // 1. Calcula o tamanho da carga util para ESTE frame
      int bitsParaLer = Math.min(TAMANHO_MAX_CARGA_UTIL_EM_BITS, totalDeBitsReais - bitLeituraGlobal);
      int cargaUtil = ManipulacaoBits.lerBits(quadro, bitLeituraGlobal, bitsParaLer);
      bitLeituraGlobal += bitsParaLer;

      // 2. Calcula o valor do cabecalho para ESTE frame
      // O valor eh o numero de bytes da carga util + 1 (o proprio cabecalho)
      int valorDoCabecalho = (bitsParaLer / 8) + 1;

      // 3. Escreve o cabecalho (8 bits)
      ManipulacaoBits.escreverBits(quadroEnquadrado, bitEscritaGlobal, valorDoCabecalho, TAMANHO_CABECALHO_EM_BITS);
      bitEscritaGlobal += TAMANHO_CABECALHO_EM_BITS;

      // 4. Escreve a carga util (APENAS os bits lidos)
      ManipulacaoBits.escreverBits(quadroEnquadrado, bitEscritaGlobal, cargaUtil, bitsParaLer);
      bitEscritaGlobal += bitsParaLer;
    }

    return quadroEnquadrado;
  } // fim de CamadaEnlaceDadosTransmissoraContagemCaractere

  /**
   * metodo para realizar o enquadramento por insercao de bytes
   * 
   * @param quadro quadro original a ser enquadrado
   * @return o quadro ja enquadrado
   */
  public int[] CamadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBytes(int quadro[]) {

    final int FLAG = 0b01111110; // valor do byte de flag, equivale a 126 em decimal, em ASCII eh o '~'
    final int SCAPE = 0b01111101; // valor do byte de escape, equivale a 125 em decimal, em ASCII eh o '}'
    final int TAMANHO_SUBQUADRO_EM_BYTES = 4; // define que a cada quntos bytes sera adicionado uma flag

    int contadorBytesCargaUtilQuadro = 0; // contador de quantos bytes de carga util tem na mensagem
    int contadorBytesEnquadrados = 0; // contador para enquadrar os bits da mensagem

    boolean fimDados = false; // controle se a carga util acabou
    for (int inteiroAgrupado : quadro) {
      if (fimDados)
        break; // se acabou a carga util sai do loop

      for (int i = 0; i < 4; i++) { // percorre os 4 bytes do inteiro e contabiliza os validos
        int umByte = (inteiroAgrupado >> (24 - i * 8)) & 0xFF; // pega 1 byte do inteiro, comecando do mais a direita
                                                               // ate o mais a esquerda
        if (umByte == 0) { // se um byte for 00000000 significa que a acabou as cargas uteis daquele
                           // inteiro, possivelmente da mensagem
          fimDados = true;
          break;
        } // fim do if
        contadorBytesCargaUtilQuadro++; // se chegou ate aqui, significa que o byte eh uma carga util.
      } // fim do for
    } // fim do for

    if (contadorBytesCargaUtilQuadro > 0) { // tem ao menos 1 carga util na mensagem
      contadorBytesEnquadrados = 1; // comeca com a flag inicial
      int contadorFlagIntermediaria = 0;

      for (int i = 0; i < contadorBytesCargaUtilQuadro; i++) { // percorre todas as cargas uteis do quadro
                                                               // contabilizando quantas flags serao necessarias
        // extrair o byte novamente
        int indiceInteiro = i / 4;
        int posicaoNoInteiro = i % 4;

        int umByte = (quadro[indiceInteiro] >> (24 - posicaoNoInteiro * 8)) & 0xFF;

        if (umByte == FLAG || umByte == SCAPE) {
          contadorBytesEnquadrados += 2; // Adiciona SCAPE + byte
        } else {
          contadorBytesEnquadrados += 1; // Adiciona o byte
        }

        contadorFlagIntermediaria++; // conta quantos bytes ja foram lidos

        if (contadorFlagIntermediaria == TAMANHO_SUBQUADRO_EM_BYTES) { // quando chega no byte que tem que adicionar a
                                                                       // flag ele adiciona e 0 o contador
          contadorBytesEnquadrados++; // Adiciona a FLAG intermediária
          contadorFlagIntermediaria = 0;
        } // fim do if

        // Se a mensagem não terminou exatamente em um bloco de 4, adiciona a FLAG
        // final.
        if (contadorFlagIntermediaria != 0) {
          contadorBytesEnquadrados++;
        } // fim do if

      } // fim for

    } // fim if

    // Se não havia dados, o quadro terá 0 bytes.
    if (contadorBytesEnquadrados == 0) {
      System.out.println("MENSAGEM VAZIA");
      return new int[0];
    } // fim do if

    // agora sim cria o array final com os tamanhos corretos e preenche com as FLAGS
    // e as Cargas uteis

    int tamanhoArrayFinal = (contadorBytesEnquadrados + 3) / 4; // usa o tamanho encontrado arredondando para cima
    int[] quadroEnquadrado = new int[tamanhoArrayFinal];

    int indiceBitDestino = 0; // controla a posicao inicial do proximoBit a ser escrito

    // escreve a FLAG inicial
    ManipulacaoBits.escreverBits(quadroEnquadrado, indiceBitDestino, FLAG, 8);
    indiceBitDestino += 8;

    int contadorFlagIntermediaria = 0;
    // percorre todos os bytes de carga util adicionando as FLAGS e SCAPS
    // necessarios
    for (int i = 0; i < contadorBytesCargaUtilQuadro; i++) {
      int indiceInteiro = i / 4;
      int posicaoNoInteiro = i % 4;
      int umByte = (quadro[indiceInteiro] >> (24 - posicaoNoInteiro * 8)) & 0xFF; // extrai o byte(8bits) para comparar
                                                                                  // se precisa de Scape

      if (umByte == FLAG || umByte == SCAPE) {
        // Escreve o SCAPE (8 bits).
        ManipulacaoBits.escreverBits(quadroEnquadrado, indiceBitDestino, SCAPE, 8);
        indiceBitDestino += 8;

        // Escreve o byte original (8 bits).
        ManipulacaoBits.escreverBits(quadroEnquadrado, indiceBitDestino, umByte, 8);
        indiceBitDestino += 8;
      } else {
        // Escreve o byte normal (8 bits).
        ManipulacaoBits.escreverBits(quadroEnquadrado, indiceBitDestino, umByte, 8);
        indiceBitDestino += 8;
      } // fim do if/else

      contadorFlagIntermediaria++;
      if (contadorFlagIntermediaria == TAMANHO_SUBQUADRO_EM_BYTES) {
        // escreve a flag intermediaria
        ManipulacaoBits.escreverBits(quadroEnquadrado, indiceBitDestino, FLAG, 8);
        indiceBitDestino += 8;

        contadorFlagIntermediaria = 0;
      } // fim if

    } // fim for

    if (contadorFlagIntermediaria != 0) { // caso nao tenha acabado um multiplo de 4
      // Escreve a FLAG final (8 bits).
      ManipulacaoBits.escreverBits(quadroEnquadrado, indiceBitDestino, FLAG, 8);

    } // fim if

    return quadroEnquadrado;
  }// fim do metodo CamadaEnlaceDadosTransmissoraInsercaoDeBytes

  /**
   * metodo para realizar o enquadramento por insercao de bit
   * 
   * @param quadro quadro original a ser enquadrado
   * @return o quadro ja enquadrado
   */
  public int[] CamadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBits(int quadro[]) {

    final int FLAG = 0b01111110; // valor do byte de flag, equivale a 126 em decimal, em ASCII eh o '~'

    final int TAMANHO_SUBQUADRO_EM_BYTES = 4; // a cada 32 bits lidos, ou seja 4 bytes, adiciona a flag de divisao

    // calcular o tamanho da carga util do quadro
    int contadorBytesCargaUtil = 0;
    boolean fimDados = false;
    for (int inteiroAgrupado : quadro) {
      if (fimDados)
        break;
      for (int i = 0; i < 4; i++) {
        int umByte = (inteiroAgrupado >> (24 - i * 8)) & 0xFF;
        if (umByte == 0) {
          fimDados = true;
          break;
        } // fim if
        contadorBytesCargaUtil++;
      } // fim for
    } // fim for

    if (contadorBytesCargaUtil == 0) {
      System.out.println("MENSAGEM VAZIA");
      return new int[0];
    } // fim if

    // calcula o total de bits que o quadro enquadrado vai ter
    int contadorBitsEnquadrados = 8; // comeca com 8 bits para as FLAG de inicio
    int contadorBitsUm = 0; // contador para a sequencia de bits 1
    int contadorFlagIntermediaria = 0; // conta quando colocar a flag intermediaria no quadro

    // percorre apenas os bytes de carga util, bit a bit, para simular o stuffing
    for (int i = 0; i < contadorBytesCargaUtil; i++) {
      int indiceInteiro = i / 4;
      int posicaoNoInteiro = i % 4;
      int umByte = (quadro[indiceInteiro] >> (24 - posicaoNoInteiro * 8)) & 0xFF;

      for (int j = 7; j >= 0; j--) { // percorre os 8 bits do byte
        int bitAtual = (umByte >> j) & 1;
        contadorBitsEnquadrados++; // conta o bit de dado

        if (bitAtual == 1) {
          contadorBitsUm++;
          if (contadorBitsUm == 5) {
            contadorBitsEnquadrados++; // conta o bit '0' de stuffing
            contadorBitsUm = 0;
          }
        } else {
          contadorBitsUm = 0;
        } // fim do if / else
      } // fim do for

      contadorFlagIntermediaria++;

      if (contadorFlagIntermediaria == TAMANHO_SUBQUADRO_EM_BYTES) {
        contadorBitsEnquadrados += 8; // adiciona os 8 bits da flag
        contadorFlagIntermediaria = 0;
      }

    } // fim do for

    if (contadorFlagIntermediaria != 0) { // caso nao tenha finalizado com flag adiciona a final
      contadorBitsEnquadrados += 8;
    }

    // cria o array final com o tamanho exato calculado
    int tamanhoArrayFinal = (contadorBitsEnquadrados + 31) / 32;
    int[] quadroEnquadrado = new int[tamanhoArrayFinal];

    // indice para controlar a posicao do proximo bit a ser escrito
    int indiceBitDestino = 0;
    contadorFlagIntermediaria = 0;

    // escreve a FLAG inicial (8 bits)
    ManipulacaoBits.escreverBits(quadroEnquadrado, indiceBitDestino, FLAG, 8);
    indiceBitDestino += 8;

    contadorBitsUm = 0; // reseta o contador para a escrita
    // percorre os bytes de carga util novamente para preencher o quadro
    for (int i = 0; i < contadorBytesCargaUtil; i++) {
      int indiceInteiro = i / 4;
      int posicaoNoInteiro = i % 4;
      int umByte = (quadro[indiceInteiro] >> (24 - posicaoNoInteiro * 8)) & 0xFF;

      for (int j = 7; j >= 0; j--) {
        int bitAtual = (umByte >> j) & 1;

        // escreve o bit de dado atual
        ManipulacaoBits.escreverBits(quadroEnquadrado, indiceBitDestino, bitAtual, 1);
        indiceBitDestino++;

        if (bitAtual == 1) {
          contadorBitsUm++;
          if (contadorBitsUm == 5) {
            // escreve o bit '0' de stuffing
            ManipulacaoBits.escreverBits(quadroEnquadrado, indiceBitDestino, 0, 1);
            indiceBitDestino++;
            contadorBitsUm = 0;
          } // fim if
        } else {
          contadorBitsUm = 0;
        } // fim if /else
      } // fim for

      contadorFlagIntermediaria++;

      if (contadorFlagIntermediaria == TAMANHO_SUBQUADRO_EM_BYTES) { // escreve a flag intermediaria
        ManipulacaoBits.escreverBits(quadroEnquadrado, indiceBitDestino, FLAG, 8);
        indiceBitDestino += 8;
        contadorFlagIntermediaria = 0;
      } // fim if

    } // fim for

    // escreve a FLAG final (8 bits) se nao tiver acabado com flag
    if (contadorFlagIntermediaria != 0) {
      ManipulacaoBits.escreverBits(quadroEnquadrado, indiceBitDestino, FLAG, 8);
    } // fim if

    return quadroEnquadrado;
  }// fim do metodo CamadaEnlaceDadosTransmissoraInsercaoDeBits

  /**
   * passa o quadro para a proxima camada, violando a logica de responsabilidades
   * transferindo para a camada fisica a responsabilidade de enquadrar. Por isso
   * Violacao da Camada Fisica
   * 
   * @param quadro quadro a ser enquadrado
   * @return o mesmo quadro
   */
  public int[] CamadaEnlaceDadosTransmissoraEnquadramentoViolacaoDaCamadaFisica(int quadro[]) {

    // passa o quadro recebido para a camada fisica e ela se responsabiliza por
    // enquadrar com os sinais de violacao 11, no inicio e no fim
    return quadro;
  }// fim metodo CamadaEnlaceDadosTransmissoraEnquadramentoViolacaoDeCamadaFisica

  public int[] CamadaEnlaceDadosTransmissoraControleDeErroBitParidadePar(int quadro[]) {
    int totalBits = ManipulacaoBits.descobrirTotalDeBitsReais(quadro);
    if (totalBits == 0)
      return quadro; // Nao adiciona paridade a quadros vazios

    int contagemBitsUm = 0;
    // percorre o quadro e conta o numero de bits 1
    for (int i = 0; i < totalBits; i++) {
      if (ManipulacaoBits.lerBits(quadro, i, 1) == 1) {
        contagemBitsUm++;
      } // fim if
    } // fim for

    // se os bits 1 forem par entao adiciona 0 se for impar adiciona 1 para garantir
    // a paridade
    int bitParidade = (contagemBitsUm % 2 == 0) ? 0 : 1;

    int novoTotalBits = totalBits + 1;
    int novoTamanhoArray = (novoTotalBits + 31) / 32; // arredondamento de seguranca
    int[] quadroComParidade = new int[novoTamanhoArray];

    for (int i = 0; i < totalBits; i++) {
      int bit = ManipulacaoBits.lerBits(quadro, i, 1);
      ManipulacaoBits.escreverBits(quadroComParidade, i, bit, 1);
    } // fim do for

    ManipulacaoBits.escreverBits(quadroComParidade, totalBits, bitParidade, 1);

    // debug
    System.out
        .println("Controle de Erro (Par): " + contagemBitsUm + " bits '1'. Bit de paridade anexado: " + bitParidade);

    return quadroComParidade;
  }// fim do metodo CamadaEnlaceDadosTransmissoraControleDeErroBitParidadePar

  public int[] CamadaEnlaceDadosTransmissoraControleDeErroBitParidadeImpar(int quadro[]) {
    // algum codigo aqui
    return quadro;
  }// fim do metodo CamadaEnlaceDadosTransmissoraControleDeErroBitParidadeImpar

  public int[] CamadaEnlaceDadosTransmissoraControleDeErroCRC(int quadro[]) {
    // algum codigo aqui
    return quadro;
  }// fim do metodo CamadaEnlaceDadosTransmissoraControleDeErroCRC

  public int[] CamadaEnlaceDadosTransmissoraControleDeErroCodigoDeHamming(int quadro[]) {
    // algum codigo aqui
    return quadro;
  }// fim do metodo CamadaEnlaceDadosTransmissoraControleDeErroCodigoDeHamming

} // fim da classe