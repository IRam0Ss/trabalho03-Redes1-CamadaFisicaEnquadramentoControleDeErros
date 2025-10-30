package model;

import controller.ControlerTelaPrincipal;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import util.ManipulacaoBits;

public class CamadaEnlaceDadosReceptora {

  private CamadaAplicacaoReceptora camadaAplicacaoReceptora;
  private ControlerTelaPrincipal controlerTelaPrincipal;

  /**
   * construtor da classe
   * 
   * @param camadaAplicacaoReceptora referencia para a camada de aplicacao
   *                                 receptora
   * @param controlerTelaPrincipal   referencia para a interface grafica
   */
  public CamadaEnlaceDadosReceptora(CamadaAplicacaoReceptora camadaAplicacaoReceptora,
      ControlerTelaPrincipal controlerTelaPrincipal) {
    this.camadaAplicacaoReceptora = camadaAplicacaoReceptora;
    this.controlerTelaPrincipal = controlerTelaPrincipal;
  } // fim do construtor

  /**
   * metodo responsavel por receber o quadro da camada fisica e processa-lo
   * 
   * @param quadro quadro recebido da camada fisica com enquadramento e controle
   *               de erro incluidos
   */
  public void receberQuadro(int[] quadro) {

    int[] quadroVerificado = CamadaEnlaceDadosReceptoraControleDeErro(quadro); // verifica erros no quadro

    if (quadroVerificado == null) {
      // Se o quadro for invalido descarta
      System.out.println("Camada Enlace Receptora: ERRO DETECTADO. Quadro descartado.");

      // informar o usuario, da deteccao de erros
      Platform.runLater(() -> {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Deteccao de Erro");
        alert.setHeaderText("QUADRO CORROMPIDO!");
        alert.setContentText("A Camada de Enlace Receptora detectou um erro no quadro recebido e o descartou.");
        alert.show();
      });

      return; // sai do metodo sem processar o quadro
    } // fim if

    // se chegou aqui o quadro esta valido

    int[] quadroDesenquadrado = CamadaEnlaceDadosReceptoraEnquadramento(quadroVerificado); // desenquadra o quadro

    // CamadaEnlaceDadosReceptoraControleDeFluxo(quadroVerificado); // controla o
    // fluxo de dados

    System.out.println("Camada Enlace Receptora: Quadro valido. Enviando para a Aplicacao.");

    // chama proxima camada
    this.camadaAplicacaoReceptora.receberQuadro(quadroDesenquadrado); // envia o quadro para a proxima camada

    // this.camadaAplicacaoReceptora.receberQuadro(quadroDesenquadrado); // envia o
    // quadro para a proxima camada
  } // fim do metodo receberQuadro

  /**
   * metodo que escolhe o tipo de desenquadramento a ser aplicado na mensagem
   * 
   * @param quadro mensagem recebida da camada anterior com os bits enquadrados
   * @return o quadro ja desenquadrado
   */
  public int[] CamadaEnlaceDadosReceptoraEnquadramento(int quadro[]) {
    int tipoDeEnquadramento = this.controlerTelaPrincipal.opcaoEnquadramentoSelecionada();
    int[] quadroDesenquadrado = quadro;
    switch (tipoDeEnquadramento) {
      case 0: // contagem de caracteres
        quadroDesenquadrado = CamadaEnlaceDadosReceptoraEnquadramentoContagemDeCaracteres(quadro);
        break;
      case 1: // insercao de bytes
        quadroDesenquadrado = CamadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBytes(quadro);
        break;
      case 2: // insercao de bits
        quadroDesenquadrado = CamadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBits(quadro);
        break;
      case 3: // violacao da camada fisica
        quadroDesenquadrado = CamadaEnlaceDadosReceptoraEnquadramentoViolacaoDaCamadaFisica(quadro);
        break;
    }// fim do switch/case

    return quadroDesenquadrado; // retorna o quadro ja desenquadrado

  }// fim do metodo CamadaEnlaceDadosReceptoraEnquadramento

  public int[] CamadaEnlaceDadosReceptoraControleDeErro(int quadro[]) {
    int tipoDeControleDeErro = this.controlerTelaPrincipal.opcaoControleErroSelecionada();
    int[] quadroVerificado = null;
    switch (tipoDeControleDeErro) {
      case 0: // paridade par
        quadroVerificado = CamadaEnlaceDadosReceptoraControleDeErroBitDeParidadePar(quadro);
        break;
      case 1: // paridade impar
        quadroVerificado = CamadaEnlaceDadosReceptoraControleDeErroBitDeParidadeImpar(quadro);
        break;
      case 2: // CRC
        quadroVerificado = CamadaEnlaceDadosReceptoraControleDeErroCRC(quadro);
        break;
      case 3: // hamming
        quadroVerificado = CamadaEnlaceDadosReceptoraControleDeErroCodigoDeHamming(quadro);
        break;
    }// fim do switch/case
    return quadroVerificado;
  }// fim do metodo CamadaEnlaceDadosReceptoraControleDeErro

  public void CamadaEnlaceDadosReceptoraControleDeFluxo(int quadro[]) {
    // algum codigo aqui
  }// fim do metodo CamadaEnlaceDadosReceptoraControleDeFluxo

  /**
   * metodo para desenquadrar o quadro utilizando o metodo de contagem de
   * caracteres
   * 
   * @param quadro quadro recebido com os bits enquadrados
   * @return o quadro ja desenquadrado
   */
  public int[] CamadaEnlaceDadosReceptoraEnquadramentoContagemDeCaracteres(int quadro[]) {

    // usa o tamanho real do sinal para evitar ler lixo
    int maximoDeBitsNoQuadro = ManipulacaoBits.descobrirTotalDeBitsReais(quadro);
    if (maximoDeBitsNoQuadro == 0)
      return new int[0];

    // usa um buffer grande e depois apara para o tamanho exato
    int[] bufferTemporario = new int[quadro.length];
    int bitEscritaGlobal = 0;
    int bitLeituraGlobal = 0;

    while (bitLeituraGlobal < maximoDeBitsNoQuadro) { // loop enquanto tiver bits pra ler
      // garante que ha espaco para ler um cabecalho
      if (bitLeituraGlobal + 8 > maximoDeBitsNoQuadro)
        break;

      // le o cabecalho de 8 bits
      int contagem = ManipulacaoBits.lerBits(quadro, bitLeituraGlobal, 8);
      bitLeituraGlobal += 8;

      if (contagem == 0)
        break;

      // le a carga util
      int bitsCargaUtil = (contagem - 1) * 8; // calcula quantos bits serao de carga util

      // garante que a carga util nao ultrapassa o final do quadro
      if (bitLeituraGlobal + bitsCargaUtil > maximoDeBitsNoQuadro)
        break;

      int cargaUtil = ManipulacaoBits.lerBits(quadro, bitLeituraGlobal, bitsCargaUtil);
      bitLeituraGlobal += bitsCargaUtil;

      // escreve a carga util no buffer de saida
      ManipulacaoBits.escreverBits(bufferTemporario, bitEscritaGlobal, cargaUtil, bitsCargaUtil);
      bitEscritaGlobal += bitsCargaUtil;
    } // fim while

    // Cria o array final com o tamanho EXATO dos dados extraidos.
    int[] quadroDesenquadrado = new int[(bitEscritaGlobal + 31) / 32];
    for (int i = 0; i < bitEscritaGlobal; i++) {
      int bit = ManipulacaoBits.lerBits(bufferTemporario, i, 1);
      ManipulacaoBits.escreverBits(quadroDesenquadrado, i, bit, 1);
    } // fim for

    return quadroDesenquadrado;
  }// fim do metodo CamadaEnlaceDadosReceptoraContagemDeCaracteres

  /**
   * metodo para desenquadrar o quadro utilizando o metodo de insercao de bytes
   * 
   * @param quadro quadro recebido com os bits enquadrados
   * @return o quadro ja desenquadrado
   */
  public int[] CamadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBytes(int quadro[]) {

    final int FLAG = 0b01111110; // valor do byte de flag, o mesmo do transmissor
    final int SCAPE = 0b01111101; // valor do byte de escape, o mesmo do transmissor

    // primeiro, contamos o total de bytes validos recebidos
    int contadorBytesRecebidos = 0;
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
        contadorBytesRecebidos++;
      } // fim for
    } // fim do for

    // calcular quantos bytes de carga util vao existir apos desenquadramento
    int contadorBytesCargaUtil = 0;
    for (int i = 0; i < contadorBytesRecebidos; i++) {
      // extrai o byte da posicao atual para analise
      int indiceInteiro = i / 4;
      int posicaoNoInteiro = i % 4;
      int byteAtual = (quadro[indiceInteiro] >> (24 - posicaoNoInteiro * 8)) & 0xFF;

      if (byteAtual == FLAG) {
        // Flags nao contam como carga util
        continue;
      }

      if (byteAtual == SCAPE) {
        // O SCAPE eh ignorado, mas o proximo byte conta como carga util.
        // Avanca o 'i' para pular o proprio SCAPE na contagem.
        i++;
        contadorBytesCargaUtil++;
      } else {
        // Se nao for FLAG nem SCAPE, eh um byte de carga util
        contadorBytesCargaUtil++;
      } // fim if/ else
    } // fim for

    // cria o array final com o tamanho exato que foi calculado
    int tamanhoQuadroDesenquadrado = (contadorBytesCargaUtil + 3) / 4;
    int[] quadroDesenquadrado = new int[tamanhoQuadroDesenquadrado];

    // indice para controlar em qual bit do quadro final o proximo byte sera escrito
    int indiceBitDestino = 0;

    // percorre os bytes recebidos novamente, desta vez para extrair e escrever a
    // carga util
    for (int i = 0; i < contadorBytesRecebidos; i++) {
      // extrai o byte da posicao atual
      int indiceInteiro = i / 4;
      int posicaoNoInteiro = i % 4;
      int byteAtual = (quadro[indiceInteiro] >> (24 - posicaoNoInteiro * 8)) & 0xFF;

      if (byteAtual == FLAG) {
        // se o byte for flag, ignora
        continue;
      } // fim if

      if (byteAtual == SCAPE) {
        // se o byte eh um scape, avanca para o proximo byte
        i++;
        // extrai o byte de dados que vem apos o SCAPE
        indiceInteiro = i / 4;
        posicaoNoInteiro = i % 4;
        int byteDeDados = (quadro[indiceInteiro] >> (24 - posicaoNoInteiro * 8)) & 0xFF;

        // escreve o byte de dados no quadro final
        ManipulacaoBits.escreverBits(quadroDesenquadrado, indiceBitDestino, byteDeDados, 8);
        indiceBitDestino += 8; // avanca o ponteiro de escrita em 8 bits
      } else {
        // se passou as verificacoes, o byte eh carga util
        // escreve o byte de dados no quadro final
        ManipulacaoBits.escreverBits(quadroDesenquadrado, indiceBitDestino, byteAtual, 8);
        indiceBitDestino += 8; // avanca o ponteiro de escrita em 8 bits
      } // fim if/else
    } // fim for

    return quadroDesenquadrado;
  }// fim do metodo CamadaEnlaceDadosReceptoraInsercaoDeBytes

  /**
   * metodo para desenquadrar o quadro utilizando a insercao de bits
   * 
   * @param quadro quadro recebido enquadrado
   * @return o quadro ja desenquadrado
   */
  public int[] CamadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBits(int quadro[]) {

    final int FLAG = 0b01111110; // O mesmo valor de flag do transmissor

    int tamanhoMaximoEstimado = quadro.length * 32; // define o valor maximo possivel
    int[] bufferTemporario = new int[tamanhoMaximoEstimado];
    int indiceBitDestino = 0;
    int contadorBitsUm = 0;
    boolean inicioQuadro = false;

    // itera por todos os bits do quadro recebido
    for (int i = 0; i < tamanhoMaximoEstimado; i++) {
      // leitura de um bit do quadro de entrada
      int indiceInteiro = i / 32;
      int posicaoNoInteiro = 31 - (i % 32);
      int bitAtual = (quadro[indiceInteiro] >> posicaoNoInteiro) & 1;

      // dtecta inicio de quaro descarta flag
      if (!inicioQuadro) {
        // procura pela primeira flag para começar o processamento
        if (i + 7 < tamanhoMaximoEstimado) {
          int possivelFlag = ManipulacaoBits.lerBits(quadro, i, 8);
          if (possivelFlag == FLAG) {
            inicioQuadro = true;
            i += 7; // Pula os bits da flag que já foram lidos
            continue; // Volta ao início do loop
          } // fim if
        } // fim if
        continue; // Se não for uma flag, continua procurando
      } // fim if

      // comeca a processar os dados
      // verifica se os bits formam flag
      if (i + 7 < tamanhoMaximoEstimado) {
        int possivelFlag = ManipulacaoBits.lerBits(quadro, i, 8);
        if (possivelFlag == FLAG) {
          // Encontrou uma flag, seja intermediária ou final
          i += 7; // Pula os bits da flag
          contadorBitsUm = 0; // Reseta o contador de '1's
          continue; // Continua para o próximo bit após a flag
        } // fim if
      } // fi if

      if (contadorBitsUm == 5) {
        // bit adicionado pelo stuffing eh descartado
        if (bitAtual == 0) {
          contadorBitsUm = 0;
          continue; // descarta o bit '0' e continua o loop
        } // fim if
      } // fimif

      // escreve o bit de dado no buffer temporario
      ManipulacaoBits.escreverBits(bufferTemporario, indiceBitDestino, bitAtual, 1);
      indiceBitDestino++;

      // atualiza o contador de bits '1'
      if (bitAtual == 1) {
        contadorBitsUm++;
      } else {
        contadorBitsUm = 0;
      } // fim if/else
    } // fim for

    // cria array final com tamanho exato
    int tamanhoArrayFinal = (indiceBitDestino + 31) / 32;
    int[] quadroDesenquadrado = new int[tamanhoArrayFinal];

    // Copia os bits do buffer temporário para o array final
    for (int i = 0; i < indiceBitDestino; i++) {
      int bit = ManipulacaoBits.lerBits(bufferTemporario, i, 1);
      ManipulacaoBits.escreverBits(quadroDesenquadrado, i, bit, 1);
    } // fim for

    return quadroDesenquadrado;
  } // fim do metodo CamadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBits

  /**
   * Passa somente o quadro pra proxima camada, pois a responsabilidade de
   * dosenquadramento foi violada e colocada pra camada fisica
   * 
   * @param quadro o quadro ja desenquadrado
   * @return o mesmo quadro
   */
  public int[] CamadaEnlaceDadosReceptoraEnquadramentoViolacaoDaCamadaFisica(int quadro[]) {

    return quadro;
  } // fim CamadaEnlaceDadosReceptoraEnquadramentoViolacaoDaCamadaFisica

  /**
   * metodo para verificar erros no quadro utilizando o metodo de bit de paridade
   * par
   * 
   * @param quadro quadro recebido
   * @return quadro verificado, e removido os bits de controle, ou nulo se
   *         detectar erro
   */
  public int[] CamadaEnlaceDadosReceptoraControleDeErroBitDeParidadePar(int quadro[]) {

    int totalBitsRecebidos = ManipulacaoBits.descobrirTotalDeBitsReais(quadro);// (incluindo o padding)

    if (totalBitsRecebidos == 0) {
      return quadro;
    }

    int totalBitsReaisVerificar = totalBitsRecebidos - 7; // remove os bits de padding

    // conta o numero de bits 1
    int contadorUns = 0;
    for (int i = 0; i < totalBitsReaisVerificar; i++) {
      int bitAtual = ManipulacaoBits.lerBits(quadro, i, 1);
      if (bitAtual == 1) {
        contadorUns++;
      }
    } // fim for

    if (contadorUns % 2 != 0) {
      // se o numero de uns for impar, entao houve erro
      return null; // retorna nulo para indicar erro
    }

    // nao teve erro

    int totalBitsSemControle = totalBitsReaisVerificar - 1; // remove o bit de paridade

    // Se o quadro so tinha o bit de paridade (ou estava vazio), retorna vazio
    if (totalBitsSemControle <= 0) {
      return new int[0];
    }

    int tamanhoArrayFinal = (totalBitsSemControle + 31) / 32;
    int[] quadroVerificado = new int[tamanhoArrayFinal];

    // escreve no quadro sem o controle os bits uteis
    for (int i = 0; i < totalBitsSemControle; i++) {
      int bitAtual = ManipulacaoBits.lerBits(quadro, i, 1);
      ManipulacaoBits.escreverBits(quadroVerificado, i, bitAtual, 1);
    } // fim for

    return quadroVerificado;
  }// fim do metodo CamadaEnlaceDadosReceptoraControleDeErroBitDeParidadePar

  /**
   * metodo para verificar erros no quadro utilizando o metodo de bit de paridade
   * impar
   * 
   * @param quadro quadro recebido
   * @return quadro verificado, e removido os bits de controle, ou nulo se
   *         detectar erro
   */
  public int[] CamadaEnlaceDadosReceptoraControleDeErroBitDeParidadeImpar(int quadro[]) {

    int totalBitsRecebidos = ManipulacaoBits.descobrirTotalDeBitsReais(quadro);// (incluindo o padding)

    if (totalBitsRecebidos == 0) {
      return quadro;
    }

    int totalBitsReaisVerificar = totalBitsRecebidos - 7; // remove os bits de padding

    // conta o numero de bits 1
    int contadorUns = 0;
    for (int i = 0; i < totalBitsReaisVerificar; i++) {
      int bitAtual = ManipulacaoBits.lerBits(quadro, i, 1);
      if (bitAtual == 1) {
        contadorUns++;
      }
    } // fim for

    if (contadorUns % 2 == 0) {
      // se o numero de uns for par, entao houve erro
      return null; // retorna nulo para indicar erro
    }

    // nao teve erro
    int totalBitsSemControle = totalBitsReaisVerificar - 1; // remove o bit de paridade

    // Se o quadro so tinha o bit de paridade (ou estava vazio), retorna vazio
    if (totalBitsSemControle <= 0) {
      return new int[0];
    }

    int tamanhoArrayFinal = (totalBitsSemControle + 31) / 32;
    int[] quadroVerificado = new int[tamanhoArrayFinal];

    // escreve no quadro sem o controle os bits uteis
    for (int i = 0; i < totalBitsSemControle; i++) {
      int bitAtual = ManipulacaoBits.lerBits(quadro, i, 1);
      ManipulacaoBits.escreverBits(quadroVerificado, i, bitAtual, 1);
    } // fim for

    return quadroVerificado;
  }// fim do metodo CamadaEnlaceDadosReceptoraControleDeErroBitDeParidadeImpar

  public int[] CamadaEnlaceDadosReceptoraControleDeErroCRC(int quadro[]) {

    int totalBitsRecebidos = ManipulacaoBits.descobrirTotalDeBitsReais(quadro);// (incluindo o padding)

    final int POLINOMIO_GERADOR = 0x04C11DB7;
    final int VALOR_INICIAL = 0xFFFFFFFF;
    final int VALOR_FINAL_XOR = 0xFFFFFFFF;

    int registradorCRC = VALOR_INICIAL;

    if (totalBitsRecebidos < 32) { // se tem menos de 32 bits, nao tem como ter CRC
      return null; // quadro corrompido
    } // fim if

    int totalBitsReaisVerificar = totalBitsRecebidos - 32; // remove os bits de padding
    int crcRecebido = ManipulacaoBits.lerBits(quadro, totalBitsReaisVerificar, 32); // le o CRC recebido

    // calcula o CRC dos dados recebidos
    for (int i = 0; i < totalBitsReaisVerificar; i++) {
      int bitAtual = ManipulacaoBits.lerBits(quadro, i, 1);
      int bitMaisSignificativo = (registradorCRC >> 31) & 1; // pega o bit mais significativo
      int xorBit = bitMaisSignificativo ^ bitAtual; // calcula o bit de XOR

      registradorCRC = registradorCRC << 1; // desloca o registrador para a esquerda

      if (xorBit == 1) { // aplica o polinomio gerador
        registradorCRC = registradorCRC ^ POLINOMIO_GERADOR;
      } // fim if

    } // fim for

    // processamento dos 32 bits de 0 adicionais
    for (int i = 0; i < 32; i++) {

      int bitAtual = 0; // bits adicionais sao 0
      int bitMaisSignificativo = (registradorCRC >> 31) & 1; // obtém o bit mais significativo do registrador CRC

      int xorBit = bitMaisSignificativo ^ bitAtual; // calcula o bit de XOR

      registradorCRC = registradorCRC << 1; // desloca o registrador para a esquerda

      if (xorBit == 1) {
        registradorCRC = registradorCRC ^ POLINOMIO_GERADOR; // aplica o polinomio gerador
      } // fim do if

    } // fim do for

    int crcCalculado = registradorCRC ^ VALOR_FINAL_XOR; // aplica o xor final

    if (crcRecebido != crcCalculado) { // se os crcs forem diferentes ocorreu erro
      System.out.println("Erro de CRC! Recebido: " + Integer.toHexString(crcRecebido) +
          ", Calculado: " + Integer.toHexString(crcCalculado));
      return null; // Descarta o quadro
    }

    // se nao foi corrompido entao
    int novoTamanhoArray = (totalBitsReaisVerificar + 31) / 32;
    int[] quadroVerificado = new int[novoTamanhoArray];

    for(int i = 0; i < totalBitsReaisVerificar; i++){
      int bit = ManipulacaoBits.lerBits(quadro, i, 1);
      ManipulacaoBits.escreverBits(quadroVerificado, i, bit, 1);
    } // fim for

    return quadroVerificado;
  }// fim do metodo CamadaEnlaceDadosReceptoraControleDeErroCRC

  public int[] CamadaEnlaceDadosReceptoraControleDeErroCodigoDeHamming(int quadro[]) {
    // algum codigo aqui
    return quadro;
  }// fim do metodo CamadaEnlaceDadosReceptoraControleDeErroCodigoDeHamming

}// fim da classe CamadaEnlaceDadosReceptora
